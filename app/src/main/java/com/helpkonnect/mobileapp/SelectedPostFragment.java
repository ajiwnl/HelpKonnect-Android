package com.helpkonnect.mobileapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;

import java.io.Serializable;
import java.util.List;

public class SelectedPostFragment extends Fragment {

    private static final String ARG_POST = "post";

    private CommunityListAdapter.CommunityPost post;

    public static SelectedPostFragment newInstance(CommunityListAdapter.CommunityPost post) {
        SelectedPostFragment fragment = new SelectedPostFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_POST, (Serializable) post);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            post = (CommunityListAdapter.CommunityPost) getArguments().getSerializable(ARG_POST);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_selectedpost, container, false);

        ImageView userProfileImageView = rootView.findViewById(R.id.userProfileImageView);
        TextView usernameTextView = rootView.findViewById(R.id.usernameTextView);
        TextView captionTextView = rootView.findViewById(R.id.captionTextView);
        TextView heartTextView = rootView.findViewById(R.id.heartTextView);
        TextView timeTextView = rootView.findViewById(R.id.timeTextView);
        LinearLayout imageContainer = rootView.findViewById(R.id.imageContainer);

        Glide.with(this).load(post.getUserProfileImageUrl()).into(userProfileImageView);
        usernameTextView.setText(post.getUserPostName());
        captionTextView.setText(post.getUserPostDescription());
        heartTextView.setText(String.valueOf(post.getUserPostLikes()));
        timeTextView.setText(post.getUserPostDate());

        List<String> imageUrls = post.getImageUrls();
        if (imageUrls != null) {
            for (String imageUrl : imageUrls) {
                if (!imageUrl.isEmpty()) {
                    ImageView imageView = new ImageView(getContext());
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            getResources().getDisplayMetrics().widthPixels,
                            400
                    );
                    layoutParams.setMargins(8, 8, 8, 8);
                    imageView.setLayoutParams(layoutParams);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.userprofileicon)
                            .into(imageView);

                    imageContainer.addView(imageView);
                }
            }
        }

        return rootView;
    }
}
