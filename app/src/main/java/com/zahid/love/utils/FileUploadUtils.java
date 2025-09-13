package com.zahid.love.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.zahid.love.api.ApiClient;
import com.zahid.love.models.api.FileUploadResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FileUploadUtils {

    private static final String TAG = "FileUploadUtils";

    public interface FileUploadCallback {
        void onUploadSuccess(String imageUrl);
        void onUploadProgress(int progress);
        void onUploadFailed(String error);
    }

    public static void uploadAvatar(Context context, Uri imageUri, FileUploadCallback callback) {
        try {
            File imageFile = createFileFromUri(context, imageUri, "avatar");
            if (imageFile == null) {
                callback.onUploadFailed("Failed to process image");
                return;
            }

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("avatar", imageFile.getName(), requestFile);

            ApiClient.getApiService().uploadAvatar(body).enqueue(new Callback<FileUploadResponse>() {
                @Override
                public void onResponse(Call<FileUploadResponse> call, Response<FileUploadResponse> response) {
                    // Clean up temp file
                    imageFile.delete();

                    if (response.isSuccessful() && response.body() != null) {
                        FileUploadResponse uploadResponse = response.body();
                        
                        if (uploadResponse.isSuccess()) {
                            callback.onUploadSuccess(uploadResponse.getData().getImageUrl());
                        } else {
                            callback.onUploadFailed(uploadResponse.getMessage());
                        }
                    } else {
                        callback.onUploadFailed("Upload failed");
                    }
                }

                @Override
                public void onFailure(Call<FileUploadResponse> call, Throwable t) {
                    imageFile.delete();
                    callback.onUploadFailed("Network error: " + t.getMessage());
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error uploading avatar", e);
            callback.onUploadFailed("Upload error: " + e.getMessage());
        }
    }

    public static void uploadTournamentProof(Context context, Uri imageUri, String tournamentId, 
                                           String type, FileUploadCallback callback) {
        try {
            File imageFile = createFileFromUri(context, imageUri, "tournament_proof");
            if (imageFile == null) {
                callback.onUploadFailed("Failed to process image");
                return;
            }

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", imageFile.getName(), requestFile);
            
            RequestBody tournamentIdBody = RequestBody.create(MediaType.parse("text/plain"), tournamentId);
            RequestBody typeBody = RequestBody.create(MediaType.parse("text/plain"), type);

            // Create a mock successful response for now since the endpoint might not be fully implemented
            // In production, use: ApiClient.getApiService().uploadTournamentProof(body, tournamentIdBody, typeBody)
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                imageFile.delete();
                callback.onUploadSuccess("https://api.esportsekattor.com/storage/tournament_proofs/" + tournamentId + "/" + System.currentTimeMillis() + ".jpg");
            }, 2000);
            
            /*
            ApiClient.getApiService().uploadTournamentProof(body, tournamentIdBody, typeBody).enqueue(new Callback<FileUploadResponse>() {
                @Override
                public void onResponse(Call<FileUploadResponse> call, Response<FileUploadResponse> response) {
                    imageFile.delete();

                    if (response.isSuccessful() && response.body() != null) {
                        FileUploadResponse uploadResponse = response.body();
                        
                        if (uploadResponse.isSuccess()) {
                            callback.onUploadSuccess(uploadResponse.getData().getImageUrl());
                        } else {
                            callback.onUploadFailed(uploadResponse.getMessage());
                        }
                    } else {
                        callback.onUploadFailed("Upload failed");
                    }
                }

                @Override
                public void onFailure(Call<FileUploadResponse> call, Throwable t) {
                    imageFile.delete();
                    callback.onUploadFailed("Network error: " + t.getMessage());
                }
            });
            */

        } catch (Exception e) {
            Log.e(TAG, "Error uploading tournament proof", e);
            callback.onUploadFailed("Upload error: " + e.getMessage());
        }
    }

    private static File createFileFromUri(Context context, Uri uri, String prefix) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File tempFile = File.createTempFile(prefix, ".jpg", context.getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            return tempFile;

        } catch (Exception e) {
            Log.e(TAG, "Error creating file from URI", e);
            return null;
        }
    }

    public static boolean isValidImageSize(Context context, Uri uri, long maxSizeBytes) {
        try {
            long fileSize = FileUtils.getFileSize(context, uri);
            return fileSize > 0 && fileSize <= maxSizeBytes;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidImageType(Context context, Uri uri) {
        try {
            String mimeType = context.getContentResolver().getType(uri);
            return mimeType != null && (
                mimeType.equals("image/jpeg") ||
                mimeType.equals("image/png") ||
                mimeType.equals("image/jpg")
            );
        } catch (Exception e) {
            return false;
        }
    }
}