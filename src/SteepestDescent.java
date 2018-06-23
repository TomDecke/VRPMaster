import java.io.IOException;

public class SteepestDescent {
	private final int PENALTY = 10000;
	private VRP vrp;
	private int numCustomers;
	private Relocate[][] bestMoveMatrix;
	
	public SteepestDescent(String textfile, int customers) throws IOException {
		this.vrp = new VRP(textfile,customers);
		this.bestMoveMatrix = new Relocate[customers][customers];
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
					double current = bestMoveMatrix[i][j].cost;
					if(current < cheapest) {
						cheapest = current;
					}
				}
			}
		}
		
		return cheapest;
	}
	
	public void findMinMove(Vehicle v0, Vehicle v1) {

		Relocate bestToMove = new Relocate(null, PENALTY, v0, v1);

		Customer current = v0.firstCustomer.succ;
		while(!current.equals(v0.lastCustomer)) {
			
			//find the best place to insert the customer in the other tour
			Customer insertAfter = v1.findBestPosition(current);
			
			//determine how the total distance of v0 would change
			double newDistV0 = v0.calculateDistance() + vrp.distance(current.pred, current.succ) 
			- vrp.distance(current.pred, current)
			- vrp.distance(current, current.succ);
			
			//determine how the total distance of v1 would change
			double newDistV1 = v1.calculateDistance() + vrp.distance(insertAfter, current)
			+ vrp.distance(current, insertAfter.succ) - vrp.distance(insertAfter, insertAfter.succ);
					
			//the change in cost, if this move was to be made
			double resultingCost = newDistV0 * v0.costOfUse + newDistV1 * v1.costOfUse;
			
			//if this move is cheaper, take it up
			if(resultingCost < bestToMove.cost) {
				bestToMove = new Relocate(current,resultingCost,v0,v1);
			}
			
			//go to the next customer
			current = current.succ;
		
		}
		bestMoveMatrix[v0.id-1][v1.id-1] = bestToMove;
	}
	
	
	public static void main(String[] args) throws IOException{
		SteepestDescent stDesc = new SteepestDescent(args[0],Integer.parseInt(args[1]));

	}
}
