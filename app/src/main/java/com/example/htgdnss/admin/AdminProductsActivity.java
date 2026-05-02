package com.example.htgdnss.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.htgdnss.adapter.ProductAdapter;
import com.example.htgdnss.buyer.ProductDetailActivity;
import com.example.htgdnss.common.ProductReviewsActivity;
import com.example.htgdnss.databinding.ActivityAdminProductsBinding;
import com.example.htgdnss.model.Product;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdminProductsActivity extends AppCompatActivity {

    private ActivityAdminProductsBinding binding;
    private FirebaseFirestore db;
    private ProductAdapter adapter;
    private List<Product> products = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminProductsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý sản phẩm");
        }

        db = FirebaseFirestore.getInstance();

        // Adapter với 3 listener (thêm listener xóa)
        adapter = new ProductAdapter(products,
                product -> {
                    // Xem chi tiết sản phẩm
                    Intent intent = new Intent(this, ProductDetailActivity.class);
                    intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.getProductId());
                    startActivity(intent);
                },
                product -> {
                    // Xem đánh giá
                    Intent intent = new Intent(this, ProductReviewsActivity.class);
                    intent.putExtra("productId", product.getProductId());
                    intent.putExtra("productName", product.getName());
                    startActivity(intent);
                },
                product -> {
                    // Xóa sản phẩm
                    confirmDelete(product);
                }
        );

        binding.rvProducts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvProducts.setAdapter(adapter);

        loadProducts();
    }

    private void loadProducts() {
        binding.progressBar.setVisibility(View.VISIBLE);

        db.collection("products")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snaps -> {
                    products.clear();
                    for (var doc : snaps.getDocuments()) {
                        Product p = doc.toObject(Product.class);
                        if (p != null) {
                            if (p.getProductId() == null) {
                                p.setProductId(doc.getId());
                            }
                            products.add(p);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    binding.progressBar.setVisibility(View.GONE);

                    if (products.isEmpty()) {
                        binding.tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvEmpty.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Xác nhận xóa sản phẩm
    private void confirmDelete(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn có chắc muốn xóa sản phẩm \"" + product.getName() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteProduct(product);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Xóa sản phẩm khỏi Firestore
    private void deleteProduct(Product product) {
        db.collection("products").document(product.getProductId())
                .delete()
                .addOnSuccessListener(v -> {
                    products.remove(product);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();

                    if (products.isEmpty()) {
                        binding.tvEmpty.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Xóa thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}