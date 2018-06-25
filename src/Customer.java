import java.util.*;
import java.io.*;

public class Customer {

	int custNo, xCoord, yCoord, demand, readyTime, dueDate, serviceTime;
	double earliestStart, latestStart; // ... can be reset to readyTime & dueDate 
	Customer pred,succ;
	Vehicle vehicle;
	VRP vrp;

	public Customer(){
		pred = succ = null;
	}

	public Customer (int custNo,int xCoord,int yCoord, int demand,int readyTime, int dueDate,int serviceTime){
		this.custNo = custNo;
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.demand = demand;
		this.readyTime = readyTime;
		this.dueDate = dueDate;
		this.serviceTime = serviceTime;
		pred = null;
		succ = null;
		earliestStart = readyTime;
		latestStart = dueDate;
	}

	// assuming this, y and z are all non-null can we
	// insert this customer between customers y and z?
	boolean canBeInsertedBetween(Customer y,Customer z){
		double es = Math.max(this.readyTime,y.earliestStart + y.serviceTime + vrp.distance(y,this));
		double ls = Math.min(this.dueDate,z.latestStart - (this.serviceTime + vrp.distance(this,z)));
		return es <= ls;
	}

	//TODO Think this through. Does it make sense this way?!
	/**
	 * Propagate earliest and latest start for the insertion of a customer
	 * @param y Customer, potential predecessor
	 * @param z Customer, potential successor
	 */
	void insertBetween(Customer y,Customer z){
		earliestStart = Math.max(this.readyTime,y.earliestStart + y.serviceTime + vrp.distance(y,this));
		latestStart = Math.min(this.dueDate,z.latestStart - (this.serviceTime + vrp.distance(this,z)));
		Customer current = this;
		// propagate latestStart left
		while (current.pred != null){
			Customer cPred = current.pred;
			cPred.latestStart = Math.min(cPred.dueDate,current.latestStart - (cPred.serviceTime + vrp.distance(cPred,current)));
			current = cPred;
		}
		current = this;
		// propagate earliestStart right
		while (current.succ != null){
			Customer cSucc = current.succ;
			cSucc.earliestStart = Math.max(cSucc.readyTime,current.earliestStart + current.serviceTime + vrp.distance(current,cSucc));
			current = cSucc;
		}
	}

	/**
	 * Write the variables of the Customer as String
	 */
	public String toString(){
		return custNo +" "+ xCoord +" "+ yCoord +" "+ demand +" "+ readyTime +" "+ dueDate +" "+ serviceTime;
	}

	public static void main(String[] args)  throws IOException {


		VRP vrp = new VRP(args[0],Integer.parseInt(args[1]));
		
		System.out.println();
		System.out.println("Customer main:");
		
		Customer depot = vrp.customer[0];
		Customer x = vrp.customer[1];
		Customer y = vrp.customer[2];
		Customer z = vrp.customer[3];
//		System.out.println(depot);
//		System.out.println(x);
//		System.out.println(y);
//		System.out.println(z);
//		System.out.println("depot-x: "+ vrp.distance(null,x) +" x-y: "+ vrp.distance(x,y) +" y-depot: "+ vrp.distance(y,null));
		Vehicle v2 = vrp.vehicle[2];
		Vehicle v1 = vrp.vehicle[1];
//		v2.remove(y);
//		v1.minCostInsertion(y);
		
		for(int i = 0 ; i<Integer.parseInt(args[1]); i++) {
			Vehicle v = vrp.vehicle[i];
			System.out.println("Customer of vehicle "+v.id +": " +v.firstCustomer.succ.toString());
			v.show();
			System.out.println("Cost for vehicle "+v.id+": "+v.cost);	
		}
		System.out.println("Total cost: " +vrp.calcTotalCost());
		
		v2.remove(z);
		v1.minCostInsertion(z);
		v1.show();
		v2.show();
		System.out.println("Total cost after moving y: " +vrp.calcTotalCost());
		
	}
}
