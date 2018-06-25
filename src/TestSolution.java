import java.util.ArrayList;

/**
 * A class to verify the validity of a solution for an instance of the CVRPTW
 * @author Tom Decke
 *
 */
//TODO would it make sense to make this static?
public class TestSolution {
	
	//Allowed derivation between solution and recomputation
	private final int DERIVATION = 1;
	
	private VRP vrp;
	private double solDist;
	private ArrayList<Vehicle> solVehicles;
	private boolean[] cVisited;
	
	/**
	 * Constructor to create 
	 * @param vrp
	 * @param solDist
	 * @param vehicles
	 */
	public TestSolution(VRP vrp, double solDist, ArrayList<Vehicle> vehicles) {
		this.vrp = vrp;
		this.solDist = solDist;
		this.solVehicles = vehicles;
		cVisited = new boolean[vrp.customer.length];
	}

	/**
	 * Runs the test for the solution
	 * @return boolean, whether or not the solution is valid
	 */
	public boolean runTest() {
		double totalDist = 0;
		double vehicleDist = 0;
		
		//Check all vehicles
		for(Vehicle v : solVehicles) {
			Customer cCur = v.firstCustomer;
			Customer cSucc = cCur.succ;
			
			//initialise leave to zero, as the vehicle starts from the depot at time 0 
			double leave = 0;
			
			//Check the route of the vehicle
			while(!cCur.equals(v.lastCustomer)) {
				double distTraveled = vrp.distance(cCur, cSucc);
				double arrival = distTraveled + leave;
				double start = arrival;
				
				//check if the vehicle arrives after the due date of the customer
				if(arrival > cSucc.dueDate) {
					return false;
				}
				
				//if the vehicle arrives before the ready time, wait for the customer to be ready
				if(arrival < cSucc.readyTime) {
					start = cSucc.readyTime;
				}
				
				//calculate the time at which the car leaves the customer
				leave = start + cCur.serviceTime;
				
				//mark the customer as visited
				cVisited[cCur.custNo] = true;
				
				//add the distance to the overall distance
				vehicleDist += distTraveled;
				
				//prepare to move to the next customer
				cCur = cSucc;
				cSucc = cCur.succ;
			}
			//sum up the total cost of the solution for later comparison
			totalDist += vehicleDist * v.costOfUse;
		}
		
		//check if all customers have been visited
		for(boolean b : cVisited) {
			if(!b) {
				return false;
			}
		}
		
		//TODO re-think
		//check for derivation between solution and control
		//allow minor derivation which might come from computational inaccuracy
		if(Math.abs(totalDist - solDist) > DERIVATION) {
			System.out.println("Derivation "+Math.abs(totalDist - solDist));
			return false;
		}
		
		//if all tests were passed, the solution is valid
		return true;
	}

}
