package representation;
import java.util.*;
import java.io.*;

/**
 * Class to model a VRP-instance
 * @author Patrick Prosser
 *
 */
public class VRP {

	private String name;
	private Customer[] customer;
	private Vehicle[] vehicle;
	private double[][] distance;   
	private int n, m; // number of customers and number of vehicles
	private int capacity;
	private Customer depot;

	/**
	 * Constructor to create a VRP-instance
	 * @param fname String, name of the file containing the VRP-instance
	 * @param numberOfCustomers int, the number of customers for the problem
	 * @throws IOException
	 */
	public VRP(String fname,int numberOfCustomers) throws IOException {
		n = numberOfCustomers;
		//create arrays/matrix of necessary size
		customer = new Customer[n+1]; // customer[0] is depot
		vehicle = new Vehicle[n]; // no comment
		distance = new double[n+1][n+1];

		//read information from solomon-benchmark example
		String s = "";
		Scanner sc = new Scanner(new File(fname));
		name = sc.next();
		sc.next(); sc.next(); sc.next();
		m = sc.nextInt();
		capacity = sc.nextInt();
		while (sc.hasNext() && !s.equals("TIME")) s = sc.next();
		s = "continue";
		while (sc.hasNext() && !s.equals("TIME"))s = sc.next(); 
		for (int i=0;i<=n;i++){
			int custNo = sc.nextInt();
			int x = sc.nextInt();
			int y = sc.nextInt();
			int d = sc.nextInt();
			int rt = sc.nextInt();
			int dd = sc.nextInt();
			int st = sc.nextInt();
			//create customer for read data and add it to the array
			customer[i] = new Customer(custNo,x,y,d,rt,dd,st);
			customer[i].setVrp(this);
			//System.out.println("cust: "+ customer[i]);

		}
		sc.close();

		//calculate the distances between all customers based on the euclidean distance
		for (int i=0;i<n;i++)
			for (int j=i+1;j<=n;j++){
				double deltaX = (double)(customer[i].getxCoord() - customer[j].getxCoord());
				double deltaY = (double)(customer[i].getyCoord() - customer[j].getyCoord());
				distance[i][j] = distance[j][i] = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
			}

		depot = customer[0];

		//Create one vehicle for each customer and add a customer
		for (int i=0;i<n;i++){
			//vehicle at array-position 0 gets the id 1
			vehicle[i] = new Vehicle(this,i+1,capacity,1,depot);
			//Every new vehicle that is created after the number of vehicles given by the benchmark is reached
			//becomes virtual by the assignment of a high cost of use
			if(i>m-1) {
				vehicle[i].setCostOfUse(100);
			}
			//add customer to vehicle and omit the depot
			vehicle[i].insertBetween(customer[i+1], vehicle[i].getFirstCustomer(), vehicle[i].getLastCustomer());
		}
	}

	/**
	 * Determine the distance between two customers, by accessing the distance matrix
	 * @param x Customer, customer no. 1
	 * @param y Customer, customer no. 2
	 * @return double, the euclidean distance
	 */
	public double distance(Customer x,Customer y){
		if (x == null && y == null) return Double.MAX_VALUE;
		if (x == null) return distance[0][y.getCustNo()];
		if (y == null) return distance[0][x.getCustNo()];
		return distance[x.getCustNo()][y.getCustNo()];
	}

	/**
	 * Calculate the total cost of all vehicles
	 * @return double, the total cost of travel
	 */
	public double calcTotalCost() {
		double totalCost = 0;
		//sum up the travel costs for each vehicle
		for(Vehicle v : vehicle) {
			totalCost+=v.getCost();
		}
		return totalCost;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Customer[] getCustomer() {
		return customer;
	}

	public void setCustomer(Customer[] customer) {
		this.customer = customer;
	}

	public Vehicle[] getVehicle() {
		return vehicle;
	}

	public void setVehicle(Vehicle[] vehicle) {
		this.vehicle = vehicle;
	}

	public double[][] getDistance() {
		return distance;
	}

	public void setDistance(double[][] distance) {
		this.distance = distance;
	}

	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}

	public int getM() {
		return m;
	}

	public void setM(int m) {
		this.m = m;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public Customer getDepot() {
		return depot;
	}

	public void setDepot(Customer depot) {
		this.depot = depot;
	}

}
