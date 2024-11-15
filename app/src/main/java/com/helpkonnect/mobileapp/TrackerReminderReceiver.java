package com.helpkonnect.mobileapp;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.onesignal.OneSignal;
import java.util.Calendar;
import java.util.Date;

public class TrackerReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("TrackerReminderReceiver", "onReceive triggered.");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.d("TrackerReminderReceiver", "User is not logged in.");
            return;
        }

        String userId = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get today's date as a Timestamp (start of today and end of today)
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0); // Set to midnight
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startOfDay = calendar.getTime();

        calendar.set(Calendar.HOUR_OF_DAY, 23); // Set to 11:59 PM
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date endOfDay = calendar.getTime();

        // Convert to Firebase Timestamps
        Timestamp startTimestamp = new Timestamp(startOfDay);
        Timestamp endTimestamp = new Timestamp(endOfDay);

        Log.d("TrackerReminderReceiver", "Checking journal entries for userId: " + userId + " from " + startTimestamp + " to " + endTimestamp);

        db.collection("emotion_analysis")
                .whereEqualTo("journalUserId", userId)
                .whereGreaterThanOrEqualTo("dateCreated", startTimestamp)
                .whereLessThanOrEqualTo("dateCreated", endTimestamp)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && querySnapshot.isEmpty()) {
                            // No journal found for today, send a reminder notification
                            Log.d("TrackerReminderReceiver", "No journal entry found for today.");
                            sendReminderNotification(context);
                            // Reschedule the alarm to repeat every 3 minutes
                        } else {
                            // Found a journal entry for today
                            Log.d("TrackerReminderReceiver", "Found analyzed journal entry for today.");
                        }
                    } else {
                        Log.e("TrackerReminderReceiver", "Error getting analyzed journal entries: " + task.getException());
                    }
                });
    }

      /*private static final String TAG = "DailyNotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Daily notification triggered.");

        showNotification(context);
    }

    private void showNotification(Context context) {
        Log.d(TAG, "Creating notification...");

        // Create the notification
        String channelId = "journal_reminder_channel";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) {
            Log.e(TAG, "NotificationManager is null, cannot show notification.");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Journal Reminder",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
            Log.d(TAG, "Notification channel created.");
        }

        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setContentTitle("Journal Reminder")
                .setContentText("It's time to write in your journal!")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();

        notificationManager.notify(1, notification);  // Show the notification

        Log.d(TAG, "Notification displayed.");
    }*/

    private void sendReminderNotification(Context context) {
        // Build and show the notification
        Log.d("TrackerReminderReceiver", "Sending reminder notification.");
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "tracker_reminder_channel";

        // Create the notification channel if required (for Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Emotion Analysis Reminders", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setContentTitle("Reminder")
                .setContentText("Analyze your emotion today. To keep track of your emotion.")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();

        notificationManager.notify(1, notification);
        Log.d("TrackerReminderReceiver", "Reminder notification sent.");
    }
}

