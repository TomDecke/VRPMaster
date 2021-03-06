package solver;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

import addOns.DisplayVRP;
import addOns.TestSolution;
import moves.Option;
import operators.CrossExOperation;
import operators.ExchangeOperation;
import operators.Operation;
import operators.RelocateOperation;
import operators.TwoOptOperation;
import representation.VRP;
import representation.Vehicle;

/**
 * Class to apply steepest descent to a VRP-instance
 * @author Tom Decke
 *
 */
public class SteepestDescent extends Descent{



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

		//get the involved vehicles
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
			System.out.print("v2 - before move: ");
			v2.show();
			execute.printOption();

			//execute the move
			execute.getOperation().executeOption(execute);


			//Visualize the state after the relocation on the console
			System.out.print("v1 - after move: ");
			v1.show();
			System.out.print("v2 - after move: ");
			v2.show();
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
					execute = operators.get(0).fetchBestOption();;
				}
				else {
					execute = options.get(new Random().nextInt(options.size()));
				}
			}

			//update the vehicles
			v1 = execute.getV1();
			v2 = execute.getV2();	
		}

		printResultsToConsole();
		printResultsToFile();

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
		ops.add(exo);
		ops.add(two);
		ops.add(ceo);


		stDesc.solve(ops, true);



		TestSolution.runTest(stDesc.vrp, stDesc.getTotalCost(), stDesc.getVehicles());
		DisplayVRP dVRP = new DisplayVRP(in, num, args[2]);
		dVRP.plotVRPSolution();
	}

}
