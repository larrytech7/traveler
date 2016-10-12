package com.satra.traveler;
import java.util.ArrayList;

import org.w3c.dom.Document;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 *
 * This class Get Route on the map
 *
 */
public class GetRouteTask extends AsyncTask<String, Void, String> {



	String response = "";
	Context context;
	GMapV2GetRouteDirection v2GetRouteDirection;
	Document document;
	LatLng fromPosition;
	LatLng toPosition;
	 int COLOR = Color.RED;
	GoogleMap map;

	public void affiche(String message){
		Toast.makeText(context,message, Toast.LENGTH_SHORT).show();

	}

	public GetRouteTask(Context context,GoogleMap map, LatLng fromPosition, LatLng toPosition){
		this.context = context;
		this.fromPosition = fromPosition;
		this.toPosition = toPosition;
		this.map = map;
	}
	
	public GetRouteTask(Context context,GoogleMap map, LatLng fromPosition, LatLng toPosition, int COLOR){
		this.context = context;
		this.fromPosition = fromPosition;
		this.toPosition = toPosition;
		this.map = map;
		this.COLOR = COLOR;
	}

	@Override
	protected void onPreExecute() {
		affiche(context.getString(R.string.route_calculation));
		v2GetRouteDirection = new GMapV2GetRouteDirection();
	}

	@Override
	protected String doInBackground(String... urls) {
		//Get All Route values
		try{
			document = v2GetRouteDirection.getDocument(fromPosition, toPosition, GMapV2GetRouteDirection.MODE_DRIVING);
			response = "Success";
		}catch(NullPointerException e){
			e.printStackTrace();
			// affiche("Pas d'acc�s � internet. La route ne peut pas �tre trouv�e...");

		}
		return response;

	}

	@Override
	protected void onPostExecute(String result) {
		// map.clear();
		if(response.equalsIgnoreCase("Success")){

			ArrayList<LatLng> directionPoint;
			try {
				directionPoint = v2GetRouteDirection.getDirection(document);
				PolylineOptions rectLine = new PolylineOptions().width(10).color(
						COLOR);
				for (int i = 0; i < directionPoint.size(); i++) {
					rectLine.add(directionPoint.get(i));
				}
				// Adding route on the map
				map.addPolyline(rectLine);
				


			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				affiche(context.getString(R.string.operation_failed_try_again_later));		}



		}

	}
}