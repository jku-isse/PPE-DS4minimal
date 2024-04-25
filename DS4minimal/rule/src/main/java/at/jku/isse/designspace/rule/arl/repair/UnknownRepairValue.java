package at.jku.isse.designspace.rule.arl.repair;

public class UnknownRepairValue {
	
	private UnknownRepairValue() {}
	
	public static final UnknownRepairValue UNKNOWN = new UnknownRepairValue();

	@Override
	public String toString() {
		return "UNKNOWN";
	}
	
	
	
}
