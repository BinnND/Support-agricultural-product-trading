package com.example.htgdnss.buyer;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.htgdnss.R;
import com.example.htgdnss.adapter.CartAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvProducts, tvTime, tvTotalPrice;
    private EditText edtName, edtPhone, edtAddress;
    private Button btnCancel, btnConfirm;

    private List<CartAdapter.CartItem> items;
    private final DecimalFormat df = new DecimalFormat("#,###");

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        // 🔥 init firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 🔥 ánh xạ view
        tvProducts = findViewById(R.id.tvProducts);
        tvTime = findViewById(R.id.tvTime);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);

        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);

        btnCancel = findViewById(R.id.btnCancel);
        btnConfirm = findViewById(R.id.btnConfirm);

        // 🔥 nhận data
        items = (List<CartAdapter.CartItem>) getIntent().getSerializableExtra("cart_list");

        if (items == null) items = new ArrayList<>();

        showData();

        btnCancel.setOnClickListener(v -> finish());

        btnConfirm.setOnClickListener(v -> confirmOrder());
    }

    private void showData() {
        ImageView imageView = findViewById(R.id.ivProduct);
        if (items != null && !items.isEmpty()) {
            Glide.with(this)
                    .load(items.get(0).imageUrl)
                    .into(imageView);
        }

        StringBuilder builder = new StringBuilder();
        double total = 0;

        for (CartAdapter.CartItem item : items) {
            builder.append("- ")
                    .append(item.name)
                    .append(", SL: ")
                    .append(item.quantity)
                    .append(item.unit)
                    .append(", Giá: ")
                    .append(df.format(item.unitPrice))
                    .append(" đ\n");

            total += item.unitPrice * item.quantity;
        }

        tvProducts.setText(builder.toString());

        String time = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
                .format(new Date());

        tvTime.setText("Thời gian: " + time);
        tvTotalPrice.setText("Tổng tiền: " + df.format(total) + " đ");
    }

    private void confirmOrder() {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        long now = System.currentTimeMillis();

        var batch = db.batch();

        for (CartAdapter.CartItem item : items) {
            String orderId = UUID.randomUUID().toString();

            Map<String, Object> order = new HashMap<>();
            order.put("orderId", orderId);
            order.put("buyerId", uid);
            order.put("sellerId", item.sellerId);
            order.put("productId", item.productId);
            order.put("productName", item.name);
            order.put("productImageUrl", item.imageUrl);
            order.put("unitPrice", item.unitPrice);
            order.put("quantity", item.quantity);
            order.put("totalPrice", item.unitPrice * item.quantity);

            order.put("shippingName", name);
            order.put("shippingPhone", phone);
            order.put("shippingAddress", address);

            order.put("status", "pending");
            order.put("createdAt", now);
            order.put("updatedAt", now);

            // 🔥 tạo order
            batch.set(db.collection("orders").document(orderId), order);

            // 🔥 xóa cart
            batch.delete(
                    db.collection("carts")
                            .document(uid)
                            .collection("items")
                            .document(item.productId)
            );
        }

        batch.commit()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                    finish(); // quay lại cart
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}