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
		firstCustomer = new Customer(id,depot.xCoord,depot.yCoord,0,0,0,0);
		lastCustomer = new Customer(id,depot.xCoord,depot.yCoord,0,0,vrp.latest,0);
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
		Customer cPred = firstCustomer;
		Customer cSucc = cPred.succ;
		while(cPred.succ != null) {
			if(c.canBeInsertedBetween(cPred, cSucc)) {
				c.insertBetween(cPred, cSucc);
			}
			cPred = cSucc;
			cSucc = cPred.succ;
		}
		//TODO Insert customer at the most beneficial position in the route
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
				//if found change the successor of the predecessor
				currentCustomer.pred.succ = currentCustomer.succ;
				return true;
			}
			currentCustomer = currentCustomer.succ;
		}
		return false;
	}

	//TODO this can probably be removed
	/**
	 * Add the first customer to the vehicle 
	 * @param c Customer
	 */
	void addFirstCustomer(Customer c){
		firstCustomer = c;
		load = c.demand;
		cost = vrp.distance(null,c) * costOfUse;
		c.vehicle = this;
	}


	public String toString(){
		return String.valueOf(load);
	}

	void show(){
		System.out.print("id: "+ id +" ");
		Customer customer = firstCustomer;
		while (customer != null){
			System.out.print(customer +" -> ");
			customer = customer.succ;
		}
		System.out.println();
	}
}
