package com.satra.traveler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.support.design.widget.NavigationView;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cardiomood.android.controls.gauge.SpeedometerGauge;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.satra.traveler.models.Trip;
import com.satra.traveler.utils.TConstants;
import com.satra.traveler.utils.Tutility;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mehdi.sakout.fancybuttons.FancyButton;

public class MyPositionActivity extends AppCompatActivity implements OnMapReadyCallback,LocationSource.OnLocationChangedListener {

    private static final int RAYON_TERRE = 6366000;
    private static final int MAX_VITESSE_METRE_SECONDE = 3;
    private static final float COEFF_CONVERSION_MS_KMH = 4;
    private final static int GET_FROM_GALLERY = 5, MENU_LOAD_IMAGE = 10;
    private final static int SNAP_PICTURE = 6, MENU_SNAP_IMAGE = 11;
    private static final String TAG = MyPositionActivity.class.getSimpleName();
    private static String myFormat = "dd/MM/yyyy HH:mm";
    private static SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
    final int PICK_CONTACT = 7;
    final Calendar myCalendar = Calendar.getInstance();
    private Bitmap attachedImage = null;
    private ImageButton problemPreview;
    private AlertDialog alertDialog;
    private FancyButton buttonSave;
    private FancyButton buttonCancel;
    private TextView timeOfTravel;
    private EditText guardianPhoneNumber, guardianName;
    private NavigationView navigationView;
    private SharedPreferences prefs;
    private GoogleMap googleMap;
    private boolean running = true;
    private SpeedometerGauge mspeedometer;
    private DrawerLayout drawer;
    private Trip currentTrip;

    static boolean IsMatch(String s, String pattern) {
        try {
            Pattern patt = Pattern.compile(pattern);
            Matcher matcher = patt.matcher(s);
            return matcher.matches();
        } catch (RuntimeException e) {
            return false;
        }
    }

    static double computeDistance(double latA, double lngA, double latB, double lngB){

        double pk =  180.f/Math.PI;

        double a1 = latA / pk;
        double a2 = lngA / pk;
        double b1 = latB / pk;
        double b2 = lngB / pk;

        double t1 = Math.cos(a1)*Math.cos(a2)*Math.cos(b1)*Math.cos(b2);
        double t2 = Math.cos(a1)*Math.sin(a2)*Math.cos(b1)*Math.sin(b2);
        double t3 = Math.sin(a1)*Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return RAYON_TERRE*tt;
    }

    private void updateDateVoyage() {
        if (timeOfTravel != null) timeOfTravel.setText(sdf.format(myCalendar.getTime()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_my_position);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationItemListener(this));

        prefs = getSharedPreferences(TConstants.TRAVELR_PREFERENCE, 0);
        final TextView usernameTextview = ((TextView) navigationView.getHeaderView(0).findViewById(R.id.username));
        usernameTextview.setText(prefs.getString(TConstants.PREF_USERNAME, "anonyme"));

        Typeface tf = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/digital-7.ttf");
        usernameTextview.setTypeface(tf);

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

        Intent intent = new Intent(this, SpeedMeterService.class);

        startService(intent);

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                if (msg.getData().containsKey(TConstants.SPEED_PREF)) {
                    double speed = Tutility.round(msg.getData().getFloat(TConstants.SPEED_PREF));
                    usernameTextview
                            .setText(prefs.getString(TConstants.PREF_USERNAME, "anonyme"));
                    /*
                                    +( speed >= MAX_VITESSE_METRE_SECONDE ? " ("
                                    + round(speed * COEFF_CONVERSION_MS_KMH)
                                    + " KM/H" + ")" : " (" + round(speed) + " m/s)" ));
                    */
                    //update speedometer speed value
                    mspeedometer.setSpeed(speed >= MAX_VITESSE_METRE_SECONDE?Tutility.round(speed * COEFF_CONVERSION_MS_KMH):
                    Tutility.round(speed / COEFF_CONVERSION_MS_KMH));
                }
            }
        };

        final SharedPreferences preff = prefs;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                while(running){

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
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        googleMap = map;
        // Enabling MyLocation in Google Map
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling requestPermission
            requestPermission();
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.setBuildingsEnabled(true);
    }

    private void requestPermission(){
        //ask user to grant permission to read fine location. Required for android 6.0+
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //TODO Handle operation appropriately

    }

    //initiate the speedometer controls and set it up
    public void setupSpeedometer(SpeedometerGauge mspeedometer){

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
        mspeedometer.setSpeed(1,true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        drawer.openDrawer(GravityCompat.START);
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
            Tutility.showMessage(this, R.string.about_message, R.string.about_title);
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
    protected void onPrepareDialog(int id, Dialog dialog) {

        switch (id) {
            case NavigationItemListener.DIALOG_NEW_COMPLAINT:
                alertDialog = (AlertDialog) dialog;

                final Spinner problemType = (Spinner)alertDialog.findViewById(R.id.type_of_problem);

                final  EditText busImmatriculation = (EditText)alertDialog.findViewById(R.id.bus_immatriculation);
                final EditText problemDescription = (EditText)alertDialog.findViewById(R.id.problem_description);

                buttonSave = (FancyButton)alertDialog.findViewById(R.id.button_save);
                problemPreview = (ImageButton)alertDialog.findViewById(R.id.problem_preview);

                final Spinner problemLevel = (Spinner)alertDialog.findViewById(R.id.problem_level);
                final TextView problemLevelLabel = (TextView)alertDialog.findViewById(R.id.problem_level_label);
                buttonCancel = (FancyButton)alertDialog.findViewById(R.id.button_cancel);

                problemPreview.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {

                    @Override
                    public void onCreateContextMenu(ContextMenu menu, View v,
                                                    ContextMenu.ContextMenuInfo menuInfo) {
                        menu.add(0, MENU_LOAD_IMAGE, 0, getString(R.string.load_image));
                        menu.add(0, MENU_SNAP_IMAGE, 0, getString(R.string.snap_image));

                        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
                        {
                            public boolean onMenuItemClick(MenuItem clickedItem)
                            {
                                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);

                                return true;
                            }
                        });

                        menu.getItem(1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
                        {
                            public boolean onMenuItemClick(MenuItem clickedItem)
                            {
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
                            busImmatriculation.setText("CE111AA");
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                buttonSave.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        if(busImmatriculation.getText().toString().equals("")){
                            Toast.makeText(getApplicationContext(), getString(R.string.provide_car_immatriculation_number)+" ...", Toast.LENGTH_LONG).show();
                            return;
                        }

                        if(problemDescription.getVisibility()==View.VISIBLE && problemDescription.getText().toString().equals("")){
                            Toast.makeText(getApplicationContext(), getString(R.string.provide_problem_description)+" ...", Toast.LENGTH_LONG).show();
                            return;
                        }

                        if(!IsMatch(busImmatriculation.getText().toString(), "[A-Z]{2}[0-9]{3}[A-Z]{2}")){
                            Toast.makeText(getApplicationContext(), getString(R.string.incorrect_immatriculation_number)+"...", Toast.LENGTH_LONG).show();
                            return;
                        }
                        //TODO: Save issue to DB
                        Toast.makeText(getApplicationContext(), getString(R.string.problem_saved_successfull)+"... ", Toast.LENGTH_LONG).show();

                        alertDialog.dismiss();
                    }
                });

                buttonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
                break;

            case NavigationItemListener.DIALOG_NEW_JOURNEY:
                final Trip mtrip = new Trip();
                mtrip.setDeparture("Douala");
                mtrip.setDestination("Douala");
                mtrip.setAgency_name("Buca Voyage");

                alertDialog = (AlertDialog) dialog;

                ImageButton chooseContact = (ImageButton)alertDialog.findViewById(R.id.choose_contact);
                final Spinner companyName = (Spinner)alertDialog.findViewById(R.id.company_name);
                final Spinner fromSpinner = (Spinner) alertDialog.findViewById(R.id.departure);
                Spinner destinationSpinner = (Spinner) alertDialog.findViewById(R.id.destination);

                final EditText busMatriculationNumber = (EditText)alertDialog.findViewById(R.id.matriculation_number_of_bus);
                //timeOfTravel = (EditText)alertDialog.findViewById(R.id.time_of_travel);
                //final EditText travelDuration = (EditText)alertDialog.findViewById(R.id.journey_duration);
                guardianName = (EditText)alertDialog.findViewById(R.id.guardian_name);
                guardianPhoneNumber = (EditText)alertDialog.findViewById(R.id.guardian_phone_number);

                buttonSave = (FancyButton) alertDialog.findViewById(R.id.button_save);

                final FancyButton buttonCancel = (FancyButton) alertDialog.findViewById(R.id.button_cancel);

                chooseContact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                        startActivityForResult(intent, PICK_CONTACT);
                    }
                });
                companyName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        ArrayAdapter<String> arrayAdapter = (ArrayAdapter<String>) parent.getAdapter();
                        mtrip.setAgency_name(arrayAdapter.getItem(position));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        ArrayAdapter<String> adapter = (ArrayAdapter<String>) parent.getAdapter();
                        mtrip.setDeparture(adapter.getItem(position));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
                destinationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        ArrayAdapter<String> adapter = (ArrayAdapter<String>) parent.getAdapter();
                        mtrip.setDestination(adapter.getItem(position));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                buttonSave.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if(busMatriculationNumber.getText().toString().isEmpty()||guardianName.getText().toString().isEmpty()||guardianPhoneNumber.getText().toString().isEmpty()){
                            Toast.makeText(getApplicationContext(), getString(R.string.provide_all_fields), Toast.LENGTH_LONG).show();
                            return;
                        }

                        if(!IsMatch(busMatriculationNumber.getText().toString(), "[A-Z]{2}[0-9]{3}[A-Z]{2}")){
                            Toast.makeText(getApplicationContext(), getString(R.string.incorrect_immatriculation_number), Toast.LENGTH_LONG).show();
                            return;
                        }
                        mtrip.setBus_immatriculation(busMatriculationNumber.getText().toString());
                        mtrip.setContact_name(guardianName.getText().toString());
                        mtrip.setContact_number(guardianPhoneNumber.getText().toString());
                        mtrip.setDate_start(sdf.format(Calendar.getInstance().getTime()));
                        mtrip.setDate_end("");
                        mtrip.setStatus(0);

                        //TODO: SAVE JOURNEY TO DB
                        alertDialog.dismiss();
                        long saveid = mtrip.save();
                        if (saveid > 0){
                            Toast.makeText(getApplicationContext(), getString(R.string.journey_saved_successfull), Toast.LENGTH_LONG).show();
                            currentTrip = mtrip;
                            setupCurrentTrip();
                        }else{
                            Toast.makeText(getApplicationContext(), getString(R.string.journey_saved_failed), Toast.LENGTH_LONG).show();
                        }
                        Log.d(TAG, mtrip.toString());
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
        }
    }

    private void setupCurrentTrip(){
        List<Trip> trips = Trip.listAll(Trip.class, "tid");//Trip.last(Trip.class);
        //refresh layout by getting fresh view references and setting their values
        if (trips != null && trips.size() > 0){
            Trip trip = trips.get(trips.size() - 1);
            TextView departure = (TextView) findViewById(R.id.departureTextview);
            TextView arrival = (TextView) findViewById(R.id.destinationTextview);
            TextView agence = (TextView) findViewById(R.id.agencyTextView);
            TextView timedepart = (TextView) findViewById(R.id.timeDepartureTextview);

            departure.setText(getString(R.string.depart, trip.getDeparture()));
            arrival.setText(getString(R.string.arrivee,trip.getDestination()));
            agence.setText(trip.getAgency_name());
            timedepart.setText(getString(R.string.datedepart, trip.getDate_start()));
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


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

                    phones.moveToFirst();
                    String Number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String Name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    // Toast.makeText(getApplicationContext(), cNumber, Toast.LENGTH_SHORT).show();

                    //						label_no.setText(getString(R.string.key_number_of)+" "+Name);
                    guardianPhoneNumber.setText(Number);
                    guardianName.setText(Name);
                    //						num = Number;
                }
            }
        }

        //Detects request codes
        if((requestCode==GET_FROM_GALLERY) && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();

            try {
                attachedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                //se servir du compress pour envoyer le bitmap dans un outputstream vers le serveur
                problemPreview.setImageBitmap(Bitmap.createScaledBitmap(attachedImage, problemPreview.getWidth(), problemPreview.getHeight(), false));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(requestCode==SNAP_PICTURE && resultCode == Activity.RESULT_OK) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            try{
                attachedImage = (Bitmap) data.getExtras().get("data");
                problemPreview.setImageBitmap(Bitmap.createScaledBitmap(attachedImage, problemPreview.getWidth(), problemPreview.getHeight(), false));

            }catch(Exception e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), getString(R.string.error_occur_please_retry)+"...", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupCurrentTrip();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng userAddress = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.addMarker(new MarkerOptions().position(userAddress).title("my position"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(userAddress));
    }
}
