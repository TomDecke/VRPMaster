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

	// can we place this customer before customer y?
	boolean canComeBefore(Customer y){
		Customer beforeY = y.pred;
		if (beforeY == null) beforeY = vrp.depot;
		double es = Math.max(readyTime,beforeY.earliestStart + beforeY.serviceTime + vrp.distance(beforeY,this));
		double ls = Math.min(dueDate,y.latestStart - (serviceTime + vrp.distance(this,y)));
		return es <= ls;
	}

	// can we place this customer after customer y?
	boolean canComeAfter(Customer y){
		Customer afterY = y.succ;
		if (afterY == null) afterY = vrp.depot;
		double es = Math.max(readyTime,y.earliestStart + y.serviceTime + vrp.distance(y,this));
		double ls = Math.min(dueDate,afterY.latestStart - (serviceTime + vrp.distance(this,y)));
		return es <= ls;
	}

	// assuming this, y and z are all non-null can we
	// insert this customer between customers y and z?
	boolean canBeInsertedBetween(Customer y,Customer z){
		double es = Math.max(readyTime,y.earliestStart + y.serviceTime + vrp.distance(y,this));
		double ls = Math.min(dueDate,z.latestStart - (serviceTime + vrp.distance(this,z)));
		return es <= ls;
	}

	void insertBetween(Customer y,Customer z){
		earliestStart = Math.max(readyTime,y.earliestStart + y.serviceTime + vrp.distance(y,this));
		latestStart = Math.min(dueDate,z.latestStart - (serviceTime + vrp.distance(this,z)));
		Customer current = this;
		// propagate latestStart left
		while (current != null){
			Customer cPred = current.pred;
			cPred.latestStart = Math.min(cPred.dueDate,current.latestStart - (cPred.serviceTime + vrp.distance(cPred,current)));
			current = cPred;
		}
		current = this;
		// propagate earliestStart right
		while (current != null){
			Customer cSucc = current.succ;
			cSucc.earliestStart = Math.max(cSucc.readyTime,current.earliestStart + current.serviceTime + vrp.distance(current,cSucc));
			current = cSucc;
		}
	}

	public String toString(){
		return custNo +" "+ xCoord +" "+ yCoord +" "+ demand +" "+ readyTime +" "+ dueDate +" "+ serviceTime;
	}

	public static void main(String[] args)  throws IOException {

		VRP vrp = new VRP(args[0],Integer.parseInt(args[1]));
		Customer depot = vrp.customer[0];
		Customer x = vrp.customer[1];
		Customer y = vrp.customer[2];
		Customer z = vrp.customer[3];
		System.out.println(depot);
		System.out.println(x);
		System.out.println(y);
		System.out.println(z);
		System.out.println("depot-x: "+ vrp.distance(null,x) +" x-y: "+ vrp.distance(x,y) +" y-depot: "+ vrp.distance(y,null));
		vrp.vehicle[0].show();
		vrp.vehicle[1].show();
		System.out.println(x.canComeBefore(y));
		System.out.println(y.canComeBefore(x));
		System.out.println(x.canComeAfter(y));
		System.out.println(y.canComeAfter(x));
	}
}
