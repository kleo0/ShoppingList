package com.example.klaudia.shoppinglist;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StatsActivity extends AppCompatActivity {

    PieChart contributionChart;
    HorizontalBarChart productPopularityChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        contributionChart = (PieChart)findViewById(R.id.contributionChart);
        productPopularityChart = (HorizontalBarChart)findViewById(R.id.popularityChart);

        Intent intent = getIntent();
        if(intent != null) {
            String token = intent.getStringExtra("token");
            String lid = intent.getStringExtra("lid");

            // retrieve stats data from server
            getStatsFromServer(token, lid);
        }
    }

    private void getStatsFromServer(String token, String lid) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("list_id", lid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Ion.with(getApplicationContext())
                .load("http://skyapplab.duckdns.org:7777/list.php")
                .setBodyParameter("token", token)
                .setBodyParameter("action", "stat_list_user")
                .setBodyParameter("data", obj.toString())
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (result != null) {
                            if (result.get("ERR").toString().equals("0")) {
                                JsonObject jsonObject = result.get("JSON_DATA").getAsJsonObject();
                                generateContributionStats(jsonObject);
                            } else {
                                Toast.makeText(getApplicationContext(), "Send data error!" + result.get("ERR").toString(), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Check internet connection!", Toast.LENGTH_LONG).show();
                        }
                    }
                });

        Ion.with(getApplicationContext())
                .load("http://skyapplab.duckdns.org:7777/list.php")
                .setBodyParameter("token", token)
                .setBodyParameter("action", "stat_list_product")
                .setBodyParameter("data", obj.toString())
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (result != null) {
                            if (result.get("ERR").toString().equals("0")) {
                                JsonObject jsonObject = result.get("JSON_DATA").getAsJsonObject();
                                generateDemandsStats(jsonObject);
                            } else {
                                Toast.makeText(getApplicationContext(), "Send data error!" + result.get("ERR").toString(), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Check internet connection!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    void generateContributionStats(JsonObject jsonObject) {
        int total = jsonObject.get("total").getAsInt();
        if(total == 0) {
            return;
        }

        JsonArray jsonArray = jsonObject.getAsJsonArray("contributors");
        ArrayList<PieEntry> entries = new ArrayList<>();

        for (int i=0; i<jsonArray.size(); ++i) {
            JsonObject element = jsonArray.get(i).getAsJsonObject();
            int q = element.get("count").getAsInt();
            String name = element.get("user").getAsString();

            entries.add(new PieEntry(q, name));
        }

        PieDataSet pieDataSet = new PieDataSet(entries, "% of user contribution");

        pieDataSet.setValueTextColor(Color.WHITE);
        pieDataSet.setValueTextSize(12);
        PieData pieData = new PieData(pieDataSet);
        pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        pieData.setValueTextSize(15);

        Description description = new Description();
        description.setText("");

        contributionChart.setDescription(description);
        contributionChart.setCenterText("Contributors");
        contributionChart.setCenterTextSize(14);
        contributionChart.animateY(2000);


        contributionChart.setData(pieData);
        contributionChart.setUsePercentValues(true);
        contributionChart.getLegend().setEnabled(false);
    }

    void generateDemandsStats(JsonObject jsonObject) {
        int total = jsonObject.get("total").getAsInt();
        if(total == 0) {
            return;
        }

        JsonArray jsonArray = jsonObject.getAsJsonArray("contributors");
        ArrayList<BarEntry> entries = new ArrayList<>();

        String legend = "";

        for (int i=0; i<jsonArray.size(); ++i) {
            JsonObject element = jsonArray.get(i).getAsJsonObject();
            int q = element.get("count").getAsInt();
            String name = element.get("product").getAsString();

            entries.add(new BarEntry(i, q, name));
            if (i != 0) {
                legend += ", ";
            }
            legend += name;
        }

        BarDataSet barDataSet = new BarDataSet(entries, "Products: " + legend);
        barDataSet.setValueTextColor(Color.WHITE);
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        barDataSet.setValueTextSize(12);

        BarData barData = new BarData(barDataSet);
        barData.setValueTextSize(15);
        barData.setValueTextColor(Color.WHITE);

        Description description = new Description();
        description.setText("");

        productPopularityChart.setDescription(description);
        productPopularityChart.animateY(2000);

        productPopularityChart.setData(barData);
        productPopularityChart.getLegend().setEnabled(true);
        productPopularityChart.getLegend().setTextColor(Color.WHITE);
        productPopularityChart.getLegend().setTextSize(12);
    }
}
