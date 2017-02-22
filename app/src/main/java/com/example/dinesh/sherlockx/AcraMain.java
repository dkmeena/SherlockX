package com.example.dinesh.sherlockx;

/**
 * Created by dinesh on 1/2/17.
 */

import android.app.Application;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

@ReportsCrashes(
        formKey = "",
        httpMethod = HttpSender.Method.POST,
        formUri = "http://safestreet.cse.iitb.ac.in/findmytrain/sherlock_server/acra.php",
        customReportContent = {ReportField.PHONE_MODEL,ReportField.STACK_TRACE,ReportField.ANDROID_VERSION,
                ReportField.APP_VERSION_CODE,ReportField.APP_VERSION_NAME,ReportField.BRAND
        }
)
public class AcraMain extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ACRA.init(this);
    }

}
