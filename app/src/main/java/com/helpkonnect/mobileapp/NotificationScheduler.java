package com.helpkonnect.mobileapp;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class NotificationScheduler {

    public static void scheduleBookingReminder(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // Requires any network connectivity
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setRequiresBatteryNotLow(true) // Avoid running on low battery
                .build();

        PeriodicWorkRequest bookingReminderRequest = new PeriodicWorkRequest.Builder(
                BookingReminderWorker.class,
                15, TimeUnit.MINUTES)  // Change the interval to 15 minutes
                .build();

        // Enqueue the worker
        WorkManager.getInstance(context).enqueue(bookingReminderRequest);
    }
}
