package com.example.haroldhibari.moneybox.object;

import android.support.annotation.NonNull;

/**
 * Models the necessary product information we need for the app
 */
public class Product {

    @NonNull private ProductType productType;

    private final int id;

    private final int investorId;

    private int moneyBox;

    public Product(int id, int investorId, int moneyBox, String productType) {
        this.id = id;
        this.investorId = investorId;
        this.moneyBox = moneyBox;
        this.productType = ProductType.init(productType);
    }

    @NonNull
    public ProductType getProductType() {
        return productType;
    }

    public int getId() {
        return id;
    }

    public int getInvestorId() {
        return investorId;
    }

    public int getMoneyBox() {
        return moneyBox;
    }

}
