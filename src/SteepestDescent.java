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
	private final double EPSILON = 1E-8;
	private String out;
	private VRP vrp;
	private int numCustomers;
	private RelocateOption[][] bestMoveMatrix;
	private ExchangeOption[][] exchangeMatrix;


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
					bestMoveMatrix[i][j] = new RelocateOption(null, 0,vrp.vehicle[i], vrp.vehicle[j]);
				}else {
					bestMoveMatrix[i][j] = findBestCustomer(vrp.vehicle[i], vrp.vehicle[j]);
				}				
			}
		}
	}

	//TODO code exchange matrix
	public void createExchangeMatrix() {
		for(int i = 0 ; i < numCustomers ; i++) {
			for(int j = i+1; j < numCustomers; j++) {
				System.out.println(String.format("i: %d j: %d",i,j));
			}
		}
	}

	public ExchangeOption findBestExchange(Vehicle v1, Vehicle v2) {

		//get the current distance
		double distV1 = v1.getDistance();
		double distV2 = v2.getDistance();

		//get the current cost
		double curCost = v1.cost + v2.cost;

		ExchangeOption bestExchange = new ExchangeOption(v1, v2, null, null, curCost);

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

				//get the change in distance for v1
				double newDistV1 = distV1 - vrp.distance(cV1Pred, cV1) - vrp.distance(cV1, cV1Succ)
						+ vrp.distance(cV1Pred, cV2) + vrp.distance(cV2, cV1Succ);

				//get the change in distance for v2
				double newDistV2 = distV2 - vrp.distance(cV2Pred, cV2) - vrp.distance(cV2, cV2Succ)
						+ vrp.distance(cV2Pred, cV1) + vrp.distance(cV1, cV2Succ);

				//calculate new cost
				double newCost = newDistV1 * v1.costOfUse + newDistV2 * v2.costOfUse;

				if(newCost < bestExchange.getDelta()) {
					bestExchange = new ExchangeOption(v1,v2, cV1, cV2, newCost);
				}

				//move on to the next customer of vehicle two
				cV2 = cV2.succ;
			}

			//move on to the next vehicle of customer one
			cV1 = cV1.succ;
		}

		return null;
	}
	/**
	 * Find customer who's moving to another vehicle would have the highest benefit
	 * @param vFrom Vehicle, vehicle from which a customer is to be taken
	 * @param vTo Vehicle, vehicle to which a customer is to be moved
	 * @return RelocationOption, the best option for moving a customer from vFrom to vTo
	 */
	public RelocateOption findBestCustomer(Vehicle vFrom, Vehicle vTo) {


		//create an empty move with no benefit
		//thus prevent the moving of one customer to another vehicle if there would be no benefit
		RelocateOption bestToMove = new RelocateOption(null,0, vFrom, vTo);

		//start checking from the first customer, who is not the depot-connection
		Customer cFrom = vFrom.firstCustomer.succ;
		while(!cFrom.equals(vFrom.lastCustomer)) {

			//check if the customer fits into the vehicle
			if(vTo.canAccomodate(cFrom)) {

				//determine how the distance of vFrom would change
				double changeVFrom = vrp.distance(cFrom.pred, cFrom.succ) 
						- vrp.distance(cFrom.pred, cFrom)
						- vrp.distance(cFrom, cFrom.succ);


				Customer cToPred = vTo.firstCustomer;
				Customer cToSucc = cToPred.succ;
				while(!cToPred.equals(vTo.lastCustomer)) {
					//a customer can not be inserted before/after himself
					if(!(cFrom.equals(cToPred)||cFrom.equals(cToSucc))) {
						if(cFrom.canBeInsertedBetween(cToPred, cToSucc)) {
							//determine how the distance of vTo would change
							double changeVTo =  - vrp.distance(cToPred, cToSucc)
									+ vrp.distance(cToPred, cFrom)
									+ vrp.distance(cFrom, cToSucc);


							//TODO Why do I not need to consider the cost, but only the distance?
							//the change in distance, if this move was to be made
							double resultingChange = changeVFrom + changeVTo;


							//if this move is cheaper, take it up
							if(resultingChange < bestToMove.getDelta()) {
								bestToMove = new RelocateOption(cFrom,resultingChange,vFrom,vTo);
								bestToMove.cPred = cToPred;
								bestToMove.cSucc = cToSucc;

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
	 * Runs steepest descent, to find a solution for the vrp-instance
	 */
	public void solve() {

		//create best-move-matrix and print it to the console
		createBMM();
		printBMM();
		System.out.println(" ");

		//find the first best move
		RelocateOption relocate = findBestMove();

		int iterationCounter = 0;

		//As long as there are improving moves execute them
		while(relocate.getDelta() < 0) {

			//Visualize the relocation on the console
			iterationCounter++;
			System.out.println(iterationCounter);
			//printBMM();
			System.out.print("vFrom - before move: ");
			relocate.getVehicleFrom().show();
			System.out.print("vTo - before move: ");
			relocate.getVehicleTo().show();
			relocate.printOption();

			//relocate the customer
			executeRelocation(relocate);

			//Visualize the relocation on the console
			System.out.print("vFrom - after move: ");
			relocate.getVehicleFrom().show();
			System.out.print("vTo - after move: ");
			relocate.getVehicleTo().show();
			System.out.println(" ");

			//after each move update the matrix and find the next move
			updateBMM(relocate.getVehicleFrom(), relocate.getVehicleTo());
			relocate = findBestMove();
		}
		//print the last BMM
		printBMM();
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% \n");
		printResultsToConsole();
		printResultsToFile();

	}


	/**
	 * Find the best move in the Matrix of possible moves
	 * @return RelocateOption, the currently best move in the BMM
	 */
	public RelocateOption findBestMove() {
		//start comparing with the origin of the matrix
		RelocateOption bestMove = bestMoveMatrix[0][0];
		double minCost = bestMove.getDelta();

		RelocateOption currentMove = bestMove;
		//go through the matrix and find the move with minimal cost
		for(int i = 0; i < numCustomers; i++ ) {
			for(int j = 0; j < numCustomers; j++) {
				currentMove = bestMoveMatrix[i][j];
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
	 * @param bestR RelocateOperation, option that is supposed to be executed 
	 */
	public void executeRelocation(RelocateOption bestR) {
		//get the customer which is to be moved
		Customer cRelocate = bestR.getCToMove();

		//Try to insert the customer into the new vehicle
		//if(bestR.getVehicleTo().insertBetween(cRelocate, bestR.cPred, bestR.cSucc)) {
		//remove customer from current vehicle if it exists
		if(bestR.getVehicleFrom().remove(cRelocate)) {
			bestR.getVehicleTo().insertBetween(cRelocate, bestR.cPred, bestR.cSucc);
		}

		//}



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
			bestMoveMatrix[vFrom.index][i] = findBestCustomer(vFrom, vCheck);
			bestMoveMatrix[i][vFrom.index] = findBestCustomer(vCheck,vFrom);

			//recalculate the giving and receiving of the second vehicle
			bestMoveMatrix[i][vTo.index] = findBestCustomer(vCheck, vTo);
			bestMoveMatrix[vTo.index][i] = findBestCustomer(vFrom, vCheck);
		}
	}


	/**
	 * executes a two-opt move for a vehicle if possible
	 * @param v Vehicle, the vehicle which is to be checked for crossings
	 * @return boolean, whether or not an optimization took place
	 */
	public boolean twoOpt(Vehicle v) {
		Customer c1 = v.firstCustomer;
		Customer c2 = c1.succ;
		//go through all routes
		while(!c2.equals(v.lastCustomer)) {


			Customer c3 = c2;
			Customer c4 = c3.succ;
			//compare each route with all following routes
			while(!c3.equals(v.lastCustomer)) {

				//check if the routes cross
				if(lineCollision(c1, c2, c3, c4)) {
					//check time window
					System.out.println(testReverse(v, c3, c2));
				}

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
	 * Test if the reversing of the route between two customers would yield a cost-improvement
	 * @param v Vehicle, the vehicle which drives the route
	 * @param nS Customer, the customer which is the new start for the reversal
	 * @param nE Customer, the customer which is the new end for the reversal
	 * @return boolean, true, if reversing is possible and improves the cost, false otherwise
	 */
	public boolean testReverse(Vehicle v, Customer newStart, Customer newEnd) {

		Customer nS = newStart.copy();
		Customer nE = newEnd.copy();

		//create a new vehicle based on the passed vehicle
		Vehicle testV = v.copy();

		System.out.println("Reverse vehicle: ");
		testV.show();

		//find new start and new end in the new vehicle
		Customer current = testV.firstCustomer;
		while(!current.equals(testV.lastCustomer)) {
			if(current.custNo == nS.custNo) {
				nS = current;
				nS.succ = current.succ;
				nS.pred = current.pred;
			}
			if(current.custNo == nE.custNo) {
				nE = current;
				nE.succ = current.succ;
				nE.pred = current.pred;
			}
			current = current.succ;
		}

		Customer last = nS.succ;
		Customer limit = nE.pred;
		ArrayList<Customer> customers = new ArrayList<Customer>();


		//read the customers which are to be reversed in reversed order
		Customer cCur = nS;
		while(!cCur.equals(limit)) {
			customers.add(cCur);

			//move on to the next customer
			cCur = cCur.pred;

			//remove the customer of this visit
			testV.remove(cCur.succ);

		}

		//show the new vehicle and the customers which were reversed
		testV.show();
		for(Customer c : customers) {
			System.out.print(c.custNo + " ");
		}

		Customer cPred = limit;
		for(Customer c : customers) {
			if(c.canBeInsertedBetween(cPred, last)) {
				testV.insertBetween(c, cPred, last);
			}
			else {
				System.out.println("Time window violation");
				return false;
			}
			cPred = c;

		}

		if(testV.cost < v.cost) {
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

	//TODO Exchange Matrix goes here


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
		stDesc.solve();

		//		stDesc.createExchangeMatrix();
		//TestSolution.runTest(vrp, stDesc.getTotalCost(), stDesc.getVehicles());
		DisplayVRP dVRP = new DisplayVRP(in, num, args[2]);
		dVRP.plotVRPSolution();

		for(Vehicle v : stDesc.getVehicles()) {
			stDesc.twoOpt(v);
		}

	}
}
