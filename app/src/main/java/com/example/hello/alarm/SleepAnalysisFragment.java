package com.example.hello.alarm;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SleepAnalysisFragment extends Fragment {
    LineData lineData;
    FirebaseUser currentUser;
    DatabaseReference myRef;
    List<BarEntry> entries;

    Date currentTime;
    String firstDayOfWeek;
    String currentMonth;
    String currentYear;

    Button showWeekChartRadioButton;
    Button showMonthChartRadioButton;
    Button showAllTimeChartRadioButton;

    View rootView;
    RelativeLayout parentLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        Calendar calendar = Calendar.getInstance();
        currentTime = calendar.getTime();
        //get current month
        currentMonth = new SimpleDateFormat("M", Locale.US).format(currentTime);
        currentYear = new SimpleDateFormat("YYYY", Locale.US).format(currentTime);

        //get first day of week
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        firstDayOfWeek = new SimpleDateFormat("dd", Locale.US).format(calendar.getTime());


        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        myRef = FirebaseDatabase.getInstance().getReference().child(currentUser.getUid()).child("Sleep Data");

        Utils.init(getContext());
        entries = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.sleep_analysis_fragment_layout, container, false);
        // get parent layout in xml
        parentLayout = rootView.findViewById(R.id.root);

        //Show week chart as default
        showWeekChart(rootView);

        showWeekChartRadioButton = rootView.findViewById(R.id.week_button);
        showWeekChartRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Clear all current chart before creating a new one
                parentLayout.removeAllViews();
                showWeekChart(rootView);
            }
        });
        showMonthChartRadioButton = rootView.findViewById(R.id.month_button);
        showAllTimeChartRadioButton = rootView.findViewById(R.id.all_time_button);

        // l.setExtra(ColorTemplate.VORDIPLOM_COLORS, new String[] { "abc",
        // "def", "ghj", "ikl", "mno" });
        // l.setCustom(ColorTemplate.VORDIPLOM_COLORS, new String[] { "abc",
        // "def", "ghj", "ikl", "mno" });
        //// add entries and styles to dataset
//                LineDataSet dataSet = new LineDataSet(entries, "Time");
//
//                dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
//                dataSet.setCubicIntensity(0.2f);
//                dataSet.setDrawFilled(true);
//                dataSet.setDrawCircles(false);
//                dataSet.setLineWidth(1.8f);
//                dataSet.setCircleRadius(4f);
//                dataSet.setCircleColor(android.R.color.white);
//                dataSet.setHighLightColor(Color.rgb(244, 117, 117));
//                dataSet.setColor(Color.WHITE);
//                dataSet.setFillColor(Color.WHITE);
//                dataSet.setFillAlpha(100);
//                dataSet.setDrawHorizontalHighlightIndicator(false);
//
//                lineData = new LineData(dataSet);
//
//                LineChart chart = rootView.findViewById(R.id.sleep_chart);
//                chart.setData(lineData);
//                chart.getXAxis().setDrawGridLines(false);
//                chart.invalidate(); // refresh
//                Log.e("hey", "onDataChange: "+"what happens second" );
//            }
//
//
//        });

        return rootView;
    }


    public void showWeekChart(final View rootView) {
        TextView info = rootView.findViewById(R.id.information);
        info.setText("Sleep Analysis (Week of " + firstDayOfWeek + "/" + currentMonth + "/" + currentYear + " )");
        myRef.child(currentYear).child(currentMonth).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int date;
                float hour;
                float minute;
                float hourAndMinuteValue;
                String dayOfWeek = null;
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    //Get only the dates within the current week
                    date = Integer.valueOf(data.getKey()) - Integer.valueOf(firstDayOfWeek);
                    if (date < 7 && date >= 0){
                        minute = data.getValue(long.class) / (1000 * 60) % 60;
                        hour = data.getValue(long.class) / (1000 * 60 * 60) % 24;
                        hourAndMinuteValue = hour + minute / 60 * 100;
                        Log.e("fuck", "onDataChange: " + hour + "?" + minute );
                        //Add data point
                        entries.add(new BarEntry(date, hourAndMinuteValue));
                    }
                }

                // programmatically create a BarChart and set size
                BarChart chart = new BarChart(getContext());
                chart.setLayoutParams(new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT
                ));


                parentLayout.addView(chart); // add the programmatically created chart

                chart.setDrawBarShadow(false);
                chart.setDrawValueAboveBar(true);
                chart.getDescription().setEnabled(false);
                // if more than 60 entries are displayed in the chart, no values will be
                // drawn
                chart.setMaxVisibleValueCount(8);

                // scaling can now only be done on x- and y-axis separately
                chart.setPinchZoom(false);

                chart.setDrawGridBackground(false);
                // mChart.setDrawYLabels(false);

                IAxisValueFormatter xAxisFormatter = new DayAxisValueFormatter(chart);

                XAxis xAxis = chart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setDrawGridLines(false);
                xAxis.setGranularity(1f); // only intervals of 1 day
                xAxis.setLabelCount(7);
                xAxis.setValueFormatter(xAxisFormatter);

                IAxisValueFormatter custom = new YAxisValueFormatter();

                YAxis leftAxis = chart.getAxisLeft();
                leftAxis.setLabelCount(8, false);
                leftAxis.setValueFormatter(custom);
                leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
                leftAxis.setSpaceTop(15f);
                leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

                YAxis rightAxis = chart.getAxisRight();
                rightAxis.setDrawGridLines(false);
                rightAxis.setLabelCount(8, false);
                rightAxis.setValueFormatter(custom);
                rightAxis.setSpaceTop(15f);
                rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

                Legend l = chart.getLegend();
                l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
                l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
                l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
                l.setDrawInside(false);
                l.setForm(Legend.LegendForm.SQUARE);
                l.setFormSize(9f);
                l.setTextSize(11f);
                l.setXEntrySpace(4f);

                BarDataSet dataSet = new BarDataSet(entries, "Sleeping Time");
                BarData data = new BarData(dataSet);
                chart.setData(data);
                chart.invalidate();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
//
//    public void showMonthChart(final View rootView) {
//        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                String date;
//                int hour;
//                int minute;
//                int hourAndMinuteValue;
//                DateFormat dateString;
//                Date fullDate;
//                String dayOfWeek = null;
//                for (DataSnapshot data : dataSnapshot.getChildren()) {
//                    date = data.getKey();
//                    try {
//                        dateString =new SimpleDateFormat("EEE");
//                        fullDate = new SimpleDateFormat("dd-M-yyyy").parse(date);
//                        dayOfWeek = dateString.format(fullDate);
//                    } catch (ParseException e) {
//                        e.printStackTrace();
//                    }
//                    minute = Math.round(data.getValue(long.class) / (1000 * 60) % 60);
//                    hour = Math.round(data.getValue(long.class) / (1000 * 60 * 60) % 24);
//                    hourAndMinuteValue = Math.round(hour + minute / 60 * 100);
//
//                    entries.add(new BarEntry(dayOfWeek[date], hourAndMinuteValue));
//                    Log.e("Hour and Minute", "onDataChange: " + dayOfWeek + dayOfWeekToNumber.get(dayOfWeek)+ hourAndMinuteValue);
//                }
//
//                // programmatically create a LineChart and set size
//                BarChart chart = new BarChart(getContext());
//                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
//                        RelativeLayout.LayoutParams.MATCH_PARENT,
//                        1000
//                );
//
//                params.setMargins(0, 300, 0, 0);
//
//                chart.setLayoutParams(params);
//
//                // get parent layout in xml
//                RelativeLayout rl = rootView.findViewById(R.id.root);
//                rl.addView(chart); // add the programmatically created chart
//
//                chart.setDrawBarShadow(false);
//                chart.setDrawValueAboveBar(true);
//
//                chart.getDescription().setEnabled(false);
//
//                // if more than 60 entries are displayed in the chart, no values will be
//                // drawn
//                chart.setMaxVisibleValueCount(8);
//
//                // scaling can now only be done on x- and y-axis separately
//                chart.setPinchZoom(false);
//
//                chart.setDrawGridBackground(false);
//                // mChart.setDrawYLabels(false);
//
//                IAxisValueFormatter xAxisFormatter = new DayAxisValueFormatter(chart);
//
//                XAxis xAxis = chart.getXAxis();
//                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//                xAxis.setDrawGridLines(false);
//                xAxis.setGranularity(1f); // only intervals of 1 day
//                xAxis.setLabelCount(7);
//                xAxis.setValueFormatter(xAxisFormatter);
//
//                IAxisValueFormatter custom = new YAxisValueFormatter();
//
//                YAxis leftAxis = chart.getAxisLeft();
//                leftAxis.setLabelCount(8, false);
//                leftAxis.setValueFormatter(custom);
//                leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
//                leftAxis.setSpaceTop(15f);
//                leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
//
//                YAxis rightAxis = chart.getAxisRight();
//                rightAxis.setDrawGridLines(false);
//                rightAxis.setLabelCount(8, false);
//                rightAxis.setValueFormatter(custom);
//                rightAxis.setSpaceTop(15f);
//                rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
//
//                Legend l = chart.getLegend();
//                l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
//                l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
//                l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
//                l.setDrawInside(false);
//                l.setForm(Legend.LegendForm.SQUARE);
//                l.setFormSize(9f);
//                l.setTextSize(11f);
//                l.setXEntrySpace(4f);
//
//                BarDataSet dataSet = new BarDataSet(entries, "Sleeping Time");
//                BarData data = new BarData(dataSet);
//                chart.setData(data);
//                chart.invalidate();
//            }
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//            }
//        });
//    }
}