import java.io.*;

/**
 * Class to obtain results from multiple input files in a given directory
 * @author Tom Decke
 *
 */
public class RunDescents {
	
	private static final int RANDOM_RUNS = 10;

	public static void main(String[] args) throws IOException {
		//get information from the input
		String folderpath = args[0];
		String resultpath = folderpath+"results\\";
		int numCustomers = Integer.parseInt(args[1]);
		boolean steepest = true;

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
				for(int i = 0; i < 4; i++) {
					
					vrp = new VRP(vrpInstance, numCustomers);
					//get the descent specified by the input
					if(steepest) {
						desc = new SteepestDescent(vrp, resultpath + "mode_" + i + "_"+  file.getName());
					}
					else {
						desc = new FirstFitDescent(vrp, resultpath + "mode_" + i + "_"+  file.getName());
					}
					//make sure that first descent only executes once, if chosen
					if(steepest || i%4==0) {
						//solve the VRP-instance
						desc.solve(i);
						//write the results to the output file
						writer.write("mode " + i + ": ");
						writer.write(String.format("cost: %.1f needed Vehicles: %d%n", desc.getTotalCost(),desc.getVehicleCount()));
					}
				}
				
				//determine the random result
				if(steepest) {
					SteepestDescent stDesc = new SteepestDescent(vrp, resultpath + "mode_4_"+  file.getName());
					stDesc.solve(4);
					RandomSolution rs = stDesc.getRandomSolution();
					for(int i = 0 ; i < RANDOM_RUNS ; i++) {
						vrp = new VRP(vrpInstance, numCustomers);
						stDesc = new SteepestDescent(vrp, resultpath + "mode_4_"+  file.getName());
						stDesc.solve(4);
						rs.compare(stDesc.getRandomSolution());
						
					}
					writer.write("mode rand:");
					writer.write(String.format("cost: %.1f needed Vehicles: %d%n", rs.getCost(),rs.getNeededV()));
					writer.write("\n");
				}
			}
		}
		writer.close();
	}
}
