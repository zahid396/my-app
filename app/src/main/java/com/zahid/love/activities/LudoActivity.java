package com.zahid.love.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.zahid.love.api.ApiClient;
import com.zahid.love.models.api.ApiResponse;
import com.zahid.love.models.api.JoinTournamentRequest;
import com.zahid.love.R;
import com.zahid.love.adapters.LudoParticipantAdapter;
import com.zahid.love.models.Tournament;
import com.zahid.love.models.User;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LudoActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView ludoBoardImageView, backgroundLogoImageView;
    private TextView titleTextView, typeTextView, statusTextView;
    private TextView entryFeeTextView, prizePoolTextView, playersCountTextView;
    private TextView startTimeTextView, countdownTextView, roomCodeTextView;
    private MaterialButton joinButton, rulesButton, uploadScreenshotButton;
    private RecyclerView participantsRecyclerView;
    private LudoParticipantAdapter participantAdapter;
    private List<User> participantList;
    private ProgressBar progressBar;
    
    private String tournamentId;
    private Tournament tournament;
    private CountDownTimer countDownTimer;
    private DecimalFormat decimalFormat;
    private SimpleDateFormat dateFormat;
    private Uri selectedImageUri;
    private boolean isUserJoined = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ludo);

        initializeViews();
        getTournamentData();
        setupClickListeners();
        setupRecyclerView();
    }

    private void initializeViews() {
        ludoBoardImageView = findViewById(R.id.ludoBoardImageView);
        backgroundLogoImageView = findViewById(R.id.backgroundLogoImageView);
        titleTextView = findViewById(R.id.titleTextView);
        typeTextView = findViewById(R.id.typeTextView);
        statusTextView = findViewById(R.id.statusTextView);
        entryFeeTextView = findViewById(R.id.entryFeeTextView);
        prizePoolTextView = findViewById(R.id.prizePoolTextView);
        playersCountTextView = findViewById(R.id.playersCountTextView);
        startTimeTextView = findViewById(R.id.startTimeTextView);
        countdownTextView = findViewById(R.id.countdownTextView);
        roomCodeTextView = findViewById(R.id.roomCodeTextView);
        joinButton = findViewById(R.id.joinButton);
        rulesButton = findViewById(R.id.rulesButton);
        uploadScreenshotButton = findViewById(R.id.uploadScreenshotButton);
        participantsRecyclerView = findViewById(R.id.participantsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        
        participantList = new ArrayList<>();
        decimalFormat = new DecimalFormat("#,##0.00");
        dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
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
        joinButton.setOnClickListener(v -> {
            if (isUserJoined) {
                Toast.makeText(this, "You have already joined this tournament", Toast.LENGTH_SHORT).show();
            } else {
                joinTournament();
            }
        });
        
        rulesButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, RulesActivity.class);
            intent.putExtra("tournament_id", tournamentId);
            startActivity(intent);
        });
        
        uploadScreenshotButton.setOnClickListener(v -> {
            if (isUserJoined) {
                selectImage();
            } else {
                Toast.makeText(this, "Please join the tournament first", Toast.LENGTH_SHORT).show();
            }
        });
        
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        participantAdapter = new LudoParticipantAdapter(participantList);
        participantsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        participantsRecyclerView.setAdapter(participantAdapter);
    }

    private void loadTournamentDetails() {
        showLoading(true);
        
        // Load specific tournament from Laravel API
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
                            startCountdown();
                        } else {
                            Toast.makeText(LudoActivity.this, "Tournament not found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                } else {
                    Toast.makeText(LudoActivity.this, "Failed to load tournament", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<TournamentResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(LudoActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayTournamentDetails() {
        titleTextView.setText(tournament.getTitle());
        typeTextView.setText(tournament.getMode());
        statusTextView.setText(tournament.getStatus());
        entryFeeTextView.setText("Entry: ৳" + decimalFormat.format(tournament.getEntryFee()));
        prizePoolTextView.setText("Prize: ৳" + decimalFormat.format(tournament.getPrizePool()));
        playersCountTextView.setText(tournament.getParticipantsCount() + "/" + tournament.getMaxParticipants() + " Players");
        startTimeTextView.setText(tournament.getTimeUntilStart());
        
        // Show room code if available
        if (tournament.getRoomId() != null && !tournament.getRoomId().isEmpty()) {
            roomCodeTextView.setVisibility(View.VISIBLE);
            roomCodeTextView.setText("Room Code: " + tournament.getRoomId());
            if (tournament.getRoomPassword() != null && !tournament.getRoomPassword().isEmpty()) {
                roomCodeTextView.setText("Room: " + tournament.getRoomId() + " | Pass: " + tournament.getRoomPassword());
            }
        }
        
        // Load Ludo board image
        ludoBoardImageView.setImageResource(R.drawable.ludo_board);
        
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
    }

    private void joinTournament() {
        if (tournament.getParticipantsCount() >= tournament.getMaxParticipants()) {
            Toast.makeText(this, "Tournament is full", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        
        JoinTournamentRequest joinRequest = new JoinTournamentRequest("Player" + System.currentTimeMillis());
        
        ApiClient.getApiService().joinTournament(Integer.parseInt(tournamentId), joinRequest).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Object> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess()) {
                        isUserJoined = true;
                        updateJoinButton();
                        Toast.makeText(LudoActivity.this, "Successfully joined tournament!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LudoActivity.this, apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(LudoActivity.this, "Failed to join tournament", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(LudoActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateJoinButton() {
        if (isUserJoined) {
            joinButton.setText("JOINED");
            joinButton.setEnabled(false);
            joinButton.setBackgroundTintList(getResources().getColorStateList(R.color.neon_green));
            uploadScreenshotButton.setVisibility(View.VISIBLE);
        } else {
            joinButton.setText("JOIN TOURNAMENT");
            joinButton.setEnabled(true);
            joinButton.setBackgroundTintList(getResources().getColorStateList(R.color.neon_purple));
            uploadScreenshotButton.setVisibility(View.GONE);
        }
    }

    private void startCountdown() {
        // Use the time_until_start from API response
        countdownTextView.setText(tournament.getTimeUntilStart());
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Game Screenshot"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            uploadScreenshot();
        }
    }

    private void uploadScreenshot() {
        if (selectedImageUri == null) return;
        
        showLoading(true);
        
        com.zahid.love.utils.FileUploadUtils.uploadTournamentProof(this, selectedImageUri, tournamentId, "screenshot", new com.zahid.love.utils.FileUploadUtils.FileUploadCallback() {
            @Override
            public void onUploadSuccess(String imageUrl) {
                showLoading(false);
                Toast.makeText(LudoActivity.this, "Screenshot uploaded successfully!", Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onUploadProgress(int progress) {
                // Update progress if needed
            }
            
            @Override
            public void onUploadFailed(String error) {
                showLoading(false);
                Toast.makeText(LudoActivity.this, "Upload failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}