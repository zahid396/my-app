package com.zahid.love.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.zahid.love.api.ApiClient;
import com.zahid.love.models.api.UpdateProfileRequest;
import com.zahid.love.models.api.UserResponse;
import com.zahid.love.R;
import com.zahid.love.models.User;
import com.zahid.love.utils.ApiUtils;
import com.zahid.love.utils.ImageUtils;

import de.hdodenhof.circleimageview.CircleImageView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private CircleImageView profileImageView;
    private EditText usernameEditText, inGameNameEditText, phoneEditText;
    private MaterialButton changeImageButton, saveButton;
    private ImageView backgroundLogoImageView;
    private ProgressBar progressBar;

    private Uri selectedImageUri;
    private String currentImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initializeViews();
        setupClickListeners();
        loadCurrentProfile();
    }

    private void initializeViews() {
        profileImageView = findViewById(R.id.profileImageView);
        usernameEditText = findViewById(R.id.usernameEditText);
        inGameNameEditText = findViewById(R.id.inGameNameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        changeImageButton = findViewById(R.id.changeImageButton);
        saveButton = findViewById(R.id.saveButton);
        backgroundLogoImageView = findViewById(R.id.backgroundLogoImageView);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        changeImageButton.setOnClickListener(v -> selectImage());
        saveButton.setOnClickListener(v -> saveProfile());
    }

    private void loadCurrentProfile() {
        showLoading(true);

        ApiClient.getApiService().getProfile().enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    UserResponse userResponse = response.body();
                    
                    if (userResponse.isSuccess()) {
                        User user = userResponse.getData();
                        
                        usernameEditText.setText(user.getFullName() != null ? user.getFullName() : user.getUsername());
                        inGameNameEditText.setText(user.getIgn() != null ? user.getIgn() : "");
                        phoneEditText.setText(user.getPhone() != null ? user.getPhone() : "");
                        currentImageUrl = user.getAvatarUrl();

                        if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                            ImageUtils.loadProfileImage(EditProfileActivity.this, currentImageUrl, profileImageView);
                        }
                    }
                } else {
                    ApiUtils.handleApiError(EditProfileActivity.this, response);
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                showLoading(false);
                ApiUtils.handleApiFailure(EditProfileActivity.this, t);
            }
        });
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            ImageUtils.loadProfileImage(this, selectedImageUri.toString(), profileImageView);
        }
    }

    private void saveProfile() {
        if (!validateInput()) {
            return;
        }

        showLoading(true);

        String fullName = usernameEditText.getText().toString().trim();
        String ign = inGameNameEditText.getText().toString().trim();
        String avatarUrl = currentImageUrl;
        
        // If new image selected, upload it first
        if (selectedImageUri != null) {
            uploadImageAndUpdateProfile(fullName, ign);
            return;
        }

        UpdateProfileRequest updateRequest = new UpdateProfileRequest(fullName, ign, avatarUrl);
        
        ApiClient.getApiService().updateProfile(updateRequest).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse userResponse = response.body();
                    
                    if (userResponse.isSuccess()) {
                        Toast.makeText(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditProfileActivity.this, userResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    ApiUtils.handleApiError(EditProfileActivity.this, response);
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                showLoading(false);
                ApiUtils.handleApiFailure(EditProfileActivity.this, t);
            }
        });
    }
    
    private void uploadImageAndUpdateProfile(String fullName, String ign) {
        com.zahid.love.utils.FileUploadUtils.uploadAvatar(this, selectedImageUri, new com.zahid.love.utils.FileUploadUtils.FileUploadCallback() {
            @Override
            public void onUploadSuccess(String imageUrl) {
                // Now update profile with new image URL
                UpdateProfileRequest updateRequest = new UpdateProfileRequest(fullName, ign, imageUrl);
                
                ApiClient.getApiService().updateProfile(updateRequest).enqueue(new Callback<UserResponse>() {
                    @Override
                    public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                        showLoading(false);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            UserResponse userResponse = response.body();
                            
                            if (userResponse.isSuccess()) {
                                Toast.makeText(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(EditProfileActivity.this, userResponse.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            ApiUtils.handleApiError(EditProfileActivity.this, response);
                        }
                    }

                    @Override
                    public void onFailure(Call<UserResponse> call, Throwable t) {
                        showLoading(false);
                        ApiUtils.handleApiFailure(EditProfileActivity.this, t);
                    }
                });
            }
            
            @Override
            public void onUploadProgress(int progress) {
                // Update progress if needed
            }
            
            @Override
            public void onUploadFailed(String error) {
                showLoading(false);
                Toast.makeText(EditProfileActivity.this, "Image upload failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateInput() {
        String username = usernameEditText.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Full name is required");
            usernameEditText.requestFocus();
            return false;
        }

        if (username.length() < 3) {
            usernameEditText.setError("Full name must be at least 3 characters");
            usernameEditText.requestFocus();
            return false;
        }

        return true;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        saveButton.setEnabled(!show);

        if (show) {
            saveButton.setText("SAVING...");
        } else {
            saveButton.setText("SAVE CHANGES");
        }
    }
}