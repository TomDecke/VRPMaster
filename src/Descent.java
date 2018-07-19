import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Super class to accomodate the basic functionality for optimization-processes.
 * @author Tom Decke
 *
 */
public abstract class Descent {
	/**name of the output file*/
	protected String fOut;
	/**problem instance*/
	protected VRP vrp;
	/**customers in the problem instance*/
	protected int numCustomers;

	/**
	 * Constructor for the descent
	 * @param vrp VRP, the problem instance
	 * @param fOut String, the name of the file to which the result should be printed
	 */
	public Descent(VRP vrp, String fOut) {
		this.vrp = vrp;
		this.numCustomers = vrp.n;
		this.fOut = fOut;
	}


	/**
	 * Execute the descent to find a solution to the VRP-instance
	 * @param mode int, the mode that determines which combination of operators is used
	 */
	public abstract void solve(int mode);
	
	public abstract void solve(ArrayList<Operation> ops, boolean random);

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
	 * After executing @see solve(), this method can be used to show the number of needed vehicles and the total cost
	 */
	public void printResultsToConsole() {
		System.out.println("NV: "+ this.getVehicleCount());
		System.out.println("Distance: " + vrp.calcTotalCost());
		System.out.println(" ");
	}

	/**
	 * Executes an improvement option
	 * @param o Option, the option to execute
	 */
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
			writer 	= new FileWriter(fOut);
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
	protected int getVehicleCount() {
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
	 * Accessor for the output-file-name
	 * @return String, the name of the output-file
	 */
	public String getFOut() {
		return this.fOut;
	}

	/**
	 * Accessor for the number of customers in the VRP
	 * @return int, the number of customers
	 */
	public int getNumCustomers() {
		return numCustomers;
	}
}
