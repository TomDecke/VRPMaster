import java.util.*;
import java.io.*;

public class VRP {

	String name;
	Customer[] customer;
	Vehicle[] vehicle;
	double[][] distance;   
	int n, m; // number of customers and number of vehicles
	int capacity;
	Customer depot;
	int latest = 0;

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
		//TODO why is this <=?
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
			customer[i].vrp = this;
			System.out.println("cust: "+ customer[i]);
			
			//determine the latest finish
			if(dd+st>latest) {
				latest=dd+st;
			}
		}
		sc.close();

		//calculate the distances between all customers based on the euclidean distance
		for (int i=0;i<n;i++)
			for (int j=i+1;j<=n;j++){
				double deltaX = (double)(customer[i].xCoord - customer[j].xCoord);
				double deltaY = (double)(customer[i].yCoord - customer[j].yCoord);
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
				vehicle[i].costOfUse = 100;
			}
			//add customer to vehicle and omit the depot
			vehicle[i].minCostInsertion(customer[i+1]);
		}


	}

	/**
	 * Determine the distance between two customers, by accessing the distance matrix
	 * @param x Customer, customer no. 1
	 * @param y Customer, customer no. 2
	 * @return double, the euclidean distance
	 */
	double distance(Customer x,Customer y){
		if (x == null && y == null) return Double.MAX_VALUE;
		if (x == null) return distance[0][y.custNo];
		if (y == null) return distance[0][x.custNo];
		return distance[x.custNo][y.custNo];
	}
	
	/**
	 * Calculate the total cost of all vehicles
	 * @return double, the total cost of travel
	 */
	public double calcTotalCost() {
		double totalCost = 0;
		//sum up the travel costs for each vehicle
		for(Vehicle v : vehicle) {
			totalCost+=v.cost;
		}
		return totalCost;
	}

	public static void main(String[] args)  throws IOException {

		VRP vrp = new VRP(args[0],Integer.parseInt(args[1]));
		 System.out.print(vrp.customer[8] +" // this is a customer printed ");
	}
}
