package com.example.htgdnss.seller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.htgdnss.databinding.ActivityXacNhanDangBinding;
import com.example.htgdnss.model.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class XacNhanDangActivity extends AppCompatActivity {

    private ActivityXacNhanDangBinding binding;
    private Product productDang;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private final DecimalFormat df = new DecimalFormat("#,###");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityXacNhanDangBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        productDang = (Product) getIntent().getSerializableExtra("product");
        if (productDang == null) {
            productDang = new Product();
        }

        hienThiThongTin();
        binding.btnDangSanPham.setOnClickListener(v -> dangSanPham());
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void hienThiThongTin() {
        binding.tvXacNhanTen.setText(productDang.getName() != null ? productDang.getName() : "Chưa có");
        binding.tvXacNhanDanhMuc.setText(productDang.getCategory() != null ? productDang.getCategory() : "Chưa có");
        binding.tvXacNhanGia.setText(String.format("Giá: %s đ", df.format(productDang.getPrice())));
        binding.tvXacNhanDonVi.setText(String.format("Đơn vị: %s", productDang.getUnit() != null ? productDang.getUnit() : "Chưa có"));
        binding.tvXacNhanStock.setText(String.format("Tồn kho: %d", productDang.getStock()));

        String viTri = productDang.getLocation();
        if (viTri != null && !viTri.isEmpty()) {
            binding.tvXacNhanViTri.setText(String.format("Vị trí: %s", viTri));
        } else if (productDang.getLatitude() != 0 || productDang.getLongitude() != 0) {
            binding.tvXacNhanViTri.setText(String.format("Vị trí: %.4f, %.4f", productDang.getLatitude(), productDang.getLongitude()));
        } else {
            binding.tvXacNhanViTri.setText("Vị trí: Chưa có");
        }

        binding.tvXacNhanMoTa.setText(String.format("Mô tả: %s", productDang.getDescription() != null && !productDang.getDescription().isEmpty() ? productDang.getDescription() : "Không có"));
        binding.tvXacNhanChungNhan.setText(String.format("Chứng nhận: %s", productDang.getCertification() != null && !productDang.getCertification().isEmpty() ? productDang.getCertification() : "Không có"));
        binding.tvXacNhanFarmingRegion.setText(String.format("Vùng trồng: %s", productDang.getFarmingRegion() != null ? productDang.getFarmingRegion() : "Chưa có"));
        binding.tvXacNhanFarmingMethod.setText(String.format("Phương pháp: %s", productDang.getFarmingMethod() != null ? productDang.getFarmingMethod() : "Chưa có"));

        hienThiAnh();
    }

    private void hienThiAnh() {
        String url = productDang.getImageUrl();
        String base64 = productDang.getImageBase64();

        if (url != null && !url.isEmpty()) {
            Glide.with(this).load(url).centerCrop().into(binding.imgXacNhan);
        } else if (base64 != null && !base64.isEmpty()) {
            try {
                byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                Glide.with(this).load(bytes).centerCrop().into(binding.imgXacNhan);
            } catch (Exception e) {
                binding.imgXacNhan.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            binding.imgXacNhan.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    private void dangSanPham() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (productDang.getName() == null || productDang.getName().isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        if (productDang.getPrice() <= 0) {
            Toast.makeText(this, "Vui lòng nhập giá hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        if (productDang.getProductId() == null || productDang.getProductId().isEmpty()) {
            productDang.setProductId(UUID.randomUUID().toString());
        }

        String sellerId = auth.getCurrentUser().getUid();
        long now = System.currentTimeMillis();

        Map<String, Object> productMap = new HashMap<>();
        productMap.put("productId", productDang.getProductId());
        productMap.put("name", productDang.getName());
        productMap.put("category", productDang.getCategory() != null ? productDang.getCategory() : "");
        productMap.put("description", productDang.getDescription() != null ? productDang.getDescription() : "");
        productMap.put("price", productDang.getPrice());
        productMap.put("unit", productDang.getUnit() != null ? productDang.getUnit() : "");
        productMap.put("stock", productDang.getStock());
        productMap.put("imageUrl", productDang.getImageUrl() != null ? productDang.getImageUrl() : "");
        productMap.put("imageBase64", productDang.getImageBase64() != null ? productDang.getImageBase64() : "");
        productMap.put("sellerId", sellerId);
        productMap.put("location", productDang.getLocation() != null ? productDang.getLocation() : "");
        productMap.put("latitude", productDang.getLatitude());
        productMap.put("longitude", productDang.getLongitude());
        productMap.put("farmingRegion", productDang.getFarmingRegion() != null ? productDang.getFarmingRegion() : "");
        productMap.put("farmingMethod", productDang.getFarmingMethod() != null ? productDang.getFarmingMethod() : "");
        productMap.put("certification", productDang.getCertification() != null ? productDang.getCertification() : "");
        productMap.put("inStock", productDang.getStock() > 0);
        productMap.put("createdAt", now);
        productMap.put("updatedAt", now);

        db.collection("products").document(productDang.getProductId())
                .set(productMap)
                .addOnSuccessListener(v -> {
                    setLoading(false);
                    Toast.makeText(this, "Đã đăng sản phẩm thành công!", Toast.LENGTH_SHORT).show();

                    // ✅ SỬA: Chuyển về trang chủ theo role
                    navigateToHomeByRole();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Lưu thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ✅ THÊM PHƯƠNG THỨC NÀY
    private void navigateToHomeByRole() {
        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    String role = doc.getString("role");
                    Intent intent;

                    if ("seller".equals(role)) {
                        // Người bán -> về MyProductsActivity
                        intent = new Intent(this, com.example.htgdnss.seller.MyProductsActivity.class);
                    } else if ("admin".equals(role)) {
                        // Admin -> về AdminDashboardActivity
                        intent = new Intent(this, com.example.htgdnss.admin.AdminDashboardActivity.class);
                    } else {
                        // Buyer -> về HomeBuyerActivity
                        intent = new Intent(this, com.example.htgdnss.buyer.HomeBuyerActivity.class);
                    }

                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Nếu không lấy được role, mặc định về login
                    Intent intent = new Intent(this, com.example.htgdnss.auth.LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });
    }

    private void setLoading(boolean loading) {
        binding.btnDangSanPham.setEnabled(!loading);
        binding.btnBack.setEnabled(!loading);
        binding.btnDangSanPham.setText(loading ? "Đang đăng..." : "Đăng sản phẩm");
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}