package com.zahid.love.models;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    private String id;

    @SerializedName("username")
    private String username;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

    @SerializedName("ign")
    private String ign;

    @SerializedName("avatar_url")
    private String avatarUrl;

    @SerializedName("referral_code")
    private String referralCode;

    @SerializedName("status")
    private String status;

    @SerializedName("wallet")
    private Wallet wallet;

    private double walletBalance;

    public User() {
        // Default constructor required for Gson
    }

    public User(String id, String username, String fullName, String email, String phone,
                String ign, String avatarUrl, String referralCode) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.ign = ign;
        this.avatarUrl = avatarUrl;
        this.referralCode = referralCode;
        this.status = "active";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getIgn() { return ign; }
    public void setIgn(String ign) { this.ign = ign; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getReferralCode() { return referralCode; }
    public void setReferralCode(String referralCode) { this.referralCode = referralCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Wallet getWallet() { return wallet; }
    public void setWallet(Wallet wallet) { this.wallet = wallet; }

    public double getWalletBalance() { return walletBalance; }

    public void setWalletBalance(double walletBalance) {
        this.walletBalance = walletBalance;
    }

    // Helper methods for backward compatibility
    public String getInGameName() { return ign; }
    public void setInGameName(String inGameName) { this.ign = inGameName; }

    public String getProfileImageUrl() { return avatarUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.avatarUrl = profileImageUrl; }

    public double getWalletBalanceFromWallet() {
        return wallet != null ? wallet.getBalance() : 0.0;
    }
    
    public void updateFromAuthResponse(AuthResponse.User authUser) {
        this.id = String.valueOf(authUser.getId());
        this.username = authUser.getUsername();
        this.fullName = authUser.getFullName();
        this.email = authUser.getEmail();
        this.phone = authUser.getPhone();
        this.ign = authUser.getIgn();
        this.avatarUrl = authUser.getAvatarUrl();
        this.referralCode = authUser.getReferralCode();
        this.status = authUser.getStatus();
        
        if (authUser.getWallet() != null) {
            this.walletBalance = authUser.getWallet().getBalance();
        }
    }

    public static class Wallet {
        @SerializedName("balance")
        private double balance;

        public double getBalance() { return balance; }
        public void setBalance(double balance) { this.balance = balance; }
    }
}