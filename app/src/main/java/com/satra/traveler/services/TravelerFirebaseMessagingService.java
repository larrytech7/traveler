package com.satra.traveler.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.satra.traveler.MessagingActivity;
import com.satra.traveler.R;

import org.json.JSONException;
import org.json.JSONObject;

public class TravelerFirebaseMessagingService extends FirebaseMessagingService {
    private final String TAG = "FCMMessagingService";
    private final int NOTIFICATION_MESSAGE = 1;
    private final int NOTIFICATION_AD = 2;

    public TravelerFirebaseMessagingService() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            JSONObject payload = new JSONObject(remoteMessage.getData());
            try {
                String title = payload.getString("user");
                String body = payload.getString("body");
                int type = payload.getInt("type");
                //TODO: Use type maybe to differentiate different sources of a notification like this
                showNotification(MessagingActivity.class, this, title,body, NOTIFICATION_MESSAGE);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (remoteMessage.getNotification() != null){
            //extract Data and send Notification
            String title = remoteMessage.getNotification().getTitle();
            String body =remoteMessage.getNotification().getBody();
            showNotification(MessagingActivity.class, this, title,body, NOTIFICATION_AD);
        }

    }

    @Override
    public void onMessageSent(String s) {
        super.onMessageSent(s);
        Log.d("TAG", s);
    }

    private void showNotification(Class c, Context context, String title, String content, int id){
        Intent intent1 = new Intent(context, c);
        intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager nm = (NotificationManager)
                context.getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder build = new Notification.Builder(context);

        build.setAutoCancel(true);
        build.setWhen(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            build.setStyle(new Notification.BigTextStyle().bigText(content));
        }
        build.setTicker(context.getString(R.string.app_name));
        build.setContentTitle(title);
        build.setContentText(content);
        build.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_web));
        build.setSmallIcon(R.drawable.ic_messaging_24dp);
        build.setContentIntent(pendingIntent);
        build.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        build.setOngoing(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            build.build();
        }

        Notification notif = build.getNotification();
        notif.vibrate = new long[] { 100, 250, 100, 500};
        notif.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        nm.notify(id, notif);

    }

}
