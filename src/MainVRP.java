import java.io.IOException;
import java.util.ArrayList;

/**
 * Main method to obtain solutions for VRPs
 * @author Tom
 *
 */
public class MainVRP {
	//TODO update the main
	public static void main(String[] args) throws IOException{

		//get the input
		String in = args[0];
		int numCustomer = Integer.parseInt(args[1]);

		String fileOut = in.substring(0, in.length()-4);
		fileOut += "_Solution.txt";


		//create verification instance and solver
		VRP vrp = new VRP(in,numCustomer);
		SteepestDescent stDesc = new SteepestDescent(vrp,fileOut);

		//run the solver
		System.out.println("");
		ArrayList<Operation> ops = new ArrayList<Operation>();

		//instantiate the heuristics
		RelocateOperation rlo = new RelocateOperation(vrp, numCustomer);
		ExchangeOperation exo = new ExchangeOperation(vrp, numCustomer);
		TwoOptOperation	  two = new TwoOptOperation(vrp, numCustomer);
		CrossExOperation  ceo = new CrossExOperation(vrp, numCustomer);
		ops.add(rlo);
		ops.add(exo);
		ops.add(two);
		ops.add(ceo);


		stDesc.solve(ops, true);



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
