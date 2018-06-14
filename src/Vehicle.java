import java.util.*;

public class Vehicle {

    int id, capacity, load, costOfUse;
    double cost; // this is the sum of the distance travelled times costOfUse
    Customer firstCustomer;
    VRP vrp;

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
	return false;
    }

    //
    // Remove customer c from vehicle's tour
    // deliver true if done, false otherwise
    //
    boolean remove(Customer c){
	return false;
    }

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
