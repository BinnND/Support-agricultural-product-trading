package com.example.htgdnss.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.htgdnss.adapter.OrderAdapter;
import com.example.htgdnss.databinding.ActivityManageAllOrdersBinding;
import com.example.htgdnss.model.Order;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ManageAllOrdersActivity extends AppCompatActivity {

    private ActivityManageAllOrdersBinding binding;
    private FirebaseFirestore db;
    private OrderAdapter adapter;
    private List<Order> orders = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageAllOrdersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tất cả đơn hàng");
        }

        db = FirebaseFirestore.getInstance();

        adapter = new OrderAdapter(orders,
                order -> {
                    Toast.makeText(this, "Admin không thể hủy đơn", Toast.LENGTH_SHORT).show();
                },
                order -> {
                    Toast.makeText(this, "Admin không thể đánh giá", Toast.LENGTH_SHORT).show();
                }
        );

        binding.rvOrders.setLayoutManager(new LinearLayoutManager(this));
        binding.rvOrders.setAdapter(adapter);

        loadAllOrders();
    }

    private void loadAllOrders() {
        binding.progressBar.setVisibility(View.VISIBLE);

        db.collection("orders")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snaps -> {
                    orders.clear();
                    for (var doc : snaps.getDocuments()) {
                        Order o = doc.toObject(Order.class);
                        if (o != null) {
                            if (o.getOrderId() == null) {
                                o.setOrderId(doc.getId());
                            }
                            orders.add(o);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    binding.progressBar.setVisibility(View.GONE);

                    if (orders.isEmpty()) {
                        binding.tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvEmpty.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}