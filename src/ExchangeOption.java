/**
 * Class to represent a possible customer exchange.
 * @author Tom Decke
 *
 */
public class ExchangeOption {



	private Vehicle v1;
	private Vehicle v2;
	private Customer cV1;
	private Customer cV2;
	private double delta;
	
	/**
	 * Constructor to create an exchange option
	 * @param v1 Vehicle, the first vehicle of the exchange
	 * @param v2 Vehicle, the second vehicle of the exchange
	 * @param cV1 Customer, the customer located in v1
	 * @param cV2 Customer, the customer located in v2
	 * @param cost double, the change of cost incurred by this move
	 */
	public ExchangeOption(Vehicle v1, Vehicle v2, Customer cV1, Customer cV2, double cost) {
		this.v1 = v1;
		this.v2 = v2;
		this.cV1 = cV1;
		this.cV2 = cV2;
		this.delta = cost;
	}
	
	public Vehicle getV1() {
		return v1;
	}

	public Vehicle getV2() {
		return v2;
	}

	public Customer getcV1() {
		return cV1;
	}

	public Customer getcV2() {
		return cV2;
	}

	public double getDelta() {
		return delta;
	}
}
