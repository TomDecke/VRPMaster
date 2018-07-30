import java.io.*;
import java.util.ArrayList;

/**
 * Class to obtain results from multiple input files in a given directory and test them for their validity
 * @author Tom Decke
 *
 */
public class RunDescents {

	private static final int RANDOM_RUNS = 3;

	public static void main(String[] args) throws IOException {

		//get information from the input
		String folderpath = args[0];
		int numCustomers = Integer.parseInt(args[1]);
		String resultpath = folderpath+"results\\";
		
		int[] modes = {0,1,2,3,4,5,6,7,8,9,10,11};


		//set up the descent, the problem instance and the operators
		Descent desc = null;
		VRP vrp = null; 
		ArrayList<Operation> ops = null;

		//create a folder for the results
		File result = new File(resultpath);
		result.mkdir();

		//get the files in the target directory
		File folder = new File(folderpath);
		File[] listOfFiles = folder.listFiles();

		//set up the output-file for all results
		FileWriter writer = new FileWriter(resultpath+"soln_"+numCustomers+".txt");

		//go through the files in the directory
		for (File file : listOfFiles) {
			if (file.isFile()) {
				String fInName = file.getName();

				writer.write(file.getName()+"\n");
				String vrpInstance = folderpath +fInName;

				
					//execute the the wanted modes for steepest descent 
					for(int i : modes) {

						vrp = new VRP(vrpInstance, numCustomers);
						ops = getMoves(vrp, numCustomers, i);

						//get the descent specified by the input
						desc = new SteepestDescent(vrp, resultpath + "mode_" + i + "_"+  fInName);

						//make sure that first descent only executes once, if chosen
						//solve the VRP-instance without use of random
						desc.solve(ops,false);

						//write the results to the output file
						writer.write("mode " + i + ": ");
						writer.write(String.format("cost: %.1f needed Vehicles: %d%n", desc.getTotalCost(),desc.getVehicleCount()));
					}

					//determine the random result
					
					//get problem instance
					vrp = new VRP(vrpInstance, numCustomers);
					//get operators //TODO choose the combination of working operators
					ops = getMoves(vrp, numCustomers, 0);
					//calculate the first random solution
					desc = new SteepestDescent(vrp, resultpath + "mode_r_"+  fInName);
					desc.solve(ops,true);

					//create the first random solution
					RandomSolution randSoln = new RandomSolution(desc.getTotalCost(), desc.getVehicleCount(), desc.getVRP().m, desc.getVehicles());

					//execute the random solver a given number of times and remember the one with the best result
					for(int i = 0 ; i < RANDOM_RUNS ; i++) {
						vrp = new VRP(vrpInstance, numCustomers);
						desc = new SteepestDescent(vrp, resultpath + "mode_r_"+  fInName);
						ops = getMoves(vrp, numCustomers, 11);
						desc.solve(ops,true);
						RandomSolution rsTmp = new RandomSolution(desc.getTotalCost(), desc.getVehicleCount(), desc.getVRP().m, desc.getVehicles());
						if(rsTmp.getCost() < randSoln.getCost()) {
							randSoln = rsTmp;
						}
					}

					//write the best result to the files
					randSoln.writeSolutionToFile(resultpath + "mode_rSoln_"+  file.getName());
					writer.write("mode r: ");
					writer.write(String.format("cost: %.1f needed Vehicles: %d%n", randSoln.getCost(),randSoln.getNeededV()));

					//write the result for the first fit descent
					vrp = new VRP(vrpInstance, numCustomers);
					desc = new FirstFitDescent(vrp, resultpath + "mode_FD_"+  fInName);
					desc.solve(null, false);
					writer.write("mode ffd: ");
					writer.write(String.format("cost: %.1f needed Vehicles: %d%n", desc.getTotalCost(),desc.getVehicleCount()));
					writer.write("\n");
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
	}

	/**
	 * Creates an array list containing different improvement operators
	 * @param vrp VRP, the problem instance to which the operators are to be applied
	 * @param numCustomer int, the number of customers in the vrp
	 * @param mode int, number corresponding to the combination of desired operators
	 * @return ArrayList<Operation>, the operators which are to be applied
	 */
	public static ArrayList<Operation> getMoves(VRP vrp, int numCustomer, int mode){
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
