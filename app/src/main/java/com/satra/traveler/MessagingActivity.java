package com.satra.traveler;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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
import java.util.List;

import mehdi.sakout.fancybuttons.FancyButton;

public class MessagingActivity extends AppCompatActivity {

    private static final String LOGTAG = MessagingActivity.class.getSimpleName();
    private static final int CAPTURE_IMAGE_MESSAGE = 100;
    EditText messageBox;
    ImageView previewMessageImage;
    private MessagingAdapter messagingAdapter;
    private RecyclerView messageRecyclerView;
    private Bitmap attachedImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        previewMessageImage = (ImageView) findViewById(R.id.messageImageView);
        FancyButton buttonCaptureImage = (FancyButton) findViewById(R.id.buttonCaptureImage);
        buttonCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MessagingActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermission();
                }else{
                    //lance la Camera
                    startCameraCapture();
                }
            }
        });
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

    /**
     * Lancer la capture d'image via camera
     */
    private void startCameraCapture() {
        //capter image via camera
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAPTURE_IMAGE_MESSAGE);
    }

    /**
     * Require permission de prendre photo cia camera sur Android M+
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAPTURE_IMAGE_MESSAGE);
    }

    private void setupMessageList(){
        messagingAdapter = new MessagingAdapter(this, Messages.listAll(Messages.class,"date DESC"));
        messageRecyclerView.setAdapter(messagingAdapter);
    }

    @Nullable
    @Override
    public Intent getSupportParentActivityIntent() {
        return super.getSupportParentActivityIntent();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAPTURE_IMAGE_MESSAGE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //lancer capture pour android M+
                startCameraCapture();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == CAPTURE_IMAGE_MESSAGE){
                //TODO. Gerer l'image capturer ici pour envoyer sur le serveur
                //Uri imageData = data.getData();
                try{
                    attachedImage = (Bitmap) data.getExtras().get("data");
                    previewMessageImage.setImageBitmap(attachedImage);
/*                    previewMessageImage.setImageBitmap(Bitmap.createScaledBitmap(attachedImage, previewMessageImage.getWidth(),
                            previewMessageImage.getHeight(), false));*/
                    previewMessageImage.setVisibility(View.VISIBLE);
                }catch(Exception e){
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), getString(R.string.error_occur_please_retry)+"...", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void pushMessageOnline(final View view, final String message) {
        final ProgressDialog progress = new ProgressDialog(MessagingActivity.this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCanceledOnTouchOutside(false);
        progress.setMessage(getString(R.string.sending));
        progress.show();

        new AsyncTask<Void, Void, ResponsStatusMsg>(){
            String clientMatricule = getSharedPreferences(TConstants.TRAVELR_PREFERENCE, MODE_PRIVATE)
                    .getString(TConstants.PREF_MATRICULE,"");

            @Override
            protected ResponsStatusMsg doInBackground(Void... params) {
                try {
                    //TODO. Faudras gere aussi l'envoie de l'image capturer ci disponible. C'est un element non-facultatif
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
                previewMessageImage.setVisibility(View.GONE);
                if(response == null || response.getStatus()!=100){

                    String date = SimpleDateFormat.getDateInstance().format(new Date())+". "
                            +SimpleDateFormat.getTimeInstance().format(new Date());

                    Messages mMessage = new Messages();
                    mMessage.setContent(message);
                    mMessage.setDate(date);
                    mMessage.setSender(clientMatricule);
                    mMessage.setSent(0);
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
                    String date = SimpleDateFormat.getDateInstance().format(new Date())+". "
                            +SimpleDateFormat.getTimeInstance().format(new Date());

                    Messages mMessage = new Messages();
                    mMessage.setContent(message);
                    mMessage.setDate(date);
                    mMessage.setSender(clientMatricule);
                    mMessage.setSent(1); //mettre a jour le status d'un message renvoyer ou ajoute un nouveau message
                    mMessage.save();
                    /*
                    * TODO 2 after TO DO 1
                     * @author: STEVE
                    * POST DES MESSAGES SAUVEGARDES DANS LA BD LOCALE SUR LE SERVEUR EN LIGNE
                     */
                    //voici les messages qui sont dans la bd et qui n'ont pas encore ete enregistrer en ligne
                    List<Messages> mMessages = Messages.find(Messages.class, "sent = ?",String.valueOf(0));

                    Log.d(LOGTAG, "Message Sent");
                    Snackbar.make(view, getString(R.string.message_sent), Snackbar.LENGTH_LONG)
                            .setAction("Undo", null).show();
                    setupMessageList();
                }
            }
        }.execute();
    }
}
