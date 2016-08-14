package com.satra.traveler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.satra.traveler.utils.TConstants;
import com.satra.traveler.utils.Tutility;

import java.io.FileNotFoundException;
import java.io.IOException;

import mehdi.sakout.fancybuttons.FancyButton;

public class MainActivity extends Activity {

	final private static int DIALOG_SIGNUP = 1;
	private static int GET_FROM_GALLERY=2;
	EditText username, matricule;
	FancyButton buttonLogin;
	ImageButton profilePicture;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if(getSharedPreferences(TConstants.TRAVELR_PREFERENCE, 0).contains(TConstants.PREF_USERNAME)){
			Toast.makeText(getApplicationContext(), getString(R.string.connexion_with_username)+" "
                    +getSharedPreferences(TConstants.TRAVELR_PREFERENCE, 0)
					.getString(TConstants.PREF_USERNAME, "anonyme-Travelr"),
                    Toast.LENGTH_LONG)
                    .show();
			startActivity(new Intent(getApplicationContext(), MyPositionActivity.class));
			finish();
		}

		//panneauPub = (TextView)findViewById(R.id.panneau_pub);
		username = (EditText)findViewById(R.id.username);
        matricule = (EditText)findViewById(R.id.matricule);

		buttonLogin = (FancyButton)findViewById(R.id.button_login);

		buttonLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(username.getText().toString().isEmpty() ||matricule.getText().toString().isEmpty()){

					Toast.makeText(getApplicationContext(), getString(R.string.provide_all_fields)+"...", Toast.LENGTH_LONG).show();
					return;
				}

				AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);

				ad.setTitle(R.string.username_confirm_title);
				ad.setMessage(getString(R.string.username_confirm_msg) + username.getText().toString() + "?");
				ad.setNegativeButton(R.string.username_confirm_no_label,
						new android.content.DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int arg1) {
                                dialog.dismiss();
							}
						}
				);
				ad.setPositiveButton(R.string.username_confirm_yes_label, new android.content.DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int arg1) {
                                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
								SharedPreferences prefs = getSharedPreferences(TConstants.TRAVELR_PREFERENCE, 0);
								SharedPreferences.Editor editor = prefs.edit();
								editor.putString(TConstants.PREF_USERNAME, username.getText().toString());
                                editor.putString(TConstants.PREF_PHONE, telephonyManager.getLine1Number());
                                editor.putString(TConstants.PREF_MATRICULE, matricule.getText().toString());
								editor.commit();
								Toast.makeText(getApplicationContext(), getString(R.string.connexion_with_username) + " " + username.getText(), Toast.LENGTH_LONG).show();
								startActivity(new Intent(getApplicationContext(), MyPositionActivity.class));

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
            Tutility.showMessage(this, R.string.about_message, R.string.about_title);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
