import java.io.IOException;
import java.util.ArrayList;

public class FirstFitDescent extends Descent{


	private ArrayList<Customer> cDesc;
	private Vehicle[] vehicles;
	private TwoOptOperation tOPt;
	
	public FirstFitDescent(VRP vrp, String fOut) {
		super(vrp,fOut);
		cDesc = new ArrayList<Customer>();
		vehicles = new Vehicle[vrp.m];
		tOPt = new TwoOptOperation(vrp, super.numCustomers);
		
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
			
			double curStart = Math.max(cCur.earliestStart, vrp.distance(vrp.depot, cCur));
			int pos = 0;
			//order customers by distance taking earliest start into account
			while(pos < cDesc.size() && curStart  < Math.max(cDesc.get(pos).earliestStart, vrp.distance(vrp.depot, cDesc.get(pos)))) {
				pos++;
			}
			cDesc.add(pos, cCur);
		}	
	}
	
	public void solve(int mode) {
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
				Customer cCur = v.firstCustomer;
				Customer cSucc = cCur.succ;
				//find the first position where the customer can be inserted
				while(!cCur.equals(v.lastCustomer)) {
					if(c.canBeInsertedBetween(cCur, cSucc)) {
						v.insertBetween(c, cCur, cSucc);
						
						//if it means improvement, execute a 2-opt-move
						Option twoOpt = tOPt.findBestOption(v, v);
						if(twoOpt.getDelta() < 0) {
							executeMove(twoOpt);
						}
						return;	
					}
					cCur = cSucc;
					cSucc = cSucc.succ;
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
		
		FirstFitDescent ffd = new FirstFitDescent(vrp,fileOut);
		
		for(Customer c: ffd.getcDesc()) {
			System.out.println(""+c.custNo +  " " + c.earliestStart);
		}
		
		ffd.solve(-1);
		//TestSolution.runTest(vrp, ffd.getTotalCost(), ffd.getVehicles());
		DisplayVRP disp = new DisplayVRP(in, num, fileOut);
		disp.plotVRPSolution();
	}
}
