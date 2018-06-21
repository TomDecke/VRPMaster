import java.util.*;

public class Vehicle {

	int id, capacity, load, costOfUse;
	double cost; // this is the sum of the distance travelled times costOfUse
	//customers for beginning and end of a tour
	Customer firstCustomer, lastCustomer;
	VRP vrp;

	/**
	 * Constructor to create a vehicle for the VRP
	 * @param vrp VRP, instance of the vehicle routing problem it belongs to
	 * @param id int, identifier for the vehicle
	 * @param capacity, int maximum capacity of the vehicle
	 * @param costOfUse, int cost that comes from using the vehicle
	 * @param depot, Customer that functions as the depot
	 */
	public Vehicle (VRP vrp,int id, int capacity,int costOfUse, Customer depot){
		this.vrp = vrp;
		this.id = id;
		this.capacity = capacity;
		this.costOfUse = costOfUse;

		//TODO integrate them into the distance matrix
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
	//
	boolean minCostInsertion(Customer c){

		//if the demand is to big, the customer can't be inserted
		if(c.demand+this.load>this.capacity) {
			return false;
		}
		
		System.out.println("CheckPoint1");

		Customer cCurrent = firstCustomer;
		Customer cSucc = cCurrent.succ;
		Customer cTmp = null;

		//TODO personally introduced limitation as starting value - needs to be configured
		double minCost = cost/costOfUse*2 + 1000;
		Customer cInsertAfter = null;

		//Find the position at which the increment of the distance is the smallest
		while(cSucc!= null) {
			//make sure the customer fits in the time window
			if(c.canBeInsertedBetween(cCurrent, cSucc)) {
				//determine the change in cost, caused by the insertion at the current position
				double insertionCost = vrp.distance(cCurrent,c) + vrp.distance(c, cSucc) - vrp.distance(cCurrent,cSucc);
				System.out.println(minCost + " " + insertionCost);
				if(insertionCost<minCost) {
					cInsertAfter = cCurrent;
					minCost = insertionCost;
				}
			}
			cTmp = cSucc;
			cSucc = cSucc.succ;
			cCurrent = cTmp;
		}
		
		//If there is a valid position for the customer, insert him
		if(cInsertAfter != null) {
			System.out.println("Checkpoint2");

			
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
			return true;
		}
		return false;
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
				//if found change the successor of the predecessor and the predecessor of the successor
				currentCustomer.pred.succ = currentCustomer.succ;
				currentCustomer.succ.pred = currentCustomer.pred;
				return true;
			}
			currentCustomer = currentCustomer.succ;
		}
		return false;
	}



	/**
	 * Calculate the cost for this vehicles tour
	 * @return double, the price of the tour
	 */
	double calculateCost() {
		double distance = 0;
		Customer curr = firstCustomer;
		Customer succ = firstCustomer.succ;
		
		//sum up the traveled distance
		while(curr!=lastCustomer) {			
			distance += vrp.distance(curr, succ);

			curr=succ;
			succ=curr.succ;
		}
		return distance*this.costOfUse;
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
	void show(){
		System.out.print("id(vehicle): "+ id +" ");
		Customer customer = firstCustomer;
		System.out.print(customer.custNo);
		customer = customer.succ;
		while (customer != null){
			System.out.print(" -> " + customer.custNo );
			customer = customer.succ;
		}
		System.out.println();
	}
}
