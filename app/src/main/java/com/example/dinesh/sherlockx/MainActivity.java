package com.example.dinesh.sherlockx;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,SensorEventListener {


    public TextView username;
    public String email;
    public TextView currdis, totdis, currtime, tottime, syncstatus, gpsspeed, connstatus;

    public Button start,sync;
    public int flagstrt = 0, flagstop = 0;
    private LocationManager locationManagerNET;
    private LocationManager locationManagerGPS;
    public double gpslat, gpslon, gpsacc, netlat, netlon, netacc;
    public double bearing;
    public String cellid;
    public int rssi = 0;
    public String operatorName;
    public TelephonyManager tm;
    public MyPhoneStateListener MyListener;
    public String sfile = "";

    public long prev_time;
    public long curr_time;

    public long starttime;

    // for calculating current journey distance
    public long prev_currdistime;

    public double prev_gpslat,prev_gpslon;

    public int cnt=0;
    public NmeaSentence nmea;
    public String mPDOP= "";
    public String mHDOP= "";
    public String mVDOP= "";
    public SensorManager senSensorManager;
    public Sensor senAccelerometer;
    public float accx,accy,accz; // accelerometer

    public WifiManager mainWifiObj;
    public String wifiinfo = "";

    public int issyncgoing;
    //public String mProvider = "";
    //--------



    public String fname = "";
    public int curr_dis = 0;
    public int curr_currdistime = 0;
    public String usrname="";
    public SQLiteDatabase SherlockD;

    // for acceleration //
    public int acc_cnt=0;
    public long acc_time;
    // ----------------//

    public BufferedWriter bufferedWriter;
    public FileOutputStream fos;
    public ZipOutputStream zos;
    public ZipEntry ze;
    public byte[] buffer = new byte[1024];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = (TextView) findViewById(R.id.username);

       /* Bundle extras = getIntent().getExtras();
        if (extras != null) {

            String value = extras.getString("Account Details");

            String[] uname = value.split("-");
            Log.d("dsada", uname[0]);
            Log.d("dsada", uname[1]);
            username.setText(uname[0]);
            usrname = uname[0];
            email = uname[1].replace(" ","");
            String[] a = email.split("@gmail.com");
            email = a[0];


        }*/

        SharedPreferences details = getSharedPreferences("details",MODE_PRIVATE);
        if(details.contains("islogged")&&details.contains("name")&&details.contains("email")){
            username.setText(details.getString("name","Error"));
            email = details.getString("email","Error");
            email = email.split("@gmail.com")[0];
        }
        else return;


        sfile = "";

        //text = (TextView) findViewById(R.id.text);
        start = (Button) findViewById(R.id.start);
        start.setOnClickListener(this);

        sync = (Button) findViewById(R.id.sync);
        sync.setOnClickListener(this);

        syncstatus = (TextView) findViewById(R.id.sync_status);
        issyncgoing=0;
        gpsspeed = (TextView) findViewById(R.id.gpsspeed);
        connstatus = (TextView) findViewById(R.id.connstatus);

        checkconnection();

        File dir = getExternalFilesDir(null);
        File file[] = dir.listFiles();

        if(file.length==0){
            syncstatus.setText( "Nothing to sync");
        }
        else{
            syncstatus.setText(file.length + " files to be synced");
        }

        flagstrt = 0;
        flagstop = 0;

        currdis = (TextView) findViewById(R.id.currdis);
        totdis = (TextView) findViewById(R.id.totdis);
        currtime = (TextView) findViewById(R.id.currtime);
        tottime = (TextView) findViewById(R.id.tottime);

        createdatabase();

        locationManagerNET = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManagerGPS = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mainWifiObj = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        nmea = new NmeaSentence("");


        start.setText("START");



    }



    private void createdatabase() {

        SherlockD=openOrCreateDatabase("SherlockDB", Context.MODE_PRIVATE, null);
        SherlockD.execSQL("CREATE TABLE IF NOT EXISTS Sherlock(usrname VARCHAR,email VARCHAR PRIMARY KEY,distance INTEGER, time INTEGER);");

        Cursor c=SherlockD.rawQuery("SELECT * FROM Sherlock where email='"+email+"' ", null);
        if(c.moveToFirst())
        {
            totdis.setText(c.getInt(2)+" M");
            tottime.setText(c.getInt(3) + " sec");
        }

    }

    private void updatedatabase(){



        Cursor c=SherlockD.rawQuery("SELECT * FROM Sherlock where email='"+email+"' ", null);
        int dis = 0 ,time = 0;
        if(c.moveToFirst())
        {
           dis = c.getInt(2);
           time = c.getInt(3);
        }

        dis = dis+curr_dis;
        time = time+curr_currdistime;

        SherlockD.execSQL("INSERT OR REPLACE INTO Sherlock VALUES('"+usrname+"','"+
                email+"','"+dis+"','"+time+"');");

        c=SherlockD.rawQuery("SELECT * FROM Sherlock where email='"+email+"' ", null);
        if(c.moveToFirst())
        {
            totdis.setText(c.getInt(2)+" M");
            tottime.setText(c.getInt(3)+" sec");
        }



    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.start && start.getText().toString() == "START") {

            initialize();

            locationManagerNET = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationManagerGPS = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

          //  nmea = new NmeaSentence("");

            mainWifiObj = (WifiManager) getSystemService(Context.WIFI_SERVICE);




            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        0);

                return;

            }

            if (!locationManagerGPS.isProviderEnabled(LocationManager.GPS_PROVIDER) || !locationManagerNET.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Please Enable GPS with High Accuracy")
                        .setCancelable(true)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            }
                        });


                AlertDialog alert = builder.create();
                alert.show();

                return;

            }

            // file name

            Calendar c = Calendar.getInstance();
            int hr = c.get(Calendar.HOUR);
            int mn = c.get(Calendar.MINUTE);
            int sec = c.get(Calendar.SECOND);
            SimpleDateFormat mdformat = new SimpleDateFormat("yyyy_MM_dd");
            String strDate = mdformat.format(c.getTime());
            fname = email + strDate+'_'+hr+'_'+mn+'_'+sec;
             Log.d("dsda", fname);

            // ------------- //
            //sfile = fname+"\n";

            // creeting data file //

           // saving file using buffered writer
           /* try {
                file = new File(getExternalFilesDir(null).toString());
                file.mkdirs();
                f = new File(file, fname + ".txt");
                fw = new FileWriter(f, true);
                bufferedWriter= new BufferedWriter(fw);
                bufferedWriter.write(fname+"\n");
            }
            catch (IOException e){
                e.printStackTrace();
                return;
            }
            */

            // saving file directly to zip //
            String zipfilename = fname+".zip";
            File file = new File(getExternalFilesDir(null).toString());
            file.mkdirs();
            File f = new File(file, fname + ".txt");
            File f1 = new File(file,zipfilename);
            try {
                Log.d("asa",f1.getCanonicalPath());
                Log.d("asas",f.getName());
                fos = new FileOutputStream(f1.getCanonicalPath());
                zos = new ZipOutputStream(fos);
                ze = new ZipEntry(f.getName());
                zos.putNextEntry(ze);
                Log.d("sda","adsads");

            }
            catch (IOException e){
                e.printStackTrace();

            }
            // ------------------------------------------- //

            start.setText("STOP");

            prev_time = System.currentTimeMillis(); // for alarm
            prev_currdistime = prev_time; // for current distance

            starttime = prev_time;

            senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            senSensorManager.registerListener((SensorEventListener) this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);

            locationManagerNET = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationManagerNET.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNET);
            locationManagerGPS = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationManagerGPS.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGPS);

            locationManagerGPS.addNmeaListener(nmeaListener);



            Toast.makeText(getApplicationContext(), "Data Collection Started !!!", Toast.LENGTH_SHORT).show();

            tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            MyListener = new MyPhoneStateListener();
            tm.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);


        } else if (v.getId() == R.id.start && start.getText().toString() == "STOP") {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(" Are you sure you want to STOP ?")
                    .setCancelable(true)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            start.setText("START");

                            removeupdates();

                             //sendtoserver();
                            Toast.makeText(MainActivity.this, " Data Collection Stopped ", Toast.LENGTH_SHORT).show();


                            writeToFile();
                            updatedatabase();
                            File dir = getExternalFilesDir(null);
                            File file[] = dir.listFiles();

                            if(file.length==0){
                                syncstatus.setText( "Nothing to sync");
                            }
                            else{
                                syncstatus.setText(file.length + " files to be synced");
                            }



                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();

            // --------------- //


            // --------------- //

        }

        else if(v.getId() == R.id.sync && issyncgoing==0){
            issyncgoing = 1;
            syncstart();


        }
        else if(v.getId() == R.id.sync && issyncgoing==1){

            Toast.makeText(getApplicationContext(),"Wait for previous sync to complete",Toast.LENGTH_SHORT).show();

        }
    }

    private void initialize() {

        acc_cnt=0;
        wifiinfo = "";
        cnt=0;
        currdis.setText("0 M");
        currtime.setText("0 sec");
        curr_dis = 0;
        curr_currdistime = 0;
        gpslat=0.0; gpslon=0.0; gpsacc=0.0;
        netlat=0.0; netlon=0.0; netacc=0.0;
        bearing=0.0; rssi=0;
        wifiinfo="";
        accx = 0; accy=0; accz=0;
        checkconnection();

    }

    private void checkconnection() {

        // check if internet connected //

        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo != null && activeNetworkInfo.isConnected()){
            connstatus.setText("Connected");
        }
        else
            connstatus.setText("No Internet");

        // --------------  //
    }


    LocationListener locationListenerNET = new LocationListener() {
        public void onLocationChanged(Location location) {

            // Toast.makeText(MainActivity.this, "location changed", Toast.LENGTH_SHORT).show();
            Log.d("Listener", "Location changed");

            // wifi list //

            mainWifiObj.startScan();
            List<ScanResult> wifiScanList = mainWifiObj.getScanResults();
            int length=wifiScanList.size();
            for(int i = 0; i < length; i++){
                if(i==0)
                    wifiinfo = wifiScanList.get(i).BSSID.toString()+";"+wifiScanList.get(i).SSID.toString()+";"+Integer.toString(wifiScanList.get(i).level)+",";
                else
                    wifiinfo += wifiScanList.get(i).BSSID.toString()+";"+wifiScanList.get(i).SSID.toString()+";"+Integer.toString(wifiScanList.get(i).level)+",";

            }

            // ---------------------- //

            curr_time = System.currentTimeMillis();

            currtime.setText(String.valueOf((curr_time - starttime) / 1000) + " sec");
            curr_currdistime = (int) ((curr_time -starttime)/1000);

            if(curr_time - prev_time >= 30000){
                prev_time = curr_time;
                Log.d("Alarm","Alarm");
                PowerManager.WakeLock wakeLock = ((PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE)).newWakeLock(
                        PowerManager.SCREEN_DIM_WAKE_LOCK |
                                PowerManager.ACQUIRE_CAUSES_WAKEUP , "WakeLock");

                wakeLock.acquire();
                wakeLock.release();

            }

            Calendar c = Calendar.getInstance();
            int hr = c.get(Calendar.HOUR);
            int mn = c.get(Calendar.MINUTE);
            int sec = c.get(Calendar.SECOND);
            SimpleDateFormat mdformat = new SimpleDateFormat("yyyy/MM/dd");
            String strDate = mdformat.format(c.getTime());

            GsmCellLocation loc = (GsmCellLocation) tm.getCellLocation();
            cellid = String.valueOf(loc.getCid() & 0xffff);
            operatorName = tm.getSimOperatorName();

            netlat = location.getLatitude();
            netlon = location.getLongitude();
            netacc = location.getAccuracy();
            netacc = Math.round(netacc * 100);
            netacc = netacc / 100.0;
            sfile = strDate + " || " + hr + "::" + mn + "::" + sec + " || " + gpslat + " || " + gpslon + " || " + gpsacc + " || " + netlat + " || " + netlon +
                    " || " + netacc + " || " + cellid + " || " + operatorName + " || " + rssi + " || " +bearing + " || " + mPDOP +" || "+mHDOP+" || "+mVDOP +" || "+accx+ " || " + accy + " || " + accz +" || " +wifiinfo+"\n";
            //text.setText(strDate + " || " + hr + "::" + mn + "::" + sec + " || " + gpslat + " || " + gpslon + " || " + gpsacc + " || " + netlat + " || " + netlon + " || " + netacc + " || " + cellid + " || " + operatorName + " || " + rssi);
            try {
                //bufferedWriter.write(sfile);
                zos.write(sfile.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };

    LocationListener locationListenerGPS = new LocationListener() {
        public void onLocationChanged(Location location) {

            //Toast.makeText(MainActivity.this, "location changed", Toast.LENGTH_SHORT).show();
            Log.d("Listener", "Location changed");

            bearing = location.getBearing();

            // wifi list //
            mainWifiObj.startScan();
            List<ScanResult> wifiScanList = mainWifiObj.getScanResults();
            int length=wifiScanList.size();
            for(int i = 0; i < length; i++){
                if(i==0)
                    wifiinfo = wifiScanList.get(i).BSSID.toString()+";"+wifiScanList.get(i).SSID.toString()+";"+Integer.toString(wifiScanList.get(i).level)+",";
                else
                    wifiinfo += wifiScanList.get(i).BSSID.toString()+";"+wifiScanList.get(i).SSID.toString()+";"+Integer.toString(wifiScanList.get(i).level)+",";

            }

            // ---------------------- //


            curr_time = System.currentTimeMillis();


            float spd = location.getSpeed();
            gpsspeed.setText((int) spd + " m/s");

            // for acceleration //

           /* if(senSensorManager!=null && spd>=1 && acc_cnt==0){
                acc_cnt =1;
                acc_time = curr_time;
                Log.d("fdf","hiha");
                senSensorManager.registerListener(MainActivity.this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
            }

            else {
                if (senSensorManager!=null && acc_cnt ==1 && ((curr_time-acc_time) >= 5 * 60 * 1000)) {

                    Log.d("fdf","haha");
                    senSensorManager.unregisterListener(MainActivity.this,senAccelerometer);
                    senSensorManager=null;

                }
            }

            */

            // ------------------ //

            currtime.setText(String.valueOf ((curr_time -starttime)/1000) + " sec");
            curr_currdistime = (int) ((curr_time -starttime)/1000);

            if(curr_time - prev_time >= 30000){
                prev_time = curr_time;
                Log.d("Alarm","Alarm");
                PowerManager.WakeLock wakeLock = ((PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE)).newWakeLock(
                        PowerManager.SCREEN_DIM_WAKE_LOCK |
                                PowerManager.ACQUIRE_CAUSES_WAKEUP , "WakeLock");

                wakeLock.acquire();
                wakeLock.release();

            }

            Calendar c = Calendar.getInstance();
            int hr = c.get(Calendar.HOUR);
            int mn = c.get(Calendar.MINUTE);
            int sec = c.get(Calendar.SECOND);

            SimpleDateFormat mdformat = new SimpleDateFormat("yyyy/MM/dd");
            String strDate = mdformat.format(c.getTime());

            GsmCellLocation loc = (GsmCellLocation) tm.getCellLocation();
            cellid = String.valueOf(loc.getCid() & 0xffff);
            operatorName = tm.getSimOperatorName();

            gpslat = location.getLatitude();
            gpslon = location.getLongitude();
            gpsacc = location.getAccuracy();
            gpsacc = Math.round(gpsacc * 100);
            gpsacc = gpsacc / 100.0;

            sfile = strDate + " || " + hr + "::" + mn + "::" + sec + " || " + gpslat + " || " + gpslon + " || " + gpsacc + " || " + netlat + " || " + netlon +
                    " || " + netacc + " || " + cellid + " || " + operatorName + " || " + rssi + " || " +bearing + " || " + mPDOP +" || "+mHDOP+" || "+mVDOP +" || "+accx+ " || " + accy + " || " + accz +" || " +wifiinfo+"\n";
            //text.setText(strDate + " || " + hr + "::" + mn + "::" + sec + " || " + gpslat + " || " + gpslon + " || " + gpsacc + " || " + netlat + " || " + netlon + " || " + netacc + " || " + cellid + " || " + operatorName + " || " + rssi);
            try {
                //bufferedWriter.write(sfile);
                zos.write(sfile.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            // for current journey distance //

            if(cnt==0 && gpslat!=0 && gpslon!=0 ){
                prev_gpslat = gpslat;
                prev_gpslon = gpslon;
                cnt=1;
                prev_currdistime = curr_time;
                //Toast.makeText(getApplicationContext(),"hiha",Toast.LENGTH_SHORT).show();
            }

            else if(cnt==1 && curr_time-prev_currdistime >=10000 ){
                float[] res = new float[3];
                //Toast.makeText(getApplicationContext(),"above if",Toast.LENGTH_SHORT).show();
                if(gpslat != 0 && gpslon != 0){
                    Location.distanceBetween(prev_gpslat,prev_gpslon,gpslat,gpslon,res);

                    // Toast.makeText(getApplicationContext(), (int) res[0],Toast.LENGTH_SHORT).show();
                    // Toast.makeText(getApplicationContext(),"hahaaha == "+res[0],Toast.LENGTH_SHORT).show();
                    curr_dis = (int) (curr_dis + res[0]);
                    currdis.setText(String.valueOf(curr_dis) + " M");

                    //currdis.setText("100 KM");
                    prev_gpslat = gpslat;

                    prev_gpslon = gpslon;
                    prev_currdistime = curr_time;
                    // Toast.makeText(getApplicationContext(),"inside if",Toast.LENGTH_SHORT).show();
                }

            }

            // ------------------------ //

        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };


    private class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            // Toast.makeText(getApplicationContext(), "signal changed" , Toast.LENGTH_SHORT).show();
            Log.d("signal", "signal changed");

            // collect wifi information //

            curr_time = System.currentTimeMillis();

            // Accelerometer //
            /*
            if (senSensorManager!=null && acc_cnt ==1 && ((curr_time-acc_time) >= 5*60 * 1000)) {

                Log.d("fdf","haha");
                senSensorManager.unregisterListener(MainActivity.this,senAccelerometer);
                senSensorManager=null;

            }*/


            if (!locationManagerGPS.isProviderEnabled(LocationManager.GPS_PROVIDER) || !locationManagerNET.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("GPS setting changed -- Please Enable GPS with High Accuracy and start again")
                        .setCancelable(true)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });


                AlertDialog alert = builder.create();
                alert.show();

                start.setText("START");

                removeupdates();

                //sendtoserver();
                Toast.makeText(MainActivity.this, " Data Collection Stopped ", Toast.LENGTH_SHORT).show();


                writeToFile();
                updatedatabase();
                File dir = getExternalFilesDir(null);
                File file[] = dir.listFiles();

                if(file.length==0){
                    syncstatus.setText( "Nothing to sync");
                }
                else{
                    syncstatus.setText(file.length + " files to be synced");
                }


                return;

            }


            mainWifiObj.startScan();
            List<ScanResult> wifiScanList = mainWifiObj.getScanResults();
            int length=wifiScanList.size();
            for(int i = 0; i < length; i++){
                if(i==0)
                    wifiinfo = wifiScanList.get(i).BSSID.toString()+";"+wifiScanList.get(i).SSID.toString()+";"+Integer.toString(wifiScanList.get(i).level)+",";
                else
                    wifiinfo += wifiScanList.get(i).BSSID.toString()+";"+wifiScanList.get(i).SSID.toString()+";"+Integer.toString(wifiScanList.get(i).level)+",";

            }

            Log.d("wifi: ",wifiinfo);
            // -------------- //

            currtime.setText(String.valueOf ((curr_time -starttime)/1000) + " sec");
            curr_currdistime = (int) ((curr_time -starttime)/1000);

            if(curr_time - prev_time >= 30000){

                prev_time = curr_time;

                Log.d("Alarm","Alarm");
                PowerManager.WakeLock wakeLock = ((PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE)).newWakeLock(
                        PowerManager.SCREEN_DIM_WAKE_LOCK |
                                PowerManager.ACQUIRE_CAUSES_WAKEUP , "WakeLock");

                wakeLock.acquire();
                wakeLock.release();

            }

            int val = -113 + 2 * signalStrength.getGsmSignalStrength();
            rssi = val;
            GsmCellLocation loc = (GsmCellLocation) tm.getCellLocation();

            cellid = String.valueOf(loc.getCid() & 0xffff);
            operatorName = tm.getSimOperatorName();

            Calendar c = Calendar.getInstance();
            int hr = c.get(Calendar.HOUR);
            int mn = c.get(Calendar.MINUTE);
            int sec = c.get(Calendar.SECOND);

            SimpleDateFormat mdformat =new SimpleDateFormat("yyyy/MM/dd");
            String strDate = mdformat.format(c.getTime());

            sfile = strDate + " || " + hr + "::" + mn + "::" + sec + " || " + gpslat + " || " + gpslon + " || " + gpsacc + " || " + netlat + " || " + netlon +
                    " || " + netacc + " || " + cellid + " || " + operatorName + " || " + rssi + " || " +bearing + " || " + mPDOP +" || "+mHDOP+" || "+mVDOP +" || "+accx+ " || " + accy + " || " + accz +" || " +wifiinfo+"\n";
            //text.setText(strDate + " || " + hr + "::" + mn + "::" + sec + " || " + gpslat + " || " + gpslon + " || " + gpsacc + " || " + netlat + " || " + netlon + " || " + netacc + " || " + cellid + " || " + operatorName + " || " + rssi);
            try {
                //bufferedWriter.write(sfile);
                zos.write(sfile.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    private GpsStatus.NmeaListener nmeaListener = new GpsStatus.NmeaListener() {
        @Override
        public void onNmeaReceived(long timestamp, String nmeaSentence) {

            //Toast.makeText(getApplicationContext(),"HIHA",Toast.LENGTH_SHORT).show();
            if ((nmeaSentence==null || nmeaSentence.trim().length()==0 )) {
                return;
            }
           // Toast.makeText(getApplicationContext(),nmeaSentence,Toast.LENGTH_SHORT).show();
            nmea.setNmeaSentence(nmeaSentence);
            if (nmea.isLocationSentence()) {
                mPDOP = nmea.getLatestPdop().isEmpty() ? mPDOP : nmea.getLatestPdop();
                mHDOP = nmea.getLatestHdop().isEmpty() ? mHDOP : nmea.getLatestHdop();;
                mVDOP = nmea.getLatestVdop().isEmpty() ? mVDOP : nmea.getLatestVdop();;
                //Toast.makeText(getApplicationContext(),mPDOP + " :: " + mHDOP + " :: " + mVDOP,Toast.LENGTH_SHORT).show();
               // Log.d("dasda","asda");
            }

        }
    };

    // sensor methods

    @Override
    public void onSensorChanged(SensorEvent sensorEvent){
        Sensor mySensor = sensorEvent.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
             accx = sensorEvent.values[0];
             accy = sensorEvent.values[1];
             accz = sensorEvent.values[2];
            Calendar c = Calendar.getInstance();
            int hr = c.get(Calendar.HOUR);
            int mn = c.get(Calendar.MINUTE);
            int sec = c.get(Calendar.SECOND);

            SimpleDateFormat mdformat =new SimpleDateFormat("yyyy/MM/dd");
            String strDate = mdformat.format(c.getTime());

            sfile = strDate + " || " + hr + "::" + mn + "::" + sec + " || " + gpslat + " || " + gpslon + " || " + gpsacc + " || " + netlat + " || " + netlon +
                    " || " + netacc + " || " + cellid + " || " + operatorName + " || " + rssi + " || " +bearing + " || " + mPDOP +" || "+mHDOP+" || "+mVDOP +" || "+accx+ " || " + accy + " || " + accz +" || " +wifiinfo+"\n";

           Log.d("accx", String.valueOf(accx)+" "+String.valueOf(accy)+" "+String.valueOf(accz));
//            Log.d("accy",String.valueof(accy) );
//            Log.d("accz", String.valueOf(accz));
            try {
                //bufferedWriter.write(sfile);
                zos.write(sfile.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // --------------------- //

   /* private void sendtoserver() {

        new Thread(new Runnable() {
            public void run() {

                try{
                    URL url = new URL("http://10.129.28.209:8080/sherlock_server/Main");
                    URLConnection connection = url.openConnection();

                    Log.d("inputString", sfile);

                    connection.setDoOutput(true);
                    OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
                    out.write(sfile);
                    out.close();


                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    final String returnString = in.readLine();

                    runOnUiThread(new Runnable() {
                        public void run() {

                            if (returnString.equals("Success"))
                                Toast.makeText(getApplicationContext(), "file uploaded to server successfully", Toast.LENGTH_SHORT).show();
                            else {
                                Toast.makeText(getApplicationContext(), " Server error -- saving file locally ...", Toast.LENGTH_SHORT).show();
                                writeToFile(sfile);
                            }

                        }
                    });


                    in.close();

                    //if(returnString == "Success") fileuploadsuccess="yes";


                }catch(Exception e)
                {
                    Log.d("Exception", e.toString());
                    writeToFile(sfile);
                    runOnUiThread(new Runnable() {
                        public void run() {

                            Toast.makeText(getApplicationContext(), "network error -- saving file locally ... ", Toast.LENGTH_SHORT).show();
                            writeToFile(sfile);
                        }
                    });



                }



            }
        }).start();

    }

       */

    private void syncstart() {

        new Thread(new Runnable() {
            public void run() {

                try{

                    File dir = getExternalFilesDir(null);
                    File file[] = dir.listFiles();

                    if(file.length==0){
                        issyncgoing = 0;
                        runOnUiThread(new Runnable() {
                            public void run() {

                                Toast.makeText(getApplicationContext(), "No files to Sync", Toast.LENGTH_SHORT).show();

                            }
                        });
                        return;
                    }

                    int i=1;
                    final int l=file.length;
                    final int[] x = {l};
                    for(File f : file){

                        URL url = new URL("http://10.129.28.209:8080/sherlock_server/Main");
                        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                        conn.setConnectTimeout(10000); // connection timeout set to be 10 seconds

                        Log.d("inputString", String.valueOf(f));

                        FileInputStream fstrm = new FileInputStream(f);


                        String boundary = "*****";
                        String Tag="fSnd";
                        Log.e(Tag,"Starting Http File Sending to URL");

                        // Open a HTTP connection to the URL
                       // HttpURLConnection conn = (HttpURLConnection)connectURL.openConnection();

                        // Allow Inputs
                        conn.setDoInput(true);

                        // Allow Outputs
                        conn.setDoOutput(true);

                        // Don't use a cached copy.
                        conn.setUseCaches(false);

                        // Use a post method.
                        conn.setRequestMethod("POST");

                        conn.setRequestProperty("Connection", "Keep-Alive");

                        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
                        Log.e(Tag,"Starting Http File Sending to URL");

                        // Open a HTTP connection to the URL
                        //HttpURLConnection conn = (HttpURLConnection)connectURL.openConnection();


                        // Allow Inputs
                        /* conn.setDoInput(true);

                        // Allow Outputs
                        conn.setDoOutput(true);

                        // Don't use a cached copy.
                        conn.setUseCaches(false);

                        // Use a post method.
                        conn.setRequestMethod("POST");

                        conn.setRequestProperty("Connection", "Keep-Alive");

                        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary); */

                        DataOutputStream dos = new DataOutputStream(conn.getOutputStream());


                        //String[] fn = f.toString().split("/");

                       // dos.writeBytes(fn[fn.length-1].replace(".txt","")+"\n");

                        Log.e(Tag, "Headers are written");

                        int bytesAvailable = fstrm.available();

                        int maxBufferSize = 1024;
                        int bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        byte[ ] buffer = new byte[bufferSize];

                        // read file and write it into form...
                        int bytesRead = fstrm.read(buffer, 0, bufferSize);

                        while (bytesRead > 0)
                        {
                            dos.write(buffer, 0, bufferSize);
                            bytesAvailable = fstrm.available();
                            bufferSize = Math.min(bytesAvailable,maxBufferSize);
                            bytesRead = fstrm.read(buffer, 0,bufferSize);
                        }
                        //dos.writeBytes(lineEnd);
                        //dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                        // close streams
                        fstrm.close();

                        dos.flush();

                        Log.e(Tag,"File Sent, Response: "+String.valueOf(conn.getResponseCode()));

                        InputStream is = conn.getInputStream();

                        // retrieve the response from server
                        /*int ch;
                        StringBuffer b =new StringBuffer();
                        while( ( ch = is.read() ) != -1 ){ b.append( (char)ch ); }
                        String s=b.toString();
                        Log.i("Response",s);*/
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        final String returnString = in.readLine();

                        Log.d("asdas",returnString);
                        if(returnString.equals("Success")){

                            f.delete();

                            final int finalI = i;
                            runOnUiThread(new Runnable() {
                                public void run() {

                                    x[0] = x[0] -1;

                                    Toast.makeText(getApplicationContext(), finalI +" out of "+l+" files synched succesfully",Toast.LENGTH_SHORT).show();
                                    syncstatus.setText(x[0] +" files to be synced");
                                }
                            });
                            i++;
                        }



                        dos.close();


                    }


                    issyncgoing=0;


                }catch(Exception e)
                {
                    Log.d("Exception",e.toString());
                    if(e.toString().contains("ConnectException")){
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(MainActivity.this, "Unable to Connect -- check your internet connection", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    issyncgoing=0;
                }

            }
        }).start();

    }

    private void removeupdates() {
        if(senSensorManager!=null) {
            senSensorManager.unregisterListener((SensorEventListener) this);
            senSensorManager = null;
        }
        if (locationManagerNET != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            locationManagerNET.removeUpdates(locationListenerNET);
            locationManagerNET = null;
        }

        if (locationManagerGPS != null) {
            locationManagerGPS.removeUpdates(locationListenerGPS);
            locationManagerGPS.removeNmeaListener(nmeaListener);
           // nmeaListener=null;
            locationManagerGPS = null;

        }

        if (tm != null)
            tm.listen(MyListener, PhoneStateListener.LISTEN_NONE);



    }



    public boolean writeToFile()  {

        //Log.d("write :", data);

        try {
            //bufferedWriter.close();
            zos.closeEntry();
            zos.close();
            fos.close();
            Toast.makeText(this,"file successfully saved locally ",Toast.LENGTH_SHORT).show();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }



    }




    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        // Log.d("sda","sdas");
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            moveTaskToBack(true);
            return true; // return
        }

        return false;
    }
}
