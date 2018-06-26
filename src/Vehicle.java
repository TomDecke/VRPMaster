import java.util.*;

/**
 * 
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
		lastCustomer = new Customer(depot.custNo,depot.xCoord,depot.yCoord,0,0,vrp.latest,0);
		firstCustomer.succ = lastCustomer;
		lastCustomer.pred = firstCustomer;
	}

	//
	// Insert customer c into vehicle's tour
	// in least cost position. Deliver true
	// if the insertion was possible, i.e.
	// capacity & time windows respected
	/**
	 * Make sure to remove the customer from the vehicle from which he is coming
	 * @param c
	 * @return
	 */
	boolean minCostInsertion(Customer c){		

		Customer cInsertAfter = findBestPosition(c);
		
		//If there is a valid position for the customer, insert him
		if(cInsertAfter != null) {

			
			//insert the customer into the vehicle
			Customer cInsertSucc = cInsertAfter.succ;
			c.pred = cInsertAfter;
			c.succ = cInsertSucc;
			cInsertSucc.pred = c;
			cInsertAfter.succ = c;
			
			//propagate the earliest and latest start
			c.insertBetween(cInsertAfter, cInsertSucc);
			
			//increase the load of the vehicle by the customers demand
			this.load += c.demand;
			
			//update the distance by removing the prior edge and adding the new ones
			distance += (vrp.distance(cInsertAfter,c) + vrp.distance(c, cInsertSucc) - vrp.distance(cInsertAfter,cInsertSucc));
			
			//update the cost of this vehicle
			this.cost = this.distance * this.costOfUse;
			
			//update the number of customers of the vehicle
			this.numCostumer++;
			
			return true;
		}
		return false;
	}
	
	
	/**
	 * Find the best position for a customer in the vehicle
	 * @param c Customer, the customer that is to be inserted into the vehicle
	 * @return Customer, the customer after which the new customer should be inserted 
	 */
	public Customer findBestPosition(Customer c) {
		
		//make sure the vehicle has enough capacity to take the customer
		if(c.demand+this.load>this.capacity) {
			return null;
		}
		
		Customer cCurrent = firstCustomer;
		Customer cSucc = cCurrent.succ;
		Customer cTmp = null;

		//TODO personally introduced limitation as starting value
		double minCost = cost/costOfUse*2 + 1000;
		Customer cInsertAfter = null;

		//Find the position at which the increment of the distance is the smallest
		while(cSucc!= null) {
			//make sure the customer fits in the time window
			if(c.canBeInsertedBetween(cCurrent, cSucc)) {
				//determine the change in cost, caused by the insertion at the current position
				double insertionCost = vrp.distance(cCurrent,c) + vrp.distance(c, cSucc) - vrp.distance(cCurrent,cSucc);
				if(insertionCost<minCost) {
					cInsertAfter = cCurrent;
					minCost = insertionCost;
				}
			}
			cTmp = cSucc;
			cSucc = cSucc.succ;
			cCurrent = cTmp;
		}
		return cInsertAfter;
	}

	//
	// Remove customer c from vehicle's tour
	// deliver true if done, false otherwise
	//
	boolean remove(Customer c){
		Customer currentCustomer = firstCustomer;
		
		//search for customer c
		while(currentCustomer.succ != null) {
			if(c.equals(currentCustomer)) {
				Customer cPred =  currentCustomer.pred;
				Customer cSucc = currentCustomer.succ;
				//if found change the successor of the predecessor and the predecessor of the successor
				currentCustomer.pred.succ = cSucc;
				currentCustomer.succ.pred = cPred;
				
				//remove the load
				this.load -= c.demand;
				
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
	
	public double getDistance() {
		return this.distance;
	}

	public void setDistance(double distance) {
		this.distance=distance;
	}

}
