//TODO can I use the best move straight away?
public class CostAdjustment {
	private Customer cMoved;
	private double costMove;
	
	public CostAdjustment(Customer c, double cost) {
		this.cMoved = c;
		this.costMove = cost;
	}

	public Customer getcMoved() {
		return cMoved;
	}

	public void setcMoved(Customer cMoved) {
		this.cMoved = cMoved;
	}

	public double getCostMove() {
		return costMove;
	}

	public void setCostMove(double costMove) {
		this.costMove = costMove;
	}
	public void set() {
		
	}
}
