package com.sunit.groceryplus.models;

/**
 * Product model class representing a product in the grocery store
 */
public class Product {
    private int productId;
    private String productName;
    private int categoryId;
    private String categoryName;
    private double price;
    private String description;
    private String image;
    private int stock;

    // Default constructor
    public Product() {
    }

    // Constructor with all fields
    public Product(int productId, String productName, int categoryId, String categoryName, 
                   double price, String description, String image, int stock) {
        this.productId = productId;
        this.productName = productName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.price = price;
        this.description = description;
        this.image = image;
        this.stock = stock;
    }

    // Constructor without ID (for new products)
    public Product(String productName, int categoryId, double price, String description, String image, int stock) {
        this.productName = productName;
        this.categoryId = categoryId;
        this.price = price;
        this.description = description;
        this.image = image;
        this.stock = stock;
    }

    // Getters and Setters
    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public boolean isInStock() {
        return stock > 0;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                ", price=" + price +
                ", description='" + description + '\'' +
                ", image='" + image + '\'' +
                ", stock=" + stock +
                '}';
    }
}
