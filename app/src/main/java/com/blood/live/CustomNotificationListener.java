package com.blood.live;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import com.google.firebase.messaging.RemoteMessage;
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationListener;

import java.util.Map;
import java.util.Random;

import static android.content.ContentValues.TAG;

//This class was used as a listener for notifications
public class CustomNotificationListener implements NotificationListener {

    public static final String NOTIFICATION_CHANNEL_ID = "BLOODLIVENOTIFICATIONCHANNEL";
    private NotificationManager mNotificationManager;
    public static int NOTIFICATION_ID = 1;
    String KEY_REPLY = "key_reply";

    @Override
    public void onPushNotificationReceived(Context context, RemoteMessage message) {

        /* The following notification properties are available. */
        try {
            RemoteMessage.Notification notification = message.getNotification();
            Map<String, String> data = message.getData();
            if (data != null) {
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    Log.d("Test", "key, " + entry.getKey() + " value " + entry.getValue());
                }
            }
             sendNotification(data, context);
        } catch (Exception ex) {
            Log.d("Listener", "onPushNotificationReceived: ");
        }

    }

    private void sendNotification(Map<String, String> data, Context ctx) {
        try {
            Intent intent = new Intent(ctx, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            if(!Utility.IsNullOrEmpty(data.get("link"))){
                intent.putExtra(Constants.NOTIFICATIONMSG, data.get("link"));
                intent.setData(Uri.parse(data.get("link")));
            }
            mNotificationManager = (NotificationManager)
                    ctx.getSystemService(Context.NOTIFICATION_SERVICE);


            PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                    ctx,
                    NOTIFICATION_CHANNEL_ID)
                    .setContentText(data.get("body"))
                    .setContentTitle(data.get("title"))
                    .setSmallIcon(R.drawable.not_icon)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setNumber(Constants.NOTIFICATION_COUNTER)
                    ;
            notificationBuilder.setContentIntent(contentIntent);
            NOTIFICATION_ID++;
            mNotificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

        } catch (Exception ex) {
            Log.e(TAG, "sendNotification: " + ex.getMessage());
        }
    }

}


