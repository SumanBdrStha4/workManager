package com.example.workmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Boot completed, scheduling work.");
//            WorkScheduler.scheduleFetchAt(context, 15, 30);
            AlarmHelper.scheduleAlarm(context, 15, 59);
        }
    }
}
