import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class TestFiles {

	public static void main(String[] args) throws IOException {
		ArrayList<Boolean> testResult = new ArrayList<Boolean>();

		//path to the vrp-instance
		String pPath = args[0];
		//path to the solution
		String sPath = args[1];
		File folder = new File(sPath);

		//go through all solutions in the given directory
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles) {
			if (file.isFile()) {
				String fName = file.getPath();
				String vrpName = pPath+fName.substring(fName.length()-8, fName.length());
				//test if the solution is valid
				boolean result = TestSolution.testFile(vrpName,file.getAbsolutePath());
				if(!result) {
					System.out.println("Invalid solution");
					return;
				}
			}
		}
		System.out.println("Valid solution!");
	}
}
