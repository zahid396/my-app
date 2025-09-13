package com.zahid.love.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.zahid.love.R;
import com.zahid.love.adapters.JoinedTournamentAdapter;
import com.zahid.love.api.ApiClient;
import com.zahid.love.models.Tournament;
import com.zahid.love.models.api.TournamentResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JoinedTournamentsActivity extends AppCompatActivity {

    private static final String TAG = "JoinedTournamentsActivity";

    private RecyclerView joinedTournamentsRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private ImageView emptyStateImage;

    private JoinedTournamentAdapter adapter;
    private List<Tournament> tournamentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_joined_tournaments);

            initializeViews();
            setupRecyclerView();
            setupClickListeners();
            loadJoinedTournaments();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            finish();
        }
    }

    private void initializeViews() {
        try {
            joinedTournamentsRecyclerView = findViewById(R.id.joinedTournamentsRecyclerView);
            swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
            progressBar = findViewById(R.id.progressBar);
            emptyStateText = findViewById(R.id.emptyStateText);
            emptyStateImage = findViewById(R.id.emptyStateImage);

            tournamentList = new ArrayList<>();

            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setOnRefreshListener(this::loadJoinedTournaments);
                swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.neon_blue));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
        }
    }

    private void setupRecyclerView() {
        try {
            if (joinedTournamentsRecyclerView != null) {
                adapter = new JoinedTournamentAdapter(tournamentList);
                joinedTournamentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                joinedTournamentsRecyclerView.setAdapter(adapter);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView", e);
        }
    }

    private void setupClickListeners() {
        try {
            findViewById(R.id.backButton).setOnClickListener(v -> finish());
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners", e);
        }
    }

    private void loadJoinedTournaments() {
        showLoading(true);
        
        ApiClient.getApiService().getMyTournaments().enqueue(new Callback<TournamentResponse>() {
            @Override
            public void onResponse(Call<TournamentResponse> call, Response<TournamentResponse> response) {
                showLoading(false);
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    TournamentResponse tournamentResponse = response.body();
                    
                    if (tournamentResponse.isSuccess()) {
                        tournamentList.clear();
                        
                        if (tournamentResponse.getData() != null) {
                            tournamentList.addAll(tournamentResponse.getData());
                        }
                        
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                        
                        Log.d(TAG, "Joined tournaments loaded: " + tournamentList.size());
                    } else {
                        Log.e(TAG, "Failed to load joined tournaments: " + tournamentResponse.getMessage());
                    }
                } else {
                    Log.e(TAG, "API call failed: " + response.code());
                }
                
                updateEmptyState();
            }

            @Override
            public void onFailure(Call<TournamentResponse> call, Throwable t) {
                showLoading(false);
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                Log.e(TAG, "Failed to load joined tournaments", t);
                updateEmptyState();
            }
        });
    }

    private void showLoading(boolean show) {
        try {
            if (progressBar != null) {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing loading", e);
        }
    }

    private void updateEmptyState() {
        try {
            if (tournamentList.isEmpty()) {
                if (emptyStateImage != null) emptyStateImage.setVisibility(View.VISIBLE);
                if (emptyStateText != null) {
                    emptyStateText.setVisibility(View.VISIBLE);
                    emptyStateText.setText("You haven't joined any tournaments yet.\nJoin your first match to get started!");
                }
            } else {
                if (emptyStateImage != null) emptyStateImage.setVisibility(View.GONE);
                if (emptyStateText != null) emptyStateText.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating empty state", e);
        }
    }
}