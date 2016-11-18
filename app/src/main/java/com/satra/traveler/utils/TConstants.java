package com.satra.traveler.utils;

/**
 * Created by Larry Akah on 6/11/16.
 * This class contains all referenced constant values in the app
 */
public class TConstants {

    public static final String TRAVELR_PREFERENCE= "traveler_prefs";
    public static final String PREF_USERNAME = "username";
    public static final String PREF_PHONE = "telephone";
    public static final String SPEED_PREF = "vitesse";
    public static final String PREF_MATRICULE = "MATRICULE";
    public static final String PREF_MAT_ID = "MAT_ID";
    public static final String PREF_EMERGENCY_CONTACT_1 = "EMERGENCY_CONTACT_1";
    public static final String PREF_EMERGENCY_CONTACT_2 = "EMERGENCY_CONTACT_2";

    public static final String PREF_FROM_1 = "FROM_1";
    public static final String PREF_FROM_2 = "FROM_2";
    public static final String PREF_FROM_3 = "FROM_3";
    public static final String PREF_FROM_4 = "FROM_4";
    public static final String PREF_FROM_5 = "FROM_5";
    public static final String PREF_FROM_6 = "FROM_6";


    public static final String PREF_TO_1 = "TO_1";
    public static final String PREF_TO_2 = "TO_2";
    public static final String PREF_TO_3 = "TO_3";
    public static final String PREF_TO_4 = "TO_4";
    public static final String PREF_TO_5 = "TO_5";
    public static final String PREF_TO_6 = "TO_6";



    public static final String REGISTRATION_URL="http://travelr.iceteck.com/index.php/home/matricule/add";
    public static final String REGISTRATION_URL_PARAM_CODE="code";
    public static final String REGISTRATION_URL_PARAM_MSISDN="msisdn";
    public static final String REGISTRATION_URL_PARAM_USERNAME="username";
    public static final String REGISTRATION_URL_PARAM_EMERGENCY_ONE="emergency_one";
    public static final String REGISTRATION_URL_PARAM_EMERGENCY_TWO="emergency_two";
    public static final String GET_MAT_ID_URL="http://travelr.iceteck.com/index.php/home/matricule/get/";
    public static final String POST_SPEED_AND_POSITION_PARAM_MAT_ID="mat_id";
    public static final String POST_SPEED_AND_POSITION_PARAM_LNG="lng";
    public static final String POST_SPEED_AND_POSITION_PARAM_LAT="lat";
    public static final String POST_SPEED_AND_POSITION_PARAM_SPEED="speed";
    public static final String POST_SPEED_AND_POSITION_PARAM_MATRICULE="matricule";
    public static final String POST_SPEED_AND_POSITION_URL="http://travelr.iceteck.com/index.php/home/matricule/data/add";
    public static final String POST_MESSAGE_PARAM_MAT_ID="mat_id";
    public static final String POST_MESSAGE_PARAM_MSISDN="msisdn";
    public static final String POST_MESSAGE_PARAM_MATRICULE="matricule";
    public static final String POST_MESSAGE_PARAM_USERNAME="username";
    public static final String POST_MESSAGE_PARAM_MESSAGE="message";
    public static final String POST_SPEED_POSTION_PARAM_TIMESTAMP = "timestamp";
    public static final String POST_MESSAGE_URL="http://travelr.iceteck.com/index.php/home/matricule/message/add";


    public static final int GEOFENCE_RADIUS_IN_METERS = 1000;
    public static final int GEOFENCE_EXPIRATION_IN_MILLISECONDS = 7*24*3600*1000;
    public static final int GEOFENCE_DWELL_DELAY_IN_MILLISECONDS = 30*60*1000;
}
