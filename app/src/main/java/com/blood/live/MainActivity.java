package com.blood.live;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationHub;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    public static MainActivity mainActivity;
    public static Boolean isVisible = false;
    private static final String TAG = "MainActivity";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;
        //registerWithNotificationHubs();

       // FirebaseService.createChannelAndHandleNotifications(getApplicationContext());
        onNewIntent(getIntent());
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Intent intent=new Intent(MainActivity.this,
//                        HomeActivity.class);
//                //Intent is used to switch from one activity to another.
//
//                startActivity(intent);
//                //invoke the SecondActivity.
//
//                finish();
//                //the current activity will get finished.
//            }
//        }, 2000);

        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        this.finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String notification = intent.getStringExtra(Constants.NOTIFICATIONMSG);
        String url = Utility.IsNullOrEmpty(notification) ? Constants.EMPTY_STRING : notification;
        if(!Utility.IsNullOrEmpty(url)){
            getIntent().putExtra("consumed", true);
            Utility.SetUserPreferences(Constants.NOTIFICATIONMSGPREF, !Utility.IsNullOrEmpty(url) ? url + "/?clientType=Android" : Constants.EMPTY_STRING, this);
            getIntent().removeExtra(Constants.NOTIFICATIONMSG);
        }else
        {
            Utility.SetUserPreferences(Constants.NOTIFICATIONMSGPREF, "", this);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getIntent().removeExtra(Constants.NOTIFICATIONMSG);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getIntent().removeExtra(Constants.NOTIFICATIONMSG);
    }

    @Override
    protected void onStart() {
        super.onStart();
        isVisible = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isVisible = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isVisible = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isVisible = false;
    }

    public void ToastNotify(final String notificationMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(MainActivity.this, notificationMessage, Toast.LENGTH_LONG).show();
                //TextView helloText = (TextView) findViewById(R.id.text_hello);
               // helloText.setText(notificationMessage);
            }
        });
    }


    public void registerWithNotificationHubs()
    {
        if (checkPlayServices()) {
            // Start IntentService to register this application with FCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog box that enables  users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported by Google Play Services.");
                ToastNotify("This device is not supported by Google Play Services.");
                finish();
            }
            return false;
        }
        return true;
    }
}