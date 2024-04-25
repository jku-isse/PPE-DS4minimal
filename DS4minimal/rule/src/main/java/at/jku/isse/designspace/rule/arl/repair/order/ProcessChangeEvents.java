package at.jku.isse.designspace.rule.arl.repair.order;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import at.jku.isse.designspace.core.events.PropertyUpdate;
import at.jku.isse.designspace.core.events.PropertyUpdateAdd;
import at.jku.isse.designspace.core.events.PropertyUpdateRemove;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.rule.arl.repair.AbstractRepairAction;
import at.jku.isse.designspace.rule.arl.repair.RepairAction;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.arl.repair.RestrictionNode;
import at.jku.isse.designspace.rule.arl.repair.SideEffect;
import at.jku.isse.designspace.rule.arl.repair.UnknownRepairValue;
import at.jku.isse.designspace.rule.model.ConsistencyRule;

public class ProcessChangeEvents {

	Map<ConsistencyRule,List<RestrictionAnalysisDS>> restrictionAnalysis=new HashMap<>();
	Map<ConsistencyRule, List<Event_DS>> allexecutedEventLog = new HashMap<>(); // store all events throughout the lifetime.
	Map<ConsistencyRule, List<Event_DS>> allreleventEventLog = new HashMap<>(); // store all events throughout the lifetime leading to CRE fulfillment.
	Map<ConsistencyRule, List<Event_DS>> cre_CurrentEventLog = new HashMap<>(); // store the events until status turn true
	Map<ConsistencyRule, List<Event_DS>> unSelectRepairLog = new HashMap<>();

	// Side task
	Map<Event_DS, Event_DS> inverseEventLog = new HashMap<>();
	List<Event_DS> propUpdates = Collections.synchronizedList(new LinkedList<Event_DS>());
	// Side task
	RepairStats rs;

	/*public ProcessChangeEvents(RepairStats rs) {
		this.rs=rs;
	}*/

	public ProcessChangeEvents(RepairStats rs) {
		this.rs = rs;
	}
	//Restriction Analysis Function
	public void addRestrictionData(RestrictionAnalysisDS restDS,ConsistencyRule cre,RepairNode rn)
	{
		// Step 1: Complete the data in restDS
		populateRestDS(restDS, rn, 1);
		// Step 2: add into restrictionAnalysis
		if(!restDS.getRepTree().isEmpty())
		{
			if(restrictionAnalysis.containsKey(cre))
			{
				if(!restrictionAnalysis.get(cre).contains(restDS))
					restrictionAnalysis.get(cre).add(restDS);
			}
			else
			{
				List<RestrictionAnalysisDS> restList=Collections.synchronizedList(new LinkedList<RestrictionAnalysisDS>());
				restList.add(restDS);
				restrictionAnalysis.put(cre, restList);
			}
		}
	}
	public void populateRestDS(RestrictionAnalysisDS restDS,RepairNode rn,int position)
	{
		if(rn instanceof AbstractRepairAction)
		{
			AbstractRepairAction ra = (AbstractRepairAction)rn;
			RestrictionNode rootNode =  ra.getValue()==UnknownRepairValue.UNKNOWN && ra.getRepairValueOption().getRestriction() != null ? ra.getRepairValueOption().getRestriction().getRootNode() : null;
			if (rootNode != null) {
				//rootNode.resetRestComplexity();
				String restText=rootNode.printNodeTree(false, 40);
				restText=restText.replaceAll("(?m)^[ \t]*\r?\n", "");
				Map<String, Integer> restComp=rootNode.getRestComplexity();
				if(!restComp.isEmpty())
					restDS.addInRepTree(rn.toString(), restText, restComp);
				//restDS.setRepairNode(rn.toString());
				//restDS.setRestDetails(restText, rootNode.getRestComplexity());
			}
		}
		for (RepairNode child : rn.getChildren()) {
			populateRestDS(restDS,child, position + 1);
		}
	}
	//End Here
	// Property Change Map Functions
	public int PropertyChangeScore(String pUpdate, InstanceType iType, PropertyChange_DS pcD)
	{
		return rs.getPropertyChangeScore(pUpdate, iType, pcD);
	}
	public void addPropertyChange(String pUpdate, InstanceType iType, PropertyChange_DS pcD)
	{
		rs.addPropertyChange(pUpdate, iType, pcD);
	}
	// End here
	public void addCRE_CurrentEventList(Event_DS event)
	{
		ConsistencyRule cre=event.getCre();
		if(cre_CurrentEventLog.containsKey(cre))
		{
			cre_CurrentEventLog.get(cre).add(event);
		}
		else
		{
			List<Event_DS> temp=Collections.synchronizedList(new LinkedList<Event_DS>());
			temp.add(event);
			cre_CurrentEventLog.put(cre, temp);
		}
	}

	// Executes each time CRE gets fulfilled
	public void updateRepairTemplateScores(ConsistencyRule cre) {
		// Adding of select Repair Templates
		List<Event_DS> creSelectEventLog=this.cre_CurrentEventLog.get(cre);
		String rule=cre.getProperty("name").getValue().toString();
		//Assurance Check
		if(this.cre_CurrentEventLog.get(cre)==null)
		{
			System.out.println("Check it Out");
		}
		else
		{
			for(Event_DS event:this.cre_CurrentEventLog.get(cre))
			{
				//only add events with positive effect
				if(event.getRt()!=null)
				{
					if(event.getSideEffect().getSideEffectType()==SideEffect.Type.POSITIVE && !event.getRt().isNull())
					{
						rs.addRTSelectScore(event,rule);
					}
				}
				else
				{
					System.out.println("Check it Out");
				}

			}
			addRelevantEventLog(cre);
			this.cre_CurrentEventLog.remove(cre);
		}
		// Adding of Unselected Repair template Scores
		//Assurance Check
		if(this.unSelectRepairLog.get(cre)==null)
		{
			System.out.println("Check it Out");
		}
		else
		{
			for(Event_DS event:this.unSelectRepairLog.get(cre))
			{
				//only add events with positive effect
				if(event.getRt()!=null)
				{
					rs.addRTUnSelectScore(event, rule);
				}
				else
				{
					System.out.println("Check it Out");
				}

			}
			this.unSelectRepairLog.remove(cre);
		}
	}

	public void addUnSelectRepairLog(Event_DS event) {
		ConsistencyRule cre=event.getCre();
		if(unSelectRepairLog.containsKey(cre))
		{
			if(!unSelectRepairLog.get(cre).contains(event))
				unSelectRepairLog.get(cre).add(event);
		}
		else
		{
			List<Event_DS> temp=Collections.synchronizedList(new LinkedList<Event_DS>());
			temp.add(event);
			unSelectRepairLog.put(cre, temp);
		}
	}

	// Keep track of all the events being executed that lead to CRE fulfillment
	public void addRelevantEventLog(ConsistencyRule cre) {
		if(allreleventEventLog.containsKey(cre))
		{
			allreleventEventLog.get(cre).addAll(cre_CurrentEventLog.get(cre));
		}
		else
		{
			List<Event_DS> temp=Collections.synchronizedList(new LinkedList<Event_DS>());
			temp.addAll(cre_CurrentEventLog.get(cre));
			allreleventEventLog.put(cre, temp);
		}
	}


	// Keep track of all the events being executed throughout lifetime
	public void addAllExecuteEventLog(Event_DS event) {
		ConsistencyRule cre=event.getCre();
		if(allexecutedEventLog.containsKey(cre))
		{
			allexecutedEventLog.get(cre).add(event);
		}
		else
		{
			List<Event_DS> temp=Collections.synchronizedList(new LinkedList<Event_DS>());
			temp.add(event);
			allexecutedEventLog.put(cre, temp);
		}
	}

	// Update the tracked event with it's other properties such as repair template, rank, score etc.
	// After updating it adds the event into the cre current event list. 
	public void updateExecutedEventLog(SideEffect<ConsistencyRule> se_cre, PropertyUpdate clientop, Instance stepInst, OffsetDateTime dateTime, Repair_template rt,
			RepairAction ra) {
		List<Event_DS> creEventLog=allexecutedEventLog.get(se_cre.getInconsistency());
		//Assurance Check
		if(creEventLog==null)
		{
			System.out.println("Check it Out");
		}
		else
		{
			for(Event_DS event:creEventLog)
			{
				if(event.getCurrentEvent().equals(clientop) && event.getDatetime().equals(dateTime) && event.getSideEffect().equals(se_cre) && event.getStepInst().equals(stepInst))
				{
					event.setRt(rt);
				}
			}
		}
	}
	public void identifyUndo(Event_DS event) {
		PropertyUpdate op1=event.getCurrentEvent();
		//List<Event_DS> inverse=Collections.synchronizedList(new LinkedList<Event_DS>());
		if(op1 instanceof PropertyUpdateAdd || op1 instanceof PropertyUpdateRemove)
		{
			for(Event_DS ev: allexecutedEventLog.get(event.getCre()))
			{
				PropertyUpdate op2=ev.getCurrentEvent();
				if(op2 instanceof PropertyUpdateAdd || op2 instanceof PropertyUpdateRemove)
				{
					if((op1 instanceof PropertyUpdateAdd && op2 instanceof PropertyUpdateRemove) ||(op1 instanceof PropertyUpdateRemove && op2 instanceof PropertyUpdateAdd))
					{
						if(op1.elementId()==op2.elementId() && op1.name().equals(op2.name()))
						{
							inverseEventLog.put(event, ev);
							break;
						}
					}
				}
			}
		}
		else // in case of update operation
		{
			List<PropertyUpdate> operations=Collections.synchronizedList(new LinkedList<PropertyUpdate>());
			for(Event_DS ev: allexecutedEventLog.get(event.getCre()))
			{
				PropertyUpdate op2=ev.getCurrentEvent();
				if(!op1.equals(op2))
				{
					if(!(op2 instanceof PropertyUpdateAdd || op2 instanceof PropertyUpdateRemove))
					{
						if(op1.elementId()==op2.elementId() && op1.name().equals(op2.name()))
						{
							operations.add(op2);
							propUpdates.add(ev);
						}
					}
				}
			}
			//findundoSeries(op1,operations);
		}
	}

	public Map<ConsistencyRule, List<Event_DS>> getAllexecutedEventLog() {
		return allexecutedEventLog;
	}

	public void setAllexecutedEventLog(Map<ConsistencyRule, List<Event_DS>> allexecutedEventLog) {
		this.allexecutedEventLog = allexecutedEventLog;
	}

	public Map<ConsistencyRule, List<Event_DS>> getAllreleventEventLog() {
		return allreleventEventLog;
	}

	public void setAllreleventEventLog(Map<ConsistencyRule, List<Event_DS>> allreleventEventLog) {
		this.allreleventEventLog = allreleventEventLog;
	}

	public Map<ConsistencyRule, List<Event_DS>> getCre_CurrentEventLog() {
		return cre_CurrentEventLog;
	}

	public void setCre_CurrentEventLog(Map<ConsistencyRule, List<Event_DS>> cre_CurrentEventLog) {
		this.cre_CurrentEventLog = cre_CurrentEventLog;
	}

	public Map<ConsistencyRule, List<Event_DS>> getUnSelectRepairLog() {
		return unSelectRepairLog;
	}

	public void setUnSelectRepairLog(Map<ConsistencyRule, List<Event_DS>> unSelectRepairLog) {
		this.unSelectRepairLog = unSelectRepairLog;
	}

	public Map<Event_DS, Event_DS> getInverseEventLog() {
		return inverseEventLog;
	}

	public void setInverseEventLog(Map<Event_DS, Event_DS> inverseEventLog) {
		this.inverseEventLog = inverseEventLog;
	}

	public List<Event_DS> getPropUpdates() {
		return propUpdates;
	}

	public void setPropUpdates(List<Event_DS> propUpdates) {
		this.propUpdates = propUpdates;
	}


	public RepairStats getRs() {
		return rs;
	}

	public void setRs(RepairStats rs) {
		this.rs = rs;
	}

	public Map<ConsistencyRule, List<RestrictionAnalysisDS>> getRestrictionAnalysis() {
		return restrictionAnalysis;
	}

	public void setRestrictionAnalysis(Map<ConsistencyRule, List<RestrictionAnalysisDS>> restrictionAnalysis) {
		this.restrictionAnalysis = restrictionAnalysis;
	}




}
