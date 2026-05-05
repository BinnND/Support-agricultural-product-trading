package com.example.htgdnss.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.htgdnss.R;
import com.example.htgdnss.adapter.OrderAdapter;
import com.example.htgdnss.common.ProfileActivity;
import com.example.htgdnss.common.SettingsActivity;
import com.example.htgdnss.databinding.ActivityAdminDashboardBinding;
import com.example.htgdnss.model.Order;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private ActivityAdminDashboardBinding binding;
    private FirebaseFirestore db;
    private OrderAdapter adapter;
    private final List<Order> latestOrders = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        // Khởi tạo adapter cho danh sách đơn hàng
        adapter = new OrderAdapter(latestOrders,
                order -> {
                    Toast.makeText(this, "Admin chỉ có quyền xem", Toast.LENGTH_SHORT).show();
                },
                order -> {
                    Toast.makeText(this, "Admin chỉ có quyền xem", Toast.LENGTH_SHORT).show();
                }
        );

        binding.rvLatestOrders.setLayoutManager(new LinearLayoutManager(this));
        binding.rvLatestOrders.setAdapter(adapter);

        // Nút quản lý người dùng
        binding.btnManageUsers.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageUsersActivity.class);
            startActivity(intent);
        });

        // Nút quản lý đơn hàng
        binding.btnManageOrders.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageAllOrdersActivity.class);
            startActivity(intent);
        });

        // SỬA BOTTOM NAVIGATION - KHÔNG CẦN inflateMenu
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_users) {
                startActivity(new Intent(this, ManageUsersActivity.class));
                return true;
            } else if (id == R.id.nav_products) {
                startActivity(new Intent(this, AdminProductsActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
        loadLatestOrders();
    }

    private void loadStats() {
        // Tổng số người dùng (không bao gồm admin)
        db.collection("users")
                .whereNotEqualTo("role", "admin")
                .get()
                .addOnSuccessListener(snaps -> {
                    binding.tvTotalUsers.setText(String.valueOf(snaps.size()));
                });

        // Tổng số đơn hàng
        db.collection("orders").get()
                .addOnSuccessListener(snaps -> {
                    binding.tvTotalOrders.setText(String.valueOf(snaps.size()));
                });
    }

    private void loadLatestOrders() {
        db.collection("orders")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(snaps -> {
                    latestOrders.clear();
                    for (var doc : snaps.getDocuments()) {
                        Order o = doc.toObject(Order.class);
                        if (o != null) {
                            if (o.getOrderId() == null) {
                                o.setOrderId(doc.getId());
                            }
                            latestOrders.add(o);
                        }
                    }
                    adapter.notifyDataSetChanged();

                    if (latestOrders.isEmpty()) {
                        binding.tvEmptyOrders.setVisibility(View.VISIBLE);
                        binding.rvLatestOrders.setVisibility(View.GONE);
                    } else {
                        binding.tvEmptyOrders.setVisibility(View.GONE);
                        binding.rvLatestOrders.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}