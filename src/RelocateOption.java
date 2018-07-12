/**
 * Class to represent a possible customer relocation.
 * @author Tom Decke
 *
 */
public class RelocateOption extends Option{


	/**
	 * Constructor to create a relocation-object
	 * @param c Customer, the customer who is to be moved
	 * @param delta double, the cost-reduction which occurs by the moving the customer
	 * @param vFrom Vehicle, the vehicle from which the customer would be taken
	 * @param vTo Vehicle, the vehicle to which the customer would be moved
	 */
	public RelocateOption(Customer c, double delta, Vehicle vFrom, Vehicle vTo) {
		super(null,null,delta,vFrom,vTo);
		super.cToMove = c;
	}

	/**
	 * Accessor for the customer which would be moved
	 * @return Customer
	 */
	public Customer getCToMove() {
		return super.cToMove;
	}

	/**
	 * Accessor for the cost of the move
	 * @return double
	 */
	public double getDelta() {
		return super.delta;
	}

	/**
	 * Accessor for the vehicle from which the customer would be taken
	 * @return Vehicle
	 */
	public Vehicle getV1() {
		return super.v1;
	}

	/**
	 * Accessor for the vehicle to which the customer would be moved
	 * @return Vehicle
	 */
	public Vehicle getV2() {
		return super.v2;
	}

	
	/**
	 * Accessor for the predecessor of the customer to move
	 * @return Customer, the predecessor
	 */
	public Customer getC1() {
		return super.c1;
	}

	/**
	 * Accessor for the successor of the customer to move
	 * @return Customer, the successor
	 */
	public Customer getC2() {
		return super.c2;
	}

	/**
	 * Mutator for the predecessor of the customer to move
	 * @param cPred Customer, the new predecessor
	 */
	public void setcPred(Customer cPred) {
		super.c1 = cPred;
	}

	/**
	 * Mutator for the successor of the customer to move
	 * @param cSucc Customer, the new successor
	 */
	public void setcSucc(Customer cSucc) {
		super.c2 = cSucc;
	}

	/**
	 * Print the relocate option to the console
	 */
	public void printOption() {
		if(cToMove != null) {
			System.out.println("Move c"+cToMove.custNo+" from v" +v1.id + " to v" + v2.id+ " at cost: "+delta);
		}
		else {
			System.out.println("Move cX from v" +v1.id + " to v" + v2.id+ " at cost: "+delta);	
		}
	}
}
