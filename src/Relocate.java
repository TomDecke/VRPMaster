
public class Relocate {
	Customer toInsert;
	double cost;
	Vehicle from;
	Vehicle to;
	
	public Relocate(Customer c, double cost, Vehicle v0, Vehicle v1) {
		this.toInsert = c;
		this.cost = cost;
		this.from = v0;
		this.to= v1;
	}
	
	
}
