package com.zahid.love.api;

import com.zahid.love.models.api.AuthResponse;
import com.zahid.love.models.api.UserResponse;
import com.zahid.love.models.api.TransactionResponse;
import com.zahid.love.models.api.DepositResponse;
import com.zahid.love.models.api.BannerResponse;
import com.zahid.love.models.api.ContentResponse;
import com.zahid.love.models.api.LeaderboardResponse;
import com.zahid.love.models.api.ApiResponse;
import com.zahid.love.models.api.LoginRequest;
import com.zahid.love.models.api.RegisterRequest;
import com.zahid.love.models.api.WalletResponse;
import com.zahid.love.models.api.TournamentResponse;
import com.zahid.love.models.api.GameResponse;
import com.zahid.love.models.api.NotificationResponse;
import com.zahid.love.models.api.DepositRequest;
import com.zahid.love.models.api.WithdrawRequest;
import com.zahid.love.models.api.JoinTournamentRequest;
import com.zahid.love.models.api.UpdateProfileRequest;
import com.zahid.love.models.api.FileUploadResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.DELETE;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Multipart;
import retrofit2.http.Part;


public interface ApiService {

    // Authentication endpoints
    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("auth/logout")
    Call<AuthResponse> logout();

    @GET("auth/me")
    Call<UserResponse> getProfile();

    @PUT("auth/profile")
    Call<UserResponse> updateProfile(@Body UpdateProfileRequest request);

    @PUT("auth/password")
    Call<AuthResponse> changePassword(@Body UpdateProfileRequest request);

    // Wallet endpoints
    @GET("wallet")
    Call<WalletResponse> getWallet();

    @GET("wallet/transactions")
    Call<TransactionResponse> getTransactions(@Query("per_page") int perPage, @Query("type") String type);

    @POST("wallet/deposit")
    Call<DepositResponse> initiateDeposit(@Body DepositRequest request);

    @POST("wallet/withdraw")
    Call<ApiResponse<Object>> requestWithdraw(@Body WithdrawRequest request);

    @GET("wallet/withdraw-requests")
    Call<ApiResponse<Object>> getWithdrawRequests();

    @DELETE("wallet/withdraw-requests/{id}")
    Call<ApiResponse<Object>> cancelWithdrawRequest(@Path("id") int id);

    // Tournament endpoints
    @GET("games")
    Call<GameResponse> getGames();

    @GET("tournaments")
    Call<TournamentResponse> getTournaments(@Query("mode_id") Integer modeId, @Query("game_id") Integer gameId);

    @POST("tournaments/{id}/join")
    Call<ApiResponse<Object>> joinTournament(@Path("id") int tournamentId, @Body JoinTournamentRequest request);

    @GET("tournaments/my")
    Call<TournamentResponse> getMyTournaments();

    // Content endpoints
    @GET("content/banners")
    Call<BannerResponse> getBanners();

    @GET("content/highlights")
    Call<ContentResponse> getHighlights();

    @GET("content/videos")
    Call<ContentResponse> getVideos();

    @GET("content/settings")
    Call<ContentResponse> getSettings(@Query("key") String key);

    // Notification endpoints
    @GET("notifications")
    Call<NotificationResponse> getNotifications(@Query("per_page") int perPage, @Query("priority") String priority);

    @PUT("notifications/{id}/read")
    Call<ApiResponse<Object>> markNotificationAsRead(@Path("id") int id);

    @PUT("notifications/read-all")
    Call<ApiResponse<Object>> markAllNotificationsAsRead();

    @GET("notifications/unread-count")
    Call<ApiResponse<Object>> getUnreadNotificationCount();

    // Leaderboard endpoints
    @GET("leaderboard")
    Call<LeaderboardResponse> getLeaderboard(@Query("limit") int limit);

    // File upload endpoints
    @Multipart
    @POST("files/upload-avatar")
    Call<FileUploadResponse> uploadAvatar(@Part MultipartBody.Part avatar);
    
    @Multipart
    @POST("files/upload-tournament-proof")
    Call<FileUploadResponse> uploadTournamentProof(@Part MultipartBody.Part image, 
                                                   @Part("tournament_id") RequestBody tournamentId,
                                                   @Part("type") RequestBody type);

    // Health and stats endpoints
    @GET("health")
    Call<ApiResponse<Object>> getHealth();

}