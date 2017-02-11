package com.sameenabhatia.ilovezappos;

import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.sameenabhatia.ilovezappos.databinding.ProductBinding;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    List<Product> productArray = new ArrayList<>();
    ListView lv;
    ProductAdapter adapter;
    EditText etSearch;
    FloatingActionButton fab;
    boolean flag = false;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etSearch = (EditText) findViewById(R.id.etSearch);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        lv = (ListView) findViewById(R.id.lv);
        adapter = new ProductAdapter();
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                final String name = productArray.get(position).brandName;
                flag=true;
                final Animation animation = new AlphaAnimation(1, 0);
                animation.setDuration(100);
                animation.setInterpolator(new LinearInterpolator());
                animation.setRepeatCount(Animation.INFINITE);
                animation.setRepeatMode(Animation.REVERSE);
                fab.startAnimation(animation);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        if(flag)
                        {
                            view.clearAnimation();
                            flag = false;
                            Toast.makeText(MainActivity.this, name + " added to cart", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(MainActivity.this, "Please click on an item to add it to the cart.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
    public static class Product
    {
        String brandName, productName,discount,price;
             Bitmap image;

        public Product(String brandName, String productName, String discount, String price, Bitmap image)
        {
            this.brandName = brandName;
            this.productName = productName;
            this.discount = discount;
            this.price = price;
            this.image = image;
        }
        public String getBrandName()
        {
            return this.brandName;
        }
        public String getProductName()
        {
            return this.productName;
        }
        public String getDiscount()
        {
            return this.discount;
        }
        public String getPrice()
        {
            return this.price;
        }
        public Bitmap getImage()
        {
            return this.image;
        }
        @BindingAdapter("bind:imageBitmap")
        public static void loadImage(ImageView view, Bitmap bitmap)
        {
            view.setImageBitmap(bitmap);
        }
    }
    public void search(View view)
    {
        productArray.clear();
        adapter.notifyDataSetChanged();
        String searchItem = etSearch.getText().toString();

        new JSONResult().execute(searchItem);
    }
    private class JSONResult extends AsyncTask<String,Void,String>
    {
        @Override
        protected String doInBackground(String... params)
        {
            HttpURLConnection urlConnection = null;
            try
            {
                String searchUrl = "https://api.zappos.com/Search?term="+params[0]+"&key=b743e26728e16b81da139182bb2094357c31d331";
                URL url = new URL(searchUrl);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 );
                urlConnection.setConnectTimeout(15000 );
                urlConnection.setDoOutput(true);
                urlConnection.connect();

                BufferedReader br=new BufferedReader(new InputStreamReader(url.openStream()));

                char[] buffer = new char[1024];

                String jsonString = new String();

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null)
                {
                    sb.append(line+"\n");
                }
                br.close();

                jsonString = sb.toString();

                try
                {
                    JSONObject json = new JSONObject(jsonString);
                    JSONArray results = json.getJSONArray("results");
                    for (int i = 0; i < results.length(); i++)
                    {
                        URL urll = new URL(results.getJSONObject(i).getString("thumbnailImageUrl"));
                        Bitmap bmp = BitmapFactory.decodeStream(urll.openConnection().getInputStream());

                        productArray.add(new Product(results.getJSONObject(i).getString("brandName"),
                                results.getJSONObject(i).getString("productName"),
                                results.getJSONObject(i).getString("percentOff"),
                                results.getJSONObject(i).getString("price"),
                                bmp));
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                return null;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }
        protected void onPostExecute(String result)
        {
            adapter.notifyDataSetChanged();
        }

    }
    class ProductAdapter extends BaseAdapter
    {
        @Override
        public int getCount()
        {
            return productArray.size();
        }

        @Override
        public Object getItem(int position)
        {
            return productArray.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View singleView, ViewGroup parent)
        {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            ProductBinding binding = DataBindingUtil.inflate(inflater,R.layout.single_list_item,parent,false);
            Product pro = productArray.get(position);
            binding.setProduct(pro);

            return binding.getRoot();
        }
    }
}
