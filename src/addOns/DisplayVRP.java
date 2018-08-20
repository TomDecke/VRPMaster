package addOns;
import java.io.*;
import java.util.*;

import representation.Customer;
import representation.VRP;

/**
 * Class to visualize the solution of a VRP-instance
 * @author Tom Decke
 *
 */
public class DisplayVRP {

	private String vrpInstance;
	private int xMax, xMin, yMax, yMin;
	private int xDepot, yDepot;
	private VRP vrp;
	private double costSol;
	private int numVehicles;
	private ArrayList<int[]> vehicles; 

	/**
	 * Constructor to enable the display of solutions for a VRP-instance
	 * @param vrpInstance String, the path to the instance
	 * @param numCust int, the number of customers in the instance
	 * @param sol String, the path to the proposed solution
	 */
	public DisplayVRP(String vrpInstance, int numCust, String sol) {

		try {
			this.vrpInstance = vrpInstance;
			this.vrp = new VRP(vrpInstance,numCust);
			vehicles = new ArrayList<int[]>();

			this.xDepot = vrp.getDepot().getxCoord();
			this.yDepot = vrp.getDepot().getyCoord();

			//create reader to take in the solution
			FileReader reader;
			Scanner sc;
			reader = new FileReader(sol);
			sc = new Scanner(reader);

			//read number of customers and needed vehicles, then move to the next line
			numCust = sc.nextInt();
			numVehicles = sc.nextInt();
			sc.nextLine();

			//extract the routes from the file
			int vCount = 0;
			while(sc.hasNextLine() && vCount < numVehicles) {
				String[] vArray = sc.nextLine().split("[ ]+");
				int[] customers = new int[vArray.length-1];
				for (int i = 0; i < vArray.length-1; i++) {
					customers[i] = Integer.parseInt(vArray[i]);
				}
				vehicles.add(customers);
				vCount++;
			}
			//retrieve the cost
			String[] tmp = sc.nextLine().split(" ");
			costSol = Double.parseDouble(tmp[2]);


			//Get the size of the map
			xMax = vrp.getCustomer()[0].getxCoord();
			xMin = xMax;
			yMax = vrp.getCustomer()[0].getyCoord();
			yMin = yMax;
			for(Customer c : vrp.getCustomer()) {
				int currentX = c.getxCoord();
				int currentY = c.getyCoord();
				if(currentX > xMax) {
					xMax = currentX;
				}
				else if (currentX < xMin) {
					xMin = currentX;
				}

				if(currentY > yMax) {
					yMax = currentY;
				}
				else if(currentY < yMin) {
					yMin = currentY;
				}
			}
			//Add buffer in the lower dimension to accommodate text
			yMin -= 5;

			reader.close();
			sc.close();

		} catch(FileNotFoundException fnfe) {
			System.err.println("File not found");
		} catch(IOException ioe) {
			System.err.println("Could not read file");
		}

	}

	/**
	 * Accessor for the cost of the solution
	 * @return double, the cost
	 */
	public double getCostSol() {
		return this.costSol;
	}

	/**
	 * Accessor for the vehicles
	 * @return ArrayList<int[]>
	 */
	public ArrayList<int[]> getVehicles(){
		return vehicles;
	}

	/**
	 * Plots the cities of a VRP-instance
	 */
	public void plotVRPInstance() {
		//set up a new plot
		StdDraw.clear(StdDraw.WHITE);
		StdDraw.setXscale(xMin, xMax);
		StdDraw.setYscale(yMin, yMax);

		//set customer size and print them to the map
		StdDraw.setPenRadius(0.005);
		for(Customer c : vrp.getCustomer()){
			StdDraw.point(c.getxCoord(), c.getyCoord());
		}

		StdDraw.show(0);
	}

	/**
	 * Draw the solution
	 */
	public void plotVRPSolution() {
		//set up a new plot
		StdDraw.clear(StdDraw.LIGHT_GRAY);
		StdDraw.setXscale(xMin, xMax);
		StdDraw.setYscale(yMin, yMax);

		//write file name and solution cost
		StdDraw.textLeft(xMin,yMin,vrpInstance.substring(vrpInstance.length()-20, vrpInstance.length()));
		StdDraw.textRight(xMax,yMin,String.format("Distance: %.3f", costSol));

		//determine customer size and print them to the map
		StdDraw.setPenRadius(0.008);
		for(Customer c : vrp.getCustomer()){
			StdDraw.point(c.getxCoord(), c.getyCoord());
		}

		//reset the pen-size and draw the connections
		StdDraw.setPenRadius(0.0005);
		for(int[] vehicle : vehicles) {

			//draw the line from the depot to the first customer
			int firstCustomer = vehicle[0];
			StdDraw.line(xDepot, yDepot,vrp.getCustomer()[firstCustomer].getxCoord(),vrp.getCustomer()[firstCustomer].getyCoord());

			//draw the tour-intermediates
			for(int i = 0; i< vehicle.length-1; i++) {
				Customer cCur = vrp.getCustomer()[vehicle[i]];
				Customer cSucc = vrp.getCustomer()[vehicle[i+1]];
				StdDraw.line(cCur.getxCoord(), cCur.getyCoord(), cSucc.getxCoord(), cSucc.getyCoord());
			}

			//draw the line from the last customer back to the depot
			int lastCustomer = vehicle[vehicle.length-1];
			StdDraw.line(vrp.getCustomer()[lastCustomer].getxCoord(),vrp.getCustomer()[lastCustomer].getyCoord(),xDepot, yDepot);
		}


		StdDraw.show(0);
	}

	/**
	 * Main method for testing
	 * @param args
	 */
	public static void main(String[] args) {

	}
}
