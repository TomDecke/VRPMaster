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

    public VRP(String fname,int numberOfCustomers) throws IOException {
	n = numberOfCustomers;
	customer = new Customer[n+1]; // customer[0] is depot
	vehicle = new Vehicle[n]; // no comment
	distance = new double[n+1][n+1];
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
	    customer[i] = new Customer(custNo,x,y,d,rt,dd,st);
	    customer[i].vrp = this;
	    System.out.println("cust: "+ customer[i]);
	}
	sc.close();
	for (int i=0;i<n;i++)
	    for (int j=i+1;j<=n;j++){
		double deltaX = (double)(customer[i].xCoord - customer[j].xCoord);
		double deltaY = (double)(customer[i].yCoord - customer[j].yCoord);
		distance[i][j] = distance[j][i] = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
	    }
	for (int i=0;i<n;i++){
	    vehicle[i] = new Vehicle(this,i,capacity,1);
	    vehicle[i].addFirstCustomer(customer[i+1]);	    
	}
	depot = customer[0];
    }

    double distance(Customer x,Customer y){
	if (x == null && y == null) return Double.MAX_VALUE;
	if (x == null) return distance[0][y.custNo];
	if (y == null) return distance[0][x.custNo];
	return distance[x.custNo][y.custNo];
    }

    public static void main(String[] args)  throws IOException {

	VRP vrp = new VRP(args[0],Integer.parseInt(args[1]));
    }
}
