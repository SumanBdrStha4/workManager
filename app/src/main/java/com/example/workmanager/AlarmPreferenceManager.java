package com.example.workmanager;

import android.content.Context;
import android.content.SharedPreferences;

public class AlarmPreferenceManager {
    private static final String PREF_NAME = "AlarmSettings";
    private static final String KEY_ALARM_HOUR = "alarm_hour";
    private static final String KEY_ALARM_MINUTE = "alarm_minute";
    private static final String KEY_DAYS_TO_SKIP = "days_to_skip";
    private static final String KEY_ALARM_ENABLED = "alarm_enabled";

    // Default values
    private static final int DEFAULT_HOUR = 15;
    private static final int DEFAULT_MINUTE = 59;
    private static final int DEFAULT_DAYS_TO_ADD = 0;

//    public static void saveAlarmTime(Context context, int hour, int minute) {
//        saveAlarmTime(context, hour, minute, DEFAULT_DAYS_TO_ADD);
//    }

    public static void saveAlarmTime(Context context, int hour, int minute, int daysToSkip) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_ALARM_HOUR, hour);
        editor.putInt(KEY_ALARM_MINUTE, minute);
        editor.putInt(KEY_DAYS_TO_SKIP, daysToSkip);
        editor.putBoolean(KEY_ALARM_ENABLED, true);
        editor.apply();
    }

    public static int getAlarmHour(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_ALARM_HOUR, DEFAULT_HOUR);
    }

    public static int getAlarmMinute(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_ALARM_MINUTE, DEFAULT_MINUTE);
    }

    public static int getDaysToSkip(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        return prefs.getInt(KEY_DAYS_TO_SKIP, DEFAULT_DAYS_TO_ADD);
    }

    public static boolean isAlarmEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_ALARM_ENABLED, false);
    }

    public static void setAlarmEnabled(Context context, boolean isEnabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_ALARM_ENABLED, isEnabled);
        editor.apply();
    }
}
