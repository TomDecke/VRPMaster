import java.io.IOException;
import java.util.ArrayList;

/**
 * Main method to obtain solutions for VRPs
 * @author Tom
 *
 */
public class MainVRP {
	public static void main(String[] args) throws IOException{

		//get the input
		String in = args[0];
		int numCustomer = Integer.parseInt(args[1]);
		int mode = Integer.parseInt(args[2]);
		String random = args[3];
		boolean rand = false;
		if(random.equals("random")) {
			rand = true;
		}

		String fileOut = in.substring(0, in.length()-4);
		fileOut += "_Solution.txt";


		//create verification instance and solver
		VRP vrp = new VRP(in,numCustomer);
		SteepestDescent stDesc = new SteepestDescent(vrp,fileOut);

		//run the solver
		System.out.println("");
		ArrayList<Operation> ops = RunDescents.getMoves(vrp, numCustomer, mode);


		stDesc.solve(ops, rand);



		//show all vehicles after the search finished
		for(int i = 0 ; i<numCustomer; i++) {
			Vehicle v = stDesc.getVRP().vehicle[i];
			System.out.println("Customer of vehicle "+v.id +": " +v.firstCustomer.succ.toString());
			v.show();
			System.out.println("Cost for vehicle "+v.id+": "+v.cost);	
		}
		System.out.println(" ");

		//print the results of the steepest descent
		System.out.println("Results:");
		stDesc.printResultsToConsole();

		//test the solution
		System.out.println("Test:");
		boolean valid = TestSolution.runTest(vrp, stDesc.getTotalCost(), stDesc.getVehicles());

		//if the solution is valid display it otherwise display a failure-message
		if(valid) {
			DisplayVRP dVRP = new DisplayVRP(in, numCustomer, fileOut);
			dVRP.plotVRPSolution();
		}
		else {
			System.out.println(" ");
			System.err.println("invalid solution");		
		}


		System.out.println(fileOut);

	}
}
