// ReviewSectionAdapter.java
package com.example.htgdnss.adapter;

import android.view.LayoutInflater;
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

public class ReviewSectionAdapter extends RecyclerView.Adapter<ReviewSectionAdapter.ViewHolder> {

    private List<Review> reviews;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public ReviewSectionAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        android.view.View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review_section, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = reviews.get(position);

        holder.tvBuyerName.setText(review.getBuyerName() != null ? review.getBuyerName() : "Ẩn danh");
        holder.tvStars.setText(getStarString(review.getStars()));
        holder.tvContent.setText(review.getContent());
        holder.tvTime.setText(dateFormat.format(new Date(review.getCreatedAt())));

        if (review.getSellerReply() != null && !review.getSellerReply().isEmpty()) {
            holder.tvSellerReply.setVisibility(android.view.View.VISIBLE);
            holder.tvSellerReply.setText("Người bán: " + review.getSellerReply());
        } else {
            holder.tvSellerReply.setVisibility(android.view.View.GONE);
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

        ViewHolder(android.view.View itemView) {
            super(itemView);
            tvBuyerName = itemView.findViewById(R.id.tvBuyerName);
            tvStars = itemView.findViewById(R.id.tvStars);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvSellerReply = itemView.findViewById(R.id.tvSellerReply);
        }
    }
}