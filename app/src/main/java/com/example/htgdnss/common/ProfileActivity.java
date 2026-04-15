package com.example.htgdnss.common;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.htgdnss.MainActivity;
import com.example.htgdnss.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        binding.btnSave.setOnClickListener(v -> save());
        binding.btnLogout.setOnClickListener(v -> logout());

        load();
    }

    private void load() {
        if (auth.getCurrentUser() == null) {
            finish();
            return;
        }
        String uid = auth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;
                    binding.edtName.setText(doc.getString("fullName"));
                    binding.edtPhone.setText(doc.getString("phone"));
                    binding.edtAddress.setText(doc.getString("address"));
                });
    }

    private void save() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        String name = binding.edtName.getText() == null ? "" : binding.edtName.getText().toString().trim();
        String phone = binding.edtPhone.getText() == null ? "" : binding.edtPhone.getText().toString().trim();
        String address = binding.edtAddress.getText() == null ? "" : binding.edtAddress.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Vui lòng nhập họ tên", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("fullName", name);
        data.put("phone", phone);
        data.put("address", address);

        db.collection("users").document(uid)
                .update(data)
                .addOnSuccessListener(v -> Toast.makeText(this, "Đã lưu", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Lưu thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void logout() {
        auth.signOut();
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }
}
