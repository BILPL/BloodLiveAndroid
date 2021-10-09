package com.blood.live;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.microsoft.windowsazure.messaging.notificationhubs.NotificationHub;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;
import static android.view.ViewConfiguration.*;
import static com.blood.live.Utility.GetUTCDateTimeWithFormat;

public class HomeActivity extends Activity {

    public static HomeActivity homeActivity;
    /// <summary>
    ///
    /// </summary>
    WebView webView;


    ConstraintLayout relativeLayout;

    ArrayList mobileArray;

    String notificationUrl;

    String deepLinkURL;

    String deviceId;

    ArrayList<User> listOfContacts = new ArrayList<>();

    /// <summary>
    ///
    /// </summary>
    public ValueCallback mUploadMessage;

    public static final int REQUEST_READ_CONTACTS = 79;

    public static final int REQUEST_FINE_LOCATION = 72;

    public DatabaseHelper mydb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        onNewIntent(getIntent());
        homeActivity = this;
        mydb = new DatabaseHelper(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
        } else {
            requestLocationPermission();
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            Utility.SyncContactsToServer(this, mobileArray);
        } else {
            requestPermission();
        }

        String regId =Utility.GetUserPreference(Constants.registrationID, this);
        String userId = Utility.GetUserPreference(Constants.UseruniqueId, MainActivity.mainActivity);
        if(Utility.IsNullOrEmpty(regId) && !Utility.IsNullOrEmpty(userId)){
            //registerWithNotificationHubs();
            NotificationHub.setListener(new CustomNotificationListener());
            //Here we are registering the notification with server
            NotificationHub.start(this.getApplication(),NotificationSettings.HubName, NotificationSettings.HubListenConnectionString);

            NotificationHub.setInstallationSavedListener(i -> {
                Toast.makeText(this, "SUCCESS", Toast.LENGTH_LONG).show();
                String regID = NotificationHub.getInstallationId();
                Utility.SetUserPreferences(Constants.registrationID, regID, this);
            });
            NotificationHub.setInstallationSaveFailureListener(e -> Toast.makeText(this,e.getMessage(), Toast.LENGTH_LONG).show());
            NotificationHub.addTag(userId);
        }
        deviceId = Utility.GetDeviceUniqueId(this);
        String notificationUrl = Utility.GetUserPreference(Constants.NOTIFICATIONMSGPREF, this);
        String url = !Utility.IsNullOrEmpty(notificationUrl) ? notificationUrl : !Utility.IsNullOrEmpty(deepLinkURL) ? deepLinkURL : Constants.BaseUrl;
        relativeLayout=findViewById(R.id.frame);

        PopulateWebview(url);


    }
    public void RefreshView(WebView view){
        try{
                view.reload();
        }catch (Exception ex){
            Log.e("RefreshView: ", ex.getMessage());
        }
    }
    public  void PopulateWebview( String url){
        webView = (WebView) this.findViewById(R.id.webView);

        final View getDecorView = this.getWindow().getDecorView();
        getDecorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
                int height = relativeLayout.getContext().getResources().getDisplayMetrics().heightPixels;
                int diff = height - r.bottom;
                if (diff > 0) {
                    if (relativeLayout.getPaddingBottom() != diff) {
                        relativeLayout.setPadding(0, 0, 0, diff);
                    }
                    boolean hasSoftKey = get(homeActivity).hasPermanentMenuKey();
                    if(hasSoftKey){
                        relativeLayout.setPadding(0, 0, 0, diff + 90);
                    }
                }
                else if(diff < 0){
                    int bottomPad = 0;
                    relativeLayout.setPadding(0, 0, 0, bottomPad);
                }
                else {
                    if (relativeLayout.getPaddingBottom() != 0) {
                        relativeLayout.setPadding(0, 0, 0, 0);
                    }
                }
                getDecorView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        webView.setVerticalScrollBarEnabled(true);
        WebView.setWebContentsDebuggingEnabled(true);
        webView.setWebViewClient(new Utility.CustomWebViewClient(this, null,null));
        webView.setWebChromeClient(new Utility.CustomWebChromeClient(this));


        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setUserAgentString("Mozilla/5.0 (Linux; U; Android 2.2.1; en-us; Nexus One Build/FRG83) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        } else {
            android.webkit.CookieManager.getInstance().setAcceptCookie(true);
        }

        try {
            String encryptValue = CryptoUtils.getencryptedString(GetUTCDateTimeWithFormat());
            url = url + "?ct=Android&token=" + encryptValue;
        } catch (Exception e) {
            e.printStackTrace();
        }
        webView.loadUrl(url);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String deepString = intent.getDataString();
        if(!Utility.IsNullOrEmpty(deepString)){
            deepLinkURL = deepString;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            if (null != this.mUploadMessage) {
                mUploadMessage.onReceiveValue(null);
                mUploadMessage = null;
                return;
            }
        }
        if (null == this.mUploadMessage) {
            return;
        }
        if (requestCode != 1) {
            //
        }
        Object result = data == null || resultCode != RESULT_OK ? null : data.getDataString();
        if (resultCode == RESULT_OK) {

            if (data.getClipData() != null) {
                Uri[] resultURI = new Uri[data.getClipData().getItemCount()];
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    resultURI[i] = Uri.parse(data.getClipData().getItemAt(i).getUri().toString());
                }
                result = resultURI;
            } else {
                result = new Uri[]{Uri.parse(data.getDataString())};
            }
        }

        mUploadMessage.onReceiveValue(result);
        mUploadMessage = null;
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()){
            webView.goBack();
        }
        else
        {
            if(!Utility.IsNullOrEmpty(deepLinkURL)){
                PopulateWebview(Constants.BaseUrl);
                deepLinkURL = "";
                return;
            }
            super.onBackPressed();
            getIntent().removeExtra(Constants.NOTIFICATIONMSG);

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getIntent().removeExtra(Constants.NOTIFICATIONMSG);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Utility.SyncContactsToServer(this, null);
                } else {
                    // permission denied,Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case REQUEST_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // permission denied,Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    public void requestPermission() {
        try {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_CONTACTS)) {
                // show UI part if you want here to show some rationale !!!
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS},
                        REQUEST_READ_CONTACTS);
            }
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_CONTACTS)) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                    this.requestPermissions(new String[]{
                            Manifest.permission.READ_CONTACTS,

                    }, REQUEST_READ_CONTACTS);
                }
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS},
                        REQUEST_READ_CONTACTS);
            }
        } catch (Exception ex) {
            Log.e("requestPermission: ", ex.getMessage());
        }

    }

    private void requestLocationPermission() {
        try {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // show UI part if you want here to show some rationale !!!
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_FINE_LOCATION);
            }
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                    this.requestPermissions(new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,

                    }, REQUEST_FINE_LOCATION);
                }
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_FINE_LOCATION);
            }
        } catch (Exception ex) {
            Log.e("requestPermission: ", ex.getMessage());
        }

    }
    public void ToastNotify(final String notificationMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(HomeActivity.homeActivity, notificationMessage, Toast.LENGTH_LONG).show();
                //TextView helloText = (TextView) findViewById(R.id.text_hello);
                // helloText.setText(notificationMessage);
            }
        });
    }

    public ArrayList ReadAndContacts() {
        ArrayList<String> nameList = new ArrayList<>();
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ContentResolver cr = getContentResolver();
                        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                        if ((cur != null ? cur.getCount() : 0) > 0) {
                            while (cur != null && cur.moveToNext()) {
                                User contact = new User();
                                String id = cur.getString(
                                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                                String name = cur.getString(cur.getColumnIndex(
                                        ContactsContract.Contacts.DISPLAY_NAME));
                                contact.Name = name;
                                if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                                    Cursor pCur = cr.query(
                                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                            null,
                                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                            new String[]{id}, null);
                                    while (pCur.moveToNext()) {
                                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                                ContactsContract.CommonDataKinds.Phone.NUMBER));

                                        if (Utility.IsNullOrEmpty(phoneNo)) {
                                            continue;
                                        }

                                        if (phoneNo.contains("*") || phoneNo.contains("#")) {
                                            continue;
                                        }

                                        phoneNo = phoneNo.trim().replace(" ", Constants.EMPTY_STRING).replace("+", Constants.EMPTY_STRING).replace("(", Constants.EMPTY_STRING).replace(")", Constants.EMPTY_STRING).trim();
                                        if (phoneNo.length() < 10) {
                                            continue;
                                        }

                                        if (phoneNo.length() == 10) {
                                            phoneNo = phoneNo;
                                        }
                                        contact.MobileNumber = phoneNo;
                                        contact.F5 = "ANDROID";
                                        contact.F6 = deviceId;
                                    }
                                    pCur.close();
                                }
                                if (!Utility.IsNullOrEmpty(contact.MobileNumber)) {
                                    listOfContacts.add(contact);
                                    mydb.addContact(contact);
                                }
                            }
                        }else{
                            Log.d(TAG, "run: Naveen closed");
                        }
                        if (cur != null) {
                            cur.close();
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "run: " + ex.getMessage());
                    }
                }

            }).start();
        } catch (Exception ex) {
            Log.e("getAllContacts: ", ex.getMessage());
        }

        return listOfContacts;
    }
}