package com.satra.traveler;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.satra.traveler.models.Rewards;
import com.satra.traveler.utils.TConstants;
import com.satra.traveler.utils.TpointsListener;
import com.satra.traveler.utils.Tutility;

import cn.pedant.SweetAlert.SweetAlertDialog;
import ernestoyaquello.com.verticalstepperform.VerticalStepperFormLayout;
import ernestoyaquello.com.verticalstepperform.interfaces.VerticalStepperForm;

public class AidActivity extends AppCompatActivity implements VerticalStepperForm, TpointsListener {

    private VerticalStepperFormLayout verticalStepperForm;
    private static final int steps = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aid);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int colorPrimary = ContextCompat.getColor(getApplicationContext(), R.color.bluePrimary);
        int colorPrimaryDark = ContextCompat.getColor(getApplicationContext(), R.color.black);

        // Finding the view
        verticalStepperForm = (VerticalStepperFormLayout) findViewById(R.id.vertical_stepper_form);

        //subtitles
        String[] titles = getResources().getStringArray(R.array.numnsteps);
        String[] subs = getResources().getStringArray(R.array.aid_tips);
        // Setting up and initializing the form
        VerticalStepperFormLayout.Builder.newInstance(verticalStepperForm, titles, this, this)
                .primaryColor(colorPrimary)
                .primaryDarkColor(colorPrimaryDark)
                .stepsSubtitles(subs)
                .stepTitleTextColor(colorPrimary)
                .stepNumberTextColor(colorPrimaryDark)
                .init();
    }

    @Override
    public View createStepContentView(int stepNumber) {
        return createStep(stepNumber);
    }

    private View createStep(int stepNumber) {
        View view;
        LayoutInflater layoutInflater = LayoutInflater.from(getBaseContext());
        view = layoutInflater.inflate(R.layout.first_aid_item_layout, null,false);
        switch (stepNumber) {
            case 0:
                ((ImageView) view.findViewById(R.id.imageViewStep1)).setImageResource(R.drawable.self);
                ((TextView)view.findViewById(R.id.step1TextView)).setText(getString(R.string.checkself));
                break;
            case 1:
                ((ImageView) view.findViewById(R.id.imageViewStep1)).setImageResource(R.drawable.injury);
                ((TextView)view.findViewById(R.id.step1TextView)).setText(getString(R.string.checkinjury));
                break;
            case 2:
                ((ImageView) view.findViewById(R.id.imageViewStep1)).setImageResource(R.drawable.signbreath);
                ((TextView)view.findViewById(R.id.step1TextView)).setText(getString(R.string.breathsigns));
                break;
            case 3:
                ((ImageView) view.findViewById(R.id.imageViewStep1)).setImageResource(R.drawable.help);
                ((TextView)view.findViewById(R.id.step1TextView)).setText(getString(R.string.callhelp));
                break;
            case 4:
                ((ImageView) view.findViewById(R.id.imageViewStep1)).setImageResource(R.drawable.obstruction);
                ((TextView)view.findViewById(R.id.step1TextView)).setText(getString(R.string.checkobstruction));
                break;
            case 5:
                ((ImageView) view.findViewById(R.id.imageViewStep1)).setImageResource(R.drawable.lifesaving1);
                try {
                    ((ImageView) view.findViewById(R.id.imageViewStep2)).setImageResource(R.drawable.lifesaving2);
                    ((ImageView) view.findViewById(R.id.imageViewStep3)).setImageResource(R.drawable.lifesaving3);
                } catch (OutOfMemoryError | Exception e) {
                    e.printStackTrace();
                }
                ((TextView)view.findViewById(R.id.step1TextView)).setText(getString(R.string.lifesaving));
                break;
            case 6:
                try {
                    ((ImageView) view.findViewById(R.id.imageViewStep1)).setImageResource(R.drawable.vomitting);
                } catch (OutOfMemoryError|Exception e) {
                    e.printStackTrace();
                }
                ((TextView)view.findViewById(R.id.step1TextView)).setText(getString(R.string.severecondition));
                break;
            case 7:
                try {
                    ((ImageView) view.findViewById(R.id.imageViewStep1)).setImageResource(R.drawable.wounds);
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                }
                ((TextView)view.findViewById(R.id.step1TextView)).setText(getString(R.string.openwounds));
                break;
            case 8:
                try {
                    ((ImageView) view.findViewById(R.id.imageViewStep1)).setImageResource(R.drawable.spinalinjury1);
                    ((ImageView) view.findViewById(R.id.imageViewStep2)).setImageResource(R.drawable.spinal2);
                } catch (OutOfMemoryError | Exception e) {
                    e.printStackTrace();
                }
                ((TextView)view.findViewById(R.id.step1TextView)).setText(getString(R.string.spinalinjury));
                break;
            case 9:
                try {
                    ((ImageView) view.findViewById(R.id.imageViewStep1)).setImageResource(R.drawable.warm);
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                }
                ((TextView)view.findViewById(R.id.step1TextView)).setText(getString(R.string.warmness));
                break;
            case 10:
                //((ImageView) view.findViewById(R.id.imageViewStep1)).setImageResource(R.drawable.victim_m5);
                ((TextView)view.findViewById(R.id.step1TextView)).setText(getString(R.string.feedingvictim));
                break;

        }
        return view;
    }

    @Override
    public void onStepOpening(int stepNumber) {
        verticalStepperForm.setActiveStepAsCompleted();
    }

    @Override
    public void sendData() {
        verticalStepperForm.goToStep(0, false);
        //Attribute points at this stage if necessary
        boolean tpointsGiven = isTpointsUpdated(null);

        if (tpointsGiven) {
            String content = getString(R.string.travel_rewards_point, TConstants.MED_REWARDS);
            Tutility.showDialog(this, getString(R.string.rewards_title), content, SweetAlertDialog.CUSTOM_IMAGE_TYPE);
        }
    }

    @Nullable
    @Override
    public Intent getSupportParentActivityIntent() {
        return super.getSupportParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    @Override
    public void onBackPressed() {
        try {
            super.onBackPressed();
            if (verticalStepperForm != null)
                verticalStepperForm = null;
        }catch (OutOfMemoryError ofMemoryError){
            ofMemoryError.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (verticalStepperForm != null)
            verticalStepperForm = null;
    }

    /**
     * If this activity is completed the first time and for every 24 hours, user gets 5 Tpoints
     * @param c object criteria
     * @return whether or not user got tPoints
     */
    @Override
    public boolean isTpointsUpdated(@Nullable Object c) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        long now = System.nanoTime();
        long startTime = sp.getLong(TConstants.AID_ACTIVITY_NAVIGATION_START_TIME, 0);

        if( (now - startTime) >= (24 * 60 * 60 * Math.pow(10,9))){
            sp.edit().putLong(TConstants.AID_ACTIVITY_NAVIGATION_START_TIME, now).apply();
            Rewards rewards = Tutility.getAppRewards();
            rewards.setAppOfflineGuides(5);
            rewards.save();
            return true;
        }
        return false;
    }
}
