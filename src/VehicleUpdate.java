
public class VehicleUpdate {
	private double newDistV1;
	private double newDistV2;
	private int newLoadV1;
	private int newLoadV2;
	
	public VehicleUpdate(double nD1, double nD2, int nL1, int nL2) {
		this.newDistV1 = nD1;
		this.newDistV2 = nD2;
		this.newLoadV1 = nL1;
		this.newLoadV2 = nL2;
	}

	public double getNewDistV1() {
		return newDistV1;
	}

	public void setNewDistV1(double newDistV1) {
		this.newDistV1 = newDistV1;
	}

	public double getNewDistV2() {
		return newDistV2;
	}

	public void setNewDistV2(double newDistV2) {
		this.newDistV2 = newDistV2;
	}

	public int getNewLoadV1() {
		return newLoadV1;
	}

	public void setNewLoadV1(int newLoadV1) {
		this.newLoadV1 = newLoadV1;
	}

	public int getNewLoadV2() {
		return newLoadV2;
	}

	public void setNewLoadV2(int newLoadV2) {
		this.newLoadV2 = newLoadV2;
	}

	
}
