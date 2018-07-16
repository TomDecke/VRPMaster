import java.io.*;

/**
 * Class to obtain results from multiple input files in a given directory
 * @author Tom Decke
 *
 */
public class RunDescents {

	public static void main(String[] args) throws IOException {
		String folderpath = args[0];
		String resultpath = folderpath+"results\\";
		int numCustomers = Integer.parseInt(args[1]);
		
		SteepestDescent stDesc = null;
		VRP vrp = null; 
		
		File folder = new File(folderpath);
		File[] listOfFiles = folder.listFiles();

		File result = new File(resultpath);
		result.mkdir();
		
		FileWriter writer = new FileWriter(resultpath+"soln_"+numCustomers+".txt");
		
		for (File file : listOfFiles) {
		    if (file.isFile()) {
		    	writer.write(file.getName()+"\n");
		    	String vrpInstance = folderpath +file.getName();
		        vrp = new VRP(vrpInstance, numCustomers);
		        for(int i = 0; i < 5; i++) {
		        	writer.write("mode " + i + ": ");
			        stDesc = new SteepestDescent(vrp, resultpath + "mode_" + i + "_"+  file.getName());
			        stDesc.solve(i);
			        System.out.println(String.format("c: %.1f nV: %d ", stDesc.getTotalCost(),stDesc.getVehicleCount()));
			        writer.write(String.format("cost: %.1f needed Vehicles: %d%n", stDesc.getTotalCost(),stDesc.getVehicleCount()));
		        }
		        writer.write("\n");

		    }
		}
		
		writer.close();
	}
}
