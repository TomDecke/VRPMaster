import java.util.ArrayList;

public class RandomSolution {

	private double cost;
	private int neededV;
	private ArrayList<Vehicle> soln;
	
	//TODO do I need to memorise in which vehicle the customers were? I.e since they are all identical
	public RandomSolution(double cost, int needed, ArrayList<Vehicle> v) {
		this.cost = cost;
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
