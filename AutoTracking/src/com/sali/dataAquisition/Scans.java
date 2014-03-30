package com.sali.dataAquisition;

import java.util.List;

import android.net.wifi.ScanResult;

public interface Scans {
	void processScans(List<ScanResult> results);

}
