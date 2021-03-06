package solver;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import moves.Option;
import operators.Operation;
import representation.Customer;
import representation.VRP;
import representation.Vehicle;

/**
 * Super class to accommodate the basic functionality for optimization-processes, i.e. solvers.
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
		this.numCustomers = vrp.getN();
		this.fOut = fOut;
	}


	/**
	 * Execute the descent to find a solution to the VRP-instance
	 * @param ops ArrayList<Operation> the improvement moves which should be used
	 * @param random boolean, whether or not the solution should use a randomly selected improving move
	 */
	public abstract void solve(ArrayList<Operation> ops, boolean random);

	/**
	 * After executing @see solve(), this method can be used to obtain the vehicles, which are present in the solution 
	 * @return ArrayList<Vehicle>, list of vehicles with customers
	 */
	public ArrayList<Vehicle> getVehicles(){
		ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
		for(int i = 0 ; i<numCustomers; i++) {
			Vehicle v = vrp.getVehicle()[i];
			//check if there are still customers in between the dummies
			if(!v.getFirstCustomer().getSucc().equals(v.getLastCustomer())) {
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
			writer.write(""+vrp.getM() +" "+this.getVehicleCount()+"\n");

			//write the customers of each vehicle as a route
			for(Vehicle v : getVehicles()) {
				StringBuilder sBuild = new StringBuilder();
				Customer customer = v.getFirstCustomer().getSucc();
				while (!customer.equals(v.getLastCustomer())){
					sBuild.append(customer.getCustNo() + " ");
					customer = customer.getSucc();
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
	public int getVehicleCount() {
		int vehicleCount = 0; //number of vehicles needed in the solution
		for(int i = 0 ; i<numCustomers; i++) {
			Vehicle v = vrp.getVehicle()[i];
			//check if there are still customers in between the dummies
			if(!v.getFirstCustomer().getSucc().equals(v.getLastCustomer())) {
				v.show();
				System.out.println("Distance Vehicle: " + v.getCost());
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
