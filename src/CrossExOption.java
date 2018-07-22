/**
 * Class to represent a possible cross exchange between vehicles
 * @author Tom Decke
 *
 */
public class CrossExOption extends Option{

	/**
	 * Constructor to create an exchange option
	 * @param v1 Vehicle, the first vehicle of the exchange
	 * @param v2 Vehicle, the second vehicle of the exchange
	 * @param cV1 Customer, the customer in v1 after which the route is exchanged
	 * @param cV2 Customer, the customer in v2 after which the route is exchanged
	 * @param int, the load of v1 once the exchange is executed
	 * @param int, the load of v2 once the exchange is executed
	 * @param delta double, the change of cost incurred by this move
	 * @param op Operation, the operation to which the cross exchange option belongs
	 */
	public CrossExOption(Vehicle v1, Vehicle v2, Customer cV1, Customer cV2, int loadForV1, int loadForV2, double delta, Operation op) {
		super(cV1,cV2,delta,v1,v2,op);
		super.loadForV1 = loadForV1;
		super.loadForV2 = loadForV2;

	}

	/**
	 * Accessor for the first vehicle
	 * @return Vehicle, the vehicle
	 */
	@Override
	public Vehicle getV1() {
		return super.v1;
	}

	/**
	 * Accessor for the seconf vehicle
	 * @return Vehicle, the vehicle
	 */
	@Override
	public Vehicle getV2() {
		return super.v2;
	}

	/**
	 * Accessor for the first customer
	 * @return Customer, the customer
	 */
	@Override
	public Customer getC1() {
		return super.c1;
	}

	/**
	 * Accessor for the second customer
	 * @return Customer, the customer
	 */
	@Override
	public Customer getC2() {
		return super.c2;
	}

	/**
	 * Accessor for the cost difference
	 * @return double, the difference
	 */
	@Override
	public double getDelta() {
		return super.delta;
	}

	/**
	 * Print the option to the console
	 */
	@Override
	public void printOption() {
		if(c1 == null) {
			System.out.println(String.format("There are no customers to swap"));
		}
		else {
			System.out.println(String.format("Exchange after c%d from vehicle %d with part after c%d from vehicle %d. Improvement: %.2f",c1.custNo,v1.id,c2.custNo,v2.id,delta));

		}
	}	
}
