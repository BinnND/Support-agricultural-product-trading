package com.example.htgdnss.buyer;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.htgdnss.databinding.ActivityProductDetailBinding;
import com.example.htgdnss.model.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = "extra_product_id";

    private ActivityProductDetailBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String productId;
    private Product product;
    private final DecimalFormat df = new DecimalFormat("#,###");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        productId = getIntent().getStringExtra(EXTRA_PRODUCT_ID);
        if (TextUtils.isEmpty(productId)) {
            Toast.makeText(this, "Thiếu productId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.btnAddToCart.setOnClickListener(v -> addToCart());

        loadProduct();
    }

    private void loadProduct() {
        setLoading(true);
        db.collection("products").document(productId)
                .get()
                .addOnSuccessListener(doc -> {
                    product = doc.toObject(Product.class);
                    if (product != null) {
                        if (product.getProductId() == null || product.getProductId().isEmpty()) {
                            product.setProductId(doc.getId());
                        }
                        bindProduct(product);
                    }
                    setLoading(false);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Tải chi tiết thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void bindProduct(Product p) {
        binding.tvName.setText(p.getName());
        binding.tvPrice.setText(df.format(p.getPrice()) + " đ / " + p.getUnit());
        binding.tvMeta.setText("Danh mục: " + nvl(p.getCategory()) + "\n"
                + "Vùng trồng: " + nvl(p.getFarmingRegion()) + "\n"
                + "Phương pháp: " + nvl(p.getFarmingMethod()) + "\n"
                + "Chứng nhận: " + nvl(p.getCertification()) + "\n"
                + "Địa điểm: " + nvl(p.getLocation()));
        binding.tvDescription.setText(nvl(p.getDescription()));

        String url = p.getImageUrl();
        if (url != null && !url.isEmpty()) {
            Glide.with(this).load(url).centerCrop().into(binding.ivImage);
        } else if (p.getImageBase64() != null && !p.getImageBase64().isEmpty()) {
            try {
                byte[] bytes = Base64.decode(p.getImageBase64(), Base64.DEFAULT);
                Glide.with(this).load(bytes).centerCrop().into(binding.ivImage);
            } catch (Exception ignored) {
            }
        }
    }

    private void addToCart() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        if (product == null) return;

        int qty = 1;
        try {
            String raw = binding.edtQty.getText() == null ? "1" : binding.edtQty.getText().toString().trim();
            qty = Integer.parseInt(raw);
        } catch (Exception ignored) {
        }
        if (qty <= 0) qty = 1;

        String uid = auth.getCurrentUser().getUid();
        String cartItemId = product.getProductId();

        Map<String, Object> data = new HashMap<>();
        data.put("productId", product.getProductId());
        data.put("name", product.getName());
        data.put("imageUrl", product.getImageUrl());
        data.put("unitPrice", product.getPrice());
        data.put("unit", product.getUnit());
        data.put("sellerId", product.getSellerId());
        data.put("quantity", qty);
        data.put("updatedAt", System.currentTimeMillis());

        setLoading(true);
        db.collection("carts").document(uid)
                .collection("items").document(cartItemId)
                .set(data)
                .addOnSuccessListener(v -> {
                    setLoading(false);
                    Toast.makeText(this, "Đã thêm vào giỏ", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Thêm giỏ thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setLoading(boolean loading) {
        binding.btnAddToCart.setEnabled(!loading);
        binding.btnAddToCart.setText(loading ? "Đang xử lý…" : "Thêm vào giỏ");
        binding.tilQty.setEnabled(!loading);
        binding.ivImage.setAlpha(loading ? 0.7f : 1f);
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}
