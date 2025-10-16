package com.example.workmanager;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class WorkScheduler {
    private static final String WORK_TAG = "apiVolleyWork";
    public static void scheduleFetchAt(Context context, int hour, int minute) {
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();

        target.set(Calendar.HOUR_OF_DAY, hour);
        target.set(Calendar.MINUTE, minute);
        target.set(Calendar.SECOND, 0);
//
        if (target.before(now)) {
            target.add(Calendar.DATE, 1);
        }
//
        long delay = target.getTimeInMillis() - now.getTimeInMillis();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest workRequest =
                new OneTimeWorkRequest.Builder(ApiVolleyWorker.class)
                        .setConstraints(constraints)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .build();

        PeriodicWorkRequest apiWorkRequest =
                new PeriodicWorkRequest.Builder(ApiVolleyWorker.class, 15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .addTag(WORK_TAG)
                        .build();

        WorkManager.getInstance(context)
                .beginUniqueWork(WORK_TAG, ExistingWorkPolicy.REPLACE, workRequest)
//                .then(apiWorkRequest)
                .enqueue();
    }
}
