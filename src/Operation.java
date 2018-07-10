
public interface Operation {
	
	public void createOperationMatrix();
	public void updateOperationMatrix(Vehicle v1, Vehicle v2);
	public Option findBestOption(Vehicle v1, Vehicle v2);
	public Option fetchBestOption();
	public void executeOption(Option o);

}
