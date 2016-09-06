package com.satra.traveler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
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

import com.google.gson.Gson;
import com.satra.traveler.models.ResponsStatusMsg;
import com.satra.traveler.models.ResponsStatusMsgData;
import com.satra.traveler.utils.TConstants;
import com.satra.traveler.utils.Tutility;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.FileNotFoundException;
import java.io.IOException;

import mehdi.sakout.fancybuttons.FancyButton;

public class MainActivity extends Activity {

    final private static int DIALOG_SIGNUP = 1;
    private static int GET_FROM_GALLERY=2;
    EditText username, matricule, noTelephone;
    FancyButton buttonLogin;
    ImageButton profilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(getSharedPreferences(TConstants.TRAVELR_PREFERENCE, 0).contains(TConstants.PREF_USERNAME)&&
                getSharedPreferences(TConstants.TRAVELR_PREFERENCE, 0).contains(TConstants.PREF_PHONE)){

            final ProgressDialog progress = new ProgressDialog(MainActivity.this);
            progress.setIcon(R.mipmap.ic_launcher);
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.setTitle(getString(R.string.key_chargement));
            progress.setMessage(getString(R.string.key_account_creation_loading_msg));
            progress.show();

            new AsyncTask<Void, Void, ResponsStatusMsgData>(){
                @Override
                protected ResponsStatusMsgData doInBackground(Void... params) {
                    try {
                        HttpHeaders requestHeaders = new HttpHeaders();
                        //Create the request body as a MultiValueMap
                        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

                        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);
                        RestTemplate restTemplate = new RestTemplate(true);
                        Gson gson = new Gson();

                        ResponseEntity<String>  response = restTemplate.exchange(TConstants.GET_MAT_ID_URL+getSharedPreferences(TConstants.TRAVELR_PREFERENCE, 0)
                                .getString(TConstants.PREF_PHONE, ""), HttpMethod.GET, httpEntity, String.class);

                        Log.e("response get_mat", "response: "+response.getBody());
                        return gson.fromJson(response.getBody(), ResponsStatusMsgData.class);
                    } catch (Exception e) {
                        Log.e("MainActivity", e.getMessage(), e);
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(ResponsStatusMsgData response) {
                    if(response!=null && response.getStatus()==200){
                        try {
                            Toast.makeText(getApplicationContext(), R.string.mat_id_synchronization_success, Toast.LENGTH_LONG).show();
                            SharedPreferences prefs = getSharedPreferences(TConstants.TRAVELR_PREFERENCE, 0);
                            SharedPreferences.Editor editor = prefs.edit();
                            //TODO IndexOutOfBounds error ici quand la synchronisation ne s'effectue pas correctement
                            editor.putString(TConstants.PREF_MAT_ID, response.getData()[0].getId());
                            editor.putString(TConstants.PREF_MATRICULE, response.getData()[0].getCode());
                            editor.commit();

                            progress.dismiss();
                            Toast.makeText(getApplicationContext(), getString(R.string.connexion_with_username)+" "
                                            +getSharedPreferences(TConstants.TRAVELR_PREFERENCE, 0)
                                            .getString(TConstants.PREF_USERNAME, "anonyme-Travelr"),
                                    Toast.LENGTH_LONG)
                                    .show();
                            startActivity(new Intent(getApplicationContext(), MyPositionActivity.class));
                            finish();

                            Log.e("message", "reponse: "+response.getMessage());
                        } catch (Resources.NotFoundException e) {
                            e.printStackTrace();
                        } catch (IndexOutOfBoundsException iob){
                            iob.printStackTrace();
                        }
                    }
                    else{
                        progress.dismiss();
                        Log.e("message", "response: "+response);
                        Toast.makeText(getApplicationContext(), R.string.mat_id_synchronization_operation_failed, Toast.LENGTH_LONG).show();

                        if(getSharedPreferences(TConstants.TRAVELR_PREFERENCE, 0).contains(TConstants.PREF_MAT_ID)){
                            Toast.makeText(getApplicationContext(), getString(R.string.connexion_with_username)+" "
                                            +getSharedPreferences(TConstants.TRAVELR_PREFERENCE, 0)
                                            .getString(TConstants.PREF_USERNAME, "anonyme-Travelr"),
                                    Toast.LENGTH_LONG)
                                    .show();

                            startActivity(new Intent(getApplicationContext(), MyPositionActivity.class));
                            finish();
                        }
                        else{

                            Toast.makeText(getApplicationContext(), R.string.mat_id_synchronization_failed_no_mat_id_saved, Toast.LENGTH_LONG).show();
                        }

                    }
                }
            }.execute();
        }

        username = (EditText)findViewById(R.id.username);
        matricule = (EditText)findViewById(R.id.matricule1);
        noTelephone = (EditText)findViewById(R.id.no_telephone);
        noTelephone.setText(((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getLine1Number());

        buttonLogin = (FancyButton)findViewById(R.id.button_login);

        buttonLogin.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(username.getText().toString().isEmpty() ||noTelephone.getText().toString().isEmpty()){

                    Toast.makeText(getApplicationContext(), getString(R.string.provide_all_fields)+"...", Toast.LENGTH_LONG).show();
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

                                final String matriculeString = (matricule.getText().toString().equals(""))?"indisponible":matricule.getText().toString();
                                final String telephoneString = noTelephone.getText().toString();
                                final String usernameString = username.getText().toString();
                                final ProgressDialog progress = new ProgressDialog(MainActivity.this);
                                progress.setIcon(R.mipmap.ic_launcher);
                                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                progress.setIndeterminate(true);
                                progress.setTitle(getString(R.string.key_chargement));
                                progress.setMessage(getString(R.string.key_account_creation_loading_msg));
                                progress.show();

                                new AsyncTask<Void, Void, ResponsStatusMsg>(){
                                    @Override
                                    protected ResponsStatusMsg doInBackground(Void... params) {
                                        try {
                                            HttpHeaders requestHeaders = new HttpHeaders();
                                            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
                                            body.add(TConstants.REGISTRATION_URL_PARAM_CODE, matriculeString);
                                            body.add(TConstants.REGISTRATION_URL_PARAM_MSISDN, telephoneString);

                                            //Log.e("error", "no: "+telephoneString);
                                            HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);
                                            RestTemplate restTemplate = new RestTemplate(true);
                                            Gson gson = new Gson();

                                            ResponseEntity<String>  response = restTemplate.exchange(TConstants.REGISTRATION_URL, HttpMethod.POST, httpEntity, String.class);
                                            Log.e("Response", "res: "+response);
                                            Log.e("Response body", "body "+response.getBody());

                                            return gson.fromJson(response.getBody(), ResponsStatusMsg.class);
                                        } catch (Exception e) {
                                            Log.e("MainActivity", e.getMessage(), e);
                                        }

                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(ResponsStatusMsg response) {

                                        if(response!=null && response.getStatus()==100){
                                            Toast.makeText(getApplicationContext(), R.string.success_account_creation, Toast.LENGTH_LONG).show();

                                            SharedPreferences prefs = getSharedPreferences(TConstants.TRAVELR_PREFERENCE, 0);
                                            SharedPreferences.Editor editor = prefs.edit();
                                            editor.putString(TConstants.PREF_USERNAME, usernameString);
                                            editor.putString(TConstants.PREF_PHONE, telephoneString);
                                            editor.putString(TConstants.PREF_MATRICULE, matriculeString);
                                            editor.commit();

                                            new AsyncTask<Void, Void, ResponsStatusMsgData>(){
                                                @Override
                                                protected ResponsStatusMsgData doInBackground(Void... params) {
                                                    try {
                                                        // HttpAuthentication httpAuthentication = new HttpBasicAuthentication("username", "password");
                                                        HttpHeaders requestHeaders = new HttpHeaders();

                                                        //Create the request body as a MultiValueMap
                                                        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

                                                        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);
                                                        RestTemplate restTemplate = new RestTemplate(true);

                                                        Gson gson = new Gson();
                                                        ResponseEntity<String>  response = restTemplate.exchange(TConstants.GET_MAT_ID_URL+getSharedPreferences(TConstants.TRAVELR_PREFERENCE, 0)
                                                                .getString(TConstants.PREF_PHONE, ""), HttpMethod.GET, httpEntity, String.class);

                                                        Log.e("response get_mat", "response: "+response.getBody());
                                                        return gson.fromJson(response.getBody(), ResponsStatusMsgData.class);
                                                    } catch (Exception e) {
                                                        Log.e("MainActivity", e.getMessage(), e);
                                                    }

                                                    return null;
                                                }

                                                @Override
                                                protected void onPostExecute(ResponsStatusMsgData response) {

                                                    if(response!=null && response.getStatus()==200){
                                                        Toast.makeText(getApplicationContext(), R.string.mat_id_synchronization_success, Toast.LENGTH_LONG).show();

                                                        SharedPreferences prefs = getSharedPreferences(TConstants.TRAVELR_PREFERENCE, 0);
                                                        SharedPreferences.Editor editor = prefs.edit();
                                                        editor.putString(TConstants.PREF_MAT_ID, response.getData()[0].getId());
                                                        editor.commit();

                                                        progress.dismiss();

                                                        Toast.makeText(getApplicationContext(), getString(R.string.connexion_with_username)+" "
                                                                        +getSharedPreferences(TConstants.TRAVELR_PREFERENCE, 0)
                                                                        .getString(TConstants.PREF_USERNAME, "anonyme-Travelr"),
                                                                Toast.LENGTH_LONG)
                                                                .show();
                                                        startActivity(new Intent(getApplicationContext(), MyPositionActivity.class));
                                                        finish();

                                                        Log.e("message", "reponse: "+response.getMessage());
                                                    }
                                                    else{
                                                        progress.dismiss();
                                                        Log.e("message", "response: "+response);
                                                        Toast.makeText(getApplicationContext(), R.string.mat_id_synchronization_operation_failed, Toast.LENGTH_LONG).show();
                                                        Toast.makeText(getApplicationContext(), R.string.mat_id_synchronization_failed_no_mat_id_saved, Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            }.execute();
                                        }
                                        else{
                                            progress.dismiss();
                                            Log.e("message", "response: "+response);
                                            Toast.makeText(getApplicationContext(), R.string.echec_creation_compte_contact_serveur, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }.execute();

                            }
                        }
                );
                ad.show();
            }
        });

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
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);

                //se servir du compress pour envoyer le bitmap dans un outputstream vers le serveur

                profilePicture.setImageBitmap(Bitmap.createScaledBitmap(bitmap, profilePicture.getWidth(), profilePicture.getHeight(), false));

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            Tutility.showMessage(getApplicationContext(), R.string.about_message1, R.string.about_title1);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
