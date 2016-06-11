package com.satra.traveler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.satra.traveler.utils.Tutility;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyPositionActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Bitmap attachedImage = null;
    private ImageButton problemPreview;
    private AlertDialog alertDialog;
    private ImageButton buttonSave, buttonCancel;
    private TextView timeOfTravel;
    private EditText guardianPhoneNumber, guardianName;
    private NavigationView navigationView;
    private SharedPreferences prefs;
    private static final int RAYON_TERRE = 6366000;
    private static final int MAX_VITESSE_METRE_SECONDE = 3;
    private static final float COEFF_CONVERSION_MS_KMH = 4;

    private final static int GET_FROM_GALLERY = 5, MENU_LOAD_IMAGE = 10;
    private final static int SNAP_PICTURE = 6, MENU_SNAP_IMAGE = 11;
    private boolean running = true;

    final int PICK_CONTACT = 7;

    final Calendar myCalendar = Calendar.getInstance();
    private static String myFormat = "dd/MM/yyyy HH:mm";
    private static SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

    private void updateDateVoyage() {
        if (timeOfTravel != null) timeOfTravel.setText(sdf.format(myCalendar.getTime()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_my_position);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationItemListener(this));

        TextView panneauPub = (TextView) navigationView.getHeaderView(0).findViewById(R.id.panneau_pub);

        prefs = getSharedPreferences("traveler_prefs", 0);
        ((TextView) navigationView.getHeaderView(0).findViewById(R.id.username)).setText("Hi " + prefs.getString("username", "anonyme"));

        Typeface tf = Typeface.createFromAsset(getApplicationContext().getAssets(),
                "fonts/digital-7.ttf");
        ((TextView) navigationView.getHeaderView(0).findViewById(R.id.username)).setTypeface(tf);

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

                if (msg.getData().containsKey("vitesse")) {
                    ((TextView) navigationView.getHeaderView(0).findViewById(R.id.username)).setText("Hi " + prefs.getString("username", "anonyme") +( msg.getData().getFloat("vitesse") >= MAX_VITESSE_METRE_SECONDE ? " (" + round(msg.getData().getFloat("vitesse") * COEFF_CONVERSION_MS_KMH) + " KM/H" + ")" : " (" + round(msg.getData().getFloat("vitesse")) + " m/s)" ));
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

                    data.putFloat("vitesse", preff.getFloat("vitesse", 0.0f));
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
        // Enabling MyLocation in Google Map
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.// TODO: Consider calling

            return;
        }
        map.setMyLocationEnabled(true);

//        this.map = map;



//		LatLng userAdress = new LatLng(map.getMyLocation().getLatitude(), map.getMyLocation().getLongitude());
//		map.addMarker(new MarkerOptions().position(userAdress).title("my position"));
//		map.moveCamera(CameraUpdateFactory.newLatLng(userAdress));
//




    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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

                buttonSave = (ImageButton)alertDialog.findViewById(R.id.button_save);
                problemPreview = (ImageButton)alertDialog.findViewById(R.id.problem_preview);

                final Spinner problemLevel = (Spinner)alertDialog.findViewById(R.id.problem_level);
                final TextView problemLevelLabel = (TextView)alertDialog.findViewById(R.id.problem_level_label);
                buttonCancel = (ImageButton)alertDialog.findViewById(R.id.button_cancel);

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
                        // TODO Auto-generated method stub
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
                        // TODO Auto-generated method stub

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
                alertDialog = (AlertDialog) dialog;

                ImageButton chooseContact = (ImageButton)alertDialog.findViewById(R.id.choose_contact);
                Spinner companyName = (Spinner)alertDialog.findViewById(R.id.company_name);
                final EditText busMatriculationNumber = (EditText)alertDialog.findViewById(R.id.matriculation_number_of_bus);
                timeOfTravel = (EditText)alertDialog.findViewById(R.id.time_of_travel);
                final EditText travelDuration = (EditText)alertDialog.findViewById(R.id.journey_duration);
                guardianName = (EditText)alertDialog.findViewById(R.id.guardian_name);
                guardianPhoneNumber = (EditText)alertDialog.findViewById(R.id.guardian_phone_number);

                buttonSave = (ImageButton)alertDialog.findViewById(R.id.button_save);

                final ImageButton buttonCancel = (ImageButton)alertDialog.findViewById(R.id.button_cancel);

                chooseContact.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                        startActivityForResult(intent, PICK_CONTACT);
                    }
                });



                buttonSave.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub

                        if(busMatriculationNumber.getText().toString().equals("")||timeOfTravel.getText().toString().equals("")||travelDuration.getText().toString().equals("")||guardianName.getText().toString().equals("")||guardianPhoneNumber.getText().toString().equals("")){
                            Toast.makeText(getApplicationContext(), getString(R.string.provide_all_fields)+"...", Toast.LENGTH_LONG).show();
                            return;
                        }

                        if(!IsMatch(busMatriculationNumber.getText().toString(), "[A-Z]{2}[0-9]{3}[A-Z]{2}")){
                            Toast.makeText(getApplicationContext(), getString(R.string.incorrect_immatriculation_number)+"...", Toast.LENGTH_LONG).show();
                            return;
                        }

                        Toast.makeText(getApplicationContext(), getString(R.string.journey_saved_successfull)+" ... ", Toast.LENGTH_LONG).show();

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

                final TimePickerDialog.OnTimeSetListener time = new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // TODO Auto-generated method stub

                        myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        myCalendar.set(Calendar.MINUTE, minute);

                        updateDateVoyage();
                    }
                };

                final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        // TODO Auto-generated method stub
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);


                        new TimePickerDialog(MyPositionActivity.this, time, myCalendar
                                .get(Calendar.HOUR_OF_DAY), myCalendar.get(Calendar.MINUTE), true).show();

                    }

                };

                timeOfTravel.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        new DatePickerDialog(MyPositionActivity.this, date, myCalendar
                                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                                myCalendar.get(Calendar.DAY_OF_MONTH)).show();

                    }
                });
                break;
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
    }

    double round(double c){
        return Math.round(c*100)/100.0;
    }

}
