package com.example.htgdnss.model;

public class Review {
    private String reviewId;
    private String productId;
    private String orderId;
    private String buyerId;
    private String buyerName;
    private int stars; // 1-5
    private String content;
    private String sellerReply;
    private long createdAt;
    private long repliedAt;

    public Review() {}

    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getBuyerId() { return buyerId; }
    public void setBuyerId(String buyerId) { this.buyerId = buyerId; }
    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSellerReply() { return sellerReply; }
    public void setSellerReply(String sellerReply) { this.sellerReply = sellerReply; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getRepliedAt() { return repliedAt; }
    public void setRepliedAt(long repliedAt) { this.repliedAt = repliedAt; }
}
