package com.satra.traveler.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.satra.traveler.MessagingActivity;
import com.satra.traveler.NewsActivity;
import com.satra.traveler.R;
import com.satra.traveler.models.User;
import com.satra.traveler.utils.TConstants;
import com.satra.traveler.utils.Tutility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TravelerFirebaseMessagingService extends FirebaseMessagingService {
    private final String TAG = "FCMMessagingService";
    private final int NOTIFICATION_MESSAGE = 1;
    private final int NOTIFICATION_AD = 2;
    private int numMessages = 0;
    private List<String> messageList = new ArrayList<>();
    private User travelerUser = Tutility.getAppUser();

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
                if(travelerUser != null && travelerUser.getUsername().equals(title))
                    return;
                messageList.add(body);
                ++numMessages;
                //TODO: Use type maybe to differentiate different sources of a notification like this
                showNotification(type == 1 ? MessagingActivity.class : NewsActivity.class,
                        this, title,body, type == 1 ? NOTIFICATION_MESSAGE : NOTIFICATION_MESSAGE);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (remoteMessage.getNotification() != null){
            //extract Data and send Notification
            String title = remoteMessage.getNotification().getTitle();
            String body =remoteMessage.getNotification().getBody();
            showNotification(NewsActivity.class, this, title,body, NOTIFICATION_AD);
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

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setTicker(context.getString(R.string.app_name));
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_web));
        builder.setSmallIcon(R.drawable.ic_messaging_24dp);
        builder.setContentIntent(pendingIntent);
        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        builder.setOngoing(true);
        builder.setAutoCancel(true);
        builder.setWhen(0);
        builder.setNumber(numMessages);
        /*NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.bigText(content);
        bigTextStyle.setSummaryText(getString(R.string.new_comments, numMessages));
        builder.setStyle(bigTextStyle);*/

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setSummaryText(getString(R.string.new_comments, numMessages));
        inboxStyle.setBigContentTitle(title);
        for(String m : messageList){
            inboxStyle.addLine(m);
        }
        builder.setStyle(inboxStyle);
        //set action button
        RemoteInput remoteInput = new RemoteInput.Builder(TConstants.INSTANT_REPLY)
                .setLabel(getString(R.string.reply))
                .build();
        NotificationCompat.Action commentAction = new NotificationCompat.Action.Builder(R.drawable.ic_send,
                getString(R.string.reply),
                pendingIntent)
                .setAllowGeneratedReplies(true)
                .addRemoteInput(remoteInput)
                .build();
        builder.addAction(commentAction);

        Notification notif = builder.build();
        notif.vibrate = new long[] { 100, 250, 100, 500};
        notif.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        nm.notify(id, notif);

    }

}
