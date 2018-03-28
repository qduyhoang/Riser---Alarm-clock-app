package com.example.hello.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity {

    AlarmManager alarmManager;
    TimePicker timePicker;
    TextView updateText;
    Context context;
    Button setOnButton;
    PendingIntent pending_intent;
    ArrayList alarm_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = this;
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        timePicker = findViewById(R.id.timePicker);
        //Create array of all alarm to show on the list view
        alarm_data = new ArrayList<>();
        final alarm_adapter adapter = new alarm_adapter(this, R.layout.listitem, alarm_data);
        final ListView alarm_list = findViewById(R.id.listView);
        alarm_list.setAdapter(adapter);
        final Calendar calendar = Calendar.getInstance();
        final Intent alarm_intent = new Intent(this.context, alarm_receiver.class);


        setOnButton = findViewById(R.id.setOn);
        setOnButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                calendar.set(Calendar.MINUTE, timePicker.getMinute());

                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();


                Intent intent_main_activity = new Intent(context, MainActivity.class);
                PendingIntent pending_main_activity = PendingIntent.getActivity(context, 0, intent_main_activity, 0);
                alarm_intent.putExtra("extra", "yes");
                pending_intent = PendingIntent.getBroadcast(MainActivity.this, 0, alarm_intent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager.AlarmClockInfo alarm_info = new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pending_main_activity);
                alarmManager.setAlarmClock(alarm_info, pending_intent);

                //Add alarm to list view
                alarm newAlarm = new alarm(hour, minute);
                adapter.add(newAlarm);



            }
        });
    };

    public class alarm{
        String time_string;
        public alarm(){
            super();
        }
        public alarm(int hour, int minute){
            super();
            String hour_string, minute_string;
            hour_string = String.valueOf(hour);
            minute_string = minute < 10 ? "0" + String.valueOf(minute) : String.valueOf(minute);
            this.time_string = hour_string + " :" + minute_string;
            Log.e("alarm", "alarm: "+this.time_string);
        }

    }

    public class alarm_adapter extends ArrayAdapter<alarm> {
        alarm_adapter(Context context, int listViewResource, ArrayList<alarm> alarms) {
            super(context, R.layout.listitem, alarms);
        }


        @NonNull
        @Override
        public View getView(final int position, View convertView, ViewGroup parent){
            final alarm alarm = getItem(position);
            if (convertView == null){
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem, parent, false);
            }
            TextView tvTime = convertView.findViewById(R.id.tvTime);
            Switch switchButton = convertView.findViewById(R.id.switchButton);
            Button removeButton = convertView.findViewById(R.id.remove_button);
            switchButton.setChecked(true);
            removeButton.setOnClickListener(new View.OnClickListener(){
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    alarm_data.remove(position);
                    notifyDataSetChanged();
                }


            });
            assert alarm != null;
            tvTime.setText(alarm.time_string);
            return convertView;
        }

    }


}
