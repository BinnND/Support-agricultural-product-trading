package com.example.htgdnss.buyer;

import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.htgdnss.databinding.ActivityAddReviewBinding;
import com.example.htgdnss.model.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddReviewActivity extends AppCompatActivity {

    private ActivityAddReviewBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Order order;
    private String productId;
    private String productName;
    private int selectedStars = 0;  // THÊM BIẾN LƯU SỐ SAO

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Lấy order từ Intent
        if (getIntent().hasExtra("order")) {
            try {
                order = (Order) getIntent().getSerializableExtra("order");
            } catch (ClassCastException e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi dữ liệu", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        if (order == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        productId = order.getProductId();
        productName = order.getProductName();

        binding.tvProductName.setText(productName != null ? productName : "Sản phẩm không xác định");

        // SỬA LẠI CÁCH LẤY SỐ SAO
        binding.ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                selectedStars = (int) rating;
                binding.tvRatingHint.setText("Bạn đã chọn " + selectedStars + " sao");
            }
        });

        binding.btnSubmit.setOnClickListener(v -> submitReview());
        binding.btnCancel.setOnClickListener(v -> finish());
    }

    private void submitReview() {
        // LẤY SỐ SAO TRỰC TIẾP TỪ RATINGBAR
        float rating = binding.ratingBar.getRating();
        int stars = (int) rating;

        // Debug: In ra log để kiểm tra
        android.util.Log.d("AddReview", "Rating value: " + rating);
        android.util.Log.d("AddReview", "Stars: " + stars);

        if (stars == 0) {
            Toast.makeText(this, "Vui lòng chọn số sao đánh giá (1-5 sao)", Toast.LENGTH_SHORT).show();
            return;
        }

        String content = binding.edtContent.getText() != null ?
                binding.edtContent.getText().toString().trim() : "";

        if (content.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nội dung đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        String reviewId = UUID.randomUUID().toString();
        String buyerId = auth.getCurrentUser().getUid();
        String buyerEmail = auth.getCurrentUser().getEmail();
        String buyerName = buyerEmail != null ? buyerEmail.split("@")[0] : "Ẩn danh";

        Map<String, Object> review = new HashMap<>();
        review.put("reviewId", reviewId);
        review.put("productId", productId);
        review.put("orderId", order.getOrderId());
        review.put("buyerId", buyerId);
        review.put("buyerName", buyerName);
        review.put("stars", stars);  // DÙNG BIẾN stars
        review.put("content", content);
        review.put("sellerReply", "");
        review.put("createdAt", System.currentTimeMillis());
        review.put("repliedAt", 0);

        db.collection("reviews").document(reviewId)
                .set(review)
                .addOnSuccessListener(v -> {
                    // Cập nhật trạng thái đã đánh giá cho đơn hàng
                    db.collection("orders").document(order.getOrderId())
                            .update("reviewed", true)
                            .addOnSuccessListener(u -> {
                                setLoading(false);
                                Toast.makeText(this, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                setLoading(false);
                                Toast.makeText(this, "Cập nhật đơn hàng thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Gửi đánh giá thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setLoading(boolean loading) {
        binding.btnSubmit.setEnabled(!loading);
        binding.btnCancel.setEnabled(!loading);
        binding.btnSubmit.setText(loading ? "Đang gửi..." : "Gửi đánh giá");
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.ratingBar.setEnabled(!loading);
        binding.edtContent.setEnabled(!loading);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}