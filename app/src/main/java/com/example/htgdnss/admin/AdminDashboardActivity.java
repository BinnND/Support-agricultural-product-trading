package com.example.htgdnss.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.htgdnss.R;
import com.example.htgdnss.adapter.OrderAdapter;
import com.example.htgdnss.buyer.MyOrdersBuyerActivity;
import com.example.htgdnss.common.ProfileActivity;
import com.example.htgdnss.databinding.ActivityAdminDashboardBinding;
import com.example.htgdnss.model.Order;
import com.example.htgdnss.seller.AddProductActivity;
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

        // SỬA: OrderAdapter cần 2 listener (hủy và đánh giá)
        // Admin không cần hủy hay đánh giá, truyền null hoặc empty handler
        adapter = new OrderAdapter(latestOrders,
                order -> {
                    // Admin xem đơn - không cho hủy
                    Toast.makeText(this, "Admin đang xem đơn", Toast.LENGTH_SHORT).show();
                },
                order -> {
                    // Admin không đánh giá
                    Toast.makeText(this, "Admin không thể đánh giá đơn hàng", Toast.LENGTH_SHORT).show();
                }
        );

        binding.rvLatestOrders.setLayoutManager(new LinearLayoutManager(this));
        binding.rvLatestOrders.setAdapter(adapter);

        // Nút thêm sản phẩm
        binding.btnAddProduct.setOnClickListener(v -> {
            startActivity(new Intent(this, AddProductActivity.class));
        });

        // Nút đơn hàng
        binding.btnOrders.setOnClickListener(v -> {
            startActivity(new Intent(this, MyOrdersBuyerActivity.class));
        });

        // Bottom Navigation
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_orders) {
                Toast.makeText(this, "Trang đơn hàng", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }

            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStatsAndLatest();
    }

    private void loadStatsAndLatest() {
        // Load số đơn hàng
        db.collection("orders").get()
                .addOnSuccessListener(snaps -> {
                    binding.tvOrders.setText(String.valueOf(snaps.size()));
                });

        // Load doanh thu (demo)
        db.collection("orders").get()
                .addOnSuccessListener(snaps -> {
                    int total = snaps.size() * 100000;
                    binding.tvRevenue.setText(total + " đ");
                });

        // Load danh sách đơn gần đây
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
                });
    }
}