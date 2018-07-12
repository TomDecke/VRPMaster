/**
 * Class to represent a possible customer exchange.
 * @author Tom Decke
 *
 */
public class ExchangeOption {

	private Vehicle v1;
	private Vehicle v2;
	private Customer c1;
	private Customer c2;
	private double delta;

	/**
	 * Constructor to create an exchange option
	 * @param v1 Vehicle, the first vehicle of the exchange
	 * @param v2 Vehicle, the second vehicle of the exchange
	 * @param cV1 Customer, the customer located in v1
	 * @param cV2 Customer, the customer located in v2
	 * @param cost double, the change of cost incurred by this move
	 */
	public ExchangeOption(Vehicle v1, Vehicle v2, Customer cV1, Customer cV2, double delta) {
		this.v1 = v1;
		this.v2 = v2;
		this.c1 = cV1;
		this.c2 = cV2;
		this.delta = delta;
	}

	/**
	 * Accessor for the first vehicle of the exchange
	 * @return v1 Vehicle
	 */
	public Vehicle getV1() {
		return v1;
	}

	/**
	 * Accessor for the second vehicle of the exchange
	 * @return v2 Vehicle
	 */
	public Vehicle getV2() {
		return v2;
	}

	/**
	 * Accessor for the customer located in vehicle 1
	 * @return c1, the customer in vehicle 1
	 */
	public Customer getC1() {
		return c1;
	}

	/**
	 * Accessor for the customer located in vehicle 2
	 * @return c2, the customer in vehicle 2
	 */
	public Customer getC2() {
		return c2;
	}

	/**
	 * Accessor for the change in cost brought by this option
	 * @return double, the change
	 */
	public double getDelta() {
		return delta;
	}

	public void printOption() {
		if(c1 == null) {
			System.out.println(String.format("There are no customers to swap"));
		}
		else {
			System.out.println(String.format("Swap customer c%d from vehicle v%d with customer c%d from vehicle v%d",c1.custNo,v1.id,c2.custNo,v2.id));

		}
	}
}
