import java.io.*;

/**
 * Class to obtain results from multiple input files in a given directory
 * @author Tom Decke
 *
 */
public class RunDescents {

	public static void main(String[] args) throws IOException {
		//get information from the input
		String folderpath = args[0];
		String resultpath = folderpath+"results\\";
		int numCustomers = Integer.parseInt(args[1]);
		boolean steepest = Boolean.getBoolean(args[2]);

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

				//execute every mode for steepest descent
				for(int i = 0; i < 5; i++) {
					
					vrp = new VRP(vrpInstance, numCustomers);
					//get the descent specified by the input
					if(steepest) {
						desc = new SteepestDescent(vrp, resultpath + "mode_" + i + "_"+  file.getName());
					}
					else {
						desc = new FirstFitDescent(vrp, resultpath + "mode_" + i + "_"+  file.getName());
					}
					//make sure that first descent only executes once, if chosen
					if(steepest || i%5==0) {
						//solve the VRP-instance
						desc.solve(i);
						//write the results to the output file
						writer.write("mode " + i + ": ");
						System.out.println(String.format("c: %.1f nV: %d ", desc.getTotalCost(),desc.getVehicleCount()));
						writer.write(String.format("cost: %.1f needed Vehicles: %d%n", desc.getTotalCost(),desc.getVehicleCount()));
					}
				}
				writer.write("\n");
			}
		}
		writer.close();
	}
}
