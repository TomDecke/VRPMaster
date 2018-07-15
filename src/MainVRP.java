import java.io.IOException;

public class MainVRP {
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
		stDesc.solve(0);


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
			//determine the name of the output-file
			String out = in.substring(0, in.length()-4);
			fileOut += "_Solution.txt";
			DisplayVRP dVRP = new DisplayVRP(in, numCustomer, out);
			dVRP.plotVRPSolution();
		}
		else {
			System.out.println(" ");
			System.err.println("invalid solution");		
		}
		


	}
}
