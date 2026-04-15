package com.example.htgdnss.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.htgdnss.databinding.ItemReviewBinding;
import com.example.htgdnss.model.Review;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.VH> {

    private final List<Review> items;

    public ReviewAdapter(List<Review> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReviewBinding binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Review r = items.get(position);
        holder.binding.tvBuyerName.setText(nvl(r.getBuyerName(), "Ẩn danh"));
        holder.binding.tvStars.setText(starText(r.getStars()));
        holder.binding.tvContent.setText(nvl(r.getContent(), ""));

        if (!TextUtils.isEmpty(r.getSellerReply())) {
            holder.binding.tvSellerReply.setVisibility(View.VISIBLE);
            holder.binding.tvSellerReply.setText("Người bán phản hồi: " + r.getSellerReply());
        } else {
            holder.binding.tvSellerReply.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemReviewBinding binding;

        VH(ItemReviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private String starText(int stars) {
        int s = Math.max(0, Math.min(5, stars));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s; i++) sb.append("★");
        for (int i = s; i < 5; i++) sb.append("☆");
        sb.append(" (").append(s).append("/5)");
        return sb.toString();
    }

    private String nvl(String s, String def) {
        return s == null ? def : s;
    }
}
