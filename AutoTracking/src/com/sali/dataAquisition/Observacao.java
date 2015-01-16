package com.sali.dataAquisition;


public class Observacao{
	
	private Long id;
	
	private Long idPosicao;
	
	private Integer timestamp;
	
	public Observacao(){
	}
	
	public Observacao(Integer timestamp, Long idPosicao){
		super();
		this.timestamp = timestamp;
		this.idPosicao = idPosicao;
	}
	
	public Long getId() {
		return id;
	}

	public Integer gettimestamp() {
		return timestamp;
	}


	public Long getidPosicao() {
		return idPosicao;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void settimestamp(Integer timestamp) {
		this.timestamp = timestamp;
	}

	public void setidPosicao(Long idPosicao) {
		this.idPosicao = idPosicao;
	}

	@Override
	public String toString() {
		return "Observacao [id=" + id + ", timestamp=" + timestamp + ", idPosicao="
				+ idPosicao + "]";
	}
	
}
