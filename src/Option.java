
public abstract class Option {
	protected Operation operator;
	protected Vehicle v1;
	protected Vehicle v2;
	protected Customer cToMove;
	protected Customer c1;
	protected Customer c2;
	protected double delta;
	
	/**
	 * Create an option
	 * @param c
	 * @param delta
	 * @param v1
	 * @param v2
	 */
	public Option(Customer c1, Customer c2, double delta, Vehicle v1, Vehicle v2, Operation op) {
		this.operator = op;
		this.c1 = c1;
		this.c2 = c2;
		this.delta = delta;
		this.v1 = v1;
		this.v2 = v2;
	}
	
	public void setCToMove(Customer c) {
		this.cToMove = c;
	}
	
	public Customer getCToMove() {
		return this.cToMove;
	}
	
	public Operation getOperation() {
		return this.operator;
	}
	
	public abstract Vehicle getV1();
	public abstract Vehicle getV2();
	public abstract Customer getC1();
	public abstract Customer getC2();
	public abstract double getDelta();
	public abstract void printOption();
}
