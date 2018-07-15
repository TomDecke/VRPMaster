

public interface Operation {
	
	public void createOptionMatrix();
	public void updateOptionMatrix(Vehicle v1, Vehicle v2);
	public Option findBestOption(Vehicle v1, Vehicle v2);
	public Option fetchBestOption();
	public void executeOption(Option o);

}
