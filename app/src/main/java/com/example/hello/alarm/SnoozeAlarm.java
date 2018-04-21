package com.example.hello.alarm;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Calendar;


/**
 * Created by hello on 4/4/18.
 */

public class SnoozeAlarm extends Service {

    public static final String notification_id = "notification_id";
    long current_time;
    PendingIntent pending_intent;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("snooze", "onCreate: Hello");
        NotificationManager notification = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        assert notification != null;
        notification.cancel(intent.getIntExtra(notification_id, -1));

        //Set a temporary alarm after one minute
        long one_minute = 60000;
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        final Intent alarm_intent = new Intent(this, alarm_receiver.class);
        pending_intent = PendingIntent.getBroadcast(this, startId, alarm_intent, PendingIntent.FLAG_UPDATE_CURRENT);
        current_time = Calendar.getInstance().getTimeInMillis();
        AlarmManager.AlarmClockInfo alarm_info = new AlarmManager.AlarmClockInfo(current_time + one_minute, pending_intent);
        alarmManager.setAlarmClock(alarm_info, pending_intent);

        stopSelf(startId);
        return START_NOT_STICKY;
    }
}