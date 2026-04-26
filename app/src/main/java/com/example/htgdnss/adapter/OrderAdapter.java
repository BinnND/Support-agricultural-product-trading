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

    // THÊM INTERFACE CHO ĐÁNH GIÁ
    public interface OnReviewClick {
        void onReview(Order order);
    }

    private final List<Order> items;
    private final OnCancelClick onCancelClick;
    private final OnReviewClick onReviewClick;  // THÊM
    private final DecimalFormat df = new DecimalFormat("#,###");

    // SỬA CONSTRUCTOR - THÊM THAM SỐ onReviewClick
    public OrderAdapter(List<Order> items, OnCancelClick onCancelClick, OnReviewClick onReviewClick) {
        this.items = items;
        this.onCancelClick = onCancelClick;
        this.onReviewClick = onReviewClick;
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
        holder.binding.tvStatus.setText("Trạng thái: " + getStatusText(o.getStatus()));
        holder.binding.tvTotal.setText("Tổng: " + df.format(o.getTotalPrice()) + " đ");

        if (!TextUtils.isEmpty(o.getProductImageUrl())) {
            Glide.with(holder.binding.ivImage.getContext()).load(o.getProductImageUrl()).centerCrop().into(holder.binding.ivImage);
        }

        // Nút hủy đơn (chỉ hiển thị khi pending)
        boolean canCancel = "pending".equals(o.getStatus());
        holder.binding.btnCancel.setVisibility(canCancel ? View.VISIBLE : View.GONE);
        holder.binding.btnCancel.setOnClickListener(v -> {
            if (onCancelClick != null) onCancelClick.onCancel(o);
        });

        // Nút đánh giá (chỉ hiển thị khi done)
        boolean canReview = "done".equals(o.getStatus());
        holder.binding.btnReview.setVisibility(canReview ? View.VISIBLE : View.GONE);
        holder.binding.btnReview.setOnClickListener(v -> {
            if (onReviewClick != null) onReviewClick.onReview(o);
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

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}