package kz.gereski.m.homebank.domain;


import java.util.Date;

public class Product {
    public long id = 0;
    public Date date = new Date();
    public String name = "";
    public ProductType group = new ProductType();
    public String shop = "";
    public double price = 0.0;
    public double amount = 0;

    public Product() {}

    public Product(long id, Date date, String name, ProductType group, String shop, double price, double amount) {
        this.id = id;
        this.date = date;
        this.name = name;
        this.group = group;
        this.shop = shop;
        this.price = price;
        this.amount = amount;
    }
}
