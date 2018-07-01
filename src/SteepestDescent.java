import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


/**
 * 
 * @author Tom Decke
 *
 */
public class SteepestDescent {
	private final int PENALTY = 10000;
	private String out;
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
	public RelocateOption findBestCustomer(Vehicle vFrom, Vehicle vTo) {


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
				if(newDistVFrom < 1E-10) {
					newDistVFrom = 0;
				}
				
				Customer cToPred = vTo.firstCustomer;
				Customer cToSucc = cToPred.succ;
				while(!cToPred.equals(vTo.lastCustomer)) {
					if(cFrom.canBeInsertedBetween(cToPred, cToSucc)) {
						//determine how the total distance of vTo would change
						double newDistVTo = vTo.getDistance() - vrp.distance(cToPred, cToSucc)
								+ vrp.distance(cToPred, cFrom)
								+ vrp.distance(cFrom, cToSucc);


						if(newDistVTo < 1E-10) {
							newDistVTo = 0;
						}

						//the change in cost, if this move was to be made
						double resultingCost = newDistVFrom * vFrom.costOfUse + newDistVTo * vTo.costOfUse;


						//if this move is cheaper, take it up
						if(resultingCost < bestToMove.getCostOfMove()) {
							bestToMove = new RelocateOption(cFrom,resultingCost,vFrom,vTo);
							bestToMove.cPred = cToPred;
							bestToMove.cSucc = cToSucc;

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

		//if there is no customer who's movement could lead to optimisation, penalise the move
		if(bestToMove.getCToMove() == null) {
			bestToMove.setCostOfMove(PENALTY);
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
		while(relocate.getCostOfMove() < PENALTY) {

			//Visualize the relocation on the console
			iterationCounter++;
			System.out.println(iterationCounter);
			printBMM();
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
	 * Executes the relocation of a customer
	 * @param bR RelocateOperation, option that is supposed to be executed 
	 */
	public void executeRelocation(RelocateOption bR) {
		//get the customer which is to be moved
		Customer cRelocate = bR.getCToMove();

		//remove customer from current vehicle if it exists
		if(bR.getVehicleFrom().remove(cRelocate)) {
			//insert customer into new vehicle
			bR.getVehicleTo().insertBetween(bR.getCToMove(), bR.cPred, bR.cSucc);
		}
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
		VRP stVRP = stDesc.getVRP();
		System.out.println(2*(stVRP.distance(stVRP.depot, stVRP.customer[8])));
		VRP vrp = new VRP (in,num);
		stDesc.solve();
		TestSolution.runTest(vrp, stDesc.getTotalCost(), stDesc.getVehicles());
		DisplayVRP dVRP = new DisplayVRP(in, num, args[2]);
		dVRP.plotVRPSolution();
	}
}
