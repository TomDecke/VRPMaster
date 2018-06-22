
public class RelocateOption {
	private Vehicle vehicleFrom;
	private Vehicle vehicleTo;
	private Customer cToMove;
	private double costOfMove;
	
	public RelocateOption(Customer c, double cost, Vehicle vFrom, Vehicle vTo) {
		this.cToMove = c;
		this.costOfMove = cost;
		this.vehicleFrom = vFrom;
		this.vehicleTo = vTo;
	}
	
	public Customer getCToMove() {
		return cToMove;
	}
	
	public double getCostOfMove() {
		return this.costOfMove;
	}
}
