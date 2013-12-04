package com.sali.autotracking;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.widget.Toast;

public class DataManager {
	SQLiteStatement nroomsquery;
	SQLiteStatement nsamplesquery;
	
	public static class Local {
		public static final String ID = "Cd_local";
		public static final String PX = "Vi_px";
		public static final String PY = "Vi_py";
		public static final String ROOM = "Cd_room";
	} 

	private static final String LocalTable = "Local";

	public static class Access_Point {
		public static final String ID = "Cd_access_point";
		public static final String NAME = "Nm_name";
	}

	private static final String Access_PointTable = "Access_Point";

	public static class KSD {
		public static final String ID_LOCAL = "Cd_local";
		public static final String ID_AP = "Cd_access_point";
		public static final String LINK = "Nm_ksd_link";
	}

	private static final String KSDTable = "KSD";

	public static class Sample {
		public static final String ID = "Cd_sample";
		public static final String VALUE = "Vi_value";
		public static final String GYROX = "Vi_gyrox";
		public static final String GYROY = "Vi_gyroy";
		public static final String GYROZ = "Vi_gyroz";
		public static final String GYROAC = "Vi_gyro_accuracy";
		public static final String LOCAL = "Cd_local";
		public static final String AP = "Cd_access_point";
	}

	private static final String SampleTable = "Sample";

	private static final String DB_NAME = "FindDB";
	private static final int DB_VERSION = 1;

	private DBMng DBManager;
	private final Context theContext;
	private SQLiteDatabase DBFind;
	private ArrayList<Cursor> cursors = new ArrayList<Cursor>();

	private static class DBMng extends SQLiteOpenHelper {

		public DBMng(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// Foreign Keys = ON
			db.execSQL("PRAGMA foreign_keys = ON;");
			
			// Local			
			db.execSQL("CREATE TABLE " + LocalTable + "(" 
					+ Local.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
					+ Local.PX + " INTEGER, "
					+ Local.PY + " INTEGER, " 
					+ Local.ROOM +" INTEGER, " 
					+ "UNIQUE(" + Local.PX + ", " + Local.PY + ") );");
			
			
			// Access_Point	
			db.execSQL("CREATE TABLE " + Access_PointTable + "(" 
					+ Access_Point.ID + " integer primary key autoincrement, "
					+ Access_Point.NAME + " TEXT UNIQUE);");

			// KSD
			db.execSQL("CREATE TABLE " + KSDTable + "(" 
					+ KSD.ID_AP + " INTEGER,"
					+ KSD.ID_LOCAL + " INTEGER, " 
					+ KSD.LINK + " TEXT,"
					+ "FOREIGN KEY(" + KSD.ID_LOCAL + ") REFERENCES " + LocalTable + "(" + Local.ID + "),"
					+ "FOREIGN KEY(" + KSD.ID_AP + ") REFERENCES " + Access_PointTable + "(" + Access_Point.ID + ")," 
					+ "PRIMARY KEY(" + KSD.ID_AP + ", " + KSD.ID_LOCAL + ") );");

			// Sample
			db.execSQL("CREATE TABLE " + SampleTable + "(" 
					+ Sample.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
					+ Sample.VALUE + " INTEGER,"
					+ Sample.GYROX + " REAL,"
					+ Sample.GYROY + " REAL,"
					+ Sample.GYROZ + " REAL,"
					+ Sample.GYROAC + " INTEGER,"
					+ Sample.LOCAL + " INTEGER," 
					+ Sample.AP + " INTEGER,"
					+ "FOREIGN KEY(" + Sample.LOCAL + ") REFERENCES " + KSDTable + "(" + KSD.ID_LOCAL + "),"
					+ "FOREIGN KEY(" + Sample.AP + ") REFERENCES " + KSDTable + "(" + KSD.ID_AP + ") );");


		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub

		}

	}

	public DataManager(Context c) {
		theContext = c;
		DBManager = new DBMng(theContext);
	}

	public DataManager open() {
		DBFind = DBManager.getWritableDatabase();
		return this;
	}

	public void close() {
		for(Cursor cursor : cursors)
			cursor.close();
		DBManager.close();
	}

	public void insert(int power, String APcode, int localX, int localY,
			int room, float gyrox, float gyroy, float gyroz, int gyroac) {
		long localid, apid;

		// Local
		ContentValues entry = new ContentValues();
		entry.put(Local.PX, localX);
		entry.put(Local.PY, localY);
		entry.put(Local.ROOM, room);
		localid = DBFind.insert(LocalTable, null, entry);
		if(localid == -1){
			Cursor c = DBFind.query(LocalTable, new String[]{"rowid"}, Local.PX + " == " + localX + " AND " + Local.PY + " == " + localY, null, null, null, null);
			c.moveToFirst();
			localid = c.getLong(0);
			c.close();
			}
		
		// Access Point
		entry.clear();
		entry.put(Access_Point.NAME, APcode);
		apid = DBFind.insert(Access_PointTable, null, entry);
		if(apid == -1){
			Cursor c = DBFind.query(Access_PointTable, new String[]{"rowid"}, Access_Point.NAME + " == '" + APcode + "'", null, null, null, null);
			c.moveToFirst();
			apid = c.getLong(0);
			c.close();
		}
		
		// KSD
		entry.clear();
		entry.put(KSD.ID_AP, apid);
		entry.put(KSD.ID_LOCAL, localid);
		entry.put(KSD.LINK, "");
		DBFind.insert(KSDTable, null, entry);

		// Sample
		entry.clear();
		entry.put(Sample.VALUE, power);
		entry.put(Sample.GYROX, gyrox);
		entry.put(Sample.GYROY, gyroy);
		entry.put(Sample.GYROZ, gyroz);
		entry.put(Sample.GYROAC, gyroac);
		entry.put(Sample.LOCAL, localid);
		entry.put(Sample.AP, apid);
		DBFind.insert(SampleTable, null, entry);

	}

	public Cursor Local(String[] infos, String Where) {
		Cursor c = DBFind.query(LocalTable, infos, Where, null, null, null, null);
		cursors.add(c);
		return c;
	}

	public Cursor AP(String[] infos, String Where) {
		Cursor c = DBFind.query(Access_PointTable, infos, Where, null, null, null,
				null);
		cursors.add(c);
		return c;
	}

	public KDE KSDFunction(long localid, long apid) {
		InputStream is = null;
		ObjectInputStream ois = null;
		KDE function = null;
		String where = KSD.ID_AP + " == " + apid + " AND " + KSD.ID_LOCAL
				+ " == " + localid;
		Cursor list = DBFind.query(KSDTable, new String[] { KSD.LINK,
				KSD.ID_AP, KSD.ID_LOCAL }, where, null, null, null, null);
		if (list != null) {
			list.moveToFirst();
			String filename;
			if (list.getCount() == 0){
				list.close();
				return KDE.kde;
			}

			filename = list.getString(0);
			try {
				Log.d("DTM", "opening...");
				is = new FileInputStream(theContext.getFilesDir()
						+ File.separator + filename);
				Log.d("DTM", "Opened " + theContext.getFilesDir()
						+ File.separator + filename);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				list.close();
				return null;
			}

			try {
				Log.d("DTM", "null");
				ois = new ObjectInputStream(is);
				Log.d("DTM", "Opened2");
			} catch (StreamCorruptedException e) {
				Toast.makeText(theContext, "File " + filename + " corrupted.",
						Toast.LENGTH_LONG).show();
				e.printStackTrace();
			} catch (IOException e) {
				Toast.makeText(theContext,
						"File " + filename + " cannot be read.",
						Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
			
			try {
				function = (KDE) ois.readObject();
				Log.d("DTM", "Opened3");
			} catch (OptionalDataException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (ois != null)
				try {
					ois.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			
			list.close();
			return function;
		}
		
		return null;
	}

	public void KSDWarming() {
		ContentValues update = new ContentValues();
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		Cursor KSDlist = DBFind.query(KSDTable, new String[] { "rowid",	KSD.ID_AP, KSD.ID_LOCAL }, null, null, null, null, null);
		Cursor Samplelist = null;
		KDE ksfunction = null;
		String filename = "";
		String where = Sample.LOCAL + " = ? AND " + Sample.AP + " = ?";
		String[] columns = new String[] { Sample.VALUE };
		
	

		for (KSDlist.moveToFirst(); !KSDlist.isAfterLast(); KSDlist.moveToNext()) {
			Samplelist = DBFind
					.query(SampleTable,
							columns,
							where,
							new String[] { String.valueOf(KSDlist.getLong(2)) ,
									String.valueOf(KSDlist.getLong(1)) }, null, null, null);

			ksfunction = new KDE(Samplelist, 0);
			filename = "ks" + KSDlist.getLong(0);
			try {
				fos = new FileOutputStream(theContext.getFilesDir() + File.separator + filename);
			} catch (FileNotFoundException e) {
				
				Toast.makeText(theContext,
						"File " + filename + " cannot be created.",
						Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
			try {
				oos = new ObjectOutputStream(fos);
			} catch (IOException e) {
				Toast.makeText(theContext,
						"File " + filename + " cannot be accessed.",
						Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
			try {
				oos.writeObject(ksfunction);
			} catch (IOException e) {
				Toast.makeText(theContext,
						"File " + filename + " cannot be written.",
						Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}

			update.clear();
			update.put(KSD.LINK, filename);
			DBFind.update(KSDTable, update, "rowid == " + KSDlist.getLong(0),
					null);

		}
		try {
			oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		KSDlist.close();
		Samplelist.close();

	}

	public Cursor Samples(long localid, long apid) {
		String where = Sample.AP + " == " + apid + " AND " + Sample.LOCAL
				+ " == " + localid;
		Cursor list = DBFind.query(SampleTable, new String[] { Sample.VALUE },
				where, null, null, null, null);
		cursors.add(list);
		return list;
	}
	
	public long NRooms(){
		nroomsquery = DBFind.compileStatement("select count(*) from " + LocalTable + " ");
		return nroomsquery.simpleQueryForLong();		
	}
	
	public long NSamples(long room){
		nsamplesquery = DBFind.compileStatement("select count(*) from " + SampleTable + ", " + LocalTable  + " where " 
		+ SampleTable+"."+Sample.LOCAL + " = " + LocalTable+"."+Local.ID
		+ " and " +
		 LocalTable+"."+Local.ROOM+" = ? ");
		
		nsamplesquery.bindLong(1, room);
		return nsamplesquery.simpleQueryForLong();
		
	}
	
	public void deleteroom(int room){
		
		String where;
		
		SQLiteStatement sqlroom = DBFind.compileStatement("select ("+Local.ID+") from "+LocalTable+" where "+Local.ROOM+" = "+room);
		int cdroom = (int) sqlroom.simpleQueryForLong();
		
		where = Sample.LOCAL + " == " + cdroom;
		DBFind.delete(SampleTable, where, null);
		
		where = KSD.ID_LOCAL + " == " + cdroom;
		DBFind.delete(KSDTable, where, null);
		
		where = Local.ID + " == " + cdroom;
		DBFind.delete(LocalTable, where, null);
		
		
	}
	
	public float NSamplesmean(long room){

		nsamplesquery = DBFind.compileStatement("select count(*) from " + SampleTable + ", " + LocalTable  + " where " 
		+ SampleTable+"."+Sample.LOCAL + " = " + LocalTable+"."+Local.ID
		+ " and " +
		 LocalTable+"."+Local.ROOM+" = ? ");
		
		nsamplesquery.bindLong(1, room);

		SQLiteStatement npaquery = DBFind.compileStatement("select count(*) from " + KSDTable + ", " + LocalTable  + " where " 
		+ KSDTable+"."+KSD.ID_LOCAL + " = " + LocalTable+"."+Local.ID
		+ " and " +
		 LocalTable+"."+Local.ROOM+" = " + room);
		
		float ans = (float) npaquery.simpleQueryForLong();
		if(ans == 0)
			return 0;
		
		
		return nsamplesquery.simpleQueryForLong()/ans;
		
	}
	
	public void DBexport(String toPath) throws IOException{
		String dbPath = DBFind.getPath();
		Log.d("DBexport", dbPath);
		FileInputStream newDb = new FileInputStream(new File(dbPath));
		FileOutputStream toFile = new FileOutputStream(new File(toPath,"BDbackup.db"));
	    FileChannel fromChannel = null;
        FileChannel toChannel = null;
        try {
            fromChannel = newDb.getChannel();
            toChannel = toFile.getChannel();
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        } finally {
            try {
                if (fromChannel != null) {
                    fromChannel.close();
                    newDb.close();
                }
            } finally {
                if (toChannel != null) {
                    toChannel.close();
                    toFile.close();
                }
            }
        }
	}
	
	public void DBimport(String fromPath) throws IOException{
		String dbPath = DBFind.getPath();
		Log.d("DBexport", dbPath);
		FileInputStream newDb = new FileInputStream(new File(fromPath,"BDbackup.db"));
		FileOutputStream toFile = new FileOutputStream(new File(dbPath));
	    FileChannel fromChannel = null;
        FileChannel toChannel = null;
        try {
            fromChannel = newDb.getChannel();
            toChannel = toFile.getChannel();
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        } finally {
            try {
                if (fromChannel != null) {
                    fromChannel.close();
                    newDb.close();
                }
            } finally {
                if (toChannel != null) {
                    toChannel.close();
                    toFile.close();
                }
            }
        }
	}
}

