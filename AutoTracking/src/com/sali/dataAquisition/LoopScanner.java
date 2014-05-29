package com.sali.dataAquisition;

import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

/*
 * Class that implements the scanning loop, throwing the request and capturing the 
 * returned values by itself.
 */

public class LoopScanner extends BroadcastReceiver implements
		SensorEventListener, LocationListener {

	// Callback
	Scans host;
	
	// To be used as filter to get ONLY the wifi scan values, no other broadcasted information. 
	private IntentFilter i;
	// To use as context reference.
	private Context hostContext;
	
	// Sensors values.
	private SensorManager Smg;
	private float[] orientationv;
	private float pressure_millibars;
	private float temp_celsius;
	private float[] magn_uT;
	private float lux;
	private float proximity_cm;
	private float[] gravity;
	private float humidity;
	private float[] acceleration;
	
	// PackageManager
	private PackageManager PM;
		
	// WiFi module use.
	private WifiManager Wmg;
	private boolean regReceiver;
	
	// GPS module use
	private LocationManager gps;
	private String provider;
	private boolean gpsReceiver;
	double lat;
	double lon;

	/*
	 * Save the context reference and initialize all used variables.
	 */
	public LoopScanner(Context c, Scans h) {
		
		hostContext = c;
		host=h;
		regReceiver = false;

		i = new IntentFilter();
		i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		PackageManager PM= hostContext.getPackageManager();
		Wmg = (WifiManager) hostContext.getSystemService(Context.WIFI_SERVICE);
		Smg = (SensorManager) hostContext.getSystemService(Context.SENSOR_SERVICE);
		gps = (LocationManager) hostContext.getSystemService(Context.LOCATION_SERVICE);
		gps.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

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
		Sensor Magnetic = Smg.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		Sensor Light = Smg.getDefaultSensor(Sensor.TYPE_LIGHT);
		Sensor Proximity = Smg.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		Sensor Gravity = Smg.getDefaultSensor(Sensor.TYPE_GRAVITY);
		Sensor Humidity = Smg.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
		Sensor Accelerometer = Smg.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		
		if (PM.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE))
		Smg.registerListener(this, orientation,SensorManager.SENSOR_DELAY_GAME);
		if (PM.hasSystemFeature(PackageManager.FEATURE_SENSOR_BAROMETER)){
		Smg.registerListener(this, Humidity, SensorManager.SENSOR_DELAY_GAME);
		Smg.registerListener(this, Pressure,SensorManager.SENSOR_DELAY_GAME);
		Smg.registerListener(this, Temperature,SensorManager.SENSOR_DELAY_GAME);}
		if (PM.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS)){
		Smg.registerListener(this, Magnetic, SensorManager.SENSOR_DELAY_GAME);
		Smg.registerListener(this, Gravity, SensorManager.SENSOR_DELAY_GAME);}
		if (PM.hasSystemFeature(PackageManager.FEATURE_SENSOR_LIGHT))
		Smg.registerListener(this, Light, SensorManager.SENSOR_DELAY_GAME);
		if (PM.hasSystemFeature(PackageManager.FEATURE_SENSOR_PROXIMITY))
		Smg.registerListener(this, Proximity, SensorManager.SENSOR_DELAY_GAME);
		if (PM.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER))
		Smg.registerListener(this, Accelerometer, SensorManager.SENSOR_DELAY_GAME);

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
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			magn_uT = event.values.clone();
			break;
		case Sensor.TYPE_LIGHT:
			lux = event.values[0];
			break;
		case Sensor.TYPE_PROXIMITY:
			proximity_cm = event.values[0];
			break;
		case Sensor.TYPE_GRAVITY:
			gravity = (float[]) event.values.clone();
			break;
		case Sensor.TYPE_RELATIVE_HUMIDITY:
			humidity = event.values[0];
			break;
		case Sensor.TYPE_LINEAR_ACCELERATION:
			acceleration = event.values.clone();
			break;
		default:
			return;
		}

	}


	
	public void onLocationChanged(Location location) {
	    lat = location.getLatitude();
	    lon = location.getLongitude();
	}
	
	@SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@SuppressWarnings("deprecation")
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
		
		float gravityx = gravity[0];
		float gravityy = gravity[1];
		float gravityz = gravity[2];
		
		float magn_uTx = magn_uT[0];
		float magn_uTy = magn_uT[1];
		float magn_uTz = magn_uT[2];
		
		float accelerationx = acceleration[0];
		float accelerationy = acceleration[1];
		float accelerationz = acceleration[2];
		
		List<ScanResult> results = Wmg.getScanResults();

		Wmg.startScan();
		
		host.processScans(results,gyrox,gyroy,gyroz,pressure_millibars,temp_celsius,magn_uTx,magn_uTy,magn_uTz,proximity_cm,gravityx,gravityy,gravityz,humidity,accelerationx,accelerationy,accelerationz,lat,lon);

	}


	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}


	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}


	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
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