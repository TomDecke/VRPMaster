import java.io.IOException;

public class SteepestDescent {
	private final int PENALTY = 10000;
	private VRP vrp;
	private int numCustomers;
	private RelocateOption[][] bestMoveMatrix;
	
	public SteepestDescent(String textfile, int customers) throws IOException {
		this.vrp = new VRP(textfile,customers);
		this.bestMoveMatrix = new RelocateOption[customers][customers];
		this.numCustomers = customers;
	}
	
	
	/**
	 * Create the matrix containing the best moves from one vehicle to another
	 */
	public void createBMM() {
		for(int i = 0; i < numCustomers; i++) {
			for(int j = 0; j < numCustomers; j++) {
				bestMoveMatrix[i][j] = findMinCustomer(vrp.vehicle[i], vrp.vehicle[j]);
			}
		}
	}
	
	/**
	 * Find customer who's moving to another vehicle would have the highest benefit
	 * @param vFrom Vehicle, vehicle from which a customer is to be taken
	 * @param vTo Vehicle, vehicle to which a customer is to be moved
	 * @return ReocationOption, the best option for moving a customer from vFrom to vTo
	 */
	public RelocateOption findMinCustomer(Vehicle vFrom, Vehicle vTo) {
		RelocateOption bestToMove = null;
		int counter = vFrom.numCostumer;
		Customer current = vFrom.firstCustomer.succ;
		while(counter > 0) {
			
			vTo.findBestPosition(current);
			//calc cost
			//if cost is better than so far, update 
			current = current.succ;
			counter--;
		}
		//TODO decide on an indexing policy
		return bestToMove;
		
	}
	
	/**
	 * Find the best move in the Matrix of possible moves
	 * @return RelocateOption, the currently best move in the BMM
	 */
	public RelocateOption findCheapestMove() {
		
		RelocateOption cheapestMove = null;
		double cheapest = PENALTY;
		for(int i = 0; i < numCustomers; i++ ) {
			for(int j = 0; j < numCustomers; j++) {
				if(i!=j) {
					double current = bestMoveMatrix[i][j].getCostOfMove();
					if(current < cheapest) {
						cheapest = current;
						cheapestMove = bestMoveMatrix[i][j];
					}
				}
			}
		}
		
		return cheapestMove;
	}
	

	public VRP getVRP() {
		return this.vrp;
	}
	
	
	public static void main(String[] args) throws IOException{
		SteepestDescent stDesc = new SteepestDescent(args[0],Integer.parseInt(args[1]));
		
		double currentCost = stDesc.getVRP().calcTotalCost();
		for(;;) {
			stDesc.findCheapestMove();
			
		}
	}
}
