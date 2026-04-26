package com.example.htgdnss.common;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.htgdnss.adapter.ReviewAdapter;
import com.example.htgdnss.databinding.ActivityProductReviewsBinding;
import com.example.htgdnss.model.Review;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ProductReviewsActivity extends AppCompatActivity {

    private ActivityProductReviewsBinding binding;
    private FirebaseFirestore db;
    private ReviewAdapter adapter;
    private List<Review> reviews = new ArrayList<>();
    private String productId;
    private String productName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductReviewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        productId = getIntent().getStringExtra("productId");
        productName = getIntent().getStringExtra("productName");

        binding.tvProductName.setText(productName);
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        adapter = new ReviewAdapter(reviews);
        binding.rvReviews.setLayoutManager(new LinearLayoutManager(this));
        binding.rvReviews.setAdapter(adapter);

        loadReviews();
    }

    private void loadReviews() {
        binding.progressBar.setVisibility(View.VISIBLE);

        db.collection("reviews")
                .whereEqualTo("productId", productId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snaps -> {
                    reviews.clear();
                    for (var doc : snaps.getDocuments()) {
                        Review r = doc.toObject(Review.class);
                        if (r != null) {
                            reviews.add(r);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    binding.progressBar.setVisibility(View.GONE);

                    if (reviews.isEmpty()) {
                        binding.tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvEmpty.setVisibility(View.GONE);
                        calculateAverage();
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void calculateAverage() {
        int sum = 0;
        for (Review r : reviews) {
            sum += r.getStars();
        }
        double avg = (double) sum / reviews.size();
        binding.tvAverageRating.setText(String.format("%.1f ★", avg));
        binding.tvReviewCount.setText("(" + reviews.size() + " đánh giá)");
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}