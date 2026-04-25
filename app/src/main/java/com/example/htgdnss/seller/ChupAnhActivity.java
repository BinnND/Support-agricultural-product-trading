package com.example.htgdnss.seller;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.htgdnss.databinding.ActivityChupAnhBinding;
import com.example.htgdnss.model.Product;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChupAnhActivity extends AppCompatActivity {

    private static final int PERMISSION_CAMERA = 101;
    private static final String FILE_PROVIDER_AUTH = "com.example.htgdnss.fileprovider";

    private ActivityChupAnhBinding binding;

    private Product productDang;
    private Uri anhUri;

    // ===== CAMERA =====
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && anhUri != null) {
                            hienThiAnh(anhUri);
                            productDang.setImageUrl(anhUri.toString());
                            chuyenSangGPS();
                        }
                    });

    // ===== GALLERY =====
    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Uri selected = result.getData().getData();
                            if (selected != null) {
                                anhUri = selected;
                                hienThiAnh(anhUri);
                                productDang.setImageUrl(anhUri.toString());
                                chuyenSangGPS();
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChupAnhBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Data
        productDang = (Product) getIntent().getSerializableExtra("product");

        // Ẩn preview nếu có
        if (binding.cameraPreview != null) {
            binding.cameraPreview.setVisibility(View.GONE);
        }

        // Button click
        binding.btnChup.setOnClickListener(v -> moCamera());
        binding.btnThuVien.setOnClickListener(v -> moThuVien());
    }

    // ===== MỞ CAMERA =====
    private void moCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_CAMERA);
            return;
        }

        try {
            File imageFile = taoFileAnh();
            anhUri = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTH, imageFile);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, anhUri);

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            cameraLauncher.launch(intent);

        } catch (IOException e) {
            hienLoi("Không tạo được file ảnh: " + e.getMessage());
        }
    }

    // ===== MỞ THƯ VIỆN =====
    private void moThuVien() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        galleryLauncher.launch(intent);
    }

    // ===== TẠO FILE ẢNH =====
    private File taoFileAnh() throws IOException {
        String timeStamp = new SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
        ).format(new Date());

        String fileName = "SP_" + timeStamp;

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs();
        }

        return File.createTempFile(fileName, ".jpg", storageDir);
    }

    // ===== HIỂN THỊ ẢNH =====
    private void hienThiAnh(Uri uri) {
        binding.tvCameraPlaceholder.setVisibility(View.GONE);
        binding.tvCameraError.setVisibility(View.GONE);
        binding.imgSelected.setVisibility(View.VISIBLE);

        Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(binding.imgSelected);
    }

    // ===== LỖI =====
    private void hienLoi(String msg) {
        binding.tvCameraError.setText(msg);
        binding.tvCameraError.setVisibility(View.VISIBLE);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    // ===== CHUYỂN GPS =====
    private void chuyenSangGPS() {
        Intent intent = new Intent(this, XacNhanViTriActivity.class);
        intent.putExtra("product", productDang);
        startActivityForResult(intent, 3001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 3001 && resultCode == RESULT_OK) {
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int req, String[] perms, int[] grants) {
        super.onRequestPermissionsResult(req, perms, grants);

        if (req == PERMISSION_CAMERA
                && grants.length > 0
                && grants[0] == PackageManager.PERMISSION_GRANTED) {

            moCamera();

        } else {
            hienLoi("Camera không hoạt động!");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}