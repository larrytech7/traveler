package com.satra.traveler.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.satra.traveler.MyPositionActivity;
import com.satra.traveler.R;
import com.satra.traveler.models.Incident;
import com.satra.traveler.models.SpeedOverhead;
import com.satra.traveler.models.TrackingData;
import com.satra.traveler.models.Trip;
import com.satra.traveler.models.User;
import com.satra.traveler.utils.TConstants;
import com.satra.traveler.utils.Tutility;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import static com.satra.traveler.MyPositionActivity.getCurrentTrip;


/**
 * Created by Steve Jeff on 17/04/2016.
 */
public class SpeedMeterService extends Service implements SensorEventListener, OnFailureListener, RecognitionListener, TextToSpeech.OnInitListener, OnCompleteListener<Void> {

    private static final int NBRE_MAX_ITERATION_POUR_MOYENNE_VITESSES = 3;
    private static final int MAX_VITESSE_METRE_SECONDE = 0;
    private static final float COEFF_CONVERSION_MS_KMH = 4;
    private static final int MAX_SPEED_ALLOWED_KMH = 90;
    private static final float MOVING_SPEED_THRESHOLD = 3f;//25f; //speed at which we can certify that object is moving
    private static final int NATURAL_LIMIT_OF_SPEED = 200;
    private static final int ERREUR_ACCEPTE_VITESSE_MAX=2;
    private static final int MAX_SPEED_TO_ALERT_KMH = 80;
    private static final long INTERVAL_BETWEEN_UPDATES = 10000;
    private static final float MAX_NORMAL_ACCELERATION_COEFF_MOVING = 2.0f;//4.5f;
    private static final float MAX_NORMAL_ACCELERATION_COEFF_NOT_MOVING = 5.0f;
    private static final float MAX_ALLOWED_ACCELERATION = 100.0f;

    private static final int TIME_TO_WAIT_FOR_SPEED_OVERHEAD_CONFIRMATION=5000;
    private Long durationElapsed = null;

    LocationManager locationManager;
    float vitesse = 0;
    private static int id = 1;
    private static boolean hasReachLimit = false;
    private SharedPreferences.Editor editor;
    private Location previousLocation;
    private long durationGPS=0, durationNetwork=0;
    private Vector<Float> vitesses = new Vector<>();
    private LocationListener locationListenerGPS, locationListenerNetwork;
    static final String LOGTAG = SpeedMeterService.class.getSimpleName();
    DatabaseReference databaseReference, baseReference;
    private User travelerUser;

    private SensorManager sensorMan;
    private Sensor accelerometer, magnetometer;

    private float mAccelCurrent;
    private float mAccelLast;
    private SpeechRecognizer speechRecognizer;
    private TextToSpeech tts;

    private float[] lastAccelerometer = new float[3];
    private float[] lastMagnetometer = new float[3];
    private boolean lastAccelerometerSet = false;
    private boolean lastMagnetometerSet = false;
    private float[] r = new float[9];
    private float[] orientationRadian = new float[3];
    private float[] orientationDegree = new float[3];
    private float lastSpeed;


    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startid){
        Log.e("service starting...", "service SpeedMeterService is starting ");
        databaseReference = FirebaseDatabase.getInstance().getReference(Tutility.FIREBASE_TRIPS);
        baseReference = FirebaseDatabase.getInstance().getReference(TConstants.FIREBASE_NOTIFICATION);
        Iterator<User> users = User.findAll(User.class);
        if (users.hasNext())
            travelerUser = users.next();
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
        //subscribe to FCM services to receive incoming notifs
        FirebaseMessaging.getInstance().subscribeToTopic(TConstants.FIREBASE_AD_TOPIC);

    }

    public void start(){
        id = 1;
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();

        editor = getSharedPreferences(TConstants.TRAVELR_PREFERENCE, MODE_PRIVATE).edit();

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
        } catch (IllegalArgumentException | SecurityException e) {
            e.printStackTrace();
        }

        sensorMan = (SensorManager)getSystemService(SENSOR_SERVICE);

        accelerometer = sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorMan.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        sensorMan.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_UI);
        sensorMan.registerListener(this, magnetometer,
                SensorManager.SENSOR_DELAY_UI);
        //init Speech to text operation
        startSpeechRecognition();
        //prepare TextToSpeech engine
        tts = new TextToSpeech(this, this);
    }

    private void startSpeechRecognition() {
        //check if device can provides speech recognition
        if(SpeechRecognizer.isRecognitionAvailable(this)) {
            Log.d(LOGTAG, "Speech recognition available");
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(this);
            speechRecognizer.startListening(new Intent(Intent.ACTION_VOICE_COMMAND));
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor == accelerometer){
            lastAccelerometer = event.values.clone();
            lastAccelerometerSet = true;
            // Shake detection
            float x = lastAccelerometer[0];
            float y = lastAccelerometer[1];
            float z = lastAccelerometer[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt(x*x + y*y + z*z);
            float mspeed = getSharedPreferences(TConstants.TRAVELR_PREFERENCE, MODE_PRIVATE).getFloat(TConstants.SPEED_PREF, 0.0f)* COEFF_CONVERSION_MS_KMH;

            if(mspeed >= MOVING_SPEED_THRESHOLD) { //speed to get that this is a moving vehicle
                if (MyPositionActivity.isCurrentTripExist() &&
                        mAccelCurrent >= MAX_NORMAL_ACCELERATION_COEFF_MOVING*SensorManager.GRAVITY_EARTH &&
                        mAccelCurrent < (MAX_ALLOWED_ACCELERATION * SensorManager.GRAVITY_EARTH) ) {
                    //Log.e("Accident detected: ", " -- mAccelCurrent: "+mAccelCurrent+" -- mAccelCurrent/9.8: "+(mAccelCurrent/SensorManager.GRAVITY_EARTH));

                    pushIncidentOnline(mspeed, 1);

                }
            }
            else if(mspeed > 0){
                //for stationary object, impact should increase acceleration
                if (MyPositionActivity.isCurrentTripExist() &&
                        mAccelCurrent  >= MAX_NORMAL_ACCELERATION_COEFF_NOT_MOVING * SensorManager.GRAVITY_EARTH &&
                        mAccelCurrent < (MAX_ALLOWED_ACCELERATION * SensorManager.GRAVITY_EARTH)) {
                    //Log.e("Accident detected: ", " -- mAccelCurrent: "+mAccelCurrent+" -- mAccelCurrent/9.8: "+(mAccelCurrent/SensorManager.GRAVITY_EARTH));
                    pushIncidentOnline(mspeed, 2);
                }
            }

        }
        else if (event.sensor== magnetometer){
            lastMagnetometer = event.values.clone();
            lastMagnetometerSet = true;
        }

        if (lastAccelerometerSet && lastMagnetometerSet) {
            /**
             * Computes the device's orientation
             * orientationRadian[0]: Azimuth, angle of rotation about the -z axis.
             *                This value represents the angle between the device's y
             *                axis and the magnetic north pole. When facing north, this
             *                angle is 0, when facing south, this angle is pi.
             *                Likewise, when facing east, this angle is pi/2, and
             *                when facing west, this angle is -pi/2. The range of
             *                values is -pi to pi.
             *
             * orientationRadian[1]: Pitch, angle of rotation about the x axis.
             *                This value represents the angle between a plane parallel
             *                to the device's screen and a plane parallel to the ground.
             *                Assuming that the bottom edge of the device faces the
             *                user and that the screen is face-up, tilting the top edge
             *                of the device toward the ground creates a positive pitch
             *                angle. The range of values is -pi to pi.
             *
             * orientationRadian[2]:Roll, angle of rotation about the y axis. This
             *                value represents the angle between a plane perpendicular
             *                to the device's screen and a plane perpendicular to the
             *                ground. Assuming that the bottom edge of the device faces
             *                the user and that the screen is face-up, tilting the left
             *                edge of the device toward the ground creates a positive
             *                roll angle. The range of values is -pi/2 to pi/2.
             *
             *  All three orientation angles in orientationRadian  are expressed in radians.
             *  Their equivalents in degree are in orientationDegree.
            */

            SensorManager.getRotationMatrix(r, null, lastAccelerometer, lastMagnetometer);
            SensorManager.getOrientation(r, orientationRadian);


            orientationDegree[0] = (float)(Math.toDegrees(orientationRadian[0])+360)%360;
            orientationDegree[1] = (float)(Math.toDegrees(orientationRadian[1])+360)%360;
            orientationDegree[2] = (float)(Math.toDegrees(orientationRadian[2])+360)%360;

            //Log.e("orientation", " azimuth: "+orientationDegree[0]+" Pitch: "+orientationDegree[1]+" Rool: "+orientationDegree[2]);
            //currentDegree = -azimuthInDegress;
        }

    }

    private void pushIncidentOnline(float mspeed, int type){

        notifyAlert(mAccelCurrent / SensorManager.GRAVITY_EARTH);
            lastSpeed = mspeed;
         /*
                 * Construire l'objet accident. L'objet a transmettre dans le setValue() method doit avoir ces proprietes
                 * matricule - le matricule du vehicule du trajet en cours
                 * speed - derniere vitesse enregistrer pour ce vehicule
                 * agency - agence ou personel
                 * acc - l'acceleration enregistrer en ms-2
                 * latitude - position
                 * longitude - position
                 * key - la cle est une propriete de l'objet Trip et celui ci correspond a la cle du trajet en cours
                 * timestamp - l'emprunte du temps de l'enregistrement de cet notification
                 */

        Trip trip = MyPositionActivity.getCurrentTrip();

        Incident incident = new Incident();

        incident.setKey(trip.getTripKey());
        incident.setMatricule(travelerUser.getCurrent_matricule());
        incident.setAgency(trip.getAgency_name());
        incident.setSpeed(Tutility.round(mspeed));
        incident.setAcc(mAccelCurrent / SensorManager.GRAVITY_EARTH);
        incident.setAcc_last(mAccelLast / SensorManager.GRAVITY_EARTH);
        incident.setLongitude(location == null ? 0 : location.getLongitude());
        incident.setLatitude(location == null ? 0 : location.getLatitude());
        incident.setTimestamp(System.currentTimeMillis());
        incident.setType(type);

        incident.setAzimuth(orientationDegree[0]);
        incident.setPitch(orientationDegree[1]);
        incident.setRoll(orientationDegree[2]);

        baseReference.child(TConstants.FIREBASE_NOTIF_ACCIDENT)
                .push()
                .setValue(incident)
                .addOnCompleteListener(this)
                .addOnFailureListener(this);
    }

    private void notifyAlert(float acc) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        Intent intent1 = new Intent(this, MyPositionActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager nm = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder build = new Notification.Builder(this);

        String message = String.format(Locale.ENGLISH, "GFORCE VAL: %.3f", acc);

        build.setAutoCancel(false);
        build.setWhen(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            build.setStyle(new Notification.BigTextStyle().bigText(message));
        }
        build.setTicker(getString(R.string.app_name));
        build.setContentTitle("Incident/impact alert");
        build.setContentText(message);
        build.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_web));
        build.setSmallIcon(R.drawable.ic_menu_mylocation);
        build.setContentIntent(pendingIntent);
        build.setOngoing(true);
        if (sp.getBoolean("play_impact_sound_pref", false)) //whether to play sound in notification (parameter is configured in settings)
            build.setSound(Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.alert));
        build.build();
        Notification notif = build.getNotification();
//        notif.vibrate = new long[] { 100, 250, 100, 500};
//        notif.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (sp.getBoolean("show_impact_pref", false)) //whether to even launch notification or not (configured via application settings)
            nm.notify(0, notif);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // required method
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
        build.setSmallIcon(R.drawable.ic_settings);
        build.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_web));
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

    public void showNotification(String vitesse, Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Intent intent1 = new Intent(context, MyPositionActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager nm = (NotificationManager)
                context.getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder build = new Notification.Builder(context);

        int nbre = MyPositionActivity.isCurrentTripExist()?SpeedOverhead.find(SpeedOverhead.class, "tripid = ?", ""+ getCurrentTrip().getId()).size():0;
        String message = nbre>0?nbre+context.getString(R.string.speed_overheading):context.getString(R.string.travelr_secure_your_trip);

        build.setAutoCancel(sp.getBoolean("persist_speed_pref", false));
        build.setWhen(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            build.setStyle(new Notification.BigTextStyle().bigText(message));
        }
        build.setTicker(context.getString(R.string.app_name));
        build.setContentTitle(vitesse);
        build.setContentText(message);
        build.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_web));
        build.setSmallIcon(R.drawable.ic_menu_mylocation);
        build.setContentIntent(pendingIntent);
        build.setOngoing(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            build.build();
        }

        Notification notif = build.getNotification();

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
    private Location location;

    private long updateSpeed(Location location, long duration){
        this.location = location;

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

        String displayedSpeed = vitesse >= MAX_VITESSE_METRE_SECONDE ? +Tutility.round(vitesse * COEFF_CONVERSION_MS_KMH) + " KM/H" : Tutility.round(vitesse) + " M/S";

        showNotification(displayedSpeed, getApplicationContext());

        editor.putFloat(TConstants.SPEED_PREF, vitesse);
        editor.commit();

        //Log.e(LOGTAG, "new speed received and injected: "+vitesse);

       if(MyPositionActivity.isCurrentTripExist()){
           Trip mtrip = MyPositionActivity.getCurrentTrip();
           if(((vitesse * COEFF_CONVERSION_MS_KMH) -ERREUR_ACCEPTE_VITESSE_MAX> MAX_SPEED_ALLOWED_KMH)){

               pushSpeedOnline(SpeedMeterService.this, Tutility.round(vitesse * COEFF_CONVERSION_MS_KMH), location, mtrip);

           }
           else if((lastUpdate==null||System.currentTimeMillis()-lastUpdate>INTERVAL_BETWEEN_UPDATES)){
               pushSpeedOnline(SpeedMeterService.this, Tutility.round(vitesse * COEFF_CONVERSION_MS_KMH), location, mtrip);
               lastUpdate = System.currentTimeMillis();
           }

       }

        //tryToSentDataOnline(getApplicationContext());

        return  duration;
    }

    /*private static MultiValueMap<String, String> body ;
    private static ResponseEntity<String> response;
    private static TrackingData trackingDataa;
    private static RestTemplate restTemplate;*/
    private static  SpeedOverhead so = null;

    private void pushSpeedOnline(final Context context, final double vitesse, @NotNull final Location location, @NotNull final Trip trip) {
        //push speed to firebase
        long timestamp = System.currentTimeMillis();
        TrackingData data = new TrackingData();
        data.setTimestamp(timestamp);
        data.setLatitude(location.getLatitude());
        data.setLongitude(location.getLongitude());
        data.setSpeed(vitesse );
        data.setTrackingMatricule(trip.getBus_immatriculation());
        data.setSender(travelerUser.getUserphone());
        data.setBearing(0f);
        data.setTemperature(0f);

        data.setAzimuth(orientationDegree[0]);
        data.setPitch(orientationDegree[1]);
        data.setRoll(orientationDegree[2]);

        databaseReference.child(trip.getBus_immatriculation())
                .child(trip.getTripKey())
                .child(TConstants.FIREBASE_DATA)
                .push()
                .setValue(data);

        if((vitesse *COEFF_CONVERSION_MS_KMH) -ERREUR_ACCEPTE_VITESSE_MAX> MAX_SPEED_TO_ALERT_KMH) {
            if (!hasReachLimit) {

                if(durationElapsed==null){
                    durationElapsed = System.currentTimeMillis();

                    so = new SpeedOverhead();
                    so.setDate_start(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US).format(Calendar.getInstance().getTime()));
                    so.setLatitude_start(location.getLatitude());
                    so.setLongitude_start(location.getLongitude());
                    so.setSpeed_start(vitesse);
                    so.setTripid("" + MyPositionActivity.getCurrentTrip().getId());
                }
                else if(System.currentTimeMillis()>=durationElapsed+TIME_TO_WAIT_FOR_SPEED_OVERHEAD_CONFIRMATION){

                    hasReachLimit = true;
                }
            }



        }else {
            if(hasReachLimit) {

                so.setLatitude_end(location.getLatitude());
                so.setLongitude_end(location.getLongitude());
                so.setSpeed_end(vitesse);
                so.setDate_end(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US).format(Calendar.getInstance().getTime()));
                so.save();

                hasReachLimit = false;
                //speak out loud high speed values if set in the settings
                boolean speakOut = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("high_speed_pref", false);
                if (speakOut ){
                    tts.speak(getString(R.string.highspeed, Tutility.round(vitesse * COEFF_CONVERSION_MS_KMH) ),
                            TextToSpeech.QUEUE_ADD, null);
                }
            }
            durationElapsed = null;
        }
        /*
        new AsyncTask<Void, Void, ResponsStatusMsgMeta>(){
            private long timestamp = System.currentTimeMillis();
            @Override
            protected ResponsStatusMsgMeta doInBackground(Void... params) {
                try {
                    // HttpAuthentication httpAuthentication = new HttpBasicAuthentication("username", "password");
                    //Create the request body as a MultiValueMap
                    SharedPreferences sharedPreferences = context.getSharedPreferences(TConstants.TRAVELR_PREFERENCE, MODE_PRIVATE);
                    if(body==null){
                        body= new LinkedMultiValueMap<>();
                    }
                    else{
                        body.clear();
                    }
                    body.add(TConstants.POST_SPEED_AND_POSITION_PARAM_LAT, String.valueOf(location.getLatitude()));
                    body.add(TConstants.POST_SPEED_AND_POSITION_PARAM_LNG, String.valueOf(location.getLongitude()));
                    body.add(TConstants.POST_SPEED_AND_POSITION_PARAM_SPEED, String.valueOf(vitesse));
                    body.add(TConstants.POST_SPEED_AND_POSITION_PARAM_PHONE, sharedPreferences.getString(TConstants.PREF_PHONE, "000000000"));
                    body.add(TConstants.POST_SPEED_AND_POSITION_PARAM_MATRICULE, MyPositionActivity.getCurrentTrip().getBus_immatriculation());
                    body.add(TConstants.POST_SPEED_AND_POSITION_PARAM_MAT_ID, sharedPreferences.getString(MyPositionActivity.getCurrentTrip().getBus_immatriculation(),
                            sharedPreferences.getString(TConstants.PREF_MAT_ID, "0")));
                    body.add(TConstants.POST_SPEED_POSTION_PARAM_TIMESTAMP, String.valueOf(trackingData == null? timestamp:trackingData.getTimestamp()));


                    if(restTemplate==null){
                        restTemplate = new RestTemplate(true);
                    }

                    response = restTemplate.exchange(TConstants.POST_SPEED_AND_POSITION_URL, HttpMethod.POST, new HttpEntity<Object>(body, new HttpHeaders()), String.class);
                    Log.e(LOGTAG, "res: "+response);
                    Log.e(LOGTAG, "body "+response.getBody());

                    return new Gson().fromJson(response.getBody(), ResponsStatusMsgMeta.class);
                } catch (Exception e) {
                    Log.e(LOGTAG, e.getMessage(), e);
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
                                so.setTripid(String.valueOf(MyPositionActivity.getCurrentTrip().getId()));
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
                        trackingDataa.setTrackingMatricule(context.getSharedPreferences(TConstants.TRAVELR_PREFERENCE, MODE_PRIVATE)
                                .getString(TConstants.PREF_MAT_ID, "0"));
                        trackingDataa.setLatitude(location.getLatitude());
                        trackingDataa.setLongitude(location.getLongitude());
                        trackingDataa.setLocation(""); //je sais pas si c'est possible d'avoir le nom de l'endroit ou ces donnees ont ete recuperer
                        trackingDataa.setSpeed(vitesse);
                        trackingDataa.setTimestamp(timestamp);
                        trackingDataa.save();
                    }

                }
                else{

                    if(trackingData!=null){
                        trackingData.delete();

                        SharedPreferences.Editor editor = context.getSharedPreferences(TConstants.TRAVELR_PREFERENCE, MODE_PRIVATE).edit();
                        editor.putString(trackingData.getTrackingMatricule(), String.valueOf(response.getMeta().getMatricule_id())).apply();

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
                        context.getSharedPreferences(TConstants.TRAVELR_PREFERENCE, MODE_PRIVATE)
                                .edit().putString(MyPositionActivity.getCurrentTrip().getBus_immatriculation(), response.getMeta().getMatricule_id()+"")
                                .apply();
                    }

                    tryToSentDataOnline(context);
                }

            }
        }.execute();
        */
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
        if (tts != null){
            tts.shutdown();
        }

    }

    @Override
    public void onFailure(@NonNull Exception e) {
        //Send impact notifications via SMS when offline and sending alerts fail
        e.printStackTrace();
        Trip currentTrip = getCurrentTrip();
        String emergencyMessage = getResources().getString(R.string.emergency_sms,
                mAccelCurrent,
                mAccelLast,
                previousLocation == null ? 0 : previousLocation.getLatitude(),
                previousLocation == null ? 0 : previousLocation.getLongitude() ,
                lastSpeed,
                System.currentTimeMillis(),
                currentTrip == null ? "Unknown" : currentTrip.getBus_immatriculation(),
                currentTrip == null ? "Unknown" : currentTrip.getAgency_name(),
                orientationDegree[0],
                orientationDegree[1],
                orientationDegree[2],
                currentTrip == null ? "" : currentTrip.getTripKey(),
                0);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){
            //send SMS peacefully
            SmsManager smsManager = SmsManager.getDefault();

            smsManager.sendTextMessage(Tutility.APP_EMERGENCY_CONTACT, null,
                    emergencyMessage, null ,null);
            Log.d(LOGTAG, emergencyMessage);
        }else{
            //send broadcast to issue permission request for user to grant permission
            Intent dataIntent = new Intent(Tutility.BROADCAST_SMS_EMERGENCY);
            dataIntent.putExtra("message", emergencyMessage);
            dataIntent.putExtra("src", travelerUser.getUserphone());

            sendBroadcast(dataIntent, Manifest.permission.SEND_SMS);
        }

    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {

    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d(LOGTAG, "Beginning speech recognition");
    }

    @Override
    public void onRmsChanged(float v) {

    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {
        speechRecognizer.stopListening();
    }

    @Override
    public void onError(int i) {
        String error = "";
        switch (i){
            case SpeechRecognizer.ERROR_AUDIO:
                error = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                error = "insufficient permission error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                error = "Client error!";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                error = "network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                error = "Network Timeout Error";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                error = "No Match Error";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                error = "Recognizer busy error!";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                error = "Server Error!";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                error = "Speech timeout error!";
                break;
        }
        Log.d(LOGTAG, error);
    }

    @Override
    public void onResults(Bundle bundle) {
        ArrayList<String> recognizedList = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        Log.d(LOGTAG, "Result: "+recognizedList.get(0));
    }

    @Override
    public void onPartialResults(Bundle bundle) {
        ArrayList<String> recognizedList = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        Log.d(LOGTAG, "Partial Result: "+recognizedList.get(0));
    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }

    @Override
    public void onInit(int i) {
        //text to speech initialization listener
        switch (i){
            case TextToSpeech.ERROR:
                Log.d(LOGTAG, "Error initializing speech engine");
                break;
            case TextToSpeech.SUCCESS:
                String language = PreferenceManager.getDefaultSharedPreferences(this).getString("language_pref", "");
                tts.setLanguage( language.equalsIgnoreCase("English") ? Locale.ENGLISH :
                        language.equalsIgnoreCase("French") ? Locale.FRENCH : Locale.getDefault());
                tts.setPitch(2);
                break;
        }
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {

            }

            @Override
            public void onDone(String s) {
                Log.d(LOGTAG, s);
                tts.speak(s, TextToSpeech.QUEUE_ADD, null);
                tts.stop();
            }

            @Override
            public void onError(String s) {
                Log.d(LOGTAG, s);
            }
        });
    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if (!task.isSuccessful()){
            //act offline
            try {
                onFailure(task.getException());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
