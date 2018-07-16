import java.io.*;

/**
 * Class to obtain results from multiple input files
 * @author Tom Decke
 *
 */
public class RunDescents {

	public static void main(String[] args) throws IOException {
		String folderpath = args[0];
		String resultpath = args[1];
		int numCustomers = Integer.parseInt(args[2]);
		int mode = Integer.parseInt(args[3]);
		
		SteepestDescent stDesc = null;
		VRP vrp = null; 
		
		File folder = new File(folderpath);
		File[] listOfFiles = folder.listFiles();

		for (File file : listOfFiles) {
		    if (file.isFile()) {
		    	String vrpInstance = folderpath +file.getName(); 
		        System.out.println(vrpInstance);
		        vrp = new VRP(vrpInstance, numCustomers);
		        stDesc = new SteepestDescent(vrp, resultpath+file.getName());
		        stDesc.solve(mode);
		    }
		}
	}
}
