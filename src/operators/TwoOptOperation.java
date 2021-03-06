package operators;
import java.io.IOException;
import java.util.ArrayList;

import moves.Option;
import moves.TwoOptOption;
import representation.Customer;
import representation.VRP;
import representation.Vehicle;

/**
 * Class to represent the 2-opt operator
 * @author Tom Decke
 *
 */
public class TwoOptOperation implements Operation{

	private static final double EPSILON = 1E-10;

	private VRP vrp;
	private int numCustomers;
	private Option[] twoOptMatrix;


	/**
	 * Constructor for the 2-opt operation
	 * @param vrp VRP, the VRP to which the operation is to be applied
	 * @param numCustomers int, the number of customers in the VRP
	 */
	public TwoOptOperation(VRP vrp, int numCustomers) {
		this.vrp = vrp;
		this.numCustomers = numCustomers;
		this.twoOptMatrix = new Option[numCustomers];
	}

	/**
	 * Create the matrix containing the best 2-opt-options for each vehicle
	 */
	@Override
	public void createOptionMatrix() {
		for(int i = 0; i < numCustomers; i++) {
			twoOptMatrix[i] = findBestOption(vrp.getVehicle()[i], vrp.getVehicle()[i]);
		}
	}

	/**
	 * Find the new best 2-opts for vehicles that were affected by a change
	 * @param v1 Vehicle, the first vehicle that was affected by the last option
	 * @param v2 Vehicle, the second vehicle that was affected by the last option
	 */
	@Override
	public void updateOptionMatrix(Vehicle v1, Vehicle v2) {
		twoOptMatrix[v1.getIndex()] = findBestOption(v1, v1);
		twoOptMatrix[v2.getIndex()] = findBestOption(v2, v2);
	}

	/**
	 * Executes the reversal by extracting the information from the option and passing them on
	 * @see reverserRoute()
	 */
	@Override
	public void executeOption(Option o) {
		//the vehicle of interest
		Vehicle v = o.getV1();
		//the new start and end point of the route-part which is to be reversed
		Customer newStart = o.getC1();
		Customer newEnd = o.getC2();

		Customer last = newStart.getSucc();
		Customer limit = newEnd.getPred();
		ArrayList<Customer> customers = new ArrayList<Customer>();


		//read the customers which are to be reversed in reversed order
		Customer cCur = newStart;
		while(!cCur.equals(limit)) {
			customers.add(cCur);

			//move on to the next customer
			cCur = cCur.getPred();

			//remove the customer of this visit
			v.remove(cCur.getSucc());
		}

		//try to insert the customers that were taken back into the route
		Customer cPred = limit;
		for(Customer c : customers) {
			if(c.canBeInsertedBetween(cPred, last)) {
				v.insertBetween(c, cPred, last);
			}
			else {
				System.out.println("Time window violation");
				return;
			}
			cPred = c;
		}
	}


	/**
	 * Get the best option of the twoOptMatrix
	 * @return Option, the best option
	 */
	@Override
	public Option fetchBestOption() {
		Option bestTwoOpt = twoOptMatrix[0];
		for(Option o : twoOptMatrix) {
			if(o.getDelta() < bestTwoOpt.getDelta()) {
				bestTwoOpt = o;
			}
		}
		return bestTwoOpt;
	}

	/**
	 * Find the best possible reversion within a vehicle
	 * @param v1 Vehicle, the vehicle which is to be checked
	 * @param v2 this parameter is not used, as it is mandated by the structure of the Operation-interface
	 * @return Option, the best reverse option for vehicle 1
	 */
	@Override
	public Option findBestOption(Vehicle v1, Vehicle v2) {
		Vehicle v = v1;
		Option twoOpt = new TwoOptOption(null, null, v, 0, this);

		//get the first route of the vehicle
		Customer c1 = v.getFirstCustomer();
		Customer c2 = c1.getSucc();
		//go through all routes

		while(!c2.equals(v.getLastCustomer())) {

			//get the succeeding route
			Customer c3 = c2;
			Customer c4 = c3.getSucc();
			//compare each route with all following routes
			while(!c3.equals(v.getLastCustomer())) {

				//check if the routes cross
				if(lineCollision(c1, c2, c3, c4)) {
					System.out.println("Collision");

					//check if a reversal is possible and what benefit it would bring
					double delta = checkReversal(v,c3, c2);

					if(delta < twoOpt.getDelta()) {
						twoOpt = new TwoOptOption(c3, c2, v, delta, this);
					}
				}
				//move to the following route
				c3 = c4;
				c4 = c4.getSucc();
			}
			//move to the next route
			c1 = c2;
			c2 = c2.getSucc();
		}
		//return the best two opt
		return twoOpt;
	}

	/**
	 * Check if the route between two customers can be reversed
	 * @param newStart Customer, the start of the new middle route
	 * @param newEnd Customer, the end of the new middle route
	 * @return double, the cost-benefit of the reversal or 0 if there is none
	 */
	private double checkReversal(Vehicle v, Customer newStart, Customer newEnd) {

		Customer oldEnd = newStart.getSucc();
		Customer oldStart = newEnd.getPred();
		
		//calculate the change in distance
		double oldCost = vrp.distance(oldStart, newEnd) + vrp.distance(newStart, oldEnd);
		double newCost = vrp.distance(oldStart, newStart) + vrp.distance(newEnd, oldEnd);

		double deltaCost = newCost - oldCost;

		Customer cCur = oldStart;
		//check for improvement and catch computational error
		if(deltaCost < EPSILON) {
			cCur = v.getFirstCustomer();

			//get the propagation times
			while(cCur != null) {
				cCur.setCheckEarliest(cCur.getEarliestStart());
				cCur.setCheckLatest(cCur.getLatestStart());
				cCur = cCur.getSucc();
			}

			//forward propagation for the reversal part
			cCur = oldStart;
			Customer cSucc = newStart;
			while (!cSucc.equals(oldStart)) {
				cSucc.setCheckEarliest(Math.max(cSucc.getReadyTime(),cCur.getCheckEarliest()+cCur.getServiceTime()+vrp.distance(cCur,cSucc)));
				//take the predecessor instead of the successor because of reversion
				cCur = cSucc;
				cSucc = cSucc.getPred();
			}

			//forward propagation for the remaining customers after the reversal (regular forward propagation)
			cSucc = oldEnd;
			while(cSucc != null) {
				cSucc.setCheckEarliest(Math.max(cSucc.getReadyTime(),cCur.getCheckEarliest()+cCur.getServiceTime()+vrp.distance(cCur,cSucc)));
				cCur = cSucc;
				cSucc = cSucc.getSucc();
			}

			//backward propagation for the reversal part 
			cCur = oldEnd;
			Customer cPred = newEnd;
			while(!cPred.equals(newStart) ) {
				cPred.setCheckLatest(Math.min(cPred.getDueDate(), cCur.getCheckLatest() - cCur.getServiceTime() - vrp.distance(cPred, cCur)));
				//take the successor instead of the predecessor because of reversion
				cCur = cPred;
				cPred = cPred.getSucc();
			}

			//backward propagation for the remaining customers before the reversal (regular backward propagation)
			cCur = oldStart;
			while(cPred != null) {
				cPred.setCheckLatest(Math.min(cPred.getDueDate(), cCur.getCheckLatest() - cCur.getServiceTime() - vrp.distance(cPred, cCur)));
				cCur = cPred;
				cPred = cPred.getPred();
			}

			//check for time-constraint violations
			cCur = v.getFirstCustomer();
			while(cCur != null) {
				if(cCur.getCheckLatest() < cCur.getCheckEarliest()) {
					System.out.println("Time window violation");
					return 0;
				}
				cCur = cCur.getSucc();
			}

			return deltaCost;
		}
		else {
			return 0;
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
	private boolean lineCollision(Customer c1, Customer c2, Customer c3, Customer c4  ) {

		//extract the coordinates of the routes
		double xC1 = c1.getxCoord();
		double yC1 = c1.getyCoord();
		double xC2 = c2.getxCoord();
		double yC2 = c2.getyCoord();
		double xC3 = c3.getxCoord();
		double yC3 = c3.getyCoord();
		double xC4 = c4.getxCoord();
		double yC4 = c4.getyCoord();

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
	
	/**
	 * Main method for testing
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		//get information from the input
		String folderpath = args[0];
		int numCustomers = Integer.parseInt(args[1]);
		VRP vrp = new VRP(folderpath, numCustomers);
		Vehicle testV = vrp.getVehicle()[7];
		Customer[] c = vrp.getCustomer();
		testV.show();
		testV.insertBetween(c[10], testV.getFirstCustomer(), testV.getLastCustomer());
		testV.insertBetween(c[4], c[10], testV.getLastCustomer());
		testV.insertBetween(c[6], c[4], testV.getLastCustomer());
		testV.insertBetween(c[8], c[6], testV.getLastCustomer());
		testV.insertBetween(c[7], c[8], testV.getLastCustomer());
		testV.show();
		
		TwoOptOperation two = new TwoOptOperation(vrp, numCustomers);
		
		Option x = two.findBestOption(testV, testV);
		x.printOption();
		two.executeOption(x);
		testV.show();
	}
}