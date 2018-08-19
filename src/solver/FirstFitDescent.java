package solver;
import java.io.IOException;
import java.util.ArrayList;
import moves.Option;
import operators.Operation;
import operators.TwoOptOperation;
import representation.Customer;
import representation.VRP;
import representation.Vehicle;

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
		vehicles = new Vehicle[vrp.getM()];
		tOPt = new TwoOptOperation(vrp, super.numCustomers);

		//iterate through vehicles and customers
		for(int i = 0 ; i < vrp.getN() ; i++) {
			Vehicle vCur = vrp.getVehicle()[i];
			Customer cCur = vrp.getCustomer()[i+1];

			//clear the vehicles
			vCur.remove(cCur);
			//remember the non-penalizing vehicles
			if(i < vrp.getM()) {
				vehicles[i] = vCur;
			}

			double curStart = Math.max(cCur.getEarliestStart(), vrp.distance(vrp.getDepot(), cCur));
			int pos = 0;
			//order customers by distance taking earliest start into account
			while(pos < cDesc.size() && curStart  < Math.max(cDesc.get(pos).getEarliestStart(), vrp.distance(vrp.getDepot(), cDesc.get(pos)))) {
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
				Customer cCur = v.getFirstCustomer();
				Customer cSucc = cCur.getSucc();
				//find the first position where the customer can be inserted
				while(!cCur.equals(v.getLastCustomer())) {
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
					cSucc = cSucc.getSucc();
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

	}
}
