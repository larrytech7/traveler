package com.satra.traveler;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.gson.Gson;
import com.satra.traveler.models.ResponsStatusMsg;
import com.satra.traveler.utils.TConstants;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class MessagingActivity extends AppCompatActivity {

    EditText messageBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        messageBox = (EditText)findViewById(R.id.messageText);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabSend);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(!messageBox.getText().toString().equals("")){
                    pushMessageOnline(view, messageBox.getText().toString());
                    messageBox.setText("");
                }


            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Nullable
    @Override
    public Intent getSupportParentActivityIntent() {
        return super.getSupportParentActivityIntent();
    }

    private void pushMessageOnline(final View view, final String message) {
        final ProgressDialog progress = new ProgressDialog(MessagingActivity.this);
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
                    // HttpAuthentication httpAuthentication = new HttpBasicAuthentication("username", "password");
                    HttpHeaders requestHeaders = new HttpHeaders();
                    //requestHeaders.setAuthorization(httpAuthentication);
                    // requestHeaders.setAccept(Collections.singletonList(new MediaType("application", "json")));
                    // requestHeaders.setContentType(MediaType.APPLICATION_JSON);

                    //Create the request body as a MultiValueMap
                    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
                    body.add(TConstants.POST_MESSAGE_PARAM_MESSAGE, message);
                    body.add(TConstants.POST_MESSAGE_PARAM_MAT_ID, getSharedPreferences(TConstants.TRAVELR_PREFERENCE, 0)
                            .getString(TConstants.PREF_MAT_ID, "0"));

                    //Log.e("error", "no: "+telephoneString);


                    HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);
                    RestTemplate restTemplate = new RestTemplate(true);

                    Gson gson = new Gson();


                    //restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());
                    //restTemplate.getMessageConverters().add(new StringHttpMessageConverter());



                    ResponseEntity<String> response = restTemplate.exchange(TConstants.POST_MESSAGE_URL, HttpMethod.POST, httpEntity, String.class);
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

                progress.dismiss();
                if(response==null || response.getStatus()!=100){

                    /*
                    * TO DO 1
                     * @author: HARRY
                    * SAUVEGARDE DES MESSAGES DANS LA BD LOCALE
                     */

                    Snackbar.make(view, "Failed to connect to server. Message SAVED offline", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                }
                else{
                    /*
                    * TO DO 2 after TO DO 1
                     * @author: STEVE
                    * POST DES MESSAGES SAUVEGARDES DANS LA BD LOCALE SUR LE SERVEUR EN LIGNE
                     */

                    Snackbar.make(view, "Message SENT", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

            }
        }.execute();
    }
}
