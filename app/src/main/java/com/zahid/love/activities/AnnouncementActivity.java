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
import com.zahid.love.models.api.ContentResponse;
import com.zahid.love.R;
import com.zahid.love.adapters.AnnouncementAdapter;
import com.zahid.love.models.Announcement;
import com.zahid.love.utils.ApiUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnnouncementActivity extends AppCompatActivity {

    private static final String TAG = "AnnouncementActivity";
    
    private RecyclerView announcementsRecyclerView;
    private AnnouncementAdapter announcementAdapter;
    private List<Announcement> announcementList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private ImageView emptyStateImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement);

        initializeViews();
        setupRecyclerView();
        setupClickListeners();
        loadAnnouncements();
    }

    private void initializeViews() {
        announcementsRecyclerView = findViewById(R.id.announcementsRecyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
        emptyStateText = findViewById(R.id.emptyStateText);
        emptyStateImage = findViewById(R.id.emptyStateImage);
        
        announcementList = new ArrayList<>();
        
        swipeRefreshLayout.setOnRefreshListener(this::loadAnnouncements);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.neon_blue));
    }

    private void setupRecyclerView() {
        announcementAdapter = new AnnouncementAdapter(announcementList);
        announcementsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        announcementsRecyclerView.setAdapter(announcementAdapter);
    }

    private void setupClickListeners() {
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void loadAnnouncements() {
        showLoading(true);
        
        ApiClient.getApiService().getHighlights().enqueue(new Callback<ContentResponse>() {
            @Override
            public void onResponse(Call<ContentResponse> call, Response<ContentResponse> response) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ContentResponse contentResponse = response.body();
                    
                    if (contentResponse.isSuccess()) {
                        announcementList.clear();
                        
                        // Convert highlights to announcements
                        try {
                            if (contentResponse.getData() instanceof java.util.List) {
                                java.util.List<java.util.Map<String, Object>> highlights = (java.util.List<java.util.Map<String, Object>>) contentResponse.getData();
                                
                                for (java.util.Map<String, Object> highlight : highlights) {
                                    Announcement announcement = new Announcement();
                                    announcement.setId(String.valueOf(highlight.get("id")));
                                    announcement.setTitle((String) highlight.get("title"));
                                    announcement.setDescription((String) highlight.get("description"));
                                    announcement.setImageUrl((String) highlight.get("image_url"));
                                    announcement.setPriority("medium");
                                    announcement.setTimestamp(System.currentTimeMillis());
                                    
                                    announcementList.add(announcement);
                                }
                                
                                announcementAdapter.notifyDataSetChanged();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing highlights", e);
                        }
                        
                        updateEmptyState();
                    }
                } else {
                    ApiUtils.handleApiError(AnnouncementActivity.this, response);
                }
            }

            @Override
            public void onFailure(Call<ContentResponse> call, Throwable t) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                ApiUtils.handleApiFailure(AnnouncementActivity.this, t);
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        announcementsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void updateEmptyState() {
        if (announcementList.isEmpty()) {
            emptyStateImage.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.VISIBLE);
            emptyStateText.setText("No announcements yet");
        } else {
            emptyStateImage.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.GONE);
        }
    }
}