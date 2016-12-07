package com.satra.traveler.utils;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import com.satra.traveler.R;

/**
 * Created by Larry Akah on 6/11/16.
 * Travelr Utility Class contains global utility functions used within the app
 */
public class Tutility {

    public static final String FIREBASE_USER = "users";

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
    public static double round(double c){
        return Math.round(c*100)/100.0;
    }
}
