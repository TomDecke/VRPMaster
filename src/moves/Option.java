package moves;
import operators.Operation;
import representation.Customer;
import representation.Vehicle;

/**
 * Abstract class to represent a move, which could be executed to improve the solution to a VRP
 * @author Tom Decke
 *
 */
public abstract class Option {
	protected Operation operator;
	protected Vehicle v1;
	protected Vehicle v2;
	protected int loadForV1;
	protected int loadForV2;
	protected Customer cToMove;
	protected Customer c1;
	protected Customer c2;
	protected double delta;

	/**
	 * Create an option
	 * @param c1 Customer, the first customer to memorize
	 * @param c2 Customer, the second customer to memorize
	 * @param delta double, the cost improvement of the option
	 * @param v1 Vehicle, the first vehicle to memorize
	 * @param v2 Vehicle, the second vehicle to memorize
	 * @param op Operation, the operation to which the option belongs
	 */
	public Option(Customer c1, Customer c2, double delta, Vehicle v1, Vehicle v2, Operation op) {
		this.operator = op;
		this.c1 = c1;
		this.c2 = c2;
		this.delta = delta;
		this.v1 = v1;
		this.v2 = v2;
	}

	/**
	 * Set the customer that is to be moved in the relocate-operator
	 * @param c Customer, the customer to relocate
	 */
	public void setCToMove(Customer c) {
		this.cToMove = c;
	}

	/**
	 * Accessor for the customer to relocate
	 * @return Customer, the customer to relocate
	 */
	public Customer getCToMove() {
		return this.cToMove;
	}

	/**
	 * Accessor for the operation
	 * @return Operation, the operation
	 */
	public Operation getOperation() {
		return this.operator;
	}

	/**
	 * Accessor for the new load of vehicle 1
	 * @return int, the new load
	 */
	public int getLoadForV1() {
		return loadForV1;
	}

	/**
	 * Accessor for the new load of vehicle 2
	 * @return int, the new load
	 */
	public int getLoadForV2() {
		return loadForV2;
	}

	/**
	 * Accessor for the first involved vehicle
	 * @return Vehicle, the first vehicle
	 */
	public abstract Vehicle getV1();

	/**
	 * Accessor for the second involved vehicle
	 * @return Vehicle, the second vehicle
	 */
	public abstract Vehicle getV2();

	/**
	 * Accessor for the first involved customer
	 * @return Customer, the first customer
	 */
	public abstract Customer getC1();

	/**
	 * Accessor for the second involved customer
	 * @return Customer, the second customer
	 */
	public abstract Customer getC2();

	/**
	 * Accessor for the cost-improvement of the option
	 * @return double, the cost-difference
	 */
	public abstract double getDelta();

	/**
	 * Print the information about the option to the console
	 */
	public abstract void printOption();
}
