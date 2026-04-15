package com.example.htgdnss.buyer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.htgdnss.R;
import com.example.htgdnss.adapter.ProductAdapter;
import com.example.htgdnss.common.ProfileActivity;
import com.example.htgdnss.databinding.ActivityHomeBuyerBinding;
import com.example.htgdnss.model.Product;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class HomeBuyerActivity extends AppCompatActivity {

    private ActivityHomeBuyerBinding binding;
    private FirebaseFirestore db;
    private ProductAdapter adapter;
    private final List<Product> products = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBuyerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        adapter = new ProductAdapter(products, product -> {
            Intent i = new Intent(this, ProductDetailActivity.class);
            i.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.getProductId());
            startActivity(i);
        });

        binding.rvProducts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvProducts.setAdapter(adapter);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true;

            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(this, CartActivity.class));
                return true;

            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.nav_orders) {
                Intent intent = new Intent(this, MyOrdersBuyerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            }

            return false;
        });

        binding.edtSearch.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        binding.tvTitle.setOnClickListener(v -> startActivity(new Intent(this, MyOrdersBuyerActivity.class)));

        loadProducts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }

    private void loadProducts() {
        binding.progress.setVisibility(View.VISIBLE);
        binding.tvEmpty.setVisibility(View.GONE);
        binding.rvProducts.setVisibility(View.GONE);

        db.collection("products")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnSuccessListener(snaps -> {
                    products.clear();
                    for (var doc : snaps.getDocuments()) {
                        Product p = doc.toObject(Product.class);
                        if (p != null) {
                            if (p.getProductId() == null || p.getProductId().isEmpty()) {
                                p.setProductId(doc.getId());
                            }
                            products.add(p);
                        }
                    }

                    binding.progress.setVisibility(View.GONE);
                    if (products.isEmpty()) {
                        binding.tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        binding.rvProducts.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progress.setVisibility(View.GONE);
                    binding.tvEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Tải sản phẩm thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
