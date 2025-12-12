package com.sunit.groceryplus.models;

public class Promotion {
    private int promoId;
    private String code;
    private double discountPercentage;
    private String validUntil;
    private boolean isActive;

    public Promotion(int promoId, String code, double discountPercentage, String validUntil, boolean isActive) {
        this.promoId = promoId;
        this.code = code;
        this.discountPercentage = discountPercentage;
        this.validUntil = validUntil;
        this.isActive = isActive;
    }

    public int getPromoId() {
        return promoId;
    }

    public String getCode() {
        return code;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public String getValidUntil() {
        return validUntil;
    }

    public boolean isActive() {
        return isActive;
    }
}
