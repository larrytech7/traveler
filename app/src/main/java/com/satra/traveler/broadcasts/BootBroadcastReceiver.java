package com.satra.traveler.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.satra.traveler.services.SpeedMeterService;

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TRAVELER");
        //Acquire the lock
        wl.acquire();

        //You can do the processing here.


        context.startService(new Intent(context, SpeedMeterService.class));


        //Release the lock
        wl.release();
    }



}
