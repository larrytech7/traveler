package com.satra.traveler.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;

import com.satra.traveler.R;
import com.satra.traveler.models.Rewards;
import com.satra.traveler.models.User;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Larry Akah on 6/11/16.
 * Travelr Utility Class contains global utility functions used within the app
 */
public class Tutility {

    public static final String FIREBASE_USER = "users";
    public static final String FIREBASE_MESSAGES = "messages";
    public static final String FIREBASE_TRIPS = "trips";
    public static final String SHOW_HINTS = "app_hints";
    public static final String BROADCAST_SMS_EMERGENCY = "com.satra.traveler.SEND_EMERGENCY_SMS";

    public static final String APP_EMERGENCY_CONTACT = "";
    public static final String FIREBASE_FLAGS = "flags";
    public static final String FLAG_EVENT = "FLAG_NEW_INCIDENT_EVENT";
    public static final String ANALYTICS_EVENT_ID = "EVENT_ID";
    public static final String ANALYTICS_EVENT_NAME = "EVENT_NAME";
    public static final String ANALYTICS_EVENT_CATEGORY = "EVENT_CATEGORY";

    public static final String APP_EMERGENCY_CONTACT = "+237698592004";

    //show a dialog to the user about a certain message/information
    public static void showMessage(Context c, String message, String title){
        AlertDialog alertDialog = new AlertDialog
                .Builder(c)
                .setTitle(title)
                .setMessage(message)
                .setIcon(R.mipmap.ic_launcher)
                .create();
        alertDialog.show();
    }

    /**
     * same method as above but with a different signature to reference resources by id than use the actual string
     * @param c Application/Activity context
     * @param message_id Resource id of the String to use in the message body
     * @param title_id Resource id of the String to use in the message title of the dialog
     */
    public static void showMessage(Context c, int message_id, int title_id){
        AlertDialog alertDialog = new AlertDialog
                .Builder(c)
                .setTitle(c.getString(title_id))
                .setMessage(c.getString(message_id))
               .setIcon(R.mipmap.ic_launcher)
                .create();
        alertDialog.show();
    }

    /**
     * Format and returns a traveler email for this user to be used for authentication and acount creation
     * @param phone user's phone number to serve as unique feature
     * @return new traveler authenticating email
     */
    public static String getAuthenticationEmail(String phone){
        return phone + TConstants.TRAVELR_EMAIL_EXT;
    }

    /**
     * Return two decimal places of this double, c
     * @param c double to round
     * @return double rounded to two decimal places
     */
    public static double round(double c){
        return Math.round(c*100)/100.0;
    }

    /**
     * Get simple time elapsed and represent as human readable
     * @param c context to fetch string resources
     * @param previousTimestamp previous timestamp in the past
     * @param currentTimeStamp current timestamp
     * @param date string date to return if time lapse is more than 24 hours
     * @return string time for elapsed time since @param previousTimestamp
     */
    public static String getTimeDifference(Context  c, long previousTimestamp, long currentTimeStamp, String date ){
        long diff = Math.abs(currentTimeStamp - previousTimestamp); //avoid negative values however
        if (diff > 86 * Math.pow(10,11))
            return date;
        //now do calculations and round up to nearest second, minute or hour
        double intervalInSeconds = diff / Math.pow(10, 9); //convert to seconds
        if (intervalInSeconds < 60)
            return c.getString(R.string.timeinterval, Math.round(intervalInSeconds), "s"); //time in seconds
        if (intervalInSeconds < 3600)
            return c.getString(R.string.timeinterval, Math.round((intervalInSeconds / 60 )), "m"); //time in minutes
        if (intervalInSeconds > 3600)
            return c.getString(R.string.timeinterval, Math.round((intervalInSeconds / 3600 )), "h"); //time in hours
        return date;
    }

    public static String getMicroTimeString(Context  c, long previousTimestamp, long currentTimeStamp, String date ){
        long diff = Math.abs(currentTimeStamp - previousTimestamp); //avoid negative values however
        if (diff > 864 * Math.pow(10,5))
            return date;
        //now do calculations and round up to nearest second, minute or hour
        double intervalInSeconds = diff / Math.pow(10, 3); //convert to seconds
        if (intervalInSeconds < 60)
            return c.getString(R.string.timeinterval, Math.round(intervalInSeconds), "s"); //time in seconds
        if (intervalInSeconds < 3600)
            return c.getString(R.string.timeinterval, Math.round((intervalInSeconds / 60 )), "m"); //time in minutes
        if (intervalInSeconds > 3600)
            return c.getString(R.string.timeinterval, Math.round((intervalInSeconds / 3600 )), "h"); //time in hours
        return date;
    }

    public static String getTripKeyAsString(String departure, String destination, String date){
        String key = departure+"_"+destination+"_"+date;
        return key.replaceAll(" ", "_");
    }

    public static void showDialog(Context ctx, String title, String content, int dialogType){
        new SweetAlertDialog(ctx, dialogType)
                .setTitleText(title)
                .setContentText(content)
                .setCustomImage(R.mipmap.ic_trophy)
                .show();
    }

    public static Rewards getAppRewards(){
        Rewards reward = Rewards.last(Rewards.class);
        return reward == null? new Rewards() : reward;
    }

    public static User getAppUser() {
        return User.last(User.class);
    }
}
