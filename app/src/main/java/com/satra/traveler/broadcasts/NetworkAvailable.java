package com.satra.traveler.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.satra.traveler.utils.TConstants;

public class NetworkAvailable extends BroadcastReceiver {
    private SharedPreferences sharedPreferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        sharedPreferences = context.getSharedPreferences(TConstants.TRAVELR_PREFERENCE, Context.MODE_PRIVATE);
        // Network changed. If connected and data not synced, retrieve trips the user created and sync with online data sources
        if (intent.getAction().equalsIgnoreCase("android.net.conn.CONNECTIVITY_CHANGE"));
    }

}
