package com.satra.traveler;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.satra.traveler.adapter.MessagingAdapter;
import com.satra.traveler.models.Messages;
import com.satra.traveler.models.ResponsStatusMsg;
import com.satra.traveler.models.User;
import com.satra.traveler.utils.TConstants;
import com.satra.traveler.utils.Tutility;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import mehdi.sakout.fancybuttons.FancyButton;

public class MessagingActivity extends AppCompatActivity {

    private static final String LOGTAG = MessagingActivity.class.getSimpleName();
    private static final int CAPTURE_IMAGE_MESSAGE = 100;
    EditText messageBox;
    private ImageView previewMessageImage;

    private  RecyclerView messageRecyclerView;
    private static ProgressDialog progress;
    private String clientMatricule;
    private String clientName;
    private SharedPreferences sharedPreferences;
    private User travelerUser;

    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreferences = getSharedPreferences(TConstants.TRAVELR_PREFERENCE, MODE_PRIVATE);

        travelerUser = User.findAll(User.class).next();
        //initialize sender variables
        clientMatricule = travelerUser == null? "" : travelerUser.getCurrent_matricule();
        clientName = travelerUser == null? "": travelerUser.getUsername();//sharedPreferences.getString(TConstants.PREF_USERNAME,"");
        //prepare message reference base for sending and receiving messages
        reference = FirebaseDatabase.getInstance().getReference(Tutility.FIREBASE_MESSAGES)
                .child(travelerUser.getCurrent_matricule());
        reference.keepSynced(true);
        //setup remaining view
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
        messageRecyclerView.setHasFixedSize(true);
        messageBox = (EditText)findViewById(R.id.messageText);

        FancyButton fab = (FancyButton) findViewById(R.id.fabSend);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!messageBox.getText().toString().equals("")){
                    String message = messageBox.getText().toString();
                    messageBox.setText("");
                    //prepare message
                    Messages textMessage = new Messages();
                    textMessage.setAuthor(travelerUser.getUsername());
                    textMessage.setSender(travelerUser.getCurrent_matricule());
                    textMessage.setPhonenumber(travelerUser.getUserphone());
                    textMessage.setContent(message);
                    textMessage.setTimestamp(System.currentTimeMillis());
                    textMessage.setDate(SimpleDateFormat.getDateInstance().format(new Date(textMessage.getTimestamp())));
                    textMessage.setImageUrl(""); //TODO. If image available , send first before message

                    //push to reference
                    final String key = reference.push().getKey();
                    reference.child(key)
                            .setValue(textMessage)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //if task completed, update sent status
                                    if (task.isSuccessful()){
                                        Map<String, Object> statusUpdate = new HashMap<>();
                                        statusUpdate.put("sent", 1);
                                        reference.child(key).updateChildren(statusUpdate);
                                    }
                                }
                            });
                    //pushMessageOnline(MessagingActivity.this, view, message, null);

                }
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setupMessageList(this);
    }

    /**
     * Lancer la capture d'image via camera
     */
    private void startCameraCapture() {
        //capter image via camera pour ajouter au message
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAPTURE_IMAGE_MESSAGE);
    }

    /**
     * Require permission de prendre photo cia camera sur Android M+
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAPTURE_IMAGE_MESSAGE);
    }


    private void setupMessageList(Context context){

        //populate list view
        MessagingAdapter messagingAdapter = new MessagingAdapter(Messages.class, R.layout.item_message_layout,
                MessagingAdapter.ViewHolder.class,reference,
                null,travelerUser,MessagingActivity.this);
        messageRecyclerView.setAdapter(messagingAdapter);
        messageRecyclerView.scrollToPosition(messagingAdapter.getItemCount() - 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            ((MessagingAdapter)messageRecyclerView.getAdapter()).cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
                    Bitmap attachedImage = (Bitmap) data.getExtras().get("data");
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

    private void pushMessageOnline(final Context context, final View view, final String message, final Messages oMessage) {
        if(view!=null){
            progress = new ProgressDialog(context);
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.setMessage(context.getString(R.string.sending));
            progress.show();
        }

        new AsyncTask<Void, Void, ResponsStatusMsg>(){

            @Override
            protected ResponsStatusMsg doInBackground(Void... params) {
                try {
                    //TODO. Faudras gere aussi l'envoie de l'image capturer ci disponible. C'est un element non-facultatif
                    // HttpAuthentication httpAuthentication = new HttpBasicAuthentication("username", "password");
                    HttpHeaders requestHeaders = new HttpHeaders();
                    //Create the request body as a MultiValueMap
                    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

                    body.add(TConstants.POST_MESSAGE_PARAM_TIMESTAMP, System.currentTimeMillis()+"");
                    body.add(TConstants.POST_MESSAGE_PARAM_MESSAGE, message);
                    body.add(TConstants.POST_MESSAGE_PARAM_MAT_ID, MyPositionActivity.isCurrentTripExist()?sharedPreferences
                            .getString(MyPositionActivity.getCurrentTrip().getBus_immatriculation(), sharedPreferences
                                    .getString(TConstants.PREF_MAT_ID, "0")):sharedPreferences
                            .getString(TConstants.PREF_MAT_ID, "0"));

                    body.add(TConstants.POST_MESSAGE_PARAM_MATRICULE, MyPositionActivity.isCurrentTripExist()?
                            MyPositionActivity.getCurrentTrip().getBus_immatriculation():sharedPreferences.getString(TConstants.PREF_MATRICULE, "0"));
                    body.add(TConstants.POST_MESSAGE_PARAM_MSISDN, sharedPreferences
                            .getString(TConstants.PREF_PHONE, "0"));
                    body.add(TConstants.POST_MESSAGE_PARAM_USERNAME, sharedPreferences
                            .getString(TConstants.PREF_USERNAME, "0"));
                    body.add(TConstants.POST_MESSAGE_SPEED, sharedPreferences.getString(TConstants.SPEED_PREF, "0"));
                    body.add(TConstants.POST_MESSAGE_AGENCY, MyPositionActivity.isCurrentTripExist()?
                            MyPositionActivity.getCurrentTrip().getAgency_name():"unregistered");
                    body.add(TConstants.POST_MESSAGE_DEPARTURE, MyPositionActivity.isCurrentTripExist()?
                            MyPositionActivity.getCurrentTrip().getDeparture():"NONE");
                    body.add(TConstants.POST_MESSAGE_DESTINATION, MyPositionActivity.isCurrentTripExist()?
                            MyPositionActivity.getCurrentTrip().getDestination():"NONE");

                    Log.e("body params", "body: "+body.toString());

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

                if(view!=null){
                    progress.dismiss();
                    previewMessageImage.setVisibility(View.GONE);
                }

                String date = SimpleDateFormat.getDateInstance().format(new Date())+". "
                        +SimpleDateFormat.getTimeInstance().format(new Date());
                //message not sent or server error occured. Save message offline and load list
                if((response == null || response.getStatus()!=100)){
                    Messages mMessage=null;
                    if(oMessage==null){

                        /**
                         * TODO Quand un utilisateur essaie de sauvegarder encore via l'action optionel l'hors d'un echec d'envoie,
                         * le message ne doit pas encore etre sauvegarde une deuxieme fois en bd locale.
                         */
                        mMessage = new Messages();
                        mMessage.setContent(message);
                        mMessage.setDate(date);
                        mMessage.setSender(clientMatricule);
                        mMessage.setAuthor(clientName);
                        mMessage.setSent(0);
                        mMessage.save();
                        if(view!=null){
                            setupMessageList(context);
                        }
                    }
                    else{
                        mMessage = oMessage;
                    }

                    final Messages omMessage = mMessage;

                    if(view!=null){
                        Snackbar.make(messageRecyclerView, context.getString(R.string.error_message_send)+"\n\""+message+"\"", Snackbar.LENGTH_LONG)
                                .setAction(context.getString(R.string.tryagain), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        pushMessageOnline(context, v, message, omMessage);
                                    }
                                }).show();
                    }

                }
                else{ //message sent successfully. set successful and save to DB

                    Messages mMessage=null;
                    if(oMessage==null){

                        mMessage = new Messages();
                        mMessage.setContent(message);
                        mMessage.setDate(date);
                        mMessage.setSender(clientMatricule);
                        mMessage.setAuthor(clientName);
                    }
                    else{
                        mMessage = oMessage;
                    }
                    mMessage.setSent(1);
                    mMessage.save();
                    if(view!=null){
                        setupMessageList(context);
                    }

                    /*
                     * @author: STEVE
                    * POST DES MESSAGES SAUVEGARDES DANS LA BD LOCALE SUR LE SERVEUR EN LIGNE
                     */
                    //voici les messages qui sont dans la bd et qui n'ont pas encore ete enregistrer en ligne

                    Iterator<Messages> mMessages = Messages.find(Messages.class, "sent = ?", "0").iterator();

                    if(mMessages.hasNext()){
                        Messages nextMessage = mMessages.next();
                        pushMessageOnline(context, view, nextMessage.getContent(), nextMessage);
                    }
                    else{
                        Log.d(LOGTAG, "Message Sent");
                        if(view!=null){
                            Snackbar.make(view, context.getString(R.string.message_sent), Snackbar.LENGTH_LONG)
                                    .show();
                            setupMessageList(context);
                        }
                    }
                }
            }
        }.execute();
    }

    public void tryToSentDataOnline(Context context){
        Iterator<Messages> mMessages = Messages.find(Messages.class, "sent = ?", "0").iterator();
        if(mMessages.hasNext()){
            Messages mMessage = mMessages.next();
            pushMessageOnline(context, null, mMessage.getContent(), mMessage);
        }
    }
}
