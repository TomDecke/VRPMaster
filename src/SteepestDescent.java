import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import addOns.TimeConstraintViolationException;


/**
 * 
 * @author Tom Decke
 *
 */
public class SteepestDescent {
	private final int PENALTY = 10000;
	private final double EPSILON = 1E-10;
	private String out;
	private VRP vrp;
	private int numCustomers;

	//memory for the operators
	private RelocateOption[][] bestMoveMatrix;
	private ExchangeOption[][] exchangeMatrix;
	private CrossExOption[][] crossExMatrix;


	/**
	 * Constructor for the steepest descent
	 * @param textfile
	 * @param customers
	 * @throws IOException
	 */
	public SteepestDescent(String textfile, int customers) throws IOException {
		this.vrp = new VRP(textfile,customers);
		this.out = textfile.substring(0, textfile.length()-4);
		this.out += "_Solution.txt";
		this.bestMoveMatrix = new RelocateOption[customers][customers];
		this.exchangeMatrix = new ExchangeOption[customers][customers];
		this.crossExMatrix = new CrossExOption[customers][customers];
		this.numCustomers = customers;
	}


	/**
	 * Create the matrix containing the best moves from one vehicle to another
	 */
	public void createBMM() {
		//go through the matrix and determine the best move for each combination 
		for(int i = 0; i < numCustomers; i++) {
			for(int j = 0; j < numCustomers; j++) {
				//omit the relocation in the same vehicle on the first run
				if(i==j) {
					bestMoveMatrix[i][j] = new RelocateOption(null, PENALTY,vrp.vehicle[i], vrp.vehicle[j]);
				}else {
					bestMoveMatrix[i][j] = findBestRelocation(vrp.vehicle[i], vrp.vehicle[j]);
				}				
			}
		}
	}

	/**
	 * Create the matrix containing the best exchanges between tours
	 */
	public void createExchangeMatrix() {
		//fill half of the matrix since swapping a & b is equivalent to swapping b & a
		for(int i = 0 ; i < numCustomers ; i++) {
			for(int j = i+1; j < numCustomers; j++) {
				exchangeMatrix[i][j] = findBestExchange(vrp.vehicle[i], vrp.vehicle[j]);
			}
		}
	}

	/**
	 * Create the matrix containing the best cross exchange for two vehicles
	 */
	public void createCrossExMatrix() {
		//fill half of the matrix since exchanging between a & b is equivalent to exchanging between b & a
		for(int i = 0 ; i < numCustomers ; i++) {
			for(int j = i+1; j < numCustomers; j++) {
				crossExMatrix[i][j] = findBestCrossEx(vrp.vehicle[i], vrp.vehicle[j]);
			}
		}
		printCrossEx();

	}


	/**
	 * Find customer who's relocation to another vehicle would have the highest benefit
	 * @param vFrom Vehicle, vehicle from which a customer is to be taken
	 * @param vTo Vehicle, vehicle to which a customer is to be moved
	 * @return RelocationOption, the best option for relocating a customer from vFrom to vTo
	 */
	public RelocateOption findBestRelocation(Vehicle vFrom, Vehicle vTo) {

		//create an empty move with the current cost of the vehicles
		//thus prevent the moving of one customer to another vehicle if there would be no benefit
		RelocateOption bestToMove = new RelocateOption(null, vFrom.cost + vTo.cost, vFrom, vTo);

		//if the vehicles are the same, then there is only one cost
		if(vFrom.equals(vTo)) {
			bestToMove.setCostOfMove(vFrom.cost);
		}

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
				if(newDistVFrom < EPSILON) {
					newDistVFrom = 0;
				}

				//compare to all other valid options
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
							if(newDistVTo < EPSILON) {
								newDistVTo = 0;
							}

							//the occurring cost, if this move was to be made
							double resultingCost = newDistVFrom * vFrom.costOfUse + newDistVTo * vTo.costOfUse;

							//if this move is cheaper, take it up
							if(resultingCost < bestToMove.getCostOfMove()) {
								bestToMove = new RelocateOption(cFrom,resultingCost,vFrom,vTo);
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
		//if there is no customer who's movement could lead to improvement, penalise the move
		if(bestToMove.getCToMove() == null) {
			bestToMove.setCostOfMove(PENALTY);
		}
		return bestToMove;
	}

	/**
	 * Find the customer exchange between two vehicles that yields the biggest cost benefit
	 * @param v1 Vehicle, the first vehicle that is part of the swap
	 * @param v2 Vehicle, the second vehicle that is part of the swap
	 * @return ExchangeOption, the best exchange option for v1 and v2
	 */
	public ExchangeOption findBestExchange(Vehicle v1, Vehicle v2) {

		//create a default exchange option
		ExchangeOption bestExchange = new ExchangeOption(v1, v2, null, null, 0);

		//set up the encapsulating customers
		Customer cV1Pred = v1.firstCustomer;
		Customer cV1Succ = cV1Pred.succ;
		Customer cV2Pred = v2.firstCustomer;
		Customer cV2Succ = cV2Pred.succ;

		//Iterate through the customers from the first vehicle
		Customer cV1 = v1.firstCustomer.succ;
		while(!cV1.equals(v1.lastCustomer)) {

			//get the encapsulating customers for c1
			cV1Pred = cV1.pred;
			cV1Succ = cV1.succ;

			//check the exchange with every customer from the second vehicle
			Customer cV2 = v2.firstCustomer.succ;
			while(!cV2.equals(v2.lastCustomer)) {

				//get the encapsulating customers for c2
				cV2Pred = cV2.pred;
				cV2Succ = cV2.succ;

				//make sure the exchange does not violate time window constraints
				if(cV1.canBeInsertedBetween(cV2Pred, cV2Succ) && cV2.canBeInsertedBetween(cV1Pred, cV1Succ)) {

					//ensure that the vehicles possess the capacity for the exchange
					if((v1.load-cV1.demand+cV2.demand)<=v1.capacity && (v2.load-cV2.demand+cV1.demand)<=v2.capacity) {
						//get the change in distance for v1
						double deltaDistV1 = - vrp.distance(cV1Pred, cV1) - vrp.distance(cV1, cV1Succ)
								+ vrp.distance(cV1Pred, cV2) + vrp.distance(cV2, cV1Succ);

						//get the change in distance for v2
						double deltaDistV2 =  - vrp.distance(cV2Pred, cV2) - vrp.distance(cV2, cV2Succ)
								+ vrp.distance(cV2Pred, cV1) + vrp.distance(cV1, cV2Succ);

						//get the change in cost by subtracting the current cost from the potential new cost
						double delta = (deltaDistV1 * v1.costOfUse + deltaDistV2 * v2.costOfUse)-(v1.cost + v2.cost); 

						if(delta < bestExchange.getDelta()) {
							bestExchange = new ExchangeOption(v1,v2, cV1, cV2, delta);
						}
					}
				}

				//move on to the next customer of vehicle two
				cV2 = cV2.succ;
			}

			//move on to the next vehicle of customer one
			cV1 = cV1.succ;
		}

		return bestExchange;
	}

	/**
	 * Find the best possible cross exchange between two vehicle routes
	 * @param v1 Vehicle, the first vehicle for comparison
	 * @param v2 Vehicle, the second vehicle for comparison
	 * @return boolean, whether or not the cross exchange was successful
	 */
	public CrossExOption findBestCrossEx(Vehicle v1, Vehicle v2) {

		Customer cV1 = v1.firstCustomer;
		Customer cV2 = v2.firstCustomer.succ;


		//memorize the demand of the route-parts 
		int loadUpToC1 = cV1.demand;
		int loadAfterC1 = v1.load-loadUpToC1;

		int loadUpToC2 = cV2.demand;
		int loadAfterC2 = v2.load - loadUpToC2;

		int newLoadV1 = loadUpToC1 + loadAfterC2;
		int newLoadV2 = loadUpToC2 + loadAfterC1;

		//create a default best cross exchange without improvement
		CrossExOption bestCrossEx = new CrossExOption(v1, v2, cV1, cV2, newLoadV1, newLoadV2, 0);

		//memorize the distance of the route-parts
		double distUpToC1 = 0;
		double distAfterC1 = v1.getDistance();

		double distUpToC2 = vrp.distance(v2.firstCustomer, cV2);
		double distAfterC2 = v2.getDistance()-distUpToC2;

		//go through the customer combinations 
		while(!cV1.equals(v1.lastCustomer)) {
			
			//reset distance, load and starting point for the new combination
			distUpToC2 = vrp.distance(v2.firstCustomer, cV2);
			distAfterC2 = v2.getDistance()-distUpToC2;
			
			loadUpToC2 = cV2.demand;
			loadAfterC2 = v2.load - loadUpToC2;
			
			cV2 = v2.firstCustomer.succ;
			
			while(!cV2.equals(v2.lastCustomer)) {

				//make sure that a swap would not violate capacity constraints
				if(newLoadV1 <= v1.capacity && newLoadV2 <= v2.capacity) {

					//calculate the change in cost due to this move
					//TODO include new distances, something is fishy here
					double delta = (distUpToC1 * v1.costOfUse + distAfterC2 * v2.costOfUse)
							+ (distUpToC2 * v2.costOfUse + distAfterC1 * v1.costOfUse)
							- (v1.cost + v2.cost);

					//make sure the move would be an improvement
					if(delta < bestCrossEx.getDelta()) {

						//get the succeeding customers
						Customer cV1Succ = cV1.succ;
						Customer cV2Succ = cV2.succ;

						//swap the routes
						cV1.succ = cV2Succ;
						cV2.succ = cV1Succ;

						cV1Succ.pred = cV2;
						cV2Succ.pred = cV1;

						boolean exchangeSuccess = true;

						try {
							//check for time window violations
							cV1.propagateEarliestStart();
							cV1.propagateLatestStart();

							cV2.propagateEarliestStart();
							cV2.propagateLatestStart();
						} catch (TimeConstraintViolationException e) {
							//the swapping violated a constraint, thus reverse it
							cV1.succ = cV1Succ;
							cV2.succ = cV2Succ;

							cV1Succ.pred = cV1;
							cV2Succ.pred = cV2;

							System.out.println(e.getMessage());

						}

						//in case of a success reset the situation and create a new best exchange
						if(exchangeSuccess) {
							bestCrossEx = new CrossExOption(v1, v2, cV1, cV2, newLoadV1, newLoadV2, delta);

							cV1.succ = cV1Succ;
							cV2.succ = cV2Succ;

							cV1Succ.pred = cV1;
							cV2Succ.pred = cV2;
						}
					}
				}
				//move to the next customer of vehicle 2
				cV2 = cV2.succ;	

				//update the demand before/after the second customer
				loadUpToC2 += cV2.demand;
				loadAfterC2 -= cV2.demand;

				//update the distance towards/after the second customer
				distUpToC2 += vrp.distance(cV2.pred, cV2);
				distAfterC2 -=  vrp.distance(cV2.pred, cV2);

				//update the load which a vehicle would have to carry in case of an exchange
				newLoadV1 = loadUpToC1 + loadAfterC2;
				newLoadV2 = loadUpToC2 + loadAfterC1;
			}

			//move to the next customer of vehicle 1
			cV1 = cV1.succ;		

			//update the demand before/after the first customer
			loadUpToC1 += cV1.demand;
			loadAfterC1 -= cV1.demand;

			//update the distance towards/after the second customer
			distUpToC1 += vrp.distance(cV1.pred, cV1);
			distAfterC1 -= vrp.distance(cV1.pred, cV1);
		}

		return bestCrossEx;
	}


	/**
	 * Runs steepest descent, to find a solution for the vrp-instance
	 */
	public void solve_Relocate() {

		//create best-move-matrix and print it to the console
		createBMM();
		printBMM();
		System.out.println(" ");

		//find the first best move
		RelocateOption relocate = fetchBestRelocation();

		int iterationCounter = 0;

		//As long as there are improving moves execute them
		while(relocate.getCostOfMove() < PENALTY) {

			//Visualize the state before the relocation on the console
			iterationCounter++;
			System.out.println(iterationCounter);
			//uncomment if wanted printBMM();
			System.out.print("vFrom - before move: ");
			relocate.getVehicleFrom().show();
			System.out.print("vTo - before move: ");
			relocate.getVehicleTo().show();
			relocate.printOption();

			//relocate the customer
			executeRelocation(relocate);

			//Visualize the state after the relocation on the console
			System.out.print("vFrom - after move: ");
			relocate.getVehicleFrom().show();
			System.out.print("vTo - after move: ");
			relocate.getVehicleTo().show();
			System.out.println(" ");

			//after each move update the matrix and find the next move
			updateBMM(relocate.getVehicleFrom(), relocate.getVehicleTo());
			relocate = fetchBestRelocation();
		}
		//print the last BMM
		printBMM();
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% \n");

		//		//TODO let's try something funny
		//		createExchangeMatrix();
		//		fetchBestExchange().printOption();
		//		executeExchange(fetchBestExchange());		

		printResultsToConsole();
		printResultsToFile();
	}

	/**
	 * Solve the problem with help of the cross exchange operator
	 */
	public void solve_CrossEx() {
		createCrossExMatrix();
		CrossExOption crossEx = fetchBestCrossEx();
		while(crossEx.getDelta() < 0) {
			executeCrossEx(crossEx);
			updateCrossExMatrix(crossEx.getV1(), crossEx.getV2());
			crossEx = fetchBestCrossEx();
		}

		printResultsToConsole();
		printResultsToFile();
	}


	/**
	 * Find the best relocation in the matrix of possible relocations
	 * @return RelocateOption, the currently best move in the BMM
	 */
	public RelocateOption fetchBestRelocation() {
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
					bestMove = currentMove;
				}
			}
		}
		return bestMove;
	}


	/**
	 * Retrieves the best exchange option from the exchange matrix
	 * @return ExchangeOption, the option with the greatest cost reduction
	 */
	public ExchangeOption fetchBestExchange() {
		ExchangeOption bestExchange = exchangeMatrix[0][1];
		for(int i = 0 ; i < numCustomers ; i++) {
			for(int j = i+1; j < numCustomers; j++) {
				ExchangeOption curExch = exchangeMatrix[i][j];
				if(curExch.getDelta() < bestExchange.getDelta()) {
					bestExchange = curExch;
				}
			}
		}
		return bestExchange;
	}

	/**
	 * Retrieve the best cross exchange option from the cross-exchange matrix
	 * @return CrossExOption, the cross exchange option with the greatest benefit
	 */
	public CrossExOption fetchBestCrossEx() {
		CrossExOption bestCrossEx = crossExMatrix[0][1];
		for(int i = 0 ; i < numCustomers ; i++) {
			for(int j = i+1; j < numCustomers; j++) {
				CrossExOption curCrossEx = crossExMatrix[i][j];
				if(curCrossEx.getDelta() < bestCrossEx.getDelta()) {
					bestCrossEx = curCrossEx;
				}
			}
		}
		return bestCrossEx;
	}

	/**
	 * Executes the relocation of a customer
	 * @param bR RelocateOperation, option that is supposed to be executed 
	 */
	public void executeRelocation(RelocateOption bR) {
		//get the customer which is to be moved
		Customer cRelocate = bR.getCToMove();

		//remove customer from current vehicle if it exists
		if(bR.getVehicleFrom().remove(cRelocate)) {
			//insert customer into new vehicle
			bR.getVehicleTo().insertBetween(bR.getCToMove(), bR.getcPred(), bR.getcSucc());
		}
	}

	/**
	 * Swaps two customers according to the information stored in the exchange option
	 * @param bE ExchangeOption, exchange option to be used
	 */
	public void executeExchange(ExchangeOption bE) {
		//obtain information of customer from vehicle 1
		Customer c1		= bE.getcV1();
		Customer c1Pred = c1.pred;
		Customer c1Succ = c1.succ;
		Vehicle v1 = bE.getV1();

		//obtain information of customer from vehicle 2
		Customer c2		= bE.getcV2();
		Customer c2Pred = c2.pred;
		Customer c2Succ = c2.succ;
		Vehicle v2 = bE.getV2();

		if(v1.remove(c1)) {
			if(v2.remove(c2)) {
				//swap the customers
				v1.insertBetween(c2, c1Pred, c1Succ);
				v2.insertBetween(c1, c2Pred, c2Succ);
			}
			//if the removal fails, reverse the prior one
			else {
				v1.insertBetween(c1, c1Pred, c1Succ);
			}
		}
	}

	/**
	 * Execute the cross exchange between two vehicles
	 * @param bCE CrossExOption, the cross exchange that is to be executed
	 */
	public void executeCrossEx(CrossExOption bCE) {

		//get the involved vehicles
		Vehicle v1 = bCE.getV1();
		Vehicle v2 = bCE.getV2();

		//get the customer needed for the exchange
		Customer cV1 = bCE.getcV1();
		Customer cV2 = bCE.getcV2();

		Customer cV1Succ = cV1.succ;
		Customer cV2Succ = cV2.succ;

		//swap the routes
		cV1.succ = cV2Succ;
		cV2.succ = cV1Succ;

		cV1Succ.pred = cV2;
		cV2Succ.pred = cV1;

		//assign the customers to their new vehicles
		Customer cTmp = cV2Succ;
		//TODO should I swap the last customer as well? In the end it's just the depot.
		while(!cTmp.equals(v2.lastCustomer)) {
			cTmp.vehicle = v1;
			cTmp = cTmp.succ;
		}
		cTmp = cV1Succ;
		while(!cTmp.equals(v2.lastCustomer)) {
			cTmp.vehicle = v2;
			cTmp = cTmp.succ;
		}

		//update the load of the vehicles after the exchange
		v1.load = bCE.getLoadForV1(); 
		v2.load = bCE.getLoadForV2();
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
				bestMoveMatrix[vFrom.index][i] = findBestRelocation(vFrom, vCheck);
				bestMoveMatrix[i][vFrom.index] = findBestRelocation(vCheck,vFrom);
			}

			if(vTo.index!=i) {
				//recalculate the giving and receiving of the second vehicle
				bestMoveMatrix[i][vTo.index] = findBestRelocation(vCheck, vTo);
				bestMoveMatrix[vTo.index][i] = findBestRelocation(vFrom, vCheck);
			}
		}
	}


	/**
	 * Update the exchange matrix by finding new best exchanges for vehicles that were involved in the change
	 * @param v1 Vehicle, the first vehicles that was involved in the exchange
	 * @param v2 Vehicle, the second vehicles that was involved in the exchange
	 */
	public void updateExchangeMatrix(Vehicle v1, Vehicle v2){
		for(int i = 0; i < numCustomers; i++) {
			Vehicle cV = vrp.vehicle[i];
			int indV1 = v1.index;
			int indV2 = v1.index;
			//only consider inter-route changes and one way swapping
			if(indV1 < i) {
				exchangeMatrix[i][indV1] = findBestExchange(v1, cV);
			}
			else if(indV1 > i) {
				exchangeMatrix[indV1][i] = findBestExchange(v1, cV);
			}
			if(indV2 < i) {
				exchangeMatrix[i][indV2] = findBestExchange(v2, cV);		
			}
			else if(indV2 > i) {
				exchangeMatrix[indV2][i] = findBestExchange(v2, cV);		
			}
		}
	}


	/**
	 * Update the cross-exchange matrix by finding new best crossings for the involved vehicles
	 * @param v1 Vehicle, the first vehicles that was involved in the cross-exchange
	 * @param v2 Vehicle, the second vehicles that was involved in the cross-exchange
	 */
	public void updateCrossExMatrix(Vehicle v1, Vehicle v2){
		for(int i = 0; i < numCustomers; i++) {
			Vehicle cV = vrp.vehicle[i];
			int indV1 = v1.index;
			int indV2 = v1.index;
			//only consider inter-route and one way crossing
			if(indV1 < i) {
				crossExMatrix[i][indV1] = findBestCrossEx(v1, cV);
			}
			else if(indV1 > i) {
				crossExMatrix[indV1][i] = findBestCrossEx(v1, cV);
			}
			if(indV2 < i) {
				crossExMatrix[i][indV2] = findBestCrossEx(v2, cV);		
			}
			else if(indV2 > i) {
				crossExMatrix[indV2][i] = findBestCrossEx(v2, cV);		
			}
		}
	}



	/**
	 * Executes a two-opt move for a vehicle if possible
	 * @param v Vehicle, the vehicle which is to be checked for crossings
	 * @return boolean, whether or not an optimization took place
	 */
	public boolean twoOpt(Vehicle v) {
		//get the first route of the vehicle
		Customer c1 = v.firstCustomer;
		Customer c2 = c1.succ;
		//go through all routes

		while(!c2.equals(v.lastCustomer)) {

			//get the succeeding route
			Customer c3 = c2;
			Customer c4 = c3.succ;
			//compare each route with all following routes
			while(!c3.equals(v.lastCustomer)) {

				//check if the routes cross
				if(lineCollision(c1, c2, c3, c4)) {

					//try the reversion on a copy of the data to check time windows and cost constraint
					if(reverseRoute(v.copy(), c3.copy(), c2.copy())) {
						//if the reversion is possible, execute it
						reverseRoute(v, c3, c2);
					}
				}
				//move to the following route
				c3 = c4;
				c4 = c4.succ;
			}
			//move to the next route
			c1 = c2;
			c2 = c2.succ;
		}
		//no crossing occurred, thus 
		return false;
	}

	/**
	 * Reverse the route between two customers
	 * @param v Vehicle, the vehicle which drives the route
	 * @param newStart Customer, the customer which is the new start for the reversal
	 * @param newEnd Customer, the customer which is the new end for the reversal
	 * @return boolean, true, if reversing is possible and improves the cost, false otherwise
	 */
	public boolean reverseRoute(Vehicle v, Customer newStart, Customer newEnd) {
		;

		System.out.println("Reverse vehicle: ");
		v.show();

		double preCost = v.cost;

		Customer last = newStart.succ;
		Customer limit = newEnd.pred;
		ArrayList<Customer> customers = new ArrayList<Customer>();


		//read the customers which are to be reversed in reversed order
		Customer cCur = newStart;
		while(!cCur.equals(limit)) {
			customers.add(cCur);

			//move on to the next customer
			cCur = cCur.pred;

			//remove the customer of this visit
			v.remove(cCur.succ);

		}

		//display the customers that were taken out
		v.show();
		for(Customer c : customers) {
			System.out.println(c.custNo);
		}

		//try to insert the customers that were taken back into the route
		Customer cPred = limit;
		for(Customer c : customers) {
			if(c.canBeInsertedBetween(cPred, last)) {
				v.insertBetween(c, cPred, last);
			}
			else {
				System.out.println("Time window violation");
				return false;
			}
			cPred = c;
		}

		//check if the cost of the vehicle has decreased
		if(v.cost < preCost) {
			return true;
		}
		else {
			System.out.println("No cost improvement");
			return false;
		}
	}

	/**
	 * Determine if the routes from c1c2 and c3c4 cross each other
	 * the solution is taken from 
	 * https://www.spieleprogrammierer.de/wiki/2D-Kollisionserkennung#Kollision_zwischen_zwei_Strecken (01.07.2018)
	 * @param c1 Customer, containing the starting point of c1c2 
	 * @param c2 Customer, containing the end point of c1c2
	 * @param c3 Customer, containing the starting point of c3c4
	 * @param c4 Customer, containing the end point of c3c4
	 * @return
	 */
	public boolean lineCollision(Customer c1, Customer c2, Customer c3, Customer c4  ) {

		//extract the coordinates of the routes
		double xC1 = c1.xCoord;
		double yC1 = c1.yCoord;
		double xC2 = c2.xCoord;
		double yC2 = c2.yCoord;
		double xC3 = c3.xCoord;
		double yC3 = c3.yCoord;
		double xC4 = c4.xCoord;
		double yC4 = c4.yCoord;

		//check if the routes are contiguous
		if((xC1 == xC3 && yC1 == yC3) || (xC1 == xC4 && yC1 == yC4)) {
			//the start point of c1c2 is identical with either c3 or c4
			return false;
		}
		else if((xC2 == xC3 && yC2 == yC3) || (xC2 == xC4 && yC2 == yC4)) {
			//the end point of c1c2 is identical with either c3 or c4
			return false;
		}

		//calculate the denominator
		double denom = (yC4-yC3) * (xC2-xC1) - (xC4-xC3) * (yC2-yC1);

		//If the solution is close to 0 the routes are parallel
		if(Math.abs(denom)<EPSILON) {
			return false;
		}

		double c1c2 = ((xC4-xC3)*(yC1-yC3) - (yC4-yC3)*(xC1-xC3))/denom;
		double c3c4 = ((xC2-xC1)*(yC1-yC3) - (yC2-yC1)*(xC1-xC3))/denom;

		//check if the crossing happens between the end points of both routes
		return (c1c2 >= 0 && c1c2 <= 1) && (c3c4 >= 0 && c3c4 <= 1);
	}


	/**
	 * Construct the current best move matrix, showing which customer to move from which vehicle to an other
	 */
	public void printBMM() {

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
	 * Construct the current cross exchange matrix, showing the obtained deltas
	 */
	public void printCrossEx() {

		//create the top line of the matrix with vehicle-id's
		String format = "\\ |";
		System.out.print(String.format("%5s",format));
		for(int i = 0 ; i < numCustomers; i++) {
			format = "v"+vrp.vehicle[i].id+"|";
			System.out.print(String.format("%5s", format));
		}
		System.out.println("");

		//print the move options line by line
		for(int j = 0 ; j< numCustomers ; j++) {
			format = "v"+vrp.vehicle[j].id+"|";
			System.out.print(String.format("%5s", format));
			for(int k = 0; k<numCustomers;k++) {
				if(k<=j) {
					System.out.print(String.format("%5s","X |"));
				}
				else {


					System.out.print(String.format("%1.2f|", crossExMatrix[j][k].getDelta()));
				}
			}
			System.out.println("");
		}
	}


	/**
	 * After executing @see solve(), this method can be used to show the number of needed vehicles and the total cost
	 */
	public void printResultsToConsole() {
		System.out.println("NV: "+ this.getVehicleCount());
		System.out.println("Distance: " + vrp.calcTotalCost());
		System.out.println(" ");
	}


	/**
	 * Write the results to a text-file
	 */
	public void printResultsToFile() {
		//create a writer
		FileWriter writer;
		try {
			writer 	= new FileWriter(out);
			//write the cost of the solution
			writer.write(""+vrp.m +" "+this.getVehicleCount()+"\n");

			//write the customers of each vehicle as a route
			for(Vehicle v : getVehicles()) {
				StringBuilder sBuild = new StringBuilder();
				Customer customer = v.firstCustomer.succ;
				while (customer != v.lastCustomer){
					sBuild.append(customer.custNo + " ");
					customer = customer.succ;
				}
				sBuild.append(String.format(" -1%n"));
				//write the tour of the vehicle
				writer.write(sBuild.toString());
			}
			writer.write("total cost: "+getTotalCost());
			writer.close();
		}catch(IOException ioe) {
			System.out.println("Error whilst writing");
		}
	}


	/**
	 * Determine the number of vehicles that are needed in the solution
	 * @return int, the number of vehicles
	 */
	private int getVehicleCount() {
		int vehicleCount = 0; //number of vehicles needed in the solution
		for(int i = 0 ; i<numCustomers; i++) {
			Vehicle v = vrp.vehicle[i];
			//check if there are still customers in between the dummies
			if(!v.firstCustomer.succ.equals(v.lastCustomer)) {
				v.show();
				System.out.println("Distance Vehicle: " + v.cost);
				vehicleCount++;
			}
		}
		return vehicleCount;
	}


	/**
	 * After executing @see solve(), this method can be used to obtain the vehicles, which are present in the solution 
	 * @return ArrayList<Vehicle>, list of vehicles with customers
	 */
	public ArrayList<Vehicle> getVehicles(){
		ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
		for(int i = 0 ; i<numCustomers; i++) {
			Vehicle v = vrp.vehicle[i];
			//check if there are still customers in between the dummies
			if(!v.firstCustomer.succ.equals(v.lastCustomer)) {
				vehicles.add(v);
			}
		}
		return vehicles;
	}


	/**
	 * After executing @see solve(), this method can be used to obtain the cost of the solution
	 * @return double, the total cost of the solution
	 */
	public double getTotalCost() {
		return vrp.calcTotalCost();
	}


	/**
	 * Accessor for the VRP
	 * @return VRP, the VRP-instance of the class
	 */
	public VRP getVRP() {
		return this.vrp;
	}


	/**
	 * Main method for testing
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException{
		String in = args[0];
		int num = Integer.parseInt(args[1]);


		SteepestDescent stDesc = new SteepestDescent(in, num);
		//		stDesc.solve_Relocate();
		//		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		//		stDesc.createExchangeMatrix();
		//		ExchangeOption bE = stDesc.fetchBestExchange();
		//		bE.printOption();
		//		stDesc.executeExchange(bE);
		//		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");

		stDesc.solve_CrossEx();
		


		//		stDesc.createExchangeMatrix();
		TestSolution.runTest(stDesc.vrp, stDesc.getTotalCost(), stDesc.getVehicles());
		DisplayVRP dVRP = new DisplayVRP(in, num, args[2]);
		dVRP.plotVRPSolution();

		//		for(Vehicle v : stDesc.getVehicles()) {
		//			stDesc.twoOpt(v);
		//		}


		stDesc.printCrossEx();
	}
}
