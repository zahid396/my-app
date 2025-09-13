package com.zahid.love.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.zahid.love.api.ApiClient;
import com.zahid.love.models.api.ApiResponse;
import com.zahid.love.models.api.JoinTournamentRequest;
import com.zahid.love.models.api.JoinTournamentRequest;
import com.zahid.love.models.api.TournamentResponse;
import com.zahid.love.R;
import com.zahid.love.adapters.ParticipantAdapter;
import com.zahid.love.models.Tournament;
import com.zahid.love.models.User;
import com.zahid.love.utils.ApiUtils;
import com.zahid.love.utils.ClipboardUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TournamentActivity extends AppCompatActivity {

    private ImageView gameImageView, backgroundLogoImageView;
    private TextView titleTextView, gameTextView, typeTextView, statusTextView;
    private TextView entryFeeTextView, prizePoolTextView, playersCountTextView;
    private TextView startTimeTextView, countdownTextView;
    private MaterialButton joinButton, rulesButton;
    private RecyclerView participantsRecyclerView;
    private ParticipantAdapter participantAdapter;
    private List<User> participantList;
    private ProgressBar progressBar;
    
    private String tournamentId;
    private Tournament tournament;
    private DecimalFormat decimalFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tournament);

        initializeViews();
        getTournamentData();
        setupClickListeners();
        setupRecyclerView();
    }

    private void initializeViews() {
        gameImageView = findViewById(R.id.gameImageView);
        backgroundLogoImageView = findViewById(R.id.backgroundLogoImageView);
        titleTextView = findViewById(R.id.titleTextView);
        gameTextView = findViewById(R.id.gameTextView);
        typeTextView = findViewById(R.id.typeTextView);
        statusTextView = findViewById(R.id.statusTextView);
        entryFeeTextView = findViewById(R.id.entryFeeTextView);
        prizePoolTextView = findViewById(R.id.prizePoolTextView);
        playersCountTextView = findViewById(R.id.playersCountTextView);
        startTimeTextView = findViewById(R.id.startTimeTextView);
        countdownTextView = findViewById(R.id.countdownTextView);
        joinButton = findViewById(R.id.joinButton);
        rulesButton = findViewById(R.id.rulesButton);
        participantsRecyclerView = findViewById(R.id.participantsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        
        participantList = new ArrayList<>();
        decimalFormat = new DecimalFormat("#,##0.00");
    }

    private void getTournamentData() {
        tournamentId = getIntent().getStringExtra("tournament_id");
        if (tournamentId == null) {
            Toast.makeText(this, "Tournament not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        loadTournamentDetails();
    }

    private void setupClickListeners() {
        joinButton.setOnClickListener(v -> showJoinDialog());
        
        rulesButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, RulesActivity.class);
            intent.putExtra("tournament_id", tournamentId);
            intent.putExtra("rules", tournament != null ? tournament.getRules() : "");
            startActivity(intent);
        });
        
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        participantAdapter = new ParticipantAdapter(participantList);
        participantsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        participantsRecyclerView.setAdapter(participantAdapter);
    }

    private void loadTournamentDetails() {
        showLoading(true);
        
        ApiClient.getApiService().getTournaments(null, null).enqueue(new Callback<TournamentResponse>() {
            @Override
            public void onResponse(Call<TournamentResponse> call, Response<TournamentResponse> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    TournamentResponse tournamentResponse = response.body();
                    
                    if (tournamentResponse.isSuccess() && tournamentResponse.getData() != null) {
                        // Find the tournament by ID
                        for (Tournament t : tournamentResponse.getData()) {
                            if (tournamentId.equals(t.getId())) {
                                tournament = t;
                                break;
                            }
                        }
                        
                        if (tournament != null) {
                            displayTournamentDetails();
                        } else {
                            Toast.makeText(TournamentActivity.this, "Tournament not found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(TournamentActivity.this, "Failed to load tournament", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    ApiUtils.handleApiError(TournamentActivity.this, response);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<TournamentResponse> call, Throwable t) {
                showLoading(false);
                ApiUtils.handleApiFailure(TournamentActivity.this, t);
                finish();
            }
        });
    }

    private void displayTournamentDetails() {
        titleTextView.setText(tournament.getTitle());
        gameTextView.setText(tournament.getGame());
        typeTextView.setText(tournament.getMode());
        statusTextView.setText(tournament.getStatus());
        entryFeeTextView.setText("Entry: ৳" + decimalFormat.format(tournament.getEntryFee()));
        prizePoolTextView.setText("Prize: ৳" + decimalFormat.format(tournament.getPrizePool()));
        playersCountTextView.setText(tournament.getParticipantsCount() + "/" + tournament.getMaxParticipants() + " Players");
        startTimeTextView.setText(tournament.getTimeUntilStart());
        countdownTextView.setText(tournament.getTimeUntilStart());
        
        // Load game image
        if (tournament.getGame().equals("Free Fire")) {
            gameImageView.setImageResource(R.drawable.free_fire_logo);
        } else if (tournament.getGame().equals("PUBG Mobile")) {
            gameImageView.setImageResource(R.drawable.app_logo);
        } else if (tournament.getGame().equals("Call of Duty Mobile")) {
            gameImageView.setImageResource(R.drawable.app_logo);
        }
        
        // Set status color
        int statusColor;
        switch (tournament.getStatus()) {
            case "Open":
                statusColor = getResources().getColor(R.color.neon_blue);
                break;
            case "Closed":
                statusColor = getResources().getColor(R.color.neon_green);
                break;
            case "Completed":
                statusColor = getResources().getColor(R.color.text_secondary);
                break;
            default:
                statusColor = getResources().getColor(R.color.text_primary);
                break;
        }
        statusTextView.setTextColor(statusColor);

        // Show room details if available
        if (tournament.getRoomId() != null && !tournament.getRoomId().isEmpty()) {
            showRoomDetails();
        } else {
            // Check if user has joined to show upload button
            checkUserJoinedStatus();
        }
    }
    
    private void checkUserJoinedStatus() {
        // Check if current user has joined this tournament
        ApiClient.getApiService().getMyTournaments().enqueue(new Callback<TournamentResponse>() {
            @Override
            public void onResponse(Call<TournamentResponse> call, Response<TournamentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TournamentResponse tournamentResponse = response.body();
                    
                    if (tournamentResponse.isSuccess() && tournamentResponse.getData() != null) {
                        for (Tournament joinedTournament : tournamentResponse.getData()) {
                            if (tournamentId.equals(joinedTournament.getId())) {
                                // User has joined, update UI
                                updateJoinedUI();
                                break;
                            }
                        }
                    }
                }
            }
            
            @Override
            public void onFailure(Call<TournamentResponse> call, Throwable t) {
                Log.e("TournamentActivity", "Failed to check join status", t);
            }
        });
    }
    
    private void updateJoinedUI() {
        if (joinButton != null) {
            joinButton.setText("JOINED");
            joinButton.setEnabled(false);
            joinButton.setBackgroundTintList(getResources().getColorStateList(R.color.neon_green));
        }
        
        // Add upload UID button
        if (rulesButton != null) {
            rulesButton.setText("UPLOAD UID");
            rulesButton.setOnClickListener(v -> {
                Intent intent = new Intent(TournamentActivity.this, com.zahid.love.activities.UploadImageActivity.class);
                intent.putExtra("upload_type", "uid");
                intent.putExtra("reference_id", tournamentId);
                startActivity(intent);
            });
        }
    }

    private void showJoinDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_ign_input, null);
        
        EditText ignEditText = dialogView.findViewById(R.id.ignEditText);
        
        builder.setView(dialogView)
                .setTitle("Join Tournament")
                .setPositiveButton("JOIN", (dialog, which) -> {
                    String ign = ignEditText.getText().toString().trim();
                    if (!TextUtils.isEmpty(ign)) {
                        joinTournament(ign);
                    } else {
                        Toast.makeText(this, "Please enter your in-game name", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void joinTournament(String ign) {
        if (tournament.getParticipantsCount() >= tournament.getMaxParticipants()) {
            Toast.makeText(this, "Tournament is full", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        
        JoinTournamentRequest joinRequest = new JoinTournamentRequest(ign);
        
        ApiClient.getApiService().joinTournament(Integer.parseInt(tournamentId), joinRequest).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Object> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(TournamentActivity.this, "Successfully joined tournament!", Toast.LENGTH_SHORT).show();
                        finish(); // Go back to refresh the tournament list
                    } else {
                        Toast.makeText(TournamentActivity.this, apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    ApiUtils.handleApiError(TournamentActivity.this, response);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                showLoading(false);
                ApiUtils.handleApiFailure(TournamentActivity.this, t);
            }
        });
    }

    private void showRoomDetails() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Room Details")
                .setMessage("Room ID: " + tournament.getRoomId() + 
                           "\nPassword: " + (tournament.getRoomPassword() != null ? tournament.getRoomPassword() : "No password"))
                .setPositiveButton("COPY ROOM ID", (dialog, which) -> {
                    ClipboardUtils.copyRoomId(this, tournament.getRoomId());
                })
                .setNegativeButton("COPY PASSWORD", (dialog, which) -> {
                    if (tournament.getRoomPassword() != null) {
                        ClipboardUtils.copyRoomPassword(this, tournament.getRoomPassword());
                    }
                })
                .setNeutralButton("CLOSE", null)
                .show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}