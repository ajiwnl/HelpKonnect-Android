package com.helpkonnect.mobileapp;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class TrackerFragment extends Fragment {

    private TextView noQuestionsText;
    private ProgressBar chartLoader, emotionListLoader;
    private PieChart emotionChart;
    private List<PieEntry> dataList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_tracker, container, false);

        chartLoader = rootView.findViewById(R.id.pieChartLoader);
        emotionListLoader = rootView.findViewById(R.id.emotionListLoader);
        noQuestionsText = rootView.findViewById(R.id.NoQuestionsAnswered);
        emotionChart = rootView.findViewById(R.id.EmotionChart);
        dataList = new ArrayList<>();


        chartLoader.setVisibility(View.VISIBLE);

        new Handler().postDelayed(() -> {

            populateChart();
            chartLoader.setVisibility(View.GONE);
            loadChart();

        }, 1000);

        new Handler().postDelayed(() -> {
            List<String> moodData = loadMoodData();

            if (true) {

                noQuestionsText.setVisibility(View.VISIBLE);
            } else {

                noQuestionsText.setVisibility(View.GONE);


            }

            emotionListLoader.setVisibility(View.GONE);
        }, 2000);

        return rootView;
    }

    private List<String> loadMoodData() {

        List<String> moodData = new ArrayList<>();

        moodData.add("Happy");
        moodData.add("Relaxed");
        moodData.add("Neutral");
        moodData.add("Stressed");
        moodData.add("Sad");

        return moodData;
    }

    private void loadChart() {
        PieDataSet emotionDataSet = new PieDataSet(dataList, "Emotions");
        PieData emotionData = new PieData(emotionDataSet);
        emotionDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        emotionDataSet.setValueTextColor(R.color.black);
        emotionData.setValueTextSize(14f);
        emotionChart.setData(emotionData);;
        emotionChart.invalidate();
    }

    private void populateChart() {
        //Load Data
        dataList.add(new PieEntry(25, "Happy"));
        dataList.add(new PieEntry(30, "Relaxed"));
        dataList.add(new PieEntry(25, "Neutral"));
        dataList.add(new PieEntry(25, "Stressed"));
        dataList.add(new PieEntry(25, "Sad"));
    }
}
