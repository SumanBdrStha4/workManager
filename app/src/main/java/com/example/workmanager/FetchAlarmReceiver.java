package com.example.workmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;

public class FetchAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        OneTimeWorkRequest workRequest =
                new OneTimeWorkRequest.Builder(ApiVolleyWorker.class)
                        .build();
        WorkManager.getInstance(context).enqueue(workRequest);

        // Schedule next alarm in 15 minutes
        Calendar next = Calendar.getInstance();
        next.add(Calendar.MINUTE, 15);
//        AlarmHelper.scheduleAlarm(context, next.get(Calendar.HOUR_OF_DAY), next.get(Calendar.MINUTE), 1);
        AlarmHelper.scheduleAlarm(context);
    }
}
