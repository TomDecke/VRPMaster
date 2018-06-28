import java.awt.Color;
import java.io.*;
import java.util.*;

/**
 * Class to visualize the solution of a VRP-instance
 * @author Tom Decke
 *
 */
public class DisplayVRP {
	
	private int xMax, xMin, yMax, yMin;
	private VRP vrp;
	private double costSol;
	private ArrayList<int[]> vehicles; 
	
	public DisplayVRP(String vrpInstance, int numCost, String sol) {
		
		try {
			this.vrp = new VRP(vrpInstance,numCost);
			vehicles = new ArrayList<int[]>();
			
			//create reader to take in the solution
			FileReader reader;
			Scanner sc;
			reader = new FileReader(sol);
			sc = new Scanner(reader);

			//get the cost proposed by the solution
			if(sc.hasNextLine()) {
				costSol = Double.parseDouble(sc.nextLine());	
			}
			//get the routes proposed by the solution
			while(sc.hasNextLine()) {
				//read the vehicle and copy it's customers into an int-array
				String[] cInfo =sc.nextLine().split(" ");
				int[] customers = new int[cInfo.length];
				for(int i = 0; i < cInfo.length; i++) {
					customers[i] = Integer.parseInt(cInfo[i]);
				}
				vehicles.add(customers);
			}
			
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
	
	public void plotVRPInstance() {
		//set up a new plot
		StdDraw.clear(StdDraw.WHITE);
		StdDraw.setXscale(xMin, xMax);
	    StdDraw.setYscale(yMin, yMax);
	    
	    //determine customer size and print them to the map
	    StdDraw.setPenRadius(0.005);
	    for(Customer c : vrp.customer){
	    	StdDraw.point(c.xCoord, c.yCoord);
	    }
	    
	    StdDraw.show(0);
	}
	
	public void plotVRPSolution() {
		//set up a new plot
		StdDraw.clear(StdDraw.WHITE);
		StdDraw.setXscale(xMin, xMax);
	    StdDraw.setYscale(yMin, yMax);
	    
	    StdDraw.textLeft(xMin,yMin,String.format("Distance %.3f", costSol));
	    
	    //determine customer size and print them to the map
	    StdDraw.setPenRadius(0.005);
	    for(Customer c : vrp.customer){
	    	StdDraw.point(c.xCoord, c.yCoord);
	    }
	    StdDraw.setPenRadius(0.0005);
	    for(int[] vehicle : vehicles) {
		    StdDraw.setPenColor(new Color((int)(Math.random() * 0x1000000)));
	    	for(int i = 0; i< vehicle.length-1; i++) {
	    		Customer cCur = vrp.customer[vehicle[i]];
	    		Customer cSucc = vrp.customer[vehicle[i+1]];
	    		StdDraw.line(cCur.xCoord, cCur.yCoord, cSucc.xCoord, cSucc.yCoord);
	    	}
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
