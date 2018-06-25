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
				if(i==j) {
					bestMoveMatrix[i][j] = new RelocateOption(null, PENALTY, null, null);
				}else {
					bestMoveMatrix[i][j] = findBestCustomer(vrp.vehicle[i], vrp.vehicle[j]);
				}
				
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
		//start comparing with the origin of the matrix
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
	 * Runs steepest descent, to find a solution for the vrp-instance
	 */
	//TODO tidy up
	public void solve() {
		//create best-move-matrix
		createBMM();
		
		//find the first best move
		RelocateOption relocate = findBestMove();
		
		int debugCounter = 0;
		
		//As long as there are improving moves execute them
		while(relocate.getCostOfMove() < PENALTY) {
	
			//TODO remove
			debugCounter++;
			System.out.println(debugCounter);
			printBMM();
			System.out.print("vFrom - before move: ");
			relocate.getVehicleFrom().show();
			
			System.out.print("vTo - before move: ");
			relocate.getVehicleTo().show();
			
			System.out.print("Move: c" + relocate.getCToMove().custNo);
			System.out.println(" ");
			
			executeRelocation(relocate);
			
			System.out.print("vFrom - after move: ");
			relocate.getVehicleFrom().show();
			
			System.out.print("vTo - after move: ");
			relocate.getVehicleTo().show();
			
			//after each move update the matrix and find the next move
			updateBMM(relocate.getVehicleFrom(), relocate.getVehicleTo());
			relocate = findBestMove();
		}
		
	}
	
	/**
	 * Executes the relocation of a customer
	 * @param bestRelocation RelocateOperation, option that is supposed to be executed 
	 */
	public void executeRelocation(RelocateOption bestRelocation) {
		//get the customer which is to be moved
		Customer cRelocate = bestRelocation.getCToMove();
		//remove customer from current vehicle
		bestRelocation.getVehicleFrom().remove(cRelocate);
		//insert customer into new vehicle
		bestRelocation.getVehicleTo().minCostInsertion(cRelocate);
	}
	
	/**
	 * Find the new best customer to move for vehicle-relations that were affected by a change
	 * @param vFrom Vehicle, vehicle from which a customer was removed
	 * @param vTo Vehicle, vehicle to which a customer was moved
	 */
	public void updateBMM(Vehicle vFrom, Vehicle vTo) {
		for(int i = 0; i < numCustomers; i++) {
			Vehicle vCheck = vrp.vehicle[i];
			//recalculate the giving and receiving of the first vehicle
			if(vFrom.index!=i) {
				bestMoveMatrix[vFrom.index][i] = findBestCustomer(vFrom, vCheck);
				bestMoveMatrix[i][vFrom.index] = findBestCustomer(vCheck,vFrom);
			}

			

			//recalculate the giving and receiving of the second vehicle
			if(vTo.index!=i) {
				bestMoveMatrix[i][vTo.index] = findBestCustomer(vCheck, vTo);
				bestMoveMatrix[vTo.index][i] = findBestCustomer(vFrom, vCheck);
			}
		}
	}
	
	/**
	 * Construct the current best move matrix, showing which customer to move from which vehicle to another
	 */
	public void printBMM() {
		String format = "\\ |";
		System.out.print(String.format("%4s",format));
		for(int i = 0 ; i < numCustomers; i++) {
			format = "v"+vrp.vehicle[i].id+"|";
			System.out.print(String.format("%4s", format));
		}
		System.out.println("");
		for(int j = 0 ; j< numCustomers ; j++) {
			format = "v"+vrp.vehicle[j].id+"|";
			System.out.print(String.format("%4s", format));
			for(int k = 0; k<numCustomers;k++) {
				Customer current = bestMoveMatrix[j][k].getCToMove();
				if (current == null) {
					System.out.print(String.format("%4s","X |"));
				}
				else {
				//	format = ""+(int)bestMoveMatrix[j][k].getCostOfMove()+"|";
					format ="c"+current.custNo+"|";
					
					System.out.print(String.format("%4s", format));
				}
			}
			System.out.println("");
		}
	}
	
	/**
	 * After executing @see solve() this method can be used to show the number of needed vehicles and the total cost
	 */
	public void printResults() {
		int vehicleCount = 0;
		for(int i = 0 ; i<numCustomers; i++) {
			Vehicle v = vrp.vehicle[i];
			//check if there are still customers in between the dummies
			if(!v.firstCustomer.succ.equals(v.lastCustomer)) {
				v.show();
				vehicleCount++;
			}
		}
		System.out.println("NV: "+ vehicleCount);
		System.out.println("Distance: " + vrp.calcTotalCost());
	}


	public VRP getVRP() {
		return this.vrp;
	}
	
	public RelocateOption getBestMoveAtPos(int i,int j) {
		return bestMoveMatrix[i][j];
	}


	public static void main(String[] args) throws IOException{
		SteepestDescent stDesc = new SteepestDescent(args[0],Integer.parseInt(args[1]));
		stDesc.createBMM();
		stDesc.printBMM();
		System.out.println("");
		stDesc.solve();
		stDesc.printBMM();
		
		for(int i = 0 ; i<Integer.parseInt(args[1]); i++) {
			Vehicle v = stDesc.getVRP().vehicle[i];
			System.out.println("Customer of vehicle "+v.id +": " +v.firstCustomer.succ.toString());
			v.show();
			System.out.println("Cost for vehicle "+v.id+": "+v.calculateCost());	
		}
		System.out.println("Results");
		stDesc.printResults();
	}
}
