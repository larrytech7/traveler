package com.satra.traveler.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.satra.traveler.MyPositionActivity;
import com.satra.traveler.SpeedMeterService;
import com.satra.traveler.models.Messages;
import com.satra.traveler.models.ResponsStatusMsg;
import com.satra.traveler.utils.TConstants;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class NetworkAvailable extends BroadcastReceiver {
    private boolean isConnected = false;
    private SharedPreferences sharedPreferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        sharedPreferences = context.getSharedPreferences(TConstants.TRAVELR_PREFERENCE, Context.MODE_PRIVATE);
        // Network changed. If connected and data not synced, retrieve trips the user created and sync with online data sources
        if (intent.getAction().equalsIgnoreCase("android.net.conn.CONNECTIVITY_CHANGE"))
        if (isNetworkAvailable(context)){

            Log.d("Network available", "Connected");
            //send speeding data online
            SpeedMeterService.tryToSentDataOnline(context);
            //send message data when internet is reestablished
            sendMessageOnline(context);
        }
    }

    /**
     * Send messages (saved offline) online
     * @param context application context to operate within
     */
    public void sendMessageOnline(Context context){
        for (Messages mMessage : Messages.find(Messages.class, "sent = ?", "0")) {
            pushMessageOnline(context, null, mMessage.getContent(), mMessage);
        }
    }

    private void pushMessageOnline(final Context context, Object o, final String content, final Messages mMessage) {
        new AsyncTask<Void, Void, ResponsStatusMsg>(){

            @Override
            protected ResponsStatusMsg doInBackground(Void... params) {
                try {
                    //TODO. Faudras gerer aussi l'envoie de l'image capturer ci disponible. C'est un element non-facultatif
                    // HttpAuthentication httpAuthentication = new HttpBasicAuthentication("username", "password");
                    HttpHeaders requestHeaders = new HttpHeaders();
                    //Create the request body as a MultiValueMap
                    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

                    body.add(TConstants.POST_MESSAGE_PARAM_TIMESTAMP, System.currentTimeMillis()+"");
                    body.add(TConstants.POST_MESSAGE_PARAM_MESSAGE, content);
                    body.add(TConstants.POST_MESSAGE_PARAM_MAT_ID, MyPositionActivity.isCurrentTripExist()?sharedPreferences
                            .getString(MyPositionActivity.getCurrentTrip().getBus_immatriculation(), sharedPreferences
                                    .getString(TConstants.PREF_MAT_ID, "0")):sharedPreferences
                            .getString(TConstants.PREF_MAT_ID, "0"));

                    body.add(TConstants.POST_MESSAGE_PARAM_MATRICULE, MyPositionActivity.isCurrentTripExist()?MyPositionActivity.getCurrentTrip().getBus_immatriculation():sharedPreferences.getString(TConstants.PREF_MATRICULE, "0"));
                    body.add(TConstants.POST_MESSAGE_PARAM_MSISDN, sharedPreferences
                            .getString(TConstants.PREF_PHONE, "0"));
                    body.add(TConstants.POST_MESSAGE_PARAM_USERNAME, sharedPreferences
                            .getString(TConstants.PREF_USERNAME, "0"));

                    HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);
                    RestTemplate restTemplate = new RestTemplate(true);

                    Gson gson = new Gson();
                    ResponseEntity<String> response = restTemplate.exchange(TConstants.POST_MESSAGE_URL, HttpMethod.POST, httpEntity, String.class);
                    Log.e("Response", "res: "+response);
                    Log.e("Response body", "body "+response.getBody());

                    return gson.fromJson(response.getBody(), ResponsStatusMsg.class);
                } catch (Exception e) {
                    Log.e("MainActivity", e.getMessage(), e);
                }

                return null;
            }

            @Override
            protected void onPostExecute(ResponsStatusMsg response) {
                //Sent messages need to be marked as sent by changing the status of the message
                if (response != null )
                    if (response.getStatus() == 100){
                        //set message status to 'sent'
                        mMessage.setSent(1);
                        mMessage.save();
                    }
            }
        }.execute();
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity.getActiveNetworkInfo() != null)
            return connectivity.getActiveNetworkInfo().isConnected();
        return isConnected;
    }
}
