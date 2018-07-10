import addOns.TimeConstraintViolationException;

public class CrossExOperation {
	
	private VRP vrp;
	private int numCustomers;
	private CrossExOption[][] crossExMatrix;
	
	public CrossExOperation(VRP vrp, int numCustomers) {
		this.vrp=vrp;
		this.numCustomers = numCustomers;
		this.crossExMatrix = new CrossExOption[numCustomers][numCustomers];
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
		
		//TODO re-think this part ignore empty vehicles
		if(cV1.succ.equals(v1.lastCustomer) || cV2.equals(v2.lastCustomer)) {
			return bestCrossEx;
		}

		//memorize the distance of the route-parts
		double distUpToC1 = 0;
		double distAfterC1 = v1.getDistance();

		double distUpToC2 = vrp.distance(v2.firstCustomer, cV2);
		double distAfterC2 = v2.getDistance()-distUpToC2;

		//go through the customer combinations 
		while(!cV1.equals(v1.lastCustomer)) {
			
			//reset distance, load and starting point for the new combination
			cV2 = v2.firstCustomer.succ;
			
			distUpToC2 = vrp.distance(v2.firstCustomer, cV2);
			distAfterC2 = v2.getDistance()-distUpToC2;
			
			loadUpToC2 = cV2.demand;
			loadAfterC2 = v2.load - loadUpToC2;
			
			while(!cV2.equals(v2.lastCustomer)) {

				//make sure that a swap would not violate capacity constraints
				if(newLoadV1 <= v1.capacity && newLoadV2 <= v2.capacity) {
					
					//get the succeeding customers
					Customer cV1Succ = cV1.succ;
					Customer cV2Succ = cV2.succ;

					//calculate the change in cost due to this move
					//TODO would just the distance-change multiplied by the cost of use be sufficient?
					double delta = (distUpToC1  + distAfterC2 + vrp.distance(cV1, cV2Succ) - vrp.distance(cV1, cV1Succ)) * v1.costOfUse
							+ (distUpToC2  + distAfterC1 + vrp.distance(cV2, cV1Succ) - vrp.distance(cV2, cV2Succ)) * v2.costOfUse
							- (v1.cost + v2.cost);

					//make sure the move would be an improvement
					if(delta < bestCrossEx.getDelta()) {

						//swap the routes
						cV1.succ = cV2Succ;
						cV2.succ = cV1Succ;

						cV1Succ.pred = cV2;
						cV2Succ.pred = cV1;

						boolean exchangeSuccess = true;

						try {
							//check for time window violations
							//make sure not to access [0][0] of the distance matrix
							//TODO re-think position
							if(cV1.custNo != 0 && cV2.custNo !=0 && cV2Succ.custNo != 0 && cV1Succ.custNo !=0) {
								cV1.propagateEarliestStart();
								cV1.propagateLatestStart();

								cV2.propagateEarliestStart();
								cV2.propagateLatestStart();
							}

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
		while(!cTmp.equals(v2.lastCustomer)) {
			cTmp.vehicle = v1;
			cTmp = cTmp.succ;
		}
		cTmp = cV1Succ;
		while(!cTmp.equals(v1.lastCustomer)) {
			cTmp.vehicle = v2;
			cTmp = cTmp.succ;
		}
		
		//swap the last customers
		cTmp = v2.lastCustomer;
		v2.lastCustomer = v1.lastCustomer;
		v1.lastCustomer = cTmp;
//		v1.lastCustomer.vehicle = v1;
//		v2.lastCustomer.vehicle = v2;
		
		//update the load of the vehicles after the exchange
		v1.load = bCE.getLoadForV1(); 
		v2.load = bCE.getLoadForV2();
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
}
