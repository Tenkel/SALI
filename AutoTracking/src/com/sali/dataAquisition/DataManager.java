package com.sali.dataAquisition;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/*
 *  Class that Manages all data access, especially to the database.
 */


/*
 * Extending SQLiteOpenHelper so its objects can manipulate the database
 */
public class DataManager extends SQLiteOpenHelper {

	/*
	 * String with table names (eg. private static final String LocalTable =
	 * "Local") and class (structure like) with inside table columns (eg. public
	 * static class Local)
	 */

	@Override
	public void onConfigure(SQLiteDatabase db) {
		db.setForeignKeyConstraintsEnabled(true);
		super.onConfigure(db);
	}

	private static final String LocalTable = "Local";
	
	public static class Local {
		public static final String ID = "id";
		public static final String NOME = "nome";
		public static final String POSICAOGLOBALLAT0 = "posicaoGlobalLat0";
		public static final String POSICAOGLOBALLONG0 = "posicaoGlobalLong0";
		public static final String POSICAOGLOBALLAT1 = "posicaoGlobalLat1";
		public static final String POSICAOGLOBALLONG1 = "posicaoGlobalLong1";
	}
	
	private static final String AndarTable = "Andar";

	public static class Andar {
		public static final String ID = "id";
		public static final String IDLOCAL = "idLocal";
		public static final String NOME = "nome";
		public static final String URIMAPA = "uriMapa";
		public static final String CAMADAS = "camadas";
		public static final String FK_ANDAR_LOCAL = "fk_Andar_Local";
		public static final String FK_ANDAR_LOCAL_IDX = "fk_Andar_Local_idx";
	}
	
	private static final String PosicaoTable = "Posicao";

	public static class Posicao {
		public static final String ID = "id";
		public static final String IDANDAR = "idAndar";
		public static final String X = "x";
		public static final String Y = "y";
		public static final String FK_POSICAO_ANDAR = "fk_Posicao_Andar";
		public static final String FK_POSICAO_ANDAR_IDX = "fk_Posicao_Andar_idx";
	}
	
	private static final String ObservacaoTable = "Observacao";

	public static class Observacao {
		public static final String ID = "id";
		public static final String IDPOSICAO = "idPosicao";
		public static final String INSTANTECOLETA = "instanteColeta";
		public static final String FK_OBSERVACAO_POSICAO = "fk_Observacao_Posicao";
		public static final String FK_SAMPLE_LOCAL_IDX = "fk_Sample_Local_idx";
	}
	
	private static final String AccessPointTable = "AccessPoint";

	public static class AccessPoint {
		public static final String ID = "id";
		public static final String IDPOSICAO = "idPosicao";
		public static final String BSSID = "bssid";
		public static final String ESSID = "essid";
		public static final String CONFIANCA = "confianca";
		public static final String FK_ACCESSPOINT_POSICAO = "fk_AccessPoint_Posicao";
		public static final String FK_ACCESSPOINT_LOCAL_IDX = "fk_AccessPoint_Local_idx";
	}
	
	private static final String UsuarioTable = "Usuario";

	public static class Usuario {
		public static final String ID = "id";
		public static final String NOME = "nome";
		public static final String CHAVE = "chave";
		public static final String IMEI = "imei";
		public static final String ENDMAC = "endMac";
	}
	
	private static final String LeituraWiFiTable = "LeituraWiFi";

	public static class LeituraWiFi {
		public static final String ID = "id";
		public static final String IDACCESSPOINT = "idAccessPoint";
		public static final String IDOBSERVACAO = "idObservacao";
		public static final String VALOR = "valor";
		public static final String FK_LEITURAWIFI_ACCESSPOINT = "fk_LeituraWiFi_AccessPoint";
		public static final String FK_LEITURAWIFI_OBSERVACAO = "fk_LeituraWiFi_Observacao";
		public static final String FK_WIFIREADING_ACCESSPOINT_IDX = "fk_WiFiReading_AccessPoint_idx";
		public static final String FK_WIFIREADING_SAMPLE_IDX = "fk_WiFiReading_Sample_idx";
	}
	
	private static final String UnidadeSensoresTable = "UnidadeSensores";

	public static class UnidadeSensores {
		public static final String ID = "id";
		public static final String ACC = "acc";
		public static final String TEMP = "temp";
		public static final String GRAV = "grav";
		public static final String GYRO = "gyro";
		public static final String LIGHT = "light";
		public static final String LINACC = "linAcc";
		public static final String MAG = "mag";
		public static final String ORIENT = "orient";
		public static final String PRESS = "press";
		public static final String PROX = "prox";
		public static final String HUM = "hum";
		public static final String ROT = "rot";
	}
	
	private static final String LeituraSensoresTable = "LeituraSensores";

	public static class LeituraSensores {
		public static final String ID = "id";
		public static final String IDOBSERVACAO = "idObservacao";
		public static final String IDUNIDADESENSORES = "idUnidadeSensores";
		public static final String ACCX = "accX";
		public static final String ACCY = "accY";
		public static final String ACCZ = "accZ";
		public static final String TEMP = "temp";
		public static final String GRAVX = "gravX";
		public static final String GRAVY = "gravY";
		public static final String GRAVZ = "gravZ";
		public static final String GYROX = "gyroX";
		public static final String GYROY = "gyroY";
		public static final String GYROZ = "gyroZ";
		public static final String LIGHT = "light";
		public static final String LINACCX = "linAccX";
		public static final String LINACCY = "linAccY";
		public static final String LINACCZ = "linAccZ";
		public static final String MAGX = "magX";
		public static final String MAGY = "magY";
		public static final String MAGZ = "magZ";
		public static final String ORIENTX = "orientX";
		public static final String ORIENTY = "orientY";
		public static final String ORIENTZ = "orientZ";
		public static final String PRESS = "press";
		public static final String PROX = "prox";
		public static final String HUM = "hum";
		public static final String ROTX = "rotX";
		public static final String ROTY = "rotY";
		public static final String ROTZ = "rotZ";
		public static final String ROTSCALAR = "rotScalar";
		public static final String FK_LEITURASENSORES_UNIDADESENSORES = "fk_LeituraSensores_UnidadeSensores";
		public static final String FK_LEITURASENSORES_OBSERVACAO = "fk_LeituraSensores_Observacao";
		public static final String FK_SENSORSREADING_SENSORUNITS_IDX = "fk_SensorsReading_SensorUnits_idx";
		public static final String FK_SENSORSREADING_SAMPLE_IDX = "fk_SensorsReading_Sample_idx";
	}
	
	private static final String FuncaoTipoTable = "FuncaoTipo";

	public static class FuncaoTipo {
		public static final String ID = "id";
		public static final String TIPO = "tipo";
	}

	
	private static final String FuncaoPosicaoTable = "FuncaoPosicao";

	public static class FuncaoPosicao {
		public static final String ID = "id";
		public static final String IDPOSICAO = "idPosicao";
		public static final String IDACCESSPOINT = "idAccessPoint";
		public static final String TIPO = "tipo";
		public static final String URI = "uri";
		public static final String FK_FUNCAOPOSICAO_POSICAO = "fk_FuncaoPosicao_Posicao";
		public static final String FK_FUNCAOPOSICAO_ACCESSPOINT = "fk_FuncaoPosicao_AccessPoint";
		public static final String FK_POSFUNC_LOCAL_IDX = "fk_PosFunc_Local_idx";
		public static final String FK_POSFUNC_ACCESSPOINT_IDX = "fk_PosFunc_AccessPoint_idx";
		public static final String IDTIPO = "idTipo";
		public static final String FK_FUNCAOPOSICAO_FUNCAOTIPO = "fk_FuncaoPosicao_FuncaoTipo";
		public static final String UNIQUE_FUNCAOTIPO = "unique_FuncaoTipo";
	}
	
	private static final String SalaTable = "Sala";

	public static class Sala {
		public static final String ID = "id";
		public static final String IDANDAR = "idAndar";
		public static final String NOME = "nome";
		public static final String IDPOLIGONO = "idPoligono";
		public static final String FK_SALA_ANDAR = "fk_Sala_Andar";
		public static final String FK_SALA_ANDAR_IDX = "fk_Sala_Andar_idx";
	}

	/*
	 * Data base name and version
	 */

	private static final String DB_NAME = "database.db";
	private static final int DB_VERSION = 1;
	private Context theContext;
	
	
	public DataManager(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		theContext = context;
	}

	// Database Creation thought SQL 'CREATE TABLE'
	@Override
	public void onCreate(SQLiteDatabase db) {

		// Local
		db.execSQL("CREATE TABLE " + LocalTable +  " (  " 
				+ Local.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ Local.NOME + " TEXT, "
				+ Local.POSICAOGLOBALLAT0 + " REAL, "
				+ Local.POSICAOGLOBALLAT1 + " REAL, "
				+ Local.POSICAOGLOBALLONG0 + " REAL, "
				+ Local.POSICAOGLOBALLONG1 + " REAL );");

		
		//Table `Andar`
		db.execSQL("CREATE TABLE "+ AndarTable + " ( "
		  + Andar.ID + "INTEGER PRIMARY KEY AUTOINCREMENT, "
		  + Andar.IDLOCAL + " INTEGER, "
		  + Andar.NOME + " TEXT, "
		  + Andar.URIMAPA + " TEXT, "
		  + Andar.CAMADAS + " TEXT, "

		  +"CONSTRAINT " + Andar.FK_ANDAR_LOCAL +
		    "FOREIGN KEY (" + Andar.IDLOCAL + ")" +
		    "REFERENCES " + LocalTable + " (" + Local.ID + ")"
		  
		  +");");

		db.execSQL("CREATE INDEX " + Andar.FK_ANDAR_LOCAL_IDX + " ON " + AndarTable + " (" + Andar.IDLOCAL + " ASC);");
		
		//Table `Posicao`
		db.execSQL("CREATE TABLE "+ PosicaoTable + " ( "
				  + Posicao.ID + "INTEGER PRIMARY KEY AUTOINCREMENT, "
				  + Posicao.IDANDAR + " INTEGER, "
				  + Posicao.X + " REAL, "
				  + Posicao.Y + " REAL, "

				  +"CONSTRAINT " + Posicao.FK_POSICAO_ANDAR +
				    "FOREIGN KEY (" + Posicao.IDANDAR + ")" +
				    "REFERENCES " + AndarTable + " (" + Andar.ID + ")"
				  
				  +");");

		db.execSQL("CREATE INDEX " + Posicao.FK_POSICAO_ANDAR_IDX + " ON " + PosicaoTable + " (" + Posicao.IDANDAR + " ASC);");
				
		//Table `Posicao`
		db.execSQL("CREATE TABLE "+ ObservacaoTable + " ( "
				  + Observacao.ID + "INTEGER PRIMARY KEY AUTOINCREMENT, "
				  + Observacao.IDPOSICAO + " INTEGER, "
				  + Observacao.INSTANTECOLETA + " INTEGER, "

				  +"CONSTRAINT " + Observacao.FK_OBSERVACAO_POSICAO +
				    "FOREIGN KEY (" + Observacao.IDPOSICAO + ")" +
				    "REFERENCES " + PosicaoTable + " (" + Posicao.ID + ")"
				  
				  +");");

		db.execSQL("CREATE INDEX " + Observacao.FK_SAMPLE_LOCAL_IDX + " ON " + ObservacaoTable + " (" + Observacao.IDPOSICAO + " ASC);");
				
		//Table `Observacao`
		db.execSQL("CREATE TABLE "+ ObservacaoTable + " ( "
				  + Observacao.ID + "INTEGER PRIMARY KEY AUTOINCREMENT, "
				  + Observacao.IDPOSICAO + " INTEGER, "
				  + Observacao.INSTANTECOLETA + " INTEGER, "

				  +"CONSTRAINT " + Observacao.FK_OBSERVACAO_POSICAO +
				    "FOREIGN KEY (" + Observacao.IDPOSICAO + ")" +
				    "REFERENCES " + PosicaoTable + " (" + Posicao.ID + ")"
				  
				  +");");

		db.execSQL("CREATE INDEX " + Observacao.FK_SAMPLE_LOCAL_IDX + " ON " + ObservacaoTable + " (" + Observacao.IDPOSICAO + " ASC);");

		
		//Table `AccessPoint`
		db.execSQL("CREATE TABLE "+ AccessPointTable + " ( "
				  + AccessPoint.ID + "INTEGER PRIMARY KEY AUTOINCREMENT, "
				  + AccessPoint.IDPOSICAO + " INTEGER, "
				  + AccessPoint.BSSID + " TEXT UNIQUE, "
				  + AccessPoint.ESSID + " TEXT, "
				  + AccessPoint.CONFIANCA + " TEXT, "

				  +"CONSTRAINT " + AccessPoint.FK_ACCESSPOINT_POSICAO +
				    "FOREIGN KEY (" + AccessPoint.IDPOSICAO + ")" +
				    "REFERENCES " + PosicaoTable + " (" + Posicao.ID + ")"
				  
				  +");");

		db.execSQL("CREATE INDEX " + AccessPoint.FK_ACCESSPOINT_LOCAL_IDX + " ON " + AccessPointTable + " (" + AccessPoint.IDPOSICAO + " ASC);");


		//Table `Usuario`
		db.execSQL("CREATE TABLE "+ UsuarioTable + " ( "
				  + Usuario.ID + "INTEGER PRIMARY KEY AUTOINCREMENT, "
				  + Usuario.NOME + " TEXT, "
				  + Usuario.CHAVE + " TEXT, "
				  + Usuario.IMEI + " TEXT, "
				  + Usuario.ENDMAC + " TEXT");
		

		//Table `LeituraWiFi`
		db.execSQL("CREATE TABLE "+ LeituraWiFiTable + " ( "
				  + LeituraWiFi.ID + "INTEGER PRIMARY KEY AUTOINCREMENT, "
				  + LeituraWiFi.IDACCESSPOINT + " INTEGER, "
				  + LeituraWiFi.IDOBSERVACAO + " INTEGER, "
				  + LeituraWiFi.VALOR + " INTEGER, "

				  +"CONSTRAINT " + LeituraWiFi.FK_LEITURAWIFI_ACCESSPOINT +
				    "FOREIGN KEY (" + LeituraWiFi.IDACCESSPOINT + ")" +
				    "REFERENCES " + AccessPointTable + " (" + AccessPoint.ID + ")"

				  +"CONSTRAINT " + LeituraWiFi.FK_LEITURAWIFI_OBSERVACAO +
				    "FOREIGN KEY (" + LeituraWiFi.IDOBSERVACAO + ")" +
				    "REFERENCES " + ObservacaoTable + " (" + Observacao.ID + ")"
				  
				  +");");

		db.execSQL("CREATE INDEX " + LeituraWiFi.FK_WIFIREADING_ACCESSPOINT_IDX + " ON " + LeituraWiFiTable + " (" + LeituraWiFi.IDACCESSPOINT + " ASC);");
		

		db.execSQL("CREATE INDEX " + LeituraWiFi.FK_WIFIREADING_SAMPLE_IDX + " ON " + LeituraWiFiTable + " (" + LeituraWiFi.IDOBSERVACAO + " ASC);");
		

		//Table `UnidadeSensores`
		db.execSQL("CREATE TABLE "+ UnidadeSensoresTable + " ( "
				  + UnidadeSensores.ID + "INTEGER PRIMARY KEY AUTOINCREMENT, "
				  + UnidadeSensores.ACC + " TEXT, "
				  + UnidadeSensores.TEMP + " TEXT, "
				  + UnidadeSensores.GRAV + " TEXT, "
				  + UnidadeSensores.GYRO + " TEXT, "
				  + UnidadeSensores.LIGHT + " TEXT, "
				  + UnidadeSensores.LINACC + " TEXT, "
				  + UnidadeSensores.MAG + " TEXT, "
				  + UnidadeSensores.ORIENT + " TEXT, "
				  + UnidadeSensores.PRESS + " TEXT, "
				  + UnidadeSensores.PROX + " TEXT, "
				  + UnidadeSensores.HUM + " TEXT, "
				  + UnidadeSensores.ROT + " TEXT");
		
		

		//Table `LeituraSensores`
		db.execSQL("CREATE TABLE "+ LeituraSensoresTable + " ( "
				  + LeituraSensores.ID + "INTEGER PRIMARY KEY AUTOINCREMENT, "
				  + LeituraSensores.IDOBSERVACAO + " INTEGER, "
				  + LeituraSensores.IDUNIDADESENSORES + " INTEGER, "
				  + LeituraSensores.ACCX + " REAL, "
				  + LeituraSensores.ACCY + " REAL, "
				  + LeituraSensores.ACCZ + " REAL, "
				  + LeituraSensores.TEMP + " REAL, "
				  + LeituraSensores.GRAVX + " REAL, "
				  + LeituraSensores.GRAVY + " REAL, "
				  + LeituraSensores.GRAVZ + " REAL, "
				  + LeituraSensores.GYROX + " REAL, "
				  + LeituraSensores.GYROY + " REAL, "
				  + LeituraSensores.GYROZ + " REAL, "
				  + LeituraSensores.LIGHT + " REAL, "
				  + LeituraSensores.LINACCX + " REAL, "
				  + LeituraSensores.LINACCY + " REAL, "
				  + LeituraSensores.LINACCZ + " REAL, "
				  + LeituraSensores.MAGX + " REAL, "
				  + LeituraSensores.MAGY + " REAL, "
				  + LeituraSensores.MAGZ + " REAL, "
				  + LeituraSensores.ORIENTX + " REAL, "
				  + LeituraSensores.ORIENTY + " REAL, "
				  + LeituraSensores.ORIENTZ + " REAL, "
				  + LeituraSensores.PRESS + " REAL, "
				  + LeituraSensores.PROX + " REAL, "
				  + LeituraSensores.HUM + " REAL, "
				  + LeituraSensores.ROTX + " REAL, "
				  + LeituraSensores.ROTY + " REAL, "
				  + LeituraSensores.ROTZ + " REAL, "
				  + LeituraSensores.ROTSCALAR + " REAL, "
				  
				  
				  +"CONSTRAINT " + LeituraSensores.FK_LEITURASENSORES_UNIDADESENSORES +
				    "FOREIGN KEY (" + LeituraSensores.IDUNIDADESENSORES + ")" +
				    "REFERENCES " + UnidadeSensoresTable + " (" + UnidadeSensores.ID + ")"

				  +"CONSTRAINT " + LeituraSensores.FK_LEITURASENSORES_OBSERVACAO +
				    "FOREIGN KEY (" + LeituraSensores.IDOBSERVACAO + ")" +
				    "REFERENCES " + ObservacaoTable + " (" + Observacao.ID + ")"
				  
				  +");");

		db.execSQL("CREATE INDEX " + LeituraSensores.FK_SENSORSREADING_SENSORUNITS_IDX + " ON " + LeituraSensoresTable + " (" + LeituraSensores.IDUNIDADESENSORES + " ASC);");
		

		db.execSQL("CREATE INDEX " + LeituraSensores.FK_SENSORSREADING_SAMPLE_IDX + " ON " +LeituraSensoresTable + " (" + LeituraSensores.IDOBSERVACAO + " ASC);");


		// Local
		db.execSQL("CREATE TABLE " + FuncaoTipoTable +  " (  " 
				+ FuncaoTipo.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ FuncaoTipo.TIPO + " TEXT");

		//Table `FuncaoPosicao`
		db.execSQL("CREATE TABLE "+ FuncaoPosicaoTable + " ( "
				  + FuncaoPosicao.ID + "INTEGER PRIMARY KEY AUTOINCREMENT, "
				  + FuncaoPosicao.IDPOSICAO + " INTEGER, "
				  + FuncaoPosicao.IDACCESSPOINT + " INTEGER, "
				  + FuncaoPosicao.IDTIPO + " INTEGER, "
				  + FuncaoPosicao.URI + " TEXT, "

				  +"CONSTRAINT " + FuncaoPosicao.FK_FUNCAOPOSICAO_POSICAO +
				    "FOREIGN KEY (" + FuncaoPosicao.IDPOSICAO + ")" +
				    "REFERENCES " + PosicaoTable + " (" + Posicao.ID + ")"

				  +"CONSTRAINT " + FuncaoPosicao.FK_FUNCAOPOSICAO_ACCESSPOINT +
				    "FOREIGN KEY (" + FuncaoPosicao.IDACCESSPOINT + ")" +
				    "REFERENCES " + AccessPointTable + " (" + AccessPoint.ID + ")"

				  +"CONSTRAINT " + FuncaoPosicao.FK_FUNCAOPOSICAO_FUNCAOTIPO +
				    "FOREIGN KEY (" + FuncaoPosicao.IDTIPO + ")" +
				    "REFERENCES " + FuncaoTipoTable + " (" + FuncaoTipo.ID + ")"

				  +"CONSTRAINT " + FuncaoPosicao.UNIQUE_FUNCAOTIPO +
				    "UNIQUE (" + FuncaoPosicao.IDPOSICAO + ", " 
				    		   + FuncaoPosicao.IDACCESSPOINT + ", " 
				    		   + FuncaoPosicao.IDTIPO + ")"
				  
				  +");");

		db.execSQL("CREATE INDEX " + FuncaoPosicao.FK_POSFUNC_LOCAL_IDX + " ON " + FuncaoPosicaoTable + " (" + FuncaoPosicao.IDPOSICAO + " ASC);");
		

		db.execSQL("CREATE INDEX " + FuncaoPosicao.FK_POSFUNC_ACCESSPOINT_IDX + " ON " + FuncaoPosicaoTable + " (" + FuncaoPosicao.IDACCESSPOINT + " ASC);");

		
		

		//Table `Sala`
		db.execSQL("CREATE TABLE "+ SalaTable + " ( "
				  + Sala.ID + "INTEGER PRIMARY KEY AUTOINCREMENT, "
				  + Sala.IDANDAR + " INTEGER, "
				  + Sala.NOME + " TEXT, "
				  + Sala.IDPOLIGONO + " TEXT, "

				  +"CONSTRAINT " + Sala.FK_SALA_ANDAR +
				    "FOREIGN KEY (" + Sala.IDANDAR + ")" +
				    "REFERENCES " + AndarTable + " (" + Andar.ID + ")"
				  
				  +");");

		db.execSQL("CREATE INDEX " + Sala.FK_SALA_ANDAR_IDX + " ON " + SalaTable + " (" + Sala.IDANDAR + " ASC);");
		
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
