package com.helpkonnect.mobileapp;

import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

import androidx.annotation.OptIn;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.logger.ChatLogLevel;
import io.getstream.chat.android.core.internal.InternalStreamChatApi;
import io.getstream.chat.android.models.UploadAttachmentsNetworkType;
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory;
import io.getstream.chat.android.state.plugin.config.StatePluginConfig;
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory;
import com.onesignal.OneSignal;
public class MyApp extends Application {
    private static final String TAG = "MyApp";
    private static final String STREAM_KEY_URL = "https://helpkonnect.vercel.app/api/streamKey";
    private static final String ONE_KEY_URL = "https://helpkonnect.vercel.app/api/onesignalKey";
    private String oneSignalKey = null;
    private String oneSignalID = null;


    @Override
    public void onCreate() {
        super.onCreate();

        // Set default locale
        setDefaultLocale("en");

        // Initialize Firebase and App Check
        FirebaseApp.initializeApp(this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance());

        // Schedule notifications
        scheduleDailyNotifications();

        // Fetch API keys
        fetchOneSignalKeys(); // Ensure to fetch OneSignal keys first
        fetchApiKeyAndInitializeChatClient(); // Fetch Stream API keys
    }


    private void scheduleDailyNotifications() {
        PeriodicWorkRequest notificationWorkRequest = new PeriodicWorkRequest.Builder(
                DailyNotificationWorker.class, 15, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "DailyNotificationWork",
                ExistingPeriodicWorkPolicy.REPLACE,
                notificationWorkRequest
        );

    }


    private void setDefaultLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        Log.d(TAG, "Locale set to: " + lang);
    }

    private void fetchApiKeyAndInitializeChatClient() {
        Log.d(TAG, "Fetching API key");
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        boolean backgroundSyncEnabled = true;
        boolean userPresence = true;

        StreamStatePluginFactory statePluginFactory = new StreamStatePluginFactory(
                new StatePluginConfig(
                        backgroundSyncEnabled,
                        userPresence
                ),
                this
        );

        StreamOfflinePluginFactory offlinePluginFactory = new StreamOfflinePluginFactory(this);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                STREAM_KEY_URL,
                null,
                new Response.Listener<JSONObject>() {
                    @OptIn(markerClass = InternalStreamChatApi.class)
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String apiKey = response.getString("streamKey");

                            // Initialize ChatClient with the fetched API key
                            new ChatClient.Builder(apiKey, MyApp.this)
                                    .logLevel(ChatLogLevel.ALL)
                                    .uploadAttachmentsNetworkType(UploadAttachmentsNetworkType.NOT_ROAMING)
                                    .withPlugins(statePluginFactory, offlinePluginFactory)
                                    .build();
                            Log.d(TAG, "ChatClient initialized with API key: " + apiKey);
                        } catch (JSONException e) {
                            Log.e(TAG, "Failed to parse API key", e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Failed to fetch API key", error);
                    }
                }
        );

        requestQueue.add(jsonObjectRequest);
    }

    private void fetchOneSignalKeys() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, ONE_KEY_URL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        oneSignalID = jsonResponse.getString("onesignalID");

                        // Cache the key locally
                        getSharedPreferences("AppPrefs", MODE_PRIVATE)
                                .edit()
                                .putString("oneSignalID", oneSignalID)
                                .apply();

                        // Initialize OneSignal
                        OneSignal.initWithContext(this);
                        OneSignal.setAppId(oneSignalID);

                        Log.d(TAG, "Fetched and initialized OneSignal with ID: " + oneSignalID);
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error: " + e.getMessage());
                    }
                },
                error -> Log.e(TAG, "Error fetching API keys: " + error.toString())
        );

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

        // Use cached value if network fails
        String cachedKey = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                .getString("oneSignalID", null);
        if (cachedKey != null) {
            OneSignal.initWithContext(this);
            OneSignal.setAppId(cachedKey);
            Log.d(TAG, "Initialized OneSignal with cached ID: " + cachedKey);
        }
    }


}
