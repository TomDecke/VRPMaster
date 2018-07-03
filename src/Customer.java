import java.util.*;

import addOns.TimeConstraintViolationException;

import java.io.*;

/**
 * Class modelling a customer for a VRP-instance
 * @author Patrick Prosser
 *
 */
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

	/**
	 * Propagate earliest and latest start for the insertion of a customer
	 * @param y Customer, potential predecessor
	 * @param z Customer, potential successor
	 * @throws TimeConstraintViolationException 
	 */
	void insertBetween(Customer y,Customer z) throws TimeConstraintViolationException{
		earliestStart = Math.max(this.readyTime,y.earliestStart + y.serviceTime + vrp.distance(y,this));
		latestStart = Math.min(this.dueDate,z.latestStart - (this.serviceTime + vrp.distance(this,z)));
		propagateLatestStart();
		propagateEarliestStart();
	}

	public void propagateLatestStart() throws TimeConstraintViolationException{
		Customer current = this;
		// propagate latestStart left
		while (current.pred != null){
			Customer cPred = current.pred;
			cPred.latestStart = Math.min(cPred.dueDate,current.latestStart - (cPred.serviceTime + vrp.distance(cPred,current)));
			if(cPred.earliestStart > cPred.latestStart) {
				throw new TimeConstraintViolationException("Updated latest start comes before current earliest start");
			}
			current = cPred;
		}
	}

	public void propagateEarliestStart() throws TimeConstraintViolationException {
		Customer current = this;
		// propagate earliestStart right
		while (current.succ != null){
			Customer cSucc = current.succ;
			cSucc.earliestStart = Math.max(cSucc.readyTime,current.earliestStart + current.serviceTime + vrp.distance(current,cSucc));
			if(cSucc.earliestStart > cSucc.latestStart) {
				throw new TimeConstraintViolationException("Updated earliest start comes before current latest start");
			}
			current = cSucc;
		}
	}

	/**
	 * Write the variables of the Customer as String
	 */
	public String toString(){
		return custNo +" "+ xCoord +" "+ yCoord +" "+ demand +" "+ readyTime +" "+ dueDate +" "+ serviceTime;
	}

	/**
	 * Creates a copy of the customer (without reference to other customers)
	 * @return Customer, a new customer object with the same information
	 */
	public Customer copy() {
		Customer nC = new Customer(this.custNo,this.xCoord,this.yCoord,this.demand,this.readyTime,this.dueDate,this.serviceTime);
		nC.vehicle = this.vehicle;
		nC.vrp = this.vrp;
		nC.earliestStart = this.earliestStart;
		nC.latestStart = this.latestStart;
		
		return nC;
	}


	/**
	 * Main method for testing
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args)  throws IOException {

	}
}
