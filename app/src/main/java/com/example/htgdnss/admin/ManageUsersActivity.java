// ManageUsersActivity.java
package com.example.htgdnss.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.htgdnss.adapter.UserManagementAdapter;
import com.example.htgdnss.databinding.ActivityManageUsersBinding;
import com.example.htgdnss.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageUsersActivity extends AppCompatActivity {

    private ActivityManageUsersBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private UserManagementAdapter adapter;
    private List<User> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        adapter = new UserManagementAdapter(userList, new UserManagementAdapter.UserActionListener() {
            @Override
            public void onLockUnlock(User user) {
                toggleUserStatus(user);
            }

            @Override
            public void onDelete(User user) {
                confirmDeleteUser(user);
            }
        });

        binding.rvUsers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvUsers.setAdapter(adapter);

        loadUsers();
    }

    private void loadUsers() {
        binding.progressBar.setVisibility(View.VISIBLE);

        db.collection("users")
                .get()
                .addOnSuccessListener(snapshots -> {
                    userList.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        User user = doc.toObject(User.class);
                        if (user != null && !"admin".equals(user.getRole())) {
                            userList.add(user);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    binding.progressBar.setVisibility(View.GONE);

                    if (userList.isEmpty()) {
                        binding.tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvEmpty.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void toggleUserStatus(User user) {
        String currentUid = auth.getCurrentUser().getUid();
        if (currentUid.equals(user.getUid())) {
            Toast.makeText(this, "Không thể tự khóa tài khoản của chính mình", Toast.LENGTH_SHORT).show();
            return;
        }

        String newStatus = "active".equals(user.getStatus()) ? "locked" : "active";
        String message = "active".equals(user.getStatus()) ? "khóa" : "mở khóa";

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Bạn có chắc muốn " + message + " tài khoản " + user.getEmail() + "?")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    db.collection("users").document(user.getUid())
                            .update("status", newStatus)
                            .addOnSuccessListener(v -> {
                                user.setStatus(newStatus);
                                adapter.notifyDataSetChanged();
                                Toast.makeText(this, "Đã " + message + " tài khoản", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void confirmDeleteUser(User user) {
        String currentUid = auth.getCurrentUser().getUid();
        if (currentUid.equals(user.getUid())) {
            Toast.makeText(this, "Không thể tự xóa tài khoản của chính mình", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xóa tài khoản")
                .setMessage("Bạn có chắc muốn xóa tài khoản " + user.getEmail() + "? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Xóa user khỏi Firestore
                    db.collection("users").document(user.getUid())
                            .delete()
                            .addOnSuccessListener(v -> {
                                // Có thể xóa luôn tài khoản Firebase Auth (cần quyền admin)
                                userList.remove(user);
                                adapter.notifyDataSetChanged();
                                Toast.makeText(this, "Đã xóa tài khoản", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}