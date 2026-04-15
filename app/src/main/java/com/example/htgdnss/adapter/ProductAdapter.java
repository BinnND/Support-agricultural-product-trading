package com.example.htgdnss.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.htgdnss.databinding.ItemProductBinding;
import com.example.htgdnss.model.Product;

import java.text.DecimalFormat;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.VH> {

    public interface OnProductClick {
        void onClick(Product product);
    }

    private final List<Product> items;
    private final OnProductClick onClick;
    private final DecimalFormat df = new DecimalFormat("#,###");

    public ProductAdapter(List<Product> items, OnProductClick onClick) {
        this.items = items;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding binding = ItemProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Product p = items.get(position);
        holder.binding.tvName.setText(nvl(p.getName()));

        holder.binding.tvPrice.setText(df.format(p.getPrice()) + " đ / " + nvl(p.getUnit()));
        holder.binding.tvStock.setText("Kho: " + p.getStock());

        String url = p.getImageUrl();
        if (url != null && !url.isEmpty()) {
            Glide.with(holder.binding.ivProduct.getContext()).load(url).centerCrop().into(holder.binding.ivProduct);
        } else if (p.getImageBase64() != null && !p.getImageBase64().isEmpty()) {
            try {
                byte[] bytes = Base64.decode(p.getImageBase64(), Base64.DEFAULT);
                // Sửa ivImage thành ivProduct
                Glide.with(holder.binding.ivProduct.getContext()).load(bytes).centerCrop().into(holder.binding.ivProduct);
            } catch (Exception ignored) {
            }
        }

        holder.binding.getRoot().setOnClickListener(v -> {
            if (onClick != null) onClick.onClick(p);
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemProductBinding binding;

        VH(ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private String buildMeta(Product p) {
        StringBuilder sb = new StringBuilder();
        append(sb, p.getCategory());
        append(sb, p.getLocation());
        append(sb, p.getCertification());
        return sb.toString();
    }

    private void append(StringBuilder sb, String s) {
        if (s == null || s.trim().isEmpty()) return;
        if (sb.length() > 0) sb.append(" • ");
        sb.append(s.trim());
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}
