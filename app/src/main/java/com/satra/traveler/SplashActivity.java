package com.satra.traveler;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.satra.traveler.utils.TConstants;
import com.satra.traveler.utils.Tutility;
import com.stephentuso.welcome.WelcomeHelper;

import static android.R.attr.data;

public class SplashActivity extends AppCompatActivity {

    private WelcomeHelper welcomeHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //TODO: Decide whether or not to create and show splash
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean showSplash = sp.getBoolean(TConstants.PREFERENCE_SPLASH, true);
        if (!showSplash) {
            startNextActivity();
        } else {
            welcomeHelper = new WelcomeHelper(this, IntroActivity.class);
            welcomeHelper.show(savedInstanceState);
            sp.edit().putBoolean(TConstants.PREFERENCE_SPLASH, false).apply();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        welcomeHelper.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == WelcomeHelper.DEFAULT_WELCOME_SCREEN_REQUEST) {
            //whatever the case launch Signup Activity
            startNextActivity();
        }

    }

    private void startNextActivity(){
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
