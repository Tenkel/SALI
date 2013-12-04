package com.example.autotracking;

import java.util.List;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Captar extends Activity implements SensorEventListener {
	// DB
		private DataManager DTmg;
		
		// Shared Variables
		SharedPreferences settings;
		public static final String PREF = "ZoomPref";
		
		//Room
		EditText Room;
		int nroom=1;	
		
		// Dialogs
		AlertDialog.Builder size_dim;
		AlertDialog.Builder builder;
		
		// Progress Bar Variables
		private LinearLayout linProgressBar;
		private boolean BUSY=false;
		
		//Real Image Coordinates
//		private float relativeX=0;
//		private float relativeY=0;

		
		// DEBUG
		private static final String TAG = "Captar";
		
		
		// Wifi Variables
		private int numscan=0;
		WifiManager wifi;
		BroadcastReceiver receiver = new BroadcastReceiver() {
			@SuppressLint("InlinedApi")
			@SuppressWarnings("deprecation")
			public void onReceive(Context c, Intent i) {
				WifiManager w = (WifiManager) c
						.getSystemService(Context.WIFI_SERVICE);
				w.getScanResults();

				
				DTmg.open();
				List<ScanResult> results = w.getScanResults();
				for (ScanResult result : results) {
					DTmg.insert(result.level,result.BSSID,nroom,nroom,nroom);
				}
				WifiInfo actual_connection = w.getConnectionInfo ();
				if (actual_connection.getNetworkId()!=-1){
				DTmg.insert(actual_connection.getRssi(), actual_connection.getBSSID(),nroom,nroom,nroom);
			}
				DTmg.close();

				numscan+=1;
				ProgressBar pb = (ProgressBar) findViewById(R.id.progress_bar);
				pb.setProgress(numscan);
				//
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (numscan<20){
					w.startScan();}
				else{
					// Block UI
					BUSY=false;
					
					// Shared that DB has new entry
					SharedPreferences settings = getSharedPreferences(PREF,0);
	    			SharedPreferences.Editor editor = settings.edit();
	    			editor.putBoolean("warmed", false);
	    			editor.commit();
					
					// Destroy ProgressBar
					linProgressBar = (LinearLayout) findViewById(R.id.lin_progress_bar);
			        linProgressBar.setVisibility(View.INVISIBLE);
			        pb.setProgress(0);
			        
			        // UI can sleep again
			        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			        
			        // Can Disconnect WIFI
			        if (android.os.Build.VERSION.SDK_INT >= 17){
				        Settings.Global.putInt(getContentResolver(),Settings.Global.WIFI_SLEEP_POLICY,Settings.Global.WIFI_SLEEP_POLICY_DEFAULT);
			        } else{
				        Settings.System.putInt(getContentResolver(),Settings.System.WIFI_SLEEP_POLICY,Settings.System.WIFI_SLEEP_POLICY_DEFAULT);
			        }
			        
			        // Finish
			        //unregisterReceiver(receiver);
					
				}
				
			}
		};
		

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_captar);
		Button capture = (Button) findViewById(R.id.button1);
        DTmg = new DataManager(this);
      //  BroadcastScanner breceiver = new BroadcastScanner(this);
        capture.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				if (!BUSY){
					BUSY=true;
					TextView Room = (TextView) findViewById(R.id.TextView01);
			    	nroom = Integer.parseInt(Room.getText().toString());
		        	Room.setText(String.valueOf(nroom+1));
		        	acquire();
				}
				}

			@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
			@SuppressWarnings("deprecation")
			private void acquire() {
				getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

				// Can't Disconnect from WIFI
		        if (android.os.Build.VERSION.SDK_INT >= 17){
			        Settings.Global.putInt(getContentResolver(),Settings.Global.WIFI_SLEEP_POLICY,Settings.Global.WIFI_SLEEP_POLICY_NEVER);
		        } else{
					Settings.System.putInt(getContentResolver(),Settings.System.WIFI_SLEEP_POLICY,Settings.System.WIFI_SLEEP_POLICY_NEVER);
		        }
		        
				IntentFilter i = new IntentFilter();
				i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
				registerReceiver(receiver, i);
				WifiManager w = (WifiManager) getSystemService(Context.WIFI_SERVICE);
				numscan=0;
				linProgressBar = (LinearLayout) findViewById(R.id.lin_progress_bar);
		        linProgressBar.setVisibility(View.VISIBLE);
				w.startScan();
				//unregisterReceiver(receiver);	
				
			}
				
			}
		);}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.captar, menu);
		return true;
	}


	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}


	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		
	}

}
