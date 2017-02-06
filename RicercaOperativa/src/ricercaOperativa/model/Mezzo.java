package ricercaOperativa.model;

import java.util.ArrayList;

public class Mezzo {
	private double livelloCarburante;
	private double livelloCarico;
	private double kmInEccedenza = 0;
	private double kmPercorsi;
	private boolean timeException;
	private boolean loadException;
	private ArrayList<Tappa> tappe = new ArrayList<Tappa>();
	
	public Mezzo(double livelloCarburante, double livelloCarico, Tappa tappa) {
		this.livelloCarburante = livelloCarburante;
		this.livelloCarico = livelloCarico;
		tappe.add(tappa);
	}
	
	public double getKmPercorsi() {
		return kmPercorsi;
	}

	public void setKmPercorsi(double kmPercorsi) {
		this.kmPercorsi = kmPercorsi;
	}

	public double getKmInEccedenza() {
		return kmInEccedenza;
	}

	public void setKmInEccedenza(double kmInEccedenza) {
		this.kmInEccedenza = kmInEccedenza;
	}
	
	public double getLivelloCarburante() {
		return livelloCarburante;
	}

	public void setLivelloCarburante(double livelloCarburante) {
		this.livelloCarburante = livelloCarburante;
	}

	public double getLivelloCarico() {
		return livelloCarico;
	}

	public void setLivelloCarico(double livelloCarico) {
		this.livelloCarico = livelloCarico;
	}

	public ArrayList<Tappa> getTappe() {
		return tappe;
	}
	
	public void setMezzo (Mezzo mezzo){
		livelloCarburante = mezzo.getLivelloCarburante();
		livelloCarico = mezzo.getLivelloCarico();
		kmInEccedenza = mezzo.getKmInEccedenza();
		kmPercorsi = mezzo.getKmPercorsi();
		tappe.clear();
		tappe = mezzo.getTappe();
	}
	
}
