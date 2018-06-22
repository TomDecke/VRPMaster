import java.io.IOException;

public class SteepestDescent {
	private final int PENALTY = 10000;
	private VRP vrp;
	private int numCustomers;
	private double[][] costs;
	private Customer[][] bestCustomer;
	
	public SteepestDescent(String textfile, int customers) throws IOException {
		this.vrp = new VRP(textfile,customers);
		this.costs = new double[customers+1][customers+1];
		this.bestCustomer = new Customer[customers+1][customers+1];
		this.numCustomers = customers;
	}
	
	public VRP getVRP() {
		return this.vrp;
	}
	
	public double findCheapestMove() {
		
		double cheapest = PENALTY;
		for(int i = 0; i <= numCustomers; i++ ) {
			for(int j = 0; j <= numCustomers; j++) {
				if(i!=j) {
					double current = costs[i][j];
					if(current < cheapest) {
						cheapest = current;
					}
				}
			}
		}
		
		return cheapest;
	}
	
	public void findMinMove(Vehicle v0, Vehicle v1) {
		Customer bestToMove = null;
		int counter = v0.numCostumer;
		Customer current = v0.firstCustomer.succ;
		while(counter > 0) {
			v1.findBestPosition(current);
		}
		bestCustomer[v0.id][v1.id] = bestToMove;
	}
	
	
	public static void main(String[] args) throws IOException{
		SteepestDescent stDesc = new SteepestDescent(args[0],Integer.parseInt(args[1]));
		
		double currentCost = stDesc.getVRP().calcTotalCost();
		for(;;) {
			stDesc.findCheapestMove();
			
		}
	}
}
