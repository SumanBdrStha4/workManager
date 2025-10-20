package com.example.workmanager;

import android.Manifest;
import android.app.AlarmManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private TextView tvSelectedTime;
    private int selectedHour = -1;
    private int selectedMinute = -1;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. You can now schedule work or post notifications.
                    Toast.makeText(MainActivity.this, "Notifications permission granted", Toast.LENGTH_SHORT).show();
                    checkAndSchedule();
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied.
                    Toast.makeText(MainActivity.this, "Notifications will not be shown as permission was denied", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnSelectTime = findViewById(R.id.btnSelectTime);
        Button btnScheduleAlarm = findViewById(R.id.btnScheduleAlarm);
        Button btnCancelAlarm = findViewById(R.id.btnCancelAlarm);
        tvSelectedTime = findViewById(R.id.tvSelectedTime);

        loadSavedTime();

        btnSelectTime.setOnClickListener(v -> showTimePickerDialog());

        btnScheduleAlarm.setOnClickListener(v -> handleScheduleClick());

        btnCancelAlarm.setOnClickListener(v -> cancelCurrentAlarm());
//        askNotificationPermission();
    }

    private void handleScheduleClick() {
        if (selectedHour == -1 || selectedMinute == -1) {
            Toast.makeText(this, "Please select a time first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, so request it.
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            Toast.makeText(this, "Please grant notification permission to schedule alarms.", Toast.LENGTH_LONG).show();
            return;
        }
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(this, "Please grant permission to schedule exact alarms.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            startActivity(intent);
            return;
        }
        scheduleNewAlarm();
    }

    private void scheduleNewAlarm() {
        AlarmPreferenceManager.saveAlarmTime(this, selectedHour, selectedMinute, 1);
        AlarmHelper.scheduleAlarm(this);
        Toast.makeText(this, "Alarm scheduled for " + formatTime(selectedHour, selectedMinute), Toast.LENGTH_SHORT).show();
    }

    private void cancelCurrentAlarm() {
        AlarmHelper.cancelAlarm(this);
        Toast.makeText(this, "Alarm canceled", Toast.LENGTH_SHORT).show();
        tvSelectedTime.setText("No alarm set");
    }

    private void showTimePickerDialog() {
        int initialHour = selectedHour != -1 ? selectedHour : AlarmPreferenceManager.getAlarmHour(this);
        int initialMinute = selectedMinute != -1 ? selectedMinute : AlarmPreferenceManager.getAlarmMinute(this);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    // This is called when the user clicks "OK"
                    selectedHour = hourOfDay;
                    selectedMinute = minute;
                    tvSelectedTime.setText("New Time: " + formatTime(selectedHour, selectedMinute));
                },
                initialHour,
                initialMinute,
                false // Use true for 24-hour view, false for AM/PM
        );
        timePickerDialog.show();
    }

    private void loadSavedTime() {
        if (AlarmPreferenceManager.isAlarmEnabled(this)) {
            selectedHour = AlarmPreferenceManager.getAlarmHour(this);
            selectedMinute = AlarmPreferenceManager.getAlarmMinute(this);
            tvSelectedTime.setText("Current Alarm Time: " + formatTime(selectedHour, selectedMinute));
        } else {
            tvSelectedTime.setText("No alarm set");
        }
    }

    private String formatTime(int hour, int minute) {
        // Formats time to a readable format like "03:30 PM"
        int displayHour = hour % 12;
        if (displayHour == 0) displayHour = 12; // Handle 12 AM/PM
        String amPm = hour < 12 ? "AM" : "PM";
        return String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, minute, amPm);
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                checkAndSchedule();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            checkAndSchedule();
        }
    }

    private void checkAndSchedule() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "Please grant permission to schedule exact alarms", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.fromParts("package", getPackageName(), null));
                startActivity(intent);
            } else {
                scheduleApiFetch();
            }
        } else {
            scheduleApiFetch();
        }
    }

    private void scheduleApiFetch() {
        WorkScheduler.scheduleFetchAt(getApplicationContext(), 15, 59);
//        AlarmHelper.scheduleAlarm(getApplicationContext(), 15, 59, 1);
        Toast.makeText(this, "API fetch scheduled to run every 15 minutes", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //stop or kill all
//        WorkManager.getInstance(this).cancelAllWork();
    }
}