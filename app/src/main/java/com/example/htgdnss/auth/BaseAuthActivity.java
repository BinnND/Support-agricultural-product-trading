package com.example.htgdnss.auth;

import android.content.Intent;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.htgdnss.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class BaseAuthActivity extends AppCompatActivity {

    protected FirebaseAuth auth;
    protected FirebaseFirestore db;

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    private void checkUserStatus() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            db.collection("users").document(firebaseUser.getUid())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (!doc.exists()) {
                            // Document không tồn tại = đã bị xóa
                            forceLogout("Tài khoản không tồn tại");
                        } else {
                            String status = doc.getString("status");
                            if ("deleted".equals(status)) {
                                forceLogout("Tài khoản đã bị xóa");
                            } else if ("locked".equals(status)) {
                                forceLogout("Tài khoản đã bị khóa");
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Nếu lỗi khi check, vẫn cho phép tiếp tục
                        // Tránh logout khi mất mạng
                    });
        }
    }

    private void forceLogout(String message) {
        auth.signOut();
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        // Chuyển về màn hình login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}