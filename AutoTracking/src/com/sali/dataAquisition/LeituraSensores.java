package com.sali.dataAquisition;

import android.content.Context;

public class LeituraSensores extends DataManager {

	public void insert_acc(float[] acc, int idObservacao, int idUnidadeSensores){
	};
	
	public void insert_temp(float temp, int idObservacao, int idUnidadeSensores){
	};
	
	public void insert_grav(float[] grav, int idObservacao, int idUnidadeSensores){
	};
	
	public void insert_gyro(float[] gyro, int idObservacao, int idUnidadeSensores){
	};
	
	public void insert_light(float light, int idObservacao, int idUnidadeSensores){
	};
	
	public void insert_linAcc(float[] linAcc, int idObservacao, int idUnidadeSensores){
	};
	
	public void insert_mag(float[] mag, int idObservacao, int idUnidadeSensores){
	};

	public void insert_orient(float[] orient, int idObservacao, int idUnidadeSensores){
	};
	
	public void insert_press(float press, int idObservacao, int idUnidadeSensores){
	};
	
	public void insert_prox(float prox, int idObservacao, int idUnidadeSensores){
	};
	
	public void insert_hum(float hum, int idObservacao, int idUnidadeSensores){
	};
	
	public void insert_rot(float[] rot, int idObservacao, int idUnidadeSensores){
	};
	
	public void insert_rotScalar(float rotScalar, int idObservacao, int idUnidadeSensores){
	};
	
	public void insert_allsensors(float[] acc, float temp,float[] grav,float[] gyro,float light,
			float[] linAcc,float[] mag,float[] orient,float press,float prox,float hum,
			float[] rot,float rotScalar){
		
	};
	
	public LeituraSensores(Context c) {
		super(c);
		// TODO Auto-generated constructor stub
	}

}
