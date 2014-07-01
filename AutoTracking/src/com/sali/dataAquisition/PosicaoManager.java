package com.sali.dataAquisition;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class PosicaoManager extends DataManager {

	public PosicaoManager(Context c) {
		super(c);
	}
	
	public void save(Posicao posicao) {
		SQLiteDatabase db = getWritableDatabase();

		try {
			ContentValues values = new ContentValues();
			values.put("x", posicao.getX());
			values.put("y", posicao.getY());
			values.put("idAndar", posicao.getidAndar());
			posicao.setId(db.insert("posicao", null, values));
		} finally {
			db.close();
		}
	}
	
	public List<Posicao> getByidAndar(long idAndar) {
		List<Posicao> list = new ArrayList<Posicao>();
		SQLiteDatabase db = getReadableDatabase();

		try {
			Cursor c = db.rawQuery("SELECT id, x, y, idAndar " +
					"FROM samples WHERE idAndar=" + idAndar, null);
			
			while (c.moveToNext()) {
				Posicao posicao = new Posicao();
				posicao.setId(c.getLong(0));
				posicao.setX(c.getInt(1));
				posicao.setY(c.getInt(2));
				posicao.setidAndar(c.getLong(3));
				list.add(posicao);
			}
		} finally {
			db.close();
		}

		return list;
	}

	public List<Posicao> getAll() {
		List<Posicao> list = new ArrayList<Posicao>();
		SQLiteDatabase db = getReadableDatabase();
		
		try {
			Cursor c = db.query("posicao", new String[] {"id", "x", "y", "idAndar"}, null, null, null, null, null);
			
			while(c.moveToNext()) {
				Posicao posicao = new Posicao();
				posicao.setId(c.getLong(0));
				posicao.setX(c.getInt(1));
				posicao.setY(c.getInt(2));
				posicao.setidAndar(c.getLong(3));
				list.add(posicao);
			}
		} finally {
			db.close();
		}
		
		return list;
	}

	public void delete(Posicao posicao) {
		if (posicao.getId() == null) 
			return;
		
		SQLiteDatabase db = getWritableDatabase();
		
		try {
			db.delete("posicao", "id=?", new String[] {posicao.getId().toString()});
		} finally {
			db.close();
		}
	}

	public Posicao getFirstById(Long id) {
		if (id == null)
			return null;
		
		SQLiteDatabase db = getReadableDatabase();
		Posicao posicao = null;
		
		try {
			Cursor c = db.rawQuery("SELECT id,  x, y, idAndar " +
					"FROM rooms WHERE id=" + id, null);
			
			if (c.moveToFirst()) {
				posicao = new Posicao();
				posicao.setId(c.getLong(0));
				posicao.setX(c.getInt(1));
				posicao.setY(c.getInt(2));
				posicao.setidAndar(c.getLong(3));
			}
		} finally {
			db.close();
		}
		
		return posicao;
	}

}
