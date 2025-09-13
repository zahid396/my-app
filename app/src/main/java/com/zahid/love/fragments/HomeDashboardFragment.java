package com.zahid.love.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zahid.love.R;
import com.zahid.love.activities.TournamentActivity;
import com.zahid.love.activities.MainActivity;
import com.zahid.love.adapters.BannerAdapter;
import com.zahid.love.adapters.GameModeCardAdapter;
import com.zahid.love.adapters.MatchAdapter;
import com.zahid.love.api.ApiClient;
import com.zahid.love.models.Banner;
import com.zahid.love.models.GameMode;
import com.zahid.love.models.Tournament;
import com.zahid.love.models.Transaction;
import com.zahid.love.models.User;
import com.zahid.love.models.api.BannerResponse;
import com.zahid.love.models.api.GameResponse;
import com.zahid.love.models.api.TournamentResponse;
import com.zahid.love.models.api.UserResponse;
import com.zahid.love.models.api.WalletResponse;
import com.zahid.love.utils.ImageUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeDashboardFragment extends Fragment {

    private static final String TAG = "HomeDashboardFragment";

    private CircleImageView userProfileImageView;
    private TextView usernameTextView, walletBalanceTextView, joinedMatchesTextView, totalWinningsTextView, referralCodeTextView;
    private RecyclerView bannerRecyclerView, gameModeRecyclerView, recentTournamentsRecyclerView;
    private ProgressBar progressBar;
    private View emptyStateLayout;

    private BannerAdapter bannerAdapter;
    private GameModeCardAdapter gameModeAdapter;
    private MatchAdapter matchAdapter;
    
    private List<Banner> bannerList;
    private List<GameMode> gameModeList;
    private List<Tournament> tournamentList;
    
    private DecimalFormat decimalFormat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_home_dashboard, container, false);

            initializeViews(view);
            setupRecyclerViews();
            loadData();

            return view;
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView", e);
            return inflater.inflate(R.layout.empty_state_layout, container, false);
        }
    }

    private void initializeViews(View view) {
        try {
            userProfileImageView = view.findViewById(R.id.userProfileImageView);
            usernameTextView = view.findViewById(R.id.usernameTextView);
            walletBalanceTextView = view.findViewById(R.id.walletBalanceTextView);
            joinedMatchesTextView = view.findViewById(R.id.joinedMatchesTextView);
            totalWinningsTextView = view.findViewById(R.id.totalWinningsTextView);
            referralCodeTextView = view.findViewById(R.id.referralCodeTextView);
            bannerRecyclerView = view.findViewById(R.id.bannerRecyclerView);
            gameModeRecyclerView = view.findViewById(R.id.gameModeRecyclerView);
            recentTournamentsRecyclerView = view.findViewById(R.id.recentTournamentsRecyclerView);
            progressBar = view.findViewById(R.id.progressBar);
            emptyStateLayout = view.findViewById(R.id.emptyStateLayout);

            bannerList = new ArrayList<>();
            gameModeList = new ArrayList<>();
            tournamentList = new ArrayList<>();
            decimalFormat = new DecimalFormat("#,##0.00");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
        }
    }

    private void setupRecyclerViews() {
        try {
            // Banner RecyclerView
            if (bannerRecyclerView != null) {
                bannerAdapter = new BannerAdapter(bannerList);
                bannerRecyclerView.setLayoutManager(
                        new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
                );
                bannerRecyclerView.setAdapter(bannerAdapter);
            }

            // Game Mode RecyclerView
            if (gameModeRecyclerView != null) {
                gameModeAdapter = new GameModeCardAdapter(gameModeList, new GameModeCardAdapter.OnGameModeCardClickListener() {
                    @Override
                    public void onGameModeCardClick(GameMode gameMode) {
                        // Switch to tournaments tab with filter
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).switchToTournamentsWithFilter(gameMode.getGame());
                        }
                    }
                });
                gameModeRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                gameModeRecyclerView.setAdapter(gameModeAdapter);
            }

            // Recent Tournaments RecyclerView
            if (recentTournamentsRecyclerView != null) {
                matchAdapter = new MatchAdapter(tournamentList, new MatchAdapter.OnMatchClickListener() {
                    @Override
                    public void onMatchClick(Tournament tournament) {
                        Intent intent = new Intent(getContext(), TournamentActivity.class);
                        intent.putExtra("tournament_id", tournament.getId());
                        startActivity(intent);
                    }
                });
                recentTournamentsRecyclerView.setLayoutManager(
                        new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
                );
                recentTournamentsRecyclerView.setAdapter(matchAdapter);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerViews", e);
        }
    }

    private void loadData() {
        showLoading(true);
        loadUserProfile();
        loadWalletBalance();
        loadBanners();
        loadGames();
        loadRecentTournaments();
    }

    private void loadUserProfile() {
        ApiClient.getApiService().getProfile().enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse userResponse = response.body();
                    
                    if (userResponse.isSuccess()) {
                        User user = userResponse.getData();
                        updateUserInfo(user);
                        
                        // Load user stats
                        loadUserStats(user);
                    }
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e(TAG, "Failed to load user profile", t);
            }
        });
    }
    
    private void loadUserStats(User user) {
        // Load joined tournaments count
        ApiClient.getApiService().getMyTournaments().enqueue(new Callback<TournamentResponse>() {
            @Override
            public void onResponse(Call<TournamentResponse> call, Response<TournamentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TournamentResponse tournamentResponse = response.body();
                    
                    if (tournamentResponse.isSuccess()) {
                        int joinedCount = tournamentResponse.getData() != null ? tournamentResponse.getData().size() : 0;
                        if (joinedMatchesTextView != null) {
                            joinedMatchesTextView.setText(String.valueOf(joinedCount));
                        }
                    }
                }
            }
            
            @Override
            public void onFailure(Call<TournamentResponse> call, Throwable t) {
                Log.e(TAG, "Failed to load user stats", t);
            }
        });
        
        // Calculate total winnings from transactions
        ApiClient.getApiService().getTransactions(100, "credit").enqueue(new Callback<com.zahid.love.models.api.TransactionResponse>() {
            @Override
            public void onResponse(Call<com.zahid.love.models.api.TransactionResponse> call, Response<com.zahid.love.models.api.TransactionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.zahid.love.models.api.TransactionResponse transactionResponse = response.body();
                    
                    if (transactionResponse.isSuccess() && transactionResponse.getData() != null) {
                        double totalWinnings = 0.0;
                        for (Transaction transaction : transactionResponse.getData().getTransactions()) {
                            if (transaction.getReason() != null && transaction.getReason().toLowerCase().contains("prize")) {
                                totalWinnings += transaction.getAmount();
                            }
                        }
                        
                        if (totalWinningsTextView != null) {
                            totalWinningsTextView.setText("৳ " + decimalFormat.format(totalWinnings));
                        }
                    }
                }
            }
            
            @Override
            public void onFailure(Call<com.zahid.love.models.api.TransactionResponse> call, Throwable t) {
                Log.e(TAG, "Failed to load winnings", t);
            }
        });
    }

    private void loadWalletBalance() {
        ApiClient.getApiService().getWallet().enqueue(new Callback<WalletResponse>() {
            @Override
            public void onResponse(Call<WalletResponse> call, Response<WalletResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WalletResponse walletResponse = response.body();
                    
                    if (walletResponse.isSuccess()) {
                        double balance = walletResponse.getData().getBalance();
                        updateWalletBalance(balance);
                    }
                }
            }

            @Override
            public void onFailure(Call<WalletResponse> call, Throwable t) {
                Log.e(TAG, "Failed to load wallet balance", t);
            }
        });
    }

    private void loadBanners() {
        ApiClient.getApiService().getBanners().enqueue(new Callback<BannerResponse>() {
            @Override
            public void onResponse(Call<BannerResponse> call, Response<BannerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BannerResponse bannerResponse = response.body();
                    
                    if (bannerResponse.isSuccess()) {
                        bannerList.clear();
                        bannerList.addAll(bannerResponse.getData());
                        bannerAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<BannerResponse> call, Throwable t) {
                Log.e(TAG, "Failed to load banners", t);
            }
        });
    }

    private void loadGames() {
        ApiClient.getApiService().getGames().enqueue(new Callback<GameResponse>() {
            @Override
            public void onResponse(Call<GameResponse> call, Response<GameResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GameResponse gameResponse = response.body();
                    
                    if (gameResponse.isSuccess()) {
                        gameModeList.clear();
                        
                        // Convert games to game modes for display
                        for (GameResponse.Game game : gameResponse.getData()) {
                            for (GameResponse.GameMode mode : game.getModes()) {
                                GameMode gameMode = new GameMode();
                                gameMode.setId(String.valueOf(mode.getId()));
                                gameMode.setTitle(game.getName() + " - " + mode.getName());
                                gameMode.setGame(game.getName());
                                gameMode.setMode(mode.getName());
                                gameMode.setType(mode.getName());
                                gameModeList.add(gameMode);
                            }
                        }
                        
                        gameModeAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<GameResponse> call, Throwable t) {
                Log.e(TAG, "Failed to load games", t);
            }
        });
    }

    private void loadRecentTournaments() {
        ApiClient.getApiService().getTournaments(null, null).enqueue(new Callback<TournamentResponse>() {
            @Override
            public void onResponse(Call<TournamentResponse> call, Response<TournamentResponse> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    TournamentResponse tournamentResponse = response.body();
                    
                    if (tournamentResponse.isSuccess()) {
                        tournamentList.clear();
                        tournamentList.addAll(tournamentResponse.getData());
                        matchAdapter.notifyDataSetChanged();
                        
                        updateEmptyState();
                    }
                }
            }

            @Override
            public void onFailure(Call<TournamentResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Failed to load tournaments", t);
                updateEmptyState();
            }
        });
    }

    private void updateUserInfo(User user) {
        try {
            if (usernameTextView != null) {
                usernameTextView.setText(user.getUsername());
            }
            
            if (referralCodeTextView != null) {
                referralCodeTextView.setText(user.getReferralCode());
            }
            
            if (userProfileImageView != null && user.getAvatarUrl() != null) {
                ImageUtils.loadProfileImage(getContext(), user.getAvatarUrl(), userProfileImageView);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating user info", e);
        }
    }

    private void updateWalletBalance(double balance) {
        try {
            if (walletBalanceTextView != null) {
                walletBalanceTextView.setText("৳ " + decimalFormat.format(balance));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating wallet balance", e);
        }
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
                if (emptyStateLayout != null) {
                    emptyStateLayout.setVisibility(View.VISIBLE);
                }
            } else {
                if (emptyStateLayout != null) {
                    emptyStateLayout.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating empty state", e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        loadWalletBalance();
    }
}