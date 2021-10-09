package com.blood.live;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.google.gson.Gson;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static android.content.ContentValues.TAG;

public class Utility {
    public static boolean IsNullOrEmpty(String str) {
        Boolean value = false;
        try {
            if (null == str || str.length() == 0 || str == "" || str.isEmpty()) {
                return true;
            }

        } catch (Exception ex) {

        }
        return value;
    }

    static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String GetUTCDateTimeWithFormat() {
        try {
            final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            final String utcTime = sdf.format(new Date());

            return utcTime;
        } catch (Exception ex) {
            Log.e("GetUTCDateTimeWith: ", ex.getMessage());
            throw ex;
        }
    }

    public static Date ConvertStringToDate(String StrDate) {
        Date dateToReturn = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        try {
            dateToReturn = (Date) dateFormat.parse(StrDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateToReturn;
    }

    /// <summary>
    /// Sets the given value in user preferences using given key.
    /// </summary>
    /// <returns><c>true</c>, if user preferences was set, <c>false</c> otherwise.</returns>
    /// <param name="key">Preference key.</param>
    /// <param name="value">Preference value.</param>
    public static boolean SetUserPreferences(String key, String value, Context context) {
        try {
            SharedPreferences sprefs = PreferenceManager.getDefaultSharedPreferences(context);
            sprefs.edit().putString(key, value).commit();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }


    /// <summary>
    /// This method for Get the stores the data from shared preferences based on key value
    /// </summary>
    /// <param name="key">store key value</param>
    /// <returns>it return's stored string</returns>
    public static String GetUserPreference(String key, Context context) {
        try {
            SharedPreferences sprefs = PreferenceManager.getDefaultSharedPreferences(context);

            return sprefs.getString(key, Constants.EMPTY_STRING);
        } catch (Exception ex) {
            return null;
        }
    }

    /// <summary>
    /// Gets device unique id.
    /// </summary>
    /// <param name="context">Activity context.</param>
    /// <returns>Device unique id.</returns>
    public static String GetDeviceUniqueId(Context context) {
        String value = android.provider.Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return value;
    }

    public static void RegisterOrUpdateDeviceToken(String userId, Context context) {
        final Boolean result = false;
        if (Utility.IsNullOrEmpty(userId))
            return;
        String deviceUniqueId = GetDeviceUniqueId(context);
        String deviceToken = GetUserPreference(Constants.DeviceToken, context);
        final String endPoint = "https://www.blood.live/" + "/api/UnsecuredAPI/RegisterOrUpdateDeviceToken?personId=" + userId + "&deviceType=ANDROID" + "&deviceUniqueId=" + deviceUniqueId + "&deviceToken=" + deviceToken;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    HttpURLConnection connection = null;
                    URL url = new URL(endPoint);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(true);
                    connection.setRequestMethod("GET"); // hear you are telling that it is a POST request, which can be changed into "PUT", "GET", "DELETE" etc.
                    connection.setRequestProperty("Content-Type", "application/json"); // here you are setting the `Content-Type` for the data you are sending which is `application/json`"application/octet-stream"
                    connection.connect();

                    int response = connection.getResponseCode();
                    if (response >= 200 && response <= 399) {

                    } else {

                    }

                } catch (Exception ex) {
                    Log.e(TAG, "RegisterOrUpdateDeviceToken: ");
                }
            }

        }).start();
    }

    public static Boolean isContactServiceCallGoingOn = false;

    public static void SyncContactsToServer(final Activity activity, ArrayList<User> contacts) {
        try {
            String lastSyncDate = Utility.GetUserPreference(Constants.ContactsLastSyncDateTime, activity);
            if (!IsNullOrEmpty(lastSyncDate)) {
                long differenceinTime = (ConvertStringToDate(GetUTCDateTimeWithFormat()).getTime() - Utility.ConvertStringToDate(lastSyncDate).getTime()) / (1000 * 60 * 60) % 24;
                Log.d(TAG, "SyncContactsToServer: " + differenceinTime);
//                if (differenceinTime < 2)
//                    return;
            }

            HomeActivity.homeActivity.ReadAndContacts();
            final List<User> finalcontacts = HomeActivity.homeActivity.mydb.getAllContacts();//new ArrayList<User>();

            if (finalcontacts == null || finalcontacts.size() <= 0) {
                return;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String SYNCID = Utility.GetUserPreference(Constants.UseruniqueId, activity);//"0C5E0AAB-DBA6-4A35-AD0C-437DFE50C05B"; //
                        if (Utility.IsNullOrEmpty(SYNCID))
                            return;
                        String endPoint = "https://www.blood.live/api/UnsecuredApi/SyncContacts?userId=" + SYNCID;

                        HttpURLConnection connection = null;
                        URL url = new URL(endPoint);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setDoOutput(true);
                        connection.setRequestMethod("POST"); // hear you are telling that it is a POST request, which can be changed into "PUT", "GET", "DELETE" etc.
                        connection.setRequestProperty("Content-Type", "application/json"); // here you are setting the `Content-Type` for the data you are sending which is `application/json`"application/octet-stream"
                        Gson gson = new Gson();
                        String content = gson.toJson(finalcontacts);

                        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                        wr.write(content.getBytes());
                        connection.connect();
                        InputStream is;
                        String resp = connection.getResponseMessage();
                        int response = connection.getResponseCode();
                        if (response >= 200 && response <= 399) {
                            Utility.SetUserPreferences(Constants.ContactsLastSyncDateTime, GetUTCDateTimeWithFormat(), activity);
                            wr.flush();
                            wr.close();
                            //return is = connection.getInputStream();
                            return;
                        } else {
                            wr.flush();
                            wr.close();
                            //return is = connection.getErrorStream();
                            return;
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "run: " + ex.getMessage());
                    } finally {
                        isContactServiceCallGoingOn = false;
                    }
                }

            }).start();
        } catch (Exception ex) {
            Log.e("SyncContactsToServer: ", ex.getMessage());
        }
    }

    /// <summary>
    /// This method used to open web url
    /// </summary>
    /// <param name="context"></param>
    /// <param name="url"></param>
    public static void OpenChrome(Activity context, String url) {
        try {
            if (IsNullOrEmpty(url))
                return;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.setPackage("com.android.chrome");
            context.startActivity(intent);

        } catch (Exception ex) {
            Log.e(TAG, "OpenURL: ");
        }
    }
    /// <summary>
    /// This method used to open web url
    /// </summary>
    /// <param name="context"></param>
    /// <param name="url"></param>
    public static void OpenURL(Activity context, String url) {
        try {
            if (IsNullOrEmpty(url))
                return;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);

        } catch (Exception ex) {
            Log.e(TAG, "OpenURL: ");
        }
    }

    //*****************START :: eMail Fetch :: Nawin******************
    static String getEmail(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account account = getAccount(accountManager);

        if (account == null) {
            return null;
        } else {
            return account.name;
        }
    }

    private static Account getAccount(AccountManager accountManager) {
        Account[] accounts = accountManager.getAccountsByType("com.google");
        Account account;
        if (accounts.length > 0) {
            account = accounts[0];
        } else {
            account = null;
        }
        return account;
    }


    //*****************END:: eMail Fetch :: Nawin******************
    public static void CheckMediaPermissions(Activity activity, int read_phone_State) {
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                activity.requestPermissions(new String[]{
                        // Android.Manifest.Permission.Camera ,
                        android.Manifest.permission.MANAGE_DOCUMENTS,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,

                }, read_phone_State);
            }
        } catch (Exception ex) {
            Log.e("CheckMediaPermissions: ", ex.getMessage());
        }
    }

    public static void ShareData(Activity act, String shareData){
        try{
            String shareText = shareData.substring(1, shareData.length() - 1).replace("\\n", "\r\n");
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            share.putExtra(Intent.EXTRA_TEXT, shareText);
            Intent receiver = new Intent(act, ApplicationSelectorReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(act, 0, receiver, PendingIntent.FLAG_UPDATE_CURRENT);
            Intent chooser = Intent.createChooser(share, null, pendingIntent.getIntentSender());
            //startActivity(chooser);
            act.startActivity(chooser);
        }catch(Exception ex){
            Log.e(TAG, "ShareData: " + ex.getMessage() );
        }
    }

    public static void ShareonWhatsapp(Activity activity, String title, File file) {
        try {
            String imgPath = GetImagePathStored(activity, title);
            if (IsNullOrEmpty(imgPath))
                return;
            Intent intent = new Intent(Intent.ACTION_SEND);
            Uri uri = null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //File file = GetFileStored(activity, json, "");
                uri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileprovider", file);
            } else {
                uri = Uri.parse(imgPath);
            }
            intent.setType("text/plain");
            intent.setPackage("com.whatsapp");
            intent.putExtra(Intent.EXTRA_TEXT, title);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            try {
                activity.startActivity(intent);
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(activity, "Whatsapp have not been installed.", Toast.LENGTH_LONG);
            }

        } catch (Exception ex) {
            Log.e("ShareonWhatsapp: ", ex.getMessage());
        }
    }

    public static String GetImagePathStored(Activity activity, String json) {
        String imgPath = Constants.EMPTY_STRING;
        try {
            String direct64 = json.replace("data:image/jpeg;base64,", Constants.EMPTY_STRING);

            byte[] decodedString = Base64.decode(direct64, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/BLMedia"); //Creates app specific folder

            if (!path.exists()) {
                path.mkdirs();
            }
            imgPath = path.getAbsolutePath();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, java.lang.System.currentTimeMillis());
            values.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.MediaColumns.DATA, imgPath);
            activity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            File file = new File(imgPath, "Thumbnail.jpg");
            if (file.exists())
                file.delete();

            FileOutputStream fos = new FileOutputStream(file);
            decodedByte.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

        } catch (Exception ex) {
            Log.e("GetImagePathStored: ", ex.getMessage());
        }
        return imgPath;
    }

    public static File saveTempBitmap(Bitmap bitmap, String type) {
        File file = null;
        if (isExternalStorageWritable()) {
            file = saveImage(bitmap);
        } else {
            //prompt the user or do something
        }
        return file;
    }

    private static File saveImage(Bitmap finalBitmap) {
        File file = null;
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/BLMedia"); //Creates app specific folder

        if (!path.exists()) {
            path.mkdirs();
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fname = timeStamp + ".jpg";
        String imagePath = path.getAbsolutePath();
        file = new File(imagePath, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    private static void ReloadView(WebView view, Activity act){
        try{
            final WebView webView = view;


        }catch (Exception ex){
            Log.e(TAG, "ReloadView: "+ ex.getMessage() );
        }
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static void CloseDialog(DialogInterface dialog, WebView webview) {
        try {
            dialog.dismiss();
            dialog = null;
            webview.destroy();
        } catch (Exception ex) {
            Log.e(TAG, "CloseDialog: ");
        }
    }


    public static class CustomWebViewClient extends WebViewClient {

        Activity context;
        WebView oldWeb;
        Dialog dialog;
        Uri uri;

        public CustomWebViewClient(Activity context, WebView oldWebView, Dialog dialog) {
            this.context = context;
            this.oldWeb = oldWebView;
            this.dialog = dialog;
        }


        Intent intent = null;
        String encryptValue = "";
        String eMail = "";
        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest request) {
            try {
                String url = request.getUrl().toString();
                Intent intent = null;
                if (url.toLowerCase().contains("openinweb")) {
                    url = url.split("openinweb=")[1];
                }
                if (url.toLowerCase().contains("blscheme://logincallback")) {
                    String encryptValue = CryptoUtils.getencryptedString(GetUTCDateTimeWithFormat());

                    webView.loadUrl("https://beta.blood.live/epass?ct=Android&token=" + encryptValue);
                    return true;

                }else if(url.toLowerCase().contains("blscheme://contactpermission")){
                    intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                    intent.setData(uri);
                    context.startActivity(intent);
                    return true;
                }
                else if (url.toLowerCase().contains("social_plugin=comments") && oldWeb != null) {
                    return false;
                }
                if(url.contains("close_popup.php")){
                    String actId = Utility.GetUserPreference(Constants.ReadActId, context);
                    webView.evaluateJavascript("ReLoadFaceBookComment('" + actId + "')", new JavaScriptResult("fbLoad", context));
                    HomeActivity.homeActivity.onBackPressed();
                    CloseDialog(dialog, webView);
                    return true;
                }
                else if(url.toLowerCase().contains("blscheme://shareactcallback=")){
                    String aId = url.split("shareActcallback=")[1];
                    Utility.SetUserPreferences(Constants.ReadActId, aId, context);
                    return true;
                }
                else if (url.toLowerCase().contains("blscheme://loginsuccess")) {
                    String uId = url.split("id=")[1].split("&CN=")[0];
                    Utility.SetUserPreferences(Constants.UseruniqueId, uId, context);
                    RegisterOrUpdateDeviceToken(uId, context);
                    String eMail = getEmail(context);
                    eMail = !IsNullOrEmpty(eMail) ? eMail : "";
                    webView.evaluateJavascript("getLoggedInEmail('" + eMail + "')", new JavaScriptResult("getMail", context));
                    return true;
                }
                else if (url.toLowerCase().contains("blscheme://payment") && oldWeb != null) {
                    CloseDialog(dialog, webView);
                    oldWeb.evaluateJavascript("Payment.capturePaymentSM('" + url + "')", new JavaScriptResult("", context));
                    return true;
                }
                else if (url.toLowerCase().contains("blscheme://sharecallback")) {
                    webView.evaluateJavascript("getShareData()", new JavaScriptResult("blShare", context));
                    return true;
                } else if (url.toLowerCase().contains("logoff")) {
                    WebStorage.getInstance().deleteAllData();
                    webView.clearCache(true);
                    webView.clearFormData();
                    webView.clearHistory();
                    webView.clearSslPreferences();
                    String encryptValue = CryptoUtils.getencryptedString(GetUTCDateTimeWithFormat());
                    webView.loadUrl("https://beta.blood.live/app?ct=Android&token=" + encryptValue);
                }else if (url.toLowerCase().contains("mailto:")) {
                    String eMail = url.split("mailto:")[1];
                    String subject = "";
                    String body = "";
                    if (url.toLowerCase().contains("?subject=")) {
                        eMail = eMail.split("\\?subject=")[0];
                        subject = url.split("/?subject=")[1].replace("\\n", "\r\n");
                    }
                    if (url.toLowerCase().contains("&body=")) {
                        subject = URLDecoder.decode(subject.split("&body=")[0], "UTF-8");
                        body = URLDecoder.decode(url.split("&body=")[1], "UTF-8");
                    }
                    intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
                    intent.setData(Uri.parse("mailto:"));
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{eMail});
                    intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                    intent.putExtra(Intent.EXTRA_TEXT, body);
                    //intent.setType("text/plain");
                    this.context.startActivity(intent);
                    return true;
                }
                else if (url.toLowerCase().contains("maps.google.com") || url.toLowerCase().contains("www.google.com/maps") ) {
                    Intent mapIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
                    mapIntent.setPackage("com.google.android.apps.maps");
                    this.context.startActivity(mapIntent);
                    return true;
                }
                else if (url.toLowerCase().contains("sms:")) {
                    String mobile = url.split(":")[1];
                    String msg = "";
                    if (url.contains("#")) {
                        mobile = mobile.split("#")[0];
                        msg = url.split("#")[1];
                    }
                    //SmsManager.getDefault().sendTextMessage(mobile, null, "", null, null); [This need sms permission in manifest and external permssion from user]
                    Uri uriSms = Uri.parse("smsto:" + mobile);
                    Intent intentSMS = new Intent(Intent.ACTION_SENDTO, uriSms);
                    intentSMS.putExtra("sms_body", msg);
                    this.context.startActivity(intentSMS);
                    return true;
                } else if (url.toLowerCase().contains("blscheme://sharebccallback")) {
                    Utility.CheckMediaPermissions(context, 1);
                    String social = url.split("##")[2];
                    final String image = url.split("blscheme://sharebccallback=")[1].split("##")[1].replace('{', ' ').replace('}', ' ');
                    final String title = url.split("blscheme://sharebccallback=")[1].split("##")[0].replace('{', ' ').replace("\\n", "\r\n");
                    context.findViewById(R.id.lnrloader).setVisibility(View.VISIBLE);
                    if (social.toLowerCase().contains("whatsapp")) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Bitmap bitmap = null;
                                try {
                                    bitmap = BitmapFactory.decodeStream((InputStream) new URL(image).getContent());
                                    Log.d(TAG, "run: " + bitmap);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                File file = saveTempBitmap(bitmap, "Image");
                                ShareonWhatsapp(context, title, file);
                            }
                        }).start();
                        context.findViewById(R.id.lnrloader).setVisibility(View.GONE);
                        return true;
                    } else if (social.toLowerCase().equals("image")) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Bitmap bitmap = null;
                                try {
                                    bitmap = BitmapFactory.decodeStream((InputStream) new URL(image).getContent());
                                    Log.d(TAG, "run: " + bitmap);
                                    File file = saveTempBitmap(bitmap, "Image");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        }).start();
                        context.findViewById(R.id.lnrloader).setVisibility(View.GONE);
                        Toast.makeText(context.getBaseContext(), "Image saved successfully", Toast.LENGTH_LONG).show();
                        return true;
                    } else if (social.toLowerCase().equals("twitter")) {
                        try {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Bitmap bitmap = null;
                                    try {
                                        bitmap = BitmapFactory.decodeStream((InputStream) new URL(image).getContent());
                                        Log.d(TAG, "run: " + bitmap);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    File file = saveTempBitmap(bitmap, "Image");
                                    Uri uri = null;

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
                                    }
                                    Intent tweetIntent = new Intent(Intent.ACTION_SEND);
                                    tweetIntent.setType("text/plain");
                                    tweetIntent.setPackage("com.twitter.android");
                                    tweetIntent.putExtra(Intent.EXTRA_TEXT, title);
                                    tweetIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                    try {
                                        context.startActivity(tweetIntent);
                                    } catch (android.content.ActivityNotFoundException ex) {
                                        HomeActivity.homeActivity.ToastNotify("twitter app have not been installed.");
                                    }
                                }
                            }).start();

                        } catch (final ActivityNotFoundException e) {
                            Log.i("twitter", "no twitter native", e);
                        }
                        context.findViewById(R.id.lnrloader).setVisibility(View.GONE);
                        return true;
                    } else if (social.toLowerCase().equals("fb")) {
                        try {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Bitmap bitmap = null;
                                    try {
                                        bitmap = BitmapFactory.decodeStream((InputStream) new URL(image).getContent());
                                        Log.d(TAG, "run: " + bitmap);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    File file = saveTempBitmap(bitmap, "Image");
                                    Uri uri = null;

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
                                    }
                                    Intent fbIntent = new Intent(Intent.ACTION_SEND);
                                    //fbIntent.setType("text/plain");
                                    fbIntent.setPackage("com.facebook.katana");
                                    fbIntent.setType("image/*");
                                    fbIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                    fbIntent.putExtra(Intent.EXTRA_TEXT, title + "\r\n" + image);
                                    try {
                                        context.startActivity(fbIntent);
                                    } catch (android.content.ActivityNotFoundException ex) {
                                       // Toast.makeText(context, "facebook have not been installed.", Toast.LENGTH_LONG);
                                        HomeActivity.homeActivity.ToastNotify("facebook have not been installed.");
                                    }

                                }
                            }).start();

                        } catch (final ActivityNotFoundException e) {
                            Log.i("facebook", "no facebook native", e);
                        }
                        context.findViewById(R.id.lnrloader).setVisibility(View.GONE);
                        return true;
                    }

                } else if (url.toLowerCase().contains("https://twitter.com") || url.toLowerCase().contains("https://www.facebook.com/") || url.toLowerCase().contains("www.youtube.com")) {
                    Uri uri = Uri.parse(url);
                    intent = new Intent(Intent.ACTION_VIEW, uri);
                    this.context.startActivity(intent);
                    return true;
                } else if (url.contains("tel:")) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse(url));
                    this.context.startActivity(callIntent);
                    return true;
                }
                else if (url.toLowerCase().contains("api.whatsapp.com")
                        || url.toLowerCase().contains("whatsapp:")
                        || url.toLowerCase().contains("play.google.com")
                        || url.toLowerCase().contains("privacypolicy")
                        || url.toLowerCase().contains("termsofuse")
                        || url.toLowerCase().contains(".pdf")
                        || url.toLowerCase().contains("openinweb")
                        || url.toLowerCase().contains("kotiigroupofventures.org")
                        || url.toLowerCase().contains("bhclpl.org")
                        || url.toLowerCase().contains("https://bit.ly/")
                        || url.toLowerCase().contains("https://apps.apple.com")
                        || url.toLowerCase().contains("www.google.com")
                        || url.toLowerCase().contains("dialog/plugin.optin")

                ) {
                    OpenURL(context, url);
                    return true;
                }
            } catch (Exception ex) {
                Log.e(ex.getMessage(), "Error in override");
            }
            return super.shouldOverrideUrlLoading(webView, request);
        }
    }




    public static class CustomWebChromeClient extends WebChromeClient {
        Activity context;

        public CustomWebChromeClient(Activity context) {
            this.context = context;
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            super.onGeolocationPermissionsShowPrompt(origin, callback);
            callback.invoke(origin, true, false);
        }


        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            try {
                //Utility.SelectProfilePicture(activity);
                HomeActivity.homeActivity.mUploadMessage = filePathCallback;
                Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickImageIntent.setType("image/* video/*");
                pickImageIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                pickImageIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
                pickImageIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                this.context.startActivityForResult(Intent.createChooser(pickImageIntent, "File Chooser"), 1);

                return true;
            } catch (Exception ex) {

                return super.onShowFileChooser(webView, filePathCallback, fileChooserParams);
            }
        }


        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            try {
                WebView.WebViewTransport transport;
                final Dialog webviewDialog = new Dialog(context);
                String url = view.getUrl();
                webviewDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                webviewDialog.setCanceledOnTouchOutside(false);
                webviewDialog.setContentView(R.layout.webview_dialog);
                webviewDialog.getWindow().makeActive();
                if (isDialog)
                    webviewDialog.show();
                final WebView newWebView = webviewDialog.findViewById(R.id.childwebView);
                WebSettings webSettings = newWebView.getSettings();
                newWebView.addJavascriptInterface(new Utility.WebAppInterface(context, newWebView, webviewDialog),"Android");
                webSettings.setJavaScriptEnabled(true);
                webviewDialog.setCancelable(false);
                webSettings.setDatabaseEnabled(true);
                android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(newWebView, true);
                Window window = webviewDialog.getWindow();
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                ImageView imageView = webviewDialog.findViewById(R.id.close_payment);
                imageView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                        alertDialogBuilder.setMessage("Are you sure you want to close the dialog?");
                        alertDialogBuilder.setPositiveButton("yes",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        Utility.CloseDialog(webviewDialog, newWebView);
                                        return;
                                    }
                                });

                        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                });


                newWebView.setWebViewClient(new CustomWebViewClient(context, newWebView, webviewDialog));

                transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(newWebView);
                resultMsg.sendToTarget();

            } catch (Exception ex) {
                Log.e(TAG, "onCreateWindow: ");
            }
            return true;
        }

        @Override
        public void onPermissionRequest(PermissionRequest request) {
            super.onPermissionRequest(request);
            // Utility.CheckMediaPermissions(activity, 1);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                request.grant(request.getResources());
            }
        }
    }

    public static class WebAppInterface {
        Activity mContext;
        WebView mWebView;
        Dialog mDialog;
        public WebAppInterface(Activity context, WebView webView, Dialog dialog) {
            mContext = context;
            mWebView = webView;
            mDialog = dialog;
        }
        @JavascriptInterface   // must be added for API 17 or higher
        public void showToast(String url) {
            //Toast.makeText(mContext, url, Toast.LENGTH_SHORT).show();
            CloseDialog(mDialog, mWebView);
            mWebView.evaluateJavascript("Payment.capturePaymentSM('" + url + "')", new JavaScriptResult("", mContext));
        }
    }
}

