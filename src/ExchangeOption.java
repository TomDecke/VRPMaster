/**
 * Class to represent a possible customer exchange
 * @author Tom Decke
 *
 */
public class ExchangeOption extends Option {

	/**
	 * Constructor to create an exchange option
	 * @param v1 Vehicle, the first vehicle of the exchange
	 * @param v2 Vehicle, the second vehicle of the exchange
	 * @param cV1 Customer, the customer located in v1
	 * @param cV2 Customer, the customer located in v2
	 * @param delta double, the change of cost incurred by this move
 	 * @param op Operation, the operation to which the exchange option belongs
	 */
	public ExchangeOption(Vehicle v1, Vehicle v2, Customer cV1, Customer cV2, double delta, Operation op) {
		super(cV1,cV2,delta,v1,v2,op);
	}

	/**
	 * Accessor for the first vehicle of the exchange
	 * @return v1 Vehicle
	 */
	public Vehicle getV1() {
		return super.v1;
	}

	/**
	 * Accessor for the second vehicle of the exchange
	 * @return v2 Vehicle
	 */
	public Vehicle getV2() {
		return super.v2;
	}

	/**
	 * Accessor for the customer located in vehicle 1
	 * @return c1, the customer in vehicle 1
	 */
	public Customer getC1() {
		return super.c1;
	}

	/**
	 * Accessor for the customer located in vehicle 2
	 * @return c2, the customer in vehicle 2
	 */
	public Customer getC2() {
		return super.c2;
	}

	/**
	 * Accessor for the change in cost brought by this option
	 * @return double, the change
	 */
	public double getDelta() {
		return super.delta;
	}

	/**
	 * Print the information of the option to the console
	 */
	public void printOption() {
		if(c1 == null || c2 == null) {
			System.out.println(String.format("There are no customers to swap"));
		}
		else {
			System.out.println(String.format("Swap customer c%d from vehicle v%d with customer c%d from vehicle v%d. Improvement: %.2f",c1.custNo,v1.id,c2.custNo,v2.id,getDelta()));

		}
	}
}
