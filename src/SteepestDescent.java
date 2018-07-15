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
	private TwoOptOperation to;



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
		this.to = new TwoOptOperation(vrp, customers);
	}

	/**
	 * Runs steepest descent, to find a solution for the vrp-instance
	 */
	public void solve(int mode) {

		//create best-move-matrix and print it to the console
		ro.createOptionMatrix();
		eo.createOptionMatrix();
		to.createOptionMatrix();
		
		
		
		ro.printRelocateMatrix();
		System.out.println(" ");

		//find the first best move
		Option execute = ro.fetchBestOption();

		//set up exchange
		Option optionExchange = eo.fetchBestOption();
		Option optionTwoOpt = to.fetchBestOption();

		//get the vehicles
		Vehicle v1 = execute.getV1();
		Vehicle v2 = execute.getV2();


		int iterationCounter = 0;
		//As long as there are improving moves execute them
		while(execute.getDelta() < 0) {

			//Visualize the state before the relocation on the console
			iterationCounter++;
			System.out.println(iterationCounter);
			//uncomment if wanted printBMM();
			System.out.print("v1 - before move: ");
			v1.show();
			System.out.print("v2 - before move: ");
			v2.show();
			execute.printOption();
			

			executeMove(execute);


			//Visualize the state after the relocation on the console
			System.out.print("v1 - after move: ");
			v1.show();
			System.out.print("v2 - after move: ");
			v2.show();
			System.out.println(" ");

			//update relocate and find best move for comparison
			ro.updateOptionMatrix(v1,v2);
			execute = ro.fetchBestOption();
			
			//combine different moves
			switch(mode) {
			//only relocate
			case 0:
				break;
				
			//relocate and exchange
			case 1:
				//TODO figure out eo-update
				//update exchange and compare option with the current result
				eo.createOptionMatrix();
				optionExchange = eo.fetchBestOption();
				if(optionExchange.getDelta() < execute.getDelta()) {
					execute = optionExchange;
				}
				break;
				
			//relocate and two-opt
			case 2:
				//update two-opt and compare option with current result
				to.updateOptionMatrix(v1,v2);
				optionTwoOpt = to.fetchBestOption();
				if(optionTwoOpt.getDelta() < execute.getDelta()) {
					execute = optionTwoOpt;
				}
				break;
				
			//relocate, exchange and two-opt
			case 3:
				//update exchange and two-opt
				eo.updateOptionMatrix(v1, v2);
				to.updateOptionMatrix(v1, v2);
				//find the best option
				if(optionExchange.getDelta() < execute.getDelta()) {
					execute = optionExchange;
				}
				if(optionTwoOpt.getDelta() < execute.getDelta()) {
					execute = optionTwoOpt;
				}
				break;
				
			//randomly selected improvement move
			case 4: 
				
				//update all matrices
				eo.updateOptionMatrix(v1,v2);
	

				break;

			}

			//update the vehicles
			v1 = execute.getV1();
			v2 = execute.getV2();
			
			
		}
		//print the last BMM
		ro.printRelocateMatrix();
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% \n");


		
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


		VRP vrp = stDesc.vrp;
		stDesc.solve(2);

		TwoOptOperation two = new TwoOptOperation(vrp, num);

		
//		TestSolution.runTest(stDesc.vrp, stDesc.getTotalCost(), stDesc.getVehicles());
		DisplayVRP dVRP = new DisplayVRP(in, num, args[2]);
		dVRP.plotVRPSolution();
	}
}
