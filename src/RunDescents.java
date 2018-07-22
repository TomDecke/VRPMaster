import java.io.*;
import java.util.ArrayList;

/**
 * Class to obtain results from multiple input files in a given directory
 * @author Tom Decke
 *
 */
public class RunDescents {

	private static final int RANDOM_RUNS = 8;

	public static void main(String[] args) throws IOException {
		
		//get information from the input
		String folderpath = args[0];
		String resultpath = folderpath+"results\\";
		int numCustomers = Integer.parseInt(args[1]);
		
		boolean steepest = true;
		ArrayList<Operation> ops = null;
		boolean rand = false;

		//set up the descent and the problem instance
		Descent desc = null;
		VRP vrp = null; 

		//create a folder for the results
		File result = new File(resultpath);
		result.mkdir();

		//get the files in the target directory
		File folder = new File(folderpath);
		File[] listOfFiles = folder.listFiles();

		//set up the output-file
		FileWriter writer = new FileWriter(resultpath+"soln_"+numCustomers+".txt");

		//go through the files in the directory
		for (File file : listOfFiles) {
			if (file.isFile()) {

				writer.write(file.getName()+"\n");
				String vrpInstance = folderpath +file.getName();

				//execute the first four modes for steepest descent 
				for(int i = 10; i < 12; i++) {

					vrp = new VRP(vrpInstance, numCustomers);
					ops = getMoves(vrp, numCustomers, i);
					//get the descent specified by the input
					if(steepest) {
						desc = new SteepestDescent(vrp, resultpath + "mode_" + i + "_"+  file.getName());
					}
					else {
						desc = new FirstFitDescent(vrp, resultpath + "mode_" + i + "_"+  file.getName());
					}
					//make sure that first descent only executes once, if chosen
					if(steepest) {
						//solve the VRP-instance
						desc.solve(ops,rand);
						//write the results to the output file
						writer.write("mode " + i + ": ");
						writer.write(String.format("cost: %.1f needed Vehicles: %d%n", desc.getTotalCost(),desc.getVehicleCount()));
					}
				}

				//determine the random result
				if(rand) {
					
					//get problem instance
					vrp = new VRP(vrpInstance, numCustomers);
					//get operators
					ops = getMoves(vrp, numCustomers, 11);
					desc = new SteepestDescent(vrp, resultpath + "mode_r_"+  file.getName());
					desc.solve(ops,rand);
					
					RandomSolution randSoln = new RandomSolution(desc.getTotalCost(), desc.getVehicleCount(), desc.getVRP().m, desc.getVehicles());
				for(int i = 0 ; i < RANDOM_RUNS ; i++) {
						vrp = new VRP(vrpInstance, numCustomers);
						desc = new SteepestDescent(vrp, resultpath + "mode_r_"+  file.getName());
						ops = getMoves(vrp, numCustomers, 11);
						desc.solve(ops,rand);
						RandomSolution rsTmp = new RandomSolution(desc.getTotalCost(), desc.getVehicleCount(), desc.getVRP().m, desc.getVehicles());
						if(rsTmp.getCost() < randSoln.getCost()) {
							randSoln = rsTmp;
						}
					}

					randSoln.writeSolutionToFile(resultpath + "mode_rSoln_"+  file.getName());
					writer.write("mode r: ");
					writer.write(String.format("cost: %.1f needed Vehicles: %d%n", randSoln.getCost(),randSoln.getNeededV()));
					writer.write("\n");

				}
			}
		}
		writer.close();
		
		

		//Test the solutions
		//path to the solution
		String sPath = folderpath+"results";
		File resultFiles = new File(sPath);

		//go through all solutions in the given directory
		File[] listOfResults = resultFiles.listFiles();
		for (File file : listOfResults) {
			if (file.isFile()) {
				String fName = file.getName();
				String[] name = fName.split("_");
				String vrpName = folderpath+name[2];
				//test if the solution is valid
				System.out.println(fName);
				boolean testResult = TestSolution.testFile(vrpName, numCustomers,file.getAbsolutePath());
				if(!testResult) {
					System.out.println("Invalid solution: " + name[2]);
				}
			}
		}
		System.out.println("Valid solution!");
	}
	
	
	private static ArrayList<Operation> getMoves(VRP vrp, int numCustomer, int mode){
		ArrayList<Operation> ops = new ArrayList<Operation>();
		RelocateOperation rlo = new RelocateOperation(vrp, numCustomer);
		ExchangeOperation exo = new ExchangeOperation(vrp, numCustomer);
		TwoOptOperation	  two = new TwoOptOperation(vrp, numCustomer);
		CrossExOperation  ceo = new CrossExOperation(vrp, numCustomer);
		
		switch(mode) {
		case 0:
			ops.add(rlo);
			break;
		case 1:
			ops.add(rlo);
			ops.add(exo);
			break;
		case 2:
			ops.add(rlo);
			ops.add(two);
			break;
		case 3:
			ops.add(rlo);
			ops.add(exo);
			ops.add(two);
			break;
		case 4: 
			ops.add(ceo);
			break;
		case 5:
			ops.add(ceo);
			ops.add(two);
			break;
		case 6: 
			ops.add(ceo);
			ops.add(exo);
			break;
		case 7:
			ops.add(ceo);
			ops.add(rlo);
			break;
		case 8:
			ops.add(ceo);
			ops.add(exo);
			ops.add(two);
			break;
		case 9:
			ops.add(ceo);
			ops.add(rlo);
			ops.add(two);
			break;
		case 10:
			ops.add(ceo);
			ops.add(rlo);
			ops.add(exo);
			break;
		case 11:
			ops.add(rlo);
			ops.add(exo);
			ops.add(two);
			ops.add(ceo);
			break;
		
		}
		return ops;
	}
}
