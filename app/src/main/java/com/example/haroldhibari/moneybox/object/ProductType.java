package com.example.haroldhibari.moneybox.object;

import android.support.annotation.NonNull;

/**
 * Represents the currently known product types
 */
public enum ProductType {

    notSet(""),
    isa("Isa"),
    gia("Gia");

    @NonNull private final String type;

    ProductType(@NonNull final String type){
        this.type = type;
    }

    public static ProductType init(@NonNull final String type){
        switch (type){
            case "Isa" : return isa;
            case "Gia" : return gia;
            default    : return notSet;
        }
    }

    @Override
    @NonNull public String toString(){
        switch (type){
            case "Isa"   : return "Stocks & Shares ISA";
            case "Gia"   : return "General Investment Account";
            default      : return "";
        }
    }
}
