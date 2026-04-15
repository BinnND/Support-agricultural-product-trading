package com.example.htgdnss.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.htgdnss.databinding.ItemCartBinding;

import java.text.DecimalFormat;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.VH> {

    public static class CartItem {
        public String productId;
        public String name;
        public String imageUrl;
        public String unit;
        public double unitPrice;
        public int quantity;
        public String sellerId;
        public long updatedAt;

        public CartItem() {}
    }

    public interface Listener {
        void onQuantityChanged(CartItem item, int newQty);
        void onRemove(CartItem item);
    }

    private final List<CartItem> items;
    private final Listener listener;
    private final DecimalFormat df = new DecimalFormat("#,###");

    public CartAdapter(List<CartItem> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding binding = ItemCartBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        CartItem i = items.get(position);
        holder.binding.tvName.setText(nvl(i.name));
        holder.binding.tvPrice.setText(df.format(i.unitPrice) + " đ / " + nvl(i.unit));
        holder.binding.edtQty.setText(String.valueOf(Math.max(i.quantity, 1)));

        if (!TextUtils.isEmpty(i.imageUrl)) {
            Glide.with(holder.binding.ivImage.getContext()).load(i.imageUrl).centerCrop().into(holder.binding.ivImage);
        }

        holder.binding.edtQty.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) return;
            int qty = parseInt(holder.binding.edtQty.getText() == null ? "" : holder.binding.edtQty.getText().toString(), 1);
            if (listener != null) listener.onQuantityChanged(i, qty);
        });

        holder.binding.btnRemove.setOnClickListener(v -> {
            if (listener != null) listener.onRemove(i);
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemCartBinding binding;
        VH(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private int parseInt(String s, int def) {
        try {
            int v = Integer.parseInt(s.trim());
            return v <= 0 ? def : v;
        } catch (Exception e) {
            return def;
        }
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}

