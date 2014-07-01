package com.sali.dataAquisition;

import java.util.List;

import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class AccessPointManager extends DataManager {

	public AccessPointManager(Context c) {
		super(c);
	}
	
	public void save(AccessPoint accesspoint) {
		SQLiteDatabase db = getWritableDatabase();

		try {
			ContentValues values = new ContentValues();
			values.put("idPosicao", accesspoint.getidPosicao());
			values.put("bssid", accesspoint.getbssid());
			values.put("essid", accesspoint.getessid());
			values.put("confianca", accesspoint.getconfianca());
			accesspoint.setId(db.insert("accesspoint", null, values));
		} finally {
			db.close();
		}
	}

	public List<AccessPoint> getByidPosicao(long idPosicao) {
		List<AccessPoint> list = new ArrayList<AccessPoint>();
		SQLiteDatabase db = getReadableDatabase();

		try {
			Cursor c = db.rawQuery("SELECT id, bssid, essid, confianca, idPosicao " +
					"FROM accesspoint WHERE idPosicao=" + idPosicao, null);
			
			while (c.moveToNext()) {
				AccessPoint accesspoint = new AccessPoint();
				accesspoint.setId(c.getLong(0));
				accesspoint.setbssid(c.getString(1));
				accesspoint.setessid(c.getString(2));
				accesspoint.setconfianca(c.getFloat(3));
				list.add(accesspoint);
			}
		} finally {
			db.close();
		}

		return list;
	}
	
	public List<AccessPoint> buildReadingsFromSignalsWithinRoomId(long roomId) {
		List<Reading> list = new ArrayList<Reading>();
		SQLiteDatabase db = getReadableDatabase();

		try {
			Cursor c = db.rawQuery("SELECT bssid, essid, confianca, idPosicao FROM " + 
					"samples sa INNER JOIN signals si ON sa.id=si.sample_id " + 
					"WHERE sa.room_id=" + roomId, null);
			
			while (c.moveToNext()) {
				Reading reading = new Reading();
				reading.setBSSID(c.getString(0));
				reading.setMean(c.getFloat(1));
				reading.setStd(c.getFloat(2));
				reading.setMin(c.getFloat(3));
				reading.setMax(c.getFloat(4));
				list.add(reading);
			}
		} finally {
			db.close();
		}

		return list;
	}

	public List<Reading> buildReadingsFromSignalsWithinSampleId(Long sampleId) {
		List<Reading> list = new ArrayList<Reading>();
		SQLiteDatabase db = getReadableDatabase();
		
		try {
			Cursor c = db.rawQuery("SELECT bssid, mean, std, min, max FROM " + 
					"signals WHERE sample_id=" + sampleId, null);
			
			while (c.moveToNext()) {
				Reading reading = new Reading();
				reading.setBSSID(c.getString(0));
				reading.setMean(c.getFloat(1));
				reading.setStd(c.getFloat(2));
				reading.setMin(c.getFloat(3));
				reading.setMax(c.getFloat(4));
				list.add(reading);
			}
		} finally {
			db.close();
		}

		return list;
	}

	public List<Signal> getBySampleId(Long id) {
		List<Signal> list = new ArrayList<Signal>();
		SQLiteDatabase db = getReadableDatabase();
		
		try {
			Cursor c = db.rawQuery("SELECT id, bssid, mean, std, min, max, sample_id FROM signals " + 
					"WHERE sample_id=" + id, null);
			
			while (c.moveToNext()) {
				Signal s = new Signal();
				s.setId(c.getLong(0));
				s.setBSSID(c.getString(1));
				s.setMean(c.getFloat(2));
				s.setStd(c.getFloat(3));
				s.setMin(c.getFloat(4));
				s.setMax(c.getFloat(5));
				s.setSampleId(c.getLong(6));
				list.add(s);
			}
		} finally {
			db.close();
		}

		return list;
	}

	
	
}
