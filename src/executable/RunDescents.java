package executable;
import java.io.*;
import java.util.ArrayList;

import addOns.RandomSolution;
import addOns.TestSolution;
import operators.CrossExOperation;
import operators.ExchangeOperation;
import operators.Operation;
import operators.RelocateOperation;
import operators.TwoOptOperation;
import representation.VRP;
import solver.Descent;
import solver.FirstFitDescent;
import solver.SteepestDescent;

/**
 * Class to obtain results from multiple input files in a given directory and test them for their validity
 * @author Tom Decke
 *
 */
public class RunDescents {

	private static final int RANDOM_RUNS = 9;

	public static void main(String[] args) throws IOException {

		//get information from the input
		String folderpath = args[0];
		int numCustomers = Integer.parseInt(args[1]);
		String resultpath = folderpath+"results\\";

		int[] modes = {0,1,2,4,6,5,7,3,8,9,10,11};
		long[] times = new long[14];
		
		long t0 = 0;
		long t1 = 0;


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
		FileWriter w2 = new FileWriter(resultpath+"time_"+numCustomers+".txt");

		long time = System.currentTimeMillis();
		//go through the files in the directory
		for (File file : listOfFiles) {
			if (file.isFile()) {
				String fInName = file.getName();

				writer.write(file.getName()+"\n");
				String vrpInstance = folderpath +fInName;


				//execute the the wanted modes for steepest descent 
				for(int i : modes) {

					//get the system time
					t0 = System.currentTimeMillis();
					
					vrp = new VRP(vrpInstance, numCustomers);
					ops = getMoves(vrp, numCustomers, i);

					//get the descent specified by the input
					desc = new SteepestDescent(vrp, resultpath + "mode_" + i + "_"+  fInName);

					
					//make sure that first descent only executes once, if chosen
					//solve the VRP-instance without use of random
					desc.solve(ops,false);

					
					//write the results to the output file in the format cost/vehicles
					writer.write(String.format(" & %.2f/%d", desc.getTotalCost(),desc.getVehicleCount()));
					if(i == 7) {
						writer.write(String.format("%n"));
					}
					
					//write the time
					t1 = System.currentTimeMillis();
					times[i] += t1-t0;
				}

				//determine the random result
				t0 = System.currentTimeMillis();
				
				//get problem instance
				vrp = new VRP(vrpInstance, numCustomers);
				//get operators
				ops = getMoves(vrp, numCustomers, 11);
				//calculate the first random solution
				desc = new SteepestDescent(vrp, resultpath + "mode_r_"+  fInName);
				desc.solve(ops,true);

				//create the first random solution
				RandomSolution randSoln = new RandomSolution(desc.getTotalCost(), desc.getVehicleCount(), desc.getVRP().getM(), desc.getVehicles());
				

				//execute the random solver a given number of times and remember the one with the best result
				for(int i = 0 ; i < RANDOM_RUNS ; i++) {
					vrp = new VRP(vrpInstance, numCustomers);
					desc = new SteepestDescent(vrp, resultpath + "mode_r_"+  fInName);
					ops = getMoves(vrp, numCustomers, 11);

					desc.solve(ops,true);
					RandomSolution rsTmp = new RandomSolution(desc.getTotalCost(), desc.getVehicleCount(), desc.getVRP().getM(), desc.getVehicles());
					if(rsTmp.getCost() < randSoln.getCost()) {
						randSoln = rsTmp;
					}
				}

						
				//write the best result to the files
				randSoln.writeSolutionToFile(resultpath + "mode_rSoln_"+  file.getName());
				writer.write(String.format(" & %.2f/%d", randSoln.getCost(),randSoln.getNeededV()));

				times[12] += System.currentTimeMillis() - t0;

				//write the result for the first fit descent
				t0 = System.currentTimeMillis();
				vrp = new VRP(vrpInstance, numCustomers);
				desc = new FirstFitDescent(vrp, resultpath + "mode_FD_"+  fInName);
			
				
				desc.solve(null, false);
							
				
				writer.write(String.format(" & %.2f/%d%n", desc.getTotalCost(),desc.getVehicleCount()));
				writer.write("\n");
				t1 = System.currentTimeMillis();
				times[13] += t1-t0;
			}
		}
		writer.close();
		System.out.println((System.currentTimeMillis()-time));
		
		int counter = 0;
		for(long l : times) {
			w2.write(counter + " "+ l + "\n");
			counter++;
		}
		w2.close();
		


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
