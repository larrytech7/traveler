package com.satra.traveler;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class GeofenceTransitionsIntentService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
    }



    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
//            String errorMessage = GeofenceErrorMessages.getErrorString(this,
//                    geofencingEvent.getErrorCode());
            Log.e("Travelr Geofence Serv", "error: " + geofencingEvent.getErrorCode());
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {


            // Send notification and log the transition details.
            sendNotification(geofenceTransition, getString(R.string.destination_reached), getString(R.string.destination_reached_msg));
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {


            // Send notification and log the transition details.
            sendNotification(geofenceTransition, getString(R.string.destination_exited), getString(R.string.destination_exited_msg));
        } else {
            // Log the error.
            Log.e("Travelr Geofence Serv", "error: Invalide transition type:  " + geofenceTransition);
        }

    }

    public  void sendNotification(int id, String title, String message){
        Intent intent1 = new Intent(GeofenceTransitionsIntentService.this, MyPositionActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager nm = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder build = new Notification.Builder(this);

        build.setAutoCancel(true);
        build.setWhen(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            build.setStyle(new Notification.BigTextStyle().bigText(message));
        }
        build.setTicker(getString(R.string.app_name));
        build.setContentTitle(title);
        build.setContentText(message);
        build.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_web));
        build.setSmallIcon(R.drawable.ic_myposition);
        build.setContentIntent(pendingIntent);
        build.setOngoing(true);
//        build.setNumber(MAX_SPEED_ALLOWED_KMH);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            build.build();
        }

        Notification notif = build.getNotification();
        notif.vibrate = new long[] { 100, 250, 100, 500};
        notif.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        nm.notify(id, notif);

    }

}