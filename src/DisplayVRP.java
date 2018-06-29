import java.awt.Color;
import java.io.*;
import java.util.*;

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
	private int numCust;
	private int numVehicles;
	private ArrayList<int[]> vehicles; 
	
	public DisplayVRP(String vrpInstance, int numCust, String sol) {
		
		try {
			this.vrpInstance = vrpInstance;
			this.vrp = new VRP(vrpInstance,numCust);
			vehicles = new ArrayList<int[]>();
			
			this.xDepot = vrp.depot.xCoord;
			this.yDepot = vrp.depot.yCoord;
			
			//create reader to take in the solution
			FileReader reader;
			Scanner sc;
			reader = new FileReader(sol);
			sc = new Scanner(reader);

			//read number of customers and needed vehicles, then move to the next line
			numCust = sc.nextInt();
			numVehicles = sc.nextInt();
			sc.nextLine();
			
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
			xMax = vrp.customer[0].xCoord;
			xMin = xMax;
			yMax = vrp.customer[0].yCoord;
			yMin = yMax;
			for(Customer c : vrp.customer) {
				int currentX = c.xCoord;
				int currentY = c.yCoord;
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
	
	public double getCostSol() {
		return this.costSol;
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
	    for(Customer c : vrp.customer){
	    	StdDraw.point(c.xCoord, c.yCoord);
	    }
	    
	    StdDraw.show(0);
	}
	
	/**
	 * Draw the 
	 */
	public void plotVRPSolution() {
		//set up a new plot
		StdDraw.clear(StdDraw.WHITE);
		StdDraw.setXscale(xMin, xMax);
	    StdDraw.setYscale(yMin, yMax);
	    
	    StdDraw.textLeft(xMin,yMin,vrpInstance.substring(vrpInstance.length()-20, vrpInstance.length()));
	    StdDraw.textRight(xMax,yMin,String.format("Distance: %.3f", costSol));
	    
	    //determine customer size and print them to the map
	    StdDraw.setPenRadius(0.005);
	    for(Customer c : vrp.customer){
	    	StdDraw.point(c.xCoord, c.yCoord);
	    }
	    StdDraw.setPenRadius(0.0005);
	    int colourCount = 0;
	    for(int[] vehicle : vehicles) {
	    	//choose a random color for the vehicle route - StdDraw.setPenColor(new Color((int)(Math.random() * 0x1000000)));
		    StdDraw.setPenColor(MyColours.getColour((colourCount)));
		    
		    //draw the line from the depot to the first customer
		    int firstCustomer = vehicle[0];
		    StdDraw.line(xDepot, yDepot,vrp.customer[firstCustomer].xCoord,vrp.customer[firstCustomer].yCoord);
		    
		    //draw the tour-intermediates
	    	for(int i = 0; i< vehicle.length-1; i++) {
	    		Customer cCur = vrp.customer[vehicle[i]];
	    		Customer cSucc = vrp.customer[vehicle[i+1]];
	    		StdDraw.line(cCur.xCoord, cCur.yCoord, cSucc.xCoord, cSucc.yCoord);
	    	}
	    	
	    	//draw the line from the last customer back to the depot
	    	int lastCustomer = vehicle[vehicle.length-1];
		    StdDraw.line(vrp.customer[lastCustomer].xCoord,vrp.customer[lastCustomer].yCoord,xDepot, yDepot);
	    	colourCount++;
	    }
	    
	    
	    StdDraw.show(0);
	}
	
	public static void main(String[] args) {
		DisplayVRP dVRP = new DisplayVRP(args[0], Integer.parseInt(args[1]), args[2]);

		System.out.println("Costs: " + dVRP.getCostSol());
		for (int[] sa : dVRP.vehicles) {
			for (int s : sa) {
				System.out.print(s);
				System.out.print(" ");
			}
			System.out.println(" ");
		}
		
		dVRP.plotVRPInstance();
		dVRP.plotVRPSolution();
	}
}
