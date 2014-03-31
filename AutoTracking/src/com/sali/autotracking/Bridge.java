package com.sali.autotracking;

import java.util.List;

import com.sali.algorithms.KDEalg;
import com.sali.dataAquisition.Scans;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.provider.Settings;

public class Bridge extends Service implements Scans {
	
	public static final String PREF = "ZoomPref";
	SharedPreferences settings = getSharedPreferences(PREF,0);
	int algchoice = settings.getInt("Algorithm",0);
	
	public Bridge() {
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		
		switch (algchoice){
		case 1:
		Thread kdealg = new Thread(){
			public void run(){
			 bindService(
			        new Intent(Bridge.this, KDEalg.class),
			        serviceConnection,
			        Context.BIND_AUTO_CREATE
			    );
			}
			};
		kdealg.start();
		
		}
		
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public void processScans(List<ScanResult> results) {
		// TODO Auto-generated method stub
		
	}
	
}
