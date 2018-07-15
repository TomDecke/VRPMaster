public class TwoOptOption extends Option{

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
		// TODO Auto-generated method stub
		return super.v1;
	}

	/**
	 * Accessor for the new start after the reversal
	 * @return Customer, the new start
	 */
	@Override
	public Customer getC1() {
		// TODO Auto-generated method stub
		return super.c1;
	}

	/**
	 * Accessor for the new end after the reversal
	 * @return Customer, the new end
	 */
	@Override
	public Customer getC2() {
		// TODO Auto-generated method stub
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

	@Override
	public void printOption() {
		System.out.println(String.format("Reverse the route between C%s and C%s in V%s. Cost benefit: %.2f", getC2().custNo,getC1().custNo,getV1().id,getDelta()));
	}

}
