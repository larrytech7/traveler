package com.satra.traveler;

import android.app.Activity;
import android.content.Intent;
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

import org.jetbrains.annotations.NotNull;

/**
 * Created by Steve Jeff on 16/02/2016.
 */
public class NavigationItemListener implements NavigationView.OnNavigationItemSelectedListener {
    final static int DIALOG_NEW_COMPLAINT = 3;
    final static int DIALOG_NEW_JOURNEY = 4;
    final static int DIALOG_NEW_INSURANCE = 5;
    private Activity context;
    private MyPositionActivity activity;

    void setActivity(MyPositionActivity activity) {
        this.activity = activity;
    }

    NavigationItemListener(Activity context){
        this.context = context;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

       switch (id) {
           case (R.id.nav_repport_complaint): {
               Intent msgIntent = new Intent(context, MessagingActivity.class);
               String matricule = PreferenceManager.getDefaultSharedPreferences(context).getString(TConstants.PREF_MATRICULE, "");
               msgIntent.putExtra(TConstants.PREF_MATRICULE, matricule);
               context.startActivity(msgIntent);
//            context.showDialog(DIALOG_NEW_COMPLAINT);
           } break;
           case R.id.nav_new_journey: {
               if (!MyPositionActivity.isCurrentTripExist()) {
                   context.showDialog(DIALOG_NEW_JOURNEY);
               } else {
                   Snackbar.make(context.findViewById(R.id.drawer_layout), R.string.current_trip_already_exist, Snackbar.LENGTH_LONG)
                           .setAction(context.getString(R.string.end_journey), new View.OnClickListener() {
                               @Override
                               public void onClick(View v) {
                                   endJourney(MyPositionActivity.getCurrentTrip());
                               }
                           })
                           .show();
               }
           } break;
           case (R.id.nav_end_journey): {
               if (MyPositionActivity.isCurrentTripExist()) {
                   endJourney(MyPositionActivity.getCurrentTrip());
               } else {
                   Snackbar.make(context.findViewById(R.id.drawer_layout), R.string.no_current_trip_defined, Snackbar.LENGTH_LONG)
                           .setAction(context.getString(R.string.confirm_journey), new View.OnClickListener() {
                               @Override
                               public void onClick(View v) {
                                   context.showDialog(DIALOG_NEW_JOURNEY);
                               }
                           })
                           .show();
               }

           }break;
           case (R.id.nav_cancel_journey): {
               if (MyPositionActivity.isCurrentTripExist()) {
                   cancelJourney(MyPositionActivity.getCurrentTrip());
               } else {
                   Snackbar.make(context.findViewById(R.id.drawer_layout), R.string.no_current_trip_defined, Snackbar.LENGTH_LONG)
                           .setAction(context.getString(R.string.confirm_journey), new View.OnClickListener() {
                               @Override
                               public void onClick(View v) {
                                   context.showDialog(DIALOG_NEW_JOURNEY);
                               }
                           })
                           .show();
               }
           }break;
           case (R.id.nav_aid_victim): {
               //start activity to show victim aid
               context.startActivity(new Intent(context, AidActivity.class));
           }break;
           case (R.id.nav_transport_victim): {
               //start activity to show transportation for victim
               context.startActivity(new Intent(context, VictimeTransportationActivity.class));
           }break;
           case R.id.nav_share:
               //TODO: share
               Intent shareIntent = new Intent(Intent.ACTION_SEND);
               shareIntent.setType("text/plain");
               shareIntent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_app_text));
               context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_app)));
               break;

          /*
          case R.id.nav_settings:
               //TODO: launch screen with simple settings
               break;*/
           default:
               break;
       }
        DrawerLayout drawer = (DrawerLayout) context.findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void endJourney(@NotNull  Trip trip){
        //update trip to status of finished
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

    private void cancelJourney(@NotNull Trip trip){
            //updated the status of teh current trip to completed
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
