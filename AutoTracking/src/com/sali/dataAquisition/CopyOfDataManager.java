package com.sali.dataAquisition;

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

import com.sali.algorithms.KDE;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.widget.Toast;

/*
 *  Class that Manages all data access, especially to the database.
 */

public class CopyOfDataManager {
	// SQLiteStatement holders
	SQLiteStatement nroomsquery;
	SQLiteStatement nsamplesquery;

	/*
	 * String with table names (eg. private static final String LocalTable =
	 * "Local") and class (structure like) with inside table columns (eg. public
	 * static class Local)
	 */
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
//		public static final String GYROAC = "Vi_gyro_accuracy";
		public static final String LOCAL = "Cd_local";
		public static final String AP = "Cd_access_point";
	}

	private static final String SampleTable = "Sample";

	/*
	 * Data base name and version
	 */

	private static final String DB_NAME = "FindDB";
	private static final int DB_VERSION = 1;

	// Object that connects to the database
	private DBMng DBManager;
	// Used to save the original context from which it's called
	private final Context theContext;
	// The actual database object
	private SQLiteDatabase DBFind;
	// Cursors list so they are automatically destroyed when the object itself
	// is.
	private ArrayList<Cursor> cursors = new ArrayList<Cursor>();

	/*
	 * Extending SQLiteOpenHelper so its objects can manipulate the database
	 */
	private static class DBMng extends SQLiteOpenHelper {

		public DBMng(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		// Database Creation thought SQL 'CREATE TABLE'
		@Override
		public void onCreate(SQLiteDatabase db) {
			// Foreign Keys = ON
			db.execSQL("PRAGMA foreign_keys = ON;");

			// Local
			db.execSQL("CREATE TABLE " + LocalTable + "(" + Local.ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT," + Local.PX
					+ " INTEGER, " + Local.PY + " INTEGER, " + Local.ROOM
					+ " INTEGER, " + "UNIQUE(" + Local.PX + ", " + Local.PY
					+ ") );");

			// Access_Point
			db.execSQL("CREATE TABLE " + Access_PointTable + "("
					+ Access_Point.ID + " integer primary key autoincrement, "
					+ Access_Point.NAME + " TEXT UNIQUE);");

			// KSD
			db.execSQL("CREATE TABLE " + KSDTable + "(" + KSD.ID_AP
					+ " INTEGER," + KSD.ID_LOCAL + " INTEGER, " + KSD.LINK
					+ " TEXT," + "FOREIGN KEY(" + KSD.ID_LOCAL
					+ ") REFERENCES " + LocalTable + "(" + Local.ID + "),"
					+ "FOREIGN KEY(" + KSD.ID_AP + ") REFERENCES "
					+ Access_PointTable + "(" + Access_Point.ID + "),"
					+ "PRIMARY KEY(" + KSD.ID_AP + ", " + KSD.ID_LOCAL + ") );");

			// Sample
			db.execSQL("CREATE TABLE " + SampleTable + "(" + Sample.ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + Sample.VALUE
					+ " INTEGER," + Sample.GYROX + " REAL," + Sample.GYROY
					+ " REAL," + Sample.GYROZ + " REAL," 
//					+ Sample.GYROAC	+ " INTEGER," 
					+ Sample.LOCAL + " INTEGER," + Sample.AP
					+ " INTEGER," + "FOREIGN KEY(" + Sample.LOCAL
					+ ") REFERENCES " + KSDTable + "(" + KSD.ID_LOCAL + "),"
					+ "FOREIGN KEY(" + Sample.AP + ") REFERENCES " + KSDTable
					+ "(" + KSD.ID_AP + ") );");

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		}

	}

	// Constructor save context and allocate new DB.
	public CopyOfDataManager(Context c) {
		theContext = c;
		DBManager = new DBMng(theContext);
	}

	// Get writable handler to the DB.
	public CopyOfDataManager open() {
		DBFind = DBManager.getWritableDatabase();
		return this;
	}

	// Deallocate all opened cursors and release handler.
	public void close() {
		for (Cursor cursor : cursors)
			cursor.close();
		DBManager.close();
	}

	/*
	 * Insert new sample entry, including:
	 * 
	 * -RSSID power, -the Access Point BSSID, -the XY and Room position of the
	 * device (User-entered), -Angle get thought gyroscope and
	 * magnetometer(float) as well as its accuracy(int),
	 */

	public void insert(int power, String APcode, int localX, int localY,
			int room, float gyrox, float gyroy, float gyroz/*, int gyroac*/) {
		long localid, apid;

		// Local
		ContentValues entry = new ContentValues();
		entry.put(Local.PX, localX);
		entry.put(Local.PY, localY);
		entry.put(Local.ROOM, room);
		localid = DBFind.insert(LocalTable, null, entry);
		if (localid == -1) {
			Cursor c = DBFind.query(LocalTable, new String[] { "rowid" },
					Local.PX + " == " + localX + " AND " + Local.PY + " == "
							+ localY, null, null, null, null);
			c.moveToFirst();
			localid = c.getLong(0);
			c.close();
		}

		// Access Point
		entry.clear();
		entry.put(Access_Point.NAME, APcode);
		apid = DBFind.insert(Access_PointTable, null, entry);
		if (apid == -1) {
			Cursor c = DBFind.query(Access_PointTable,
					new String[] { "rowid" }, Access_Point.NAME + " == '"
							+ APcode + "'", null, null, null, null);
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
//		entry.put(Sample.GYROAC, gyroac);
		entry.put(Sample.LOCAL, localid);
		entry.put(Sample.AP, apid);
		DBFind.insert(SampleTable, null, entry);

	}

	// Indirect query for locals
	public Cursor Local(String[] infos, String Where) {
		Cursor c = DBFind.query(LocalTable, infos, Where, null, null, null,
				null);
		cursors.add(c);
		return c;
	}

	// Indirect query for Access Points
	public Cursor AP(String[] infos, String Where) {
		Cursor c = DBFind.query(Access_PointTable, infos, Where, null, null,
				null, null);
		cursors.add(c);
		return c;
	}

	// Return the KDE object, which describes the probability distribution for a
	// given Local (index) and Access Point (index).
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
			if (list.getCount() == 0) {
				list.close();
				return KDE.kde;
			}

			filename = list.getString(0);
			try {
				Log.d("DataManager", "Opening file stream...");
				is = new FileInputStream(theContext.getFilesDir()
						+ File.separator + filename);
				Log.d("DataManager", "Opened " + theContext.getFilesDir()
						+ File.separator + filename);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				list.close();
				return null;
			}

			try {
				Log.d("DataManager", "Opening object stream...");
				ois = new ObjectInputStream(is);
				Log.d("DataManager", "Object stream open");
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
				Log.d("DataManager", "KDE Object read");
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

	// Calculate all KDE objects, for the whole database.
	public void KSDWarming() {
		ContentValues update = new ContentValues();
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		Cursor KSDlist = DBFind.query(KSDTable, new String[] { "rowid",
				KSD.ID_AP, KSD.ID_LOCAL }, null, null, null, null, null);
		Cursor Samplelist = null;
		KDE ksfunction = null;
		String filename = "";
		String where = Sample.LOCAL + " = ? AND " + Sample.AP + " = ?";
		String[] columns = new String[] { Sample.VALUE };

		for (KSDlist.moveToFirst(); !KSDlist.isAfterLast(); KSDlist
				.moveToNext()) {
			Samplelist = DBFind.query(
					SampleTable,
					columns,
					where,
					new String[] { String.valueOf(KSDlist.getLong(2)),
							String.valueOf(KSDlist.getLong(1)) }, null, null,
					null);

			ksfunction = new KDE(Samplelist, 0);
			filename = "ks" + KSDlist.getLong(0);
			try {
				fos = new FileOutputStream(theContext.getFilesDir()
						+ File.separator + filename);
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

	// Indirect and simplified query for samples, for a given local an access
	// point.
	public Cursor Samples(long localid, long apid) {
		String where = Sample.AP + " == " + apid + " AND " + Sample.LOCAL
				+ " == " + localid;
		Cursor list = DBFind.query(SampleTable, new String[] { Sample.VALUE },
				where, null, null, null, null);
		cursors.add(list);
		return list;
	}

	// Number of locals samples (NOT only rooms!)
	public long NRooms() {
		nroomsquery = DBFind.compileStatement("select count(*) from "
				+ LocalTable + " ");
		return nroomsquery.simpleQueryForLong();
	}

	public long NSamples(long room) {
		nsamplesquery = DBFind.compileStatement("select count(*) from "
				+ SampleTable + ", " + LocalTable + " where " + SampleTable
				+ "." + Sample.LOCAL + " = " + LocalTable + "." + Local.ID
				+ " and " + LocalTable + "." + Local.ROOM + " = ? ");

		nsamplesquery.bindLong(1, room);
		return nsamplesquery.simpleQueryForLong();

	}

	// Delete a given local, all its samples and its KDE object link, object
	// itself if present.
	public void deleteroom(int room) {

		String where;
		int cdroom;

		SQLiteStatement sqlroom = DBFind.compileStatement("select (" + Local.ID
				+ ") from " + LocalTable + " where " + Local.ROOM + " = "
				+ room);

		// return void if there was no rows!
		try {
			cdroom = (int) sqlroom.simpleQueryForLong();
		} catch (SQLiteDoneException e) {
			e.printStackTrace();
			Toast.makeText(theContext, "There wasn't anything to delete =(",
					Toast.LENGTH_LONG).show();
			return;
		}

		where = Sample.LOCAL + " == " + cdroom;
		DBFind.delete(SampleTable, where, null);

		where = KSD.ID_LOCAL + " == " + cdroom;
		DBFind.delete(KSDTable, where, null);

		theContext.deleteFile("ks" + cdroom);

		where = Local.ID + " == " + cdroom;
		DBFind.delete(LocalTable, where, null);

		Toast.makeText(theContext,
				"YES! We did it! No more samples data here...",
				Toast.LENGTH_LONG).show();

	}

	// return the mean of the samples per Access Point at a given room
	public float NSamplesmean(long room) {

		nsamplesquery = DBFind.compileStatement("select count(*) from "
				+ SampleTable + ", " + LocalTable + " where " + SampleTable
				+ "." + Sample.LOCAL + " = " + LocalTable + "." + Local.ID
				+ " and " + LocalTable + "." + Local.ROOM + " = ? ");

		nsamplesquery.bindLong(1, room);

		SQLiteStatement npaquery = DBFind
				.compileStatement("select count(*) from " + KSDTable + ", "
						+ LocalTable + " where " + KSDTable + "."
						+ KSD.ID_LOCAL + " = " + LocalTable + "." + Local.ID
						+ " and " + LocalTable + "." + Local.ROOM + " = "
						+ room);

		float ans = (float) npaquery.simpleQueryForLong();
		if (ans == 0)
			return 0;

		return nsamplesquery.simpleQueryForLong() / ans;

	}

	// Export Database to a given path with a given name
	public void DBexport(String toPath, String name) throws IOException {
		String dbPath = DBFind.getPath();
		Log.d("DBexport", dbPath);
		FileInputStream newDb = new FileInputStream(new File(dbPath));
		FileOutputStream toFile = new FileOutputStream(new File(toPath, name));
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

	// Overload DBexport for a std name called BDbackup.db
	public void DBexport(String toPath) throws IOException {
		DBexport(toPath, "BDbackup.db");
	}

	// Import Database from a given path with a given name
	public void DBimport(String fromPath, String name) throws IOException {
		String dbPath = DBFind.getPath();
		Log.d("DBimport", dbPath);
		FileInputStream newDb = new FileInputStream(new File(fromPath, name));
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

	// Overload DBimport for a std name called BDbackup.db
	public void DBimport(String toPath) throws IOException {
		DBimport(toPath, "BDbackup.db");
	}

}
