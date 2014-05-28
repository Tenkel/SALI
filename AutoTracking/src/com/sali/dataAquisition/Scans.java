package com.sali.dataAquisition;

import java.util.List;

import android.net.wifi.ScanResult;

public interface Scans {
	void processScans(List<ScanResult> results, float gyrox, float gyroy, float gyroz, float millibars, float temp_celsius, float magn_uT, float proximity_cm, float gravityx, float gravityy, float gravityz, float humidity, float gravityz2, float humidity2, float accelerationx, float accelerationy, float accelerationz);
	void calibrateSensor();

}
