package com.sunit.groceryplus.network;

public class PaymentIntentRequest {
    private int amount;
    private String currency;

    public PaymentIntentRequest(int amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public int getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }
}
