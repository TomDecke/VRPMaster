package operators;
import moves.Option;
import representation.Vehicle;

/**
 * Interface to determine the structure for an operation
 * @author Tom Decke
 *
 */
public interface Operation {

	/**
	 * Create a matrix where each field represents the best option of interaction between vehicles
	 */
	public void createOptionMatrix();

	/**
	 * Update the option matrix for vehicles involved in the last execution
	 * @param v1 Vehicle, the first vehicle
	 * @param v2 Vehicle, the second vehicle
	 */
	public void updateOptionMatrix(Vehicle v1, Vehicle v2);

	/**
	 * Find the best option to execute including two vehicles
	 * @param v1 Vehicle, the first vehicle of interest
	 * @param v2 Vehicle, the second vehicle of interest
	 * @return Option, the best option for two vehicles
	 */
	public Option findBestOption(Vehicle v1, Vehicle v2);

	/**
	 * Get the best option of the option matrix
	 * @return Option, the best option of the matrix
	 */
	public Option fetchBestOption();

	/**
	 * Executes the given option to improve the solution
	 * @param o Option, the option to execute
	 */
	public void executeOption(Option o);

}
