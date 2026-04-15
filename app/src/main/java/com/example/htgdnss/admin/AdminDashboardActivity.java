package com.example.htgdnss.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.htgdnss.adapter.OrderAdapter;
import com.example.htgdnss.common.ProfileActivity;
import com.example.htgdnss.databinding.ActivityAdminDashboardBinding;
import com.example.htgdnss.model.Order;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

        // RecyclerView
        adapter = new OrderAdapter(latestOrders, order -> {
            Toast.makeText(this, "Admin đang xem đơn", Toast.LENGTH_SHORT).show();
        });

        binding.rvLatestOrders.setLayoutManager(new LinearLayoutManager(this));
        binding.rvLatestOrders.setAdapter(adapter);

        // ✅ Nút thêm sản phẩm
        binding.btnAddProduct.setOnClickListener(v -> {
            Toast.makeText(this, "Đi tới thêm sản phẩm", Toast.LENGTH_SHORT).show();
        });

        // ✅ Nút đơn hàng
        binding.btnOrders.setOnClickListener(v -> {
            Toast.makeText(this, "Đi tới danh sách đơn", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStatsAndLatest();
    }

    private void loadStatsAndLatest() {

        // ✅ Load số đơn hàng
        db.collection("orders").get()
                .addOnSuccessListener(snaps -> {
                    binding.tvOrders.setText(String.valueOf(snaps.size()));
                });

        // ✅ Load doanh thu (demo)
        db.collection("orders").get()
                .addOnSuccessListener(snaps -> {
                    int total = snaps.size() * 100000; // giả lập
                    binding.tvRevenue.setText(total + " đ");
                });

        // ✅ Load danh sách đơn gần đây
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