package com.helpkonnect.mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.BuildConfig;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
public class JournalFragment extends Fragment {

    private TextView currentDateTextView;
    private View loaderView;
    private ImageView createJournalButton, forWeatherIcon;
    private RecyclerView journalCollections;
    private List<JournalListAdapter.Journal> journalList;
    private JournalListAdapter adapter;
    private FirebaseFirestore db;
    private TextView currentTempTextView;
    private TextView currentWeatherTextView;
    private final Date DateToday = new Date();
    private final SimpleDateFormat DateTodayFormat = new SimpleDateFormat("dd MMMM, yyyy", Locale.ENGLISH);
    private final String DateTodayString = DateTodayFormat.format(DateToday);

    private final String url = "https://api.openweathermap.org/data/2.5/weather";
    private String appid;
    private DecimalFormat df = new DecimalFormat("#.##");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_journal, container, false);
        loaderView = inflater.inflate(R.layout.initial_loader, container, false);
        db = FirebaseFirestore.getInstance();
        journalList = new ArrayList<>();

        //Set OpenWeather API Key
        appid = getString(R.string.OpenWeatherApiKey);
        currentTempTextView = rootView.findViewById(R.id.currentTemp);
        currentWeatherTextView = rootView.findViewById(R.id.currentWeather);
        forWeatherIcon = rootView.findViewById(R.id.weatherIcon);

        // RecyclerView setup
        journalCollections = rootView.findViewById(R.id.journalcollectionrecyclerview);
        journalCollections.setLayoutManager(new LinearLayoutManager(requireContext()));



        // Initialize adapter with click listener to navigate to JournalDetailActivity
        adapter = new JournalListAdapter(journalList, journal -> {
            //Convert Timestamp to String
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault());
            String formattedDate = sdf.format(journal.getDate().toDate());

            // Intent to pass journal data to the detail activity
            Intent intent = new Intent(requireContext(), JournalDetailActivity.class);
            intent.putExtra("journalId", journal.getDocumentId());
            intent.putExtra("journalTitle", journal.getTitle());
            intent.putExtra("journalSubtitle", journal.getSubtitle());
            intent.putExtra("journalDate", formattedDate);
            intent.putExtra("journalNotes", journal.getNotes());
            intent.putExtra("journalNotes", journal.getFullNotes());
            intent.putExtra("journalImageUrl", journal.getImageUrl());
            startActivity(intent);
        });

        journalCollections.setAdapter(adapter);

        currentDateTextView = rootView.findViewById(R.id.currentdate);
        currentDateTextView.setText(DateTodayString);

        createJournalButton = rootView.findViewById(R.id.createjournalbutton);
        createJournalButton.setOnClickListener(v -> {
            Intent toCreateJournal = new Intent(requireContext(), CreateJournalActivity.class);
            toCreateJournal.putExtra("isNewJournal", true);
            startActivity(toCreateJournal);
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Do nothing, prevent back navigation
                Toast.makeText(requireContext(), "Back button disabled", Toast.LENGTH_SHORT).show();
            }
        });

        fetchJournals(); // Load the journals

        getWeatherDetails();
        return rootView;
    }


    private void fetchJournals() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = user.getUid();

        showLoader(true);

        db.collection("journals")
                .whereEqualTo("userId", userId)
                .orderBy("dateCreated", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w("JournalFragment", "Listen failed.", e);
                        Toast.makeText(requireContext(), "Error getting documents: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        journalList.clear();
                        for (QueryDocumentSnapshot document : snapshots) {
                            String documentId = document.getId();
                            String title = document.getString("title");
                            String subtitle = document.getString("subtitle");
                            Timestamp date = document.getTimestamp("dateCreated");
                            String notes = document.getString("notes");
                            String imageUrl = document.getString("imageUrl");
                            String preview = (notes != null && notes.length() > 45) ? notes.substring(0, 45) + "..." : notes;

                            // Create Journal object with both truncated preview and full notes
                            JournalListAdapter.Journal journal = new JournalListAdapter.Journal(imageUrl, title, subtitle, date, preview, documentId);
                            journal.setFullNotes(notes);  // Add a new field to store full notes
                            journalList.add(journal);
                        }
                        showLoader(false);
                        adapter.notifyDataSetChanged();
                    }
                });
    }


    private void showLoader(boolean show) {
        TextView loadingText = loaderView.findViewById(R.id.loadingText); // Get the TextView

        if (show) {
            getActivity().addContentView(loaderView, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } else {
            if (loaderView.getParent() != null) {
                ((ViewGroup) loaderView.getParent()).removeView(loaderView);
            }
        }
    }

    //Open Weather API for temperature details
    public void getWeatherDetails() {
        String city = "Cebu City";
        String country = "Philippines";
        String tempUrl = url + "?q=" + city + "," + country + "&appid=" + appid;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, tempUrl, response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                JSONObject jsonObjectMain = jsonResponse.getJSONObject("main");
                double temp = jsonObjectMain.getDouble("temp") - 273.15; // Convert Kelvin to Celsius
                String suggestion = weatherSuggestion(temp); // Get suggestion based on temperature

                // Log temperature and suggestion
                String output = "Temp: " + df.format(temp) + " °C" + "\nSuggestion: " + suggestion;
                Log.d("WeatherDetails", output);

                // Update TextViews with temp and suggestion
                currentTempTextView.setText(df.format(temp) + " °C");
                currentWeatherTextView.setText(suggestion);

                updateWeatherIcon(temp, forWeatherIcon);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Toast.makeText(requireContext(), error.toString().trim(), Toast.LENGTH_SHORT).show());

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        requestQueue.add(stringRequest);
    }


    private String weatherSuggestion(double temp) {
        String[] hotSuggestions = {
                "Stay cool and hydrated!",
                "Maybe it's time for some ice cream!",
                "Perfect weather for a swim!",
                "Stay in the shade when possible.",
                "Don't forget your sunscreen!",
                "Light, breathable clothing is a must!",
                "How about a cold smoothie?",
                "Best to stay indoors during peak heat.",
                "Take frequent breaks if you're outside.",
                "An icy drink can help cool you down!"
        };

        String[] warmSuggestions = {
                "A nice day for a walk.",
                "Enjoy the outdoors!",
                "Great day for a picnic.",
                "Perfect for some outdoor sports!",
                "A beautiful day for a bike ride.",
                "Open the windows and let the breeze in.",
                "How about a nice outdoor brunch?",
                "Catch up on gardening!",
                "Ideal weather for a scenic hike.",
                "Wear light layers just in case!"
        };

        String[] coolSuggestions = {
                "Perfect for a cozy day indoors.",
                "How about some warm tea?",
                "Cool weather—stay comfy!",
                "Snuggle up with a good book.",
                "Maybe time for a light jacket.",
                "Great weather for a brisk walk.",
                "How about baking something warm?",
                "A light scarf might come in handy.",
                "A nice day for indoor activities.",
                "Consider layering up just in case!"
        };

        String[] chillySuggestions = {
                "Time for a sweater!",
                "A good day for a warm drink.",
                "Stay warm!",
                "Consider bundling up a bit more today.",
                "Perfect day for some hot cocoa!",
                "You might need a coat today.",
                "Stay inside and keep warm if possible.",
                "A chilly day—keep your feet warm!",
                "How about a bowl of soup?",
                "Mittens might be a good idea!"
        };

        Random random = new Random();
        String suggestion = "";

        if (temp > 30) {
            suggestion = hotSuggestions[random.nextInt(hotSuggestions.length)];
        } else if (temp > 24 && temp <= 30) {
            suggestion = warmSuggestions[random.nextInt(warmSuggestions.length)];
        } else if (temp >= 18 && temp <= 24) {
            suggestion = coolSuggestions[random.nextInt(coolSuggestions.length)];
        } else if (temp < 18) {
            suggestion = chillySuggestions[random.nextInt(chillySuggestions.length)];
        }


        return suggestion;
    }

    private void updateWeatherIcon(double temp, ImageView weatherIcon) {
        int iconResId = R.drawable.default_icon;

        if (temp > 30) {
            iconResId = R.drawable.sun_icon;
        } else if (temp > 24 && temp <= 30) {
            iconResId = R.drawable.cloudy_icon;
        } else if (temp >= 18 && temp <= 24) {
            iconResId = R.drawable.rain_icon;
        } else if (temp < 18) {
            iconResId = R.drawable.cold_icon;
        }

        // Set the image resource to the ImageView
        weatherIcon.setImageResource(iconResId);
    }



}
