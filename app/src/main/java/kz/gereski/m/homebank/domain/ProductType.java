package kz.gereski.m.homebank.domain;


public class ProductType {
    public long id = 0L;
    public String name = "";

    public ProductType() {}

    public ProductType(long id, String name) {
        this.id = id;
        this.name = name;
    }
}
