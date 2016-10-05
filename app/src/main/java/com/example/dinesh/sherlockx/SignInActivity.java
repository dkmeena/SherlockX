package com.example.dinesh.sherlockx;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;


import java.util.Calendar;

public class SignInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // for Notifications //

        Calendar cal1 = Calendar.getInstance();

        cal1.set(Calendar.HOUR_OF_DAY, 07);
        cal1.set(Calendar.MINUTE, 00);
        cal1.set(Calendar.SECOND, 00);

        Calendar cal2 = Calendar.getInstance();

        cal2.set(Calendar.HOUR_OF_DAY, 17);
        cal2.set(Calendar.MINUTE, 00);
        cal2.set(Calendar.SECOND, 00);

        Calendar now = Calendar.getInstance();
        if (now.after(cal1))
            cal1.add(Calendar.HOUR_OF_DAY, 24);
        if (now.after(cal2))
            cal2.add(Calendar.HOUR_OF_DAY, 24);

        Intent intent1 = new Intent(getApplicationContext(), NotificationReceiver.class);

        PendingIntent morning = PendingIntent.getBroadcast(getApplicationContext(), 100, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent evening = PendingIntent.getBroadcast(getApplicationContext(), 101, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager amNot = (AlarmManager) getSystemService(ALARM_SERVICE);

        amNot.setRepeating(AlarmManager.RTC_WAKEUP, cal1.getTimeInMillis(), DateUtils.DAY_IN_MILLIS, morning);
        amNot.setRepeating(AlarmManager.RTC_WAKEUP, cal2.getTimeInMillis(), DateUtils.DAY_IN_MILLIS, evening);

        // ----------------------------- //

        SharedPreferences details = getSharedPreferences("details", MODE_PRIVATE);
        Log.d("asas", String.valueOf(details));
        if (!details.contains("islogged")) {

            Intent i = new Intent(getApplicationContext(), SignIn.class);
            startActivity(i);
            Log.d("first","first time login");


        } else {

            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
            Log.d("exists", "login exists");
        }


        //amNot.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() , 30*1000, pendingIntent);
        // -----------------//


    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // getIntent() should always return the most recent
        //setIntent(intent);
    }
}

