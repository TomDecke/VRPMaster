import java.util.*;

public class Vehicle {

    int id, capacity, load, costOfUse;
    double cost; // this is the sum of the distance traveled times costOfUse
    Customer firstCustomer;
    VRP vrp;

    /**
     * Constructor to create a vehicle for the VRP
     * @param vrp VRP, instance of the vehicle routing problem it belongs to
     * @param id int, identifier for the vehicle
     * @param capacity, int maximum capacity of the vehicle
     * @param costOfUse, int cost that comes from using the vehicle
     */
    public Vehicle (VRP vrp,int id, int capacity,int costOfUse){
	this.vrp = vrp;
	this.id = id;
	this.capacity = capacity;
	this.costOfUse = costOfUse;
    }

    //
    // Insert customer c into vehicle's tour
    // in least cost position. Deliver true
    // if the insertion was possible, i.e.
    // capacity & time windows respected
    //
    boolean minCostInsertion(Customer c){
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

    /**
     * Add the first customer to the vehicle 
     * @param c Customer
     */
    void addFirstCustomer(Customer c){
	firstCustomer = c;
	//TODO What if the demand of the (first) customer is larger than the capacity of a vehicle?
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
