package representation;
import java.io.IOException;
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
	boolean insertBetween(Customer cInsert, Customer cPred, Customer cSucc){		


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
	boolean remove(Customer c){
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public int getLoad() {
		return load;
	}

	public void setLoad(int load) {
		this.load = load;
	}

	public int getCostOfUse() {
		return costOfUse;
	}

	public void setCostOfUse(int costOfUse) {
		this.costOfUse = costOfUse;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public Customer getFirstCustomer() {
		return firstCustomer;
	}

	public void setFirstCustomer(Customer firstCustomer) {
		this.firstCustomer = firstCustomer;
	}

	public Customer getLastCustomer() {
		return lastCustomer;
	}

	public void setLastCustomer(Customer lastCustomer) {
		this.lastCustomer = lastCustomer;
	}

	public VRP getVrp() {
		return vrp;
	}

	public void setVrp(VRP vrp) {
		this.vrp = vrp;
	}

}
