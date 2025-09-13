package com.zahid.love.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String TAG = "ApiClient";
    private static final String BASE_URL = "https://api.esportsekattor.com/api/";
    private static final String PREFS_NAME = "auth_prefs";
    private static final String TOKEN_KEY = "auth_token";

    private static Retrofit retrofit = null;
    private static Context appContext;

    public static void initialize(Context context) {
        try {
            appContext = context.getApplicationContext();
            Log.d(TAG, "ApiClient initialized");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing ApiClient", e);
        }
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            try {
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);

                OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .addInterceptor(logging);

                // Add auth interceptor
                if (appContext != null) {
                    httpClient.addInterceptor(chain -> {
                        Request original = chain.request();
                        String token = getAuthToken();

                        if (token != null && !token.isEmpty()) {
                            Request.Builder requestBuilder = original.newBuilder()
                                    .header("Authorization", "Bearer " + token)
                                    .header("Accept", "application/json")
                                    .header("Content-Type", "application/json");

                            Request request = requestBuilder.build();
                            return chain.proceed(request);
                        }

                        return chain.proceed(original);
                    });
                }

                Gson gson = new GsonBuilder()
                        .setLenient()
                        .create();

                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(httpClient.build())
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();

                Log.d(TAG, "Retrofit client created successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error creating Retrofit client", e);
            }
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        try {
            return getClient().create(ApiService.class);
        } catch (Exception e) {
            Log.e(TAG, "Error creating ApiService", e);
            return null;
        }
    }

    public static void saveAuthToken(String token) {
        try {
            if (appContext != null) {
                SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                prefs.edit().putString(TOKEN_KEY, token).apply();
                Log.d(TAG, "Auth token saved");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving auth token", e);
        }
    }

    public static String getAuthToken() {
        try {
            if (appContext != null) {
                SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                return prefs.getString(TOKEN_KEY, null);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting auth token", e);
        }
        return null;
    }

    public static void clearAuthToken() {
        try {
            if (appContext != null) {
                SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                prefs.edit().remove(TOKEN_KEY).apply();
                Log.d(TAG, "Auth token cleared");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clearing auth token", e);
        }
    }

    public static boolean isLoggedIn() {
        try {
            String token = getAuthToken();
            return token != null && !token.isEmpty();
        } catch (Exception e) {
            Log.e(TAG, "Error checking login status", e);
            return false;
        }
    }
}