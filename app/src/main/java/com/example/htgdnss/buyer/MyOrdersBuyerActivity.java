package com.example.htgdnss.buyer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.htgdnss.R;
import com.example.htgdnss.adapter.OrderAdapter;
import com.example.htgdnss.auth.BaseAuthActivity;
import com.example.htgdnss.databinding.ActivityMyOrdersBuyerBinding;
import com.example.htgdnss.model.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyOrdersBuyerActivity extends BaseAuthActivity {

    private ActivityMyOrdersBuyerBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private OrderAdapter adapter;
    private final List<Order> allOrders = new ArrayList<>();
    private final List<Order> visibleOrders = new ArrayList<>();
    private String selectedStatus = "all";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyOrdersBuyerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        adapter = new OrderAdapter(visibleOrders,
                this::confirmCancelOrder,
                this::reviewOrder,
                this::openOrderDetail
        );

        binding.rvOrders.setLayoutManager(new LinearLayoutManager(this));
        binding.rvOrders.setAdapter(adapter);
        setupStatusFilter();
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
                    allOrders.clear();
                    for (var doc : snaps.getDocuments()) {
                        Order order = doc.toObject(Order.class);
                        if (order != null) {
                            if (order.getOrderId() == null || order.getOrderId().isEmpty()) {
                                order.setOrderId(doc.getId());
                            }
                            allOrders.add(order);
                        }
                    }
                    Collections.sort(allOrders, (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                    applyFilter();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Tải đơn thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void setupStatusFilter() {
        binding.chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID) checkedId = R.id.chipAll;
            if (checkedId == R.id.chipPending) {
                selectedStatus = "pending";
            } else if (checkedId == R.id.chipProcessing) {
                selectedStatus = "processing";
            } else if (checkedId == R.id.chipDone) {
                selectedStatus = "done";
            } else if (checkedId == R.id.chipCancelled) {
                selectedStatus = "cancelled";
            } else {
                selectedStatus = "all";
            }
            applyFilter();
        });
    }

    private void applyFilter() {
        visibleOrders.clear();
        for (Order order : allOrders) {
            if (matchesSelectedFilter(order)) {
                visibleOrders.add(order);
            }
        }
        adapter.notifyDataSetChanged();
        binding.layoutEmpty.setVisibility(visibleOrders.isEmpty() ? View.VISIBLE : View.GONE);
        binding.rvOrders.setVisibility(visibleOrders.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private boolean matchesSelectedFilter(Order order) {
        if (order == null || "all".equals(selectedStatus)) return true;
        String status = order.getStatus();
        if ("processing".equals(selectedStatus)) {
            return "confirmed".equals(status) || "shipping".equals(status);
        }
        return selectedStatus.equals(status);
    }

    private void confirmCancelOrder(Order order) {
        if (order == null || order.getOrderId() == null) return;
        if (!"pending".equals(order.getStatus())) {
            Toast.makeText(this, "Chỉ hủy được đơn đang chờ xác nhận", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Hủy đơn hàng")
                .setMessage("Bạn chắc chắn muốn hủy đơn này?")
                .setNegativeButton("Không", null)
                .setPositiveButton("Hủy đơn", (dialog, which) -> cancelOrder(order))
                .show();
    }

    private void cancelOrder(Order order) {
        long now = System.currentTimeMillis();
        db.runTransaction(transaction -> {
                    var orderRef = db.collection("orders").document(order.getOrderId());
                    var orderDoc = transaction.get(orderRef);
                    String status = orderDoc.getString("status");
                    if (!"pending".equals(status)) {
                        throw new IllegalStateException("Chỉ hủy được đơn đang chờ xác nhận");
                    }

                    var productRef = db.collection("products").document(order.getProductId());
                    var productDoc = transaction.get(productRef);
                    if (productDoc.exists()) {
                        Long stockValue = productDoc.getLong("stock");
                        int stock = stockValue == null ? 0 : stockValue.intValue();
                        int newStock = stock + order.getQuantity();
                        transaction.update(productRef, "stock", newStock, "inStock", newStock > 0, "updatedAt", now);
                    }

                    transaction.update(orderRef, "status", "cancelled", "updatedAt", now, "cancelReason", "Người mua hủy đơn");
                    return null;
                })
                .addOnSuccessListener(v -> {
                    order.setStatus("cancelled");
                    order.setCancelReason("Người mua hủy đơn");
                    applyFilter();
                    Toast.makeText(this, "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage() != null ? e.getMessage() : "Hủy thất bại", Toast.LENGTH_SHORT).show());
    }

    private void reviewOrder(Order order) {
        if (order == null || order.getOrderId() == null) return;
        if (!"done".equals(order.getStatus())) {
            Toast.makeText(this, "Chỉ có thể đánh giá sau khi đơn hàng hoàn thành", Toast.LENGTH_SHORT).show();
            return;
        }
        if (order.isReviewed()) {
            Toast.makeText(this, "Đơn hàng này đã được đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, AddReviewActivity.class);
        intent.putExtra("order", order);
        startActivity(intent);
    }

    private void openOrderDetail(Order order) {
        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra("order", order);
        startActivity(intent);
    }
}
