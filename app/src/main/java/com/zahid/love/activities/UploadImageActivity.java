package com.zahid.love.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.zahid.love.R;

public class UploadImageActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView selectedImageView, backgroundLogoImageView;
    private TextView titleTextView, descriptionTextView;
    private MaterialButton selectImageButton, uploadButton;
    private ProgressBar progressBar;
    
    private Uri selectedImageUri;
    private String uploadType; // "uid", "transaction", "profile"
    private String referenceId; // tournament_id or transaction_id

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        initializeViews();
        getIntentData();
        setupClickListeners();
        setupUI();
    }

    private void initializeViews() {
        selectedImageView = findViewById(R.id.selectedImageView);
        backgroundLogoImageView = findViewById(R.id.backgroundLogoImageView);
        titleTextView = findViewById(R.id.titleTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        selectImageButton = findViewById(R.id.selectImageButton);
        uploadButton = findViewById(R.id.uploadButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void getIntentData() {
        uploadType = getIntent().getStringExtra("upload_type");
        referenceId = getIntent().getStringExtra("reference_id");
        
        if (uploadType == null) {
            uploadType = "general";
        }
    }

    private void setupClickListeners() {
        selectImageButton.setOnClickListener(v -> selectImage());
        uploadButton.setOnClickListener(v -> uploadImage());
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void setupUI() {
        switch (uploadType) {
            case "uid":
                titleTextView.setText("Upload UID Screenshot");
                descriptionTextView.setText("Please upload a clear screenshot of your game UID/ID for verification.");
                break;
            case "transaction":
                titleTextView.setText("Upload Transaction Proof");
                descriptionTextView.setText("Please upload a screenshot of your payment transaction for verification.");
                break;
            case "profile":
                titleTextView.setText("Upload Profile Picture");
                descriptionTextView.setText("Please select a clear profile picture.");
                break;
            default:
                titleTextView.setText("Upload Image");
                descriptionTextView.setText("Please select an image to upload.");
                break;
        }
        
        uploadButton.setEnabled(false);
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            
            // Display selected image
            Glide.with(this)
                    .load(selectedImageUri)
                    .centerCrop()
                    .into(selectedImageView);
            
            selectedImageView.setVisibility(View.VISIBLE);
            uploadButton.setEnabled(true);
        }
    }

    private void uploadImage() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        
        if (uploadType.equals("uid") || uploadType.equals("screenshot")) {
            // Upload tournament proof
            com.zahid.love.utils.FileUploadUtils.uploadTournamentProof(this, selectedImageUri, referenceId, uploadType, new com.zahid.love.utils.FileUploadUtils.FileUploadCallback() {
                @Override
                public void onUploadSuccess(String imageUrl) {
                    showLoading(false);
                    Toast.makeText(UploadImageActivity.this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
                    
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("upload_success", true);
                    resultIntent.putExtra("image_url", imageUrl);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
                
                @Override
                public void onUploadProgress(int progress) {
                    // Update progress if needed
                }
                
                @Override
                public void onUploadFailed(String error) {
                    showLoading(false);
                    Toast.makeText(UploadImageActivity.this, "Upload failed: " + error, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // Upload avatar
            com.zahid.love.utils.FileUploadUtils.uploadAvatar(this, selectedImageUri, new com.zahid.love.utils.FileUploadUtils.FileUploadCallback() {
                @Override
                public void onUploadSuccess(String imageUrl) {
                    showLoading(false);
                    Toast.makeText(UploadImageActivity.this, "Profile picture uploaded successfully!", Toast.LENGTH_SHORT).show();
                    
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("upload_success", true);
                    resultIntent.putExtra("image_url", imageUrl);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
                
                @Override
                public void onUploadProgress(int progress) {
                    // Update progress if needed
                }
                
                @Override
                public void onUploadFailed(String error) {
                    showLoading(false);
                    Toast.makeText(UploadImageActivity.this, "Upload failed: " + error, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        selectImageButton.setEnabled(!show);
        uploadButton.setEnabled(!show && selectedImageUri != null);
    }
}