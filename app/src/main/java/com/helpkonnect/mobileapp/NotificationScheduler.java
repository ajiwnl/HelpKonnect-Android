package com.helpkonnect.mobileapp;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class NotificationScheduler {

    public static void scheduleBookingReminder(Context context) {
        // Define the constraints for the worker
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // Requires any network connectivity
                .setRequiresCharging(false) // Can run even if not charging
                .setRequiresDeviceIdle(false) // Can run even if device is not idle
                .setRequiresBatteryNotLow(true) // Avoid running on low battery
                .build();

        // Create a periodic work request with a 15-minute interval
        PeriodicWorkRequest bookingReminderRequest = new PeriodicWorkRequest.Builder(
                BookingReminderWorker.class,
                15, TimeUnit.MINUTES) // Interval of 15 minutes
                .setConstraints(constraints) // Apply constraints
                .build();

        // Enqueue the worker uniquely to avoid duplicates
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "BookingReminderWorker", // Unique name for this worker
                ExistingPeriodicWorkPolicy.KEEP, // Keep the existing work
                bookingReminderRequest
        );
    }
}