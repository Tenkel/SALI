package com.sali.dataAquisition;

public class Posicao{

	private Long id;

	private Long idAndar;
	
	private Integer x;
	
	private Integer y;
	
	public Posicao() {

	}

	public Posicao(Long idAndar, Integer x, Integer y) {
		super();
		this.idAndar = idAndar;
		this.x = x;
		this.y = y;
	}

	public Long getId() {
		return id;
	}
	
	public Integer getX() {
		return x;
	}

	public Integer getY() {
		return y;
	}

	public Long getidAndar() {
		return idAndar;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setX(Integer x) {
		this.x = x;
	}

	public void setY(Integer y) {
		this.y = y;
	}

	public void setidAndar(Long idAndar) {
		this.idAndar = idAndar;
	}
	
}
