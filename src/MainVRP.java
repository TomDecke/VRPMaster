import java.io.IOException;

public class MainVRP {
	public static void main(String[] args) throws IOException{
		SteepestDescent stDesc = new SteepestDescent(args[0],Integer.parseInt(args[1]));
		VRP vrp = new VRP(args[0],Integer.parseInt(args[1]));
		stDesc.createBMM();
		stDesc.printBMM();
		System.out.println("");
		stDesc.solve();
		stDesc.printBMM();
		
		for(int i = 0 ; i<Integer.parseInt(args[1]); i++) {
			Vehicle v = stDesc.getVRP().vehicle[i];
			System.out.println("Customer of vehicle "+v.id +": " +v.firstCustomer.succ.toString());
			v.show();
			System.out.println("Cost for vehicle "+v.id+": "+v.cost);	
		}
		
		System.out.println(" ");
		System.out.println("Results:");
		stDesc.printResultsToConsole();
		
		//stDesc.printResultsToFile();
		
		TestSolution ts = new TestSolution(vrp, stDesc.getTotalCost(), stDesc.getVehicles());
		System.out.println("Test:");
		if(ts.runTest()) {
			System.out.println(" ");
			System.out.println("valid solution");
		}
		else {
			System.out.println(" ");
			System.out.println("invalid solution");		
		}
		
		
		DisplayVRP dVRP = new DisplayVRP(args[0], Integer.parseInt(args[1]), args[2]);

		System.out.println("Costs: " + dVRP.getCostSol());
		for (int[] sa : dVRP.getVehicles()) {
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
