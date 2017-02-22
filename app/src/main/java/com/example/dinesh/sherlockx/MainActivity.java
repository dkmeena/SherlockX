package com.example.dinesh.sherlockx;

import android.Manifest;

import android.app.AlertDialog;
import android.app.KeyguardManager;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,SensorEventListener {


    public TextView username;
    public String email;
    public TextView currdis, totdis, currtime, tottime, syncstatus, gpsspeed, connstatus;

    public TextView gpsstatuslat,gpsstatuslon;

    public Button start,sync;
    public int flagstrt = 0, flagstop = 0;
    private LocationManager locationManagerNET;
    private LocationManager locationManagerGPS;
    public double gpslat, gpslon, gpsacc, netlat, netlon, netacc;
    public double bearing;
    public String cellid;
    public int rssi = 0;
    public int dbm = 99;
    public String operatorName;
    public String operatorName2;
    public String nettype;
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

    public static boolean ifopen=false;

    IntentFilter ifilter;
    Intent batteryStatus;
    float init_level= 0;
    float final_level=0;


    boolean nearstation=false;
    boolean accflag=false;
    public long acctime ;
    public FileWriter fout=null;
    public BufferedWriter out=null;
    public FileWriter fout_acc=null;
    public BufferedWriter out_acc=null;



    Double stationgps[]={
            19.128257, 72.928069,
            19.128815, 72.928195,
            19.129557, 72.928443,
            19.127481, 72.927857,
            19.126671, 72.927758,

            19.113150, 72.928319,
            19.112532, 72.928260,
            19.111979, 72.928211,
            19.111427, 72.928201,
            19.110925, 72.928131,

            19.086521, 72.909514,
            19.086176, 72.908956,
            19.085700, 72.908285,
            19.085365, 72.907663,
            19.084909, 72.906821,

            19.078805, 72.896555,
            19.079191, 72.897080,
            19.079558, 72.897552,
            19.079891, 72.897992,
            19.080289, 72.898511,

            19.064414, 72.878243,
            19.064698, 72.878924,
            19.065286, 72.879101,
            19.065428, 72.879900,
            19.066072, 72.880018,

            19.046555, 72.863284,
            19.047037, 72.863544,

            19.026616, 72.849602,
            19.027150, 72.849940,
            19.027469, 72.850213,
            19.027902, 72.850481,
            19.028383, 72.850782,

            19.009218, 72.837669,
            19.009903, 72.838051,
            19.008478, 72.837059,
            19.007844, 72.836667,
            19.010370, 72.838336,

            18.994093, 72.832918,
            18.994950, 72.832928,
            18.993627, 72.832853,
            18.993104, 72.832885,
            18.992440, 72.832902,

            18.976611, 72.832729,
            18.977210, 72.832713,
            18.976008, 72.832659,
            18.975221, 72.832740,
            18.977895, 72.832708,

            18.952149, 72.838310,
            18.952783, 72.838202,
            18.951398, 72.838278,
            18.953560, 72.838256,
            18.950683, 72.838320,
            //
            19.439547, 73.307914,
            19.438836, 73.307555,
            19.440333, 73.308271,

  //          Vasind
            19.406632, 73.267605,
            19.406176, 73.266960,
            19.405595, 73.266185,
            19.407343, 73.268486,

//            Khadavali
            19.356798, 73.218951,
            19.357121, 73.219964,
            19.356511, 73.218096,
            19.356249, 73.217373,
            19.357368, 73.220588,


//            Titwala
            19.296863, 73.203305,
            19.297520, 73.203515,
            19.296255, 73.203084,
            19.295519, 73.202866,

           // Ambivalli
            19.267802, 73.171780,
            19.267119, 73.171364,
            19.268442, 73.172179,
            19.269179, 73.172880,

         //   Shahad
            19.244364, 73.158364,
            19.243736, 73.158030,
            19.245046, 73.158667,
            19.243235, 73.157690,
            19.245555, 73.158887,

            //Kalyan
            19.235420, 73.130976,
            19.236047, 73.131516,
            19.235928, 73.132321,
            19.235287, 73.129669,
            19.235038, 73.130145,

            //Thakurli
            19.225988, 73.097955,
            19.225652, 73.097204,
            19.226346, 73.098891,


            //Dombivali
            19.218298, 73.086807,
            19.217618, 73.085975,
            19.218933, 73.087697,

            //Kopar
            19.210744, 73.076940,
            19.210279, 73.076403,
            19.211266, 73.077681,
            19.211855, 73.078471,

          //  Diva
            19.188420, 73.041522,
            19.188518, 73.041765,
            19.188632, 73.042010,

        //    Mumbra
            19.190273, 73.023123,
            19.190960, 73.023073,
            19.189365, 73.023348,


      //      Kalwa
            19.195287, 72.996686,
            19.195870, 72.997392,
            19.196504, 72.998160,

    //        Thane
            19.186451, 72.975497,

  //          Mulund
            19.171849, 72.956456,
            19.171472, 72.956047,
            19.172461, 72.957040,

//            Nahur
            19.154635, 72.946618,
            19.155238, 72.946917,
            19.155842, 72.947248,
            19.154238, 72.946362,
            19.153639, 72.946046,

            //Bhandup
            19.142332, 72.937632,
            19.142936, 72.938175,
            19.143627, 72.938745,
            19.141957, 72.937342,
            19.141343, 72.936959,

            //Western line

            //Churchgate
            18.935297, 72.827158,
            18.934815, 72.827287,
            18.934115, 72.827389,
            18.935799, 72.827142,
            18.936368, 72.827077,


//            Marine Lines
            18.945749, 72.823815,
            18.945318, 72.824036,
            18.946167, 72.823485,
            18.946792, 72.823056,

  //          Charni Road
            18.951544, 72.818762,
            18.951262, 72.819129,
            18.950910, 72.819553,
            18.951874, 72.818287,
            18.952239, 72.817791,

    //        Grant Road
            18.963693, 72.816265,
            18.963144, 72.815969,
            18.962504, 72.815707,
            18.964296, 72.816450,
            18.964905, 72.816759,

      //      Grant Road
            18.970751, 72.819355,
            18.969944, 72.819373,
            18.971742, 72.819532,
            18.970957, 72.818797,
            18.971661, 72.819032,

        //    Maha laxmi
            18.982049, 72.823837,
            18.981340, 72.823196,
            18.983251, 72.824549,

          //  Lower Parel
            18.995660, 72.830291,
            18.994846, 72.829925,
            18.996616, 72.830679,

            //Elphinston Road
            19.007414, 72.835924,
            19.006654, 72.835362,
            19.008301, 72.836415,

            //Dadar
            19.018757, 72.842616,
            19.019515, 72.843059,
            19.017866, 72.842051,

            19.018086, 72.843672,
            19.017178, 72.842905,
            19.018935, 72.844277,

            //Matunga Road
            19.028384, 72.846740,
            19.027812, 72.846618,
            19.027120, 72.846429,
            19.029026, 72.846999,
            19.029735, 72.847192,

           // Mahim
            19.040668, 72.846982,
            19.039648, 72.847297,
            19.041496, 72.846570,

            //Bandra
            19.054803, 72.840716,
            19.054041, 72.840931,
            19.053225, 72.841242,
            19.055619, 72.840357,
            19.056502, 72.839988,

           // Khar Road
            19.069781, 72.840315,
            19.068957, 72.840135,
            19.067984, 72.839986,
            19.070697, 72.840372,

            //Santa Cruz
            19.082497, 72.841731,
            19.081697, 72.841574,

           // Ville parle
            19.099369, 72.843921,
            19.098514, 72.843872,
            19.100248, 72.844023,
            19.101272, 72.844175,

           // Andheri
            19.119829, 72.846531,
            19.118987, 72.846380,
            19.120676, 72.846604,

            //Jogeshwari
            19.136475, 72.848929,
            19.135569, 72.848804,
            19.137406, 72.849038,

            //Goregaon
            19.164742, 72.849622,
            19.163802, 72.849680,
            19.165766, 72.849549,

           // Malad
            19.187191, 72.848954,

            //kandavali
            19.204464, 72.852038,

            //Borivali
            19.229379, 72.856971,
            19.228845, 72.856895,
            19.230010, 72.856918,

            //Dahisar
            19.249853, 72.859416,
            19.249073, 72.859397,
            19.250512, 72.859336,
            19.251307, 72.859294,

            //Mira Road
            19.280910, 72.856027,
            19.281608, 72.855883,
            19.280283, 72.856046,

            //Bhayandar
            19.311440, 72.852689,
            19.310669, 72.852689,
            19.309871, 72.852730,
            19.312309, 72.852583,
            19.313317, 72.852529,

            //Naigaon
            19.351281, 72.846454,
            19.352192, 72.845875,
            19.353037, 72.845414,

            //Vasai Road
            19.382434, 72.832216,
            19.383487, 72.831685,
            19.381397, 72.832575,
            19.380157, 72.833133,
            19.384241, 72.831164,

            //Nalasapora
            19.417697, 72.818855,
            19.419235, 72.818652,
            19.416563, 72.819118,

            //Virar
            19.455004, 72.812034,
            19.454255, 72.812195,
            19.453430, 72.812448,
            19.455691, 72.812061,
            19.456405, 72.811879,

            //Vaitarna
            19.518091, 72.850170,

            //saphale
            19.576794, 72.821972,

            //Kelve road
            19.624768, 72.791100,

            //Phalgar
            19.697858, 72.772304,
            19.696853, 72.772492,
            19.699146, 72.771918,

            //Umroli
            19.755729, 72.760540,

            //Boisor
            19.798330, 72.761686,
            19.799922, 72.761726,
            19.796807, 72.761585,

            //Vangaon
            19.883068, 72.763315,

            //Dhanu road
            19.991153, 72.743748,
            19.992100, 72.743499,

//            Harbour

//            cst
            18.939885, 72.835354,
            18.940520, 72.835323,
            18.941172, 72.835255,
            18.941983, 72.835188,
            18.942448, 72.835318,
            18.940315, 72.835354,

//            Masjid bunder
            18.951945, 72.838273,
            18.951490, 72.838300,
            18.952329, 72.838281,

//            Sundarast road
            18.961531, 72.839978,
            18.961083, 72.839659,
            18.961921, 72.840372,

//            Dockyard road
            18.966426, 72.844206,
            18.965773, 72.843952,
            18.966967, 72.844327,

//            Reay road
            18.977301, 72.844302,
            18.976734, 72.844419,
            18.977917, 72.844128,

//            Cotton green
            18.986533, 72.843264,
            18.986028, 72.842976,
            18.986988, 72.843711,

//            Sewri
            18.998918, 72.854547,
            18.998230, 72.854266,
            18.997485, 72.853941,
            18.999534, 72.854812,

//            Vadala
            19.016435, 72.858940,
            19.016062, 72.858633,
            19.015686, 72.858512,
            19.016725, 72.859372,

//            Guru teg bahadur
            19.038027, 72.864337,
            19.037601, 72.863980,
            19.037160, 72.863586,
            19.038400, 72.864678,

//            Chunabhatti
            19.051693, 72.868984,
            19.051013, 72.869049,
            19.050386, 72.869079,
            19.052295, 72.869011,
            19.052957, 72.869079,

//            Kurla

//            Tilak nagar
            19.065860, 72.889963,
            19.065703, 72.890577,
            19.065502, 72.891274,
            19.066046, 72.889122,

//            Chembur
            19.062596, 72.901207,
            19.062872, 72.900563,
            19.062314, 72.901802,
            19.062056, 72.902378,

//            Govandi
            19.055165, 72.915460,
            19.054761, 72.916115,
            19.055588, 72.914816,

//            Mankhurd
            19.048055, 72.931712,
            19.048076, 72.931560,
            19.048073, 72.931827,

//            Vashi
            19.063095, 72.998904,
            19.063122, 72.998185,
            19.062553, 72.998062,
            19.063637, 72.999206,
            19.063330, 72.999797,

//            Sanpada
            19.066146, 73.009356,
            19.066064, 73.008612,
            19.065530, 73.008539,
            19.066405, 73.010008,
            19.065924, 73.010536,

//            Juinagar
            19.055844, 73.018207,
            19.055252, 73.018227,
            19.056318, 73.017897,
            19.056680, 73.018540,

//            Nerul
            19.033547, 73.018133,
            19.033980, 73.017929,
            19.034472, 73.018416,
            19.032901, 73.017842,
            19.032407, 73.018293,

//            Seawoods-darave
            19.021942, 73.019267,
            19.022400, 73.018780,
            19.022545, 73.018120,
            19.021342, 73.019541,
            19.021154, 73.020316,

//            Belapur cbd
            19.018999, 73.039179,
            19.019404, 73.039549,
            19.019351, 73.040234,
            19.018467, 73.038723,
            19.018462, 73.037971,

//            Khargpur
            19.026500, 73.059488,
            19.026148, 73.059434,
            19.026239, 73.059848,
            19.026259, 73.059430,

//            Mansarovar
            19.016766, 73.080536,
            19.016946, 73.079972,
            19.017335, 73.080026,
            19.016257, 73.080642,
            19.016174, 73.081136,

//            khandeshwar
            19.007496, 73.094763,
            19.007652, 73.094153,
            19.007337, 73.095404,
            19.007202, 73.096224,

//            Panvel
            18.990925, 73.120730,
            18.991454, 73.120392,
            18.991737, 73.119737,
            18.990438, 73.121105,
            18.990317, 73.121729

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ifopen = true;

        SharedPreferences details = getSharedPreferences("details",MODE_PRIVATE);

        // check for updates //

        // leave for now
       /* if(details.contains("lastupdate")) {
            Long lu = details.getLong("lastupdate",0);
            if(lu!=0 && System.currentTimeMillis() -lu > 2*DateUtils.DAY_IN_MILLIS);
            new version().execute("http://safestreet.cse.iitb.ac.in/findmytrain/sherlock_server/version.php");
        }*/

        // ----------------- //



        // Notify to sync data if lot of data to sync //

        double size=0.0;
        File dir1 = getExternalFilesDir(null);
        File file1[] = dir1.listFiles();


        for(File f:file1){
            /* bad means app crashed or closed unexpectedly
            if(f.getName().contains("bad")){
                f.delete();
                continue;
            }
            */
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
        ////////////
        gpsstatuslat = (TextView) findViewById(R.id.GPSstatuslat);
        gpsstatuslon = (TextView) findViewById(R.id.GPSstatuslon);
        ///////////
        connstatus = (TextView) findViewById(R.id.connstatus);

        checkconnection();

        File dir = getExternalFilesDir(null);
        File file[] = dir.listFiles();

        //////////
        /*
        if(file.length==0){
            syncstatus.setText( "Nothing to sync");
        }
        else{
            syncstatus.setText(file.length + " files to be synced");
        }
        */
        /////////

        syncstatus.setText(Integer.toString(file.length));

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
        }

        else if (v.getId() == R.id.start && start.getText().toString() == "STOP" ) {

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
                syncstatus.setText( "0");
                Toast.makeText(this,"Nothing to sync",Toast.LENGTH_SHORT).show();
                wait=0;
                return;
            }


            // comment sync_check -- sending limit
            // new sync_check().execute("http://safestreet.cse.iitb.ac.in/findmytrain/sherlock_server/data_synced.php");

            issyncgoing = 1;
            // Toast.makeText(MainActivity.this, " data sync started", Toast.LENGTH_SHORT).show();
            syncstart();
            wait=0;

        }
        else if(v.getId() == R.id.sync && issyncgoing==1){

            Toast.makeText(getApplicationContext(),"Wait for previous sync to complete",Toast.LENGTH_SHORT).show();

        }
    }


    ////////////////////
    /*
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
                Log.d("2",e.toString());

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

    */
    ///////////////

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
        //locationManagerGPS=null;
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
            Log.d("2",e.toString());
            e.printStackTrace();
        }

        Calendar c = Calendar.getInstance();
        int hr = c.get(Calendar.HOUR_OF_DAY);
        int mn = c.get(Calendar.MINUTE);
        int sec = c.get(Calendar.SECOND);
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy_MM_dd");
        String strDate = mdformat.format(c.getTime());
            fname = email +'-'+versionName+'-'+ strDate+'-'+hr+'_'+mn+'_'+sec+'-'+"0_0_0";
        // Log.d("dsda", fname);

        // saving file directly to zip //

        ///////////////////////////////////
       /*
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
            Log.d("4",e.toString());

            e.printStackTrace();

        }

        ////////////////////////////////////
        */
        // ------------------------------------------- //

        File root = new File(getExternalFilesDir(null).toString());
        if (!root.exists()) {
            root.mkdirs();
            //Log.d("as", "sds");
        }
        else{
            //Log.d("as", "sds");
        }
        //File file = new File(root,fname+".txt");
        File file = new File(root,fname+".txt");

        try {
            fout = new FileWriter(file,true);
        } catch (IOException e) {
            e.printStackTrace();
           // Log.d("as","sds");
        }

        out = new BufferedWriter(fout);



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

                        gpsstatuslat.setText("Latitude");
                        gpsstatuslon.setText("Longitude");

                        ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                        batteryStatus = registerReceiver(null, ifilter);

                        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                        final_level = level / (float) scale;
                        final_level=final_level*100;
                        // Log.d("final", String.valueOf(final_level));

                        removeupdates();

                        //sendtoserver();
                        Toast.makeText(MainActivity.this, " Data Collection Stopped ", Toast.LENGTH_SHORT).show();


                        writeToFile();
                        updatedatabase();

                        start.setText("START");

                        File dir = getExternalFilesDir(null);
                        File file[] = dir.listFiles();

                        //////////
                        /*
                        if(file.length==0){
                                syncstatus.setText( "Nothing to sync");
                            }
                            else{
                                syncstatus.setText(file.length + " files to be synced");
                            }
                        */
                        ////////

                        syncstatus.setText(Integer.toString(file.length));
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

        ////////////////
        nearstation=false;
        accflag=false;
        ///////////////

        // battery level //

        ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        init_level = level / (float)scale;
        init_level=init_level*100;
        //Log.d("final", String.valueOf(init_level));
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
        bearing=0.0; rssi=99; dbm=99; // rssi,dbm = 99 means unable to get signal strength
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



    }

    private void checkconnection() {

        // check if internet connected //

        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo != null && activeNetworkInfo.isConnected()){
            connstatus.setText("Yes");
        }
        else
            connstatus.setText("No");

        // --------------  //
    }


    LocationListener locationListenerNET = new LocationListener() {
        public void onLocationChanged(Location location) {

            // --- Stop taking acceleration data --- //

            if (senSensorManager!=null && accflag && ((curr_time-acc_time) >= 15 * 60 * 1000)) {  // acc. changed to 15 minutes

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



            // right now do not stop in 2 hours
           /* if(curr_time - starttime > 7200000){
                stop();
                return;
            }*/

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

            if(loc==null){
                cellid = "Not found";
            }
            else
                cellid = String.valueOf(loc.getCid() & 0xffff);

            // cellid = String.valueOf(loc.getCid() & 0xffff);
            operatorName = "None";
            operatorName2 = "None";
            nettype = "None";
            if(tm!=null)
            {
                if(!tm.getSimOperatorName().isEmpty())
                    operatorName = tm.getSimOperatorName();
                if(!tm.getNetworkOperatorName().isEmpty())
                    operatorName2 = tm.getSimOperatorName();
                nettype = String.valueOf(tm.getNetworkType());
            }



            netlat = location.getLatitude();
            netlon = location.getLongitude();
            netacc = location.getAccuracy();
            netacc = Math.round(netacc * 100);
            netacc = netacc / 100.0;


            // check if connected to network //
            String connect = "no";
            String type="null",subtype="null";

            boolean isConnected;

            if(cm!=null){

                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                isConnected = activeNetwork != null && activeNetwork.isConnected();
                if(isConnected){
                    connect = "yes";
                    if(activeNetwork.getTypeName()!=null)
                        type = activeNetwork.getTypeName();
                    if(activeNetwork.getSubtypeName()!=null)
                        subtype = activeNetwork.getSubtypeName();

                }

            }

            // ------------------------------ //

            sfile =  hr + "::" + mn + "::" + sec + " || " + gpslat + " || " + gpslon + " || " + gpsacc + " || " + netlat + " || " + netlon +
                    " || " + netacc + " || " + cellid + " || " + operatorName + " || " + operatorName2 + " || " + nettype + " || " + rssi + " || " + dbm + " || " + connect+ " || " + type + " || " + subtype +" || " +spd+" || "+bearing + " || " + mPDOP +" || "+mHDOP+" || "+mVDOP +" || "+wifiinfo+"\n";

            try {
                out.flush();
                out.append(sfile);
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


           /* if(curr_time - starttime > 7200000){
                stop();
                return;
            }*/

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

            GsmCellLocation loc = null;
            if(tm!=null)
                loc = (GsmCellLocation) tm.getCellLocation();

            if(loc==null){
                cellid = "Not found";
            }
            else
                cellid = String.valueOf(loc.getCid() & 0xffff);

            operatorName = "None";
            operatorName2 = "None";
            nettype = "None";
            if(tm!=null)
            {
                if(!tm.getSimOperatorName().isEmpty())
                    operatorName = tm.getSimOperatorName();
                if(!tm.getNetworkOperatorName().isEmpty())
                    operatorName2 = tm.getSimOperatorName();
                nettype = String.valueOf(tm.getNetworkType());
            }



            bearing = location.getBearing();
            gpslat = location.getLatitude();
            gpslon = location.getLongitude();
            gpsacc = location.getAccuracy();
            gpsacc = Math.round(gpsacc * 100);
            gpsacc = gpsacc / 100.0;

            gpsstatuslat.setText(Double.toString(gpslat));
            gpsstatuslon.setText(Double.toString(gpslon));

            spd = location.getSpeed();
            gpsspeed.setText((int) spd + " m/s");

            if(!accflag){

                if(spd<2 && dist(gpslat,gpslon)){

                    nearstation=true;
                    //accflag=true;
                    acctime = curr_time;

                }

                else if(nearstation && (curr_time-acctime)<5*1000*60 && spd>6){

                    if(senSensorManager!=null){

                        acc_time = curr_time;
                        ///////////
                        accflag=true;
                        ///////////

                        File root = new File(getExternalFilesDir(null).toString());
                        File file = new File(root,fname+"_acc.txt");
                        try {
                            fout_acc = new FileWriter(file,true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        out_acc = new BufferedWriter(fout_acc);

                        senSensorManager.registerListener(MainActivity.this, senAccelerometer , SensorManager.SENSOR_DELAY_GAME);

                    }


                }


            }



            // for acceleration //

           /* if(spd>=5 && acc_cnt==0 && senSensorManager!=null ){
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
                    Log.d("6",e.toString());
                    e.printStackTrace();

                }

                senSensorManager.registerListener(MainActivity.this, senAccelerometer , SensorManager.SENSOR_DELAY_GAME);
            }
          */


            // check if connected to network //

            String connect = "no";
            String type="null",subtype="null";
            boolean isConnected;

            if(cm!=null){
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                isConnected = activeNetwork != null && activeNetwork.isConnected();
                if(isConnected){
                    connect = "yes";
                    if(activeNetwork.getTypeName()!=null)
                        type = activeNetwork.getTypeName();
                    if(activeNetwork.getSubtypeName()!=null)
                        subtype = activeNetwork.getSubtypeName();
                }
            }
            // ------------------------------ //

            sfile =  hr + "::" + mn + "::" + sec + " || " + gpslat + " || " + gpslon + " || " + gpsacc + " || " + netlat + " || " + netlon +
                    " || " + netacc + " || " + cellid + " || " + operatorName + " || " + operatorName2 + " || " + nettype + " || " + rssi + " || " + dbm + " || " + connect+ " || " + type + " || " + subtype +" || " +spd+" || "+bearing + " || " + mPDOP +" || "+mHDOP+" || "+mVDOP +" || "+wifiinfo+"\n";


            try {
                out.flush();
                out.append(sfile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            /*
            try {
                //bufferedWriter.write(sfile);
                zos.write(sfile.getBytes());
            } catch (IOException e) {
                Log.d("7",e.toString());

                e.printStackTrace();
            }
            */
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

    private boolean dist(double gpslat, double gpslon) {

        //int min = 9999999;
        for(int i=0;i<stationgps.length;i=i+2){
            float[] res = new float[3];
            Location.distanceBetween(gpslat,gpslon,stationgps[i],stationgps[i+1],res);
            //mindist(gpslat,gpslon,stationgps[i],stationgps[i+1]);
            if(res[0]<=80)
                return true;
        }

        return false;
    }


    private class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);


            curr_time = System.currentTimeMillis();

           /* if(curr_time - starttime > 7200000){
                stop();
                return;
            }*/

            if (senSensorManager!=null && accflag && ((curr_time-acc_time) >= 15 * 60 * 1000)) {  // acc. changed to 15 minutes

                // Log.d("fdf","haha");
                senSensorManager.unregisterListener(MainActivity.this,senAccelerometer);
                senSensorManager=null;

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

                //////////
               /*
                if(file.length==0){
                    syncstatus.setText( "Nothing to sync");
                }
                else{
                    syncstatus.setText(file.length + " files to be synced");
                }
                */
                ////////
                syncstatus.setText(Integer.toString(file.length));
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

            // ---------------- //

           String ssignal = signalStrength.toString();
           String[] parts = ssignal.split(" ");
           //Log.d("asd",parts[8]);
           if(parts.length>=10){
               dbm = Integer.parseInt(parts[9]);
           }
            // ----------------//

            int val;
            val = -113 + 2 * signalStrength.getGsmSignalStrength();
            rssi = val;
            GsmCellLocation loc=null;

            if(tm!=null){
                loc = (GsmCellLocation) tm.getCellLocation();
            }

            if(loc==null){
                cellid = "Not found";
            }
            else
                cellid = String.valueOf(loc.getCid() & 0xffff);

            // cellid = String.valueOf(loc.getCid() & 0xffff);
            operatorName = "None";
            operatorName2 = "None";
            nettype = "None";
            if(tm!=null)
            {
                if(!tm.getSimOperatorName().isEmpty())
                    operatorName = tm.getSimOperatorName();
                if(!tm.getNetworkOperatorName().isEmpty())
                    operatorName2 = tm.getSimOperatorName();
                nettype = String.valueOf(tm.getNetworkType());
            }


            Calendar c = Calendar.getInstance();
            int hr = c.get(Calendar.HOUR_OF_DAY);
            int mn = c.get(Calendar.MINUTE);
            int sec = c.get(Calendar.SECOND);


            // check if connected to network //
            String connect = "no";
            String type="null",subtype="null";
            boolean isConnected;

            if(cm!=null){
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                isConnected = activeNetwork != null && activeNetwork.isConnected();
                if(isConnected){
                    connect = "yes";
                    if(activeNetwork.getTypeName()!=null)
                        type = activeNetwork.getTypeName();
                    if(activeNetwork.getSubtypeName()!=null)
                        subtype = activeNetwork.getSubtypeName();
                }
            }

            // ------------------------------ //

            sfile =  hr + "::" + mn + "::" + sec + " || " + gpslat + " || " + gpslon + " || " + gpsacc + " || " + netlat + " || " + netlon +
                    " || " + netacc + " || " + cellid + " || " + operatorName + " || " + operatorName2 + " || " + nettype + " || " + rssi + " || " + dbm + " || " + connect+ " || " + type + " || " + subtype +" || " +spd+" || "+bearing + " || " + mPDOP +" || "+mHDOP+" || "+mVDOP +" || "+wifiinfo+"\n";


            try {
                out.flush();
                out.append(sfile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            /*
            try {
                //bufferedWriter.write(sfile);
                zos.write(sfile.getBytes());
            } catch (IOException e) {
                Log.d("8",e.toString());

                e.printStackTrace();
            }*/

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

        // -------- //

            if (senSensorManager!=null && accflag && ((curr_time-acc_time) >= 15 * 60 * 1000)) {  // acc. changed to 15 minutes

                senSensorManager.unregisterListener(MainActivity.this,senAccelerometer);
                senSensorManager=null;

            }

        // --------- //
            try {
                out_acc.flush();
                out_acc.append(acc_s);
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

                        // ----------------- //
                        // Zipping the files before sending

                        String a = f.getName();

                        if(a.contains(".zip")){

                        }
                        else {

                            String zipname = f.getCanonicalPath() + ".zip";
                            String name = f.getName() + ".zip";

                            byte data[] = new byte[2048];
                            fos = new FileOutputStream(zipname);
                            zos = new ZipOutputStream(fos);
                            ze = new ZipEntry(f.getName());
                            zos.putNextEntry(ze);
                            FileInputStream fi = new FileInputStream(f.getCanonicalPath());
                            BufferedInputStream origin = null;
                            origin = new BufferedInputStream(fi, 2048);
                            int count;
                            while ((count = origin.read(data, 0, 2048)) != -1) {
                                zos.write(data, 0, count);
                            }
                            origin.close();
                            zos.close();

                            f.delete();
                            File fle = new File(getExternalFilesDir(null).toString());

                            f = new File(fle, name);
                            // ----------------- //
                        }
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
                                    syncstatus.setText(Integer.toString(x[0]));
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
                {Log.d("10",e.toString());

                    e.printStackTrace();
                    Log.d("Exception",e.toString());
                    if(e.toString().contains("failed to connect") || e.toString().contains("SocketTimeout") || e.toString().contains("Unable to resolve host")){
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(MainActivity.this, "Unable to Connect -- check your internet connection", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    Log.d("Exception","hihihi");
                    issyncgoing=0;
                }

            }
        }).start();

    }

    private void removeupdates() {
        if(senSensorManager!=null) {
            senSensorManager.unregisterListener( this);
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

        /*
        /////////////////////////

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
            // Toast.makeText(this,String.valueOf(power_consumed),Toast.LENGTH_SHORT).show();
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
            Log.d("11",e.toString());

            e.printStackTrace();
            return false;
        }
        */

        try{

            int power_consumed = Math.round(init_level-final_level);
            if(power_consumed<0) power_consumed = 0;

            File file = new File(getExternalFilesDir(null).toString());
            String fname2;
            fname2 = fname.split("-0_0_0")[0];

            out.flush();
            out.close();


            File f = new File(file, fname+".txt");
            f.renameTo(new File(file, fname2 + "-" + curr_dis + "_" + curr_currdistime + "_" + power_consumed + ".txt"));

            if(out_acc!=null){
                out_acc.flush();
                out_acc.close();

                f = new File(file, fname + "_acc.txt");
                f.renameTo(new File(file, fname2 + "-" + curr_dis + "_" + curr_currdistime + "_acc.txt"));

            }



            //Log.d("sad",fname);

            Toast.makeText(this, "file successfully saved locally ",Toast.LENGTH_SHORT).show();
            return true;


        } catch (IOException e) {
            e.printStackTrace();

        }

        return true;
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
                Log.d("12",e.toString());
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
                    Log.d("13",e.toString());

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
