package com.example.htgdnss.model; // Sửa package cho đồng bộ

import java.io.Serializable;

public class Product implements Serializable { // Thêm Serializable

    private String productId;
    private String name;
    private String category;
    private String description;
    private double price;
    private String unit;
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

    public Product() {}

    // Thêm method format giá cho tiện
    public String getPriceFormatted() {
        return String.format("%,.0f đ/%s", price, unit != null ? unit : "");
    }

    // Getters & Setters (giữ nguyên như file của bạn)
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