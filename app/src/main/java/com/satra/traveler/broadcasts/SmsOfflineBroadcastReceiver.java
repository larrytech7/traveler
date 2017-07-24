package com.satra.traveler.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;

import com.satra.traveler.utils.Tutility;

import static com.google.ads.AdRequest.LOGTAG;

public class SmsOfflineBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        //This method is called when the BroadcastReceiver is receiving
        SmsManager smsManager = SmsManager.getDefault();

            if (TextUtils.equals(intent.getAction(), Tutility.APP_EMERGENCY_CONTACT)) {
                String contact = intent.getStringExtra("src");
                String message = intent.getStringExtra("message");
                smsManager.sendTextMessage(Tutility.APP_EMERGENCY_CONTACT, null,
                        message, null, null);
                Log.d(LOGTAG, "Message sent from broadcast: " + message);
            }
    }
}
