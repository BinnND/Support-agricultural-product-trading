package com.example.htgdnss.buyer;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.htgdnss.adapter.CartAdapter;
import com.example.htgdnss.databinding.ActivityCartBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CartActivity extends AppCompatActivity {

    private ActivityCartBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private CartAdapter adapter;
    private final List<CartAdapter.CartItem> items = new ArrayList<>();
    private final DecimalFormat df = new DecimalFormat("#,###");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        adapter = new CartAdapter(items, new CartAdapter.Listener() {
            @Override
            public void onQuantityChanged(CartAdapter.CartItem item, int newQty) {
                updateQty(item, newQty);
            }

            @Override
            public void onRemove(CartAdapter.CartItem item) {
                removeItem(item);
            }
        });

        binding.rvCart.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCart.setAdapter(adapter);

        binding.btnCheckout.setOnClickListener(v -> checkout());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCart();
    }

    private void loadCart() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String uid = auth.getCurrentUser().getUid();

        db.collection("carts").document(uid)
                .collection("items")
                .get()
                .addOnSuccessListener(snaps -> {
                    items.clear();
                    for (var doc : snaps.getDocuments()) {
                        CartAdapter.CartItem ci = doc.toObject(CartAdapter.CartItem.class);
                        if (ci != null) {
                            if (TextUtils.isEmpty(ci.productId)) ci.productId = doc.getId();
                            items.add(ci);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    updateTotal();
                    updateEmptyState();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Tải giỏ thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateEmptyState() {
        boolean empty = items.isEmpty();
        binding.tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.rvCart.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.btnCheckout.setEnabled(!empty);
    }

    private void updateTotal() {
        double total = 0;
        for (CartAdapter.CartItem i : items) {
            total += i.unitPrice * i.quantity;
        }
        binding.tvTotal.setText("Tổng: " + df.format(total) + " đ");
    }

    private void updateQty(CartAdapter.CartItem item, int newQty) {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();
        final int qty = Math.max(newQty, 1);

        Map<String, Object> data = new HashMap<>();
        data.put("quantity", qty);
        data.put("updatedAt", System.currentTimeMillis());

        db.collection("carts").document(uid)
                .collection("items").document(item.productId)
                .update(data)
                .addOnSuccessListener(v -> {
                    item.quantity = qty;
                    updateTotal();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void removeItem(CartAdapter.CartItem item) {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        db.collection("carts").document(uid)
                .collection("items").document(item.productId)
                .delete()
                .addOnSuccessListener(v -> {
                    items.remove(item);
                    adapter.notifyDataSetChanged();
                    updateTotal();
                    updateEmptyState();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Xóa thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void checkout() {
        if (auth.getCurrentUser() == null) return;
        if (items.isEmpty()) return;
        String buyerId = auth.getCurrentUser().getUid();

        binding.btnCheckout.setEnabled(false);

        // Tối giản: tạo đơn theo từng item (mỗi seller 1 đơn/item).
        List<Map<String, Object>> orders = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (CartAdapter.CartItem ci : items) {
            Map<String, Object> o = new HashMap<>();
            String orderId = UUID.randomUUID().toString();
            o.put("orderId", orderId);
            o.put("buyerId", buyerId);
            o.put("sellerId", ci.sellerId);
            o.put("productId", ci.productId);
            o.put("productName", ci.name);
            o.put("productImageUrl", ci.imageUrl);
            o.put("unitPrice", ci.unitPrice);
            o.put("quantity", ci.quantity);
            o.put("totalPrice", ci.unitPrice * ci.quantity);
            o.put("shippingName", "");
            o.put("shippingPhone", "");
            o.put("shippingAddress", "");
            o.put("status", "pending");
            o.put("cancelReason", "");
            o.put("createdAt", now);
            o.put("updatedAt", now);
            orders.add(o);
        }

        // Batch write
        var batch = db.batch();
        for (Map<String, Object> o : orders) {
            String orderId = (String) o.get("orderId");
            batch.set(db.collection("orders").document(orderId), o);
        }
        // clear cart
        for (CartAdapter.CartItem ci : items) {
            batch.delete(db.collection("carts").document(buyerId).collection("items").document(ci.productId));
        }

        batch.commit()
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                    items.clear();
                    adapter.notifyDataSetChanged();
                    updateTotal();
                    updateEmptyState();
                    binding.btnCheckout.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    binding.btnCheckout.setEnabled(true);
                    Toast.makeText(this, "Đặt hàng thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
