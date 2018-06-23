import java.io.IOException;

public class SteepestDescent {
	private final int PENALTY = 10000;
	private VRP vrp;
	private int numCustomers;
	private RelocateOption[][] bestMoveMatrix;

	/**
	 * Constructor for the steepest descent
	 * @param textfile
	 * @param customers
	 * @throws IOException
	 */
	public SteepestDescent(String textfile, int customers) throws IOException {
		this.vrp = new VRP(textfile,customers);
		this.bestMoveMatrix = new RelocateOption[customers][customers];
		this.numCustomers = customers;
	}


	/**
	 * Create the matrix containing the best moves from one vehicle to another
	 */
	public void createBMM() {
		//go through the matrix and determine the best move for each combination 
		for(int i = 0; i < numCustomers; i++) {
			for(int j = 0; j < numCustomers; j++) {
				bestMoveMatrix[i][j] = findBestCustomer(vrp.vehicle[i], vrp.vehicle[j]);
			}
		}
	}

	/**
	 * Find customer who's moving to another vehicle would have the highest benefit
	 * @param vFrom Vehicle, vehicle from which a customer is to be taken
	 * @param vTo Vehicle, vehicle to which a customer is to be moved
	 * @return RelocationOption, the best option for moving a customer from vFrom to vTo
	 */
	public RelocateOption findBestCustomer(Vehicle v0, Vehicle v1) {

		//create an empty move with the current cost of the vehicles
		//thus prevent the moving of one customer to another vehicle if there would be no benefit
		RelocateOption bestToMove = new RelocateOption(null, v0.calculateCost() + v1.calculateCost(), v0, v1);
		System.out.println("Cost of v" +v0.id +" and v"+ v1.id+" cost: " +bestToMove.getCostOfMove());
		//start checking from the first customer, who is not the depot-connection
		Customer current = v0.firstCustomer.succ;
		while(!current.equals(v0.lastCustomer)) {

			//find the best place to insert the customer in the other tour
			Customer insertAfter = v1.findBestPosition(current);
			if(insertAfter != null) {
				//TODO would it make sense to keep track of the distance in the vehicle?
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
				if(resultingCost < bestToMove.getCostOfMove()) {
					bestToMove = new RelocateOption(current,resultingCost,v0,v1);

				}
			}
			//go to the next customer
			current = current.succ;

		}

		//if there is no customer who's movement could lead to optimisation, penalise the move
		if(bestToMove.getCToMove() == null) {
			bestToMove.setCostOfMove(PENALTY);
		}
		return bestToMove;
	}


	/**
	 * Find the best move in the Matrix of possible moves
	 * @return RelocateOption, the currently best move in the BMM
	 */
	public RelocateOption findBestMove() {
		//start comparing with the centre of the matrix
		RelocateOption bestMove = bestMoveMatrix[0][0];
		double minCost = bestMove.getCostOfMove();
		
		RelocateOption currentMove = bestMove;
		//go through the matrix and find the move with minimal cost
		for(int i = 0; i < numCustomers; i++ ) {
			for(int j = 0; j < numCustomers; j++) {
				currentMove = bestMoveMatrix[i][j];
				double currentCost = currentMove.getCostOfMove();
				if(currentMove.getCToMove()!=null && currentCost < minCost) {
					minCost = currentCost;
					bestMove = bestMoveMatrix[i][j];
				}
			}
		}

		return bestMove;
	}
	
	/**
	 * Construct the current best move matrix, showing which customer to move from which vehicle to another
	 */
	public void printBMM() {
		System.out.print("\\ |");
		for(int i = 0 ; i < numCustomers; i++) {
			System.out.print("v"+vrp.vehicle[i].id+"|");
		}
		System.out.println("");
		for(int j = 0 ; j< numCustomers ; j++) {
			System.out.print("v"+vrp.vehicle[j].id+"|");
			for(int k = 0; k<numCustomers;k++) {
				Customer current = bestMoveMatrix[j][k].getCToMove();
				if (current == null) {
					System.out.print("X |");
				}
				else {
					System.out.print("c"+current.custNo+"|");
				}
			}
			System.out.println("");
		}
	}


	public VRP getVRP() {
		return this.vrp;
	}


	public static void main(String[] args) throws IOException{
		SteepestDescent stDesc = new SteepestDescent(args[0],Integer.parseInt(args[1]));
		stDesc.createBMM();

		stDesc.printBMM();
		stDesc.findBestMove().printOption();
	}
}
