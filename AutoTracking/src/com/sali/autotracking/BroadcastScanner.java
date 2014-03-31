package com.sali.autotracking;

import java.util.List;

import android.annotation.SuppressLint;
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
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.sali.algorithms.DataManager;
import com.sali.autotracking.R;

@SuppressLint({ "InlinedApi", "NewApi" })
public class BroadcastScanner extends BroadcastReceiver implements SensorEventListener {

	private int numscan;
	private LinearLayout linProgressBar;
	private IntentFilter i;
	private Activity HostAct;
	private int acc;
	private float[] geomagv;
	private float[] accelv;
	private float[] orientationv;
	private WifiManager Wmg;
	private SensorManager Smg;
	private boolean regReceiver;
	
	public DataManager DTmg; 
	public int nroom;	
	public boolean BUSY;

	// Shared Variables
	SharedPreferences settings;
	SharedPreferences.Editor editor;
	private static final String SAVESCAN = "numscan";
	
	public BroadcastScanner(Activity Act) {
		HostAct = Act;
		linProgressBar = (LinearLayout) HostAct.findViewById(R.id.lin_progress_bar);
		nroom=1;	
		regReceiver=false;
		BUSY=false;
		acc=0;
        DTmg = new DataManager(Act);
        i = new IntentFilter();
		i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        Wmg = (WifiManager) HostAct.getSystemService(Context.WIFI_SERVICE);
        Smg = (SensorManager) HostAct.getSystemService(Context.SENSOR_SERVICE);

		settings = HostAct.getSharedPreferences(Offline.PREF,0);
        
        registerSensor();
	}
	
	public void pause(){
		editor = settings.edit();
		editor.putInt(SAVESCAN, nroom);
		editor.commit();
		
		if (regReceiver)
			HostAct.unregisterReceiver(this);
		regReceiver=false;
		Smg.unregisterListener(this);
	}

	public void start(){
//		if (regReceiver)
//			HostAct.unregisterReceiver(this);
//		HostAct.registerReceiver(this, i);
//		regReceiver=true;
		
		nroom = settings.getInt(SAVESCAN, 1);
		
		registerSensor();
	}
	
	
	public void registerSensor(){

        Sensor geomag = Smg.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor accel = Smg.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        Smg.unregisterListener(this);

        if (android.os.Build.VERSION.SDK_INT >= 9){
            Sensor orientation = Smg.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        	Smg.registerListener(this, orientation, SensorManager.SENSOR_DELAY_GAME);
        } else{
        	Smg.registerListener(this, geomag, SensorManager.SENSOR_DELAY_GAME);
        	Smg.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME);
        }
		
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onReceive(Context c, Intent i) {
		
		float gyrox=0; 
		float gyroy=0;
		float gyroz=0;
		
        if (android.os.Build.VERSION.SDK_INT >= 9){
        	gyrox = orientationv[0];
        	gyroy = orientationv[1];
        	gyroz = orientationv[2];
        }
        else {
        	float[] R=new float[9];
        	SensorManager.getRotationMatrix(R, null, accelv, geomagv);	
        	gyrox = (float) Math.abs(0.5*Math.sqrt(1+R[0]-R[4]-R[8]))*Math.signum(R[7]-R[5]);
        	gyroy = (float) Math.abs(0.5*Math.sqrt(1-R[0]+R[4]-R[8]))*Math.signum( R[2]-R[6]);
        	gyroz = (float) Math.abs(0.5*Math.sqrt(1-R[0]-R[4]+R[8]))*Math.signum( R[3]-R[1]);
        }

		
		DTmg.open();
		List<ScanResult> results = Wmg.getScanResults();
		for (ScanResult result : results) {
			DTmg.insert(result.level,result.BSSID,nroom,nroom,nroom, gyrox, gyroy, gyroz, acc);
		}
		WifiInfo actual_connection = Wmg.getConnectionInfo ();
		if (actual_connection.getNetworkId()!=-1){
		DTmg.insert(actual_connection.getRssi(), actual_connection.getBSSID(),nroom,nroom,nroom, gyrox, gyroy, gyroz, acc);
	}
		DTmg.close();

		numscan+=1;
		ProgressBar pb = (ProgressBar) HostAct.findViewById(R.id.progress_bar);
		pb.setProgress(numscan);
		
		
		if (numscan<20){
			Wmg.startScan();}
		else{
			// Block UI
			BUSY=false;
			
			// Shared that DB has new entry
			editor = settings.edit();
			editor.putBoolean("warmed", false);
			editor.commit();
			
			// Destroy ProgressBar
	        linProgressBar.setVisibility(View.INVISIBLE);
	        pb.setProgress(0);
	        
	        // UI can sleep again
	        HostAct.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	        
	        // Can Disconnect WIFI
	        if (android.os.Build.VERSION.SDK_INT >= 17){
		        Settings.Global.putInt(c.getContentResolver(),Settings.Global.WIFI_SLEEP_POLICY,Settings.Global.WIFI_SLEEP_POLICY_DEFAULT);
	        } else{
		        Settings.System.putInt(c.getContentResolver(),Settings.System.WIFI_SLEEP_POLICY,Settings.System.WIFI_SLEEP_POLICY_DEFAULT);
	        }
			HostAct.unregisterReceiver(this);
			regReceiver=false;
		}
		
	}

	
	
	@SuppressWarnings("deprecation")
	public void acquire() {
		numscan=0;
		HostAct.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Can't Disconnect from WIFI
        if (android.os.Build.VERSION.SDK_INT >= 17){
	        Settings.Global.putInt(HostAct.getContentResolver(),Settings.Global.WIFI_SLEEP_POLICY,Settings.Global.WIFI_SLEEP_POLICY_NEVER);
        } else{
			Settings.System.putInt(HostAct.getContentResolver(),Settings.System.WIFI_SLEEP_POLICY,Settings.System.WIFI_SLEEP_POLICY_NEVER);
        }
        
		HostAct.registerReceiver(this, i);
		regReceiver=true;
		
		linProgressBar = (LinearLayout) HostAct.findViewById(R.id.lin_progress_bar);
        linProgressBar.setVisibility(View.VISIBLE);
		Wmg.startScan();
		
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
		
}
