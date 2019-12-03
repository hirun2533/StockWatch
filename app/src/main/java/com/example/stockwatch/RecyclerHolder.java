package com.example.stockwatch;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class RecyclerHolder extends RecyclerView.ViewHolder {

    TextView Symbol;
    TextView Price;
    TextView Percent;
    TextView Company;

    RecyclerHolder(View view){
        super(view);
        Symbol = view.findViewById(R.id.Symbol);
        Price = view.findViewById(R.id.Price);
        Percent = view.findViewById(R.id.Percent);
        Company = view.findViewById(R.id.Company);


    }
}
