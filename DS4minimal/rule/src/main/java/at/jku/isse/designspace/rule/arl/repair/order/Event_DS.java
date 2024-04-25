package at.jku.isse.designspace.rule.arl.repair.order;

import java.time.OffsetDateTime;

import at.jku.isse.designspace.core.events.PropertyUpdate;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.rule.arl.repair.RepairAction;
import at.jku.isse.designspace.rule.arl.repair.SideEffect;
import at.jku.isse.designspace.rule.model.ConsistencyRule;

public class Event_DS {
	PropertyUpdate currentEvent;
	RepairAction matchedRA;
	SideEffect<ConsistencyRule> sideEffect;
	ConsistencyRule cre;
	boolean cre_status;
	Instance stepInst;
	Repair_template rt;
	OffsetDateTime datetime;
	int HighestRank;
	int Rank;
	double score;
	public Event_DS(PropertyUpdate currentEvent, RepairAction matchedRA, SideEffect<ConsistencyRule> sideEffect,
			ConsistencyRule cre, boolean cre_status, Instance stepInst, Repair_template rt, OffsetDateTime datetime,
		 int highestRank, int rank, double score) {
		this.currentEvent = currentEvent;
		this.matchedRA = matchedRA;
		this.sideEffect = sideEffect;
		this.cre = cre;
		this.cre_status = cre_status;
		this.stepInst = stepInst;
		this.rt = rt;
		this.datetime = datetime;
		HighestRank = highestRank;
		Rank = rank;
		this.score = score;
	}

	public String getEventCurrentOperationValue()
	{
		PropertyUpdate pu=this.getCurrentEvent();
		if(pu.value()!=null)
			return pu.value().toString();
		else
			return "";
	}
	public String getEventRT()
	{
		if(this.getRt()!=null)
		{
			return this.getRt().asString();
		}
		else
			return "null null null";
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
	
	public PropertyUpdate getCurrentEvent() {
		return currentEvent;
	}
	public void setCurrentEvent(PropertyUpdate currentEvent) {
		this.currentEvent = currentEvent;
	}
	public RepairAction getMatchedRA() {
		return matchedRA;
	}
	public void setMatchedRA(RepairAction matchedRA) {
		this.matchedRA = matchedRA;
	}
	public SideEffect<ConsistencyRule> getSideEffect() {
		return sideEffect;
	}
	public void setSideEffect(SideEffect<ConsistencyRule> sideEffect) {
		this.sideEffect = sideEffect;
	}
	public ConsistencyRule getCre() {
		return cre;
	}
	public void setCre(ConsistencyRule cre) {
		this.cre = cre;
	}
	public boolean isCre_status() {
		return cre_status;
	}
	public void setCre_status(boolean cre_status) {
		this.cre_status = cre_status;
	}
	public Instance getStepInst() {
		return stepInst;
	}
	public void setStepInst(Instance stepInst) {
		this.stepInst = stepInst;
	}
	public Repair_template getRt() {
		return rt;
	}
	public void setRt(Repair_template rt) {
		this.rt = rt;
	}
	public OffsetDateTime getDatetime() {
		return datetime;
	}
	public void setDatetime(OffsetDateTime datetime) {
		this.datetime = datetime;
	}
	public int getHighestRank() {
		return HighestRank;
	}
	public void setHighestRank(int highestRank) {
		HighestRank = highestRank;
	}
	public int getRank() {
		return Rank;
	}
	public void setRank(int rank) {
		Rank = rank;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
}
