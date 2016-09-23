package com.example.dinesh.sherlockx;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Calendar;

public class SignInActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private GoogleApiClient mGoogleApiClient;
    private TextView mStatusTextView;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // for Notifications //

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

        Intent intent1 = new Intent(getApplicationContext(), NotificationReceiver.class);

        PendingIntent morning = PendingIntent.getBroadcast(getApplicationContext(), 100, intent1,PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent evening = PendingIntent.getBroadcast(getApplicationContext(), 101, intent1,PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager amNot = (AlarmManager) getSystemService(ALARM_SERVICE);

        amNot.setRepeating(AlarmManager.RTC_WAKEUP, cal1.getTimeInMillis(), DateUtils.DAY_IN_MILLIS, morning);
        amNot.setRepeating(AlarmManager.RTC_WAKEUP, cal2.getTimeInMillis(), DateUtils.DAY_IN_MILLIS, evening);
       //amNot.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() , 30*1000, pendingIntent);
        // -----------------//



        // Button listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);
//        findViewById(R.id.sign_out_button).setOnClickListener(this);
//        findViewById(R.id.disconnect_button).setOnClickListener(this);

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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // getIntent() should always return the most recent
        //setIntent(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            // handleSignInResult(result);
            if(result.isSuccess()){
                Log.d("aad", String.valueOf(result.getSignInAccount().getDisplayName()));
                Toast.makeText(this,result.getSignInAccount().getDisplayName(),Toast.LENGTH_SHORT).show();
                HandleSignIn(result);
                finish();
            }
            else{
                Toast.makeText(this,"Network Error -- Check your internet connection",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void HandleSignIn(GoogleSignInResult result) {

        String s = result.getSignInAccount().getDisplayName()+"-"+result.getSignInAccount().getEmail();
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        i.putExtra("Account Details",s);
        startActivity(i);
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
}
