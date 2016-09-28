package com.satra.traveler.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import com.satra.traveler.MessagingActivity;
import com.satra.traveler.SpeedMeterService;
import com.satra.traveler.models.TrackingData;

import java.util.List;

public class NetworkAvailable extends BroadcastReceiver {
    private boolean isConnected = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Network changed. If connected and data not synced, retrieve trips the user created and sync with online data sources
        if (intent.getAction().equalsIgnoreCase("android.net.conn.CONNECTIVITY_CHANGE"))
        if (isNetworkAvailable(context)){
            /**
             * @Author Larry A.
             * TODO Synchroniser ls donnes non synchroniser en ligne des que la connexion redevient disponbile
             */
//            List<TrackingData> trackingData = TrackingData.listAll(TrackingData.class);
//            Log.d("Network available", "Connected");

            SpeedMeterService.tryToSentDataOnline(context);

            MessagingActivity.tryToSentDataOnline(context);
        }
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity.getActiveNetworkInfo() != null)
            return connectivity.getActiveNetworkInfo().isConnected();
        return isConnected;
    }
}
