// UserManagementAdapter.java
package com.example.htgdnss.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.htgdnss.R;
import com.example.htgdnss.model.User;

import java.util.List;

public class UserManagementAdapter extends RecyclerView.Adapter<UserManagementAdapter.ViewHolder> {

    private List<User> users;
    private UserActionListener listener;

    public interface UserActionListener {
        void onLockUnlock(User user);
        void onDelete(User user);
    }

    public UserManagementAdapter(List<User> users, UserActionListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        android.view.View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_management, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);

        holder.tvEmail.setText(user.getEmail());
        holder.tvName.setText(user.getFullName() != null ? user.getFullName() : "Chưa có tên");
        holder.tvPhone.setText(user.getPhone() != null ? user.getPhone() : "Chưa có SĐT");
        holder.tvRole.setText("seller".equals(user.getRole()) ? "👨‍🌾 Người bán" : "👤 Người mua");

        boolean isLocked = "locked".equals(user.getStatus());
        holder.btnLockUnlock.setText(isLocked ? "Mở khóa" : "Khóa");
        holder.btnLockUnlock.setBackgroundColor(holder.itemView.getContext()
                .getColor(isLocked ? android.R.color.holo_green_dark : android.R.color.holo_orange_dark));

        holder.btnLockUnlock.setOnClickListener(v -> {
            if (listener != null) listener.onLockUnlock(user);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(user);
        });
    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmail, tvName, tvPhone, tvRole;
        Button btnLockUnlock, btnDelete;

        ViewHolder(android.view.View itemView) {
            super(itemView);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvRole = itemView.findViewById(R.id.tvRole);
            btnLockUnlock = itemView.findViewById(R.id.btnLockUnlock);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}