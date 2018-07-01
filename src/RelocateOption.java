/**
 * Class to represent a possible customer relocation.
 * @author Tom Decke
 *
 */
public class RelocateOption {
	private Vehicle vehicleFrom;
	private Vehicle vehicleTo;
	private Customer cToMove;
	private double costOfMove;

	/**
	 * Constructor to create a relocation-object
	 * @param c Customer, the customer who is to be moved
	 * @param cost double, the cost-reduction which occurs by the moving the customer
	 * @param vFrom Vehicle, the vehicle from which the customer would be taken
	 * @param vTo Vehicle, the vehicle to which the customer would be moved
	 */
	public RelocateOption(Customer c, double cost, Vehicle vFrom, Vehicle vTo) {
		this.cToMove = c;
		this.costOfMove = cost;
		this.vehicleFrom = vFrom;
		this.vehicleTo = vTo;
	}

	/**
	 * Accessor for the customer which would be moved
	 * @return Customer
	 */
	public Customer getCToMove() {
		return cToMove;
	}

	/**
	 * Accessor for the cost of the move
	 * @return double
	 */
	public double getCostOfMove() {
		return this.costOfMove;
	}

	/**
	 * Accessor for the vehicle from which the customer would be taken
	 * @return Vehicle
	 */
	public Vehicle getVehicleFrom() {
		return vehicleFrom;
	}

	/**
	 * Accessor for the vehicle to which the customer would be moved
	 * @return Vehicle
	 */
	public Vehicle getVehicleTo() {
		return vehicleTo;
	}

	/**
	 * Mutator for the cost of the move
	 * @param cost double, the new cost 
	 */
	public void setCostOfMove(double cost) {
		this.costOfMove = cost;
	}

	/**
	 * Print the relocate option to the console
	 */
	public void printOption() {
		if(cToMove != null) {
			System.out.println("Move c"+cToMove.custNo+" from v" +vehicleFrom.id + " to v" + vehicleTo.id+ " at cost: "+costOfMove);
		}
		else {
			System.out.println("Move cX from v" +vehicleFrom.id + " to v" + vehicleTo.id+ " at cost: "+costOfMove);	
		}
	}
}
