// ReplyToReviewActivity.java
package com.example.htgdnss.seller;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.htgdnss.databinding.ActivityReplyToReviewBinding;
import com.example.htgdnss.model.Review;
import com.google.firebase.firestore.FirebaseFirestore;

public class ReplyToReviewActivity extends AppCompatActivity {

    private ActivityReplyToReviewBinding binding;
    private FirebaseFirestore db;
    private Review review;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReplyToReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        review = (Review) getIntent().getSerializableExtra("review");

        if (review == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy đánh giá", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.tvProductName.setText(getIntent().getStringExtra("productName"));
        binding.tvBuyerReview.setText(review.getContent());

        String stars = getStarString(review.getStars());
        binding.tvStars.setText(stars);

        binding.btnSubmit.setOnClickListener(v -> submitReply());
        binding.btnCancel.setOnClickListener(v -> finish());
    }

    private String getStarString(int stars) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stars; i++) sb.append("⭐");
        return sb.toString();
    }

    private void submitReply() {
        String reply = binding.edtReply.getText() != null ?
                binding.edtReply.getText().toString().trim() : "";

        if (reply.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập phản hồi", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnSubmit.setEnabled(false);
        binding.btnSubmit.setText("Đang gửi...");

        db.collection("reviews").document(review.getReviewId())
                .update("sellerReply", reply, "repliedAt", System.currentTimeMillis())
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Đã phản hồi đánh giá", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.btnSubmit.setEnabled(true);
                    binding.btnSubmit.setText("Gửi phản hồi");
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}