import java.util.ArrayList;

/**
 * A class to verify the validity of a solution for an instance of the CVRPTW
 * @author Tom Decke
 *
 */
public class TestSolution {
	private static final double DERIVATION = 1E-10;

	private VRP vrp;
	private double solDist;
	private ArrayList<Vehicle> solVehicles;
	private int[] cVisited;

	/**
	 * Constructor to create a test instance
	 * @param vrp VRP, the given problem
	 * @param solDist double, the proposed solution (distance)
	 * @param vehicles ArrayList<Vehicle>, the proposed solution (vehicle routes)
	 */
	public TestSolution(VRP vrp, double solDist, ArrayList<Vehicle> vehicles) {
		this.vrp = vrp;
		this.solDist = solDist;
		this.solVehicles = vehicles;
		cVisited = new int[vrp.customer.length];
	}

	/**
	 * Runs the test for the solution
	 * @return boolean, whether or not the solution is valid
	 */
	public boolean runTest() {
		

		//distance of all vehicles
		double totalDist = 0; 

		//Check all vehicles/routes
		for(Vehicle v : solVehicles) {
			Customer cCur = v.firstCustomer;
			Customer cSucc = cCur.succ;

			//initialise leave-time to zero, as the vehicle starts from the depot at time 0 
			double leave = 0;

			//set carried load for the vehicle to zero for capacity check
			int carriedLoad = 0;

			//set distance traveled of a single vehicle for distance check
			double vehicleDist = 0;

			//print vehicle route and cost 
			v.show();
			System.out.println("cost: "+v.cost);

			//Check the route of the vehicle
			while(!cCur.equals(v.lastCustomer)) {
				
				//check if the customer actually is in the vehicle
				if(!v.equals(cCur.vehicle)) {
					System.out.println(cCur);
					System.out.println(v);
					System.out.println("Customer in wrong vehicle");
					return false;
				}

				//compute and print the distance between the current customers
				double distTraveled = vrp.distance(cCur, cSucc);
				System.out.println("Distance from " + cCur.custNo + " to "+ cSucc.custNo +": "+ distTraveled);

				//add the distance to the overall distance and show the intermediate distance
				vehicleDist += distTraveled;
				System.out.println(vehicleDist);

				//the time of travel and the time of leave(pred) determine the arrival at succ
				double arrival = distTraveled + leave;

				//check if the vehicle arrives after the due date of the customer
				if(arrival > cSucc.dueDate) {
					System.out.println("Violated time window constraint");
					return false;
				}


				//if the vehicle arrives before the ready time, wait for the customer to be ready
				double start = arrival;
				if(arrival < cSucc.readyTime) {
					start = cSucc.readyTime;
				}

				//calculate the time at which the car leaves the customer(pred)
				leave = start + cCur.serviceTime;

				//mark the customer as visited
				cVisited[cCur.custNo] += 1;

				//increase the load of the vehicle
				carriedLoad += cCur.demand;

				//prepare to move to the next customer
				cCur = cSucc;
				cSucc = cCur.succ;
			}

			//make sure the vehicle does not carry more goods than it has capacity
			if(carriedLoad > v.capacity) {
				System.out.println("Violated capacity constraint");
				return false;
			}


			//sum up the total cost of the solution for later comparison
			totalDist += vehicleDist * v.costOfUse;
			System.out.println("Total distance so far: " + totalDist);
			System.out.println("");
		}

		//check if all customers have been visited
		for (int i = 0; i < cVisited.length ; i++) {
			int visits = cVisited[i];
			
			if(visits == 0) {
				System.out.println("Did not visit all customers");
				return false;
			}else if(visits > 1 && i!=0) {
				System.out.println("Re-visited a customer");
				return false;
			}
		}

		//check for derivation between solution and control
		if(Math.abs(totalDist - solDist) > DERIVATION) {
			System.out.println("The distance of the solutions differ by: "+Math.abs(totalDist - solDist));
			return false;
		}
		System.out.println("Total distance: " + totalDist);

		//if all tests were passed, the solution is valid
		return true;
	}

}
