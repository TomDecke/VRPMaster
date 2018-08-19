import java.io.IOException;
import java.util.ArrayList;

/**
 * Class to apply first fit decreasing to a VRP-instance
 * @author Tom
 *
 */
public class FirstFitDescent extends Descent{

	private ArrayList<Customer> cDesc;
	private Vehicle[] vehicles;
	private TwoOptOperation tOPt;

	/**
	 * Constructor for the first fit descent
	 * @param vrp VRP, the problem-instance to which the descent is to be applied
	 * @param fOut String, the name for the file with the solution
	 */
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

	/**
	 * Run first fit descent to find a solution for the given VRP-instance
	 */
	public void solve(ArrayList<Operation> ops, boolean random) {
		Customer cCur = null;
		while(!cDesc.isEmpty()) {
			cCur = cDesc.remove(0);
			placeCustomer(cCur);
		}
		super.printResultsToFile();
	}

	/**
	 * Place a customer into the first vehicle which can accommodate it and execute a 2-opt
	 * @param c Customer, the customer to place into a vehicle
	 */
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

	/**
	 * Accessor for the VRP-instance
	 * @return VRP, the vrp-instance
	 */
	public VRP getVrp() {
		return vrp;
	}

	/**
	 * Accessor for the customers
	 * @return ArrayList<Customer>, the ordered customers
	 */
	public ArrayList<Customer> getcDesc() {
		return cDesc;
	}

	/**
	 * Main method for testing
	 * @param args
	 * @throws IOException
	 */
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

		ffd.solve(null,false);
		//TestSolution.runTest(vrp, ffd.getTotalCost(), ffd.getVehicles());
		DisplayVRP disp = new DisplayVRP(in, num, fileOut);
		disp.plotVRPSolution();
	}
}
