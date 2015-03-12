package com.sali.autotracking;

import java.util.Iterator;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sali.autotracking.R;

/*
 * Class that implements the scanning loop, throwing the request and capturing the 
 * returned values by itself.
 */

public class LoopScanner extends BroadcastReceiver implements
		SensorEventListener {

	// Number of scan rounds
	private int numscan;
	// To be used as filter to get ONLY the wifi scan values, no other broadcasted information. 
	private IntentFilter i;
	// To use as context reference.
	private Activity HostAct;
	
	// Sensor Accuracy, Geomagnetic(or orientation for backward compatibility) and Acceleration last values.
	private SensorManager Smg;
	private int acc;
	private float[] geomagv;
	private float[] orientationv;
	private float[] accelv;
	
	// Wifi module use.
	private WifiManager Wmg;
	private boolean regReceiver;
	
	private int nroom;
	private float mean;

	public DataManager DTmg;

	// Shared Variables
	SharedPreferences settings;
	SharedPreferences.Editor editor;
	private static final String SAVESCAN = "numscan";
	public boolean toSave=true;

	/*
	 * Save the context reference and initialize all used variables.
	 */
	public LoopScanner(Activity Act) {
		HostAct = Act;
		nroom = 1;
		regReceiver = false;
		acc = 0;
		DTmg = new DataManager(Act);
		i = new IntentFilter();
		i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		Wmg = (WifiManager) HostAct.getSystemService(Context.WIFI_SERVICE);
		Smg = (SensorManager) HostAct.getSystemService(Context.SENSOR_SERVICE);

		settings = HostAct.getSharedPreferences(Offline.PREF, 0);

		registerSensor();
	}

	
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	/*
	 * Stop, close and unregister what needed.
	 */
	public void pause() {
		// Save number of scan rounds.
		editor = settings.edit();
		editor.putInt(SAVESCAN, nroom);
		editor.commit();

		// Unregister Wifi and Sensors.
		if (regReceiver)
			HostAct.unregisterReceiver(this);
		regReceiver = false;
		Smg.unregisterListener(this);

		// UI can sleep again
		HostAct.getWindow().clearFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Can Disconnect WIFI - Manage Deprecation.
//		if (android.os.Build.VERSION.SDK_INT >= 17) {
//			Settings.Global.putInt(HostAct.getContentResolver(),
//					Settings.Global.WIFI_SLEEP_POLICY,
//					Settings.Global.WIFI_SLEEP_POLICY_DEFAULT);
//		} else {
//			Settings.System.putInt(HostAct.getContentResolver(),
//					Settings.System.WIFI_SLEEP_POLICY,
//					Settings.System.WIFI_SLEEP_POLICY_DEFAULT);
//		}

		DTmg.close();

	}

	public int getroom() {
		return nroom;
	}

	public void start() {

		//load number of scan rounds.
		nroom = settings.getInt(SAVESCAN, 1);

		registerSensor();
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	/*
	 * Manage rotation sensor deprecation.
	 */
	public void registerSensor() {

		Smg.unregisterListener(this);

		if (android.os.Build.VERSION.SDK_INT >= 9) {
			Sensor orientation = Smg.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
			
			Smg.registerListener(this, orientation,SensorManager.SENSOR_DELAY_GAME);
			
		} else {
			Sensor geomag = Smg.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
			Sensor accel = Smg.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			
			Smg.registerListener(this, geomag, SensorManager.SENSOR_DELAY_GAME);
			Smg.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME);
		}

	}

	/*
	 * Set actual room.
	 */
	public void setroom(int value) {
		nroom = value;
		DTmg.open();
		mean = DTmg.NSamplesmean(nroom);
		DTmg.close();
		((TextView) HostAct.findViewById(R.id.textView1)).setText(String
				.valueOf(mean));
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		acc = accuracy;
	}

	/*
	 * (non-Javadoc)
	 * @see android.hardware.SensorEventListener#onSensorChanged(android.hardware.SensorEvent)
	 * 
	 * Save values when it changes.
	 * 
	 */
	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()) {

		case Sensor.TYPE_ROTATION_VECTOR:
			orientationv = (float[]) event.values.clone();
			break;
		case Sensor.TYPE_ACCELEROMETER:
			accelv = (float[]) event.values.clone();
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			geomagv = (float[]) event.values.clone();
			break;

		default:
			return;
		}

	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@SuppressWarnings("deprecation")
	/*
	 * Prepare and start acquisition cycle.
	 */
	public void acquire() {

		numscan = 0;
		((TextView) HostAct.findViewById(R.id.textView2)).setText(String
				.valueOf(numscan));

		// UI can't sleep
		HostAct.getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Can't Disconnect from WIFI - Manage Deprecation.
//		if (android.os.Build.VERSION.SDK_INT >= 17) {
//			Settings.Global.putInt(HostAct.getContentResolver(),
//					Settings.Global.WIFI_SLEEP_POLICY,
//					Settings.Global.WIFI_SLEEP_POLICY_NEVER);
//		} else {
//			Settings.System.putInt(HostAct.getContentResolver(),
//					Settings.System.WIFI_SLEEP_POLICY,
//					Settings.System.WIFI_SLEEP_POLICY_NEVER);
//		}

		HostAct.registerReceiver(this, i);
		regReceiver = true;


		Wmg.startScan();

	}
	
	public void saveInBD(List<ScanResult> results, float gyrox, float gyroy, float gyroz){
		DTmg.open();
		for (ScanResult result : results) {
			DTmg.insert(result.level, result.BSSID, nroom, nroom, nroom, gyrox,
					gyroy, gyroz, acc);
		}

		// Shared that DB has new entry
		editor = settings.edit();
		editor.putBoolean("warmed", false);
		editor.commit();

		mean = DTmg.NSamplesmean(nroom);

		DTmg.close();
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override
	/*
	 * (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 * 
	 * Save data to the database when received, then re-start the cycle. 
	 * 
	 */
	public void onReceive(Context c, Intent i) {

		float gyrox = 0;
		float gyroy = 0;
		float gyroz = 0;
		int place = 0;

		if (android.os.Build.VERSION.SDK_INT >= 9) {
			gyrox = orientationv[0];
			gyroy = orientationv[1];
			gyroz = orientationv[2];
		} else {
			float[] R = new float[9];
			//Converts the Rotation Matrix to the quaternion notation and stores the tree first values (the forth is consequence).
			SensorManager.getRotationMatrix(R, null, accelv, geomagv);
			gyrox = (float) Math.abs(0.5 * Math.sqrt(1 + R[0] - R[4] - R[8]))
					* Math.signum(R[7] - R[5]);
			gyroy = (float) Math.abs(0.5 * Math.sqrt(1 - R[0] + R[4] - R[8]))
					* Math.signum(R[2] - R[6]);
			gyroz = (float) Math.abs(0.5 * Math.sqrt(1 - R[0] - R[4] + R[8]))
					* Math.signum(R[3] - R[1]);
		}

		List<ScanResult> results = Wmg.getScanResults();
		if(toSave)saveInBD(results,gyrox,gyroy,gyroz);
		else place = predict(results);
		numscan += 1;

		if(toSave){
		((TextView) HostAct.findViewById(R.id.textView2)).setText(String
				.valueOf(numscan));
		((TextView) HostAct.findViewById(R.id.textView1)).setText(String
				.valueOf(mean));
		}
		else{
			((TextView) HostAct.findViewById(R.id.textView2)).setText(String
					.valueOf(numscan));
			((TextView) HostAct.findViewById(R.id.textView3)).setText(String
					.valueOf(place));	
		}
		Wmg.startScan();

	}


	private int predict(List<ScanResult> results) {
		DTmg.open();
		int place = 0;
		Cursor c = DTmg.Local(new String[]{DataManager.Local.ID,DataManager.Local.PX,DataManager.Local.PY}, null); // cursor com os locais da base de dados
		Cursor c2 = null;
		String[] info = new String[]{DataManager.Access_Point.ID};
		  
		float[] probabilities = new float[c.getCount()];
		for (int i=0;i<c.getCount();i++)probabilities[i]=0;
		float maxprob = Float.NEGATIVE_INFINITY;
		c.moveToFirst();
		for (int l=0;!c.isAfterLast();l++){ // Um loop para todos os locais
			Iterator<ScanResult> pa = results.iterator();
			while (pa.hasNext()){
				c2 = DTmg.AP(info, DataManager.Access_Point.NAME + " == '" + pa.next().BSSID + "'");
				if(c2.getCount()==0)continue;
				else{ 
					c2.moveToFirst();
					probabilities[l] += DTmg.KSDFunction(c.getLong(0), c2.getLong(0)).prob(pa.next().level);
				}
			// Maior probabilidade é onde o usuário está
		   if (probabilities[l]>maxprob){
			   place = c.getInt(1);
			   maxprob=probabilities[l];
		   }
		}
		c.moveToNext();
	}
		DTmg.close();
		return place;
		
	}

}
