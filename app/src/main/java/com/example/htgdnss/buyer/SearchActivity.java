package com.example.htgdnss.buyer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.htgdnss.adapter.ProductAdapter;
import com.example.htgdnss.common.ProductReviewsActivity;
import com.example.htgdnss.databinding.ActivitySearchBinding;
import com.example.htgdnss.model.Product;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private FirebaseFirestore db;
    private ProductAdapter adapter;
    private final List<Product> results = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        adapter = new ProductAdapter(results,
                product -> {
                    // Click vào sản phẩm -> xem chi tiết
                    Intent i = new Intent(this, ProductDetailActivity.class);
                    i.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.getProductId());
                    startActivity(i);
                },
                product -> {
                    // Click vào nút xem đánh giá
                    Intent intent = new Intent(this, ProductReviewsActivity.class);
                    intent.putExtra("productId", product.getProductId());
                    intent.putExtra("productName", product.getName());
                    startActivity(intent);
                }
        );

        binding.rvResults.setLayoutManager(new LinearLayoutManager(this));
        binding.rvResults.setAdapter(adapter);

        binding.btnDoSearch.setOnClickListener(v -> doSearch());
    }

    private void doSearch() {
        String q = binding.edtQuery.getText() == null ? "" : binding.edtQuery.getText().toString().trim();
        String queryLower = q.toLowerCase(Locale.ROOT);

        binding.progress.setVisibility(View.VISIBLE);
        db.collection("products")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(200)
                .get()
                .addOnSuccessListener(snaps -> {
                    results.clear();
                    for (var doc : snaps.getDocuments()) {
                        Product p = doc.toObject(Product.class);
                        if (p == null) continue;
                        if (p.getProductId() == null || p.getProductId().isEmpty()) p.setProductId(doc.getId());

                        if (queryLower.isEmpty()) {
                            results.add(p);
                        } else {
                            String haystack = (safe(p.getName()) + " " + safe(p.getCategory()) + " " + safe(p.getLocation()) + " " +
                                    safe(p.getFarmingRegion()) + " " + safe(p.getFarmingMethod()) + " " + safe(p.getCertification()))
                                    .toLowerCase(Locale.ROOT);
                            if (haystack.contains(queryLower)) results.add(p);
                        }
                    }
                    binding.progress.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                    if (results.isEmpty()) {
                        Toast.makeText(this, "Không có kết quả phù hợp", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progress.setVisibility(View.GONE);
                    Toast.makeText(this, "Tìm kiếm thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
