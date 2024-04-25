package at.jku.isse.designspace.rule.arl.repair.order;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import at.jku.isse.designspace.core.events.PropertyUpdate;

public class RestrictionAnalysisDS {

	PropertyUpdate currentChange;
	OffsetDateTime datetime;
	Map<String,RestrictionComponentDetails> repTree;
	
	public RestrictionAnalysisDS(PropertyUpdate currentEvent, OffsetDateTime datetime) {
		super();
		this.currentChange = currentEvent;
		this.datetime = datetime;
		repTree=new HashMap<>();
	}
	
	public void addInRepTree(String rn,String rstText,Map<String, Integer> restComponents)
	{
		RestrictionComponentDetails rst=new RestrictionComponentDetails(rstText, restComponents);
		this.repTree.putIfAbsent(rn, rst);
	}
	public PropertyUpdate getCurrentChange() {
		return currentChange;
	}
	public void setCurrentChange(PropertyUpdate currentEvent) {
		this.currentChange = currentEvent;
	}
	public String getEventDate()
	{
		if(this.getDatetime()!=null)
		{
			return this.getDatetime().toLocalDate().toString();
		}
		else
			return "";
	}
	public String getEventTime()
	{
		if(this.getDatetime()!=null)
		{
			return this.getDatetime().toLocalTime().toString();
		}
		else
			return "";
	}
	public OffsetDateTime getDatetime() {
		return datetime;
	}
	public void setDatetime(OffsetDateTime datetime) {
		this.datetime = datetime;
	}
	
	
	public Map<String, RestrictionComponentDetails> getRepTree() {
		return repTree;
	}

	public void setRepTree(Map<String, RestrictionComponentDetails> repTree) {
		this.repTree = repTree;
	}

}
