package at.jku.isse.designspace.rule.arl.repair.order;

import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.rule.arl.repair.RepairAction;

public class Repair_template {
	
	String originalARL;
	String operator;
	String concreteValue;
	int select_count;
	int unSelect_count;
	
	public Repair_template()
	{
		this.originalARL=null;
		this.operator=null;
		this.concreteValue=null;
		this.select_count=0;
		this.unSelect_count=0;
	}
	
	public String asString() {
		return originalARL+" "+operator+" "+concreteValue;
	}
	
	public boolean isNull()
	{
		if(this.originalARL==null && this.concreteValue==null && this.operator==null)
			return true;
		return false;
	}
	

	public boolean isRepairTemplateSame(Repair_template rt1)
	{
		if(this.isNull() || rt1.isNull())
		{
			return false;
		}
		else if (rt1.getConcreteValue() == null && this.getConcreteValue() == null) {
			if (rt1.getOriginalARL().equals(this.getOriginalARL()) && rt1.getOperator().equals(this.getOperator())) {
				return true;
			}
		} else if (rt1.getConcreteValue() != null && this.getConcreteValue() != null) {
			if (rt1.getOriginalARL().equals(this.getOriginalARL()) && rt1.getOperator().equals(this.getOperator())
					&& rt1.getConcreteValue().equals(this.getConcreteValue())) {
				return true;
			}
		}
		return false;
	}
	
	public static Repair_template toRepairTemplate(RepairAction ra)
	{
		Repair_template temp=new Repair_template();
		if(ra!=null)
		{
		if (ra.getValue() != null)
		{
			if((ra.getOperator().toString().equals("Add") || ra.getOperator().toString().equals("Remove")) && !ra.getValue().toString().equals("UNKNOWN"))
			{
				Instance it=(Instance) ra.getValue();
				temp.setConcreteValue(it.getInstanceType().name());
			}
			else
				temp.setConcreteValue(ra.getValue().toString());
		}
		else
		{
			temp.setConcreteValue(null);
		}
		temp.setOperator(ra.getOperator().toString());
		temp.setOriginalARL(ra.getEvalNode().expression.getOriginalARL(0, false));
		}
		return temp;
	}
	
	public int getSelect_count() {
		return select_count;
	}
	public void setSelect_count(int select_count) {
		this.select_count = select_count;
	}
	public int getUnSelect_count() {
		return unSelect_count;
	}
	public void setUnSelect_count(int unSelect_count) {
		this.unSelect_count = unSelect_count;
	}
	public String getOriginalARL() {
		return originalARL;
	}
	public void setOriginalARL(String originalARL) {
		this.originalARL = originalARL;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public String getConcreteValue() {
		return concreteValue;
	}
	public void setConcreteValue(String concreteValue) {
		this.concreteValue = concreteValue;
	}

/*	public ConsistencyRule getCre() {
		return cre;
	}

	public void setCre(ConsistencyRule cre) {
		this.cre = cre;
	}
*/
	
	

	
}
