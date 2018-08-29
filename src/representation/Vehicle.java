package representation;
import addOns.TimeConstraintViolationException;

/**
 * Class modeling a vehicle for a VRP-instance
 * @author Patrick Prosser
 *
 */
public class Vehicle {

	private int id, index, capacity, load, costOfUse;
	private double cost; // this is the sum of the distance travelled times costOfUse
	private double distance; //distance travelled by the vehicle
	//customers for beginning and end of a tour
	private Customer firstCustomer, lastCustomer;
	private VRP vrp;

	/**
	 * Constructor to create a vehicle for the VRP
	 * @param vrp VRP, instance of the vehicle routing problem it belongs to
	 * @param id int, identifier for the vehicle
	 * @param capacity int, maximum capacity of the vehicle
	 * @param costOfUse int, cost that comes from using the vehicle
	 * @param depot Customer, customer that functions as the depot
	 */
	public Vehicle (VRP vrp,int id, int capacity,int costOfUse, Customer depot){
		this.vrp = vrp;
		this.id = id;
		this.index=id-1;
		this.capacity = capacity;
		this.costOfUse = costOfUse;
		this.distance = 0;

		//set up dummy customers who come at the beginning and at the end of a tour 
		firstCustomer = new Customer(depot.getCustNo(),depot.getxCoord(),depot.getyCoord(),0,0,0,0);
		firstCustomer.setVehicle(this);
		lastCustomer = new Customer(depot.getCustNo(),depot.getxCoord(),depot.getyCoord(),0,0,depot.getDueDate(),0);
		lastCustomer.setVehicle(this);
		firstCustomer.setSucc(lastCustomer);
		lastCustomer.setPred(firstCustomer);
	}


	/**
	 * Insert customer c into vehicle's tour
	 * in least cost position. Deliver true
	 * if the insertion was possible, i.e.
	 * capacity & time windows respected
	 * @param cInsert Customer, the customer which is to be inserted 
	 * @return boolean, whether or not the insertion was successful
	 */
	public boolean insertBetween(Customer cInsert, Customer cPred, Customer cSucc){		


		//tell the customer he now belongs to this vehicle
		cInsert.setVehicle(this);

		//insert the customer into the vehicle
		cInsert.setPred(cPred);
		cInsert.setSucc(cSucc);
		cSucc.setPred(cInsert);
		cPred.setSucc(cInsert);

		//propagate the earliest and latest start
		try {
			cInsert.insertBetween(cPred, cSucc);
		} catch (TimeConstraintViolationException e) {
			System.out.println(e.getMessage());
		}

		//increase the load of the vehicle by the customers demand
		this.load += cInsert.getDemand();

		//update the distance by removing the prior edge and adding the new ones
		distance += (vrp.distance(cPred,cInsert) + vrp.distance(cInsert, cSucc) - vrp.distance(cPred,cSucc));

		//update the cost of this vehicle
		this.cost = this.distance * this.costOfUse;

		return true;
	}

	/**
	 * Check if the vehicle can accommodate a customer
	 * @param c Customer, the customer to be checked
	 * @return boolean, true if the vehicle can accommodate the customer, false otherwise
	 */
	public boolean canAccomodate(Customer c) {
		if(this.capacity < this.load + c.getDemand()) {
			return false;
		}
		else {
			return true;
		}
	}


	/**
	 * Remove a customer from the vehicle's tour
	 * @param c Customer, the customer which should be removed
	 * @return boolean, true if successful, false otherwise
	 */
	public boolean remove(Customer c){
		Customer currentCustomer = firstCustomer;

		//search for customer c
		while(currentCustomer.getSucc() != null) {
			if(c.equals(currentCustomer)) {
				Customer cPred =  currentCustomer.getPred();
				Customer cSucc = currentCustomer.getSucc();

				//if found change the successor of the predecessor and the predecessor of the successor
				cPred.setSucc(cSucc);
				cSucc.setPred(cPred);

				//re-propagate earliest and latest start
				try {
					//propagate earliest start 
					cSucc.propagateEarliestStart();

					//propagate latest start 
					cPred.propagateLatestStart();

				} catch (TimeConstraintViolationException e) {
					System.out.println(e.getMessage());
				}


				//remove the load
				this.load -= c.getDemand();

				//remove pointers of the customer
				c.setVehicle(null);
				c.setPred(null);
				c.setSucc(null);

				//update distance, by removing edges to former customer and adding new edge between now-neighbours
				distance += vrp.distance(cPred, cSucc) - vrp.distance(cPred, currentCustomer) - vrp.distance(currentCustomer, cSucc);

				//recalculate the cost
				this.cost = this.distance * this.costOfUse;

				return true;
			}
			currentCustomer = currentCustomer.getSucc();
		}
		return false;
	}

	/**
	 * Get the load of the vehicle as String
	 * @return String, the value of the vehicle's load
	 */
	public String toString(){
		return String.valueOf(load);
	}

	/**
	 * Shows the route of the vehicle
	 */
	public void show(){
		System.out.print("vehicle "+ id +": ");
		Customer customer = firstCustomer;
		System.out.print(customer.getCustNo());
		customer = customer.getSucc();
		while (customer != null){
			System.out.print(" -> " + customer.getCustNo() );
			customer = customer.getSucc();
		}
		System.out.println();
	}


	/**
	 * Accessor for the distance travelled by this vehicle 
	 * @return double, the distance
	 */
	public double getDistance() {
		return this.distance;
	}

	/**
	 * Mutator for the distance
	 * @param distance double, the new distance
	 */
	public void setDistance(double distance) {
		this.distance=distance;
	}

	/**
	 * Accessor for the id of the vehicle
	 * @return int, the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Mutator for the id of the vehicle
	 * @param id int, the new id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Accesor for the index of the vehicle in the VRP vehicle array
	 * @return int, the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Mutator for the index of the vehicle in the VRP vehicle array
	 * @param index int, the new index
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * Accessor for the capacity of a vehicle
	 * @return int, the capacity
	 */
	public int getCapacity() {
		return capacity;
	}

	/**
	 * Mutator for the capacity of a vehicle
	 * @param capacity int, the new capacity
	 */
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	/**
	 * Accessor for the load carried by a vehicle
	 * @return int, the load
	 */
	public int getLoad() {
		return load;
	}

	/**
	 * Mutator for the load carried by a vehicle
	 * @param load int, the new load
	 */
	public void setLoad(int load) {
		this.load = load;
	}

	/**
	 * Accessor for the cost of use incurred by a vehicle
	 * @return int, the cost of use
	 */
	public int getCostOfUse() {
		return costOfUse;
	}

	/**
	 * Mutator for the cost of use for a vehicle
	 * @param costOfUse int, the new cost of use
	 */
	public void setCostOfUse(int costOfUse) {
		this.costOfUse = costOfUse;
	}

	/**
	 * Accessor for the total cost incurred by a vehicle (distance x cost of use)
	 * @return double, the cost
	 */
	public double getCost() {
		return cost;
	}

	/**
	 * Mutator for the cost of a vehicle
	 * @param cost double, the new cost
	 */
	public void setCost(double cost) {
		this.cost = cost;
	}

	/**
	 * Accessor for the first customer of a vehicle (depot)
	 * @return Customer, the first customer
	 */
	public Customer getFirstCustomer() {
		return firstCustomer;
	}

	/**
	 * Mutator for the first customer of a vehicle
	 * @param firstCustomer Customer, the new first customer
	 */
	public void setFirstCustomer(Customer firstCustomer) {
		this.firstCustomer = firstCustomer;
	}
	/**
	 * Accessor for the last customer of a vehicle (depot)
	 * @return Customer, the last customer
	 */
	public Customer getLastCustomer() {
		return lastCustomer;
	}

	/**
	 * Mutator for the last customer of a vehicle
	 * @param lastCustomer Customer, the new last customer
	 */
	public void setLastCustomer(Customer lastCustomer) {
		this.lastCustomer = lastCustomer;
	}

	/**
	 * Accessor for the VRP of a vehicle
	 * @return VRP, the VRP to which the vehicle belongs
	 */
	public VRP getVrp() {
		return vrp;
	}

	/**
	 * Mutator for the VRP of a vehicle
	 * @param vrp VRP, the new VRP
	 */
	public void setVrp(VRP vrp) {
		this.vrp = vrp;
	}

}
