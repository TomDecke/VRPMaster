import java.io.*;
import java.util.ArrayList;

public class RandomSolution {

	private double cost;
	private int neededV;
	private int availableV;
	private ArrayList<Vehicle> soln;
	
	//TODO do I need to memorise in which vehicle the customers were? I.e since they are all identical
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
	 * Compares the object to another solution and returns the better one
	 * @param rs RandomSolution, the solution with which to compare
	 * @return RandomSolution, the better solution
	 */
	public RandomSolution compare(RandomSolution rs) {
		if(rs.getCost() < this.cost) {
			return rs;
		}
		else if(rs.getCost() == this.cost) {
			if(rs.neededV < this.neededV) {
				return rs;
			}
		}
		return this;
	}

	public double getCost() {
		return cost;
	}

	public int getNeededV() {
		return neededV;
	}

	public ArrayList<Vehicle> getSoln() {
		return soln;
	}

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
				Customer customer = v.firstCustomer.succ;
				while (customer != v.lastCustomer){
					sBuild.append(customer.custNo + " ");
					customer = customer.succ;
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
