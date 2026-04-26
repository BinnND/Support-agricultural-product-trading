package com.example.htgdnss.buyer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.htgdnss.adapter.OrderAdapter;
import com.example.htgdnss.databinding.ActivityMyOrdersBuyerBinding;
import com.example.htgdnss.model.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyOrdersBuyerActivity extends AppCompatActivity {

    private ActivityMyOrdersBuyerBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private OrderAdapter adapter;
    private final List<Order> orders = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyOrdersBuyerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // SỬA: Truyền 2 listener
        adapter = new OrderAdapter(orders,
                this::cancelOrder,   // Hủy đơn
                this::reviewOrder    // Đánh giá
        );

        binding.rvOrders.setLayoutManager(new LinearLayoutManager(this));
        binding.rvOrders.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }

    private void loadOrders() {
        if (auth.getCurrentUser() == null) {
            finish();
            return;
        }
        String uid = auth.getCurrentUser().getUid();

        binding.layoutEmpty.setVisibility(View.GONE);
        db.collection("orders")
                .whereEqualTo("buyerId", uid)
                .limit(200)
                .get()
                .addOnSuccessListener(snaps -> {
                    orders.clear();
                    for (var doc : snaps.getDocuments()) {
                        Order o = doc.toObject(Order.class);
                        if (o != null) {
                            if (o.getOrderId() == null || o.getOrderId().isEmpty()) {
                                o.setOrderId(doc.getId());
                            }
                            orders.add(o);
                        }
                    }
                    Collections.sort(orders, (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                    adapter.notifyDataSetChanged();
                    binding.layoutEmpty.setVisibility(orders.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Tải đơn thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void cancelOrder(Order order) {
        if (order == null || order.getOrderId() == null) return;
        if (!"pending".equals(order.getStatus())) {
            Toast.makeText(this, "Chỉ hủy được đơn đang chờ", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("orders").document(order.getOrderId())
                .update("status", "cancelled", "updatedAt", System.currentTimeMillis(), "cancelReason", "Buyer cancelled")
                .addOnSuccessListener(v -> {
                    order.setStatus("cancelled");
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Hủy thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // PHƯƠNG THỨC ĐÁNH GIÁ
    private void reviewOrder(Order order) {
        if (order == null || order.getOrderId() == null) return;

        if (!"done".equals(order.getStatus())) {
            Toast.makeText(this, "Chỉ có thể đánh giá sau khi đơn hàng hoàn thành", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, AddReviewActivity.class);
        intent.putExtra("order", order);
        startActivity(intent);
    }
}