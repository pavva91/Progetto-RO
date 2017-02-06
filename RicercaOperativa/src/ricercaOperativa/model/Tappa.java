package ricercaOperativa.model;

public class Tappa {
	Nodo nodoDaVisitare;
	double arrivalTime;
	double departureTime;

	public Tappa(Nodo nodoDaVisitare, double arrivalTime, double departureTime) {
		this.nodoDaVisitare = nodoDaVisitare;
		this.arrivalTime = arrivalTime;
		this.departureTime = departureTime;
	}
	
	public Nodo getNodoDaVisitare() {
		return nodoDaVisitare;
	}
	public double getArrivalTime() {
		return arrivalTime;
	}
	public void setArrivalTime(double arrivalTime) {
		this.arrivalTime = arrivalTime;
	}
	public double getDepartureTime() {
		return departureTime;
	}
	public void setDepartureTime(double departureTime) {
		this.departureTime = departureTime;
	}
}
