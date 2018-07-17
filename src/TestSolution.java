import java.io.IOException;
import java.util.ArrayList;

/**
 * A class to verify the validity of a solution for an instance of the CVRPTW
 * @author Tom Decke
 *
 */
public class TestSolution {
	private static final double DERIVATION = 1E-10;

	/**
	 * Runs the test for the solution
	 * @param vrp VRP, the given problem
	 * @param solDist double, the proposed solution (distance)
	 * @param vehicles ArrayList<Vehicle>, the proposed solution (vehicle routes)
	 * @return boolean, whether or not the solution is valid
	 */
	public static boolean runTest(VRP vrpIn, double solDistIn, ArrayList<Vehicle> vehicles) {

		//create variables for the input
		VRP vrp = vrpIn;
		double solDist = solDistIn;
		ArrayList<Vehicle> solVehicles = vehicles;
		int[] cVisited = new int[vrp.customer.length] ;		

		//distance of all vehicles
		double totalDist = 0; 

		//Check all vehicles/routes
		for(Vehicle v : solVehicles) {
			//check the vehicle
			if(v.id > vrp.m) {
				System.out.println("Vehicle does not belong to VRP");
				return false;
			}
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

				//check if the target of travel belongs to the vrp
				if(cSucc.custNo > vrp.n) {
					System.out.println("Customer does not belong to the VRP.");
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

		//check if all customers have been visited exactly once
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
		//allow minor derivation, which might be due to computational errors
		if(Math.abs(totalDist - solDist) > DERIVATION) {
			System.out.println("Wrong distance. The distance of the solutions differ by: "+Math.abs(totalDist - solDist));
			return false;
		}
		System.out.println("Total distance: " + totalDist);

		//if all tests were passed, the solution is valid
		System.out.println("The solution is valid!");
		return true;
	}

	/**
	 * Main method for testing
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		//get the input
		String fileIn = args[0];
		int numCustomer = Integer.parseInt(args[1]);

		//create verification instance and solver
		VRP vrp = new VRP(fileIn,numCustomer);

		System.out.print(String.format("%s", "\\|"));
		for(Customer c : vrp.customer) {
			System.out.print(String.format("%5d|", c.custNo));
		}
		System.out.println("");
		for(Customer c : vrp.customer) {
			System.out.print(String.format("%d|", c.custNo));
			for(Customer c2 : vrp.customer) {
				System.out.print(String.format("%5.2f|", vrp.distance(c,c2)));
			}	
			System.out.println("");
		}

		//get customers
		Customer c1 = vrp.customer[1];
		Customer c2 = vrp.customer[2];
		Customer c3 = vrp.customer[3];
		Customer c4 = vrp.customer[4];
		Customer c5 = vrp.customer[5];

		//create unknown customer 
		Customer cX = new Customer(7,48,48, 20,666,666,5);
		cX.earliestStart = 666;
		cX.latestStart = 666;

		//get vehicles
		Vehicle v1 = vrp.vehicle[0];
		Vehicle v2 = vrp.vehicle[1];
		Vehicle v3 = vrp.vehicle[2];
		Vehicle v4 = vrp.vehicle[3];
		Vehicle v5 = vrp.vehicle[4];

		//create unknown vehicle
		Vehicle vX = new Vehicle(vrp, 6, 30, 1, vrp.depot);

		//create array-list
		ArrayList<Vehicle> testV = new ArrayList<Vehicle>();

		double dist = 0;

		//Test unknown customer
		//		cX.vehicle = v3;
		//		cX.succ = v3.lastCustomer;
		//		v3.lastCustomer.pred = cX;
		//		c3.succ = cX;
		//		cX.pred = c3;
		//		testV.add(v3);


		//TODO Test customer revisit
		//		c3.succ = cX;
		//		cX.pred = c3;
		//		cX.succ = v3.lastCustomer;
		//		v3.lastCustomer.pred = cX;
		//		cX.custNo = 3;
		//		cX.vehicle = v3;
		//		testV.add(v3);
		//		testV.add(v2);
		//		testV.add(v1);
		//		testV.add(v4);
		//		testV.add(v5);
		//		v3.show();


		//Test customer in multiple vehicles
		//		v5.insertBetween(c1, c5, v5.lastCustomer);
		//		testV.add(v1);
		//		testV.add(v5);

		//Test omit customers
		//		testV.add(v1);
		//		testV.add(v2);

		//Test overloading a vehicle
		//		v2.insertBetween(c1, c2, v2.lastCustomer);
		//		testV.add(v2);

		//Test time constraint violation
		//		v1.insertBetween(c5, c1, v1.lastCustomer);
		//		testV.add(v1);

		//Test non-existing vehicle
		//		testV.add(vX);

		//Test wrong distance proposal
		//		dist = -1;
		//		testV.add(v1);
		//		testV.add(v2);
		//		testV.add(v3);
		//		testV.add(v4);
		//		testV.add(v5);

		TestSolution.runTest(vrp, dist, testV);


	}


}
