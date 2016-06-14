package com.satra.traveler.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

public class NetworkAvailable extends BroadcastReceiver {
    private boolean isConnected = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Network changed. If connected and data not synced, retrieve trips the user created and sync with online data sources
        if (intent.getAction().equalsIgnoreCase("android.net.conn.CONNECTIVITY_CHANGE"))
        if (isNetworkAvailable(context)){
            //TODO sync travel data is not synced
            Log.d("Network available", "Connected");
        }
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity.getActiveNetworkInfo() != null)
            return connectivity.getActiveNetworkInfo().isConnected();
        return isConnected;
    }
}
