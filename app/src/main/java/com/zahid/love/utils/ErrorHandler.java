package com.zahid.love.utils;

import android.content.Context;
import android.util.Log;

import retrofit2.Response;

public class ErrorHandler {

    private static final String TAG = "ErrorHandler";

    public static String getErrorMessage(Exception exception) {
        if (exception instanceof java.net.SocketTimeoutException) {
            return "Request timeout. Please try again.";
        } else if (exception instanceof java.net.UnknownHostException) {
            return "No internet connection. Please check your network.";
        } else if (exception instanceof java.net.ConnectException) {
            return "Cannot connect to server. Please try again later.";
        } else if (exception instanceof java.io.IOException) {
            return "Network error occurred. Please check your connection.";
        } else {
            return "An unexpected error occurred: " + exception.getMessage();
        }
    }

    public static String getApiErrorMessage(Response<?> response) {
        switch (response.code()) {
            case 400:
                return "Bad request. Please check your input.";
            case 401:
                return "Authentication failed. Please login again.";
            case 403:
                return "Access denied. You don't have permission.";
            case 404:
                return "Resource not found.";
            case 422:
                return "Validation error. Please check your input.";
            case 429:
                return "Too many requests. Please try again later.";
            case 500:
                return "Server error. Please try again later.";
            case 502:
                return "Bad gateway. Server is temporarily unavailable.";
            case 503:
                return "Service unavailable. Please try again later.";
            default:
                return "Request failed: " + response.message();
        }
    }

    public static void logError(String tag, String message, Exception exception) {
        Log.e(tag, message, exception);
    }

    public static void handleError(Context context, Exception exception, String defaultMessage) {
        String errorMessage = getErrorMessage(exception);
        logError(TAG, defaultMessage, exception);
        ToastUtils.showError(context, errorMessage);
    }

    public static void handleApiError(Context context, Response<?> response) {
        String errorMessage;
        
        try {
            if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();
                com.google.gson.JsonObject errorObject = com.google.gson.JsonParser.parseString(errorJson).getAsJsonObject();
                
                if (errorObject.has("message")) {
                    errorMessage = errorObject.get("message").getAsString();
                } else {
                    errorMessage = getApiErrorMessage(response);
                }
            } else {
                errorMessage = getApiErrorMessage(response);
            }
        } catch (Exception e) {
            errorMessage = getApiErrorMessage(response);
        }
        
        Log.e(TAG, "API Error: " + response.code() + " - " + response.message());
        ToastUtils.showError(context, errorMessage);
    }
}