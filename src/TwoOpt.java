import java.util.ArrayList;

public class TwoOpt {

	private static final double EPSILON = 1E-10;
	

	/**
	 * Executes a two-opt move for a vehicle if possible
	 * @param v Vehicle, the vehicle which is to be checked for crossings
	 * @return boolean, whether or not an optimization took place
	 */
	public static boolean twoOpt(Vehicle v) {
		//get the first route of the vehicle
		Customer c1 = v.firstCustomer;
		Customer c2 = c1.succ;
		//go through all routes

		while(!c2.equals(v.lastCustomer)) {

			//get the succeeding route
			Customer c3 = c2;
			Customer c4 = c3.succ;
			//compare each route with all following routes
			while(!c3.equals(v.lastCustomer)) {

				//check if the routes cross
				if(lineCollision(c1, c2, c3, c4)) {
					System.out.println("Collision");
					//try the reversion on a copy of the data to check time windows and cost constraint
					if(reverseRoute(v.copy(), c3.copy(), c2.copy())) {
						//if the reversion is possible, execute it
						reverseRoute(v, c3, c2);
					}
				}
				//move to the following route
				c3 = c4;
				c4 = c4.succ;
			}
			//move to the next route
			c1 = c2;
			c2 = c2.succ;
		}
		//no crossing occurred, thus 
		return false;
	}

	/**
	 * Reverse the route between two customers
	 * @param v Vehicle, the vehicle which drives the route
	 * @param newStart Customer, the customer which is the new start for the reversal
	 * @param newEnd Customer, the customer which is the new end for the reversal
	 * @return boolean, true, if reversing is possible and improves the cost, false otherwise
	 */
	public static boolean reverseRoute(Vehicle v, Customer newStart, Customer newEnd) {
		

		System.out.println("Reverse vehicle: ");
		v.show();

		double preCost = v.cost;

		Customer last = newStart.succ;
		Customer limit = newEnd.pred;
		ArrayList<Customer> customers = new ArrayList<Customer>();


		//read the customers which are to be reversed in reversed order
		Customer cCur = newStart;
		while(!cCur.equals(limit)) {
			customers.add(cCur);

			//move on to the next customer
			cCur = cCur.pred;

			//TODO find bug
			//remove the customer of this visit
			v.remove(cCur.succ);

		}

		//display the customers that were taken out
		v.show();
		for(Customer c : customers) {
			System.out.println(c.custNo);
		}

		//try to insert the customers that were taken back into the route
		Customer cPred = limit;
		for(Customer c : customers) {
			if(c.canBeInsertedBetween(cPred, last)) {
				v.insertBetween(c, cPred, last);
			}
			else {
				System.out.println("Time window violation");
				return false;
			}
			cPred = c;
		}

		//check if the cost of the vehicle has decreased
		if(v.cost < preCost) {
			return true;
		}
		else {
			System.out.println("No cost improvement");
			return false;
		}
	}
	
	
	/**
	 * Determine if the routes from c1c2 and c3c4 cross each other
	 * the solution is taken from 
	 * https://www.spieleprogrammierer.de/wiki/2D-Kollisionserkennung#Kollision_zwischen_zwei_Strecken (01.07.2018)
	 * @param c1 Customer, containing the starting point of c1c2 
	 * @param c2 Customer, containing the end point of c1c2
	 * @param c3 Customer, containing the starting point of c3c4
	 * @param c4 Customer, containing the end point of c3c4
	 * @return
	 */
	public static boolean lineCollision(Customer c1, Customer c2, Customer c3, Customer c4  ) {

		//extract the coordinates of the routes
		double xC1 = c1.xCoord;
		double yC1 = c1.yCoord;
		double xC2 = c2.xCoord;
		double yC2 = c2.yCoord;
		double xC3 = c3.xCoord;
		double yC3 = c3.yCoord;
		double xC4 = c4.xCoord;
		double yC4 = c4.yCoord;

		//check if the routes are contiguous
		if((xC1 == xC3 && yC1 == yC3) || (xC1 == xC4 && yC1 == yC4)) {
			//the start point of c1c2 is identical with either c3 or c4
			return false;
		}
		else if((xC2 == xC3 && yC2 == yC3) || (xC2 == xC4 && yC2 == yC4)) {
			//the end point of c1c2 is identical with either c3 or c4
			return false;
		}

		//calculate the denominator
		double denom = (yC4-yC3) * (xC2-xC1) - (xC4-xC3) * (yC2-yC1);

		//If the solution is close to 0 the routes are parallel
		if(Math.abs(denom)<EPSILON) {
			return false;
		}

		double c1c2 = ((xC4-xC3)*(yC1-yC3) - (yC4-yC3)*(xC1-xC3))/denom;
		double c3c4 = ((xC2-xC1)*(yC1-yC3) - (yC2-yC1)*(xC1-xC3))/denom;

		//check if the crossing happens between the end points of both routes
		return (c1c2 >= 0 && c1c2 <= 1) && (c3c4 >= 0 && c3c4 <= 1);
	}
}
