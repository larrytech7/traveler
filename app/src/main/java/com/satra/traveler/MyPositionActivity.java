package com.satra.traveler;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cardiomood.android.controls.gauge.SpeedometerGauge;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.satra.traveler.models.SpeedOverhead;
import com.satra.traveler.models.Trip;
import com.satra.traveler.models.User;
import com.satra.traveler.utils.TConstants;
import com.satra.traveler.utils.Tutility;

import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mehdi.sakout.fancybuttons.FancyButton;

public class MyPositionActivity extends AppCompatActivity implements OnMapReadyCallback, LocationSource.OnLocationChangedListener
        , GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener,
        ResultCallback {

    private static final int RAYON_TERRE = 6366000;
    private static final int MAX_VITESSE_METRE_SECONDE = 0;
    private static final float COEFF_CONVERSION_MS_KMH = 4;
    private final static int GET_FROM_GALLERY = 5, MENU_LOAD_IMAGE = 10;
    private final static int SNAP_PICTURE = 6, MENU_SNAP_IMAGE = 11;
    private static final String TAG = MyPositionActivity.class.getSimpleName();
    private static final String LOG_TAG = MyPositionActivity.class.getSimpleName();
    private static String myFormat = "dd-MM-yyyy HH:mm";
    private static SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
    final int PICK_CONTACT = 7;
    final Calendar myCalendar = Calendar.getInstance();
    private ImageButton problemPreview;
    private AlertDialog alertDialog;
    private TextView timeOfTravel;
    private EditText guardianPhoneNumber;
    private String guardianName = "";
    private SharedPreferences prefs;
    private GoogleMap googleMap;
    private boolean running = true;
    private SpeedometerGauge mspeedometer;
    private TextView speedTextview;
    private DrawerLayout drawer;
    private static Trip mTrip;
    static int PLACE_PICKER_REQUEST_FROM = 2;
    static int PLACE_PICKER_REQUEST_TO = 3;
    private  Spinner destinationSpinner;
    private Spinner fromSpinner;
     MapWrapperLayout mapWrapperLayout;
    private ViewGroup infoWindow;
    private TextView infoSnippet;
    private GoogleMap.InfoWindowAdapter infoWindowAdapter;
    private Geofence geofence;
    private PendingIntent mGeofencePendingIntent;
    private GoogleApiClient googleApiClient;
    private static int accessFineLocationSituation = 1;
    private int numberOfMapSettings = 0;
    private User travelerUser;
    //firebase database fields
    DatabaseReference firebaseDatabase;
    //bottom sheet for insurance
    BottomSheetBehavior bottomSheetBehavior;

    private static HashMap<String, double[]> knownTown;


    static boolean IsMatch(String s, String pattern) {
        try {
            Pattern patt = Pattern.compile(pattern);

            Matcher matcher = patt.matcher(s);
            return matcher.matches();
        } catch (RuntimeException e) {
            return false;
        }
    }

    static double computeDistance(double latA, double lngA, double latB, double lngB) {

        double pk = 180.f / Math.PI;

        double a1 = latA / pk;
        double a2 = lngA / pk;
        double b1 = latB / pk;
        double b2 = lngB / pk;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return RAYON_TERRE * tt;
    }

    private void updateDateVoyage() {
        if (timeOfTravel != null) timeOfTravel.setText(sdf.format(myCalendar.getTime()));
    }

    private void addToknownTown(String townStr) {
        if (knownTown == null) knownTown = new HashMap<>();
        String[] tmp = townStr.split(Pattern.quote("|"));
        knownTown.put(tmp[0], new double[]{Double.parseDouble(tmp[1]), Double.parseDouble(tmp[2])});

    }

    public void clearMap() {
        googleMap.clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        travelerUser = User.findAll(User.class).next();
        setContentView(R.layout.view_my_position);
        //setup bottom sheet
        View bottomView = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomView);
        bottomSheetBehavior.setPeekHeight(0);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED)
                    bottomSheetBehavior.setPeekHeight(0);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        prefs = getSharedPreferences(TConstants.TRAVELR_PREFERENCE, MODE_PRIVATE);
        //initialize database
        firebaseDatabase = FirebaseDatabase.getInstance().getReference(Tutility.FIREBASE_TRIPS);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    //    getSupportActionBar().setLogo(R.drawable.ic_action_name);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        NavigationItemListener navigationItemListener = new NavigationItemListener(this);
        navigationItemListener.setActivity(this);

        navigationView.setNavigationItemSelectedListener(navigationItemListener);


        addToknownTown(prefs
                .getString(TConstants.PREF_FROM_1, getString(R.string.town_1)));
        addToknownTown(prefs
                .getString(TConstants.PREF_FROM_2, getString(R.string.town_2)));
        addToknownTown(prefs
                .getString(TConstants.PREF_FROM_3, getString(R.string.town_3)));
        addToknownTown(prefs
                .getString(TConstants.PREF_FROM_4, getString(R.string.town_4)));
        addToknownTown(prefs
                .getString(TConstants.PREF_FROM_5, getString(R.string.town_5)));
        addToknownTown(prefs
                .getString(TConstants.PREF_FROM_6, getString(R.string.town_6)));

        addToknownTown(prefs
                .getString(TConstants.PREF_TO_1, getString(R.string.town_2)));
        addToknownTown(prefs
                .getString(TConstants.PREF_TO_2, getString(R.string.town_1)));
        addToknownTown(prefs
                .getString(TConstants.PREF_TO_3, getString(R.string.town_3)));
        addToknownTown(prefs
                .getString(TConstants.PREF_TO_4, getString(R.string.town_4)));
        addToknownTown(prefs
                .getString(TConstants.PREF_TO_5, getString(R.string.town_5)));
        addToknownTown(prefs
                .getString(TConstants.PREF_TO_6, getString(R.string.town_6)));

        final TextView usernameTextview = ((TextView) navigationView.getHeaderView(0).findViewById(R.id.username));
        usernameTextview.setText(travelerUser == null ? "anonyme" : travelerUser.getUsername());

        Typeface tf = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/digital-7.ttf");
        usernameTextview.setTypeface(tf);

        //speed textview
        speedTextview = (TextView) navigationView.getHeaderView(0).findViewById(R.id.speedtext);
        speedTextview.setText(getString(R.string.speed_dimen, travelerUser ==null?"OO000OO":travelerUser.getCurrent_matricule(), Float.parseFloat("0")+""));
        //build speedometer
        mspeedometer = (SpeedometerGauge) navigationView.getHeaderView(0).findViewById(R.id.speedometer);
        setupSpeedometer(mspeedometer);

        //check for GPS availability and activation
        if (!((LocationManager) getSystemService(LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.gps_disabled_message)
                    .setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();
        }


        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                if (msg.getData().containsKey(TConstants.SPEED_PREF)) {
                    double speed = Tutility.round(msg.getData().getFloat(TConstants.SPEED_PREF));
                    /*usernameTextview
                            .setText(prefs.getString(TConstants.PREF_USERNAME, "anonyme"));*/
                    /*
                                    +( speed >= MAX_VITESSE_METRE_SECONDE ? " ("
                                    + round(speed * COEFF_CONVERSION_MS_KMH)
                                    + " KM/H" + ")" : " (" + round(speed) + " m/s)" ));
                    */
                    //update speedometer speed value
                    double mspeed = speed >= MAX_VITESSE_METRE_SECONDE ? Tutility.round(speed * COEFF_CONVERSION_MS_KMH) :
                            Tutility.round(speed / COEFF_CONVERSION_MS_KMH);
                    mspeedometer.setSpeed(mspeed);
                    //update the value on the speed label
                    speedTextview.setText(getString(R.string.speed_dimen, prefs.getString(TConstants.PREF_MATRICULE, "OO000OO"), mspeed+""));
                }
            }
        };

        final SharedPreferences preff = prefs;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                while (running) {

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Message msg = handler.obtainMessage();
                    Bundle data = new Bundle();

                    data.putFloat(TConstants.SPEED_PREF, preff.getFloat(TConstants.SPEED_PREF, 0.0f));
                    msg.setData(data);
                    // Envoi du message au handler
                    handler.sendMessage(msg);
                }
            }
        });
        t.start();

        this.infoWindow = (ViewGroup) getLayoutInflater().inflate(R.layout.info_window, null);
        this.infoSnippet = (TextView) infoWindow.findViewById(R.id.snippet);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mapWrapperLayout = (MapWrapperLayout) findViewById(R.id.map_relative_layout);


        infoWindowAdapter = new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Setting up the infoWindow with current's marker info
                try {
                    mapWrapperLayout.setMarkerWithInfoWindow(marker, infoWindow);


                    //  infoSnippet.setText(marker.getSnippet());
                    infoSnippet.setText(new StringBuilder().append(marker.getTitle()).append(": \n\n").append(marker.getSnippet()).toString());


                    return infoWindow;
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MyPositionActivity.this, getString(R.string.operation_failed_try_again_later), Toast.LENGTH_LONG).show();
                    return null;
                }
            }
        };

        // create GoogleApiClient
        createGoogleApi();
    }

    // Create GoogleApiClient instance
    private void createGoogleApi() {
        Log.d(TAG, "createGoogleApi()");
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        Log.d(LOG_TAG, "Map is ready");
        googleMap = map;

        map.setInfoWindowAdapter(infoWindowAdapter);
        mapWrapperLayout.init(map, getPixelsFromDp(this, 39 + 20));


        // Enabling MyLocation in Google Map
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            accessFineLocationSituation = 1;
            requestPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }

        Intent intent = new Intent(this, SpeedMeterService.class);

        startService(intent);

        googleMap.setMyLocationEnabled(true);
        googleMap.setBuildingsEnabled(true);

        if(numberOfMapSettings==0&&googleApiClient.isConnected()){
            setupCurrentTrip();
            numberOfMapSettings=1;
        }
    }

    private void requestPermission(String permission) {
        //ask user to grant permission to read fine location. Required for android 6.0+ API level 23+
        ActivityCompat.requestPermissions(MyPositionActivity.this,
                new String[]{permission},
                MainActivity.stringToInt(permission));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MainActivity.stringToInt(android.Manifest.permission.ACCESS_FINE_LOCATION) &&
                grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            // If request is cancelled, the result arrays are empty.
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


                return;
            }
            if(accessFineLocationSituation==1) {
                Intent intent = new Intent(this, SpeedMeterService.class);

                startService(intent);

                googleMap.setMyLocationEnabled(true);
                googleMap.setBuildingsEnabled(true);
            }
            else if(accessFineLocationSituation==2){
                LocationServices.GeofencingApi.addGeofences(
                        googleApiClient,
                        getGeofencingRequest(),
                        getGeofencePendingIntent()
                ).setResultCallback(this);
            }

        } else if (requestCode == MainActivity.stringToInt(Manifest.permission.READ_CONTACTS) &&
                grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, PICK_CONTACT);

        }


    }


    //initiate the speedometer controls and set it up
    public void setupSpeedometer(SpeedometerGauge mspeedometer) {

        // Add label converter
        mspeedometer.setLabelConverter(new SpeedometerGauge.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }
        });
        mspeedometer.setLabelTextSize(25);
        // configure value range and ticks
        mspeedometer.setMaxSpeed(180);
        mspeedometer.setMajorTickStep(30);
        mspeedometer.setMinorTicks(2);

        // Configure value range colors
        mspeedometer.addColoredRange(30, 140, Color.GREEN);
        mspeedometer.addColoredRange(140, 180, Color.YELLOW);
        mspeedometer.addColoredRange(180, 400, Color.RED);
        //set initial speed reading
        mspeedometer.setSpeed(1, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_about) {
            Tutility.showMessage(this, R.string.about_message1, R.string.about_title1);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Dialog onCreateDialog(int id) {

        AlertDialog dialogDetails = null;
        LayoutInflater inflater;
        View dialogview;
        AlertDialog.Builder dialogbuilder;

        switch (id) {
            case NavigationItemListener.DIALOG_NEW_COMPLAINT:
                inflater = LayoutInflater.from(this);
                dialogview = inflater.inflate(R.layout.problem_complaint_form, null);

                dialogbuilder = new AlertDialog.Builder(this);
                dialogbuilder.setTitle(R.string.new_repport_complaint);
                dialogbuilder.setView(dialogview);
                dialogDetails = dialogbuilder.create();

                break;
            case NavigationItemListener.DIALOG_NEW_JOURNEY:
                inflater = LayoutInflater.from(this);
                dialogview = inflater.inflate(R.layout.register_a_journey, null);

                dialogbuilder = new AlertDialog.Builder(this);
                dialogbuilder.setTitle(R.string.confirm_journey);
                dialogbuilder.setView(dialogview);
                dialogDetails = dialogbuilder.create();

                break;
        }

        return dialogDetails;
    }

    @Override
    protected void onPrepareDialog(int id, final Dialog dialog) {

        switch (id) {
            case NavigationItemListener.DIALOG_NEW_COMPLAINT:
                alertDialog = (AlertDialog) dialog;

                final Spinner problemType = (Spinner) alertDialog.findViewById(R.id.type_of_problem);

                final EditText busImmatriculation = (EditText) alertDialog.findViewById(R.id.bus_immatriculation);
                final EditText problemDescription = (EditText) alertDialog.findViewById(R.id.problem_description);

                FancyButton buttonSave = (FancyButton) alertDialog.findViewById(R.id.button_save);
                problemPreview = (ImageButton) alertDialog.findViewById(R.id.problem_preview);

                final Spinner problemLevel = (Spinner) alertDialog.findViewById(R.id.problem_level);
                final TextView problemLevelLabel = (TextView) alertDialog.findViewById(R.id.problem_level_label);


                problemPreview.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {

                    @Override
                    public void onCreateContextMenu(ContextMenu menu, View v,
                                                    ContextMenu.ContextMenuInfo menuInfo) {
                        menu.add(0, MENU_LOAD_IMAGE, 0, getString(R.string.load_image));
                        menu.add(0, MENU_SNAP_IMAGE, 0, getString(R.string.snap_image));

                        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem clickedItem) {
                                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);

                                return true;
                            }
                        });

                        menu.getItem(1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem clickedItem) {
                                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(intent, SNAP_PICTURE);
                                return true;
                            }
                        });
                    }
                });

                problemPreview.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        alertDialog.openContextMenu(v);
                    }
                });

                problemType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view,
                                               int position, long id) {
                        problemDescription.setVisibility(View.INVISIBLE);
                        problemLevel.setVisibility(View.INVISIBLE);
                        problemLevelLabel.setVisibility(View.INVISIBLE);
                        busImmatriculation.setEnabled(true);

                        String[] problemTypes = MyPositionActivity.this.getResources().getStringArray(R.array.problem_types);

                        if (position == problemTypes.length - 1) {
                            problemDescription.setVisibility(View.VISIBLE);
                        } else if (position == 0) {
                            problemLevel.setVisibility(View.VISIBLE);
                            problemLevelLabel.setVisibility(View.VISIBLE);
                        } else if (position == 2) {
                            busImmatriculation.setEnabled(false);
                            busImmatriculation.setText(R.string.immatriculation_example);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                buttonSave.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        if (busImmatriculation.getText().toString().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.provide_car_immatriculation_number) + " ...", Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (problemDescription.getVisibility() == View.VISIBLE && problemDescription.getText().toString().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.provide_problem_description) + " ...", Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (!IsMatch(busImmatriculation.getText().toString().toUpperCase(), getString(R.string.car_immatriculation_regex_patern))) {
                            Toast.makeText(getApplicationContext(), getString(R.string.incorrect_immatriculation_number) + "...", Toast.LENGTH_LONG).show();
                            return;
                        }
                        Toast.makeText(getApplicationContext(), getString(R.string.problem_saved_successfull) + "... ", Toast.LENGTH_LONG).show();

                        alertDialog.dismiss();
                    }
                });

                alertDialog.findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
                break;

            case NavigationItemListener.DIALOG_NEW_JOURNEY: //register a new journey


                alertDialog = (AlertDialog) dialog;

                ImageButton chooseContact = (ImageButton) alertDialog.findViewById(R.id.choose_contact);
                final Spinner companyName = (Spinner) alertDialog.findViewById(R.id.company_name);
                fromSpinner = (Spinner) alertDialog.findViewById(R.id.departure);
                destinationSpinner = (Spinner) alertDialog.findViewById(R.id.destination);

                mTrip = new Trip();

                setFromSpinner();


                setToSpinner();

                mTrip.setAgency_name(getResources().getStringArray(R.array.compagnies_names)[0]);


                final EditText busMatriculationNumber = (EditText) alertDialog.findViewById(R.id.matriculation_number_of_bus);
                //timeOfTravel = (EditText)alertDialog.findViewById(R.id.time_of_travel);
                //final EditText travelDuration = (EditText)alertDialog.findViewById(R.id.journey_duration);
                guardianPhoneNumber = (EditText) alertDialog.findViewById(R.id.guardian_phone_number);

                guardianPhoneNumber.setText(prefs
                        .getString(TConstants.PREF_EMERGENCY_CONTACT_1, ""));

                busMatriculationNumber.setText(prefs
                        .getString(TConstants.PREF_MATRICULE, "").toUpperCase());

                buttonSave = (FancyButton) alertDialog.findViewById(R.id.button_save);

                final FancyButton buttonCancel = (FancyButton) alertDialog.findViewById(R.id.button_cancel);

                chooseContact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (ActivityCompat.checkSelfPermission(MyPositionActivity.this, android.Manifest.permission.READ_CONTACTS)
                                == PackageManager.PERMISSION_GRANTED) {
                            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                            startActivityForResult(intent, PICK_CONTACT);
                        } else {
                            requestPermission(android.Manifest.permission.READ_CONTACTS);
                        }


                    }
                });
                companyName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        ArrayAdapter<String> arrayAdapter = (ArrayAdapter<String>) parent.getAdapter();
                        mTrip.setAgency_name(arrayAdapter.getItem(position));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        ArrayAdapter<String> adapter = (ArrayAdapter<String>) parent.getAdapter();
                        if (!adapter.getItem(position).equals(getString(R.string.place_list_option_choose))) {
                            mTrip.setDeparture(adapter.getItem(position));
                            mTrip.setDepartureLatitude(knownTown.get(adapter.getItem(position))[0]);
                            mTrip.setDepartureLongitude(knownTown.get(adapter.getItem(position))[1]);

                        } else {
                            //open place chooser

                            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                            try {
                                startActivityForResult(builder.build(MyPositionActivity.this), PLACE_PICKER_REQUEST_FROM);
                            } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                                e.printStackTrace();
                                Snackbar.make(fromSpinner, R.string.operation_failed_try_again_later, Snackbar.LENGTH_LONG)
                                        .show();
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
                destinationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        ArrayAdapter<String> adapter = (ArrayAdapter<String>) parent.getAdapter();

                        if (!adapter.getItem(position).equals(getString(R.string.place_list_option_choose))) {
                            mTrip.setDestination(adapter.getItem(position));
                            mTrip.setDestinationLatitude(knownTown.get(adapter.getItem(position))[0]);
                            mTrip.setDestinationLongitude(knownTown.get(adapter.getItem(position))[1]);
                        } else {
                            //open place chooser

                            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                            try {
                                startActivityForResult(builder.build(MyPositionActivity.this), PLACE_PICKER_REQUEST_TO);
                            } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                                e.printStackTrace();
                                Snackbar.make(destinationSpinner, R.string.operation_failed_try_again_later, Snackbar.LENGTH_LONG)
                                        .show();
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                buttonSave.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dialog.findViewById(R.id.tripProgressbar).setVisibility(View.VISIBLE);
                        if (busMatriculationNumber.getText().toString().isEmpty() || guardianPhoneNumber.getText().toString().isEmpty()) {
                            Toast.makeText(getApplicationContext(), getString(R.string.provide_all_fields), Toast.LENGTH_LONG).show();
                            dialog.findViewById(R.id.tripProgressbar).setVisibility(View.GONE);
                            return;
                        }

                        if (!IsMatch(busMatriculationNumber.getText().toString().toUpperCase(), getString(R.string.car_immatriculation_regex_patern))) {
                            Log.e("regex: ", "pattern: " + getString(R.string.incorrect_immatriculation_number));
                            Toast.makeText(getApplicationContext(), getString(R.string.incorrect_immatriculation_number), Toast.LENGTH_LONG).show();
                            dialog.findViewById(R.id.tripProgressbar).setVisibility(View.GONE);
                            return;
                        }
                        mTrip.setBus_immatriculation(busMatriculationNumber.getText().toString().toUpperCase());
                        mTrip.setContact_name(guardianName);
                        mTrip.setContact_number(guardianPhoneNumber.getText().toString());
                        mTrip.setDate_start(new SimpleDateFormat("MMMM-dd-yyyy", Locale.US).format(new Date()));
                        mTrip.setDate_end("");
                        mTrip.setStatus(0);
                        mTrip.setTripKey(Tutility.getTripKeyAsString(mTrip.getDeparture(), mTrip.getDestination(), mTrip.getDate_start()));
                        //mTrip.user = travelerUser;

                        long saveid = mTrip.save();
                        if (saveid > 0) {
                            Toast.makeText(getApplicationContext(), getString(R.string.journey_saved_successfull), Toast.LENGTH_LONG).show();
                            //update map with current trip
                            setupCurrentTrip();
                            //update user's current matricule
                            travelerUser.setCurrent_matricule(mTrip.getBus_immatriculation());
                            travelerUser.save();
                            //save trip to firebase
                            Map<String, Object> tripMap = new HashMap<>();
                            tripMap.put(mTrip.getTripKey(), mTrip);
                            firebaseDatabase
                                    .child(mTrip.getBus_immatriculation())
                                    .updateChildren(tripMap);
                            firebaseDatabase.child(mTrip.getBus_immatriculation())
                                    .child(mTrip.getTripKey())
                                    .child("passengers")
                                    .push()
                                    .setValue(travelerUser);
                            //save current trip matricule to preference
                            prefs.edit().putString(TConstants.PREF_MATRICULE, mTrip.getBus_immatriculation()).apply();
                            //set new matricule on speedometer textview
                            getString(R.string.speed_dimen, mTrip.getBus_immatriculation(), Float.parseFloat("0")+"");
                            //TODO. Show snackbar asking user to setup insurance plan
                            Snackbar.make(findViewById(R.id.my_frame_host), getString(R.string.insurance_plan), Snackbar.LENGTH_LONG)
                                    .setAction(getString(R.string.get_insurance), MyPositionActivity.this)
                                    .show();
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.journey_saved_failed), Toast.LENGTH_LONG).show();
                        }
                        Log.d(TAG, mTrip.toString());
                        dialog.findViewById(R.id.tripProgressbar).setVisibility(View.GONE);
                        alertDialog.dismiss();
                    }
                });

                buttonCancel.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
                updateDateVoyage();
                break;
            case NavigationItemListener.DIALOG_NEW_INSURANCE:
                //TODO: handle insurance selection here
                break;
        }
    }

    private void setToSpinner() {
        List<String> listTo = new ArrayList<>();
        listTo.add(0, prefs
                .getString(TConstants.PREF_TO_1, getString(R.string.town_2)).split(Pattern.quote("|"))[0]);
        listTo.add(1, prefs
                .getString(TConstants.PREF_TO_2, getString(R.string.town_1)).split(Pattern.quote("|"))[0]);
        listTo.add(2, prefs
                .getString(TConstants.PREF_TO_3, getString(R.string.town_3)).split(Pattern.quote("|"))[0]);
        listTo.add(3, prefs
                .getString(TConstants.PREF_TO_4, getString(R.string.town_4)).split(Pattern.quote("|"))[0]);
        listTo.add(4, prefs
                .getString(TConstants.PREF_TO_5, getString(R.string.town_5)).split(Pattern.quote("|"))[0]);
        listTo.add(5, prefs
                .getString(TConstants.PREF_TO_6, getString(R.string.town_6)).split(Pattern.quote("|"))[0]);
        listTo.add(6, getString(R.string.place_list_option_choose));

        ArrayAdapter<String> toAdapter = new ArrayAdapter<>(MyPositionActivity.this,
                android.R.layout.simple_spinner_item, listTo);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        destinationSpinner.setAdapter(toAdapter);

        destinationSpinner.setSelection(0, true);

        mTrip.setDestination(listTo.get(0));
        mTrip.setDestinationLatitude(knownTown.get(listTo.get(0))[0]);
        mTrip.setDestinationLongitude(knownTown.get(listTo.get(0))[1]);

    }


    private void setFromSpinner() {
        List<String> listFrom = new ArrayList<>();
        listFrom.add(0, prefs
                .getString(TConstants.PREF_FROM_1, getString(R.string.town_1)).split(Pattern.quote("|"))[0]);
        listFrom.add(1, prefs
                .getString(TConstants.PREF_FROM_2, getString(R.string.town_2)).split(Pattern.quote("|"))[0]);
        listFrom.add(2, prefs
                .getString(TConstants.PREF_FROM_3, getString(R.string.town_3)).split(Pattern.quote("|"))[0]);
        listFrom.add(3, prefs
                .getString(TConstants.PREF_FROM_4, getString(R.string.town_4)).split(Pattern.quote("|"))[0]);
        listFrom.add(4, prefs
                .getString(TConstants.PREF_FROM_5, getString(R.string.town_5)).split(Pattern.quote("|"))[0]);
        listFrom.add(5, prefs
                .getString(TConstants.PREF_FROM_6, getString(R.string.town_6)).split(Pattern.quote("|"))[0]);
        listFrom.add(6, getString(R.string.place_list_option_choose));

        ArrayAdapter<String> fromAdapter = new ArrayAdapter<>(MyPositionActivity.this,
                android.R.layout.simple_spinner_item, listFrom);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);

        fromSpinner.setSelection(0, true);

        mTrip.setDeparture(listFrom.get(0));
        mTrip.setDepartureLatitude(knownTown.get(listFrom.get(0))[0]);
        mTrip.setDepartureLongitude(knownTown.get(listFrom.get(0))[1]);
    }


    private void setupCurrentTrip() {
        Trip trip = null;

        List<Trip> trips = Trip.listAll(Trip.class, "tid");//Trip.last(Trip.class);
        //refresh layout by getting fresh view references and setting their values


        if (trips != null && trips.size() > 0) {
            trip = trips.get(trips.size() - 1);
        }


        //refresh layout by getting fresh view references and setting their values


        if (trip != null && trip.getStatus() == 0) {
            /*TextView departure = (TextView) findViewById(R.id.departureTextview);
            TextView arrival = (TextView) findViewById(R.id.destinationTextview);
            TextView agence = (TextView) findViewById(R.id.agencyTextView);
            TextView timedepart = (TextView) findViewById(R.id.timeDepartureTextview);

            departure.setText(getString(R.string.depart, trip.getDeparture()));
            arrival.setText(getString(R.string.arrivee, trip.getDestination()));
            agence.setText(trip.getAgency_name());
            setDrawableStatus(agence, trip.getStatus());
            timedepart.setText(getString(R.string.datedepart, trip.getDate_start()));*/

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            int padding = 50;
            googleMap.clear();

            //positionnement des marqueurs et trace du trajet



            LatLng from, to;
            GetRouteTask getRoute1;


            from = new LatLng(trip.getDepartureLatitude(), trip.getDepartureLongitude());
            to = new LatLng(trip.getDestinationLatitude(), trip.getDestinationLongitude());

            builder.include(from);
            builder.include(to);


            googleMap.addMarker(new MarkerOptions().position(from).title(getString(R.string.start_marker_string)).snippet(trip.getDeparture()).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_start)));

            googleMap.addMarker(new MarkerOptions().position(to).title(getString(R.string.end_marker_string)).snippet(trip.getDestination()).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_end)));

            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), padding));

            getRoute1 = new GetRouteTask(MyPositionActivity.this, googleMap, from, to, Color.GREEN);

            try {
                getRoute1.execute();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MyPositionActivity.this, getString(R.string.operation_failed_try_again_later), Toast.LENGTH_LONG).show();
            }


            // PRISE EN CHARGE DE L'AFFICHAGE DES DEPASSEMENTS DE VITESSE
            for (SpeedOverhead so : SpeedOverhead.find(SpeedOverhead.class, "tripid = ?", "" + getCurrentTrip().getId())) {
                from = new LatLng(so.getLatitude_start(), so.getLongitude_start());
                to = new LatLng(so.getLatitude_end(), so.getLongitude_end());

                builder.include(from);
                builder.include(to);

                googleMap.addMarker(new MarkerOptions().position(from).title(getString(R.string.speed_overhead_marker_string)).snippet(so.getDate_start() + " \n\n " + Tutility.round(so.getSpeed_start() * COEFF_CONVERSION_MS_KMH) + " KM/H").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_speed_overhead)));

                googleMap.addMarker(new MarkerOptions().position(to).title(getString(R.string.speed_overheading_marker_end_string)).snippet(so.getDate_end() + " \n\n " + Tutility.round(so.getSpeed_end() * COEFF_CONVERSION_MS_KMH) + " KM/H").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_speed_overhead)));


                getRoute1 = new GetRouteTask(MyPositionActivity.this, googleMap, from, to, Color.RED);

                try {
                    getRoute1.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MyPositionActivity.this, getString(R.string.operation_failed_try_again_later), Toast.LENGTH_LONG).show();
                    break;
                }


            }

            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), padding));


            geofence = new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(trip.getId() + "")

                    .setCircularRegion(
                            trip.getDestinationLatitude(),
                            trip.getDestinationLongitude(),
                            TConstants.GEOFENCE_RADIUS_IN_METERS
                    )
                    .setExpirationDuration(TConstants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT|Geofence.GEOFENCE_TRANSITION_DWELL)
                    .setLoiteringDelay (TConstants.GEOFENCE_DWELL_DELAY_IN_MILLISECONDS)
                    .build();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                accessFineLocationSituation = 2;
                requestPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);

                return;
            }
            LocationServices.GeofencingApi.addGeofences(
                    googleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this);


        }
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(Geofence.GEOFENCE_TRANSITION_ENTER);
        builder.addGeofence(geofence);
        return builder.build();
    }

    public PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
         mGeofencePendingIntent =  PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);

        return mGeofencePendingIntent;
    }

    public static boolean  isCurrentTripExist(){
        Trip trip = getCurrentTrip();
        return trip != null && trip.getStatus() == 0;
    }

    public static Trip  getCurrentTrip(){
        Trip trip = null;
        List<Trip> trips = Trip.listAll(Trip.class, "tid");//Trip.last(Trip.class);

        if (trips != null && trips.size() > 0) {
            trip = trips.get(trips.size() - 1);
        }

        return trip;
    }

    public static int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dp * scale + 0.5f);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        Bitmap attachedImage = null;
        if((requestCode==PICK_CONTACT) && resultCode == Activity.RESULT_OK) {
            Uri contactData = data.getData();
            Cursor c = managedQuery(contactData, null, null, null, null);
            if (c.moveToFirst()) {
                String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                String hasPhone =
                        c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                if (hasPhone.equalsIgnoreCase("1")) {
                    Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);

                    assert phones != null;
                    phones.moveToFirst();
                    String Number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String Name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    // Toast.makeText(getApplicationContext(), cNumber, Toast.LENGTH_SHORT).show();

                    //						label_no.setText(getString(R.string.key_number_of)+" "+Name);
                    guardianPhoneNumber.setText(Number);
                    guardianName = Name;
                    //						num = Number;
                }
            }
        }

        //Detects request codes
        else if((requestCode==GET_FROM_GALLERY) && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();

            try {
                attachedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                //se servir du compress pour envoyer le bitmap dans un outputstream vers le serveur
                problemPreview.setImageBitmap(Bitmap.createScaledBitmap(attachedImage, problemPreview.getWidth(), problemPreview.getHeight(), false));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        else if(requestCode==SNAP_PICTURE && resultCode == Activity.RESULT_OK) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            try{
                attachedImage = (Bitmap) data.getExtras().get("data");
                problemPreview.setImageBitmap(Bitmap.createScaledBitmap(attachedImage, problemPreview.getWidth(), problemPreview.getHeight(), false));

            }catch(Exception e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), getString(R.string.error_occur_please_retry)+"...", Toast.LENGTH_LONG).show();
            }
        }

        else if (requestCode == PLACE_PICKER_REQUEST_FROM) {

            if(resultCode == RESULT_OK){
                Place place = PlacePicker.getPlace(data, this);
                if(place!=null){
                    String name = (place.getName()==null || place.getName().toString().isEmpty())?"("+place.getLatLng().latitude+", "+place.getLatLng().longitude+")":place.getName().toString();

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(TConstants.PREF_FROM_6, prefs
                            .getString(TConstants.PREF_FROM_5, getString(R.string.town_5)));
                    editor.putString(TConstants.PREF_FROM_5, prefs
                            .getString(TConstants.PREF_FROM_4, getString(R.string.town_4)));
                    editor.putString(TConstants.PREF_FROM_4, prefs
                            .getString(TConstants.PREF_FROM_3, getString(R.string.town_3)));
                    editor.putString(TConstants.PREF_FROM_3, prefs
                            .getString(TConstants.PREF_FROM_2, getString(R.string.town_2)));
                    editor.putString(TConstants.PREF_FROM_2, prefs
                            .getString(TConstants.PREF_FROM_1, getString(R.string.town_1)));
                    editor.putString(TConstants.PREF_FROM_1, name+"|"+place.getLatLng().latitude+"|"+place.getLatLng().longitude);

                    editor.commit();

                    addToknownTown(name+"|"+place.getLatLng().latitude+"|"+place.getLatLng().longitude);

                    setFromSpinner();

                }
            }
            else{
                fromSpinner.setSelection(0, true);
            }



        }

        else if (requestCode == PLACE_PICKER_REQUEST_TO) {

            if(resultCode == RESULT_OK){

                Place place = PlacePicker.getPlace(data, this);
                if(place!=null){
                    String name = (place.getName()==null || place.getName().toString().isEmpty())?"("+place.getLatLng().latitude+", "+place.getLatLng().longitude+")":place.getName().toString();

                    SharedPreferences.Editor editor = prefs.edit();

                    editor.putString(TConstants.PREF_TO_6, prefs
                            .getString(TConstants.PREF_TO_5, getString(R.string.town_5)));
                    editor.putString(TConstants.PREF_TO_5, prefs
                            .getString(TConstants.PREF_TO_4, getString(R.string.town_4)));
                    editor.putString(TConstants.PREF_TO_4, prefs
                            .getString(TConstants.PREF_TO_3, getString(R.string.town_3)));
                    editor.putString(TConstants.PREF_TO_3, prefs
                            .getString(TConstants.PREF_TO_2, getString(R.string.town_1)));
                    editor.putString(TConstants.PREF_TO_2, prefs
                            .getString(TConstants.PREF_TO_1, getString(R.string.town_2)));
                    editor.putString(TConstants.PREF_TO_1, name+"|"+place.getLatLng().latitude+"|"+place.getLatLng().longitude);

                    editor.commit();

                    addToknownTown(name+"|"+place.getLatLng().latitude+"|"+place.getLatLng().longitude);

                    setToSpinner();

                }
            }
            else{
                destinationSpinner.setSelection(0, true);
            }


        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        drawer.openDrawer(GravityCompat.START);

        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Disconnect GoogleApiClient when stopping Activity
        googleApiClient.disconnect();
    }

    // GoogleApiClient.ConnectionCallbacks connected
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected()");

        if(numberOfMapSettings==0){
            setupCurrentTrip();
            numberOfMapSettings=1;
        }
    }

    // GoogleApiClient.ConnectionCallbacks suspended
    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "onConnectionSuspended()");
    }

    // GoogleApiClient.OnConnectionFailedListener fail
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng userAddress = new LatLng(location.getLatitude(), location.getLongitude());
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(userAddress)
                .tilt(45)
                .zoom(17)
                .build();
        googleMap.addMarker(new MarkerOptions()
                .position(userAddress)
                .title(getString(R.string.myposition))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_myposition)));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(userAddress));
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void onResult(@NonNull Result result) {

    }

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    @Override
    public void onClick(View v) {
        //handle snackbar insurance interaction click
        Toast.makeText(this, "Selecting plan", Toast.LENGTH_SHORT).show();
        //TODO. Bring up bottom sheet with different insurance plans
        bottomSheetBehavior.setPeekHeight(200);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }
}
