package com.sali.autotracking;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.sali.algorithms.DataManager;
import com.sali.autotracking.R;

public class MainActivity extends Activity {
	
	private DataManager DTmg;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DTmg = new DataManager(this); // Inicializa objeto Datamanager (Base de dados é acessado nesta atividade)
        setContentView(R.layout.activity_main); // Layout da atividade
        Button offline_mode = (Button) findViewById(R.id.button1); // Botao que chama o modo Offline
        Button online_mode = (Button) findViewById(R.id.button2); // Botao que chama o modo Online
        Button Export_BD = (Button) findViewById(R.id.button3); // Botao que Exporta o BD
        Button Auto = (Button) findViewById(R.id.button5); // Botao que chama o modo Auto
        
        // Funcao do botao offline
        offline_mode.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View view) {
				Intent offIntent = new Intent(view.getContext(),Offline.class);
				startActivityForResult(offIntent,0); // Ao clicar o botao, a atividade é iniciada
				
			}
		});
        
        // Funcao do botao online
        online_mode.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View view) {
				Intent onIntent = new Intent(view.getContext(),Online.class);
				startActivityForResult(onIntent,0); // Ao clicar o botao, a atividade é iniciada
				
			}
		});
        
        // Funcao do botao Export
        Export_BD.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View view) {
				DTmg.open();
				String state = Environment.getExternalStorageState();
				if (Environment.MEDIA_MOUNTED.equals(state)) {
				try {
					DTmg.DBexport( getExternalFilesDir(null).getAbsolutePath());
				} catch (IOException e) {
					e.printStackTrace();
				}
				}
				else{
				Toast.makeText( getApplicationContext(), "Media cannot be written.", Toast.LENGTH_SHORT).show();
				}
				DTmg.close();
			}
		});
        
        
        // Funcao do botao Import
        ((Button) findViewById(R.id.Button01)).setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				DTmg.open();
				String state = Environment.getExternalStorageState();
				if (Environment.MEDIA_MOUNTED.equals(state)) {
				try {
					DTmg.DBimport( getExternalFilesDir(null).getAbsolutePath());
				} catch (IOException e) {
					e.printStackTrace();
				}
				}
				else{
				Toast.makeText( getApplicationContext(), "Media cannot be written.", Toast.LENGTH_SHORT).show();
				}
				DTmg.close();
			}
		});
        
        // Funcao do botao Auto       
        Auto.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				 if (android.os.Build.VERSION.SDK_INT >= 11){ 	// Verifica versao do android 
				Intent offIntent = new Intent(v.getContext(),AutoColect.class);
				startActivityForResult(offIntent,0); // Ao clicar o botao, a atividade é iniciada
				 }
				 else{
					 Toast.makeText(getApplicationContext(), "Newer Android Version Required", Toast.LENGTH_LONG).show();
					 
				 }
				
			}
		});
        
    }

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
