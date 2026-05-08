package com.example.htgdnss.buyer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.htgdnss.R;
import com.example.htgdnss.adapter.CartAdapter;
import com.example.htgdnss.model.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvProducts, tvTime, tvTotalPrice;
    private EditText edtName, edtPhone, edtAddress;
    private Button btnCancel, btnConfirm;

    private List<CartAdapter.CartItem> items;
    private Order order;
    private boolean readOnly;
    private final DecimalFormat df = new DecimalFormat("#,###");

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvProducts = findViewById(R.id.tvProducts);
        tvTime = findViewById(R.id.tvTime);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        btnCancel = findViewById(R.id.btnCancel);
        btnConfirm = findViewById(R.id.btnConfirm);

        order = (Order) getIntent().getSerializableExtra("order");
        readOnly = getIntent().getBooleanExtra("read_only", false);
        items = (List<CartAdapter.CartItem>) getIntent().getSerializableExtra("cart_list");
        if (items == null) items = new ArrayList<>();

        if (order != null) {
            showOrderDetail();
        } else {
            showCheckoutData();
        }

        btnCancel.setOnClickListener(v -> finish());
        btnConfirm.setOnClickListener(v -> {
            if (order != null) {
                handleOrderAction();
            } else {
                confirmOrder();
            }
        });
    }

    private void showCheckoutData() {
        ImageView imageView = findViewById(R.id.ivProduct);
        if (!items.isEmpty()) {
            Glide.with(this).load(items.get(0).imageUrl).into(imageView);
        }

        StringBuilder builder = new StringBuilder();
        double total = 0;
        for (CartAdapter.CartItem item : items) {
            builder.append("- ")
                    .append(item.name)
                    .append(", SL: ")
                    .append(item.quantity)
                    .append(" ")
                    .append(item.unit)
                    .append(", Giá: ")
                    .append(df.format(item.unitPrice))
                    .append(" đ\n");
            total += item.unitPrice * item.quantity;
        }

        tvProducts.setText(builder.toString());
        String time = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(new Date());
        tvTime.setText("Thời gian: " + time);
        tvTotalPrice.setText("Tổng tiền: " + df.format(total) + " đ");
    }

    private void showOrderDetail() {
        ImageView imageView = findViewById(R.id.ivProduct);
        if (order.getProductImageUrl() != null && !order.getProductImageUrl().isEmpty()) {
            Glide.with(this).load(order.getProductImageUrl()).centerCrop().into(imageView);
        }

        String product = "- " + nvl(order.getProductName())
                + "\nSL: " + order.getQuantity()
                + "\nĐơn giá: " + df.format(order.getUnitPrice()) + " đ";
        tvProducts.setText(product);

        String createdTime = order.getCreatedAt() > 0
                ? new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(new Date(order.getCreatedAt()))
                : "Không rõ";
        tvTime.setText("Trạng thái: " + getStatusText(order.getStatus()) + "\nThời gian đặt: " + createdTime);
        tvTotalPrice.setText("Tổng tiền: " + df.format(order.getTotalPrice()) + " đ");

        edtName.setText(nvl(order.getShippingName()));
        edtPhone.setText(nvl(order.getShippingPhone()));
        edtAddress.setText(nvl(order.getShippingAddress()));
        edtName.setEnabled(false);
        edtPhone.setEnabled(false);
        edtAddress.setEnabled(false);

        btnCancel.setText("ĐÓNG");
        btnConfirm.setEnabled(true);
        if (readOnly) {
            btnConfirm.setVisibility(View.GONE);
        } else if ("pending".equals(order.getStatus())) {
            btnConfirm.setText("HỦY ĐƠN");
            btnConfirm.setVisibility(View.VISIBLE);
        } else if ("done".equals(order.getStatus()) && !order.isReviewed()) {
            btnConfirm.setText("ĐÁNH GIÁ");
            btnConfirm.setVisibility(View.VISIBLE);
        } else {
            btnConfirm.setVisibility(View.GONE);
        }
    }

    private void confirmOrder() {
        if (auth.getCurrentUser() == null) return;
        if (items == null || items.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        String name = edtName.getText() == null ? "" : edtName.getText().toString().trim();
        String phone = edtPhone.getText() == null ? "" : edtPhone.getText().toString().trim();
        String address = edtAddress.getText() == null ? "" : edtAddress.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirm.setEnabled(false);
        long now = System.currentTimeMillis();

        db.runTransaction(transaction -> {
                    for (CartAdapter.CartItem item : items) {
                        var productRef = db.collection("products").document(item.productId);
                        var productDoc = transaction.get(productRef);
                        if (!productDoc.exists()) {
                            throw new IllegalStateException("Sản phẩm " + item.name + " không còn tồn tại");
                        }

                        Long stockValue = productDoc.getLong("stock");
                        int stock = stockValue == null ? 0 : stockValue.intValue();
                        if (item.quantity <= 0) {
                            throw new IllegalStateException("Số lượng sản phẩm không hợp lệ");
                        }
                        if (stock < item.quantity) {
                            throw new IllegalStateException(item.name + " chỉ còn " + stock + " sản phẩm");
                        }

                        int newStock = stock - item.quantity;
                        transaction.update(productRef, "stock", newStock, "inStock", newStock > 0, "updatedAt", now);

                        String orderId = UUID.randomUUID().toString();
                        Map<String, Object> orderData = new HashMap<>();
                        orderData.put("orderId", orderId);
                        orderData.put("buyerId", uid);
                        orderData.put("sellerId", item.sellerId);
                        orderData.put("productId", item.productId);
                        orderData.put("productName", item.name);
                        orderData.put("productImageUrl", item.imageUrl);
                        orderData.put("unitPrice", item.unitPrice);
                        orderData.put("quantity", item.quantity);
                        orderData.put("totalPrice", item.unitPrice * item.quantity);
                        orderData.put("shippingName", name);
                        orderData.put("shippingPhone", phone);
                        orderData.put("shippingAddress", address);
                        orderData.put("status", "pending");
                        orderData.put("cancelReason", "");
                        orderData.put("reviewed", false);
                        orderData.put("createdAt", now);
                        orderData.put("updatedAt", now);

                        transaction.set(db.collection("orders").document(orderId), orderData);
                        transaction.delete(db.collection("carts").document(uid).collection("items").document(item.productId));
                    }
                    return null;
                })
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnConfirm.setEnabled(true);
                    Toast.makeText(this, e.getMessage() != null ? e.getMessage() : "Đặt hàng thất bại", Toast.LENGTH_SHORT).show();
                });
    }

    private void handleOrderAction() {
        if ("pending".equals(order.getStatus())) {
            new AlertDialog.Builder(this)
                    .setTitle("Hủy đơn hàng")
                    .setMessage("Bạn chắc chắn muốn hủy đơn này?")
                    .setNegativeButton("Không", null)
                    .setPositiveButton("Hủy đơn", (dialog, which) -> cancelCurrentOrder())
                    .show();
        } else if ("done".equals(order.getStatus()) && !order.isReviewed()) {
            Intent intent = new Intent(this, AddReviewActivity.class);
            intent.putExtra("order", order);
            startActivity(intent);
            finish();
        }
    }

    private void cancelCurrentOrder() {
        if (order == null || order.getOrderId() == null) return;
        btnConfirm.setEnabled(false);
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
                    Toast.makeText(this, "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show();
                    showOrderDetail();
                })
                .addOnFailureListener(e -> {
                    btnConfirm.setEnabled(true);
                    Toast.makeText(this, e.getMessage() != null ? e.getMessage() : "Hủy thất bại", Toast.LENGTH_SHORT).show();
                });
    }

    private String getStatusText(String status) {
        if (status == null) return "Không xác định";
        switch (status) {
            case "pending": return "Chờ xác nhận";
            case "confirmed": return "Đã xác nhận";
            case "shipping": return "Đang giao hàng";
            case "done": return "Hoàn thành";
            case "cancelled": return "Đã hủy";
            default: return status;
        }
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }
}
