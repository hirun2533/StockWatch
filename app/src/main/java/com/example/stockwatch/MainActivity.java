package com.example.stockwatch;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;


public class MainActivity extends AppCompatActivity
        implements View.OnClickListener,View.OnLongClickListener {


    private static final String TAG = "Main";
    private ArrayList<Stock> stockList = new ArrayList<>();
    private StockDownloader StockDownloader;
    private String DataSymbol;
    private String Compname;
    private RecyclerView recyclerView;
    private RecyclerAdapter rAdapter;
    private SwipeRefreshLayout swiper;
    private ArrayList<String> stockArr = new ArrayList<>();
    private NameDownloader NameDownloader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.Recycler);
        rAdapter = new RecyclerAdapter(stockList, this);
        recyclerView.setAdapter(rAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        doStock();
        NetworkCheck();

        ArrayList<Stock> stockTemp = doStock();
        boolean checkNet = NetworkCheck();
        if (!checkNet) {
            for (Stock stock : stockTemp) {
                stock.setPrice(0.0);
                stock.setChange(0.0);
                stock.setPercent(0.0);
                stockList.add(stock);
            }
            Collections.sort(stockList);
            rAdapter.notifyDataSetChanged();
        } else {
            for (Stock s : stockTemp) {
                AsyncFinanceLoad(String.format("%s - %s", s.getSymbol(), s.getCompany()));
            }
        }
        swiper = findViewById(R.id.swiper);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if(!NetworkCheck()) {
                    AlertDialog.Builder build = new AlertDialog.Builder(MainActivity.this);
                    build.setTitle("No Network Connection");
                    build.setMessage("Stocks Cannot Be Added Without A Network Connection");
                    AlertDialog dialog = build.create();
                    dialog.show();
                    swiper.setRefreshing(false);
                } else {
                    doRefresh();
                    doStock();
                }


            }
        });
        NameDownloader = new NameDownloader(this);
        NameDownloader.execute();
    }

    @Override
    protected void onPause() {

        super.onPause();

        StockPut();

    }


    public void AsyncFinanceLoad(String data) {
        Log.d(TAG, "Loading??");
        String[] nameSym = data.split("-");
        DataSymbol = nameSym[0].trim();
        Compname = nameSym[1].trim();

        StockDownloader = (StockDownloader) new StockDownloader(MainActivity.this).execute(DataSymbol, Compname);
        Log.d(TAG,"name---->" + Compname.toString());
        Log.d(TAG,"Sybol---->" + DataSymbol.toString());

    }



    private boolean NetworkCheck() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            Toast.makeText(this, "Cannot access ConnectivityManager", Toast.LENGTH_SHORT).show();
            return false;
        }

        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnected()) {
            Log.d(TAG, "connected: Network is connected!!! ");

            return true;
        } else {
            Log.d(TAG, "connected: Network is not connected!!! ");
            return false;
        }

    }

    private void doRefresh() {
        if (NetworkCheck() == true) {
            Toast.makeText(this, "Swiping All data", Toast.LENGTH_SHORT).show();
            stockList.clear();
            ArrayList<Stock> doTempStock = doStock();
            for (Stock stock : doTempStock) {
                AsyncFinanceLoad(String.format("%s - %s", stock.getSymbol(), stock.getCompany()));
            }
        }
        rAdapter.notifyDataSetChanged();
        swiper.setRefreshing(false);

    }



    @Override
    public void onClick(View v) {

        int pos = recyclerView.getChildAdapterPosition(v);
        Stock select = stockList.get(pos);
        String symbol = select.getSymbol();
        String url = "http://www.marketwatch.com/investing/stock/" + symbol;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.add:

                addStock();

                return true;

            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }


    public ArrayList<Stock> doStock() {

        stockList.clear();
        ArrayList<Stock> doTempStock = new ArrayList<>();

        try {
            InputStream inS = openFileInput("Note.json");

            if (inS != null) {
                InputStreamReader in = new InputStreamReader(inS);
                BufferedReader read = new BufferedReader(in);

                String getString = "";
                StringBuilder sb = new StringBuilder();

                while ((getString = read.readLine()) != null) {
                    sb.append(getString);
                }
                String jsonText = sb.toString();

                try {
                    JSONArray jsonArray = new JSONArray(jsonText);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String symbol = jsonObject.getString("SYMBOL");
                        String latestPrice = jsonObject.getString("PRICE");
                        String change = jsonObject.getString("CHANGE");
                        String changePercent = jsonObject.getString("PERCENT");
                        String companyName = jsonObject.getString("COMPANY");
                        double lastprice = Double.parseDouble(latestPrice);
                        double changeprice = Double.parseDouble(change);
                        double percent = Double.parseDouble(changePercent);
                        Stock stock = new Stock(symbol, companyName, lastprice, changeprice, percent);
                        doTempStock.add(stock);
                        Collections.sort(doTempStock);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return doTempStock;
    }

    private void StockPut() {


        JSONArray jsonArray = new JSONArray();
        for (Stock s : stockList) {
            try {
                JSONObject nameJSON = new JSONObject();
                nameJSON.put("SYMBOL", s.getSymbol());
                nameJSON.put("PRICE", s.getPrice());
                nameJSON.put("CHANGE", s.getChange());
                nameJSON.put("PERCENT", s.getPercent());
                nameJSON.put("COMPANY", s.getCompany());

                jsonArray.put(nameJSON);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        String jsonText = jsonArray.toString();


        try {
            OutputStreamWriter out = new OutputStreamWriter((
                    openFileOutput("Note.json", Context.MODE_PRIVATE)));

            out.write(jsonText);
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private boolean checkSymbol(String symbol) {

        int i = 0;
        while (i < stockList.size()) {
            if (stockList.get(i).getSymbol().equals(symbol))
                return true;
            i++;
        }

        return false;
    }

    public void StockDialogSelect(ArrayList<String> listStock) {


        final CharSequence[] stockArr = new CharSequence[listStock.size()];

        for (int i = 0; i < listStock.size(); i++) {
            stockArr[i] = listStock.get(i);
        }

        AlertDialog.Builder build = new AlertDialog.Builder(this);
        build.setTitle("Make a selection");

        build.setItems(stockArr, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String tempstring = stockArr[which].toString();
                String[] string = tempstring.split("-");
                String temp = string[0];
                if (stockList.size() == 0) {
                    new StockDownloader(MainActivity.this).execute(temp);
                } else {

                    if (checkSymbol(temp)) {

                        AlertDialog.Builder build = new AlertDialog.Builder(MainActivity.this);
                        build.setIcon(R.drawable.baseline_warning_black_48);
                        build.setTitle("Duplicate Stock");
                        build.setMessage("Stock Symbol " + temp + " is already displayed");
                        AlertDialog dialogtemp = build.create();
                        dialogtemp.show();

                    } else
                        new StockDownloader(MainActivity.this).execute(temp);
                }

            }
        });
        build.setNegativeButton("NEVERMIND", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


            }
        });

        AlertDialog dialog = build.create();
        dialog.show();
    }



    private void addStock() {


        if (!NetworkCheck()) {
            AlertDialog.Builder build = new AlertDialog.Builder(MainActivity.this);
            build.setTitle("No Network Connection");
            build.setMessage("Stocks Cannot Be Added Without A Network Connection");
            AlertDialog dialog = build.create();
            dialog.show();

        } else {
            AlertDialog.Builder build = new AlertDialog.Builder(this);
            final EditText text = new EditText(this);


            text.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            text.setGravity(Gravity.CENTER_HORIZONTAL);
            build.setView(text);

            build.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {
                    Toast.makeText(MainActivity.this, "Symbol was added", Toast.LENGTH_SHORT).show();

                    Log.d("addStock", "Add Ok!!!!!");

                    String stock = text.getText().toString().trim();

                    final ArrayList<String> stockSelect = NameDownloader.matchName(stock);

                    if (stockSelect.size() == 0) {

                        AlertDialog.Builder build = new AlertDialog.Builder(MainActivity.this);
                        build.setTitle("Symbol Not Found: " + stock);
                        build.setMessage("Data for stock symbol");
                        AlertDialog dialg = build.create();
                        dialg.show();


                    } else if (stockSelect.size() == 1 && stockList.size() != 0) {

                        for (int i = 0; i < stockList.size(); i++) {
                            if (stockList.get(i).getSymbol().equals(stockSelect.get(0))) {
                                AlertDialog.Builder build = new AlertDialog.Builder(MainActivity.this);
                                build.setIcon(R.drawable.baseline_warning_black_48);
                                build.setTitle("Duplicate Stock");
                                build.setMessage("Stock Symbol " + stock + " is already displayed");
                                AlertDialog dialogtemp = build.create();
                                dialogtemp.show();
                            } else StockDialogSelect(stockSelect);
                        }


                    } else {
                        StockDialogSelect(stockSelect);
                        stockArr.add(stock);
                    }
                    stockSelect.clear();

                }
            });
            build.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {


                public void onClick(DialogInterface dialog, int id) {
                    Toast.makeText(MainActivity.this, "Nothing added!", Toast.LENGTH_SHORT).show();

                }
            });

            build.setTitle("Stock Selection");
            build.setMessage("Please enter a Stock Symbol:");


            AlertDialog dialog = build.create();
            dialog.show();


        }
    }

    public void updateData(Stock stock) {

        stockList.add(stock);
        Collections.sort(stockList);
        StockPut();
        rAdapter.notifyDataSetChanged();

    }


    @Override
    public boolean onLongClick(View v) {
        int pos = recyclerView.getChildAdapterPosition(v);
        final Stock select = stockList.get(pos);

        final String Symbol = select.getSymbol();
        AlertDialog.Builder build = new AlertDialog.Builder(this);
        build.setIcon(R.drawable.baseline_delete_forever_black_48);
        build.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {


            @Override
            public void onClick(DialogInterface dialog, int id) {

                for (int i = 0; i < stockList.size(); i++) {
                    if (Symbol.equals(stockList.get(i).getSymbol())) {
                        stockList.remove(i);
                        rAdapter.notifyDataSetChanged();

                    }
                }
            }
        });

        build.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        build.setTitle("Delete Stock");
        build.setMessage("Delete Stock Symbol '" + Symbol + "'?");
        AlertDialog alert = build.create();
        alert.show();

        return true;

    }

}
