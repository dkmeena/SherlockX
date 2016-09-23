package com.example.dinesh.sherlockx;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by dinesh on 22/9/16.
 */
public class RebootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        //Toast.makeText(context, "Reboot Receiver", Toast.LENGTH_SHORT).show();

        if (intent != null && intent.getAction() !=null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
          //  Toast.makeText(context, "Reboot Receiver", Toast.LENGTH_SHORT).show();
            Calendar cal1 = Calendar.getInstance();

            cal1.set(Calendar.HOUR_OF_DAY,07);
            cal1.set(Calendar.MINUTE, 00);
            cal1.set(Calendar.SECOND, 00);

            Calendar cal2 = Calendar.getInstance();

            cal2.set(Calendar.HOUR_OF_DAY,17);
            cal2.set(Calendar.MINUTE,00);
            cal2.set(Calendar.SECOND,00);

            Calendar now = Calendar.getInstance();
            if(now.after(cal1))
                cal1.add(Calendar.HOUR_OF_DAY, 24);
            if(now.after(cal2))
                cal2.add(Calendar.HOUR_OF_DAY, 24);

            Intent intent1 = new Intent(context, NotificationReceiver.class);

            PendingIntent morning = PendingIntent.getBroadcast(context, 100, intent1,PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent evening = PendingIntent.getBroadcast(context, 101, intent1,PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager amNot = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);

            amNot.setRepeating(AlarmManager.RTC_WAKEUP, cal1.getTimeInMillis(), DateUtils.DAY_IN_MILLIS, morning);
            amNot.setRepeating(AlarmManager.RTC_WAKEUP, cal1.getTimeInMillis(), DateUtils.DAY_IN_MILLIS, evening);
            //amNot.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), 30 * 1000, pendingIntent);
            // -----------------//
        }

    }
}
