package com.sali.dataAquisition;

import java.util.List;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;

/*
 * Class that implements the scanning loop, throwing the request and capturing the 
 * returned values by itself.
 */

public class LoopScanner extends BroadcastReceiver implements
		SensorEventListener {

	// Callback
	Scans host;
	
	// To be used as filter to get ONLY the wifi scan values, no other broadcasted information. 
	private IntentFilter i;
	// To use as context reference.
	private Context hostContext;
	
	// orientation last values.
	private SensorManager Smg;
	private float[] orientationv;
	private float pressure_millibars;
	private float temp_celsius;
	
	// WiFi module use.
	private WifiManager Wmg;
	private boolean regReceiver;


	/*
	 * Save the context reference and initialize all used variables.
	 */
	public LoopScanner(Context c, Scans h) {
		
		hostContext = c;
		host=h;
		regReceiver = false;

		i = new IntentFilter();
		i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		Wmg = (WifiManager) hostContext.getSystemService(Context.WIFI_SERVICE);
		Smg = (SensorManager) hostContext.getSystemService(Context.SENSOR_SERVICE);


		registerSensor();
	}

	
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	/*
	 * Stop, close and unregister what needed.
	 */
	public void pause() {

		// Unregister WiFi and Sensors.
		if (regReceiver)
			hostContext.unregisterReceiver(this);
		regReceiver = false;
		Smg.unregisterListener(this);

		// Can Disconnect WIFI - Manage Deprecation.
		if (android.os.Build.VERSION.SDK_INT >= 17) {
			Settings.Global.putInt(hostContext.getContentResolver(),
					Settings.Global.WIFI_SLEEP_POLICY,
					Settings.Global.WIFI_SLEEP_POLICY_DEFAULT);
		} else {
			Settings.System.putInt(hostContext.getContentResolver(),
					Settings.System.WIFI_SLEEP_POLICY,
					Settings.System.WIFI_SLEEP_POLICY_DEFAULT);
		}


	}

	public void start() {

		registerSensor();
	}

	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	/*
	 * Manage rotation sensor deprecation.
	 */
	public void registerSensor() {

		Smg.unregisterListener(this);

		Sensor orientation = Smg.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		Sensor Pressure = Smg.getDefaultSensor(Sensor.TYPE_PRESSURE);
		Sensor Temperature = Smg.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
		
		Smg.registerListener(this, orientation,SensorManager.SENSOR_DELAY_GAME);
		Smg.registerListener(this, Pressure,SensorManager.SENSOR_DELAY_GAME);
		Smg.registerListener(this, Temperature,SensorManager.SENSOR_DELAY_GAME);
			


	}



	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		if(accuracy< SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM)
			host.calibrateSensor();
	}

	/*
	 * 
	 * Save values when it changes.
	 * 
	 */
	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()) {

		case Sensor.TYPE_ROTATION_VECTOR:
			orientationv = (float[]) event.values.clone();
			break;
		case Sensor.TYPE_PRESSURE:
			pressure_millibars = event.values[0];
			break;
		case Sensor.TYPE_AMBIENT_TEMPERATURE:
			temp_celsius = event.values[0];
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

		// Can't Disconnect from WIFI - Manage Deprecation.
		if (android.os.Build.VERSION.SDK_INT >= 17) {
			Settings.Global.putInt(hostContext.getContentResolver(),
					Settings.Global.WIFI_SLEEP_POLICY,
					Settings.Global.WIFI_SLEEP_POLICY_NEVER);
		} else {
			Settings.System.putInt(hostContext.getContentResolver(),
					Settings.System.WIFI_SLEEP_POLICY,
					Settings.System.WIFI_SLEEP_POLICY_NEVER);
		}

		hostContext.registerReceiver(this, i);
		regReceiver = true;


		Wmg.startScan();

	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override
	/* 
	 * Save data to the database when received, then re-start the cycle.
	 */
	public void onReceive(Context c, Intent i) {

		float gyrox = orientationv[0];
		float gyroy = orientationv[1];
		float gyroz = orientationv[2];
		

		List<ScanResult> results = Wmg.getScanResults();

		Wmg.startScan();
		
		host.processScans(results,gyrox,gyroy,gyroz,pressure_millibars);

	}

}




//private float[] geomagv;
//private float[] accelv;

//} else {
//Sensor geomag = Smg.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
//Sensor accel = Smg.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//
//Smg.registerListener(this, geomag, SensorManager.SENSOR_DELAY_GAME);
//Smg.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME);
//}


//case Sensor.TYPE_ACCELEROMETER:
//	accelv = (float[]) event.values.clone();
//	break;
//case Sensor.TYPE_MAGNETIC_FIELD:
//	geomagv = (float[]) event.values.clone();
//	break;



/*			IF 		android.os.Build.VERSION.SDK_INT < 9
 * 
 * 			float[] R = new float[9];
			//Converts the Rotation Matrix to the quaternion notation and stores the tree first values (the forth is consequence).
			SensorManager.getRotationMatrix(R, null, accelv, geomagv);
			gyrox = (float) Math.abs(0.5 * Math.sqrt(1 + R[0] - R[4] - R[8]))
					* Math.signum(R[7] - R[5]);
			gyroy = (float) Math.abs(0.5 * Math.sqrt(1 - R[0] + R[4] - R[8]))
					* Math.signum(R[2] - R[6]);
			gyroz = (float) Math.abs(0.5 * Math.sqrt(1 - R[0] - R[4] + R[8]))
					* Math.signum(R[3] - R[1]);*/



// DUPLICATE ENTRY - add actual connection twice sometimes.
//
// WifiInfo actual_connection = Wmg.getConnectionInfo ();
// if (actual_connection.getNetworkId()!=-1){
// DTmg.insert(actual_connection.getRssi(),
// actual_connection.getBSSID(),nroom,nroom,nroom, gyrox, gyroy, gyroz,
// acc);
// }