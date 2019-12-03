package com.example.stockwatch;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class StockDownloader extends AsyncTask<String, Integer, String> {


    private static final String TAG = "StockDownloader";
    @SuppressLint("StaticFieldLeak")
    private MainActivity mainActivity;
    private static final String stockURL = "https://cloud.iexapis.com/stable/stock/";
    private static final String quote = "/quote?token=";
    private static final String apiKey = "sk_69338620102c4e7a81bce0d055b5ac50";

    private static Stock stock = new Stock();


    StockDownloader(MainActivity mainActivity)  {

        this.mainActivity = mainActivity;
    }


    @Override
    protected String doInBackground(String... params) {


        String apiEndPoint = params[0];
        Uri.Builder buildURL = Uri.parse(stockURL).buildUpon();
        String end = apiEndPoint + quote + apiKey;
        buildURL.appendEncodedPath(end);
        String urlToUse = buildURL.build().toString();

        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

            Log.d(TAG, "doInBackground: " + sb.toString());

        } catch (Exception e) {
            Log.e(TAG, "doInBackground: ", e);
            return null;
        }

        parseJSON(sb.toString());

        return sb.toString();

    }






    private Stock  parseJSON(String s) {

        Stock stock = null;

        try {
            JSONObject jsonObj = new JSONObject(s);

            String Symbol = jsonObj.getString("symbol");
            String Company = jsonObj.getString("companyName");

            String pr = jsonObj.getString("latestPrice");
            Double price = 0.0;
            if(pr != null && !pr.trim().equals("null"))
                price = Double.parseDouble(pr);

            String ce = jsonObj.getString("change");
            Double change = 0.0;
            if(ce != null && !ce.trim().equals("null"))
                change = Double.parseDouble(ce);

            String pc = jsonObj.getString("changePercent");
            Double Percent = 0.0;
            if(pc != null && !pc.trim().equals("null"))
                Percent = Double.parseDouble(pc);


            stock = new Stock(Symbol,Company,price,change,Percent);



        } catch (Exception e) {
            Log.d(TAG, "parseJSON: " + e.getMessage());
            e.printStackTrace();
        }
        return stock;
    }

    @Override
    protected void onPostExecute(String s) {
        Stock stock = parseJSON(s);

        if(stock !=null){
            mainActivity.updateData(stock);

        }

    }

}
