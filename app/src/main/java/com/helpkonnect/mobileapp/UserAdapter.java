package com.helpkonnect.mobileapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<UserList> userList;

    public UserAdapter(List<UserList> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_list_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserList user = userList.get(position);
        holder.displayName.setText(user.getDisplayName());

        Glide.with(holder.itemView.getContext())
                .load(user.getImageUrl())
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.userprofileicon)
                .into(holder.profileImage);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView displayName;

        UserViewHolder(View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.user_profile_image);
            displayName = itemView.findViewById(R.id.user_display_name);
        }
    }
}