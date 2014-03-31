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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.provider.Settings;

public class Bridge extends Service implements Scans {
	
	public static final String PREF = "ZoomPref";
	SharedPreferences settings = getSharedPreferences(PREF,0);
	int algchoice = settings.getInt("Algorithm",0);
	
	class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            
        	Bundle data = msg.getData();        	
        	String dataString = data.getString("MyString");
        }
     }
	
	final Messenger myMessenger = new Messenger(new IncomingHandler());
	
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
		
		return myMessenger.getBinder();
	}
	
	public void processScans(List<ScanResult> results, float gyrox, float gyroy, float gyroz) {
		// TODO Auto-generated method stub
		
	}

	public void calibrateSensor() {
		// TODO Auto-generated method stub
		
	}
	
}
