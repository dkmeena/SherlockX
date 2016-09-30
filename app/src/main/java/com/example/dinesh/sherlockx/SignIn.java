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
import android.telephony.TelephonyManager;
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

public class SignIn extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {


    private static final int RC_SIGN_IN = 9001;

    private GoogleApiClient mGoogleApiClient;
    private TextView mStatusTextView;
    private ProgressDialog mProgressDialog;

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
        TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        String ph_number = tm.getLine1Number();
        SharedPreferences details = getSharedPreferences("details", MODE_PRIVATE);
        SharedPreferences.Editor edit = details.edit();
        edit.clear();
        edit.putBoolean("islogged", true);
        edit.putString("name", result.getSignInAccount().getDisplayName());
        edit.putString("email", result.getSignInAccount().getEmail());
        edit.putString("phone",ph_number);
        edit.putLong("lastupdate",System.currentTimeMillis());
        edit.commit();
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
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
