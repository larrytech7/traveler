package com.satra.traveler;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.stephentuso.welcome.WelcomeHelper;

public class SplashActivity extends AppCompatActivity {

    private WelcomeHelper welcomeHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //TODO: Decide whether or not to create and show splash
        welcomeHelper = new WelcomeHelper(this, IntroActivity.class);
        welcomeHelper.show(savedInstanceState);
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
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

    }
}
