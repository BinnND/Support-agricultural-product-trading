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

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.VH> {

    public interface OnCancelClick {
        void onCancel(Order order);
    }

    public interface OnReviewClick {
        void onReview(Order order);
    }

    public interface OnOrderClick {
        void onOpen(Order order);
    }

    private final List<Order> items;
    private final OnCancelClick onCancelClick;
    private final OnReviewClick onReviewClick;
    private final OnOrderClick onOrderClick;
    private final DecimalFormat df = new DecimalFormat("#,###");

    public OrderAdapter(List<Order> items, OnCancelClick onCancelClick, OnReviewClick onReviewClick) {
        this(items, onCancelClick, onReviewClick, null);
    }

    public OrderAdapter(List<Order> items, OnCancelClick onCancelClick, OnReviewClick onReviewClick, OnOrderClick onOrderClick) {
        this.items = items;
        this.onCancelClick = onCancelClick;
        this.onReviewClick = onReviewClick;
        this.onOrderClick = onOrderClick;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderBinding binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Order order = items.get(position);

        holder.binding.tvName.setText(nvl(order.getProductName()));
        holder.binding.tvStatus.setText("Trạng thái: " + getStatusText(order.getStatus()));
        holder.binding.tvTotal.setText("SL: " + order.getQuantity() + " | Tổng: " + df.format(order.getTotalPrice()) + " đ");

        if (!TextUtils.isEmpty(order.getProductImageUrl())) {
            Glide.with(holder.binding.ivImage.getContext()).load(order.getProductImageUrl()).centerCrop().into(holder.binding.ivImage);
        } else {
            holder.binding.ivImage.setImageDrawable(null);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onOrderClick != null) onOrderClick.onOpen(order);
        });

        boolean canCancel = "pending".equals(order.getStatus());
        holder.binding.btnCancel.setVisibility(canCancel ? View.VISIBLE : View.GONE);
        holder.binding.btnCancel.setText("Hủy");
        holder.binding.btnCancel.setOnClickListener(v -> {
            if (onCancelClick != null) onCancelClick.onCancel(order);
        });

        boolean canReview = "done".equals(order.getStatus()) && !order.isReviewed();
        holder.binding.btnReview.setVisibility(canReview ? View.VISIBLE : View.GONE);
        holder.binding.btnReview.setOnClickListener(v -> {
            if (onReviewClick != null) onReviewClick.onReview(order);
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

    private String nvl(String value) {
        return value == null ? "" : value;
    }
}
