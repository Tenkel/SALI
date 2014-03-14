package com.sali.autotracking;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ToggleButton;
import com.sali.autotracking.R;

public class AutoColect extends Activity {

	// Shared Variables
	public static final String PREF = "ZoomPref";

	// Room
	NumberPicker Room;
	private ProgressBar LoopBar;

	// Dialogs
	private Chronometer chrono;

	// Wifi Variables
	LoopScanner receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auto_colect);
		LoopBar = (ProgressBar) HostAct.findViewById(R.id.progressBar1);
		receiver = new LoopScanner(this);
		Room = (NumberPicker) findViewById(R.id.numberPicker1);
		Room.setMaxValue(100);
		Room.setMinValue(1);
		Log.d("AC", "before Nrooms");
		receiver.DTmg.open();
		int nrooms=(int) receiver.DTmg.NRooms();
		receiver.DTmg.close();
		if(nrooms<1)
			nrooms=1;
		Log.d("AC", "after Nrooms");
		Room.setValue(nrooms);
		receiver.setroom(nrooms);
		

		chrono = (Chronometer) findViewById(R.id.chronometer1);
		Log.d("AC", "after chrono");

		((ToggleButton) findViewById(R.id.toggleButton1))
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							chrono.setBase(SystemClock.elapsedRealtime());
							chrono.start();
							LoopBar.setVisibility(View.VISIBLE);
							receiver.acquire();
						} else {
							chrono.stop();

							// Stop loop animation.
							LoopBar.setVisibility(View.INVISIBLE);
							receiver.pause();
						}

					}
				});
		Log.d("AC", "after toggle");
		
		Room.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
			
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				receiver.setroom(newVal);
				
			}
		});
		
		
		
		((Button) findViewById(R.id.button1)).setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());

				alert.setTitle("Confirm");
				alert.setMessage("Sure want to delete room "+receiver.getroom()+" ?");

				alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					receiver.DTmg.open();
					receiver.DTmg.deleteroom(receiver.getroom());
					receiver.DTmg.close();
					receiver.setroom(receiver.getroom());
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
		receiver.pause();
		chrono.stop();
		super.onPause();
	}

	@Override
	protected void onResume() {
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

}
