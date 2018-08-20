package addOns;
import java.io.*;
import java.util.ArrayList;

import representation.Customer;
import representation.Vehicle;

/**
 * Class to keep track of the best solution when using random operations
 * @author Tom Decke
 *
 */
public class RandomSolution {

	private double cost;
	private int neededV;
	private int availableV;
	private ArrayList<Vehicle> soln;

	/**
	 * Constructor for the solution
	 * @param cost double, the cost of the solution
	 * @param needed int, the number of proposed vehicles
	 * @param available int, the number of available vehicles
	 * @param v ArrayList<Vehicle>, the vehicles for the solution
	 */
	public RandomSolution(double cost, int needed, int available, ArrayList<Vehicle> v) {
		this.cost = cost;
		this.availableV = available;
		this.neededV = needed;
		this.availableV = available;
		this.soln = v;
	}

	/**
	 * Accessor for the cost of the solution
	 * @return double, the cost
	 */
	public double getCost() {
		return cost;
	}

	/**
	 * Accessor for the vehicles needed by the solution
	 * @return int, number of needed vehicles
	 */
	public int getNeededV() {
		return neededV;
	}

	/**
	 * Accessor for the solution
	 * @return ArrayList<Vehicle>, the vehicles of the solution
	 */
	public ArrayList<Vehicle> getSoln() {
		return soln;
	}

	/**
	 * Write the solution to a file
	 * @param fOut, the name for the output-file
	 */
	public void writeSolutionToFile(String fOut) {
		//create a writer
		FileWriter writer;

		try {
			writer 	= new FileWriter(fOut);
			//write the cost of the solution
			writer.write(""+availableV +" "+neededV+"\n");

			//write the customers of each vehicle as a route
			for(Vehicle v : soln) {
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
			writer.write("total cost: "+cost);
			writer.close();
		}catch(IOException ioe) {
			System.out.println("Error whilst writing");
		}
	}
}
