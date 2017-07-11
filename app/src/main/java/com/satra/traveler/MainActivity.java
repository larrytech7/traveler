package com.satra.traveler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.satra.traveler.models.User;
import com.satra.traveler.utils.TConstants;
import com.satra.traveler.utils.Tutility;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import mehdi.sakout.fancybuttons.FancyButton;

import static com.satra.traveler.utils.Tutility.stringToInt;

public class MainActivity extends AppCompatActivity {

    final private static int DIALOG_SIGNUP = 1;
    private static final int PICK_FIRST_CONTACT = 100;
    private static final int PICK_SECOND_CONTACT = 200;
    private static final String LOGTAG = "MainActivity";
    private static int contactToPick=0;
    private static int GET_FROM_GALLERY=2;
    EditText username, matricule, noTelephone, contact1EditText, contact2EditText;
    Spinner userCountry;
    FancyButton buttonLogin;
    ImageButton profilePicture, pickContactOne, pickContactTwo;

    //firebase fields
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressDialog progress;
    private EditText useremail;
    private String country = "cmr";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //user is logged-in. synchronise user info and send user to home screen
                    launchHomeActivity();
                } else {
                    //signed out. allow user to sign in
                    Log.d(LOGTAG, "user is signed out");
                }
            }
        };

        final SharedPreferences sharedPreferences = getSharedPreferences(TConstants.TRAVELR_PREFERENCE, MODE_PRIVATE);

        /*contact1EditText = (EditText) findViewById(R.id.emergencyContact1EditText);
        contact2EditText = (EditText) findViewById(R.id.emergencyContact2EditText);
        pickContactOne = (ImageButton) findViewById(R.id.buttonPickContactOne);
        pickContactTwo = (ImageButton) findViewById(R.id.buttonPickContactTwo);

        pickContactOne.setOnClickListener(this);
        pickContactTwo.setOnClickListener(this);*/

        //check if user account was already created and saved
        Iterator<User> musers = User.findAll(User.class);

        if (musers.hasNext()) {
            launchHomeActivity();

        } else {
            mAuth.addAuthStateListener(mAuthListener);
        }

        username = (EditText) findViewById(R.id.username);
        useremail = (EditText) findViewById(R.id.useremail);
        //matricule = (EditText)findViewById(R.id.matricule1);
        noTelephone = (EditText) findViewById(R.id.no_telephone);

         userCountry = (Spinner) findViewById(R.id.user_country);

        userCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ArrayAdapter<String> arrayAdapter = (ArrayAdapter<String>) parent.getAdapter();

                String country_code = getApplicationContext().getResources().getStringArray(R.array.countries_codes)[position];

                Log.e("country choosed", "country: "+arrayAdapter.getItem(position)+"\t code: "+
                        country_code);
                country = country_code;

                sharedPreferences.edit().putString(TConstants.PREF_COUNTRY,
                        country_code
                        ).apply();

       /*
                MainActivity.getResId(
                     prefs.getString(TConstants.PREF_COUNTRY, "cmr")+"_"+
                    "compagnies_names", R.array.class)
                */

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            noTelephone.setText(((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getLine1Number());
        } else {
            requestPermission(android.Manifest.permission.READ_PHONE_STATE);
        }

        buttonLogin = (FancyButton) findViewById(R.id.button_login);

        buttonLogin.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (username.getText().toString().isEmpty() ||
                        noTelephone.getText().toString().isEmpty()||
                        useremail.getText().toString().isEmpty() ) {

                    Toast.makeText(getApplicationContext(), getString(R.string.provide_all_fields) + "...", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!useremail.getText().toString().isEmpty() && !MyPositionActivity.IsMatch(useremail.getText().toString(), getString(R.string.email_regex_patern))) {
                    Toast.makeText(getApplicationContext(), getString(R.string.incorrect_email) + "...", Toast.LENGTH_LONG).show();
                    return;
                }

                final AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);

                ad.setTitle(R.string.username_confirm_title);
                ad.setMessage(getString(R.string.username_confirm_msg) + username.getText().toString() + getString(R.string.telephone_number_confirm_msg) + noTelephone.getText().toString() + " ?");
                ad.setNegativeButton(R.string.username_confirm_no_label,
                        new android.content.DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int arg1) {
                                dialog.dismiss();
                            }
                        }
                );
                ad.setPositiveButton(R.string.username_confirm_yes_label, new android.content.DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int arg1) {

                                //final String matriculeString = (matricule.getText().toString().isEmpty())?getString(R.string.information_not_available_label):matricule.getText().toString().toUpperCase();
                                String telephoneString = noTelephone.getText().toString();
                                String usernameString = username.getText().toString();
                                String userEmailString = useremail.getText().toString();
                                //final String contact1 = contact1EditText.getText().toString();
                                //final String contact2 = (contact2EditText.getText().toString().isEmpty())?getString(R.string.information_not_available_label):contact2EditText.getText().toString();
                                //new application/system user
                                final User tuser = new User();
                                tuser.setCurrent_matricule("indisponible");
                                tuser.setPassword(Tutility.getAuthenticationEmail(telephoneString));
                                tuser.setUseremail(userEmailString);
                                tuser.setUserCountry(country);
                                //tuser.setEmergency_primary(contact1);
                                //tuser.setEmergency_secondary(contact2);
                                tuser.setUserphone(telephoneString);
                                tuser.setDate_registered(SimpleDateFormat.getDateInstance().format(new Date()));
                                tuser.setUsername(usernameString);
                                tuser.setUpdated_at(System.currentTimeMillis());
                                //progress dialog to show ongoing process
                                final ProgressDialog progress = new ProgressDialog(MainActivity.this);
                                progress.setIcon(R.mipmap.ic_launcher);
                                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                progress.setIndeterminate(true);
                                progress.setTitle(getString(R.string.key_chargement));
                                progress.setMessage(getString(R.string.key_account_creation_loading_msg));
                                progress.show();
                                //signup user via firebase
                                mAuth.createUserWithEmailAndPassword(tuser.getUseremail(),
                                        Tutility.getAuthenticationEmail(telephoneString))
                                        .addOnSuccessListener(MainActivity.this, new OnSuccessListener<AuthResult>() {
                                            @Override
                                            public void onSuccess(AuthResult authResult) {
                                                //account creation succeeded
                                                sharedPreferences.edit().putString(TConstants.PREF_MATRICULE, tuser.getCurrent_matricule()).apply();
                                                sharedPreferences.edit().putString(TConstants.PREF_EMERGENCY_CONTACT_1, tuser.getEmergency_primary()).apply();
                                                //save user profile to device
                                                tuser.save();
                                                //get user and update display name
                                                FirebaseUser user = authResult.getUser();
                                                UserProfileChangeRequest updateRequest = new UserProfileChangeRequest.Builder()
                                                        .setDisplayName(tuser.getUsername())
                                                        .build();
                                                user.updateProfile(updateRequest);
                                                user.sendEmailVerification().addOnCompleteListener(MainActivity.this, new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isComplete())
                                                            Tutility.showMessage(MainActivity.this, getString(R.string.confirm_email), getString(R.string.app_name));
                                                        progress.dismiss();
                                                    }
                                                });
                                            }
                                        })
                                        .addOnFailureListener(MainActivity.this, new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //account creation failed, user may already exist
                                                progress.setMessage(getString(R.string.reauthenticate));
                                                Log.e("signintrial", "re-authenticating: " + e.getMessage());

                                                AuthCredential credential = EmailAuthProvider.getCredential(tuser.getUseremail(),tuser.getPassword());
                                                mAuth.signInWithCredential(credential).addOnSuccessListener(MainActivity.this, new OnSuccessListener<AuthResult>() {
                                                    @Override
                                                    public void onSuccess(AuthResult authResult) {
                                                        progress.dismiss();
                                                        tuser.save();
                                                        launchHomeActivity();
                                                    }
                                                });

                                            }
                                        });
                            }
                        }
                );
                ad.show();
            }
        });

        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(progress != null){
            progress.dismiss();
            progress = null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    private void requestPermission(String permission) {
        //ask user to grant permission to read fine location. Required for android 6.0+ API level 23+
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{permission},
                Tutility.stringToInt(permission));
    }

    private void launchHomeActivity(){
        startActivity(new Intent(MainActivity.this, MyPositionActivity.class));
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode== Tutility.stringToInt(android.Manifest.permission.READ_PHONE_STATE)&&
                grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            noTelephone.setText(((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getLine1Number());

        }

        else if (requestCode== Tutility.stringToInt(android.Manifest.permission.READ_CONTACTS)&&
                grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), contactToPick);

        }

    }

    @Override
    protected Dialog onCreateDialog(int id) {

        AlertDialog dialogDetails = null;

        switch (id) {
            case DIALOG_SIGNUP:
                LayoutInflater inflater = LayoutInflater.from(this);
                View dialogview = inflater.inflate(R.layout.new_account, null);

                AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(this);
                dialogbuilder.setTitle(R.string.create_profile);
                dialogbuilder.setView(dialogview);
                dialogDetails = dialogbuilder.create();

                break;
        }

        return dialogDetails;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {

        switch (id) {
            case DIALOG_SIGNUP:
                final AlertDialog alertDialog = (AlertDialog) dialog;

                final EditText usernameSignup = (EditText)alertDialog.findViewById(R.id.username);
                final EditText passwordSignup = (EditText)alertDialog.findViewById(R.id.password);
                final EditText passwordAgain = (EditText)alertDialog.findViewById(R.id.password_again);
                final EditText noTelephone = (EditText)alertDialog.findViewById(R.id.no_telephone);

                profilePicture = (ImageButton)alertDialog.findViewById(R.id.profile_picture);
                ImageButton buttonSave = (ImageButton)alertDialog.findViewById(R.id.button_save);
                ImageButton buttonCancel = (ImageButton)alertDialog.findViewById(R.id.button_cancel);

                buttonSave.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        if (usernameSignup.getText().toString().equals("") || passwordSignup.getText().toString().equals("") || passwordAgain.getText().toString().equals("") || noTelephone.getText().toString().equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.provide_all_fields) + "...", Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (!passwordAgain.getText().toString().equals(passwordAgain.getText().toString())) {
                            Toast.makeText(getApplicationContext(), getString(R.string.repeated_password_different_from_original) + "...", Toast.LENGTH_LONG).show();
                            return;
                        }

                        Toast.makeText(getApplicationContext(), getString(R.string.account_created) + " " + usernameSignup.getText(), Toast.LENGTH_LONG).show();

                        alertDialog.dismiss();
                    }
                });

                profilePicture.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(), getString(R.string.select_your_profile_image) + "...", Toast.LENGTH_LONG).show();
                        startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);

                    }
                });
                buttonCancel.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Detects request codes
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            Bitmap bitmap;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);

                //se servir du compress pour envoyer le bitmap dans un outputstream vers le serveur

                profilePicture.setImageBitmap(Bitmap.createScaledBitmap(bitmap, profilePicture.getWidth(), profilePicture.getHeight(), false));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if((requestCode==PICK_FIRST_CONTACT) && resultCode == Activity.RESULT_OK) {
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
                    String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    //set field with phone number retrieved
                    contact1EditText.setText(number);
                    phones.close();

                }
            }
        }

        if((requestCode==PICK_SECOND_CONTACT) && resultCode == Activity.RESULT_OK) {
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
                    String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    //set phone number retrieved in field
                    contact2EditText.setText(number);
                    phones.close();
                }
            }
        }

    }

    public static int getResId(String resName, Class<?> c) {

        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    /*@Override
    public void onClick(View v) {

        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);

        switch (v.getId()){
            case R.id.buttonPickContactOne:
                //OPen phonebook to pick contact

                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                        == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(intent, PICK_FIRST_CONTACT);
                }
                else{
                    contactToPick = PICK_FIRST_CONTACT;
                    requestPermission(android.Manifest.permission.READ_CONTACTS);
                }


                break;
            case R.id.buttonPickContactTwo:
                //Open phonebook to pick contact


                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                        == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(intent, PICK_SECOND_CONTACT);
                }
                else{
                    contactToPick = PICK_SECOND_CONTACT;
                    requestPermission(android.Manifest.permission.READ_CONTACTS);
                }
                break;
        }

    }
*/
}
