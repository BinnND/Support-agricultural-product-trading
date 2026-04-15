package com.example.htgdnss.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.htgdnss.databinding.ItemOrderBinding;
import com.example.htgdnss.model.Order;

import java.text.DecimalFormat;
import java.util.List;

public class SellerOrderAdapter extends RecyclerView.Adapter<SellerOrderAdapter.VH> {

    public interface ActionListener {
        void onAdvanceStatus(Order order);
    }

    private final List<Order> items;
    private final ActionListener actionListener;
    private final DecimalFormat df = new DecimalFormat("#,###");

    public SellerOrderAdapter(List<Order> items, ActionListener actionListener) {
        this.items = items;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderBinding binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Order o = items.get(position);
        holder.binding.tvName.setText(nvl(o.getProductName()));
        holder.binding.tvStatus.setText("Trạng thái: " + nvl(o.getStatus()));
        holder.binding.tvTotal.setText("Tổng: " + df.format(o.getTotalPrice()) + " đ");

        if (!TextUtils.isEmpty(o.getProductImageUrl())) {
            Glide.with(holder.binding.ivImage.getContext()).load(o.getProductImageUrl()).centerCrop().into(holder.binding.ivImage);
        }

        boolean actionable = "pending".equals(o.getStatus()) || "confirmed".equals(o.getStatus());
        holder.binding.btnCancel.setVisibility(actionable ? View.VISIBLE : View.GONE);
        holder.binding.btnCancel.setText(nextLabel(o.getStatus()));
        holder.binding.btnCancel.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onAdvanceStatus(o);
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemOrderBinding binding;
        VH(ItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private String nextLabel(String status) {
        if ("pending".equals(status)) return "Xác nhận";
        if ("confirmed".equals(status)) return "Giao xong";
        return "Cập nhật";
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}

