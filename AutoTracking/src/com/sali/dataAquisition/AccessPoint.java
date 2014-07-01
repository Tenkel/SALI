package com.sali.dataAquisition;


public class AccessPoint{

	private Long id;

	private Long idPosicao;
	
	private String bssid;
	
	private String essid;
	
	private float confianca;
	
	public AccessPoint() {

	}

	public AccessPoint(Long idPosicao, String bssid, String essid, float confianca) {
		super();
		this.idPosicao = idPosicao;
		this.bssid = bssid;
		this.essid = essid;
		this.confianca = confianca;
	}

	public Long getId() {
		return id;
	}
	
	public String getbssid() {
		return bssid;
	}

	public String getessid() {
		return essid;
	}

	public Long getidPosicao() {
		return idPosicao;
	}
	
	public float getconfianca() {
		return confianca;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setbssid(String bssid) {
		this.bssid = bssid;
	}

	public void setessid(String essid) {
		this.essid = essid;
	}

	public void setidPosicao(Long idPosicao) {
		this.idPosicao = idPosicao;
	}
	
	public void setconfianca(float confianca){
		this.confianca = confianca;
	}
	
}
