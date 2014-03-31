package com.sali.autotracking;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.sali.dataAquisition.DataManager;
import com.sali.dataAquisition.LoopScanner;
import com.sali.dataAquisition.Scans;

public class AutoColect extends Activity implements Scans {

	// Shared Variables
	public static final String PREF = "ZoomPref";

	// Number of scan rounds
	private int numscan;
	
	private int nroom;
	private float mean;
	
	// Room
	NumberPicker Room;
	private ProgressBar LoopBar;

	// Dialogs
	private Chronometer chrono;

	// WiFi Variables
	LoopScanner receiver;

	private DataManager DTmg;

	// Shared Variables
	SharedPreferences settings;
	SharedPreferences.Editor editor;
	private static final String SAVESCAN = "numscan";
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auto_colect);
		LoopBar = (ProgressBar) findViewById(R.id.progressBar1);
		
		receiver = new LoopScanner(this, this);
		settings = getSharedPreferences(Offline.PREF, 0);
		nroom = 1;
		DTmg = new DataManager(this);
		
		Room = (NumberPicker) findViewById(R.id.numberPicker1);
		Room.setMaxValue(100);
		Room.setMinValue(1);
		Log.d("AC", "before Nrooms");
		DTmg.open();
		int nrooms=(int) DTmg.NRooms();
		DTmg.close();
		if(nrooms<1)
			nrooms=1;
		Log.d("AC", "after Nrooms");
		Room.setValue(nrooms);
		setroom(nrooms);
		

		chrono = (Chronometer) findViewById(R.id.chronometer1);
		Log.d("AC", "after chrono");

		((ToggleButton) findViewById(R.id.toggleButton1))
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							chrono.setBase(SystemClock.elapsedRealtime());

							//load number of scan rounds.
							nroom = settings.getInt(SAVESCAN, 1);
							chrono.start();

							// UI can't sleep
							getWindow().addFlags(
									WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
							
							LoopBar.setVisibility(View.VISIBLE);
							

							numscan = 0;
							((TextView) findViewById(R.id.textView2)).setText(String
									.valueOf(numscan));
							
							receiver.acquire();
						} else {
							chrono.stop();

							// UI can sleep again
							getWindow().clearFlags(
									WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
							
							// Stop loop animation.
							LoopBar.setVisibility(View.INVISIBLE);
							receiver.pause();
						}

					}
				});
		Log.d("AC", "after toggle");
		
		Room.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
			
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				setroom(newVal);
				
			}
		});
		
		
		
		((Button) findViewById(R.id.button1)).setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());

				alert.setTitle("Confirm");
				alert.setMessage("Sure want to delete room "+getroom()+" ?");

				alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					DTmg.open();
					DTmg.deleteroom(getroom());
					DTmg.close();
					setroom(getroom());
				  }
				});

				alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
				  public void onClick(DialogInterface dialog, int whichButton) {
				    // Canceled.
				  }
				});

				alert.show();
				
			}
		});

		
		
	}

	@Override
	protected void onPause() {
		// Save number of scan rounds.
		editor = settings.edit();
		editor.putInt(SAVESCAN, nroom);
		editor.commit();
		
		receiver.pause();
		DTmg.close();
		
		chrono.stop();
		super.onPause();
	}

	@Override
	protected void onResume() {

		//load number of scan rounds.
		nroom = settings.getInt(SAVESCAN, 1);
		receiver.start();
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.auto_colect, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("New Value");
		alert.setMessage("Room Max");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		alert.setView(input);
		input.setInputType(InputType.TYPE_CLASS_NUMBER);
		input.setText(String.valueOf(Room.getMaxValue()));

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		  int value = Integer.parseInt(input.getText().toString());

			Room.setMaxValue(value);
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});

		alert.show();
		
		return super.onOptionsItemSelected(item);
	}

	public int getroom() {
		return nroom;
	}	
	
	/*
	 * Set actual room.
	 */
	public void setroom(int value) {
		nroom = value;
		DTmg.open();
		mean = DTmg.NSamplesmean(nroom);
		DTmg.close();
		((TextView) findViewById(R.id.textView1)).setText(String
				.valueOf(mean));
	}

	public void processScans(List<ScanResult> results, float gyrox,
			float gyroy, float gyroz) {
		DTmg.open();
		for (ScanResult result : results) {
			DTmg.insert(result.level, result.BSSID, nroom, nroom, nroom, gyrox,
					gyroy, gyroz);
		}

		// Shared that DB has new entry
		editor = settings.edit();
		editor.putBoolean("warmed", false);
		editor.commit();

		mean = DTmg.NSamplesmean(nroom);

		DTmg.close();
		
		numscan += 1;

		((TextView) findViewById(R.id.textView2)).setText(String
				.valueOf(numscan));
		((TextView) findViewById(R.id.textView1)).setText(String
				.valueOf(mean));

		
	}

	public void calibrateSensor() {
		// TODO Auto-generated method stub
		
	}

}
