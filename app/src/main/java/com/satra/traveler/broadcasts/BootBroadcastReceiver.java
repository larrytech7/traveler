package com.satra.traveler.broadcasts;

import android.app.AlarmManager;
import android.app.PendingIntent;
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

        SetAlarm(context);

        //Release the lock
        wl.release();
    }

    public void SetAlarm(Context context)
    {
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, SpeedMeterService.class);
        PendingIntent pi = PendingIntent.getService(context, 0, intent, 0);
        //After  15 minutes, restart the service if stopped
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 900, pi);
    }

    public void CancelAlarm(Context context)
    {
        Intent intent = new Intent(context, SpeedMeterService.class);
        PendingIntent sender = PendingIntent.getService(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }



}
