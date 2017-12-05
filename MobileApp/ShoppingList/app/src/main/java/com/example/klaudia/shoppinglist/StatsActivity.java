package com.example.klaudia.shoppinglist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;

public class StatsActivity extends AppCompatActivity {

    PieChart contributionChart;
    BarChart productPopularityChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        contributionChart = (PieChart)findViewById(R.id.contributionChart);
        productPopularityChart = (BarChart)findViewById(R.id.popularityChart);

        Intent intent = getIntent();
        if(intent != null) {
            String token = intent.getStringExtra("token");
            String lid = intent.getStringExtra("lid");

            // retrieve stats data from server
            // TODO
            generateStats(""); // FIXME
        }
    }

    private void generateStats(String rawJson) {
        // https://www.numetriclabz.com/android-pie-chart-using-mpandroidchart-library-tutorial/
    }
}
