package com.zahid.love.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.zahid.love.R;
import com.zahid.love.api.ApiClient;
import com.zahid.love.models.api.AuthResponse;
import com.zahid.love.models.api.RegisterRequest;
import com.zahid.love.utils.ApiUtils;
import com.zahid.love.utils.NetworkUtils;
import com.zahid.love.utils.ValidationUtils;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText usernameEditText, emailEditText, passwordEditText, phoneEditText, referralCodeEditText;
    private EditText inGameNameEditText;
    private Button registerButton;
    private Button selectImageButton;
    private ImageView logoImageView;
    private TextView loginTextView;
    private ProgressBar progressBar;
    private CircleImageView profileImageView;
    
    private static final int PICK_IMAGE_REQUEST = 1;
    private android.net.Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_register);

            initializeViews();
            setupClickListeners();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error initializing registration", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        try {
            usernameEditText = findViewById(R.id.usernameEditText);
            emailEditText = findViewById(R.id.emailEditText);
            passwordEditText = findViewById(R.id.passwordEditText);
            phoneEditText = findViewById(R.id.phoneEditText);
            referralCodeEditText = findViewById(R.id.referralCodeEditText);
            inGameNameEditText = findViewById(R.id.inGameNameEditText);
            registerButton = findViewById(R.id.registerButton);
            selectImageButton = findViewById(R.id.selectImageButton);
            logoImageView = findViewById(R.id.logoImageView);
            loginTextView = findViewById(R.id.loginTextView);
            progressBar = findViewById(R.id.progressBar);
            profileImageView = findViewById(R.id.profileImageView);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
        }
    }

    private void setupClickListeners() {
        try {
            if (registerButton != null) {
                registerButton.setOnClickListener(v -> {
                    registerUser();
                });
            }
            
            if (selectImageButton != null) {
                selectImageButton.setOnClickListener(v -> selectImage());
            }

            if (loginTextView != null) {
                loginTextView.setOnClickListener(v -> finish());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners", e);
        }
    }
    
    private void selectImage() {
        android.content.Intent intent = new android.content.Intent();
        intent.setType("image/*");
        intent.setAction(android.content.Intent.ACTION_GET_CONTENT);
        startActivityForResult(android.content.Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE_REQUEST);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            profileImageView.setVisibility(android.view.View.VISIBLE);
            com.zahid.love.utils.ImageUtils.loadProfileImage(this, selectedImageUri.toString(), profileImageView);
        }
    }
    
    private void registerUser() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!validateInput()) {
            return;
        }
        
        showProgress(true);
        
        String username = usernameEditText.getText().toString().trim();
        String fullName = username; // Use username as full name for now
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String ign = inGameNameEditText.getText().toString().trim();
        String referralCode = referralCodeEditText.getText().toString().trim();
        
        RegisterRequest registerRequest = new RegisterRequest(username, fullName, email, phone, password, ign, referralCode);
        
        ApiClient.getApiService().register(registerRequest).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                showProgress(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    
                    if (authResponse.isSuccess()) {
                        // Save auth token
                        ApiClient.saveAuthToken(authResponse.getData().getToken());
                        
                        Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        
                        // Navigate to main activity
                        android.content.Intent intent = new android.content.Intent(RegisterActivity.this, com.zahid.love.activities.MainActivity.class);
                        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, authResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    ApiUtils.handleApiError(RegisterActivity.this, response);
                }
            }
            
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                showProgress(false);
                ApiUtils.handleApiFailure(RegisterActivity.this, t);
            }
        });
    }
    
    private boolean validateInput() {
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        
        if (android.text.TextUtils.isEmpty(username)) {
            usernameEditText.setError("Username is required");
            usernameEditText.requestFocus();
            return false;
        }
        
        if (username.length() < 3) {
            usernameEditText.setError("Username must be at least 3 characters");
            usernameEditText.requestFocus();
            return false;
        }
        
        if (android.text.TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return false;
        }
        
        if (!ValidationUtils.isValidEmail(email)) {
            emailEditText.setError("Please enter a valid email");
            emailEditText.requestFocus();
            return false;
        }
        
        if (android.text.TextUtils.isEmpty(phone)) {
            phoneEditText.setError("Phone number is required");
            phoneEditText.requestFocus();
            return false;
        }
        
        if (!ValidationUtils.isValidBangladeshiMobile(phone)) {
            phoneEditText.setError("Please enter a valid Bangladeshi mobile number");
            phoneEditText.requestFocus();
            return false;
        }
        
        if (android.text.TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return false;
        }
        
        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            passwordEditText.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void showProgress(boolean show) {
        try {
            if (progressBar != null) {
                progressBar.setVisibility(show ? android.view.View.VISIBLE : android.view.View.GONE);
            }
            if (registerButton != null) {
                registerButton.setEnabled(!show);
                registerButton.setText(show ? "REGISTERING..." : "REGISTER");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing progress", e);
        }
    }
}