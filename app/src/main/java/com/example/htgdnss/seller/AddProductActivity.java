package com.example.htgdnss.seller;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.htgdnss.R;
import com.example.htgdnss.model.Product;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

public class AddProductActivity extends AppCompatActivity {

    private TextInputEditText etTen, etGia, etDonVi, etChungNhan, etMoTa, etStock, etFarmingRegion, etFarmingMethod;
    private TextInputLayout tilTen, tilGia, tilDonVi, tilDanhMuc;
    private MaterialAutoCompleteTextView actDanhMuc;
    private View btnTiepTheo;
    private View tvError;

    private Product productDang;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product); // Dùng layout cũ nhưng sửa nội dung

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        productDang = new Product();

        bindViews();
        setupDanhMucDropdown();

        btnTiepTheo.setOnClickListener(v -> kiemTraVaTiepTheo());
    }

    private void bindViews() {
        etTen = findViewById(R.id.etTenSanPham);
        etGia = findViewById(R.id.etGiaBan);
        etDonVi = findViewById(R.id.etDonVi);
        etChungNhan = findViewById(R.id.etChungNhan);
        etMoTa = findViewById(R.id.etMoTa);
        etStock = findViewById(R.id.etStock);
        etFarmingRegion = findViewById(R.id.etFarmingRegion);
        etFarmingMethod = findViewById(R.id.etFarmingMethod);
        actDanhMuc = findViewById(R.id.actDanhMuc);
        tilTen = findViewById(R.id.tilTenSanPham);
        tilGia = findViewById(R.id.tilGiaBan);
        tilDonVi = findViewById(R.id.tilDonVi);
        tilDanhMuc = findViewById(R.id.tilDanhMuc);
        btnTiepTheo = findViewById(R.id.btnTiepTheo);
        tvError = findViewById(R.id.tvError);
    }

    private void setupDanhMucDropdown() {
        String[] categories = {"Rau củ", "Trái cây", "Ngũ cốc", "Thủy hải sản",
                "Thịt gia súc", "Thịt gia cầm", "Trứng & Sữa", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories);
        actDanhMuc.setAdapter(adapter);
    }

    private void kiemTraVaTiepTheo() {
        tilTen.setError(null);
        tilGia.setError(null);
        tilDonVi.setError(null);
        tilDanhMuc.setError(null);
        tvError.setVisibility(View.GONE);

        boolean hopLe = true;

        String ten = etTen.getText() != null ? etTen.getText().toString().trim() : "";
        String danhMuc = actDanhMuc.getText().toString().trim();
        String giaStr = etGia.getText() != null ? etGia.getText().toString().trim() : "";
        String donVi = etDonVi.getText() != null ? etDonVi.getText().toString().trim() : "";
        String chungNhan = etChungNhan.getText() != null ? etChungNhan.getText().toString().trim() : "";
        String moTa = etMoTa.getText() != null ? etMoTa.getText().toString().trim() : "";
        String stockStr = etStock != null && etStock.getText() != null ? etStock.getText().toString().trim() : "0";
        String farmingRegion = etFarmingRegion != null && etFarmingRegion.getText() != null ? etFarmingRegion.getText().toString().trim() : "";
        String farmingMethod = etFarmingMethod != null && etFarmingMethod.getText() != null ? etFarmingMethod.getText().toString().trim() : "";

        if (TextUtils.isEmpty(ten)) {
            tilTen.setError("Vui lòng nhập tên sản phẩm");
            hopLe = false;
        }
        if (TextUtils.isEmpty(danhMuc)) {
            tilDanhMuc.setError("Vui lòng chọn danh mục");
            hopLe = false;
        }
        if (TextUtils.isEmpty(giaStr)) {
            tilGia.setError("Vui lòng nhập giá bán");
            hopLe = false;
        }
        if (TextUtils.isEmpty(donVi)) {
            tilDonVi.setError("Vui lòng nhập đơn vị");
            hopLe = false;
        }

        if (!hopLe) {
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        double price;
        int stock;
        try {
            price = Double.parseDouble(giaStr);
        } catch (NumberFormatException e) {
            tilGia.setError("Giá không hợp lệ");
            return;
        }
        try {
            stock = Integer.parseInt(stockStr);
        } catch (NumberFormatException e) {
            stock = 0;
        }

        productDang.setName(ten);
        productDang.setCategory(danhMuc);
        productDang.setPrice(price);
        productDang.setUnit(donVi);
        productDang.setCertification(chungNhan);
        productDang.setDescription(moTa);
        productDang.setStock(stock);
        productDang.setFarmingRegion(farmingRegion);
        productDang.setFarmingMethod(farmingMethod);
        productDang.setInStock(stock > 0);

        Intent intent = new Intent(this, ChupAnhActivity.class);
        intent.putExtra("product", productDang);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}