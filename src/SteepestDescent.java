import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Class to apply steepest descent to a VRP-instance
 * @author Tom Decke
 *
 */
public class SteepestDescent extends Descent{

	private RandomSolution soln = null;

	/**
	 * Constructor for the steepest descent
	 * @param textfile
	 * @param customers
	 * @throws IOException
	 */
	public SteepestDescent(VRP vrp, String fOut) throws IOException {
		super(vrp,fOut);
	}

	/**
	 * Runs steepest descent, to find a solution for the vrp-instance
	 */
	public void solve(ArrayList<Operation> operators, boolean random) {


		//create operation matrix
		for(Operation op : operators) {
			op.createOptionMatrix();
		}

		//get the best move
		Option execute = operators.get(0).fetchBestOption();
		Option tmp = null;
		for(Operation op : operators) {
			tmp = op.fetchBestOption();
			if(tmp.getDelta() < execute.getDelta()) {
				execute = tmp;
			}
		}

		//get the vehicles
		Vehicle v1 = execute.getV1();
		Vehicle v2 = execute.getV2();


		int iterationCounter = 0;
		//As long as there are improving moves execute them
		while(execute.getDelta() < 0) {

			//Visualize the state before the relocation on the console
			iterationCounter++;
			System.out.println(iterationCounter);
			System.out.print("v1 - before move: ");
			v1.show();
			System.out.println("load :"+v1.load);
			System.out.print("v2 - before move: ");
			v2.show();
			System.out.println("load :"+ v2.load);
			execute.printOption();

			//execute the move
			execute.getOperation().executeOption(execute);


			//Visualize the state after the relocation on the console
			System.out.print("v1 - after move: ");
			v1.show();
			System.out.println("load :"+v1.load);
			System.out.print("v2 - after move: ");
			v2.show();
			System.out.println("load :"+ v2.load);
			System.out.println(" ");

			//update the move matrices
			for(Operation op : operators) {
				op.updateOptionMatrix(v1, v2);
			}

			//get the next best move
			execute = operators.get(0).fetchBestOption();
			for(Operation op : operators) {
				tmp = op.fetchBestOption();
				if(tmp.getDelta() < execute.getDelta()) {
					execute = tmp;
				}
			}

			//if the mode is random overwrite the found move
			if(random) {
				ArrayList<Option> options = new ArrayList<Option>();
				Option cur = null;
				for(Operation op : operators) {
					cur = op.fetchBestOption();
					if(cur.getDelta() != 0 ) {
						options.add(cur);
					}
				}

				//if no move is improving set execute to such, otherwise choose a random one
				if(options.isEmpty()) {
					execute = cur;
				}
				else {
					execute = options.get(new Random().nextInt(options.size()));
				}
			}

			//update the vehicles
			v1 = execute.getV1();
			v2 = execute.getV2();	
		}

		//TODO figure where the update has to take place
		//ensure up-to-date of the cost of the vehicles
//		for(Vehicle v : vrp.vehicle) {
//			//clear empty vehicles
//			if(v.firstCustomer.succ.equals(v.lastCustomer)) {
//				v.cost = 0;
//			}
//			else {
//				//re-evaluate the cost of occupied cars
//				double dist = 0;
//				Customer cCur = v.firstCustomer;
//				Customer cSucc = cCur.succ;
//				while(cSucc != null) {
//					dist += vrp.distance(cCur, cSucc);
//					cCur = cSucc;
//					cSucc = cSucc.succ;
//				}
//				v.cost = dist * v.costOfUse;
//			}
//		}

		printResultsToConsole();
		printResultsToFile();

		//if the solution was random, memorize the result
		if(random) {
			soln = new RandomSolution(super.getTotalCost(), super.getVehicleCount(),super.vrp.m, super.getVehicles());
		}
	}

	/**
	 * Accessor for the solution
	 * @return RandomSolution, the solution obtained by using random operations
	 */
	public RandomSolution getRandomSolution() {
		return this.soln;
	}
	

	@Override
	public void solve(int mode) {
		// TODO Auto-generated method stub

	}

	/**
	 * Main method for testing
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException{

		String in = args[0];
		int num = Integer.parseInt(args[1]);
		VRP vrp = new VRP(in, num);

		String fileOut = in.substring(0, in.length()-4);
		fileOut += "_Solution.txt";

		SteepestDescent stDesc = new SteepestDescent(vrp,fileOut);

		ArrayList<Operation> ops = new ArrayList<Operation>();
		
		RelocateOperation rlo = new RelocateOperation(vrp, num);
		ExchangeOperation exo = new ExchangeOperation(vrp, num);
		TwoOptOperation	  two = new TwoOptOperation(vrp, num);
		CrossExOperation  ceo = new CrossExOperation(vrp, num);
		ops.add(rlo);
// 		ops.add(exo);
// 		ops.add(two);
 		ops.add(ceo);
		

		stDesc.solve(ops, true);



		TestSolution.runTest(stDesc.vrp, stDesc.getTotalCost(), stDesc.getVehicles());
		DisplayVRP dVRP = new DisplayVRP(in, num, args[2]);
		dVRP.plotVRPSolution();
	}

}
