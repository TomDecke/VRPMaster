package operators;

import java.io.IOException;
import java.util.ArrayList;

import addOns.DisplayVRP;
import addOns.TestSolution;
import moves.CrossExOption;
import moves.Option;
import representation.Customer;
import representation.VRP;
import representation.Vehicle;
import solver.SteepestDescent;

/**
 * Class to represent the cross-exchange-operator
 * @author Tom
 *
 */
public class CrossExOperation implements Operation{

	private final double EPSILON = 1E-8;
	private VRP vrp;
	private int numCustomers;
	private Option[][] crossExMatrix;

	/**
	 * Constructor for the cross-exchange operation
	 * @param vrp VRP, the VRP to which the operation is to be applied
	 * @param numCustomers int, the number of customers in the VRP
	 */
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
				crossExMatrix[i][j] = findBestOption(vrp.getVehicle()[i], vrp.getVehicle()[j]);
			}
		}
	}

	/**
	 * Find the best possible cross exchange between two vehicle routes
	 * @param v1 Vehicle, the first vehicle for comparison
	 * @param v2 Vehicle, the second vehicle for comparison
	 * @return boolean, whether or not the cross exchange was successful
	 */
	public Option findBestOption(Vehicle v1, Vehicle v2) {
		
		double oldCost = v1.getCost() + v2.getCost();

		Customer cV1 = v1.getFirstCustomer();
		Customer cV2 = v2.getFirstCustomer();

		//memorize the demand of the routes 
		int newLoadV1 = checkLoad(v1);
		int newLoadV2 = checkLoad(v2);

		//create a default best cross exchange without improvement
		CrossExOption bestCrossEx = new CrossExOption(v1, v2, cV1, cV2, newLoadV1, newLoadV2, 0,this);

		//memorize the distance of the route-parts
		double distUpToC1 = 0;
		double distAfterC1 = v1.getDistance();

		double distUpToC2 = vrp.distance(v2.getFirstCustomer(), cV2);
		double distAfterC2 = v2.getDistance()-distUpToC2;

		//go through the customer combinations 
		while(!cV1.equals(v1.getLastCustomer())) {

			//reset distance, load and starting point for the new combination
			cV2 = v2.getFirstCustomer();
			distUpToC2 = vrp.distance(v2.getFirstCustomer(), cV2);
			distAfterC2 = v2.getDistance() - distUpToC2;

			while(!cV2.equals(v2.getLastCustomer())) {

				//get the succeeding customers
				Customer cV1Succ = cV1.getSucc();
				Customer cV2Succ = cV2.getSucc();

				//calculate the change in cost due to this move
				double newCost = (distUpToC1 + vrp.distance(cV1, cV2Succ) + distAfterC2  - vrp.distance(cV2, cV2Succ)) * v1.getCostOfUse()
						+ (distUpToC2 + vrp.distance(cV2, cV1Succ) + distAfterC1  - vrp.distance(cV1, cV1Succ)) * v2.getCostOfUse();
				
				double delta = newCost - oldCost;

				//omit the exchange of depot-connection
				if(cV2Succ.equals(v2.getLastCustomer()) && cV1Succ.equals(v1.getLastCustomer())) {
					delta = 0;
				}

				//catch computational inaccuracy
				if(Math.abs(delta)<EPSILON) {
					delta = 0;
				}

				//make sure the move would be an improvement
				if(delta < bestCrossEx.getDelta()) {

					//swap the routes
					cV1.setSucc(cV2Succ);
					cV2.setSucc(cV1Succ);

					cV1Succ.setPred(cV2);
					cV2Succ.setPred(cV1);

					//check capacity constraints
					newLoadV1 = checkLoad(v1);
					newLoadV2 = checkLoad(v2);
					if(newLoadV1 <= v1.getCapacity() && newLoadV2 <= v2.getCapacity()) {

						//if the swap is conform to time window constraints remember the option
						if(checkPropagation(v1) && checkPropagation(v2)) {
							bestCrossEx = new CrossExOption(v1, v2, cV1, cV2, newLoadV1, newLoadV2, delta,this);
						}
					}

					//reverse the swap
					cV1.setSucc(cV1Succ);
					cV2.setSucc(cV2Succ);

					cV1Succ.setPred(cV1);
					cV2Succ.setPred(cV2);
				}

				//move to the next customer of vehicle 2
				cV2 = cV2.getSucc();	

				//update the distance towards/after the second customer
				distUpToC2 += vrp.distance(cV2.getPred(), cV2);
				distAfterC2 -=  vrp.distance(cV2.getPred(), cV2);
			}

			//move to the next customer of vehicle 1
			cV1 = cV1.getSucc();		

			//update the distance towards/after the first customer
			distUpToC1 += vrp.distance(cV1.getPred(), cV1);
			distAfterC1 -= vrp.distance(cV1.getPred(), cV1);
		}
		
		//if possible move customer from virtual to real vehicle
		if(v1.getFirstCustomer().getSucc().equals(v1.getLastCustomer())&&!v2.getFirstCustomer().getSucc().equals(v2.getLastCustomer())) {
			if(v1.getCostOfUse() < v2.getCostOfUse()) {
				double delta = v2.getDistance() - v2.getCost();
				bestCrossEx = new CrossExOption(v1, v2, v1.getFirstCustomer(), v2.getFirstCustomer(), v2.getLoad(), 0, delta, this);
			}
		}
		return bestCrossEx;
	}

	/**
	 * Determine the load carried by a vehicle
	 * @param v Vehicle, the vehicle to be checked
	 * @return int, the load carried by v
	 */
	private int checkLoad(Vehicle v) {
		int load = 0;
		//go through the customers in the vehicle and sum up the load
		Customer cCur = v.getFirstCustomer();
		while(cCur != null) {
			load += cCur.getDemand();
			cCur = cCur.getSucc();
		}
		return load;
	}

	/**
	 * Check if the vehicle violates time window constraints
	 * @param v Vehicle, the vehicle to be checked
	 * @return boolean, true if there are no time window violations
	 */
	private boolean checkPropagation(Vehicle v) {
		Customer cCur = v.getFirstCustomer();

		//get the current earliest and latest start
		while(cCur != null) {
			cCur.setCheckEarliest(cCur.getEarliestStart());
			cCur.setCheckLatest(cCur.getLatestStart());
			cCur = cCur.getSucc();
		}

		//execute forward propagation
		cCur = v.getFirstCustomer();
		Customer cSucc = cCur.getSucc();
		while(cSucc != null) {
			cSucc.setCheckEarliest(Math.max(cSucc.getReadyTime(),cCur.getCheckEarliest()+cCur.getServiceTime()+vrp.distance(cCur,cSucc)));
			cCur = cSucc;
			cSucc = cSucc.getSucc();
		}

		//execute backward propagation
		cCur = v.getLastCustomer();
		Customer cPred = cCur.getPred();
		while(cPred != null) {
			cPred.setCheckLatest(Math.min(cPred.getDueDate(), cCur.getCheckLatest() - cCur.getServiceTime() - vrp.distance(cPred, cCur)));
			cCur = cPred;
			cPred = cPred.getPred();
		}

		//check for constraint violation
		cCur = v.getFirstCustomer();
		while(cCur != null) {
			if(cCur.getCheckLatest() < cCur.getCheckEarliest()) {
				return false;
			}
			cCur = cCur.getSucc();
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

		Customer cV1Succ = cV1.getSucc();
		Customer cV2Succ = cV2.getSucc();

		//swap the routes
		cV1.setSucc(cV2Succ);
		cV2.setSucc(cV1Succ);

		cV1Succ.setPred(cV2);
		cV2Succ.setPred(cV1);

		//assign the customers to their new vehicles
		Customer cTmp = cV2Succ;
		while(!cTmp.equals(v2.getLastCustomer())) {
			cTmp.setVehicle(v1);
			cTmp = cTmp.getSucc();
		}
		cTmp = cV1Succ;
		while(!cTmp.equals(v1.getLastCustomer())) {
			cTmp.setVehicle(v2);
			cTmp = cTmp.getSucc();
		}

		//swap the last customers
		cTmp = v2.getLastCustomer();
		v2.setLastCustomer(v1.getLastCustomer());
		v1.setLastCustomer(cTmp);
		v1.getLastCustomer().setVehicle(v1);
		v2.getLastCustomer().setVehicle(v2);

		//update the load of the vehicles after the exchange
		v1.setLoad(bCE.getLoadForV1()); 
		v2.setLoad(bCE.getLoadForV2());

		//update distance and cost of the vehicle
		updateVehicle(v1);
		updateVehicle(v2);

		//update earliest and latest start
		propagateVehicle(v1);
		propagateVehicle(v2);

	}

	/**
	 * Update earliest and latest start of a vehicle
	 * @param v Vehicle, the vehicle to update
	 */
	private void propagateVehicle(Vehicle v) {
		Customer cCur = v.getFirstCustomer();
		//execute forward propagation
		cCur = v.getFirstCustomer();
		Customer cSucc = cCur.getSucc();
		while(cSucc != null) {
			cSucc.setEarliestStart(Math.max(cSucc.getReadyTime(),cCur.getEarliestStart()+cCur.getServiceTime()+vrp.distance(cCur,cSucc)));
			cCur = cSucc;
			cSucc = cSucc.getSucc();
		}

		//execute backward propagation
		cCur = v.getLastCustomer();
		Customer cPred = cCur.getPred();
		while(cPred != null) {
			cPred.setLatestStart(Math.min(cPred.getDueDate(), cCur.getLatestStart() - cCur.getServiceTime() - vrp.distance(cPred, cCur)));
			cCur = cPred;
			cPred = cPred.getPred();
		}
	}

	/**
	 * Updates the distance and cost of a vehicle
	 * @param v Vehicle, the vehicle to update
	 */
	private void updateVehicle(Vehicle v) {

		//clear empty vehicles
		if(v.getFirstCustomer().getSucc().equals(v.getLastCustomer())) {
			v.setDistance(0);
			v.setCost(0);
		}
		else {
			//re-evaluate the cost of occupied vehicles
			double dist = 0;
			Customer cCur = v.getFirstCustomer();
			Customer cSucc = cCur.getSucc();
			while(cSucc != null) {
				dist += vrp.distance(cCur, cSucc);
				cCur = cSucc;
				cSucc = cSucc.getSucc();
			}
			v.setDistance(dist);
			v.setCost(dist * v.getCostOfUse());
		}
	}

	/**
	 * Update the cross-exchange matrix by finding new best crossings for the involved vehicles
	 * @param v1 Vehicle, the first vehicles that was involved in the cross-exchange
	 * @param v2 Vehicle, the second vehicles that was involved in the cross-exchange
	 */
	public void updateOptionMatrix(Vehicle v1, Vehicle v2){
		int indV1 = v1.getIndex();
		int indV2 = v2.getIndex();
		for(int i = 0; i < numCustomers; i++) {
			Vehicle cV = vrp.getVehicle()[i];

			//only consider inter-route and one way crossing
			if(indV1 < i) {
				crossExMatrix[indV1][i] = findBestOption(v1,cV);
			}
			else if(indV1 > i) {
				crossExMatrix[i][indV1] = findBestOption(cV,v1);
			}
			if(indV2 < i) {
				crossExMatrix[indV2][i] = findBestOption(v2,cV);		
			}
			else if(indV2 > i) {
				crossExMatrix[i][indV2] = findBestOption(cV,v2);		
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
			format = "v"+vrp.getVehicle()[i].getId()+"|";
			System.out.print(String.format("%7s", format));
		}
		System.out.println("");

		//print the move options line by line
		for(int j = 0 ; j< numCustomers ; j++) {
			format = "v"+vrp.getVehicle()[j].getId()+"|";
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

		SteepestDescent stDesc = new SteepestDescent(vrp,fileOut);

		ArrayList<Operation> ops = new ArrayList<Operation>();
		CrossExOperation  ceo = new CrossExOperation(vrp, num);
		ops.add(ceo);


		stDesc.solve(ops, true);

		TestSolution.runTest(stDesc.getVRP(), stDesc.getTotalCost(), stDesc.getVehicles());
		DisplayVRP dVRP = new DisplayVRP(in, num, args[2]);
		dVRP.plotVRPSolution();
	}
}
