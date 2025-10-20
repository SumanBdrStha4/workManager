package com.example.workmanager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

public class AlarmHelper {
    private static final int ALARM_REQUEST_CODE = 101;

//    public static void scheduleAlarm(Context context, int hour, int minute, int daysToAdd) {
    public static void scheduleAlarm(Context context) {
        int hour = AlarmPreferenceManager.getAlarmHour(context);
        int minute = AlarmPreferenceManager.getAlarmMinute(context);
        int daysToAdd = 1;
        int daysToSkip = AlarmPreferenceManager.getDaysToSkip(context);
        int daysToAddForNextAlarm = daysToSkip + 1;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, FetchAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If the target time has already passed for today, schedule it for tomorrow.
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, daysToAdd);
        }

        // Add the specified number of days to skip.
        if (daysToAddForNextAlarm > 0) {
            calendar.add(Calendar.DATE, daysToAddForNextAlarm);
        }

//        long targetTime = target.getTimeInMillis();

//        AlarmManager alarmManager = (AlarmManager)
//                context.getSystemService(Context.ALARM_SERVICE);
//
//        Intent intent = new Intent(context, FetchAlarmReceiver.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(
//                context,
//                0,
//                intent,
//                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
//        );

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );
    }

    public static void cancelAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, FetchAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);

        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            AlarmPreferenceManager.setAlarmEnabled(context, false);
            Log.d("AlarmHelper", "Alarm canceled.");
        }
    }
}
