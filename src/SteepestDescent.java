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
	private CrossExOperation ceo;
	private RelocateOperation ro;
	private ExchangeOperation eo;
	private TwoOptOperation to;
	private RandomSolution soln = null;

	/**
	 * Constructor for the steepest descent
	 * @param textfile
	 * @param customers
	 * @throws IOException
	 */
	public SteepestDescent(VRP vrp, String fOut) throws IOException {
		super(vrp,fOut);
		this.ro = new RelocateOperation(vrp, super.numCustomers);
		this.eo = new ExchangeOperation(vrp, super.numCustomers);
		this.to = new TwoOptOperation(vrp, super.numCustomers);
		this.ceo = new CrossExOperation(vrp, super.numCustomers);
	}

	/**
	 * Runs steepest descent, to find a solution for the vrp-instance
	 */
	public void solve(int mode) {

		//create best-move-matrix and print it to the console
		ro.createOptionMatrix();
		eo.createOptionMatrix();
		to.createOptionMatrix();

		ro.printRelocateMatrix();
		System.out.println(" ");

		//find the first best move
		Option execute = ro.fetchBestOption();

		//set up exchange
		Option optionExchange = eo.fetchBestOption();
		Option optionTwoOpt = to.fetchBestOption();

		//get the vehicles
		Vehicle v1 = execute.getV1();
		Vehicle v2 = execute.getV2();


		int iterationCounter = 0;
		//As long as there are improving moves execute them
		while(execute.getDelta() < 0) {

			//Visualize the state before the relocation on the console
			iterationCounter++;
			System.out.println(iterationCounter);
			//uncomment if wanted printBMM();
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

			//update relocate and find best move for comparison
			ro.updateOptionMatrix(v1,v2);
			execute = ro.fetchBestOption();

			//combine different moves
			switch(mode) {
			//only relocate
			case 0:
				break;

				//relocate and exchange
			case 1:
				//TODO figure out eo-update
				//update exchange and compare option with the current result
				eo.createOptionMatrix();
				optionExchange = eo.fetchBestOption();
				if(optionExchange.getDelta() < execute.getDelta()) {
					execute = optionExchange;
				}
				break;

				//relocate and two-opt
			case 2:
				//update two-opt and compare option with current result
				to.updateOptionMatrix(v1,v2);
				optionTwoOpt = to.fetchBestOption();
				if(optionTwoOpt.getDelta() < execute.getDelta()) {
					execute = optionTwoOpt;
				}
				break;

				//relocate, exchange and two-opt
			case 3:
				//update exchange and two-opt
				eo.createOptionMatrix();
				to.updateOptionMatrix(v1, v2);
				optionExchange = eo.fetchBestOption();
				optionTwoOpt = to.fetchBestOption();
				//find the best option
				if(optionExchange.getDelta() < execute.getDelta()) {
					execute = optionExchange;
				}
				if(optionTwoOpt.getDelta() < execute.getDelta()) {
					execute = optionTwoOpt;
				}
				break;

				//randomly selected improvement move
			case 4: 
				//update all matrices
				eo.createOptionMatrix();
				to.updateOptionMatrix(v1, v2);

				optionExchange = eo.fetchBestOption();
				optionTwoOpt = to.fetchBestOption();

				double dEx = optionExchange.getDelta();
				double d2opt = optionTwoOpt.getDelta();
				double dRel = execute.getDelta();

				ArrayList<Option> options = new ArrayList<Option>();
				//ensure that there are still improving moves
				if(!(dEx == 0 && d2opt == 0 && dRel == 0)) {
					//add improving moves to array list
					if(dEx != 0) {
						options.add(optionExchange);
					}
					if(d2opt != 0) {
						options.add(optionTwoOpt);
					}
					if(dRel != 0) {
						options.add(execute);
					}
					//select a random improving move from the matrix
					execute = options.get(new Random().nextInt(options.size()));
				}
				break;
			}

			//update the vehicles
			v1 = execute.getV1();
			v2 = execute.getV2();	
		}

		//print the last BMM
		ro.printRelocateMatrix();

		printResultsToConsole();
		printResultsToFile();

		//if the solution was random, memorize the result
		if(mode == 4) {
			soln = new RandomSolution(super.getTotalCost(), super.getVehicleCount(),super.vrp.m, super.getVehicles());
		}
	}

	public void solve_CrossEx() {

		ceo.createOptionMatrix();
		Option crossEx = ceo.fetchBestOption();
		crossEx.printOption();


		while(crossEx.getDelta() < 0) {
			ceo.executeOption(crossEx);
			ceo.updateOptionMatrix(crossEx.getV1(), crossEx.getV2());
			ceo.printCrossEx();
			crossEx = ceo.fetchBestOption();
		}

		for(Vehicle v : vrp.vehicle) {
			
			if(v.firstCustomer.succ.equals(v.lastCustomer)) {
				v.cost = 0;
			}
			else {
				double dist = 0;
				Customer cCur = v.firstCustomer;
				Customer cSucc = cCur.succ;
				while(!cSucc.equals(v.lastCustomer)) {
					dist += vrp.distance(cCur, cSucc);
					cCur = cSucc;
					cSucc = cSucc.succ;
				}
				v.cost = dist;
				v.show();
				System.out.println(v.cost);
			}
		}


				printResultsToConsole();
				printResultsToFile();
	}



	/**
	 * Accessor for the solution
	 * @return RandomSolution, the solution obtained by using random operations
	 */
	public RandomSolution getRandomSolution() {
		return this.soln;
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
		stDesc.solve(4);
		System.out.println(stDesc.getRandomSolution().getCost());

		RandomSolution rs = stDesc.getRandomSolution();
		for(int i = 0 ; i < 20 ; i++) {
			vrp = new VRP(in, num);
			stDesc = new SteepestDescent(vrp,fileOut);
			stDesc.solve(4);
			rs.compare(stDesc.getRandomSolution());

		}
		System.out.println(rs.getCost());



		TwoOptOperation two = new TwoOptOperation(vrp, num);


		//		TestSolution.runTest(stDesc.vrp, stDesc.getTotalCost(), stDesc.getVehicles());
		DisplayVRP dVRP = new DisplayVRP(in, num, args[2]);
		dVRP.plotVRPSolution();
	}
}
