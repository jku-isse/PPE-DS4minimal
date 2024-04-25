package at.jku.isse.designspace.rule.arl.repair.order;

public class PropertyChange_DS {
	
	String propertyName;
	int count;
	public PropertyChange_DS(String propertyName, int count) {
		super();
		this.propertyName = propertyName;
		this.count = count;
	}
	public String getPropertyName() {
		return propertyName;
	}
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
}
