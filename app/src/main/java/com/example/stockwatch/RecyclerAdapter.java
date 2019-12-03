package com.example.stockwatch;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerHolder> {

    private static final String TAG = "RecyclerAdapter";
    private ArrayList<Stock> stockList;
    private MainActivity mainAct;

    RecyclerAdapter(ArrayList<Stock> stock, MainActivity ma){
        this.stockList = stock;
        mainAct = ma;
    }

    @NonNull
    @Override
    public RecyclerHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: MAKING NEW");
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row_item, parent, false);

        itemView.setOnClickListener(mainAct);
        itemView.setOnLongClickListener(mainAct);

        return new RecyclerHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerHolder holder, int position) {
        Stock stock = stockList.get(position);

        holder.Symbol.setText(stock.getSymbol());
        holder.Company.setText(stock.getCompany());
        holder.Price.setText(String.format("%.2f",stock.getPrice()));
        holder.Percent.setText(String.format("%.2f (%.2f%%)", stock.getChange(), stock.getPercent()));

        if(stock.getChange() >= 0) {

            holder.Symbol.setTextColor(Color.GREEN);
            holder.Company.setTextColor(Color.GREEN);
            holder.Price.setTextColor(Color.GREEN);
            holder.Percent.setTextColor(Color.GREEN);
            holder.Percent.setText(String.format("▲ %.2f (%.2f%%)", stock.getChange(), stock.getPercent()));

        }
        else{
            holder.Symbol.setTextColor(Color.RED);
            holder.Company.setTextColor(Color.RED);
            holder.Price.setTextColor(Color.RED);
            holder.Percent.setTextColor(Color.RED);
            holder.Percent.setText(String.format("▼ %.2f (%.2f%%)", stock.getChange(), stock.getPercent()));

        }

    }

    @Override
    public int getItemCount() {

        return stockList.size();
    }
}


