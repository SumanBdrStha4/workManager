package com.example.workmanager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ApiVolleyWorker extends Worker{
    private static final String TAG = "ApiVolleyWorker";
    private static final String CHANNEL_ID = "API_WORKER_CHANNEL";
    public ApiVolleyWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        return makeApiRequest(getApplicationContext());
    }

    private Result makeApiRequest(Context context) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "https://dummy.restapiexample.com/api/v1/employees";
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
//                response -> {
//                    Log.d(TAG, "Response: " + response.substring(0, 100));
//                    showNotification(context, "API Request Successful", "Data loaded successfully.");
//                },
//                this::logVollyError
//        );
//        queue.add(stringRequest);
        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, future, future);
        queue.add(stringRequest);
        try {
            String response = future.get(30, TimeUnit.MINUTES);

            Log.d(TAG, "Response: " + response.substring(0, 100));
            showNotification(context, "API Request Successful", "Data loaded successfully.");
            return Result.success();

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            // If an error occurs, log it and return failure.
            Log.e(TAG, "API request failed", e);

            // You can inspect the cause to see if it's a VolleyError.
            if (e.getCause() instanceof VolleyError) {
                Log.w(TAG, "API Rate Limit Exceeded.");
            }

            return Result.failure();
        }
    }

    private void showNotification(Context context, String title, String message) {
        createNotificationChannel(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your own icon
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        // An ID is required to post the notification. This can be used to update the notification later.
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify(1, builder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "Notification permission not granted. Please grant POST_NOTIFICATIONS permission.", e);
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "API Worker Channel";
            String description = "Notifications for API background work";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void logVollyError(VolleyError error) {
        String message = "That didn't work!";
        NetworkResponse response = error.networkResponse;

        if (response != null) {
            message = "Error: Status Code " + response.statusCode;
            Log.e(TAG, message, error);
            if (response.statusCode == 429) {
                Log.w(TAG, "API Rate Limit Exceeded. The server is blocking requests.");
            }
        } else {
            Log.e(TAG, message, error);
        }
    }
}
