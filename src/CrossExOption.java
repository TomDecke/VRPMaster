
public class CrossExOption {
	private Vehicle v1;
	private Vehicle v2;
	private Customer cV1;
	private Customer cV2;
	private int loadForV1;
	private int loadForV2;
	private double delta;
	
	public CrossExOption(Vehicle v1, Vehicle v2, Customer cV1, Customer cV2, int loadForV1, int loadForV2, double delta) {
		this.v1 = v1;
		this.v2 = v2;
		this.cV1 = cV1;
		this.cV2 = cV2;
		this.loadForV1 = loadForV1;
		this.loadForV2 = loadForV2;
		this.delta = delta;
		
	}

	public Vehicle getV1() {
		return v1;
	}

	public void setV1(Vehicle v1) {
		this.v1 = v1;
	}

	public Vehicle getV2() {
		return v2;
	}

	public void setV2(Vehicle v2) {
		this.v2 = v2;
	}

	public Customer getcV1() {
		return cV1;
	}

	public void setcV1(Customer cV1) {
		this.cV1 = cV1;
	}

	public Customer getcV2() {
		return cV2;
	}

	public void setcV2(Customer cV2) {
		this.cV2 = cV2;
	}
	
	public int getLoadForV1() {
		return loadForV1;
	}

	public void setLoadForV1(int loadForV1) {
		this.loadForV1 = loadForV1;
	}

	public int getLoadForV2() {
		return loadForV2;
	}

	public void setLoadForV2(int loadForV2) {
		this.loadForV2 = loadForV2;
	}

	public double getDelta() {
		return delta;
	}

	public void setDelta(double delta) {
		this.delta = delta;
	}
	
}
