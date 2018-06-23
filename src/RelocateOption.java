
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

	public Vehicle getVehicleFrom() {
		return vehicleFrom;
	}

	public Vehicle getVehicleTo() {
		return vehicleTo;
	}

	public void setCostOfMove(double cost) {
		this.costOfMove = cost;
	}

	public void printOption() {
		if(cToMove != null) {
			System.out.println("Move c"+cToMove.custNo+" from v" +vehicleFrom.id + " to v" + vehicleTo.id+ " at cost:"+costOfMove);
		}
		else {
			System.out.println("Move cX from v" +vehicleFrom.id + " to v" + vehicleTo.id+ " at cost:"+costOfMove);	
		}
	}
}
