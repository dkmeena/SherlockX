package com.example.dinesh.sherlockx;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Pattern;

public class SignIn extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {


    private static final int RC_SIGN_IN = 9001;

    private GoogleApiClient mGoogleApiClient;
    private TextView mStatusTextView;
    private ProgressDialog mProgressDialog;


    private String phonenum;
    private String name,email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);


        findViewById(R.id.sign_in_button).setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(gso.getScopeArray());
        // [END customize_button]
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if(result.isSuccess()){
              //  Log.d("aad", String.valueOf(result.getSignInAccount().getDisplayName()));
                Toast.makeText(this,result.getSignInAccount().getDisplayName(),Toast.LENGTH_SHORT).show();
                HandleSignIn(result);
                //finish();
            }
            else{
                Toast.makeText(this,"Network Error -- Check your internet connection",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void HandleSignIn(GoogleSignInResult result) {

        name = result.getSignInAccount().getDisplayName();
        email = result.getSignInAccount().getEmail();

        promptforphone();

    }

    private void promptforphone() {

        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.activity_phone_number, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        alertDialogBuilder.setPositiveButton("OK", null);
        alertDialogBuilder.setNegativeButton("Cancel", null);
        alertDialogBuilder.setMessage(" Enter Your Phone Number ");


        final AlertDialog mAlertDialog = alertDialogBuilder.create();
        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {

                Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {


                        if (userInput.getText() != null && Pattern.matches("[0-9]{10}", (userInput.getText().toString()))) {

                            phonenum = userInput.getText().toString();
                            new phone().execute("http://safestreet.cse.iitb.ac.in/findmytrain/sherlock_server/phone.php");
                             dialog.cancel();
                        } else {
                            Log.d("d","fsfssc");
                            userInput.setError(" Invalid Mobile Number ");

                        }

                    }
                });
            }
        });
        mAlertDialog.show();


    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.sign_in_button){
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
            mGoogleApiClient.clearDefaultAccountAndReconnect();

        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    class  phone extends AsyncTask<String,String,String> {


        private ProgressDialog pdia;

        @Override
        protected String doInBackground(String... params) {


            String returnString="--";



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
                    dos.writeBytes("Content-Disposition: form-data; name=\"phone_email\"" + lineEnd);

                    dos.writeBytes(lineEnd);
                    dos.writeBytes(email+" - " +phonenum);

                    dos.writeBytes(lineEnd);

                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    int serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn.getResponseMessage();

                    Log.i("uploadFile", "HTTP Response is : "
                            + serverResponseMessage + ": " + serverResponseCode);


                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    returnString = in.readLine();

                } catch (Exception e) {
                    if(e.toString().contains("failed to connect") || e.toString().contains("SocketTimeout")){
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(SignIn.this, "Unable to Connect -- check your internet connection", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }


            return returnString;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pdia = new ProgressDialog(SignIn.this);
            pdia.setMessage("Processing ...");
            pdia.show();
        }
        @Override
        protected void onPostExecute(String result) {
            result=result.trim();
            if(!result.equals("--")){
                Log.d("asa","asda -- "+result);

                if(result!=null && result.equals("Success")){

                    Log.d("ASA", "PHONE ADDED");

                    SharedPreferences details = getSharedPreferences("details", MODE_PRIVATE);
                    SharedPreferences.Editor edit = details.edit();
                    edit.clear();
                    edit.putString("phone", phonenum);
                    edit.putBoolean("islogged", true);
                    edit.putString("name", name);
                    edit.putString("email", email);
                    edit.putLong("lastupdate", System.currentTimeMillis());
                    edit.commit();

                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);


                }

                else{

                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(SignIn.this, " Something Wrong with the server -- please try again  ", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }

            else{

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(SignIn.this, " Network Error -- Check your Internet Connection and try again ", Toast.LENGTH_SHORT).show();
                    }
                });

            }


            pdia.dismiss();
        }
    }

}