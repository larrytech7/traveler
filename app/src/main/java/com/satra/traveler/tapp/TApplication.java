package com.satra.traveler.tapp;

import android.content.Context;
import android.support.multidex.MultiDex;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.orm.SugarApp;
import com.orm.SugarContext;

/**
 * Created by Larry Akah on 6/11/16.
 */
public class TApplication extends SugarApp {

    @Override
    public void onCreate() {
        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        SugarContext.init(this);
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        SugarContext.terminate();
        super.onTerminate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


}
