package com.example.htgdnss.model;

public class Product {

    private String productId;
    private String name;
    private String category;
    private String description;
    private double price;
    private String unit; // kg, bó, thùng...
    private int stock;
    private String imageUrl;
    private String imageBase64;
    private String sellerId;
    private String location;
    private double latitude;
    private double longitude;
    private String farmingRegion;
    private String farmingMethod;
    private String certification;
    private boolean inStock;
    private long createdAt;
    private long updatedAt;

    public Product(){}

    public Product(String productId, String name, String category, String description, double price, String unit,
                   int stock, String imageUrl, String sellerId, String location, double latitude, double longitude,
                   String farmingRegion, String farmingMethod, String certification, boolean inStock,
                   long createdAt, long updatedAt) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.description = description;
        this.price = price;
        this.unit = unit;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.sellerId = sellerId;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.farmingRegion = farmingRegion;
        this.farmingMethod = farmingMethod;
        this.certification = certification;
        this.inStock = inStock;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getFarmingRegion() { return farmingRegion; }
    public void setFarmingRegion(String farmingRegion) { this.farmingRegion = farmingRegion; }
    public String getFarmingMethod() { return farmingMethod; }
    public void setFarmingMethod(String farmingMethod) { this.farmingMethod = farmingMethod; }
    public String getCertification() { return certification; }
    public void setCertification(String certification) { this.certification = certification; }
    public boolean isInStock() { return inStock; }
    public void setInStock(boolean inStock) { this.inStock = inStock; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

}