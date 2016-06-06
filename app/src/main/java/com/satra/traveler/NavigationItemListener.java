package com.satra.traveler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;

/**
 * Created by Steve Jeff on 16/02/2016.
 */
public class NavigationItemListener implements NavigationView.OnNavigationItemSelectedListener {
    private Activity context;
    final public static int DIALOG_NEW_COMPLAINT = 3;
    final public static int DIALOG_NEW_JOURNEY = 4;

    public NavigationItemListener(Activity context){
        this.context = context;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_ma_position) {
            context.startActivity(new Intent(context, MyPositionActivity.class));
            context.finish();
        } else if (id == R.id.nav_repport_complaint) {
            context.showDialog(DIALOG_NEW_COMPLAINT);
        }
        /*else if (id == R.id.nav_find_taxi) {
            try {
                startNewActivity(context, "com.polytech.taxigetme");
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("fail taxigetme-traveler", "nature erreur " + e.getMessage());
                Uri marketUri = Uri.parse("market://details?id=com.polytech.taxigetme");
                Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
                context.startActivity(marketIntent);

            }
        }*/
        else if (id == R.id.nav_new_journey) {
            context.showDialog(DIALOG_NEW_JOURNEY);
        } else if (id == R.id.nav_confirm_journey) {

        } else if (id == R.id.nav_end_journey) {

        }
        else if (id == R.id.nav_upadate_journey) {

        }
        else if (id == R.id.nav_cancel_journey) {

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
