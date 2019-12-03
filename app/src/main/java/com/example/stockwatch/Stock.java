package com.example.stockwatch;

public class Stock implements Comparable<Stock> {


    private String Company;
    private String Symbol;
    public double Price;
    public double Percent;
    public double Change;


    @Override
    public String toString() {
        return "Stock{" +
                "Symbol='" + Symbol + '\'' +
                ", Company='" + Company + '\'' +
                ", Price=" + Price +
                ", Percent=" + Percent +
                ", Change=" + Change +
                '}';
    }

    public Stock(String Symbol, String Company,  double Price, double Percent, double Change) {

        this.Symbol = Symbol;
        this.Company = Company;
        this.Percent = Percent;
        this.Price = Price;
        this.Change = Change;

    }

    public Stock() {
    }

    public String getCompany() {
        return Company;
    }

    public void setCompany(String company) {
        Company = company;
    }

    public String getSymbol() {
        return Symbol;
    }

    public void setSymbol(String symbol) {
        Symbol = symbol;
    }

    public double getPrice() {
        return Price;
    }

    public void setPrice(double price) {
        Price = price;
    }

    public double getPercent() {
        return Percent;
    }

    public void setPercent(double percent) {
        Percent = percent;
    }

    public double getChange() { return Change; }

    public void setChange(double change) { Change = change; }


    @Override
    public int compareTo(Stock s) {

        return this.getSymbol().compareTo(s.getSymbol());
    }
}