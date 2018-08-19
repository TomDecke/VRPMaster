package operators;
import moves.ExchangeOption;
import moves.Option;
import representation.Customer;
import representation.VRP;
import representation.Vehicle;

/**
 * Class to represent the exchange-operator
 * @author Tom Decke
 *
 */
public class ExchangeOperation implements Operation {

	private final double EPSILON = 1E-10;
	private Option[][] exchangeMatrix;
	private VRP vrp;
	int numCustomers;

	/**
	 * Constructor for the exchange operation
	 * @param vrp VRP, the VRP to which the operation is to be applied
	 * @param numCustomers int, the number of customers in the VRP
	 */
	public ExchangeOperation(VRP vrp, int numCustomers) {
		this.vrp = vrp;
		this.numCustomers = numCustomers;
		exchangeMatrix = new ExchangeOption[numCustomers][numCustomers];
	}

	/**
	 * Create the matrix containing the best exchanges between tours
	 */
	public void createOptionMatrix() {
		//fill half of the matrix since swapping a & b is equivalent to swapping b & a
		for(int i = 0 ; i < numCustomers ; i++) {
			for(int j = i+1; j < numCustomers; j++) {
				exchangeMatrix[i][j] = findBestOption(vrp.vehicle[i], vrp.vehicle[j]);
			}
		}
	}

	/**
	 * Find the customer exchange between two vehicles that yields the biggest cost benefit
	 * @param v1 Vehicle, the first vehicle that is part of the swap
	 * @param v2 Vehicle, the second vehicle that is part of the swap
	 * @return ExchangeOption, the best exchange option for v1 and v2
	 */
	public Option findBestOption(Vehicle v1, Vehicle v2) {

		//create a default exchange option
		ExchangeOption bestExchange = new ExchangeOption(v1, v2, null, null, 0,this);

		//set up the encapsulating customers
		Customer cV1Pred = v1.firstCustomer;
		Customer cV1Succ = cV1Pred.succ;
		Customer cV2Pred = v2.firstCustomer;
		Customer cV2Succ = cV2Pred.succ;

		//Iterate through the customers from the first vehicle
		Customer cV1 = v1.firstCustomer.succ;
		while(!cV1.equals(v1.lastCustomer)) {

			//get the encapsulating customers for c1
			cV1Pred = cV1.pred;
			cV1Succ = cV1.succ;

			//check the exchange with every customer from the second vehicle
			Customer cV2 = v2.firstCustomer.succ;
			while(!cV2.equals(v2.lastCustomer)) {

				//get the encapsulating customers for c2
				cV2Pred = cV2.pred;
				cV2Succ = cV2.succ;

				//make sure the exchange does not violate time window constraints
				if(cV1.canBeInsertedBetween(cV2Pred, cV2Succ) && cV2.canBeInsertedBetween(cV1Pred, cV1Succ)) {

					//ensure that the vehicles possess the capacity for the exchange
					if((v1.load-cV1.demand+cV2.demand)<=v1.capacity && (v2.load-cV2.demand+cV1.demand)<=v2.capacity) {
						//get the change in distance for v1
						double deltaDistV1 = 
								- vrp.distance(cV1Pred, cV1) - vrp.distance(cV1, cV1Succ)
								+ vrp.distance(cV1Pred, cV2) + vrp.distance(cV2, cV1Succ);

						//get the change in distance for v2
						double deltaDistV2 =  
								- vrp.distance(cV2Pred, cV2) - vrp.distance(cV2, cV2Succ)
								+ vrp.distance(cV2Pred, cV1) + vrp.distance(cV1, cV2Succ);

						//catch computational inaccuracy
						if(Math.abs(deltaDistV1+deltaDistV2) < EPSILON) {
							deltaDistV1 = 0;
							deltaDistV2 = 0;
						}

						double delta  =  ((v1.getDistance()+deltaDistV1) * v1.costOfUse 
								+(v2.getDistance()+deltaDistV2) * v2.costOfUse)
								-(v1.cost + v2.cost);

						if(delta < bestExchange.getDelta()) {
							bestExchange = new ExchangeOption(v1,v2, cV1, cV2, delta,this);
						}
					}
				}

				//move on to the next customer of vehicle two
				cV2 = cV2.succ;
			}

			//move on to the next vehicle of customer one
			cV1 = cV1.succ;
		}
		return bestExchange;
	}

	/**
	 * Retrieves the best exchange option from the exchange matrix
	 * @return ExchangeOption, the option with the greatest cost reduction
	 */
	public Option fetchBestOption() {
		Option bestExchange = exchangeMatrix[0][1];
		for(int i = 0 ; i < numCustomers ; i++) {
			for(int j = i+1; j < numCustomers; j++) {
				Option curExch = exchangeMatrix[i][j];
				if(curExch.getDelta() < bestExchange.getDelta()) {
					bestExchange = curExch;
				}
			}
		}
		return bestExchange;
	}

	/**
	 * Swaps two customers according to the information stored in the exchange option
	 * @param bE ExchangeOption, exchange option to be used
	 */
	public void executeOption(Option bE) {
		//obtain information of customer from vehicle 1
		Customer c1		= bE.getC1();
		Customer c1Pred = c1.pred;
		Customer c1Succ = c1.succ;
		Vehicle v1 = bE.getV1();

		//obtain information of customer from vehicle 2
		Customer c2		= bE.getC2();
		Customer c2Pred = c2.pred;
		Customer c2Succ = c2.succ;
		Vehicle v2 = bE.getV2();

		if(v1.remove(c1)) {
			if(v2.remove(c2)) {
				//swap the customers
				v1.insertBetween(c2, c1Pred, c1Succ);
				v2.insertBetween(c1, c2Pred, c2Succ);
			}
			//if the removal fails, reverse the prior one
			else {
				v1.insertBetween(c1, c1Pred, c1Succ);
			}
		}
	}

	/**
	 * Update the exchange matrix by finding new best exchanges for vehicles that were involved in the change
	 * @param v1 Vehicle, the first vehicles that was involved in the exchange
	 * @param v2 Vehicle, the second vehicles that was involved in the exchange
	 */
	public void updateOptionMatrix(Vehicle v1, Vehicle v2){
		int indV1 = v1.index;
		int indV2 = v2.index;
		for(int i = 0; i < numCustomers; i++) {
			Vehicle cV = vrp.vehicle[i];
			//only consider inter-route changes and one way swapping
			if(indV1 < i) {
				exchangeMatrix[indV1][i] = findBestOption(v1, cV);
			}
			else if(indV1 > i) {
				exchangeMatrix[i][indV1] = findBestOption(v1, cV);
			}
			if(indV2 < i) {
				exchangeMatrix[indV2][i] = findBestOption(v2, cV);		
			}
			else if(indV2 > i) {
				exchangeMatrix[i][indV2] = findBestOption(v2, cV);		
			}
		}
	}
}
