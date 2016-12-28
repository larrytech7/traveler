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
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.satra.traveler.models.User;
import com.satra.traveler.utils.TConstants;
import com.satra.traveler.utils.Tutility;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import mehdi.sakout.fancybuttons.FancyButton;

public class MainActivity extends Activity implements OnClickListener {

    final private static int DIALOG_SIGNUP = 1;
    private static final int PICK_FIRST_CONTACT = 100;
    private static final int PICK_SECOND_CONTACT = 200;
    private static final String LOGTAG = "MainActivity";
    private static int contactToPick=0;
    private static int GET_FROM_GALLERY=2;
    EditText username, matricule, noTelephone, contact1EditText, contact2EditText;
    FancyButton buttonLogin;
    ImageButton profilePicture, pickContactOne, pickContactTwo;

    //firebase fields
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    public static Integer stringToInt(String str){
        if(str.length()==0) return 0;
        else return (str.charAt(0)+stringToInt(str.substring(1)))%256;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null){
                    //user is logged-in. synchronise user info and send user to home screen
                    Log.d(LOGTAG, "user is signed in");
                    launchHomeActivity();
                }else{
                    //signed out. allow user to sign in
                    Log.d(LOGTAG, "user is signed out");
                }
            }
        };

        final SharedPreferences sharedPreferences = getSharedPreferences(TConstants.TRAVELR_PREFERENCE, MODE_PRIVATE);

        contact1EditText = (EditText) findViewById(R.id.emergencyContact1EditText);
        contact2EditText = (EditText) findViewById(R.id.emergencyContact2EditText);
        pickContactOne = (ImageButton) findViewById(R.id.buttonPickContactOne);
        pickContactTwo = (ImageButton) findViewById(R.id.buttonPickContactTwo);

        pickContactOne.setOnClickListener(this);
        pickContactTwo.setOnClickListener(this);
        //check if user account was already created and saved
        Iterator<User> musers = User.findAll(User.class);
        if (musers.hasNext()){
            //Log.d(LOGTAG, "User available: "+musers.next().getUsername());
            final ProgressDialog progress = new ProgressDialog(MainActivity.this);
            progress.setIcon(R.mipmap.ic_launcher);
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.setCanceledOnTouchOutside(false);
            progress.setTitle(getString(R.string.key_chargement));
            progress.setMessage(getString(R.string.key_account_creation_loading_msg));
            try {
                progress.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //synchronize with firebase and login
            User user = musers.next();
            mAuth.signInWithEmailAndPassword(Tutility.getAuthenticationEmail(user.getUserphone()),
                    Tutility.getAuthenticationEmail(user.getUserphone()));
            //update user info in firebase. Works even if offline
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Tutility.FIREBASE_USER);
            reference
                    .child(user.getUserphone())
                    .setValue(user)
                    .addOnCompleteListener(MainActivity.this,
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                        Log.d(LOGTAG, "User synchronisation succeeded");
                                    else
                                        Log.d(LOGTAG, "User synchronisation failed");
                                    progress.dismiss();
                                    launchHomeActivity();
                                }
                            });

        }

        username = (EditText)findViewById(R.id.username);
        matricule = (EditText)findViewById(R.id.matricule1);
        noTelephone = (EditText)findViewById(R.id.no_telephone);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            noTelephone.setText(((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getLine1Number());
        }
        else{
            requestPermission(android.Manifest.permission.READ_PHONE_STATE);
        }

        buttonLogin = (FancyButton)findViewById(R.id.button_login);

        buttonLogin.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(username.getText().toString().isEmpty() ||
                        noTelephone.getText().toString().isEmpty() ||
                        contact1EditText.getText().toString().isEmpty()){

                    Toast.makeText(getApplicationContext(), getString(R.string.provide_all_fields)+"...", Toast.LENGTH_LONG).show();
                    return;
                }

                if(!matricule.getText().toString().isEmpty()&&!MyPositionActivity.IsMatch(matricule.getText().toString().toUpperCase(), getString(R.string.car_immatriculation_regex_patern))){
                    Toast.makeText(getApplicationContext(), getString(R.string.incorrect_immatriculation_number)+"...", Toast.LENGTH_LONG).show();
                    return;
                }

                final AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);

                ad.setTitle(R.string.username_confirm_title);
                ad.setMessage(getString(R.string.username_confirm_msg) + username.getText().toString() + getString(R.string.telephone_number_confirm_msg)+noTelephone.getText().toString()+" ?");
                ad.setNegativeButton(R.string.username_confirm_no_label,
                        new android.content.DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int arg1) {
                                dialog.dismiss();
                            }
                        }
                );
                ad.setPositiveButton(R.string.username_confirm_yes_label, new android.content.DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int arg1) {

                                final String matriculeString = (matricule.getText().toString().isEmpty())?getString(R.string.information_not_available_label):matricule.getText().toString().toUpperCase();
                                final String telephoneString = noTelephone.getText().toString();
                                final String usernameString = username.getText().toString();
                                final String contact1 = contact1EditText.getText().toString();
                                final String contact2 = (contact2EditText.getText().toString().isEmpty())?getString(R.string.information_not_available_label):contact2EditText.getText().toString();
                                //new application/system user
                                final User tuser = new User();
                                tuser.setCurrent_matricule(matriculeString);
                                tuser.setPassword(Tutility.getAuthenticationEmail(telephoneString));
                                tuser.setEmergency_primary(contact1);
                                tuser.setEmergency_secondary(contact2);
                                tuser.setUserphone(telephoneString);
                                tuser.setDate_registered(SimpleDateFormat.getDateInstance().format(new Date()));
                                tuser.setUseremail(Tutility.getAuthenticationEmail(telephoneString));
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
                                mAuth.createUserWithEmailAndPassword(Tutility.getAuthenticationEmail(telephoneString),
                                        Tutility.getAuthenticationEmail(telephoneString))
                                .addOnSuccessListener(MainActivity.this,new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        //account creation succeeded
                                        Log.d(LOGTAG, "Account created!");
                                        sharedPreferences.edit().putString(TConstants.PREF_MATRICULE, tuser.getCurrent_matricule()).apply();
                                        sharedPreferences.edit().putString(TConstants.PREF_EMERGENCY_CONTACT_1, tuser.getEmergency_primary()).apply();
                                        //save user profile to device
                                        tuser.save();
                                        //get user and update display name
                                        FirebaseUser user = authResult.getUser();
                                        UserProfileChangeRequest updateRequest = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(usernameString)
                                                .build();
                                        user.updateProfile(updateRequest)
                                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        Log.d(LOGTAG, "Display name updated Successfully");
                                                    }
                                                    progress.dismiss();
                                                    launchHomeActivity();
                                                }
                                            });
                                    }
                                })
                                .addOnFailureListener(MainActivity.this, new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //account creation failed
                                        progress.dismiss();
                                        Tutility.showMessage(MainActivity.this, getString(R.string.signinerror, e.getMessage()),getString(R.string.app_name));
                                    }
                                });
                            }
                        }
                );
                ad.show();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
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
                stringToInt(permission));
    }

    private void launchHomeActivity(){
        startActivity(new Intent(MainActivity.this, MyPositionActivity.class));
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode==stringToInt(android.Manifest.permission.READ_PHONE_STATE)&&
                grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            noTelephone.setText(((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getLine1Number());

        }

        else if (requestCode==stringToInt(android.Manifest.permission.READ_CONTACTS)&&
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            Tutility.showMessage(getApplicationContext(), R.string.about_message1, R.string.about_title1);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
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

}
