package com.example.htgdnss.seller;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.htgdnss.adapter.ProductAdapter;
import com.example.htgdnss.buyer.ProductDetailActivity;
import com.example.htgdnss.common.ProductReviewsActivity;
import com.example.htgdnss.common.ProfileActivity;
import com.example.htgdnss.common.SettingsActivity;
import com.example.htgdnss.databinding.ActivityMyProductsBinding;
import com.example.htgdnss.model.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyProductsActivity extends AppCompatActivity {

    private ActivityMyProductsBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ProductAdapter adapter;
    private final List<Product> products = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyProductsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        adapter = new ProductAdapter(products,
                p -> {
                    Intent i = new Intent(this, com.example.htgdnss.buyer.ProductDetailActivity.class);
                    i.putExtra(com.example.htgdnss.buyer.ProductDetailActivity.EXTRA_PRODUCT_ID, p.getProductId());
                    startActivity(i);
                },
                p -> {
                    Intent intent = new Intent(this, ProductReviewsActivity.class);
                    intent.putExtra("productId", p.getProductId());
                    intent.putExtra("productName", p.getName());
                    startActivity(intent);
                },
                p -> {
                    // Seller xóa sản phẩm của mình
                    new AlertDialog.Builder(this)
                            .setTitle("Xóa sản phẩm")
                            .setMessage("Bạn có chắc muốn xóa " + p.getName() + "?")
                            .setPositiveButton("Xóa", (dialog, which) -> {
                                db.collection("products").document(p.getProductId()).delete()
                                        .addOnSuccessListener(v -> {
                                            products.remove(p);
                                            adapter.notifyDataSetChanged();
                                            Toast.makeText(this, "Đã xóa", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .setNegativeButton("Hủy", null)
                            .show();
                }
        );


        binding.rvProducts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvProducts.setAdapter(adapter);

        binding.btnAdd.setOnClickListener(v -> startActivity(new Intent(this, AddProductActivity.class)));
        binding.btnOrders.setOnClickListener(v -> startActivity(new Intent(this, ManageOrdersSellerActivity.class)));
        binding.btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        binding.btnSetting.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyProducts();
    }

    private void loadMyProducts() {
        if (auth.getCurrentUser() == null) {
            finish();
            return;
        }
        String uid = auth.getCurrentUser().getUid();

        binding.layoutEmpty.setVisibility(View.GONE);
        db.collection("products")
                .whereEqualTo("sellerId", uid)
                .limit(200)
                .get()
                .addOnSuccessListener(snaps -> {
                    products.clear();
                    for (var doc : snaps.getDocuments()) {
                        Product p = doc.toObject(Product.class);
                        if (p != null) {
                            if (p.getProductId() == null || p.getProductId().isEmpty()) p.setProductId(doc.getId());
                            products.add(p);
                        }
                    }
                    Collections.sort(products, (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                    adapter.notifyDataSetChanged();
                    binding.layoutEmpty.setVisibility(products.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Tải sản phẩm thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
