package com.satra.traveler;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import com.google.gson.Gson;
import com.satra.traveler.models.ResponsStatusMsgMeta;
import com.satra.traveler.models.SpeedOverhead;
import com.satra.traveler.models.TrackingData;
import com.satra.traveler.utils.TConstants;
import com.satra.traveler.utils.Tutility;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;


/**
 * Created by Steve Jeff on 17/04/2016.
 */
public class SpeedMeterService extends Service {

    private static final int NBRE_MAX_ITERATION_POUR_MOYENNE_VITESSES = 3;
    private static final int MAX_VITESSE_METRE_SECONDE = 0;
    private static final float COEFF_CONVERSION_MS_KMH = 4;
    private static final int MAX_SPEED_ALLOWED_KMH = 90;
    private static final int NATURAL_LIMIT_OF_SPEED = 200;
    private static final int ERREUR_ACCEPTE_VITESSE_MAX=2;
    private static final int MAX_SPEED_TO_ALERT_KMH = 80;
    private static final long INTERVAL_BETWEEN_UPDATES = 10000;
    LocationManager locationManager;
    float vitesse = 0;
    private static int id = 1;
    private static boolean hasReachLimit = false;
    private static String displayedSpeed = "";
    private SharedPreferences.Editor editor;
    private Location previousLocation;
    private long durationGPS=0, durationNetwork=0;
    private Vector<Float> vitesses = new Vector<>();
    private LocationListener locationListenerGPS, locationListenerNetwork;

    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startid){
        Log.e("service starting...", "service SpeedMeterService is starting ");

//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//
//            return START_NOT_STICKY;
//        }
//        if (locationManager != null){
//            if(locationListenerNetwork!=null){
//                locationManager.removeUpdates(locationListenerNetwork);
//            }
//            if(locationListenerGPS!=null){
//                locationManager.removeUpdates(locationListenerGPS);
//            }
//        }
//
//        start();

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        start();
  //      Bundle extra = intent.getExtras();
  //      if(extra != null) {
   //             utilisateur = (Utilisateur)extra.getSerializable("utilisateur");


    }

    public void start(){
        id = 1;
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();

        editor = getSharedPreferences(TConstants.TRAVELR_PREFERENCE, 0).edit();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showAlertEnableGPS();
        }

        durationGPS = System.currentTimeMillis();
        durationNetwork = System.currentTimeMillis();

        // Define a listener that responds to GPS location updates
        locationListenerGPS = new LocationListener() {
            public void onLocationChanged(Location location) {
                durationGPS = updateSpeed(location, durationGPS);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.e("status changed  ", "status "+status);
            }

            public void onProviderEnabled(String provider) {
                Log.e("status changed  ", "provider enabled");
            }

            public void onProviderDisabled(String provider) {
                Log.e("status changed  ", "provider disabled");
            }
        };

        // Define a listener that responds to Network location updates
        locationListenerNetwork = new LocationListener() {
            public void onLocationChanged(Location location) {

                if(System.currentTimeMillis() - durationGPS>1800000)
                    durationNetwork = updateSpeed(location, durationNetwork);

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.e("status changed  ", "status "+status);
            }

            public void onProviderEnabled(String provider) {
                Log.e("status changed  ", "provider enabled");
            }

            public void onProviderDisabled(String provider) {
                Log.e("status changed  ", "provider disabled");
            }
        };



        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, locationListenerGPS);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 200, 0, locationListenerNetwork);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }catch (SecurityException sec){
            sec.printStackTrace();
        }
    }



    public void showAlertEnableGPS(){

        Intent intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager nm = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);

        Notification.Builder build = new Notification.Builder(this);
        build.setAutoCancel(false);
        build.setWhen(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            build.setStyle(new Notification.BigTextStyle().bigText(getString(R.string.gps_disabled_message)));
        }
        build.setTicker(getString(R.string.app_name));
        build.setContentTitle(getString(R.string.open_settings));
        build.setContentText(getString(R.string.gps_disabled_message));
        build.setSmallIcon(R.mipmap.ic_launcher);
        build.setContentIntent(pendingIntent);
        build.setOngoing(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            build.build();
        }

        Notification notif = build.getNotification();
        nm.notify(id, notif);
        id++;

    }

    public void showPersistentNotification(String vitesse) {
        Intent intent1 = new Intent(this, MyPositionActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager nm = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);

        Notification.Builder build = new Notification.Builder(this);
        build.setAutoCancel(false);
        build.setWhen(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            build.setStyle(new Notification.BigTextStyle().bigText(vitesse));
        }
        build.setTicker(getString(R.string.app_name));
        build.setContentTitle(getString(R.string.estimated_speed));
        build.setContentText(vitesse);
        build.setSmallIcon(R.mipmap.ic_launcher);
        build.setContentIntent(pendingIntent);
        build.setOngoing(true);

        build.setNumber(MAX_SPEED_ALLOWED_KMH);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            build.build();
        }

        Notification notif = build.getNotification();
        nm.notify(0, notif);

    }

    public static void showNotification(String vitesse, Context context){
        Intent intent1 = new Intent(context, MyPositionActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager nm = (NotificationManager)
                context.getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder build = new Notification.Builder(context);

        int nbre = MyPositionActivity.isCurrentTripExist()?SpeedOverhead.find(SpeedOverhead.class, "tripid = ?", ""+MyPositionActivity.getCurrentTrip().getId()).size():0;
        String message = nbre>0?nbre+context.getString(R.string.speed_overheading):context.getString(R.string.travelr_secure_your_trip);

        build.setAutoCancel(false);
        build.setWhen(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            build.setStyle(new Notification.BigTextStyle().bigText(message));
        }
        build.setTicker(context.getString(R.string.app_name));
        build.setContentTitle(vitesse);
        build.setContentText(message);
        build.setSmallIcon(R.mipmap.ic_launcher);
        build.setContentIntent(pendingIntent);
        build.setOngoing(true);
//        build.setNumber(MAX_SPEED_ALLOWED_KMH);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            build.build();
        }

        Notification notif = build.getNotification();
//        notif.vibrate = new long[] { 100, 250, 100, 500};
//        notif.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        nm.notify(id, notif);

    }

    public void hideNotification(){

        Intent intent1 = new Intent(this, MyPositionActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager nm = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(id);

        Notification.Builder build = new Notification.Builder(this);
        build.setAutoCancel(true);
        build.setWhen(0);
        build.setTicker(getString(R.string.app_name));
        build.setContentTitle(getString(R.string.speed_limit_reached_ago));
        build.setSmallIcon(R.mipmap.ic_launcher);
        build.setContentIntent(pendingIntent);
        build.setOngoing(false);

        build.setNumber(MAX_SPEED_ALLOWED_KMH);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            build.build();
        }

        Notification notif = build.getNotification();
        notif.vibrate = new long[] { 100, 250, 100, 500};
        notif.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        nm.notify(id, notif);

    }

    //calculating the speed in meters /sec from the GPS location or from our previous location should GPS not be available
    //save results as preference

    private static Long lastUpdate;

    private long updateSpeed(Location location, long duration){

        if(location.hasSpeed()){
            vitesse = location.getSpeed();
        }
        else{
            if(previousLocation!=null){
                vitesse = location.distanceTo(previousLocation)/((System.currentTimeMillis() - duration)/1000);
            }
            else{
                vitesse = 0;
            }
            previousLocation = location;
            duration = System.currentTimeMillis();

        }

        vitesses.add(vitesse);

        if(vitesses.size()>NBRE_MAX_ITERATION_POUR_MOYENNE_VITESSES){
            vitesses.remove(0);
        }
        vitesse = 0.0f;
        for(Float f: vitesses){
            vitesse+=f;
        }
        vitesse/=vitesses.size();


        if(vitesse*COEFF_CONVERSION_MS_KMH-ERREUR_ACCEPTE_VITESSE_MAX>NATURAL_LIMIT_OF_SPEED){
            return duration;
        }

        displayedSpeed = vitesse >= MAX_VITESSE_METRE_SECONDE ? + Tutility.round(vitesse * COEFF_CONVERSION_MS_KMH) + " KM/H" : Tutility.round(vitesse) + " M/S";

        showNotification(displayedSpeed, getApplicationContext());

        editor.putFloat(TConstants.SPEED_PREF, vitesse);
        editor.commit();

        Log.e("speed injected", "new speed received and injected: "+vitesse);

       if(MyPositionActivity.isCurrentTripExist()){
           if(((vitesse *COEFF_CONVERSION_MS_KMH) -ERREUR_ACCEPTE_VITESSE_MAX> MAX_SPEED_ALLOWED_KMH)){

               pushSpeedOnline(SpeedMeterService.this, vitesse, location, null);

           }
           else if((lastUpdate==null||System.currentTimeMillis()-lastUpdate>INTERVAL_BETWEEN_UPDATES)){
               pushSpeedOnline(SpeedMeterService.this, vitesse, location, null);
               lastUpdate = System.currentTimeMillis();
           }
       }

        tryToSentDataOnline(getApplicationContext());

        return  duration;
    }

    private static MultiValueMap<String, String> body ;
    private static ResponseEntity<String> response;
    private static TrackingData trackingDataa;
    private static RestTemplate restTemplate;
    private static  SpeedOverhead so = null;
    private static void pushSpeedOnline(final Context context, final float vitesse, final Location location, final TrackingData trackingData) {
        new AsyncTask<Void, Void, ResponsStatusMsgMeta>(){
            @Override
            protected ResponsStatusMsgMeta doInBackground(Void... params) {
                try {
                    // HttpAuthentication httpAuthentication = new HttpBasicAuthentication("username", "password");
                    //Create the request body as a MultiValueMap
                    if(body==null){
                        body= new LinkedMultiValueMap<>();
                    }
                    else{
                        body.clear();
                    }
                    body.add(TConstants.POST_SPEED_AND_POSITION_PARAM_LAT, String.valueOf(location.getLatitude()));
                    body.add(TConstants.POST_SPEED_AND_POSITION_PARAM_LNG, String.valueOf(location.getLongitude()));
                    body.add(TConstants.POST_SPEED_AND_POSITION_PARAM_SPEED, String.valueOf(vitesse));
                    body.add(TConstants.POST_SPEED_AND_POSITION_PARAM_MATRICULE, MyPositionActivity.getCurrentTrip().getBus_immatriculation());
                    body.add(TConstants.POST_SPEED_AND_POSITION_PARAM_MAT_ID, context.getSharedPreferences(TConstants.TRAVELR_PREFERENCE, 0)
                            .getString(MyPositionActivity.getCurrentTrip().getBus_immatriculation(), context.getSharedPreferences(TConstants.TRAVELR_PREFERENCE, 0)
                                    .getString(TConstants.PREF_MAT_ID, "0")));


                    if(restTemplate==null){
                        restTemplate = new RestTemplate(true);
                    }

                    response = restTemplate.exchange(TConstants.POST_SPEED_AND_POSITION_URL, HttpMethod.POST, new HttpEntity<Object>(body, new HttpHeaders()), String.class);
                    Log.e("Response speed", "res: "+response);
                    Log.e("Response body speed", "body "+response.getBody());

                    return new Gson().fromJson(response.getBody(), ResponsStatusMsgMeta.class);
                } catch (Exception e) {
                    Log.e("MainActivity", e.getMessage(), e);
                }

                return null;
            }



            @Override
            protected void onPostExecute(ResponsStatusMsgMeta response) {

                if(response==null || response.getStatus()!=100){

                    if(trackingData==null){

                        if((vitesse *COEFF_CONVERSION_MS_KMH) -ERREUR_ACCEPTE_VITESSE_MAX> MAX_SPEED_TO_ALERT_KMH){
                            if(!hasReachLimit) {

                                so = new SpeedOverhead();
                                so.setDate_start(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US).format(Calendar.getInstance().getTime()));
                                so.setLatitude_start(location.getLatitude());
                                so.setLongitude_start(location.getLongitude());
                                so.setSpeed_start(vitesse);
                                so.setTripid(""+MyPositionActivity.getCurrentTrip().getId());
                               // so.save();

                                hasReachLimit = true;
                            }
                        }
                        else{
                            if(hasReachLimit) {
                                so.setLatitude_end(location.getLatitude());
                                so.setLongitude_end(location.getLongitude());
                                so.setSpeed_end(vitesse);
                                so.setDate_end(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US).format(Calendar.getInstance().getTime()));
                                so.save();


                                hasReachLimit = false;
                            }
                        }

                        trackingDataa = new TrackingData();
                        trackingDataa.setTrackingMatricule(context.getSharedPreferences(TConstants.TRAVELR_PREFERENCE, 0)
                                .getString(TConstants.PREF_MAT_ID, "0"));
                        trackingDataa.setLatitude(location.getLatitude());
                        trackingDataa.setLongitude(location.getLongitude());
                        trackingDataa.setLocation(""); //je sais pas si c'est possible d'avoir le nom de l'endroit ou ces donnees ont ete recuperer
                        trackingDataa.setSpeed(vitesse);
                        trackingDataa.save();
                    }

                }
                else{

                    if(trackingData!=null){
                        trackingData.delete();

                        SharedPreferences.Editor editor = context.getSharedPreferences(TConstants.TRAVELR_PREFERENCE, 0).edit();
                        //TODO IndexOutOfBounds error ici quand la synchronisation ne s'effectue pas correctement
                        editor.putString(trackingData.getTrackingMatricule(), response.getMeta().getMatricule_id()+"");

                        editor.commit();

                    }
                    else{
                        if(response.getMeta().getCode()==201){
                            if(!hasReachLimit) {


                                so = new SpeedOverhead();
                                so.setDate_start(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US).format(Calendar.getInstance().getTime()));
                                so.setLatitude_start(location.getLatitude());
                                so.setLongitude_start(location.getLongitude());
                                so.setSpeed_start(vitesse);
                                so.setTripid(""+MyPositionActivity.getCurrentTrip().getId());
                                //so.save();

                                hasReachLimit = true;
                            }
                        }
                        else{
                            if(hasReachLimit) {

                                so.setLatitude_end(location.getLatitude());
                                so.setLongitude_end(location.getLongitude());
                                so.setSpeed_end(vitesse);
                                so.setDate_end(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US).format(Calendar.getInstance().getTime()));
                                so.save();

                                hasReachLimit = false;
                            }
                        }

                        SharedPreferences.Editor editor = context.getSharedPreferences(TConstants.TRAVELR_PREFERENCE, 0).edit();
                        //TODO IndexOutOfBounds error ici quand la synchronisation ne s'effectue pas correctement
                        editor.putString(MyPositionActivity.getCurrentTrip().getBus_immatriculation(), response.getMeta().getMatricule_id()+"");

                        editor.commit();
                    }

                    /*
                    * TODO 2 after TO DO 1
                     * @author: STEVE
                    * POST DES COUPLES VITESSE+POSITION SAUVEGARDES DANS LA BD LOCALE SUR LE SERVEUR EN LIGNE
                    * Prete attention a comment j'ai defini les TODO ci, c'est une facon speciale de permettre a
                    * ton editeur de code de reconaitre automatiquement tout les points dans les fichiers marque 'TODO'
                     */
                    tryToSentDataOnline(context);
                }

            }
        }.execute();
    }

    public static  void tryToSentDataOnline(Context context){
        //voici les objets de vitesse/position dans la bd locale
        Iterator<TrackingData> trackingDatas = TrackingData.findAll(TrackingData.class);
        //manipule cette liste pour envoyer ces donnes en ligne
        if(trackingDatas.hasNext()){
            TrackingData trackingData1 = trackingDatas.next();
            Location location1 = new Location(trackingData1.getLocation());
            location1.setLatitude(trackingData1.getLatitude());
            location1.setLongitude(trackingData1.getLongitude());

            if((lastUpdate==null||System.currentTimeMillis()-lastUpdate>INTERVAL_BETWEEN_UPDATES)){
                pushSpeedOnline(context, (float) trackingData1.getSpeed(), location1, trackingData1);
                lastUpdate = System.currentTimeMillis();
            }


        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null){
            try {
                if(locationListenerNetwork!=null){
                    locationManager.removeUpdates(locationListenerNetwork);
                }
                if(locationListenerGPS!=null){
                    locationManager.removeUpdates(locationListenerGPS);
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

    }
}
