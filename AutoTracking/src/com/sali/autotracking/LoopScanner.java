package com.sali.autotracking;

import java.util.List;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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


public class LoopScanner extends BroadcastReceiver implements
		SensorEventListener {

	
	private int numscan;
	private ProgressBar LoopBar;
	private IntentFilter i;
	private Activity HostAct;
	private int acc;
	private float[] geomagv;
	private float[] accelv;
	private float[] orientationv;
	private WifiManager Wmg;
	private SensorManager Smg;
	private boolean regReceiver;
	private int nroom;
	private float mean;

	public DataManager DTmg;
	public boolean BUSY;

	// Shared Variables
	SharedPreferences settings;
	SharedPreferences.Editor editor;
	private static final String SAVESCAN = "numscan";

	public LoopScanner(Activity Act) {
		HostAct = Act;
		LoopBar = (ProgressBar) HostAct.findViewById(R.id.progressBar1);
		nroom = 1;
		regReceiver = false;
		BUSY = false;
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
	public void pause() {
		editor = settings.edit();
		editor.putInt(SAVESCAN, nroom);
		editor.commit();

		if (regReceiver)
			HostAct.unregisterReceiver(this);
		regReceiver = false;
		Smg.unregisterListener(this);

		// Block UI
		BUSY = false;

		// Shared that DB has new entry
		editor = settings.edit();
		editor.putBoolean("warmed", false);
		editor.commit();

		// UI can sleep again
		HostAct.getWindow().clearFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Can Disconnect WIFI
		if (android.os.Build.VERSION.SDK_INT >= 17) {
			Settings.Global.putInt(HostAct.getContentResolver(),
					Settings.Global.WIFI_SLEEP_POLICY,
					Settings.Global.WIFI_SLEEP_POLICY_DEFAULT);
		} else {
			Settings.System.putInt(HostAct.getContentResolver(),
					Settings.System.WIFI_SLEEP_POLICY,
					Settings.System.WIFI_SLEEP_POLICY_DEFAULT);
		}

		LoopBar.setVisibility(View.INVISIBLE);

		DTmg.close();

	}
	
	public int getroom(){
		return nroom;
	}

	public void start() {

		nroom = settings.getInt(SAVESCAN, 1);

		registerSensor();
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public void registerSensor() {

		Sensor geomag = Smg.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		Sensor accel = Smg.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		Smg.unregisterListener(this);

		if (android.os.Build.VERSION.SDK_INT >= 9) {
			Sensor orientation = Smg
					.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
			Smg.registerListener(this, orientation,
					SensorManager.SENSOR_DELAY_GAME);
		} else {
			Smg.registerListener(this, geomag, SensorManager.SENSOR_DELAY_GAME);
			Smg.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME);
		}

	}

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
	public void acquire() {

		numscan = 0;
		((TextView) HostAct.findViewById(R.id.textView2)).setText(String
				.valueOf(numscan));

		HostAct.getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Can't Disconnect from WIFI
		if (android.os.Build.VERSION.SDK_INT >= 17) {
			Settings.Global.putInt(HostAct.getContentResolver(),
					Settings.Global.WIFI_SLEEP_POLICY,
					Settings.Global.WIFI_SLEEP_POLICY_NEVER);
		} else {
			Settings.System.putInt(HostAct.getContentResolver(),
					Settings.System.WIFI_SLEEP_POLICY,
					Settings.System.WIFI_SLEEP_POLICY_NEVER);
		}

		HostAct.registerReceiver(this, i);
		regReceiver = true;

		LoopBar.setVisibility(View.VISIBLE);
		Wmg.startScan();

	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override
	public void onReceive(Context c, Intent i) {

		float gyrox = 0;
		float gyroy = 0;
		float gyroz = 0;

		if (android.os.Build.VERSION.SDK_INT >= 9) {
			gyrox = orientationv[0];
			gyroy = orientationv[1];
			gyroz = orientationv[2];
		} else {
			float[] R = new float[9];
			SensorManager.getRotationMatrix(R, null, accelv, geomagv);
			gyrox = (float) Math.abs(0.5 * Math.sqrt(1 + R[0] - R[4] - R[8]))
					* Math.signum(R[7] - R[5]);
			gyroy = (float) Math.abs(0.5 * Math.sqrt(1 - R[0] + R[4] - R[8]))
					* Math.signum(R[2] - R[6]);
			gyroz = (float) Math.abs(0.5 * Math.sqrt(1 - R[0] - R[4] + R[8]))
					* Math.signum(R[3] - R[1]);
		}

		DTmg.open();
		List<ScanResult> results = Wmg.getScanResults();
		for (ScanResult result : results) {
			DTmg.insert(result.level, result.BSSID, nroom, nroom, nroom, gyrox,
					gyroy, gyroz, acc);
		}

		// DUPLICATE ENTRY
		//
		// WifiInfo actual_connection = Wmg.getConnectionInfo ();
		// if (actual_connection.getNetworkId()!=-1){
		// DTmg.insert(actual_connection.getRssi(),
		// actual_connection.getBSSID(),nroom,nroom,nroom, gyrox, gyroy, gyroz,
		// acc);
		// }

		mean = DTmg.NSamplesmean(nroom);

		DTmg.close();

		numscan += 1;

		((TextView) HostAct.findViewById(R.id.textView2)).setText(String
				.valueOf(numscan));
		((TextView) HostAct.findViewById(R.id.textView1)).setText(String
				.valueOf(mean));

		Wmg.startScan();

	}

}
