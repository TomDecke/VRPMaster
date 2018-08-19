package moves;
import operators.Operation;
import representation.Customer;
import representation.Vehicle;

/**
 * Class to represent a possible 2-opt move
 * @author Tom Decke
 *
 */
public class TwoOptOption extends Option{

	/**
	 * Constructor for a new possible two-opt-move
	 * @param newStart
	 * @param newEnd
	 * @param v Vehicle, the vehicle to which the operation would be applied
	 * @param delta double, the cost improvement that would be obtained
	 * @param op Operation, the operation to which the 2-opt option belongs
	 */
	public TwoOptOption(Customer newStart, Customer newEnd, Vehicle v, double delta, Operation op) {
		super(newStart,newEnd,delta,v,null,op);
	}

	/**
	 * Accessor for the vehicle in question
	 * @return Vehicle, the vehicle
	 */
	@Override
	public Vehicle getV1() {
		return super.v1;
	}

	/**
	 * Accessor for the vehicle in question
	 * @return Vehicle, the vehicle
	 */
	@Override
	public Vehicle getV2() {
		return super.v1;
	}

	/**
	 * Accessor for the new start after the reversal
	 * @return Customer, the new start
	 */
	@Override
	public Customer getC1() {
		return super.c1;
	}

	/**
	 * Accessor for the new end after the reversal
	 * @return Customer, the new end
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
	 * Print the information of the option to the console
	 */
	@Override
	public void printOption() {
		System.out.println(String.format("Reverse the route between C%s and C%s in V%s. Cost benefit: %.2f", getC2().getCustNo(),getC1().getCustNo(),getV1().getId(),getDelta()));
	}
}
