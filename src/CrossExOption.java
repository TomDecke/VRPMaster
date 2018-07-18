
public class CrossExOption extends Option{
	
	public CrossExOption(Vehicle v1, Vehicle v2, Customer cV1, Customer cV2, int loadForV1, int loadForV2, double delta, Operation op) {
		super(cV1,cV2,delta,v1,v2,op);
		super.loadForV1 = loadForV1;
		super.loadForV2 = loadForV2;
		
	}
	

	@Override
	public Vehicle getV1() {
		return super.v1;
	}



	@Override
	public Vehicle getV2() {
		return super.v2;
	}



	@Override
	public Customer getC1() {
		return super.c1;
	}



	@Override
	public Customer getC2() {
		return super.c2;
	}



	@Override
	public double getDelta() {
		return super.delta;
	}
	

	@Override
	public void printOption() {
		if(c1 == null) {
			System.out.println(String.format("There are no customers to swap"));
		}
		else {
			System.out.println(String.format("Exchange after customer c%d from vehicle v%d with after customer c%d from vehicle v%d. Improvement: %.2f",c1.custNo,v1.id,c2.custNo,v2.id,delta));

		}
	}
	
}
