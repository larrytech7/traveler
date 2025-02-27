package com.satra.traveler;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.popalay.tutors.TutorialListener;
import com.popalay.tutors.Tutors;
import com.popalay.tutors.TutorsBuilder;
import com.satra.traveler.adapter.MessagingAdapter;
import com.satra.traveler.models.Messages;
import com.satra.traveler.models.Rewards;
import com.satra.traveler.models.User;
import com.satra.traveler.utils.TConstants;
import com.satra.traveler.utils.TpointsListener;
import com.satra.traveler.utils.Tutility;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import mehdi.sakout.fancybuttons.FancyButton;

import static android.R.attr.data;
import static android.R.attr.numberPickerStyle;

public class MessagingActivity extends AppCompatActivity implements TutorialListener, TpointsListener{

    private static final String LOGTAG = MessagingActivity.class.getSimpleName();
    private static final int CAPTURE_IMAGE_MESSAGE = 100;
    EditText messageBox;///, extraMatriculeEditText;
    private ImageView previewMessageImage;
    private ProgressBar progressBar;

    private  RecyclerView messageRecyclerView;
    private static ProgressDialog progress;
    private String clientMatricule;
    private String clientName;
    private SharedPreferences sharedPreferences;
    private User travelerUser;

    DatabaseReference reference;
    StorageReference storageReference;
    private FrameLayout imageFrame;
    private FloatingActionButton fab, fabIncident;
    FloatingActionMenu fam; //
    private String imageUrl;
    private Tutors tutors;
    private Iterator<Map.Entry<String, View>> iterator;
    private Intent appLinkIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreferences = getSharedPreferences(TConstants.TRAVELR_PREFERENCE, MODE_PRIVATE);

        travelerUser = User.findAll(User.class).next();
        //initialize sender variables
        clientMatricule = travelerUser == null ? "" : travelerUser.getCurrent_matricule();
        clientName = travelerUser == null ? "" : travelerUser.getUsername();//sharedPreferences.getString(TConstants.PREF_USERNAME,"");
        //prepare message reference base for sending and receiving messages
        reference = FirebaseDatabase.getInstance().getReference(Tutility.FIREBASE_MESSAGES)
                .child(clientMatricule);
        storageReference = FirebaseStorage.getInstance().getReference(TConstants.FIREBASE_MEDIA_DATA);
        reference.keepSynced(true);
        //setup remaining view
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        previewMessageImage = (ImageView) findViewById(R.id.messageImageView);
        imageFrame = (FrameLayout) findViewById(R.id.imageFrameLayout);
        FancyButton buttonCaptureImage = (FancyButton) findViewById(R.id.buttonCaptureImage);
        buttonCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MessagingActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermission();
                } else {
                    //lance la Camera
                    startCameraCapture();
                }
            }
        });
        messageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        messageRecyclerView.setLayoutManager(layoutManager);
        messageRecyclerView.setHasFixedSize(true);
        messageBox = (EditText) findViewById(R.id.messageText);
        //extraMatriculeEditText = (EditText) findViewById(R.id.matriculeEditText);

        /*fam = (FloatingActionMenu) findViewById(R.id.fab_menu);
        fabIncident = (FloatingActionButton) findViewById(R.id.fabSendIncident);*/
        fab = (FloatingActionButton) findViewById(R.id.fabSend);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!messageBox.getText().toString().equals("")) {
                    String message = messageBox.getText().toString();
                    messageBox.setText("");
                    //prepare message
                    Messages textMessage = new Messages();
                    textMessage.setAuthor(clientName);
                    textMessage.setSender(clientMatricule);
                    textMessage.setPhonenumber(travelerUser.getUserphone());
                    textMessage.setContent(message);
                    textMessage.setCategory("simple");
                    textMessage.setTimestamp(System.nanoTime());
                    textMessage.setDate(new SimpleDateFormat("MMMM dd, yyyy HH:mm:ss a", Locale.US).format(Calendar.getInstance().getTime()));
                    textMessage.setImageUrl(imageUrl); //If image available , send first before message
                    //TODO: Machine learning can be used to determine the type (incident or report) of message sent.
                    //push message to reference
                    final String key = reference.push().getKey();
                    reference.child(key)
                            .setValue(textMessage)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //if task completed, update sent status
                                    if (task.isSuccessful()) {
                                        Map<String, Object> statusUpdate = new HashMap<>();
                                        statusUpdate.put("sent", 1);
                                        reference.child(key).updateChildren(statusUpdate);
                                        //TODO: Find a reliable way to verify that the message was genuinely sent before evaluating tpoints
                                        boolean isTpoints = isTpointsUpdated(null);
                                        if (isTpoints) {
                                            Tutility.showDialog(MessagingActivity.this, getString(R.string.rewards_title),
                                                    getString(R.string.travel_rewards_point, TConstants.MAX_REWARDS),
                                                    SweetAlertDialog.CUSTOM_IMAGE_TYPE);
                                        }
                                    }
                                }
                            });
                    imageFrame.setVisibility(View.GONE);
                    //pushMessageOnline(MessagingActivity.this, view, message, null);

                } else {
                    Toast.makeText(MessagingActivity.this, getString(R.string.no_message), Toast.LENGTH_LONG).show();
                }
            }
        });
        /*fabIncident.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                //get matricule from editText if incident is not on actual vehicle
                if (!extraMatriculeEditText.getText().toString().isEmpty()
                        && !MyPositionActivity.IsMatch(extraMatriculeEditText.getText().toString(), getString(R.string.cmr_car_immatriculation_regex_patern))){
                    Tutility.showMessage(MessagingActivity.this, R.string.incorrect_immatriculation_number,R.string.app_name);
                    return;
                }
                if(!messageBox.getText().toString().equals("")){
                    String message = messageBox.getText().toString();
                    messageBox.setText("");
                    extraMatriculeEditText.setText("");
                    //prepare message
                    Messages textMessage = new Messages();
                    textMessage.setAuthor(travelerUser.getUsername());
                    textMessage.setSender(travelerUser.getCurrent_matricule());
                    textMessage.setPhonenumber(travelerUser.getUserphone());
                    textMessage.setContent(message);
                    textMessage.setCategory("incident");
                    textMessage.setTimestamp(System.nanoTime());
                    textMessage.setDate(new SimpleDateFormat("MMMM dd, yyyy HH:mm:ss a", Locale.US).format(Calendar.getInstance().getTime()));
                    textMessage.setImageUrl(imageUrl); //If image available , send first before message

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
                    imageFrame.setVisibility(View.GONE);
                }
            }
        });*/
       /* fam.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean opened) {
                if (opened) {
                    // show extra message box for matricule
                    extraMatriculeEditText.setVisibility(View.VISIBLE);
                    fab.setVisibility(View.VISIBLE);
                    fabIncident.setVisibility(View.VISIBLE);
                } else {
                    // hide extra message for matricule
                    extraMatriculeEditText.setVisibility(View.GONE);
                    fab.setVisibility(View.GONE);
                    fabIncident.setVisibility(View.GONE);
                }
            }
        });*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setupMessageList();
        // ATTENTION: This was auto-generated to handle app links.
        appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        tutors = new TutorsBuilder()
                .textColorRes(android.R.color.white)
                .shadowColorRes(R.color.shadow)
                .textSizeRes(R.dimen.textNormal)
                .completeIconRes(R.drawable.ic_cross_24_white)
                .nextButtonTextRes(R.string.action_next)
                .completeButtonTextRes(R.string.action_got_it)
                .spacingRes(R.dimen.spacingNormal)
                .lineWidthRes(R.dimen.lineWidth)
                .cancelable(true)
                .build();
        tutors.setListener(this);
        HashMap<String, View> tutorials = new HashMap<>();
        tutorials.put(getString(R.string.message_text_hint), findViewById(R.id.messageText));
        tutorials.put(getString(R.string.message_picture_hint), findViewById(R.id.buttonCaptureImage));
        tutorials.put(getString(R.string.message_button_hint), fab);
        iterator = tutorials.entrySet().iterator();
        boolean showHints = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Tutility.SHOW_HINTS, true);
        //Check preference if first time so as to know if to show hints or not
        if (showHints)
            showHint(iterator);

        //TODO: Handle appIntent here for replies as instant messages
    }

    private void showHint(Iterator<Map.Entry<String, View>> iterator) {
        if (iterator == null){
            return;
        }
        if (iterator.hasNext()) {
            Map.Entry<String, View> data = iterator.next();
            tutors.show(getSupportFragmentManager(), data.getValue(),
                    data.getKey(),
                    !iterator.hasNext());
        }
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


    private void setupMessageList(){

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
            if (tutors != null)
                tutors = null;
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
                //Gerer l'image capturer ici pour envoyer sur le serveur
                //Uri imageData = data.getData();
                try{
                    Bitmap attachedImage = (Bitmap) data.getExtras().get("data");

                    previewMessageImage.setImageBitmap(attachedImage);
/*                    previewMessageImage.setImageBitmap(Bitmap.createScaledBitmap(attachedImage, previewMessageImage.getWidth(),
                            previewMessageImage.getHeight(), false));*/
                    imageFrame.setVisibility(View.VISIBLE);
                    //upload to firebase
                    Uri file = data.getData();
                    if (file != null){
                        toggleSendButton();
                        storageReference.child(TConstants.FIREBASE_MEDIA_DATA_IMAGES)
                                .putFile(file)
                                .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        //indicate success on image upload
                                        progressBar.setVisibility(View.GONE);
                                        toggleSendButton();
                                        imageUrl = taskSnapshot.getDownloadUrl().toString();
                                    }
                                })
                                .addOnFailureListener(this, new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //alert or indicate error on image
                                        Tutility.showMessage(MessagingActivity.this, R.string.error_upload_image, R.string.app_name);
                                        imageFrame.setVisibility(View.GONE);
                                        toggleSendButton();
                                    }
                                });
                    }else{
                        //alert invalid image
                        Tutility.showMessage(this, R.string.invalid_image, R.string.app_name);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), getString(R.string.error_occur_please_retry)+"...", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void toggleSendButton(){
        //activating button
        if (fab.isActivated())
            fab.setActivated(false);
        else
            fab.setActivated(true);
        //enabling button
        if (fab.isEnabled())
            fab.setEnabled(false);
        else
            fab.setEnabled(true);
    }

    @Override
    public void onNext() {
        showHint(iterator);
    }

    @Override
    public void onComplete() {
        tutors.close();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(Tutility.SHOW_HINTS, false).apply();
    }

    @Override
    public void onCompleteAll() {
        tutors.close();
        //set preference not to show hints again next time activity launches
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(Tutility.SHOW_HINTS, false).apply();
    }

    /**
     * Commenting at least 4 times on a given journey should earn users some tpoints
     * @param c object to provide more criteria
     * @return whether or not points have been gained from action
     */
    @Override
    public boolean isTpointsUpdated(Object c) {
        if (!MyPositionActivity.isCurrentTripExist())
            return false;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        long msgfactor = sp.getLong(TConstants.MESSAGING_FACTOR, 0) + 1;
        sp.edit().putLong(TConstants.MESSAGING_FACTOR, msgfactor).apply();
        if (msgfactor % 4 == 0){
            Rewards rewards = Tutility.getAppRewards();
            rewards.setAppComments(TConstants.MAX_REWARDS);
            rewards.save();
            return true;
        }
        return false;
    }
}
