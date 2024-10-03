package com.helpkonnect.mobileapp;

import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;

import java.util.Locale;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Set default locale if needed
        setDefaultLocale("en"); // Change "en" to your preferred language code

        // Initialize Firebase and App Check
        FirebaseApp.initializeApp(this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());
    }

    private void setDefaultLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        Log.d("MyApp", "Locale set to: " + lang);
    }
}
