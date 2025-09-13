package com.zahid.love.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.zahid.love.api.ApiClient;
import com.zahid.love.models.api.ApiResponse;
import com.zahid.love.models.api.NotificationResponse;
import com.zahid.love.R;
import com.zahid.love.adapters.NotificationAdapter;
import com.zahid.love.models.Notification;
import com.zahid.love.utils.ApiUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends AppCompatActivity {

    private static final String TAG = "NotificationActivity";
    
    private RecyclerView notificationsRecyclerView;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private ImageView emptyStateImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        initializeViews();
        setupRecyclerView();
        setupClickListeners();
        loadNotifications();
    }

    private void initializeViews() {
        notificationsRecyclerView = findViewById(R.id.notificationsRecyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
        emptyStateText = findViewById(R.id.emptyStateText);
        emptyStateImage = findViewById(R.id.emptyStateImage);
        
        notificationList = new ArrayList<>();
        
        swipeRefreshLayout.setOnRefreshListener(this::loadNotifications);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.neon_blue));
    }

    private void setupRecyclerView() {
        notificationAdapter = new NotificationAdapter(notificationList);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationsRecyclerView.setAdapter(notificationAdapter);
    }

    private void setupClickListeners() {
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.markAllReadButton).setOnClickListener(v -> markAllNotificationsAsRead());
    }
    
    private void markAllNotificationsAsRead() {
        ApiClient.getApiService().markAllNotificationsAsRead().enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Object> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(NotificationActivity.this, "All notifications marked as read", Toast.LENGTH_SHORT).show();
                        
                        // Update local list
                        for (Notification notification : notificationList) {
                            notification.setRead(true);
                        }
                        notificationAdapter.notifyDataSetChanged();
                        
                        loadNotifications();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(NotificationActivity.this, "Failed to mark notifications as read", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadNotifications() {
        showLoading(true);
        
        ApiClient.getApiService().getNotifications(20, null).enqueue(new Callback<NotificationResponse>() {
            @Override
            public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    NotificationResponse notificationResponse = response.body();
                    
                    if (notificationResponse.isSuccess()) {
                        notificationList.clear();
                        
                        if (notificationResponse.getData() != null && 
                            notificationResponse.getData().getNotifications() != null) {
                            notificationList.addAll(notificationResponse.getData().getNotifications());
                        }
                        
                        notificationAdapter.notifyDataSetChanged();
                        updateEmptyState();
                        
                        Log.d(TAG, "Notifications loaded: " + notificationList.size());
                    } else {
                        Log.e(TAG, "Failed to load notifications: " + notificationResponse.getMessage());
                    }
                } else {
                    ApiUtils.handleApiError(NotificationActivity.this, response);
                }
            }

            @Override
            public void onFailure(Call<NotificationResponse> call, Throwable t) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                Log.e(TAG, "Notifications API call failed", t);
                ApiUtils.handleApiFailure(NotificationActivity.this, t);
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        notificationsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void updateEmptyState() {
        if (notificationList.isEmpty()) {
            emptyStateImage.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.VISIBLE);
            emptyStateText.setText("No notifications yet");
        } else {
            emptyStateImage.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.GONE);
        }
    }
}