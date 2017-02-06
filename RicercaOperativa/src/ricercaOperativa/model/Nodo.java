package ricercaOperativa.model;

public class Nodo {
	private String id;
	private String type;
	private double x;
	private double y;
	private double demand;
	private double readyTime;
	private double dueDate;
	private double serviceTime;

	public Nodo(String id, String type, double x, double y, double demand, double readyTime, double dueDate, double serviceTime) {
		super();
		this.id = id;
		this.type = type;
		this.x = x;
		this.y = y;
		this.demand = demand;
		this.readyTime = readyTime;
		this.dueDate = dueDate;
		this.serviceTime = serviceTime;
		
	}

	public String getId() {
		return id;
	}
	public String getType() {
		return type;
	}
	public double getX() {
		return x;
	}
	public double getY() {
		return y;
	}
	public double getDemand() {
		return demand;
	}
	public double getReadyTime() {
		return readyTime;
	}
	public double getDueDate() {
		return dueDate;
	}
	public double getServiceTime() {
		return serviceTime;
	}
	
}
