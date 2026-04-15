package com.example.htgdnss;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Kiểm tra người dùng đã đăng nhập chưa
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // Chưa đăng nhập → chuyển sang Login
            startActivity(new Intent(this, com.example.htgdnss.auth.LoginActivity.class));
        } else {
            // Đã đăng nhập → kiểm tra role và chuyển màn hình tương ứng
            checkUserRole(currentUser.getUid());
        }
        finish(); // Đóng MainActivity sau khi chuyển
    }

    private void checkUserRole(String uid) {
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");

                        Intent intent;
                        switch (role) {
                            case "seller":
                                intent = new Intent(MainActivity.this, com.example.htgdnss.seller.MyProductsActivity.class);
                                break;
                            case "admin":
                                intent = new Intent(MainActivity.this, com.example.htgdnss.admin.AdminDashboardActivity.class);
                                break;
                            default: // buyer
                                intent = new Intent(MainActivity.this, com.example.htgdnss.buyer.HomeBuyerActivity.class);
                                break;
                        }
                        startActivity(intent);
                    } else {
                        // Không tìm thấy user → logout và về login
                        mAuth.signOut();
                        startActivity(new Intent(MainActivity.this, com.example.htgdnss.auth.LoginActivity.class));
                    }
                })
                .addOnFailureListener(e -> {
                    // Lỗi mạng hoặc Firestore → về login
                    mAuth.signOut();
                    startActivity(new Intent(MainActivity.this, com.example.htgdnss.auth.LoginActivity.class));
                });
    }
}