import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 
 * @author Tom Decke
 *
 */
public class SteepestDescent {
	private String out;
	private VRP vrp;
	private int numCustomers;
	private CrossExOperation ceo;
	private RelocateOperation ro;
	private ExchangeOperation eo;



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
		this.numCustomers = customers;
		this.ceo = new CrossExOperation(vrp, customers);
		this.ro = new RelocateOperation(vrp, customers);
		this.eo = new ExchangeOperation(vrp, customers);
	}

	/**
	 * Runs steepest descent, to find a solution for the vrp-instance
	 */
	public void solve_Relocate() {

		//create best-move-matrix and print it to the console
		ro.createOptionMatrix();
		eo.createOptionMatrix();

		
		
		
		ro.printRelocateMatrix();
		System.out.println(" ");

		//find the first best move
		Option execute = ro.fetchBestOption();

		//set up exchange
		Option exchange = eo.fetchBestOption();
		
		if(exchange.getDelta() < execute.getDelta()) {
			execute = exchange;
		}
		exchange.printOption();



		int iterationCounter = 0;
		//As long as there are improving moves execute them
		while(execute.getDelta() < 0) {

			//Visualize the state before the relocation on the console
			iterationCounter++;
			System.out.println(iterationCounter);
			//uncomment if wanted printBMM();
			System.out.print("vFrom - before move: ");
			execute.getV1().show();
			System.out.print("vTo - before move: ");
			execute.getV2().show();
			execute.printOption();
			

			executeMove(execute);


			//Visualize the state after the relocation on the console
			System.out.print("vFrom - after move: ");
			execute.getV1().show();
			System.out.print("vTo - after move: ");
			execute.getV2().show();
			System.out.println(" ");

			//after each move update the matrix and find the next move
			eo.updateOptionMatrix(execute.getV1(), execute.getV2());
			ro.updateOptionMatrix(execute.getV1(), execute.getV2());
			
			execute = execute.getOperation().fetchBestOption();
			
		}
		//print the last BMM
		ro.printRelocateMatrix();
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% \n");


		
		printResultsToConsole();
		printResultsToFile();
	}

	/**
	 * Solve the problem with help of the cross exchange operator
	 */
	public void solve_CrossEx() {
		
		ceo.createCrossExMatrix();
		CrossExOption crossEx = ceo.fetchBestCrossEx();
		while(crossEx.getDelta() < 0) {
			ceo.executeCrossEx(crossEx);
			ceo.updateCrossExMatrix(crossEx.getV1(), crossEx.getV2());
			crossEx = ceo.fetchBestCrossEx();
		}

		printResultsToConsole();
		printResultsToFile();
	}

	/**
	 * After executing @see solve(), this method can be used to show the number of needed vehicles and the total cost
	 */
	public void printResultsToConsole() {
		System.out.println("NV: "+ this.getVehicleCount());
		System.out.println("Distance: " + vrp.calcTotalCost());
		System.out.println(" ");
	}

	public void executeMove(Option o) {
		o.getOperation().executeOption(o);
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


		stDesc.solve_Relocate();

		

//		TestSolution.runTest(stDesc.vrp, stDesc.getTotalCost(), stDesc.getVehicles());
		DisplayVRP dVRP = new DisplayVRP(in, num, args[2]);
		dVRP.plotVRPSolution();
		
		System.out.println("Begin 2-opt");
		
//		for (Vehicle v : stDesc.getVehicles()) {
//			v.show();
//			TwoOpt.twoOpt(v);
//		}
	}
}
