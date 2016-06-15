package com.satra.traveler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import com.satra.traveler.models.Trip;
import com.satra.traveler.utils.Tutility;

import java.util.List;

/**
 * Created by Steve Jeff on 16/02/2016.
 */
public class NavigationItemListener implements NavigationView.OnNavigationItemSelectedListener {
    final public static int DIALOG_NEW_COMPLAINT = 3;
    final public static int DIALOG_NEW_JOURNEY = 4;
    private Activity context;

    public NavigationItemListener(Activity context){
        this.context = context;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_ma_position) {
            Intent mainIntent = new Intent(context, MyPositionActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(mainIntent);
        } else if (id == R.id.nav_repport_complaint) {
            context.showDialog(DIALOG_NEW_COMPLAINT);
        }
        else if (id == R.id.nav_new_journey) {
            context.showDialog(DIALOG_NEW_JOURNEY);
        } else if (id == R.id.nav_end_journey) {
            List<Trip> trips = Trip.listAll(Trip.class, "tid");//Trip.last(Trip.class);
            // Confirm/complete a trip
            if (trips != null && trips.size() > 0){
                Trip trip = trips.get(trips.size() - 1);
                trip.status = 1;
                long updateid = trip.save();
                if (updateid > 0){
                    Tutility.showMessage(context, R.string.complete_trip, R.string.complete_trip_title );
                }else{
                    Tutility.showMessage(context, R.string.complete_trip_error, R.string.complete_trip_error_title );
                }
            }
        }/* else if (id == R.id.nav_end_journey) {

        }
        else if (id == R.id.nav_upadate_journey) {

        }*/
        else if (id == R.id.nav_cancel_journey) {
            List<Trip> trips = Trip.listAll(Trip.class, "tid");//Trip.last(Trip.class);
            // Confirm/complete a trip
            if (trips != null && trips.size() > 0){
                Trip trip = trips.get(trips.size() - 1);
                trip.status = 2;
                long updateid = trip.save();
                if (updateid > 0){
                    Tutility.showMessage(context, R.string.cencel_trip, R.string.cencel_trip_title );
                }else{
                    Tutility.showMessage(context, R.string.complete_trip_error, R.string.complete_trip_error_title );
                }
            }
        }

        DrawerLayout drawer = (DrawerLayout) context.findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
