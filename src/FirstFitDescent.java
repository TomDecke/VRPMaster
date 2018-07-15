import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FirstFitDescent extends Descent{


	private ArrayList<Customer> cDesc;
	private Vehicle[] vehicles;
	
	public FirstFitDescent(VRP vrp, int numCustomers, String fOut) {
		super(vrp,numCustomers,fOut);
		cDesc = new ArrayList<Customer>();
		vehicles = new Vehicle[vrp.m];
		
		//iterate through vehicles and customers
		for(int i = 0 ; i < vrp.n ; i++) {
			Vehicle vCur = vrp.vehicle[i];
			Customer cCur = vrp.customer[i+1];
			
			//clear the vehicles
			vCur.remove(cCur);
			//remember the non-penalizing vehicles
			if(i < vrp.m) {
				vehicles[i] = vCur;
			}
			
			double dist = vrp.distance(vrp.depot, cCur);
			int pos = 0;
			//order customers by distance
			while(pos < cDesc.size() && dist < vrp.distance(vrp.depot, cDesc.get(pos))) {
				pos++;
			}
			cDesc.add(pos, cCur);
		}	
	}
	
	public void solve() {
		Customer cCur = null;
		while(!cDesc.isEmpty()) {
			cCur = cDesc.remove(0);
			placeCustomer(cCur);
		}
		super.printResultsToFile();
	}
	
	private void placeCustomer(Customer c) {
		for(Vehicle v : vehicles) {
			if(v.canAccomodate(c)) {
				if(c.canBeInsertedBetween(v.lastCustomer.pred, v.lastCustomer)) {
					v.insertBetween(c, v.lastCustomer.pred, v.lastCustomer);
					return;
				}
			}
		}
	}
	
	
	public VRP getVrp() {
		return vrp;
	}

	public ArrayList<Customer> getcDesc() {
		return cDesc;
	}


	public static void main(String[] args) throws IOException {
		String in = args[0];
		int num = Integer.parseInt(args[1]);
		
		VRP vrp = new VRP(in,num);
		
		String fileOut = in.substring(0, in.length()-4);
		fileOut += "_Solution.txt";
		
		FirstFitDescent ffd = new FirstFitDescent(vrp,num,fileOut);
		
		ffd.solve();
		TestSolution.runTest(vrp, ffd.getTotalCost(), ffd.getVehicles());
		DisplayVRP disp = new DisplayVRP(in, num, fileOut);
		disp.plotVRPSolution();
	}
}
