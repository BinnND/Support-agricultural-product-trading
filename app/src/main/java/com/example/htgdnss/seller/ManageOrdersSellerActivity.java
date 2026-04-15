package com.example.htgdnss.seller;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.htgdnss.adapter.SellerOrderAdapter;
import com.example.htgdnss.databinding.ActivityManageOrdersSellerBinding;
import com.example.htgdnss.model.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ManageOrdersSellerActivity extends AppCompatActivity {

    private ActivityManageOrdersSellerBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private SellerOrderAdapter adapter;
    private final List<Order> orders = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageOrdersSellerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        adapter = new SellerOrderAdapter(orders, this::advanceStatus);
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
                .whereEqualTo("sellerId", uid)
                .limit(200)
                .get()
                .addOnSuccessListener(snaps -> {
                    orders.clear();
                    for (var doc : snaps.getDocuments()) {
                        Order o = doc.toObject(Order.class);
                        if (o != null) {
                            if (o.getOrderId() == null || o.getOrderId().isEmpty()) o.setOrderId(doc.getId());
                            orders.add(o);
                        }
                    }
                    Collections.sort(orders, (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                    adapter.notifyDataSetChanged();
                    binding.layoutEmpty.setVisibility(orders.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Tải đơn thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void advanceStatus(Order order) {
        if (order == null || order.getOrderId() == null) return;

        String next;
        if ("pending".equals(order.getStatus())) next = "confirmed";
        else if ("confirmed".equals(order.getStatus())) next = "done";
        else return;

        db.collection("orders").document(order.getOrderId())
                .update("status", next, "updatedAt", System.currentTimeMillis())
                .addOnSuccessListener(v -> {
                    order.setStatus(next);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
