package com.example.htgdnss.buyer;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.htgdnss.R;
import com.example.htgdnss.databinding.ActivityAddReviewBinding;
import com.example.htgdnss.model.Order;
import com.google.firebase.Firebase;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        order = (Order) getIntent().getSerializableExtra("order");
        if(order == null) {
            Toast.makeText(this, "Lỗi: không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        productId = order.getProductId();
        productName = order.getProductName();

        binding.tvProductName.setText(productName);
        binding.ratingBar.setOnRatingBarChangeListener(((ratingBar, rating, fromUser) -> {
            binding.tvRatingHint.setText("Bạn đã chọn " + (int)rating + " sao");
        }));

        binding.btnSubmit.setOnClickListener(v -> submitReview());
        binding.btnCancel.setOnClickListener(v -> finish());


    }

    private void submitReview() {
        float rating = binding.ratingBar.getRating();
        if(rating != 0) {
            Toast.makeText(this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }
        String content = binding.edtContent.getText() != null ? binding.edtContent.getText().toString().trim() : "";
        if (content.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nội dung đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }
        setLoading(true);

        String reviewId = UUID.randomUUID().toString();
        String buyerId = auth.getCurrentUser().getUid();
        String buyerName = auth.getCurrentUser().getDisplayName();

        Map<String, Object> review = new HashMap<>();
        review.put("reviewId", reviewId);
        review.put("productId", productId);
        review.put("orderId", order.getOrderId());
        review.put("buyerId", buyerId);
        review.put("buyerName", buyerName);
        review.put("stars", (int) rating);
        review.put("content", content);
        review.put("sellerReply", "");
        review.put("createAt", System.currentTimeMillis());
        review.put("repliedAt", 0);
        db.collection("reviews").document(reviewId)
                .set(review)
                .addOnSuccessListener(u -> {
                    setLoading(false);
                    Toast.makeText(this, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                   setLoading(false);
                    Toast.makeText(this, "Gửi đánh giá thất bại" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }

    private void setLoading(boolean loading) {
        binding.btnSubmit.setEnabled(!loading);
        binding.btnSubmit.setText(loading ? "Đang gửi..." : "Gửi đánh giá");
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }


}