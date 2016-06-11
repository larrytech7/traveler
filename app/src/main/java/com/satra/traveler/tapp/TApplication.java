package com.satra.traveler.tapp;

import com.orm.SugarApp;
import com.orm.SugarContext;

/**
 * Created by Larry Akah on 6/11/16.
 */
public class TApplication extends SugarApp {

    @Override
    public void onCreate() {
        SugarContext.init(this);
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        SugarContext.terminate();
        super.onTerminate();
    }

}
