package com.satra.traveler;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.gson.Gson;
import com.satra.traveler.adapter.MessagingAdapter;
import com.satra.traveler.models.Messages;
import com.satra.traveler.models.ResponsStatusMsg;
import com.satra.traveler.utils.TConstants;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class MessagingActivity extends AppCompatActivity {

    private static final String LOGTAG = MessagingActivity.class.getSimpleName();
    EditText messageBox;
    private MessagingAdapter messagingAdapter;
    private RecyclerView messageRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        messageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        messageRecyclerView.setLayoutManager(layoutManager);
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
        setupMessageList();
    }

    private void setupMessageList(){
        messagingAdapter = new MessagingAdapter(this, Messages.listAll(Messages.class));
        messageRecyclerView.setAdapter(messagingAdapter);
    }

    @Nullable
    @Override
    public Intent getSupportParentActivityIntent() {
        return super.getSupportParentActivityIntent();
    }

    private void pushMessageOnline(final View view, final String message) {
        final ProgressDialog progress = new ProgressDialog(MessagingActivity.this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setMessage(getString(R.string.sending));
        progress.show();

        new AsyncTask<Void, Void, ResponsStatusMsg>(){
            String clientMatricule = getSharedPreferences(TConstants.TRAVELR_PREFERENCE, MODE_PRIVATE)
                    .getString(TConstants.PREF_MATRICULE,"");

            @Override
            protected ResponsStatusMsg doInBackground(Void... params) {
                try {
                    // HttpAuthentication httpAuthentication = new HttpBasicAuthentication("username", "password");
                    HttpHeaders requestHeaders = new HttpHeaders();
                    //Create the request body as a MultiValueMap
                    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
                    body.add(TConstants.POST_MESSAGE_PARAM_MESSAGE, message);
                    body.add(TConstants.POST_MESSAGE_PARAM_MAT_ID, getSharedPreferences(TConstants.TRAVELR_PREFERENCE,MODE_PRIVATE)
                            .getString(TConstants.PREF_MAT_ID, "0"));

                    HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);
                    RestTemplate restTemplate = new RestTemplate(true);

                    Gson gson = new Gson();
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
                if(response == null || response.getStatus()!=100){

                    String date = SimpleDateFormat.getDateInstance().format(new Date())+". "
                            +SimpleDateFormat.getTimeInstance().format(new Date());
                    /**
                     * TODO Quand un utilisateur essaie de sauvegarder encore via l'action optionel l'hors d'un echec d'envoie,
                     * le message ne doit pas encore etre sauvegarde une deuxieme fois en bd locale.
                     */
                    Messages mMessage = new Messages();
                    mMessage.setContent(message);
                    mMessage.setDate(date);
                    mMessage.setSender(clientMatricule);
                    mMessage.save();
                    setupMessageList();
                    Snackbar.make(messageRecyclerView, getString(R.string.error_message_send), Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.tryagain), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    pushMessageOnline(v, message);
                                }
                            }).show();
                }
                else{
                    /*
                    * TODO 2 after TO DO 1
                     * @author: STEVE
                    * POST DES MESSAGES SAUVEGARDES DANS LA BD LOCALE SUR LE SERVEUR EN LIGNE
                     */
                    //voici les messages qui sont dans la bd
                    Iterator<Messages> mMessages = Messages.findAll(Messages.class);

                    Log.d(LOGTAG, "Message Sent");
                    Snackbar.make(view, getString(R.string.message_sent), Snackbar.LENGTH_LONG)
                            .setAction("Undo", null).show();
                    setupMessageList();
                }
            }
        }.execute();
    }
}
