package com.example.htgdnss.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.htgdnss.R;
import com.example.htgdnss.model.Review;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private List<Review> reviews;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public ReviewAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (reviews == null || position >= reviews.size()) {
            return;
        }

        Review review = reviews.get(position);
        if (review == null) {
            return;
        }

        // SET TEXT - MỖI TEXTVIEW CHỈ 1 LẦN
        String buyerName = review.getBuyerName();
        holder.tvBuyerName.setText(buyerName != null ? buyerName : "Ẩn danh");

        holder.tvStars.setText(getStarString(review.getStars()));

        String content = review.getContent();
        holder.tvContent.setText(content != null ? content : "");

        holder.tvTime.setText(dateFormat.format(new Date(review.getCreatedAt())));

        // XỬ LÝ PHẢN HỒI CỦA NGƯỜI BÁN
        String sellerReply = review.getSellerReply();
        if (sellerReply != null && !sellerReply.isEmpty()) {
            holder.tvSellerReply.setVisibility(View.VISIBLE);
            holder.tvSellerReply.setText("Người bán: " + sellerReply);
        } else {
            holder.tvSellerReply.setVisibility(View.GONE);
        }

    }

    private String getStarString(int stars) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stars; i++) sb.append("⭐");
        for (int i = stars; i < 5; i++) sb.append("☆");
        return sb.toString();
    }

    @Override
    public int getItemCount() {
        return reviews != null ? reviews.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBuyerName, tvStars, tvContent, tvTime, tvSellerReply;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvBuyerName = itemView.findViewById(R.id.tvBuyerName);
            tvStars = itemView.findViewById(R.id.tvStars);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvSellerReply = itemView.findViewById(R.id.tvSellerReply);

            if (tvBuyerName == null) android.util.Log.e("ReviewAdapter", "tvBuyerName is null");
            if (tvStars == null) android.util.Log.e("ReviewAdapter", "tvStars is null");
            if (tvContent == null) android.util.Log.e("ReviewAdapter", "tvContent is null");
            if (tvTime == null) android.util.Log.e("ReviewAdapter", "tvTime is null");
            if (tvSellerReply == null) android.util.Log.e("ReviewAdapter", "tvSellerReply is null");

        }
    }
}