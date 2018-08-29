package representation;
import addOns.TimeConstraintViolationException;


/**
 * Class modeling a customer for a VRP-instance
 * @author Patrick Prosser
 *
 */
public class Customer {

	private int custNo, xCoord, yCoord, demand, readyTime, dueDate, serviceTime;
	private double earliestStart, latestStart, checkLatest,checkEarliest; // ... can be reset to readyTime & dueDate 
	private Customer pred,succ;
	private Vehicle vehicle;
	private VRP vrp;

	/**
	 * Default-constructor to create an empty customer
	 */
	public Customer(){
		pred = succ = null;
	}

	/**
	 * Constructor to create a customer
	 * @param custNo int, id of the customer
	 * @param xCoord int, x-coordinate
	 * @param yCoord int, y-coordinate
	 * @param demand int, demand of the customer
	 * @param readyTime int, time the customer is available
	 * @param dueDate int, time the vehicle needs to arrive at the customer
	 * @param serviceTime int, time it takes to serve the customer
	 */
	public Customer (int custNo,int xCoord,int yCoord, int demand,int readyTime, int dueDate,int serviceTime){
		this.custNo = custNo;
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.demand = demand;
		this.readyTime = readyTime;
		this.dueDate = dueDate;
		this.serviceTime = serviceTime;
		pred = null;
		succ = null;
		earliestStart = readyTime;
		latestStart = dueDate;
	}


	/**
	 * Check if this customer can be inserted between y and z
	 * @param y Customer, potential predecessor
	 * @param z Customer, potential successor
	 * @return boolean, true if this customer fits between y and z
	 */
	public boolean canBeInsertedBetween(Customer y,Customer z){
		double es = Math.max(this.readyTime,y.earliestStart + y.serviceTime + vrp.distance(y,this));
		double ls = Math.min(this.dueDate,z.latestStart - (this.serviceTime + vrp.distance(this,z)));
		return es <= ls;
	}

	/**
	 * Propagate earliest and latest start for the insertion of a customer
	 * @param y Customer, potential predecessor
	 * @param z Customer, potential successor
	 * @throws TimeConstraintViolationException 
	 */
	public void insertBetween(Customer y,Customer z) throws TimeConstraintViolationException{
		earliestStart = Math.max(this.readyTime,y.earliestStart + y.serviceTime + vrp.distance(y,this));
		latestStart = Math.min(this.dueDate,z.latestStart - (this.serviceTime + vrp.distance(this,z)));
		propagateLatestStart();
		propagateEarliestStart();
	}

	/**
	 * Propagate the latest start for a customer (propagate left)
	 * @throws TimeConstraintViolationException
	 */
	public void propagateLatestStart() throws TimeConstraintViolationException{
		Customer current = this;
		// propagate latestStart left
		while (current.pred != null){
			Customer cPred = current.pred;
			//latest start is the minimum between due date and the time necessary to leave for the successor to be served
			cPred.latestStart = Math.min(cPred.dueDate,current.latestStart - (cPred.serviceTime + vrp.distance(cPred,current)));
			if(cPred.earliestStart > cPred.latestStart) {
				throw new TimeConstraintViolationException("Updated latest start comes before current earliest start");
			}
			current = cPred;
		}
	}

	/**
	 * Propagate the earliest start for a customer (propagate right)
	 * @throws TimeConstraintViolationException
	 */
	public void propagateEarliestStart() throws TimeConstraintViolationException {
		Customer current = this;
		// propagate earliestStart right
		while (current.succ != null){
			Customer cSucc = current.succ;
			//earliest start is the max between arrival at and ready time of the customer
			cSucc.earliestStart = Math.max(cSucc.readyTime,current.earliestStart + current.serviceTime + vrp.distance(current,cSucc));
			if(cSucc.earliestStart > cSucc.latestStart) {
				throw new TimeConstraintViolationException("Updated earliest start comes before current latest start");
			}
			current = cSucc;
		}
	}

	/**
	 * Write the variables of the Customer as String
	 */
	public String toString(){
		return custNo +" "+ xCoord +" "+ yCoord +" "+ demand +" "+ readyTime +" "+ dueDate +" "+ serviceTime;
	}

	/**
	 * Creates a copy of the customer (without reference to other customers)
	 * @return Customer, a new customer object with the same information
	 */
	public Customer copy() {
		Customer nC = new Customer(this.custNo,this.xCoord,this.yCoord,this.demand,this.readyTime,this.dueDate,this.serviceTime);
		nC.vehicle = this.vehicle;
		nC.vrp = this.vrp;
		nC.earliestStart = this.earliestStart;
		nC.latestStart = this.latestStart;

		return nC;
	}

	/**
	 * Accessor for the customer number
	 * @return int, the customer number
	 */
	public int getCustNo() {
		return custNo;
	}

	/**
	 * Mutator for the customer number
	 * @param custNo int, the new customer number
	 */
	public void setCustNo(int custNo) {
		this.custNo = custNo;
	}

	/**
	 * Accessor for the x-coordinate
	 * @return int, x-coordinate of the customer
	 */
	public int getxCoord() {
		return xCoord;
	}

	/**
	 * Mutator for the x-coordinate
	 * @param xCoord int, new x-coordinate for the customer
	 */
	public void setxCoord(int xCoord) {
		this.xCoord = xCoord;
	}

	/**
	 * Accessor for the y-coordinate
	 * @return int, y-coordinate of the customer
	 */
	public int getyCoord() {
		return yCoord;
	}

	/**
	 * Mutator for the y-coordinate
	 * @param yCoord int, new y-coordinate for the customer
	 */
	public void setyCoord(int yCoord) {
		this.yCoord = yCoord;
	}

	/**
	 * Accessor for the demand of a customer
	 * @return int, the demand
	 */
	public int getDemand() {
		return demand;
	}

	/**
	 * Mutator for the demand of a customer
	 * @param demand int, the new demand
	 */
	public void setDemand(int demand) {
		this.demand = demand;
	}

	/**
	 * Accessor for the ready time of a customer
	 * @param int, the ready of the customer
	 */
	public int getReadyTime() {
		return readyTime;
	}

	/**
	 * Mutator for the ready time of a customer
	 * @param readyTime int, the new ready for the customer
	 */
	public void setReadyTime(int readyTime) {
		this.readyTime = readyTime;
	}

	/**
	 * Accessor for the due date of a customer
	 * @return int, the due date of the customer
	 */
	public int getDueDate() {
		return dueDate;
	}

	/**
	 * Mutator for the due date of a customer
	 * @param dueDate int, the new due date for the customer
	 */
	public void setDueDate(int dueDate) {
		this.dueDate = dueDate;
	}

	/**
	 * Accessor for the service time of a customer
	 * @return int, the service time of the customer
	 */
	public int getServiceTime() {
		return serviceTime;
	}

	/**
	 * Mutator for the service time of a customer
	 * @param serviceTime int, the new service time for the customer
	 */
	public void setServiceTime(int serviceTime) {
		this.serviceTime = serviceTime;
	}

	/**
	 * Accessor for the earliest start time of a customer
	 * @return double, the earliest start time of a customer
	 */
	public double getEarliestStart() {
		return earliestStart;
	}

	/**
	 * Mutator for the earliest start time of a customer
	 * @param earliestStart double, the new earliest start time for a customer
	 */
	public void setEarliestStart(double earliestStart) {
		this.earliestStart = earliestStart;
	}
	/**
	 * Accessor for the latest start time of a customer
	 * @return double, the latest start time of a customer
	 */
	public double getLatestStart() {
		return latestStart;
	}
	/**
	 * Mutator for the latest start time of a customer
	 * @param latestStart double, the new latest start time for a customer
	 */
	public void setLatestStart(double latestStart) {
		this.latestStart = latestStart;
	}

	/**
	 * Accessor for the copy of the latest start time
	 * @return double, copy of latest start
	 */
	public double getCheckLatest() {
		return checkLatest;
	}

	/**
	 * Mutator for the copy of the latest start time
	 * @param checkLatest double, new copy of latest start
	 */
	public void setCheckLatest(double checkLatest) {
		this.checkLatest = checkLatest;
	}

	/**
	 * Accessor for the copy of the earliest start time
	 * @return double, copy of earliest start
	 */
	public double getCheckEarliest() {
		return checkEarliest;
	}
	/**
	 * Mutator for the copy of the earliest start time
	 * @param checkEarliest double, new copy of earliest start
	 */
	public void setCheckEarliest(double checkEarliest) {
		this.checkEarliest = checkEarliest;
	}

	/**
	 * Accessor for the predecessor of a customer
	 * @return Customer, the predecessor
	 */
	public Customer getPred() {
		return pred;
	}

	/**
	 * Mutator for the predecessor of a customer
	 * @param pred Customer, the new predecessor
	 */
	public void setPred(Customer pred) {
		this.pred = pred;
	}

	/**
	 * Accessor for successor of a customer
	 * @return Customer, the successor
	 */
	public Customer getSucc() {
		return succ;
	}

	/**
	 * Mutator for the successor of a customer
	 * @param succ Customer, the new predecessor
	 */
	public void setSucc(Customer succ) {
		this.succ = succ;
	}

	/**
	 * Accessor for the vehicle of a customer
	 * @return Vehicle, the vehicle of the customer
	 */
	public Vehicle getVehicle() {
		return vehicle;
	}

	/**
	 * Mutator for the vehicle of a customer
	 * @param vehicle Vehicle, the new vehicle
	 */
	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	/**
	 * Accessor for the VRP of a customer
	 * @return VRP, the VRP to which the customer belongs
	 */
	public VRP getVrp() {
		return vrp;
	}

	/**
	 * Mutator for the VRP of a customer
	 * @param vrp VRP, the new VRP
	 */
	public void setVrp(VRP vrp) {
		this.vrp = vrp;
	}

}
