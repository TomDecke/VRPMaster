import java.io.*;
import java.util.*;

/**
 * Class to visualize the solution of a VRP-instance
 * @author Tom Decke
 *
 */
public class DisplayVRP {
	
	private VRP vrp;
	private double costSol;
	public ArrayList<String[]> vehicles; 
	
	public DisplayVRP(String vrp, int numCost, String sol) {
		try {
			this.vrp = new VRP(vrp,numCost);
			
			vehicles = new ArrayList<String[]>();
			
			FileReader reader;
			Scanner sc;
			reader = new FileReader(sol);
			sc = new Scanner(reader);

			if(sc.hasNextLine()) {
				costSol = Double.parseDouble(sc.nextLine());	
			}
			while(sc.hasNextLine()) {
				vehicles.add(sc.nextLine().split(" "));
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
	
	
	public static void main(String[] args) {
		DisplayVRP dVRP = new DisplayVRP(args[0], Integer.parseInt(args[1]), args[2]);

		System.out.println("Dem costs: " + dVRP.getCostSol());
		for (String[] sa : dVRP.vehicles) {
			for (String s : sa) {
				System.out.print(s);
				System.out.print(" ");
			}
			System.out.println(" ");
		}
	}
}
