/**
 * Class to represent the relocate-heuristic
 * @author Tom Decke
 *
 */
public class RelocateOperation implements Operation{

	private final double EPSILON = 1E-10;
	private VRP vrp;
	private int numCustomers;
	private Option[][] relocateMatrix;

	/**
	 * Constructor for the relocate operation
	 * @param vrp VRP, the VRP to which the operation is to be applied
	 * @param numCustomers int, the number of customers in the VRP
	 */
	public RelocateOperation(VRP vrp, int numCustomers) {
		this.vrp = vrp;
		this.numCustomers = numCustomers;
		this.relocateMatrix = new RelocateOption[numCustomers][numCustomers];
	}

	/**
	 * Create the matrix containing the best moves from one vehicle to another
	 */
	public void createOptionMatrix() {
		//go through the matrix and determine the best move for each combination 
		for(int i = 0; i < numCustomers; i++) {
			for(int j = 0; j < numCustomers; j++) {
				relocateMatrix[i][j] = findBestOption(vrp.vehicle[i], vrp.vehicle[j]);			
			}
		}
	}

	/**
	 * Find customer who's relocation to another vehicle would have the highest benefit
	 * @param vFrom Vehicle, vehicle from which a customer is to be taken
	 * @param vTo Vehicle, vehicle to which a customer is to be moved
	 * @return RelocationOption, the best option for relocating a customer from vFrom to vTo
	 */
	public Option findBestOption(Vehicle vFrom, Vehicle vTo) {

		double cCost = vFrom.cost + vTo.cost;

		//create an empty move with no improvement
		//thus prevent the moving of one customer to another vehicle if there would be no benefit
		RelocateOption bestToMove = new RelocateOption(null, 0, vFrom, vTo,this);


		//start checking from the first customer, who is not the depot-connection
		Customer cFrom = vFrom.firstCustomer.succ;
		while(!cFrom.equals(vFrom.lastCustomer)) {

			//if the vehicle can accommodate the customer find the best position for him
			if(vTo.canAccomodate(cFrom)) {

				//determine how the total distance of vFrom would change
				double newDistVFrom = vFrom.getDistance() + vrp.distance(cFrom.pred, cFrom.succ) 
				- vrp.distance(cFrom.pred, cFrom)
				- vrp.distance(cFrom, cFrom.succ);

				//catch computational inaccuracy
				if(Math.abs(newDistVFrom) < EPSILON) {
					newDistVFrom = 0;
				}

				Customer cToPred = vTo.firstCustomer;
				Customer cToSucc = cToPred.succ;
				while(!cToPred.equals(vTo.lastCustomer)) {
					// a customer can not be inserted before/after himself
					if(!(cFrom.equals(cToPred)||cFrom.equals(cToSucc))) {
						if(cFrom.canBeInsertedBetween(cToPred, cToSucc)) {
							//determine how the total distance of vTo would change
							double newDistVTo = vTo.getDistance() - vrp.distance(cToPred, cToSucc)
									+ vrp.distance(cToPred, cFrom)
									+ vrp.distance(cFrom, cToSucc);

							//catch computational inaccuracy
							if(Math.abs(newDistVTo) < EPSILON) {
								newDistVTo = 0;
							}

							//the new cost for the vehicles, if this move was to be made
							double resultingCost = newDistVFrom * vFrom.costOfUse + newDistVTo * vTo.costOfUse;

							//the change in cost
							double deltaCost =  resultingCost - cCost;

							//catch computational inaccuracy
							if(Math.abs(deltaCost) < EPSILON) {
								deltaCost = 0;
							}

							//if this move is cheaper, take it up
							if(deltaCost < bestToMove.getDelta()) {
								bestToMove = new RelocateOption(cFrom,deltaCost,vFrom,vTo,this);
								bestToMove.setcPred(cToPred);
								bestToMove.setcSucc(cToSucc);
							}
						}
					}
					//move to the next spot where the customer could be inserted
					cToPred = cToSucc;
					cToSucc = cToSucc.succ;
				}
			}
			//go to the next customer
			cFrom = cFrom.succ;
		}
		return bestToMove;
	}


	/**
	 * Find the best relocation in the matrix of possible relocations
	 * @return RelocateOption, the currently best move in the BMM
	 */
	public Option fetchBestOption() {
		//start comparing with the origin of the matrix
		Option bestMove = relocateMatrix[0][0];
		double minCost = bestMove.getDelta();

		Option currentMove = bestMove;
		//go through the matrix and find the move with minimal cost
		for(int i = 0; i < numCustomers; i++ ) {
			for(int j = 0; j < numCustomers; j++) {
				currentMove = relocateMatrix[i][j];
				double currentCost = currentMove.getDelta();
				if(currentMove.getCToMove()!=null && currentCost < minCost) {
					minCost = currentCost;
					bestMove = currentMove;
				}
			}
		}
		return bestMove;
	}

	/**
	 * Executes the relocation of a customer
	 * @param bR RelocateOperation, option that is supposed to be executed 
	 */
	public void executeOption(Option bR) {
		//get the customer which is to be moved
		Customer cRelocate = bR.getCToMove();

		//remove customer from current vehicle if it exists
		if(bR.getV1().remove(cRelocate)) {
			//insert customer into new vehicle
			bR.getV2().insertBetween(bR.getCToMove(), bR.getC1(), bR.getC2());
		}
	}

	/**
	 * Find the new best customer to move for vehicle-relations that were affected by a change
	 * @param vFrom Vehicle, vehicle from which a customer was removed
	 * @param vTo Vehicle, vehicle to which a customer was moved
	 */
	public void updateOptionMatrix(Vehicle vFrom, Vehicle vTo) {
		for(int i = 0; i < numCustomers; i++) {
			Vehicle vCheck = vrp.vehicle[i];
			//recalculate the giving and receiving of the first vehicle
			relocateMatrix[vFrom.index][i] = findBestOption(vFrom, vCheck);
			relocateMatrix[i][vFrom.index] = findBestOption(vCheck,vFrom);

			//recalculate the giving and receiving of the second vehicle
			relocateMatrix[i][vTo.index] = findBestOption(vCheck, vTo);
			relocateMatrix[vTo.index][i] = findBestOption(vFrom, vCheck);
		}
	}

	/**
	 * Construct the current best move matrix, showing which customer to move from which vehicle to an other
	 */
	public void printRelocateMatrix() {

		//create the top line of the matrix with vehicle-id's
		String format = "\\ |";
		System.out.print(String.format("%4s",format));
		for(int i = 0 ; i < numCustomers; i++) {
			format = "v"+vrp.vehicle[i].id+"|";
			System.out.print(String.format("%4s", format));
		}
		System.out.println("");

		//print the move options line by line
		for(int j = 0 ; j< numCustomers ; j++) {
			format = "v"+vrp.vehicle[j].id+"|";
			System.out.print(String.format("%4s", format));
			for(int k = 0; k<numCustomers;k++) {
				Customer current = relocateMatrix[j][k].getCToMove();
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
}
