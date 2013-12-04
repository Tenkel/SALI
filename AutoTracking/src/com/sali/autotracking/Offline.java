package com.sali.autotracking;

import java.util.List;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.FloatMath;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import com.sali.autotracking.R;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class Offline extends Activity implements OnTouchListener {

	// Matrices to move and zoom image
	ImageView view;
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();
	BitmapDrawable bd;
	
	// DB
	private DataManager DTmg;
	
	// Shared Variables
	SharedPreferences settings;
	public static final String PREF = "ZoomPref";
	
	// Wifi Variables
	private int numscan=0;
	WifiManager wifi;
	BroadcastReceiver receiver = new BroadcastReceiver() {
		@SuppressWarnings("deprecation")
		@Override
		public void onReceive(Context c, Intent i) {
			WifiManager w = (WifiManager) c
					.getSystemService(Context.WIFI_SERVICE);
			w.getScanResults();

			
			DTmg.open();
			List<ScanResult> results = w.getScanResults();
			for (ScanResult result : results) {
				DTmg.insert(result.level,result.BSSID,(int) relativeX,(int) relativeY, nroom, 0, 0, 0, 0);
			}
			WifiInfo actual_connection = w.getConnectionInfo ();
			if (actual_connection.getNetworkId()!=-1){
			DTmg.insert(actual_connection.getRssi(), actual_connection.getBSSID(),(int) relativeX,(int) relativeY, nroom, 0, 0, 0, 0);
		}
			DTmg.close();

			numscan+=1;
			ProgressBar pb = (ProgressBar) findViewById(R.id.progress_bar);
			pb.setProgress(numscan);
			//
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (numscan<20){
				w.startScan();}
			else{
				// Block UI
				BUSY=false;
				
				// Shared that DB has new entry
				SharedPreferences settings = getSharedPreferences(PREF,0);
    			SharedPreferences.Editor editor = settings.edit();
    			editor.putBoolean("warmed", false);
    			editor.commit();
				
				// Destroy ProgressBar
				linProgressBar = (LinearLayout) findViewById(R.id.lin_progress_bar);
		        linProgressBar.setVisibility(View.INVISIBLE);
		        pb.setProgress(0);
		        
		        // UI can sleep again
		        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		        
		        // Can Disconnect WIFI
				if (android.os.Build.VERSION.SDK_INT >= 17) {
					Settings.Global.putInt(getContentResolver(),
							Settings.Global.WIFI_SLEEP_POLICY,
							Settings.Global.WIFI_SLEEP_POLICY_DEFAULT);
				} else {
					Settings.System.putInt(getContentResolver(),
							Settings.System.WIFI_SLEEP_POLICY,
							Settings.System.WIFI_SLEEP_POLICY_DEFAULT);
				}
		        
		        // Finish
		        unregisterReceiver(receiver);
				
			}
			
		}
	};
	
	//Room
	EditText Room;
	int nroom=1;
	
	// Image Modes: DRAG, MOVE, ZOOM
	int mode = NONE;
	private boolean BUSY=false;
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
	double tdist=0;
	
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
	AlertDialog.Builder builder;
	private boolean b_dialog;
	
	// Progress Bar Variables
	private LinearLayout linProgressBar;

	
	
	private static final String TAG = "Touch";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SharedPreferences settings = getSharedPreferences(PREF,0);
		float iWidth = settings.getFloat("iWidth", 400);	
		float iHeight = settings.getFloat("iHeight", 400);
		
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("warmed", true);
		editor.commit();
        
        DTmg = new DataManager(this);
		
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

        matrix.postScale(iWidth/imageWidth, iHeight/imageHeight);
		imageHeight=(int) iHeight;imageWidth=(int) iWidth;
        
        view.setOnTouchListener(this);
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_offline, menu);
        return true;
    }
    
    
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		LayoutInflater settings = LayoutInflater.from(this);
		final View textEntryView = settings.inflate(R.layout.dimensions,null);
		b_dialog=false;
		AlertDialog.Builder size_dim = new AlertDialog.Builder(this);
		size_dim.setView(textEntryView);
		size_dim.setMessage("Dimensions").setPositiveButton("Ok", dialogClickListener)
	    .setNegativeButton("Cancel", dialogClickListener).show();
	
		
		
		return super.onOptionsItemSelected(item);
	}

	public boolean onTouch(View v, MotionEvent event) {
		ImageView view = (ImageView) v;
		
		//Handle touch events here
		if (!BUSY){
		switch (event.getAction() & MotionEvent.ACTION_MASK){
		case MotionEvent.ACTION_DOWN:
			savedMatrix.set(matrix);
			start.set(event.getX(),event.getY());
			mode = DRAG; 
			break;
		case MotionEvent.ACTION_UP:
			if (mode == DRAG & tdist<5){
			float[] values = new float[9];
			matrix.getValues(values);
			// values[2] and values[5] are the x,y coordinates of the top left corner of the drawable image, regardless of the zoom factor.
			// values[0] and values[4] are the zoom factors for the image's width and height respectively. If you zoom at the same factor, these should both be the same value.
			relativeX = (event.getX() - values[2]) / values[0];
			relativeY = (event.getY() - values[5]) / values[4];
			if (relativeX>=0 & relativeY>=0 & relativeX<=rimageWidth & relativeY<=rimageHeight){
				LayoutInflater factory = LayoutInflater.from(this);
				final View textEntryView = factory.inflate(R.layout.room,null);
				b_dialog=true;
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setView(textEntryView);
				builder.setMessage("Get AP powers?").setPositiveButton("Yes", dialogClickListener)
			    .setNegativeButton("No", dialogClickListener).show();
			
			}}
			previous.set(previous.x+translateX,previous.y+translateY);
			mode = NONE;
			tdist=0;
			break;
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			break;
		case MotionEvent.ACTION_MOVE:
			translateX = event.getX() - start.x;
			translateY = event.getY() - start.y;
			tdist+=Math.sqrt(Math.pow(translateX,2)+Math.pow(translateY,2));
			float reltransX = previous.x+translateX;
			float reltransY = previous.y+translateY;
			if (mode == DRAG){
				matrix.set(savedMatrix);
				//if (reltransX>0) translateX=-previous.x;
				//if (reltransX<(-imageWidth+displayWidth)) translateX=-previous.x-imageWidth+displayWidth;
				//else if ((translateY*-1)>(scale-1)*displayWidth) translateX=(1-scale)*displayWidth;
				//if(reltransY>0) translateY=-previous.y;
				//if (reltransY<(-imageHeight+displayHeight)) translateY=-previous.y-imageHeight+displayHeight;
				//else if ((translateY*-1)>(scale-1)*displayHeight) translateY=(1-scale)*displayHeight;
				//translateX=translateX/scale;
				//translateY=translateY/scale;
				matrix.postTranslate(translateX, translateY);			
			}
			else if (mode == ZOOM){
				float newDist = spacing(event);
				if (newDist > 10f){
					matrix.set(savedMatrix);
					scale=newDist/oldDist;
					//imageHeight*=scale;imageWidth*=scale;
					//previous.x*=scale;previous.y*=scale;
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
		}
		return true; //indicate event was handled
	}

	@SuppressWarnings("deprecation")
	private void acquire() {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		if (android.os.Build.VERSION.SDK_INT >= 17) {
			Settings.Global.putInt(getContentResolver(),
					Settings.Global.WIFI_SLEEP_POLICY,
					Settings.Global.WIFI_SLEEP_POLICY_NEVER);
		} else {
			Settings.System.putInt(getContentResolver(),
					Settings.System.WIFI_SLEEP_POLICY,
					Settings.System.WIFI_SLEEP_POLICY_NEVER);
		}
		
		IntentFilter i = new IntentFilter();
		i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		registerReceiver(receiver, i);
		WifiManager w = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		numscan=0;
		linProgressBar = (LinearLayout) findViewById(R.id.lin_progress_bar);
        linProgressBar.setVisibility(View.VISIBLE);
		w.startScan();
		//unregisterReceiver(receiver);	
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

	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
	    public void onClick(DialogInterface dialog, int which) {

	    	Dialog f = (Dialog) dialog;
	    	if(b_dialog){
	        switch (which){
	        case DialogInterface.BUTTON_POSITIVE:
	        	BUSY=true;
	        	Room = (EditText) findViewById(R.id.editText1);
		    	nroom = Integer.parseInt(Room.getText().toString());
	        	Room.setText(String.valueOf(nroom));
	        	acquire();
	            break;
	        case DialogInterface.BUTTON_NEGATIVE:
	            //No button clicked
	            break;
	        }
	    	}
	    	else{
	    	switch (which){
    		case DialogInterface.BUTTON_POSITIVE:
    			matrix.set(savedMatrix);
    			EditText Width = (EditText) f.findViewById(R.id.editText1);
    			EditText Height = (EditText) f.findViewById(R.id.editText2);
    			float iWidth = Integer.parseInt(Width.getText().toString());
    			float iHeight = Integer.parseInt(Height.getText().toString());
    			SharedPreferences settings = getSharedPreferences(PREF,0);
    			SharedPreferences.Editor editor = settings.edit();
    			editor.putFloat("iWidth", iWidth);
    			editor.putFloat("iHeight", iHeight);
    			editor.commit();
    			matrix.postScale(iWidth/imageWidth, iHeight/imageHeight);
    			imageHeight=(int) iHeight;imageWidth=(int) iWidth;
    			//view.setImageMatrix(matrix);
    			break;
    		case DialogInterface.BUTTON_NEGATIVE:
    			break;
	    			
	    		
	    		}
	    	}}
	};

		private Bitmap createImage() {
			ImageView view = (ImageView) findViewById(R.id.imageView);
			Bitmap bmp = ((BitmapDrawable)view.getDrawable()).getBitmap();
			Bitmap bmp2 = bmp.copy(bmp.getConfig(),true);
	        Canvas canvas = new Canvas(bmp2);
	        Paint paint = new Paint();
	        paint.setStyle(Paint.Style.STROKE);
	        paint.setColor(Color.GREEN);
	        canvas.drawCircle(relativeX, relativeY, 10, paint);
	        return bmp2;
		}
		
		

}
	

	

	
