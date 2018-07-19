
import java.io.IOException;

import addOns.TimeConstraintViolationException;

public class CrossExOperation implements Operation{

	private final double EPSILON = 1E-8;
	private VRP vrp;
	private int numCustomers;
	private Option[][] crossExMatrix;

	public CrossExOperation(VRP vrp, int numCustomers) {
		this.vrp=vrp;
		this.numCustomers = numCustomers;
		this.crossExMatrix = new CrossExOption[numCustomers][numCustomers];
	}

	/**
	 * Create the matrix containing the best cross exchange for two vehicles
	 */
	public void createOptionMatrix() {
		//fill half of the matrix since exchanging between a & b is equivalent to exchanging between b & a
		for(int i = 0 ; i < numCustomers ; i++) {
			for(int j = i+1; j < numCustomers; j++) {
				crossExMatrix[i][j] = findBestOption(vrp.vehicle[i], vrp.vehicle[j]);
			}
		}
		//printCrossEx();
	}

	/**
	 * Find the best possible cross exchange between two vehicle routes
	 * @param v1 Vehicle, the first vehicle for comparison
	 * @param v2 Vehicle, the second vehicle for comparison
	 * @return boolean, whether or not the cross exchange was successful
	 */
	public Option findBestOption(Vehicle v1, Vehicle v2) {

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
		CrossExOption bestCrossEx = new CrossExOption(v1, v2, cV1, cV2, newLoadV1, newLoadV2, 0,this);

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
			distAfterC2 = v2.getDistance() - distUpToC2;

			loadUpToC2 = cV2.demand;
			loadAfterC2 = v2.load - loadUpToC2;

			while(!cV2.equals(v2.lastCustomer)) {

				//make sure that a swap would not violate capacity constraints
				if(newLoadV1 < v1.capacity && newLoadV2 < v2.capacity) {

					//get the succeeding customers
					Customer cV1Succ = cV1.succ;
					Customer cV2Succ = cV2.succ;

					//calculate the change in cost due to this move
					double delta = (distUpToC1  + distAfterC2 + vrp.distance(cV1, cV2Succ) - vrp.distance(cV1, cV1Succ)) * v1.costOfUse
							+ (distUpToC2  + distAfterC1 + vrp.distance(cV2, cV1Succ) - vrp.distance(cV2, cV2Succ)) * v2.costOfUse
							- (v1.cost + v2.cost);

					//catch computational inaccuracy
					if(Math.abs(delta)<EPSILON) {
						delta = 0;
					}

					//make sure the move would be an improvement
					if(delta < bestCrossEx.getDelta()) {

						//swap the routes
						cV1.succ = cV2Succ;
						cV2.succ = cV1Succ;

						cV1Succ.pred = cV2;
						cV2Succ.pred = cV1;


						//if the swap is conform to time window constraints remember the option
						if(checkPropagation(v1) && checkPropagation(v2)) {
							bestCrossEx = new CrossExOption(v1, v2, cV1, cV2, newLoadV1, newLoadV2, delta,this);
							double nD1 = distUpToC1  + distAfterC2 + vrp.distance(cV1, cV2Succ) - vrp.distance(cV1, cV1Succ);
							double nD2 = distUpToC2  + distAfterC1 + vrp.distance(cV2, cV1Succ) - vrp.distance(cV2, cV2Succ);
							VehicleUpdate vUp = new VehicleUpdate(nD1,nD2,newLoadV1,newLoadV2);
							bestCrossEx.setVup(vUp);
						}

						//reverse the swap
						cV1.succ = cV1Succ;
						cV2.succ = cV2Succ;

						cV1Succ.pred = cV1;
						cV2Succ.pred = cV2;

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

	private boolean checkPropagation(Vehicle v) {
		Customer cCur = v.firstCustomer;
		while(cCur != null) {
			cCur.checkEarliest = cCur.earliestStart;
			cCur.checkLatest = cCur.latestStart;
			cCur = cCur.succ;
		}

		//execute forward propagation
		cCur = v.firstCustomer;
		Customer cSucc = cCur.succ;
		while(cSucc != null) {
			cSucc.checkEarliest = Math.max(cSucc.readyTime,cCur.checkEarliest+cCur.serviceTime+vrp.distance(cCur,cSucc));
			cCur = cSucc;
			cSucc = cSucc.succ;
		}

		//execute backward propagation
		cCur = v.lastCustomer;
		Customer cPred = cCur.pred;
		while(cPred != null) {
			cPred.checkLatest = Math.min(cPred.dueDate, cCur.checkLatest - cCur.serviceTime - vrp.distance(cPred, cCur));
			cCur = cPred;
			cPred = cPred.pred;
		}

		//check for constraint violation
		cCur = v.firstCustomer;
		while(cCur != null) {
			if(cCur.checkLatest < cCur.checkEarliest) {
				//System.out.println("Time window violation");
				return false;
			}
			cCur = cCur.succ;
		}
		return true;
	}

	/**
	 * Retrieve the best cross exchange option from the cross-exchange matrix
	 * @return CrossExOption, the cross exchange option with the greatest benefit
	 */
	public Option fetchBestOption() {
		Option bestCrossEx = crossExMatrix[0][1];
		for(int i = 0 ; i < numCustomers ; i++) {
			for(int j = i+1; j < numCustomers; j++) {
				Option curCrossEx = crossExMatrix[i][j];
				if(curCrossEx.getDelta() < bestCrossEx.getDelta()) {
					bestCrossEx = curCrossEx;
				}
			}
		}
		return bestCrossEx;
	}

	/**
	 * Execute the cross exchange between two vehicles
	 * @param bCE Option, the cross exchange that is to be executed
	 */
	public void executeOption(Option bCE) {

		//get the involved vehicles
		Vehicle v1 = bCE.getV1();
		Vehicle v2 = bCE.getV2();

		//get the customer needed for the exchange
		Customer cV1 = bCE.getC1();
		Customer cV2 = bCE.getC2();

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
		v1.lastCustomer.vehicle = v1;
		v2.lastCustomer.vehicle = v2;

		VehicleUpdate vUp = bCE.getVup();

		//update the load of the vehicles after the exchange
		v1.load = vUp.getNewLoadV1(); 
		v2.load = vUp.getNewLoadV2();
		
		//update distance and cost of the vehicle
		v1.setDistance(vUp.getNewDistV1());
		v2.setDistance(vUp.getNewDistV2());
		v1.cost = v1.getDistance() * v1.costOfUse;
		v2.cost = v2.getDistance() * v2.costOfUse;
		

		updateVehicle(v1);
		updateVehicle(v2);
		
	}
	
	private void updateVehicle(Vehicle v) {

		if(v.firstCustomer.succ.equals(v.lastCustomer)) {
			v.setDistance(0);
			v.cost = 0;
		}
		else {
//			//re-evaluate the cost of occupied cars
//			double dist = 0;
//			Customer cCur = v.firstCustomer;
//			Customer cSucc = cCur.succ;
//			while(cSucc != null) {
//				dist += vrp.distance(cCur, cSucc);
//				cCur = cSucc;
//				cSucc = cSucc.succ;
//			}
//			v.setDistance(dist);
//			v.cost = dist * v.costOfUse;
		}
	}
	


	/**
	 * Update the cross-exchange matrix by finding new best crossings for the involved vehicles
	 * @param v1 Vehicle, the first vehicles that was involved in the cross-exchange
	 * @param v2 Vehicle, the second vehicles that was involved in the cross-exchange
	 */
	public void updateOptionMatrix(Vehicle v1, Vehicle v2){
		int indV1 = v1.index;
		int indV2 = v2.index;
		for(int i = 0; i < numCustomers; i++) {
			Vehicle cV = vrp.vehicle[i];

			//only consider inter-route and one way crossing
			if(indV1 < i) {
				crossExMatrix[indV1][i] = findBestOption(v1, cV);
			}
			else if(indV1 > i) {
				crossExMatrix[i][indV1] = findBestOption(v1, cV);
			}
			if(indV2 < i) {
				crossExMatrix[indV2][i] = findBestOption(v2, cV);		
			}
			else if(indV2 > i) {
				crossExMatrix[i][indV2] = findBestOption(v2, cV);		
			}
		}
	}

	/**
	 * Construct the current cross exchange matrix, showing the obtained deltas
	 */
	public void printCrossEx() {

		//create the top line of the matrix with vehicle-id's
		String format = "\\ |";
		System.out.print(String.format("%7s",format));
		for(int i = 0 ; i < numCustomers; i++) {
			format = "v"+vrp.vehicle[i].id+"|";
			System.out.print(String.format("%7s", format));
		}
		System.out.println("");

		//print the move options line by line
		for(int j = 0 ; j< numCustomers ; j++) {
			format = "v"+vrp.vehicle[j].id+"|";
			System.out.print(String.format("%7s", format));
			for(int k = 0; k<numCustomers;k++) {
				if(k<=j) {
					System.out.print(String.format("%7s","X |"));
				}
				else {
					System.out.print(String.format("%2.2f|", crossExMatrix[j][k].getDelta()));
				}
			}
			System.out.println("");
		}
	}
	
	/**
	 * Main method for testing
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String in = args[0];
		int num = Integer.parseInt(args[1]);
		VRP vrp = new VRP(in, num);

		String fileOut = in.substring(0, in.length()-4);
		fileOut += "_Solution.txt";


	}
}
