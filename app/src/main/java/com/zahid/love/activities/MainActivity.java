package com.zahid.love.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.zahid.love.api.ApiClient;
import com.zahid.love.R;
import com.zahid.love.fragments.HomeDashboardFragment;
import com.zahid.love.fragments.DashboardFragment;
import com.zahid.love.fragments.NotificationFragment;
import com.zahid.love.fragments.ProfileFragment;
import com.zahid.love.fragments.WalletFragment;
import com.zahid.love.utils.DebugUtils;
import com.zahid.love.utils.NetworkUtils;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_main);

            Log.d(TAG, "MainActivity created");

            // Debug network state
            NetworkUtils.logNetworkState(this);

            initializeViews();
            setupBottomNavigation();

            // Load default fragment
            if (savedInstanceState == null) {
                loadFragment(new HomeDashboardFragment());
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in MainActivity onCreate", e);
            finish();
        }
    }

    private void initializeViews() {
        try {
            bottomNavigationView = findViewById(R.id.bottomNavigationView);
            fragmentManager = getSupportFragmentManager();

            // Verify user is logged in
            if (!ApiClient.isLoggedIn()) {
                Log.w(TAG, "User not logged in, redirecting to login");
                android.content.Intent intent = new android.content.Intent(this, LoginActivity.class);
                intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            }
            
            // Load user profile to verify token is still valid
            verifyUserSession();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
        }
    }
    
    private void verifyUserSession() {
        ApiClient.getApiService().getProfile().enqueue(new retrofit2.Callback<com.zahid.love.models.api.UserResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.zahid.love.models.api.UserResponse> call, retrofit2.Response<com.zahid.love.models.api.UserResponse> response) {
                if (!response.isSuccessful() || response.code() == 401) {
                    // Token expired or invalid, redirect to login
                    ApiClient.clearAuthToken();
                    android.content.Intent intent = new android.content.Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<com.zahid.love.models.api.UserResponse> call, Throwable t) {
                // Network error, continue with cached data
                Log.w(TAG, "Failed to verify session, continuing with cached data");
            }
        });
    }

    private void setupBottomNavigation() {
        try {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                Fragment selectedFragment = null;

                int itemId = item.getItemId();
                if (itemId == R.id.nav_dashboard) {
                    selectedFragment = new HomeDashboardFragment();
                } else if (itemId == R.id.nav_tournaments) {
                    selectedFragment = new DashboardFragment();
                } else if (itemId == R.id.nav_wallet) {
                    selectedFragment = new WalletFragment();
                } else if (itemId == R.id.nav_notifications) {
                    selectedFragment = new NotificationFragment();
                } else if (itemId == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                    return true;
                }
                return false;
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up bottom navigation", e);
        }
    }

    private void loadFragment(Fragment fragment) {
        try {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.frameLayout, fragment);
            transaction.commit();
        } catch (Exception e) {
            Log.e(TAG, "Error loading fragment", e);
        }
    }

    public void switchToTournamentsWithFilter(String gameFilter) {
        try {
            DashboardFragment dashboardFragment = new DashboardFragment();
            Bundle args = new Bundle();
            args.putString("game_filter", gameFilter);
            dashboardFragment.setArguments(args);

            loadFragment(dashboardFragment);
            bottomNavigationView.setSelectedItemId(R.id.nav_tournaments);
        } catch (Exception e) {
            Log.e(TAG, "Error switching to tournaments with filter", e);
        }
    }
}