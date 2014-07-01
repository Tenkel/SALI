package com.sali.dataAquisition;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ObservacaoManager extends DataManager {
	
	public ObservacaoManager(Context c) {
		super(c);
	}
	
	public void save(Observacao observacao){
		SQLiteDatabase db = getWritableDatabase();
		
		try {
			ContentValues values = new ContentValues();
			values.put("timestamp", observacao.gettimestamp());
			values.put("idPosicao", observacao.getidPosicao());
			observacao.setId(db.insert("observacao", null, values));
		} finally {
			db.close();
		}
	}
	
	public List<Observacao> getByidPosicao(long idPosicao) {
		List<Observacao> list = new ArrayList<Observacao>();
		SQLiteDatabase db = getReadableDatabase();

		try {
			Cursor c = db.rawQuery("SELECT id, timestamp, idPosicao " +
					"FROM Observacao WHERE idPosicao=" + idPosicao, null);
			
			while (c.moveToNext()) {
				Observacao observacao = new Observacao();
				observacao.setId(c.getLong(0));
				observacao.settimestamp(c.getInt(1));
				observacao.setidPosicao(c.getLong(2));
				list.add(observacao);
			}
		} finally {
			db.close();
		}

		return list;
	}
	
	public int getCount() {
		SQLiteDatabase db = getReadableDatabase();
		int counter = 0;
		
		try {
			Cursor c = db.rawQuery("SELECT count(*) FROM observacao", null);
			if (c.moveToFirst())
				counter = c.getInt(0);
			c.close();
		} finally {
			db.close();
		}
		
		return counter;
	}

	public List<Observacao> getAll() {
		List<Observacao> list = new ArrayList<Observacao>();
		SQLiteDatabase db = getReadableDatabase();

		try {
			Cursor c = db.query("observacao", new String[] {"id", "timestamp", "idPosicao"}, null, null, null, null, null);
			
			while (c.moveToNext()) {
				Observacao observacao = new Observacao();
				observacao.setId(c.getLong(0));
				observacao.settimestamp(c.getInt(1));
				observacao.setidPosicao(c.getLong(2));
				list.add(observacao);
			}
		} finally {
			db.close();
		}

		return list;
	}

}
