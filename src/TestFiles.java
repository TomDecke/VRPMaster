import java.io.*;

public class TestFiles {

	public static void main(String[] args) throws IOException {

		//path to the vrp-instance
		String pPath = args[0];
		int numCustomer = Integer.parseInt(args[1]);
		//path to the solution
		String sPath = args[2];
		File folder = new File(sPath);

		//go through all solutions in the given directory
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles) {
			if (file.isFile()) {
				String fName = file.getName();
				String[] name = fName.split("_");
				String vrpName = pPath+name[2];
				//test if the solution is valid
				System.out.println(fName);
				boolean result = TestSolution.testFile(vrpName, numCustomer,file.getAbsolutePath());
				if(!result) {
					System.out.println("Invalid solution: " + name[2]);
				}
			}
		}
		System.out.println("Valid solution!");
	}
}
