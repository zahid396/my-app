package com.zahid.love.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.zahid.love.api.ApiClient;
import com.zahid.love.R;
import com.zahid.love.activities.LoginActivity;
import com.zahid.love.activities.SettingsActivity;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private CircleImageView profileImageView;
    private ImageView backgroundLogoImageView;
    private TextView usernameTextView, emailTextView, phoneTextView;
    private MaterialButton joinedTournamentsButton, settingsButton, whatsappButton;
    private TextView appVersionTextView, aboutContentTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_profile, container, false);

            initializeViews(view);
            setupClickListeners();
            setupDefaultData();

            return view;
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView", e);
            return inflater.inflate(R.layout.empty_state_layout, container, false);
        }
    }

    private void initializeViews(View view) {
        try {
            profileImageView = view.findViewById(R.id.profileImageView);
            backgroundLogoImageView = view.findViewById(R.id.backgroundLogoImageView);
            usernameTextView = view.findViewById(R.id.usernameTextView);
            emailTextView = view.findViewById(R.id.emailTextView);
            phoneTextView = view.findViewById(R.id.phoneTextView);
            joinedTournamentsButton = view.findViewById(R.id.joinedTournamentsButton);
            settingsButton = view.findViewById(R.id.settingsButton);
            whatsappButton = view.findViewById(R.id.whatsappButton);
            appVersionTextView = view.findViewById(R.id.appVersionTextView);
            aboutContentTextView = view.findViewById(R.id.aboutContentTextView);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
        }
    }

    private void setupClickListeners() {
        try {
            if (joinedTournamentsButton != null) {
                joinedTournamentsButton.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(getContext(), com.zahid.love.activities.JoinedTournamentsActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "Error starting JoinedTournamentsActivity", e);
                    }
                });
            }

            if (settingsButton != null) {
                settingsButton.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(getContext(), SettingsActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "Error starting SettingsActivity", e);
                    }
                });
            }

            if (whatsappButton != null) {
                whatsappButton.setOnClickListener(v -> {
                    try {
                        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                        intent.setData(android.net.Uri.parse("https://wa.me/8801930119616"));
                        startActivity(intent);
                    } catch (Exception e) {
                        if (getContext() != null) {
                            android.widget.Toast.makeText(getContext(), "WhatsApp not installed", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners", e);
        }
    }

    private void setupDefaultData() {
        try {
            loadUserProfile();
            
            if (usernameTextView != null) {
                usernameTextView.setText("Gamer");
            }
            if (emailTextView != null) {
                emailTextView.setText("Loading...");
            }
            if (phoneTextView != null) {
                phoneTextView.setText("Loading...");
            }
            if (appVersionTextView != null) {
                appVersionTextView.setText("Version: 1.0.0");
            }
            if (aboutContentTextView != null) {
                aboutContentTextView.setText("eSports Ekattor - The ultimate gaming tournament platform for Free Fire and other games in Bangladesh.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up default data", e);
        }
    }
    
    private void loadUserProfile() {
        ApiClient.getApiService().getProfile().enqueue(new Callback<com.zahid.love.models.api.UserResponse>() {
            @Override
            public void onResponse(Call<com.zahid.love.models.api.UserResponse> call, Response<com.zahid.love.models.api.UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.zahid.love.models.api.UserResponse userResponse = response.body();
                    
                    if (userResponse.isSuccess()) {
                        com.zahid.love.models.User user = userResponse.getData();
                        updateUserInfo(user);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<com.zahid.love.models.api.UserResponse> call, Throwable t) {
                Log.e(TAG, "Failed to load user profile", t);
            }
        });
    }
    
    private void updateUserInfo(com.zahid.love.models.User user) {
        try {
            if (usernameTextView != null) {
                usernameTextView.setText(user.getUsername());
            }
            if (emailTextView != null) {
                emailTextView.setText(user.getEmail());
            }
            if (phoneTextView != null) {
                phoneTextView.setText(user.getPhone());
            }
            if (profileImageView != null && user.getAvatarUrl() != null) {
                com.zahid.love.utils.ImageUtils.loadProfileImage(getContext(), user.getAvatarUrl(), profileImageView);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating user info", e);
        }
    }
}