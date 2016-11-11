package com.satra.traveler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.location.LocationServices;
import com.satra.traveler.models.Trip;
import com.satra.traveler.utils.TConstants;
import com.satra.traveler.utils.Tutility;

import java.util.List;

/**
 * Created by Steve Jeff on 16/02/2016.
 */
public class NavigationItemListener implements NavigationView.OnNavigationItemSelectedListener {
    final public static int DIALOG_NEW_COMPLAINT = 3;
    final public static int DIALOG_NEW_JOURNEY = 4;
    private Activity context;
    private MyPositionActivity activity;

    public MyPositionActivity getActivity() {
        return activity;
    }

    public void setActivity(MyPositionActivity activity) {
        this.activity = activity;
    }

    public NavigationItemListener(Activity context){
        this.context = context;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

       /* if (id == R.id.nav_ma_position) {
            Intent mainIntent = new Intent(context, MyPositionActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(mainIntent);
        } else */if (id == R.id.nav_repport_complaint) {
            Intent msgIntent = new Intent(context, MessagingActivity.class);
            String matricule = PreferenceManager.getDefaultSharedPreferences(context).getString(TConstants.PREF_MATRICULE,"");
            msgIntent.putExtra(TConstants.PREF_MATRICULE,matricule);
            context.startActivity(msgIntent);
//            context.showDialog(DIALOG_NEW_COMPLAINT);
        }
        else if (id == R.id.nav_new_journey) {
            if(!MyPositionActivity.isCurrentTripExist()){
                context.showDialog(DIALOG_NEW_JOURNEY);
            }
            else{
                Snackbar.make(context.findViewById(R.id.drawer_layout), R.string.current_trip_already_exist, Snackbar.LENGTH_LONG)
                        .setAction(context.getString(R.string.end_journey), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                endJourney();
                            }
                        })
                        .show();
            }
        } else if (id == R.id.nav_end_journey) {
            if(MyPositionActivity.isCurrentTripExist()){
                endJourney();
            }
            else{
                Snackbar.make(context.findViewById(R.id.drawer_layout), R.string.no_current_trip_defined, Snackbar.LENGTH_LONG)
                        .setAction(context.getString(R.string.confirm_journey), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                context.showDialog(DIALOG_NEW_JOURNEY);
                            }
                        })
                        .show();
            }

        }/* else if (id == R.id.nav_end_journey) {

        }
        else if (id == R.id.nav_upadate_journey) {

        }*/
        else if (id == R.id.nav_cancel_journey) {
            if(MyPositionActivity.isCurrentTripExist()){
                cancelJourney();
            }
            else{
                Snackbar.make(context.findViewById(R.id.drawer_layout), R.string.no_current_trip_defined, Snackbar.LENGTH_LONG)
                        .setAction(context.getString(R.string.confirm_journey), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                context.showDialog(DIALOG_NEW_JOURNEY);
                            }
                        })
                        .show();
            }
        }else if (id == R.id.nav_aid_victim){
            //start activity to show victim aid
            context.startActivity(new Intent(context, AidActivity.class));
        }else if (id == R.id.nav_transport_victim){
            //start activity to show transportation for victim
            context.startActivity(new Intent(context, VictimeTransportationActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) context.findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void endJourney(){
        List<Trip> trips = Trip.listAll(Trip.class, "tid");//Trip.last(Trip.class);
        // Confirm/complete a trip
        if (trips != null && trips.size() > 0){
            Trip trip = trips.get(trips.size() - 1);
            trip.status = 1;
            long updateid = trip.save();
            if (updateid > 0){
                Tutility.showMessage(context, R.string.complete_trip, R.string.complete_trip_title );
                activity.clearMap();
            }else{
                Tutility.showMessage(context, R.string.complete_trip_error, R.string.complete_trip_error_title );
            }

            LocationServices.GeofencingApi.removeGeofences(
                    activity.getGoogleApiClient(),
                    // This is the same pending intent that was used in addGeofences().
                    activity.getGeofencePendingIntent()
            ).setResultCallback(activity); // Result processed in onResult().

        }
    }

    public void cancelJourney(){
        List<Trip> trips = Trip.listAll(Trip.class, "tid");//Trip.last(Trip.class);
        // Confirm/complete a trip
        if (trips != null && trips.size() > 0){
            Trip trip = trips.get(trips.size() - 1);
            trip.status = 2;
            long updateid = trip.save();
            if (updateid > 0){
                Tutility.showMessage(context, R.string.cencel_trip, R.string.cencel_trip_title );
                activity.clearMap();
            }else{
                Tutility.showMessage(context, R.string.complete_trip_error, R.string.complete_trip_error_title );
            }

            LocationServices.GeofencingApi.removeGeofences(
                    activity.getGoogleApiClient(),
                    // This is the same pending intent that was used in addGeofences().
                    activity.getGeofencePendingIntent()
            ).setResultCallback(activity); // Result processed in onResult().

        }
    }

    public void startNewActivity(Context context, String packageName) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null) {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + packageName));
        }
        context.startActivity(intent);
    }
}
