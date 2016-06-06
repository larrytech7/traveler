package com.satra.traveler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends Activity {

	TextView panneauPub;
	EditText username, telephone;
	ImageView buttonLogin;
	ImageButton profilePicture;

	final private static int DIALOG_SIGNUP = 1;
	private static int GET_FROM_GALLERY=2;

	public static int PUB_TRANSITION_DELAY=4000;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if(getSharedPreferences("traveler_prefs", 0).contains("username")){
			Toast.makeText(getApplicationContext(), getString(R.string.connexion_with_username)+" "+getSharedPreferences("traveler_prefs", 0).getString("username", "anonyme"), Toast.LENGTH_LONG).show();
			startActivity(new Intent(getApplicationContext(), MyPositionActivity.class));
			finish();
		}


		panneauPub = (TextView)findViewById(R.id.panneau_pub);
		username = (EditText)findViewById(R.id.username);
        telephone = (EditText)findViewById(R.id.no_telephone);

		buttonLogin = (ImageView)findViewById(R.id.button_login);



		startPub(panneauPub, this);


		buttonLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if(username.getText().toString().equals("")||telephone.getText().toString().equals("")){

					Toast.makeText(getApplicationContext(), getString(R.string.provide_all_fields)+"...", Toast.LENGTH_LONG).show();
					return;
				}

				AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);

				ad.setTitle(R.string.username_confirm_title);
				ad.setMessage(getString(R.string.username_confirm_msg) + username.getText().toString() + "?");
				ad.setNegativeButton(R.string.username_confirm_no_label,
						new android.content.DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int arg1) {

							}
						}
				);
				ad.setPositiveButton(R.string.username_confirm_yes_label, new android.content.DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int arg1) {

								SharedPreferences prefs = getSharedPreferences("traveler_prefs", 0);
								SharedPreferences.Editor editor = prefs.edit();
								editor.putString("username", username.getText().toString());
                                editor.putString("telephone", telephone.getText().toString());
								editor.commit();
								Toast.makeText(getApplicationContext(), getString(R.string.connexion_with_username) + " " + username.getText(), Toast.LENGTH_LONG).show();
								startActivity(new Intent(getApplicationContext(), MyPositionActivity.class));


							}
						}
				);
				ad.show();




			}
		});


//		buttonSignup.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				Toast.makeText(getApplicationContext(), getString(R.string.provide_account_informations)+"...", Toast.LENGTH_LONG).show();
//				showDialog(DIALOG_SIGNUP);
//
//			}
//		});

	}

	static long num = 0;
	static int  id=0;
	static String[] imagesNames;
	public static void startPub(final View v, final Context context){

		((TextView)v).setText("");
		num = 1;


		imagesNames = context.getResources().getStringArray(R.array.pub_images_names);
		id = context.getResources().getIdentifier(imagesNames[(int) num], "drawable", context.getPackageName());
		num++;

		((TextView)v).setBackgroundResource(id);



		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {

				if (msg.what == 1) {
					animateAdvertisingView(context, v, 2000, true);

				}
				else if(msg.what== 2){

//					Log.e("valeur de num...", "num === "+num);
					id = context.getResources().getIdentifier(imagesNames[(int) (num%2)], "drawable", context.getPackageName());
					num++;

					((TextView)v).setBackgroundResource(id);

					animateAdvertisingView(context, v, 2000, false);
				}


			}
		};


		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(true){
					try {
						Thread.sleep(2000+PUB_TRANSITION_DELAY);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					handler.sendEmptyMessage(1);

					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}


					handler.sendEmptyMessage(2);
				}


			}
		});

		t.start();
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
						// TODO Auto-generated method stub

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
						// TODO Auto-generated method stub
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}





	private static void animateAdvertisingView(final Context context, final View view, long duration, boolean go )
	{
		RelativeLayout root = (RelativeLayout) ((Activity) context).findViewById( R.id.root_layout );
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics( dm );
//		int statusBarOffset = dm.heightPixels - root.getMeasuredHeight();

		int originalPos[] = new int[2];
		view.getLocationOnScreen( originalPos );

		int xDest = dm.widthPixels;
		//	    xDest -= (view.getMeasuredWidth()/2);
		//	    int yDest = dm.heightPixels/2 - (view.getMeasuredHeight()/2) - statusBarOffset;

		if(go){
			TranslateAnimation anim = new TranslateAnimation( 0, xDest - originalPos[0] , 0, originalPos[1] - originalPos[1] );
			anim.setDuration(duration);
			anim.setFillAfter( true );
			view.startAnimation(anim);


		}
		else{
			TranslateAnimation anim = new TranslateAnimation( xDest - originalPos[0], 0 , originalPos[1] - originalPos[1], 0 );
			anim.setDuration(duration);
			anim.setFillAfter( true );
			view.startAnimation(anim);
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
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
