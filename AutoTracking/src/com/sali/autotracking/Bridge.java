package com.sali.autotracking;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.provider.Settings;

public class Bridge extends Service {
	
	public static final String PREF = "ZoomPref";
	SharedPreferences settings = getSharedPreferences(PREF,0);
	int algchoice = settings.getInt("Algorithm",0);
	
	public Bridge() {
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		
		Thread bridge = new Thread(){
			public void run(){
			 bindService(
			        new Intent(Bridge.this, KDEalg.class),
			        serviceConnection,
			        Context.BIND_AUTO_CREATE
			    );
			}
			};
		KDEalg.start();
		

		
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	private void acquire() {
		Settings.System.putInt(getContentResolver(),Settings.System.WIFI_SLEEP_POLICY,Settings.System.WIFI_SLEEP_POLICY_NEVER);
		IntentFilter i = new IntentFilter();
		i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		registerReceiver(receiver, i);
		WifiManager w = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		w.startScan();
		
	}
	
}
