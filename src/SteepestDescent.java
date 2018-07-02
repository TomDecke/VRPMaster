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
	private final double EPSILON = 1E-8;
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

//		System.out.println("v"+v0.id +" + v"+v1.id + " = "+v0.cost + v1.cost);
		//create an empty move with the current cost of the vehicles
		//thus prevent the moving of one customer to another vehicle if there would be no benefit
		RelocateOption bestToMove = new RelocateOption(null, v0.cost + v1.cost, v0, v1);
		
		//start checking from the first customer, who is not the depot-connection
		Customer current = v0.firstCustomer.succ;
		while(!current.equals(v0.lastCustomer)) {

			//find the best place to insert the customer in the other tour
			Customer insertAfter = v1.findBestPosition(current);
			
			//if insertAfter is not null current has a position into which it can be inserted
			if(insertAfter != null) {
				//determine how the total distance of v0 would change
				double newDistV0 = v0.getDistance() + vrp.distance(current.pred, current.succ) 
				- vrp.distance(current.pred, current)
				- vrp.distance(current, current.succ);

				//determine how the total distance of v1 would change
				double newDistV1 = v1.getDistance() - vrp.distance(insertAfter, insertAfter.succ)
				+ vrp.distance(insertAfter, current)
				+ vrp.distance(current, insertAfter.succ);

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
					bestMove = currentMove;
				}
			}
		}

		return bestMove;
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
	 * Executes the relocation of a customer
	 * @param bestRelocation RelocateOperation, option that is supposed to be executed 
	 */
	public void executeRelocation(RelocateOption bestRelocation) {
		//get the customer which is to be moved
		Customer cRelocate = bestRelocation.getCToMove();
		
		//remove customer from current vehicle if it exists
		if(bestRelocation.getVehicleFrom().remove(cRelocate)) {
			//insert customer into new vehicle
			bestRelocation.getVehicleTo().minCostInsertion(cRelocate);
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
	 * executes a two-opt move for a vehicle if possible
	 * @param v Vehicle, the vehicle which is to be checked for crossings
	 * @return boolean, whether or not an optimization took place
	 */
	public boolean twoOpt(Vehicle v) {
		double cCost = v.cost;
		Customer c1 = v.firstCustomer;
		Customer c2 = c1.succ;
		//go through all routes
		while(!c2.equals(v.lastCustomer)) {
			
			
			Customer c3 = c2;
			Customer c4 = c3.succ;
			//compare each route with all following routes
			while(!c4.equals(v.lastCustomer)) {
				
				//check if the routes cross
				if(lineCollision(c1, c2, c3, c4)) {
					//check time window
					
					//TODO figure out the exchange
					double newCost = cCost - vrp.distance(c1, c2) - vrp.distance(c3, c4)
							+ vrp.distance(c1, c3) + vrp.distance(c2, c4);
					// - c1c2 -c3c4
					// + c1c3 + c2c4 || + c1c4 + c2c3
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
	
	public boolean testReverse(Vehicle v, Customer newStart, Customer newEnd) {
		//create a new vehicle based on the passed vehicle
		Vehicle testV = new Vehicle(v.vrp, 0, v.capacity, v.costOfUse, this.vrp.depot);
		
		Customer cCur = v.firstCustomer.succ;
		while(!cCur.equals(newStart)) {
			
		}
		
		
		return false;
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
	 * After executing @see solve(), this method can be used to show the number of needed vehicles and the total cost
	 */
	public void printResultsToConsole() {
		

		System.out.println("NV: "+ this.getVehicleCount());
		System.out.println("Distance: " + vrp.calcTotalCost());
		System.out.println(" ");
	}
	
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
	
	}
}
