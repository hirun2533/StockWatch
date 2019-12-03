package com.example.stockwatch;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class NameDownloader extends AsyncTask<String, Void, String> {


    private static final String TAG = "NameDownloader";

    @SuppressLint("StaticFieldLeak")
    private MainActivity mainActivity;
    private static HashMap<String, String> StockHash = new HashMap<>();
    private static final String stockURL = "https://api.iextrading.com/1.0/ref-data/symbols";

    private static final    ArrayList<String> matchList = new ArrayList<>();

    NameDownloader(MainActivity mainActivity) {

        this.mainActivity = mainActivity;
    }

    @Override
    protected String doInBackground(String... params) {


        Uri dataUri = Uri.parse(stockURL);
        String urlToUse = dataUri.toString();

        Log.d(TAG, "doInBackground1: " + urlToUse);

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

            Log.d(TAG, "doInBackground2: " + sb.toString());

        } catch (Exception e) {
            Log.e(TAG, "doInBackground: ", e);
            return null;
        }

        parseJSON(sb.toString());


        return sb.toString();
    }


    private HashMap<String, String> parseJSON(String s) {

        try {
            JSONArray jObjMain = new JSONArray(s);

            for (int i = 0; i < jObjMain.length(); i++) {
                JSONObject JFirm = (JSONObject) jObjMain.get(i);
                String symbol = JFirm.getString("symbol");
                String name = JFirm.getString("name");
                StockHash.put(symbol, name);
            }
            return StockHash;
        } catch (Exception e) {
            Log.d(TAG, "parseJSON: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


    public static ArrayList<String> matchName (String keyword){

        for(Map.Entry mapElement : StockHash.entrySet()){
            String symbol = (String)mapElement.getKey();
            String name = (String)mapElement.getValue();

            if(symbol.contains(keyword) || name.contains(keyword)){
                matchList.add(symbol +  "-" + name);
            }

        }

        return matchList;

    }



    @Override
    protected void onPostExecute(String s) {

            Toast.makeText(mainActivity, "Loaded " , Toast.LENGTH_SHORT).show();

        }

    }


