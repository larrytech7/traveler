package com.satra.traveler;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.satra.traveler.utils.TConstants;

import java.util.Vector;


/**
 * Created by Steve Jeff on 17/04/2016.
 */
public class SpeedMeterService extends Service {

    private SharedPreferences.Editor editor;

    private static final int NBRE_MAX_ITERATION_POUR_MOYENNE_VITESSES = 3;
    private static final int MAX_VITESSE_METRE_SECONDE = 0;
    private static final float COEFF_CONVERSION_MS_KMH = 4;

    private static final int MAX_SPEED_ALLOWED_KMH = 80;
    private static final int NATURAL_LIMIT_OF_SPEED = 200;
    private static final int ERREUR_ACCEPTE_VITESSE_MAX=2;
    LocationManager locationManager;
    private Location previousLocation;
    private long durationGPS=0, durationNetwork=0;
    float vitesse = 0;
    private Vector<Float> vitesses = new Vector<>();
    private LocationListener locationListenerGPS, locationListenerNetwork;

    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startid){
        Log.e("service starting...", "service SpeedMeterService is starting ");

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return START_NOT_STICKY;
        }
        if (locationManager != null){
            if(locationListenerNetwork!=null){
                locationManager.removeUpdates(locationListenerNetwork);
            }
            if(locationListenerGPS!=null){
                locationManager.removeUpdates(locationListenerGPS);
            }
        }

        start();

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
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
        ((NotificationManager)
                getSystemService(NOTIFICATION_SERVICE)).cancelAll();

        editor = getSharedPreferences(TConstants.TRAVELR_PREFERENCE, 0).edit();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

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
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 200, 0, locationListenerNetwork);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, locationListenerGPS);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
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

    public void showNotification(String vitesse){
        Intent intent1 = new Intent(this, MyPositionActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager nm = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);


        Notification.Builder build = new Notification.Builder(this);

        String message = getString(R.string.speed_limit_reached_msg)+vitesse+").";

        build.setAutoCancel(true);
        build.setWhen(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            build.setStyle(new Notification.BigTextStyle().bigText(message));
        }
        build.setTicker(getString(R.string.app_name));
        build.setContentTitle(getString(R.string.speed_limit_reached));
        build.setContentText(message);
        build.setSmallIcon(R.mipmap.ic_launcher);
        build.setContentIntent(pendingIntent);
        build.setOngoing(true);

        build.setNumber(MAX_SPEED_ALLOWED_KMH);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            build.build();
        }

        Notification notif = build.getNotification();


        notif.vibrate = new long[] { 100, 250, 100, 500};
        notif.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

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

    int id = 1;
    boolean hasReachLimit = false;
    //calculating the speed in meters /sec from the GPS location or from our previous location should GPS not be available
    //save results as preference
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


        String displayedSpeed = vitesse >= MAX_VITESSE_METRE_SECONDE ? " (" + round(vitesse * COEFF_CONVERSION_MS_KMH) + " KM/H" + ")" : " (" + round(vitesse) + " m/s)";


        showPersistentNotification(displayedSpeed);

        editor.putFloat(TConstants.SPEED_PREF, vitesse);
        editor.commit();

        Log.e("speed injected", "new speed received and injected: "+vitesse);


        if((vitesse *COEFF_CONVERSION_MS_KMH) -ERREUR_ACCEPTE_VITESSE_MAX> MAX_SPEED_ALLOWED_KMH){
            if(!hasReachLimit) {
                showNotification(displayedSpeed);
                hasReachLimit = true;
            }
        }
        else{
            if(hasReachLimit) {
                hideNotification();
                id++;
                hasReachLimit = false;
            }
        }

        return  duration;
    }

    double round(double c){
        return Math.round(c*100)/100.0;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
//        flag = false;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (locationManager != null){
            if(locationListenerNetwork!=null){
                locationManager.removeUpdates(locationListenerNetwork);
            }
            if(locationListenerGPS!=null){
                locationManager.removeUpdates(locationListenerGPS);
            }
        }

    }
}
