import java.io.IOException;
import java.util.*;

import addOns.TimeConstraintViolationException;

/**
 * Class modelling a vehicle for a VRP-instance
 * @author Patrick Prosser
 *
 */
public class Vehicle {

	int id, index, capacity, load, costOfUse, numCostumer;
	double cost; // this is the sum of the distance travelled times costOfUse
	private double distance; //distance travelled by the vehicle
	//customers for beginning and end of a tour
	Customer firstCustomer, lastCustomer;
	VRP vrp;

	/**
	 * Constructor to create a vehicle for the VRP
	 * @param vrp VRP, instance of the vehicle routing problem it belongs to
	 * @param id int, identifier for the vehicle
	 * @param capacity int, maximum capacity of the vehicle
	 * @param costOfUse int, cost that comes from using the vehicle
	 * @param depot Customer, customer that functions as the depot
	 */
	public Vehicle (VRP vrp,int id, int capacity,int costOfUse, Customer depot){
		this.vrp = vrp;
		this.id = id;
		this.index=id-1;
		this.capacity = capacity;
		this.costOfUse = costOfUse;
		this.numCostumer = 0;
		this.distance = 0;

		//set up dummy customers who come at the beginning and at the end of a tour 
		firstCustomer = new Customer(depot.custNo,depot.xCoord,depot.yCoord,0,0,0,0);
		firstCustomer.vehicle = this;
		lastCustomer = new Customer(depot.custNo,depot.xCoord,depot.yCoord,0,0,depot.dueDate,0);
		lastCustomer.vehicle = this;
		firstCustomer.succ = lastCustomer;
		lastCustomer.pred = firstCustomer;
	}
	
	/**
	 * Creates a copy of this vehicle
	 * @return Vehicle the copy
	 */
	public Vehicle copy() {
		//take the values of this vehicle for the copy
		Vehicle nV = new Vehicle(this.vrp, this.id, this.capacity, this.costOfUse, this.vrp.depot);
		nV.load = this.load;
		nV.cost = this.cost;
		nV.setDistance(this.distance);
		
		//copy the customers into the vehicle
		Customer cPred = nV.firstCustomer;
		Customer cCur = this.firstCustomer.succ;
		while(!cCur.equals(this.lastCustomer)) {
			Customer cCopy = cCur.copy();
			cPred.succ = cCopy;
			cCopy.pred = cPred;
			
			//move to the next one
			cPred = cCopy;
			cCur = cCur.succ;
		}
		cPred.succ = nV.lastCustomer;
		nV.lastCustomer.pred = cPred; 
		return nV;
	}

	/**
	 * Insert customer c into vehicle's tour
	 * in least cost position. Deliver true
	 * if the insertion was possible, i.e.
	 * capacity & time windows respected
	 * @param cInsert Customer, the customer which is to be inserted 
	 * @return boolean, whether or not the insertion was successful
	 */
	boolean insertBetween(Customer cInsert, Customer cPred, Customer cSucc){		


		//tell the customer he now belongs to this vehicle
		cInsert.vehicle = this;

		//insert the customer into the vehicle
		cInsert.pred = cPred;
		cInsert.succ = cSucc;
		cSucc.pred = cInsert;
		cPred.succ = cInsert;

		//propagate the earliest and latest start
		try {
			cInsert.insertBetween(cPred, cSucc);
		} catch (TimeConstraintViolationException e) {
			System.out.println(e.getMessage());
			return false;
		}

		//increase the load of the vehicle by the customers demand
		this.load += cInsert.demand;

		//update the distance by removing the prior edge and adding the new ones
		distance += (vrp.distance(cPred,cInsert) + vrp.distance(cInsert, cSucc) - vrp.distance(cPred,cSucc));

		//update the cost of this vehicle
		this.cost = this.distance * this.costOfUse;

		//update the number of customers of the vehicle
		this.numCostumer++;

		return true;
	}

	public boolean canAccomodate(Customer c) {
		if(this.capacity < this.load + c.demand) {
			return false;
		}
		else {
			return true;
		}
	}


	/**
	 * Remove a customer from the vehicle's tour
	 * @param c Customer, the customer which should be removed
	 * @return boolean, true if successful, false otherwise
	 */
	boolean remove(Customer c){
		Customer currentCustomer = firstCustomer;

		//search for customer c
		while(currentCustomer.succ != null) {
			if(c.equals(currentCustomer)) {
				Customer cPred =  currentCustomer.pred;
				Customer cSucc = currentCustomer.succ;

				//if found change the successor of the predecessor and the predecessor of the successor
				cPred.succ = cSucc;
				cSucc.pred = cPred;

				//re-propagate earliest and latest start
				try {
					//propagate earliest start 
					cSucc.propagateEarliestStart();

					//propagate latest start 
					cPred.propagateLatestStart();

				} catch (TimeConstraintViolationException e) {
					System.err.println("removal");
					System.out.println(e.getMessage());
				}


				//remove the load
				this.load -= c.demand;

				//remove pointers of the customer
				c.vehicle = null;
				c.pred = null;
				c.succ = null;

				//update distance, by removing edges to former customer and adding new edge between now-neighbours
				distance += vrp.distance(cPred, cSucc) - vrp.distance(cPred, currentCustomer) - vrp.distance(currentCustomer, cSucc);

				//recalculate the cost
				this.cost = this.distance * this.costOfUse;

				//update the number of customers
				this.numCostumer--;
				return true;
			}
			currentCustomer = currentCustomer.succ;
		}
		return false;
	}

	/**
	 * Get the load of the vehicle as String
	 * @return String, the value of the vehicle's load
	 */
	public String toString(){
		return String.valueOf(load);
	}

	/**
	 * Shows the route of the vehicle
	 */
	public void show(){
		System.out.print("vehicle "+ id +": ");
		Customer customer = firstCustomer;
		System.out.print(customer.custNo);
		customer = customer.succ;
		while (customer != null){
			System.out.print(" -> " + customer.custNo );
			customer = customer.succ;
		}
		System.out.println();
	}


	/**
	 * Accessor for the distance travelled by this vehicle 
	 * @return double, the distance
	 */
	public double getDistance() {
		return this.distance;
	}

	/**
	 * Mutator for the distance
	 * @param distance double, the new distance
	 */
	public void setDistance(double distance) {
		this.distance=distance;
	}

	/**
	 * Main method for testing
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args)  throws IOException {
		
		//get the input
		String fileIn = args[0];
		int numCustomer = Integer.parseInt(args[1]);
	
		SteepestDescent stDesc = new SteepestDescent(fileIn,numCustomer);

		//run the solver
		System.out.println("");
		stDesc.solve();
		
		System.out.println("Copy test: ");
		Vehicle v1 = stDesc.getVehicles().get(0);
		v1.show();
		System.out.println("Actual copy:");
		Vehicle vC = v1.copy();
		vC.show();

	}
}
