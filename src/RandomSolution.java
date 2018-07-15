import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class RandomSolution {

	private double cost;
	private int availableV;
	private int neededV;
	private ArrayList<Vehicle> soln;
	
	//TODO do I need to memorise in which vehicle the customers were? I.e since they are all identical
	public RandomSolution(double cost, int needed, int available, ArrayList<Vehicle> v) {
		this.cost = cost;
		this.availableV = available;
		this.neededV = needed;
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

	public double getCost() {
		return cost;
	}

	public int getNeededV() {
		return neededV;
	}

	public ArrayList<Vehicle> getSoln() {
		return soln;
	}

	
}
