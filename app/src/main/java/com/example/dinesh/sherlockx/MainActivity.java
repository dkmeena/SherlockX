package com.example.dinesh.sherlockx;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
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
    public float spd=0;

    public WifiManager mainWifiObj;
    public String wifiinfo = "";

    public int issyncgoing;
    //public String mProvider = "";
    //--------

    public int wait=0;

    public String fname = "";
    public int curr_dis = 0;
    public int curr_currdistime = 0;
    public String usrname="";
    public SQLiteDatabase SherlockD;

    // for acceleration //
    public int acc_cnt=0;
    public long acc_time;
    // ----------------//

    public FileOutputStream fos;
    public ZipOutputStream zos;
    public ZipEntry ze;
    public FileOutputStream fos1;
    public ZipOutputStream zos1;
    public ZipEntry ze1;
    public byte[] buffer = new byte[1024];

    public ConnectivityManager cm;

    public int filesizesynced = 0;

    IntentFilter ifilter;
    Intent batteryStatus;
    float init_level= 0;
    float final_level=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences details = getSharedPreferences("details",MODE_PRIVATE);

        // check for updates //

        if(details.contains("lastupdate")) {
           Long lu = details.getLong("lastupdate",0);
            if(lu!=0 && System.currentTimeMillis() -lu > 2*DateUtils.DAY_IN_MILLIS);
                new version().execute("http://safestreet.cse.iitb.ac.in/findmytrain/sherlock_server/version.php");
        }

        // ----------------- //



        // Notify to sync data if lot of data to sync //

        double size=0.0;
        File dir1 = getExternalFilesDir(null);
        File file1[] = dir1.listFiles();


        for(File f:file1){
            if(f.getName().contains("bad")){
                f.delete();
                continue;
            }
            size = size + f.length();
            if(size>=10000000){

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(" You have lot of data collected -- tap sync to upload data to server ")
                        .setCancelable(true)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
               // Log.d("size", String.valueOf(size));
                break;
            }
        }


        // ------------------------- //



        username = (TextView) findViewById(R.id.username);

        if(details.contains("islogged")&&details.contains("name")&&details.contains("email")&&details.contains("lastupdate")){
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
        if (v.getId() == R.id.start && start.getText().toString() == "START" && issyncgoing==0) {

            start();

        } else if (v.getId() == R.id.start && start.getText().toString() == "STOP" ) {

            stop();

        }

        else if (v.getId() == R.id.start && start.getText().toString() == "START" && issyncgoing==1){
            Toast.makeText(getApplicationContext(),"Wait for sync to complete",Toast.LENGTH_SHORT).show();
        }
        else if(v.getId() == R.id.sync && start.getText().toString()=="STOP"){
            Toast.makeText(getApplicationContext()," Stop the trip first ",Toast.LENGTH_SHORT).show();
        }
        else if(v.getId() == R.id.sync && issyncgoing==0 && start.getText().toString()=="START" ){

            if(wait==1){
                Toast.makeText(getApplicationContext(), " wait ... ", Toast.LENGTH_SHORT).show();
                return;
            }

            wait=1;
            File dir = getExternalFilesDir(null);
            File file[] = dir.listFiles();

            if(file.length==0){
                syncstatus.setText( "Nothing to sync");
                Toast.makeText(this,"Nothing to sync",Toast.LENGTH_SHORT).show();
                wait=0;
                return;
            }

            new sync_check().execute("http://safestreet.cse.iitb.ac.in/findmytrain/sherlock_server/data_synced.php");

        }
        else if(v.getId() == R.id.sync && issyncgoing==1){

            Toast.makeText(getApplicationContext(),"Wait for previous sync to complete",Toast.LENGTH_SHORT).show();

        }
    }


    class  sync_check extends AsyncTask<String,String,String> {

        String versionName = "!!";
        int versionCode = 0;
        private ProgressDialog pdia;

        @Override
        protected String doInBackground(String... params) {


            String returnString = "--";

                try {
                    URL url = new URL(params[0]);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(10000);
                    String lineEnd = "\r\n";
                    String twoHyphens = "--";
                    String boundary = "*****";

                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                    DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                    dos.writeBytes(twoHyphens + boundary + lineEnd);  // new form to send
                    dos.writeBytes("Content-Disposition: form-data; name=\"data_synced_check\"" + lineEnd);

                    dos.writeBytes(lineEnd);
                    dos.writeBytes(email);

                    dos.writeBytes(lineEnd);

                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);


                    int serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn.getResponseMessage();

                    Log.i("uploadFile", "HTTP Response is : "
                            + serverResponseMessage + ": " + serverResponseCode);


                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    returnString = in.readLine();

                } catch (Exception e) {
                    e.printStackTrace();
                }

        return returnString;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdia = new ProgressDialog(MainActivity.this);
            pdia.setMessage("processing ...");
            pdia.show();
        }

        @Override
        protected void onPostExecute(String result) {
            //Log.d("asds",result);
            result=result.trim();
            wait=0;
            pdia.dismiss();
            //Log.d("sa",result);
            if(result.equals("--")||result.equals("true")) {
                issyncgoing = 1;
               // Toast.makeText(MainActivity.this, " data sync started", Toast.LENGTH_SHORT).show();
                syncstart();
            }

            else if(result.equals("false")){
               // Log.d("sa",result);
                Toast.makeText(MainActivity.this, " Your Sending limit for a month is over -- try when its over", Toast.LENGTH_SHORT).show();

            }


        }

    }


    private void start() {


        initialize();
        cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    0);

            return;

        }

        locationManagerNET = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManagerGPS = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

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

        String versionName="null";
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = packageInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Calendar c = Calendar.getInstance();
        int hr = c.get(Calendar.HOUR_OF_DAY);
        int mn = c.get(Calendar.MINUTE);
        int sec = c.get(Calendar.SECOND);
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy_MM_dd");
        String strDate = mdformat.format(c.getTime());
        fname = email +'@'+versionName+'@'+ strDate+'-'+hr+'_'+mn+'_'+sec;
       // Log.d("dsda", fname);

        // saving file directly to zip //
        String zipfilename = fname+"_bad.zip";
        File file = new File(getExternalFilesDir(null).toString());
        file.mkdirs();
        File f = new File(file, fname + ".txt");
        File f1 = new File(file,zipfilename);

        try {
            //Log.d("asa",f1.getCanonicalPath());
            //Log.d("asas", f.getName());
            fos = new FileOutputStream(f1.getCanonicalPath());
            zos = new ZipOutputStream(fos);
            ze = new ZipEntry(f.getName());
            zos.putNextEntry(ze);

           // Log.d("sda","adsads");

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
        mainWifiObj = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        nmea = new NmeaSentence("");
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        locationManagerNET.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNET);
        locationManagerGPS.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGPS);

        locationManagerGPS.addNmeaListener(nmeaListener);


        Toast.makeText(getApplicationContext(), "Data Collection Started !!!", Toast.LENGTH_SHORT).show();

        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        MyListener = new MyPhoneStateListener();
        tm.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);



    }

    private void stop() {



        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(" Are you sure you want to STOP ?")
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                        batteryStatus = registerReceiver(null, ifilter);

                        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                        final_level = level / (float) scale;
                        final_level=final_level*100;
                        Log.d("final", String.valueOf(final_level));

                        removeupdates();

                        //sendtoserver();
                        Toast.makeText(MainActivity.this, " Data Collection Stopped ", Toast.LENGTH_SHORT).show();


                        writeToFile();
                        updatedatabase();

                        start.setText("START");

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

    }

    private void initialize() {


        // battery level //

        ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        init_level = level / (float)scale;
        init_level=init_level*100;
        Log.d("final", String.valueOf(init_level));
        // ------------ //



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
        spd=0;
        wait=0;
        filesizesynced = 0;

        checkconnection();


        // for learned notification //
        Calendar c = Calendar.getInstance();
        int hr = c.get(Calendar.HOUR_OF_DAY);;

        SharedPreferences notif = getSharedPreferences("notifications", MODE_PRIVATE);
        int cnt = notif.getInt(String.valueOf(hr), 0);
        int max = notif.getInt("max", -1);
        SharedPreferences.Editor edit = notif.edit();
        if(max==-1){
            edit.putInt("max",hr);
        }
        else{
            int max_cnt =notif.getInt(String.valueOf(max),-1);
            if(max_cnt < cnt + 1)
                edit.putInt("max",hr);
        }


        edit.putInt(String.valueOf(hr), cnt+1);
        edit.apply();

        // ------------------------- //


       /* Map<String,?> keys = notif.getAll();

        for(Map.Entry<String,?> entry : keys.entrySet()){
            Log.d("map values",entry.getKey() + ": " +
                    entry.getValue().toString());
        }*/


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

            // --- Stop taking acceelration data --- //

            if (senSensorManager!=null && acc_cnt ==1 && ((curr_time-acc_time) >= 5 * 60 * 1000)) {

               // Log.d("fdf","haha");
                senSensorManager.unregisterListener(MainActivity.this,senAccelerometer);
                senSensorManager=null;

            }

             // ----------------- //
            // Toast.makeText(MainActivity.this, "location changed", Toast.LENGTH_SHORT).show();
            //Log.d("Listener", "Location changed");

            // wifi list //

            mainWifiObj.startScan();
            List<ScanResult> wifiScanList = mainWifiObj.getScanResults();
            int length=wifiScanList.size();
            for(int i = 0; i < length; i++){
                if(i==0)
                    wifiinfo = wifiScanList.get(i).BSSID+";"+wifiScanList.get(i).SSID+";"+Integer.toString(wifiScanList.get(i).level)+",";
                else
                    wifiinfo += wifiScanList.get(i).BSSID+";"+wifiScanList.get(i).SSID+";"+Integer.toString(wifiScanList.get(i).level)+",";

            }

            // ---------------------- //

            curr_time = System.currentTimeMillis();

            if(curr_time - starttime > 7200000){
                stop();
                return;
            }

            currtime.setText(String.valueOf((curr_time - starttime) / 1000) + " sec");
            curr_currdistime = (int) ((curr_time -starttime)/1000);

            if(curr_time - prev_time >= 30000){
                prev_time = curr_time;
                //Log.d("Alarm","Alarm");
                PowerManager.WakeLock wakeLock = ((PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE)).newWakeLock(
                        PowerManager.SCREEN_DIM_WAKE_LOCK |
                                PowerManager.ACQUIRE_CAUSES_WAKEUP , "WakeLock");

                wakeLock.acquire();
                wakeLock.release();

            }


            Calendar c = Calendar.getInstance();
            int hr = c.get(Calendar.HOUR_OF_DAY);
            int mn = c.get(Calendar.MINUTE);
            int sec = c.get(Calendar.SECOND);

            GsmCellLocation loc = (GsmCellLocation) tm.getCellLocation();
            cellid = String.valueOf(loc.getCid() & 0xffff);
            operatorName = tm.getSimOperatorName();

            netlat = location.getLatitude();
            netlon = location.getLongitude();
            netacc = location.getAccuracy();
            netacc = Math.round(netacc * 100);
            netacc = netacc / 100.0;


            // check if connected to network //
            String connect = "null";
            String type="null",subtype="null";
            boolean isConnected;
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            isConnected = activeNetwork != null && activeNetwork.isConnected();
            if(isConnected){
                connect = "yes";
                type = activeNetwork.getTypeName();
                subtype = activeNetwork.getSubtypeName();

            }

            // ------------------------------ //

            sfile =  hr + "::" + mn + "::" + sec + " || " + gpslat + " || " + gpslon + " || " + gpsacc + " || " + netlat + " || " + netlon +
                    " || " + netacc + " || " + cellid + " || " + operatorName + " || " + rssi + " || " +connect+ " || " + type + " || " + subtype +" || " +spd+" || "+bearing + " || " + mPDOP +" || "+mHDOP+" || "+mVDOP +" || "+wifiinfo+"\n";

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
            //Log.d("Listener", "Location changed");

            bearing = location.getBearing();

            // wifi list //
            mainWifiObj.startScan();
            List<ScanResult> wifiScanList = mainWifiObj.getScanResults();
            int length=wifiScanList.size();
            for(int i = 0; i < length; i++){
                if(i==0)
                    wifiinfo = wifiScanList.get(i).BSSID+";"+wifiScanList.get(i).SSID+";"+Integer.toString(wifiScanList.get(i).level)+",";
                else
                    wifiinfo += wifiScanList.get(i).BSSID+";"+wifiScanList.get(i).SSID+";"+Integer.toString(wifiScanList.get(i).level)+",";

            }

            // ---------------------- //


            curr_time = System.currentTimeMillis();


            if(curr_time - starttime > 7200000){
                stop();
                return;
            }

            spd = location.getSpeed();
            gpsspeed.setText((int) spd + " m/s");

            // for acceleration //

            if(spd>=5 && acc_cnt==0 && senSensorManager!=null ){
                acc_cnt =1;
                acc_time = curr_time;

                //Log.d("fdf","hiha");

                String zipfilename = "";
                File file = new File(getExternalFilesDir(null).toString());
                file.mkdirs();

                zipfilename = fname+"_bad_acc.zip";
                File f2 = new File(file, fname + "_acc.txt");
                File f22 = new File(file,zipfilename);

                try{
                fos1 = new FileOutputStream(f22.getCanonicalPath());
                zos1 = new ZipOutputStream(fos1);
                ze1 = new ZipEntry(f2.getName());
                zos1.putNextEntry(ze1);
                }
                catch (IOException e){
                    e.printStackTrace();

                }

                senSensorManager.registerListener(MainActivity.this, senAccelerometer , SensorManager.SENSOR_DELAY_FASTEST);
            }



            // ------------------ //

            currtime.setText(String.valueOf ((curr_time -starttime)/1000) + " sec");
            curr_currdistime = (int) ((curr_time -starttime)/1000);

            if(curr_time - prev_time >= 30000){
                prev_time = curr_time;
                //Log.d("Alarm","Alarm");
                PowerManager.WakeLock wakeLock = ((PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE)).newWakeLock(
                        PowerManager.SCREEN_DIM_WAKE_LOCK |
                                PowerManager.ACQUIRE_CAUSES_WAKEUP , "WakeLock");

                wakeLock.acquire();
                wakeLock.release();

            }

            Calendar c = Calendar.getInstance();
            int hr = c.get(Calendar.HOUR_OF_DAY);
            int mn = c.get(Calendar.MINUTE);
            int sec = c.get(Calendar.SECOND);


            GsmCellLocation loc = (GsmCellLocation) tm.getCellLocation();
            cellid = String.valueOf(loc.getCid() & 0xffff);
            operatorName = tm.getSimOperatorName();

            gpslat = location.getLatitude();
            gpslon = location.getLongitude();
            gpsacc = location.getAccuracy();
            gpsacc = Math.round(gpsacc * 100);
            gpsacc = gpsacc / 100.0;


            // check if connected to network //
            String connect = "null";
            String type="null",subtype="null";
            boolean isConnected;
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            isConnected = activeNetwork != null && activeNetwork.isConnected();
            if(isConnected){
                connect = "yes";
                type = activeNetwork.getTypeName();
                subtype = activeNetwork.getSubtypeName();

            }

            // ------------------------------ //

            sfile =  hr + "::" + mn + "::" + sec + " || " + gpslat + " || " + gpslon + " || " + gpsacc + " || " + netlat + " || " + netlon +
                    " || " + netacc + " || " + cellid + " || " + operatorName + " || " + rssi + " || " +connect+ " || " + type + " || " + subtype +" || " +spd+" || "+bearing + " || " + mPDOP +" || "+mHDOP+" || "+mVDOP +" || "+wifiinfo+"\n";

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
            //Log.d("signal", "signal changed");


            // collect wifi information //

            curr_time = System.currentTimeMillis();

            if(curr_time - starttime > 7200000){
                stop();
                return;
            }

            if ( locationManagerGPS!=null && !locationManagerGPS.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationListenerNET!=null && !locationManagerNET.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

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
                    wifiinfo = wifiScanList.get(i).BSSID+";"+wifiScanList.get(i).SSID+";"+Integer.toString(wifiScanList.get(i).level)+",";
                else
                    wifiinfo += wifiScanList.get(i).BSSID+";"+wifiScanList.get(i).SSID+";"+Integer.toString(wifiScanList.get(i).level)+",";

            }

            //Log.d("wifi: ",wifiinfo);
            // -------------- //

            currtime.setText(String.valueOf ((curr_time -starttime)/1000) + " sec");
            curr_currdistime = (int) ((curr_time -starttime)/1000);

            if(curr_time - prev_time >= 30000){

                prev_time = curr_time;

                //Log.d("Alarm","Alarm");
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
            int hr = c.get(Calendar.HOUR_OF_DAY);
            int mn = c.get(Calendar.MINUTE);
            int sec = c.get(Calendar.SECOND);


            // check if connected to network //
            String connect = "null";
            String type="null",subtype="null";
            boolean isConnected;
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            isConnected = activeNetwork != null && activeNetwork.isConnected();
            if(isConnected){
                connect = "yes";
                type = activeNetwork.getTypeName();
                subtype = activeNetwork.getSubtypeName();

            }

            // ------------------------------ //

            sfile =  hr + "::" + mn + "::" + sec + " || " + gpslat + " || " + gpslon + " || " + gpsacc + " || " + netlat + " || " + netlon +
                    " || " + netacc + " || " + cellid + " || " + operatorName + " || " + rssi + " || " +connect+ " || " + type + " || " + subtype +" || " +spd+" || "+bearing + " || " + mPDOP +" || "+mHDOP+" || "+mVDOP +" || "+wifiinfo+"\n";

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
            int hr = c.get(Calendar.HOUR_OF_DAY);
            int mn = c.get(Calendar.MINUTE);
            int sec = c.get(Calendar.SECOND);

            String acc_s = hr + "::" + mn + "::" + sec + " || " + accx+ " || " + accy + " || " + accz +"\n";

            try {
                //bufferedWriter.write(sfile);
                zos1.write(acc_s.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }



    private void syncstart() {

        new Thread(new Runnable() {
            public void run() {
                filesizesynced = 0;
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


                    URL url = new URL("http://safestreet.cse.iitb.ac.in/findmytrain/sherlock_server/filereceiver.php");
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.setConnectTimeout(10000); // connection timeout set to be 10 seconds


                    int i=1;
                    final int l=file.length;
                    final int[] x = {l};
                    String lineEnd = "\r\n";
                    String twoHyphens = "--";
                    String boundary = "*****";

                   for(File f : file) {

                       FileInputStream fileInputStream = new FileInputStream(f);
                       url = new  URL("http://safestreet.cse.iitb.ac.in/findmytrain/sherlock_server/filereceiver.php");
                       conn = (HttpURLConnection)url.openConnection();
                       conn.setConnectTimeout(10000); // connection timeout set to be 10 seconds

                       conn.setDoInput(true); // Allow Inputs
                       conn.setDoOutput(true); // Allow Outputs
                       conn.setUseCaches(false); // Don't use a Cached Copy
                       conn.setRequestMethod("POST");
                       conn.setRequestProperty("Connection", "Keep-Alive");
                       conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                       conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                       conn.setRequestProperty("uploaded_file", f.getName());

                       DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                       dos.writeBytes(twoHyphens + boundary + lineEnd);  // new form to send
                       dos.writeBytes("Content-Disposition: form-data; name=\"client_time\"" + lineEnd);

                       dos.writeBytes(lineEnd);

                       // client server time sync //
                       Calendar c = Calendar.getInstance();
                       int hr = c.get(Calendar.HOUR_OF_DAY);
                       int mn = c.get(Calendar.MINUTE);
                       int sec = c.get(Calendar.SECOND);
                       SimpleDateFormat mdformat = new SimpleDateFormat("yyyy_MM_dd");
                       String strDate = mdformat.format(c.getTime());

                       dos.writeBytes(strDate+"-"+hr+"_"+mn+"_"+sec);

                       dos.writeBytes(lineEnd);

                       // -------------------------- //

                       dos.writeBytes(twoHyphens + boundary + lineEnd);
                       dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""+ f.getName() + "\"" + lineEnd);

                       dos.writeBytes(lineEnd);

                       int bytesAvailable = fileInputStream.available();

                       int bufferSize = Math.min(bytesAvailable, 1024);
                       byte[] buffer = new byte[bufferSize];

                       int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                       while (bytesRead > 0) {

                           dos.write(buffer, 0, bufferSize);
                           bytesAvailable = fileInputStream.available();
                           bufferSize = Math.min(bytesAvailable, 1024);
                           bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                       }

                       dos.writeBytes(lineEnd);

                       dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                       int serverResponseCode = conn.getResponseCode();
                       String serverResponseMessage = conn.getResponseMessage();

                       Log.i("uploadFile", "HTTP Response is : "
                               + serverResponseMessage + ": " + serverResponseCode);


                       BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                       final String returnString = in.readLine();

                       //Log.d("asdas",returnString);
                       if(returnString.equals("Success")){
                           filesizesynced+= f.length();
                           f.delete();

                           final int finalI = i;
                           runOnUiThread(new Runnable() {
                               public void run() {

                                   x[0] = x[0] -1;

                                   Toast.makeText(getApplicationContext(), finalI +" out of "+l+" files synced succesfully",Toast.LENGTH_SHORT).show();
                                   syncstatus.setText(x[0] +" files to be synced");
                               }
                           });
                           i++;
                       }

                   }

                    // data sync threshold //

                    url = new  URL("http://safestreet.cse.iitb.ac.in/findmytrain/sherlock_server/data_synced.php");
                    conn = (HttpURLConnection)url.openConnection();
                    conn.setConnectTimeout(10000); // connection timeout set to be 10 seconds
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                    DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                    dos.writeBytes(twoHyphens + boundary + lineEnd);  // new form to send
                    dos.writeBytes("Content-Disposition: form-data; name=\"data_synced\"" + lineEnd);

                    dos.writeBytes(lineEnd);
                    dos.writeBytes(email+"-"+String.valueOf(filesizesynced));

                    dos.writeBytes(lineEnd);

                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    int serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn.getResponseMessage();

                    Log.i("uploadFile", "HTTP Response is : "
                            + serverResponseMessage + ": " + serverResponseCode);


                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    final String returnString = in.readLine();

                    // -------------------------------- //

                    issyncgoing=0;


                }catch(Exception e)
                {
                    Log.d("Exception",e.toString());
                    if(e.toString().contains("failed to connect") || e.toString().contains("SocketTimeout") || e.toString().contains("Unable to resolve host")){
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


        try {
            //bufferedWriter.close();
            zos.closeEntry();
            zos.close();
            fos.close();

            if(zos1!=null) {
                zos1.closeEntry();
                zos1.close();
                fos1.close();
            }


            int power_consumed = Math.round(init_level-final_level);
            Toast.makeText(this,String.valueOf(power_consumed),Toast.LENGTH_SHORT).show();
            if(power_consumed<0) power_consumed = 0;

            File file = new File(getExternalFilesDir(null).toString());
            file.mkdirs();

            File f = new File(file, fname + "_bad.zip");
            f.renameTo(new File(file,fname+":"+curr_dis+"_"+curr_currdistime+"_"+power_consumed+".zip"));

            f = new File(file, fname + "_bad_acc.zip");
            f.renameTo(new File(file,fname+":"+curr_dis+"_"+curr_currdistime+"_acc.zip"));


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


    class  version extends AsyncTask<String,String,String>{

        String versionName="!!";
        int versionCode=0;
        private ProgressDialog pdia;

        @Override
        protected String doInBackground(String... params) {


            String returnString="--";

            try {
                PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                versionName = packageInfo.versionName;
                versionCode = packageInfo.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            if(versionName!="" && versionCode!=0) {
                try {
                    URL url = new URL(params[0]);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(10000);
                    String lineEnd = "\r\n";
                    String twoHyphens = "--";
                    String boundary = "*****";

                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                    int serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn.getResponseMessage();

                    Log.i("uploadFile", "HTTP Response is : "
                            + serverResponseMessage + ": " + serverResponseCode);


                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    returnString = in.readLine();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            return returnString;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pdia = new ProgressDialog(MainActivity.this);
            pdia.setMessage("Checking for updates...");
            pdia.show();
        }
        @Override
        protected void onPostExecute(String result) {
            result=result.trim();
            if(result!="--" && versionName!="!!"){

                    if(result.equals(versionName)){
                        //Log.d("update","no update");
                    }
                    else{


                        SharedPreferences details = getSharedPreferences("details",MODE_PRIVATE);
                        SharedPreferences.Editor edit = details.edit();
                        edit.putLong("lastupdate", System.currentTimeMillis());
                        edit.commit();

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage(" Newer Version available -- Update your App ")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        intent.setData(Uri.parse("market://details?id=com.example.dinesh.sherlockx"));
                                        startActivity(intent);

                                    }
                                })
                                .setNegativeButton("Cancel",new DialogInterface.OnClickListener(){
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }

                                        });
                        AlertDialog alert = builder.create();
                        alert.show();

                       // Log.d("update", "update required" + result + versionName);
                    }

            }


            pdia.dismiss();
        }
    }


}
