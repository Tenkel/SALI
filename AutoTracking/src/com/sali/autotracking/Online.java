package com.sali.autotracking;

import java.util.List;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.util.FloatMath;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.sali.autotracking.R;
import com.sali.dataAquisition.DataManager;

public class Online extends Activity implements OnTouchListener {

	// Algorithms Variables
	private boolean warmed = false;
	private boolean AcquisitionDONE = false;
	private int numscan=0;
	private String[] PAsMEAN=new String[100];
	private int[] PowersMEAN=new int[100];
	private int[] NumbersMean = new int[100];
	private static final int STEP_ONE_COMPLETE = 0;
	int[] place=new int[2];
	
	// DB
	private DataManager DTmg;
	
	// Services variables
	boolean mBound = false;
	Messenger mService = null;

	
	// Matrices to move and zoom image
	ImageView view;
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();
	BitmapDrawable bd;
	public static final String PREF = "ZoomPref";
	
	// Wifi Variables
	WifiManager wifi;
	
	// BroadcastReceiver recebe de maneira assíncrona os dados da captura
	BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context c, Intent i) {
			WifiManager w = (WifiManager) c
					.getSystemService(Context.WIFI_SERVICE);
			w.getScanResults(); // Recebe os dados

			// Cria lista dos dados recebidos
			List<ScanResult> results = w.getScanResults();
			String[] PAs = new String[100];
			int[] Powers = new int[100];
			int index=0;
			for (ScanResult result : results) {
				PAs[index]=result.BSSID;
				Powers[index]=result.level;
				index++;
			}
			
			// Adiciona na lista a rede conectada, se houver
			WifiInfo actual_connection = w.getConnectionInfo();
			if(actual_connection.getNetworkId()!=-1){
			PAs[index]=actual_connection.getBSSID();
			Powers[index]=actual_connection.getRssi();
			index++;}
			
			RSSIMEAN(PAs,Powers,numscan,index);
			numscan++;
			
			// distancia entre os ciclos de varredura é de 10ms
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (numscan<4){ // são 3 ciclos de varredura na fase Online
				w.startScan();}
			else{
				numscan=0;
				AcquisitionDONE = true;
				
				// Can Disconnect WIFI
		        Settings.System.putInt(getContentResolver(),Settings.System.WIFI_SLEEP_POLICY,Settings.System.WIFI_SLEEP_POLICY_DEFAULT);
				
		        // Finish
				unregisterReceiver(receiver);
			}
			
		}
		
		// Função que cria média das potencias recebidas durante os ciclos de varredura (para cada ponto de acesso)
		private void RSSIMEAN(String[] pAs, int[] powers, int numscan,int index) {
			int next=0;
			int z;
			for(int j=0;j<index;j++){
				for (z=0;z<100;z++){
					
					if (pAs[j].equals(PAsMEAN[z])){
						PowersMEAN[z]*=NumbersMean[z];
						PowersMEAN[z]+=powers[j];
						NumbersMean[z]+=1;
						PowersMEAN[z]/=NumbersMean[z];
												
						break;
					}

					if(PAsMEAN[z]==null){
						next=z;
						z=100;
						break;
					}
					
				}
				//PowersMEAN[]
				if (z==100){
					NumbersMean[z]=1;
					PAsMEAN[next]=pAs[j];
					PowersMEAN[next]=powers[j];
				}
			}
		}

	};
	
	private Handler handler = new Handler(){
	    @Override
	    public void handleMessage(Message msg) {
	        if(msg.what==STEP_ONE_COMPLETE){
	        	ImageView view = (ImageView) findViewById(R.id.imageView);
	        	view.setImageBitmap(createImage(place));
	        }
	    }
	};
	
	//Room
	EditText Room;
	
	// Image Modes: DRAG, MOVE, ZOOM
	int mode = NONE;
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	
	// Translate X and Y
	float translateX=0f;
	float translateY=0f;
	
	//Real Image Coordinates
	private float relativeX=0;
	private float relativeY=0;
	
	// Variables for Zooming
	float scale=1f;
	PointF mid = new PointF();
	float oldDist = 1f;
	
	// Display Variables
	private int displayWidth;
	private int displayHeight;
	private int imageWidth;
	private int imageHeight;
	private int rimageWidth;
	private int rimageHeight;
	
	// Start Points in images
	PointF start = new PointF(0f,0f);
	PointF previous = new PointF(0f,0f);
	
	// Dialogs
	AlertDialog.Builder size_dim;
	
	// Progress Bar Variables

	
	// Debug Variable
	private static final String TAG = "Touch";
    
	// Inicialização da atividade
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		SharedPreferences settings = getSharedPreferences(PREF,0);
		warmed = false; //settings.getBoolean("warmed",false);
		
		DTmg=new DataManager(this);
		
		// Obtendo dados da imagem e tratando ela como uma matriz
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_offline);
        ImageView view = (ImageView) findViewById(R.id.imageView);
        Display d = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        displayWidth=d.getWidth();
        displayHeight=d.getHeight();
        bd = (BitmapDrawable) this.getResources().getDrawable(R.drawable.quarto);
        imageWidth = bd.getBitmap().getWidth();
        imageHeight = bd.getBitmap().getHeight();
        rimageWidth=imageWidth;
        rimageHeight=imageHeight;
        

        view.setOnTouchListener(this);
        
    }

	// Chama Menu da atividade
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_online, menu);
        return true;
    }
    
    
    // Opçoes do menu
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		LayoutInflater settings = LayoutInflater.from(this);
		final View textEntryView = settings.inflate(R.layout.algorithms,null);
		AlertDialog.Builder size_dim = new AlertDialog.Builder(this);
		size_dim.setView(textEntryView);
		size_dim.setMessage("Choose Tracking Algorithm:").setPositiveButton("Ok", dialogClickListener)
	    .setNegativeButton("Cancel", dialogClickListener).show();
	
		
		
		return super.onOptionsItemSelected(item);
	}
	
	// Tratamento da matriz (imagem) e interface com usuário (explicação detalhada no arquivo Offline)
	public boolean onTouch(View v, MotionEvent event) {
		ImageView view = (ImageView) v;
		
		//Handle touch events here
		switch (event.getAction() & MotionEvent.ACTION_MASK){
		case MotionEvent.ACTION_DOWN:
			savedMatrix.set(matrix);
			start.set(event.getX(),event.getY());
			mode = DRAG; 
			break;
		case MotionEvent.ACTION_UP:
			previous.set(previous.x+translateX,previous.y+translateY);
			mode = NONE;
			break;
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			break;
		case MotionEvent.ACTION_MOVE:
			translateX = event.getX() - start.x;
			translateY = event.getY() - start.y;

			if (mode == DRAG){
				matrix.set(savedMatrix);
				matrix.postTranslate(translateX, translateY);			
			}
			else if (mode == ZOOM){
				float newDist = spacing(event);
				if (newDist > 10f){
					matrix.set(savedMatrix);
					scale=newDist/oldDist;
					matrix.postScale(scale, scale,mid.x,mid.y);
				};
				}
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			oldDist = spacing(event);
			if (oldDist > 10f){
				savedMatrix.set(matrix);
				midPoint(mid,event);
				mode = ZOOM;
			}
			break;
			}
		
		// Perform the transformation
		
		view.setImageMatrix(matrix);
		
		return true; //indicate event was handled
	}


	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
		
	}

	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}
	
	// Opçoes do Menu
	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
	    public void onClick(DialogInterface dialog, int which) {

	    	Dialog f = (Dialog) dialog;
	    	RadioGroup radioGroup = (RadioGroup) f.findViewById(R.id.radioGroup);
	    	RadioButton Statistic = (RadioButton) f.findViewById(R.id.radioButton1);
	    	RadioButton KNN = (RadioButton) f.findViewById(R.id.radioButton2);
	    	RadioButton KNN_Statistic = (RadioButton) f.findViewById(R.id.radioButton3);
	    	RadioButton Statistic_KNN = (RadioButton) f.findViewById(R.id.radioButton4);
	    	RadioButton WISARD = (RadioButton) f.findViewById(R.id.radioButton5);
	    	int StatisticID = Statistic.getId();
	    	int KNNID = KNN.getId();
	    	int Statistic_KNNID = Statistic_KNN.getId();
	    	int KNN_StatisticID = KNN_Statistic.getId();
	    	int WISARDID = WISARD.getId();
	    	CheckBox RTime = (CheckBox) f.findViewById(R.id.checkBox1);
	    	
	    	switch (which){
    		case DialogInterface.BUTTON_POSITIVE:
    			// Algorithm Variables
    			int algchoice=0;
    			boolean realtime=RTime.isChecked();
    			
    			int selectedID = radioGroup.getCheckedRadioButtonId();
    			
    			if (selectedID==StatisticID)algchoice=1;
    			if (selectedID==KNNID)algchoice=2;
    			if (selectedID==KNN_StatisticID)algchoice=3;
    			if (selectedID==Statistic_KNNID)algchoice=4;
    			if (selectedID==WISARDID)algchoice=5;
    			
    			RadioButton Algorithm = (RadioButton) f.findViewById(selectedID);
    			Toast.makeText(Online.this, Algorithm.getText(), Toast.LENGTH_SHORT).show();
    			
    			SharedPreferences settings = getSharedPreferences(PREF,0);
    			SharedPreferences.Editor editor = settings.edit();
    			editor.putInt("Algorithm", algchoice);
    			editor.commit();
    			
    		    Thread bridge = new Thread(){
    				public void run(){
    				 bindService(
    				        new Intent(Online.this, Bridge.class),
    				        mConnection,
    				        Context.BIND_AUTO_CREATE
    				    );
    				}
    				};
    			bridge.start();
    			
    			// SelectAlgorithm(algchoice,realtime);
    			break;
    		case DialogInterface.BUTTON_NEGATIVE:
    			break;	
	    		
	    		}
	    	}

		
	    private void SelectAlgorithm(int algchoice, boolean realtime) {
			
			switch(algchoice){
			case 1:
				KSDAlg(realtime); // Algoritmo estatístico
				
				
			}
			
		}

		private void KSDAlg(final boolean realtime) {
			Thread myThread = new Thread() {
			      public void run() {
			    	  while(true){
			    		  try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
			    		  if (!warmed){ // Chama função que cria o KDE
			    			  DTmg.open();
			    			  DTmg.KSDWarming();
			    			  DTmg.close();
			    			  }
				    	  if (numscan==0)acquire(); // Captura os dados do ambiente para comparar com a base de dados
				    	  while(!AcquisitionDONE){
			    			  try {
								Thread.sleep(5000); // Espera a captura terminar
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
			    		  }
				    	  
			    		  place = KSDCalc(); // Chama função que calcula as probabilidades de cada local
			    		  Message msg = Message.obtain();
			              msg.what = STEP_ONE_COMPLETE;
			              handler.sendMessage(msg);
			              
			    		  if (!realtime) break;
			    	  }  
			      }
			};

		myThread.start();
		}
		
		// função que calcula as probabilidade de cada local
		private int[] KSDCalc() {
			DTmg.open();
			int[] place=new int[2];
			Cursor c = DTmg.Local(new String[]{DataManager.Local.ID,DataManager.Local.PX,DataManager.Local.PY}, null); // cursor com os locais da base de dados
			Cursor c2 = null;
			String[] info = new String[]{DataManager.Access_Point.ID};
			  
			float[] probabilities = new float[c.getCount()];
			for (int i=0;i<c.getCount();i++)probabilities[i]=0;
			float maxprob = Float.NEGATIVE_INFINITY;
			c.moveToFirst();
			Cursor CC = DTmg.AP(new String[]{DataManager.Access_Point.NAME}, null);
			for (int l=0;!c.isAfterLast();l++){ // Um loop para todos os locais
				for (int i=0;i<100;i++){
					if (PAsMEAN[i]==null) break;
					c2 = DTmg.AP(info, DataManager.Access_Point.NAME + " == '" + PAsMEAN[i] + "'"); // Verifica se o ponto de acesso capturado na varredura online existe no local em questão
					if(c2.getCount()==0) break;
					
					c2.moveToFirst();
					probabilities[l] += Math.log(DTmg.KSDFunction(c.getLong(0), 
							c2.getLong(0)).prob(PowersMEAN[i])); // Calcula a probabilidade pelo KDE e usa o logaritimo
			    }
				// Maior probabilidade é onde o usuário está
			   if (probabilities[l]>maxprob){
				   place[0]=c.getInt(1);
				   place[1]=c.getInt(2);
				   maxprob=probabilities[l];
			   }
			   c.moveToNext();
			}
			DTmg.close();
			return place;
		}

	};
	
		// Marca com círculo vermelho a localização no mapa
		private Bitmap createImage(int[] place) {
			ImageView view = (ImageView) findViewById(R.id.imageView);
			Bitmap bmp = ((BitmapDrawable)view.getDrawable()).getBitmap();
			Bitmap bmp2 = bmp.copy(bmp.getConfig(),true);
	        Canvas canvas = new Canvas(bmp2);
	        Paint paint = new Paint();
	        paint.setStyle(Paint.Style.STROKE);
	        paint.setColor(Color.RED);
	        canvas.drawCircle(place[0], place[1], 10, paint);
	        return bmp2;
		}

		// Função de captura
		private void acquire() {
			numscan=1;
			Settings.System.putInt(getContentResolver(),Settings.System.WIFI_SLEEP_POLICY,Settings.System.WIFI_SLEEP_POLICY_NEVER);
			IntentFilter i = new IntentFilter();
			i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
			registerReceiver(receiver, i);
			WifiManager w = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			w.startScan();
			
		}
		
		
		// Communication With Service
		private ServiceConnection mConnection = new ServiceConnection() {
			public void onServiceConnected(ComponentName name, IBinder service) {
				 //  We are communicating with the service using a Messenger, so here we get a client-side
	            // representation of that from the raw IBinder object.
	            mService = new Messenger(service);
	            mBound = true;
				
			}
			public void onServiceDisconnected(ComponentName name) {
	            // This is called when the connection with the service has been
	            // unexpectedly disconnected -- that is, its process crashed.
	            mService = null;
	            mBound = false;
				
			}
	    };
		
		@Override
	    protected void onStop() {
	        super.onStop();
	        // Unbind from the service
	        if (mBound) {
	            unbindService(mConnection);
	            mBound = false;
	        }
	    }



}
	

	

	
