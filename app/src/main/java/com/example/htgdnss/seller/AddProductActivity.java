package com.example.htgdnss.seller;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.htgdnss.databinding.ActivityAddProductBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddProductActivity extends AppCompatActivity {

    private ActivityAddProductBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private Uri pickedImageUri;

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) return;
                pickedImageUri = uri;
                Glide.with(this).load(uri).centerCrop().into(binding.ivImage);
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        binding.btnPickImage.setOnClickListener(v -> pickImage.launch("image/*"));
        binding.btnSave.setOnClickListener(v -> save());
    }

    private void save() {
        if (auth.getCurrentUser() == null) {
            finish();
            return;
        }

        String name = text(binding.edtName);
        String category = text(binding.edtCategory);
        String unit = text(binding.edtUnit);
        String location = text(binding.edtLocation);
        String certification = text(binding.edtCertification);
        String description = text(binding.edtDescription);

        double price = parseDouble(text(binding.edtPrice), 0);
        int stock = (int) parseDouble(text(binding.edtStock), 0);

        if (TextUtils.isEmpty(name) || price <= 0) {
            Toast.makeText(this, "Vui lòng nhập tên và giá hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        String productId = UUID.randomUUID().toString();
        String sellerId = auth.getCurrentUser().getUid();
        long now = System.currentTimeMillis();

        String imageBase64 = "";
        if (pickedImageUri != null) {
            try {
                imageBase64 = uriToBase64Jpeg(pickedImageUri);
            } catch (Exception e) {
                setLoading(false);
                Toast.makeText(this, "Xử lý ảnh thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        saveProductDoc(
                productId,
                sellerId,
                name,
                category,
                description,
                price,
                unit,
                stock,
                location,
                certification,
                "",
                imageBase64,
                now
        );
    }

    private void saveProductDoc(String productId,
                                String sellerId,
                                String name,
                                String category,
                                String description,
                                double price,
                                String unit,
                                int stock,
                                String location,
                                String certification,
                                String imageUrl,
                                String imageBase64,
                                long now) {
        Map<String, Object> p = new HashMap<>();
        p.put("productId", productId);
        p.put("name", name);
        p.put("category", category);
        p.put("description", description);
        p.put("price", price);
        p.put("unit", unit);
        p.put("stock", stock);
        p.put("imageUrl", imageUrl);
        p.put("imageBase64", imageBase64);
        p.put("sellerId", sellerId);
        p.put("location", location);
        p.put("latitude", 0);
        p.put("longitude", 0);
        p.put("farmingRegion", "");
        p.put("farmingMethod", "");
        p.put("certification", certification);
        p.put("inStock", stock > 0);
        p.put("createdAt", now);
        p.put("updatedAt", now);

        db.collection("products").document(productId)
                .set(p)
                .addOnSuccessListener(v -> {
                    setLoading(false);
                    Toast.makeText(this, "Đã đăng sản phẩm", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Lưu thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setLoading(boolean loading) {
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSave.setEnabled(!loading);
        binding.btnPickImage.setEnabled(!loading);
    }

    private String text(android.widget.EditText edt) {
        return edt.getText() == null ? "" : edt.getText().toString().trim();
    }

    private double parseDouble(String s, double def) {
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private String uriToBase64Jpeg(Uri uri) throws Exception {
        InputStream is = getContentResolver().openInputStream(uri);
        if (is == null) throw new RuntimeException("Không đọc được ảnh");
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        is.close();
        if (bitmap == null) throw new RuntimeException("Ảnh không hợp lệ");

        // Resize để giảm nguy cơ vượt 1MiB/document
        int maxW = 800;
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w > maxW) {
            float ratio = (float) h / (float) w;
            int newW = maxW;
            int newH = Math.round(maxW * ratio);
            bitmap = Bitmap.createScaledBitmap(bitmap, newW, newH, true);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] bytes = baos.toByteArray();

        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }
}
