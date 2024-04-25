package at.jku.isse.designspace.rule.arl.repair.order;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.rule.arl.repair.RepairAction;

public class RepairStats {
	//List<CRE_DS> cre_DS = Collections.synchronizedList(new LinkedList<CRE_DS>());
	//List<Repair_template> RepTemplateLog = Collections.synchronizedList(new LinkedList<Repair_template>());
	Map<String, List<Repair_template>> RepTemplateLog = new HashMap<>();
	Map<Repair_template,List<Event_DS>> tempLog=new HashMap<>();
	Map<String,Map<InstanceType,List<PropertyChange_DS>>> propertyChangeMap=new HashMap<>();
	
	// Property Change Map Functions
		public int getPropertyChangeScore(String pUpdate, InstanceType iType, PropertyChange_DS pcD)
		{
			if(propertyChangeMap.containsKey(pUpdate))
			{
				Map<InstanceType,List<PropertyChange_DS>> extMap=propertyChangeMap.get(pUpdate);
				if(extMap.containsKey(iType))
				{
					List<PropertyChange_DS> extList=extMap.get(iType);
					for(PropertyChange_DS prop: extList)
					{
						if(prop.getPropertyName().equalsIgnoreCase(pcD.getPropertyName()))
						{
							return prop.getCount();
						}
					}
				}
				else
				{
					return 0;
				}
			
			}
			return 0;
		}
		public void addPropertyChange(String pUpdate, InstanceType iType, PropertyChange_DS pcD)
		{
			if(propertyChangeMap.containsKey(pUpdate))
			{
				Map<InstanceType,List<PropertyChange_DS>> extMap=propertyChangeMap.get(pUpdate);
				if(extMap.containsKey(iType))
				{
					// in case it exists
					List<PropertyChange_DS> extList=extMap.get(iType);
					boolean flag=false;
					for(PropertyChange_DS prop: extList)
					{
						if(prop.getPropertyName().equalsIgnoreCase(pcD.getPropertyName()))
						{
							prop.setCount(prop.getCount()+1);
							flag=true;
						}
					}
					if(!flag)
					{
						extList.add(pcD);
					}
				}
				else
				{
					// Adding new Instance type against property
					List<PropertyChange_DS> addList=Collections.synchronizedList(new LinkedList<PropertyChange_DS>());
					addList.add(pcD);
					extMap.put(iType, addList);
				}
			
			}
			else
			{
				// Adding new Property Update
				List<PropertyChange_DS> addList=Collections.synchronizedList(new LinkedList<PropertyChange_DS>());
				addList.add(pcD);
				Map<InstanceType,List<PropertyChange_DS>> addMap=new HashMap<>();
				addMap.put(iType, addList);
				propertyChangeMap.put(pUpdate, addMap);
			}
		}
		// End here
	
	public void addTempLog(Event_DS event,Repair_template rt)
	{
		if(tempLog.containsKey(rt))
		{
			tempLog.get(rt).add(event);
		}
		else
		{
			List<Event_DS> eventList=Collections.synchronizedList(new LinkedList<Event_DS>());
			eventList.add(event);
			tempLog.put(rt, eventList);
		}
	}
	
	public void addRTSelectScore(Event_DS event,String cre) {
		Repair_template curr_rt=event.getRt();
		boolean templateExisted=false;
		if(RepTemplateLog.containsKey(cre))
		{
			for(Repair_template rt:RepTemplateLog.get(cre))
			{
				if(rt.isRepairTemplateSame(curr_rt))
				{
					rt.setSelect_count(rt.getSelect_count()+1);
					addTempLog(event, curr_rt);
					templateExisted=true;
				}
			}
			if(!templateExisted)
			{
				addTempLog(event, curr_rt);
				curr_rt.setSelect_count(1);
				RepTemplateLog.get(cre).add(curr_rt);
			}
		}
		else
		{
			addTempLog(event, curr_rt);
			curr_rt.setSelect_count(1);
			List<Repair_template> temp_rtList=Collections.synchronizedList(new LinkedList<Repair_template>());
			temp_rtList.add(curr_rt);
			RepTemplateLog.put(cre, temp_rtList);
		}
	}
	public void addRTUnSelectScore(Event_DS event,String cre) {
		Repair_template curr_rt=event.getRt();
		boolean templateExisted=false;
		if(RepTemplateLog.containsKey(cre))
		{
			for(Repair_template rt:RepTemplateLog.get(cre))
			{
				if(rt.isRepairTemplateSame(curr_rt))
				{
					rt.setUnSelect_count(rt.getUnSelect_count()+1);
					templateExisted=true;
				}
			}
			if(!templateExisted)
			{
				curr_rt.setUnSelect_count(1);
				RepTemplateLog.get(cre).add(curr_rt);
			}
		}
		else
		{
			curr_rt.setUnSelect_count(1);
			List<Repair_template> temp_rtList=Collections.synchronizedList(new LinkedList<Repair_template>());
			temp_rtList.add(curr_rt);
			RepTemplateLog.put(cre, temp_rtList);
		}
	}
	
	public int getRTSelectScore(RepairAction ra, String cre)
	{
		Repair_template curr_rt=new Repair_template();
		curr_rt=curr_rt.toRepairTemplate(ra);
		if(RepTemplateLog.containsKey(cre))
		{
			for(Repair_template rt:RepTemplateLog.get(cre))
			{
				if(rt.isRepairTemplateSame(curr_rt))
				{
					return rt.getSelect_count();
				}
			}
			return 0;
		}
		else
			return 0;
	}
	
	public int getRTUnSelectScore(RepairAction ra, String cre)
	{
		Repair_template curr_rt=new Repair_template();
		curr_rt=curr_rt.toRepairTemplate(ra);
		if(RepTemplateLog.containsKey(cre))
		{
			for(Repair_template rt:RepTemplateLog.get(cre))
			{
				if(rt.isRepairTemplateSame(curr_rt))
				{
					return rt.getUnSelect_count();
				}
			}
			return 0;
		}
		else
			return 0;
	}
	
	
	public Map<String, List<Repair_template>> getRepTemplateLog() {
		return RepTemplateLog;
	}
	
	public Map<Repair_template, List<Event_DS>> getTempLog() {
		return tempLog;
	}
	
	public void setRepTemplateLog(Map<String, List<Repair_template>> repTemplateLog) {
		RepTemplateLog = repTemplateLog;
	}
	public Map<String, Map<InstanceType, List<PropertyChange_DS>>> getPropertyChangeMap() {
		return propertyChangeMap;
	}
	public void setPropertyChangeMap(Map<String, Map<InstanceType, List<PropertyChange_DS>>> propertyChangeMap) {
		this.propertyChangeMap = propertyChangeMap;
	}
	
	
	
	
	
	/*public int getRepSelectScore(RepairAction ra) {
		if (Rep_DS.size() != 0) {
			Repair_template temp=new Repair_template();
			temp=temp.toRepairTemplate(ra);
			for (Repair_template rt:Rep_DS) {
				if(rt.isRepairTemplateSame(rt, temp))
				{
					return rt.getSelect_count();
				}
			}
		}
		return 0;
	}
	 
	public int getRepUnSelectScore(RepairAction ra) {
		if (Rep_DS.size() != 0) {
			Repair_template temp=new Repair_template();
			temp=temp.toRepairTemplate(ra);
			for (Repair_template rt:Rep_DS) {
				if(rt.isRepairTemplateSame(rt, temp))
				{
					return rt.getUnSelect_count();
				}
			}
		}
		return 0;
	}
	 
	
	
	public List<Repair_template> getRepTemplateList()
	{
		return this.Rep_DS;
	}

	
	*/
	/*
	 public void display_cre_DS()
	{
		for(CRE_DS c:cre_DS)
		{
			System.out.println("CRE: "+ c.getCRE().ruleDefinition().toString());
			System.out.println("Client Operations with Positive Impact");
			Map<RepairAction,PropertyUpdate> clientop=c.getAllclientOp();
			clientop.entrySet().stream().forEach(entry->
			{
				PropertyUpdate c_op=entry.getValue();
				System.out.println(c_op.toString());
			});
		}
	}
	 public void display_SelectedRep_DS()
	{
		System.out.println("Select Score");
		for(Repair_template s_rep:Rep_DS)
		{
			System.out.println(s_rep.getOriginalARL()+"  "+s_rep.getOperator()+"   "+s_rep.getConcreteValue()+"   "+s_rep.getSelect_count());
		}
	}
	public void display_UnSelectedRep_DS()
	{
		System.out.println("UnSelect Score");
		for(Repair_template us_rep:Rep_DS)
		{
			System.out.println(us_rep.getOriginalARL()+"  "+us_rep.getOperator()+"   "+us_rep.getConcreteValue()+"   "+us_rep.getUnSelect_count());
		}
	}

	public List<CRE_DS> getCre_DS() {
		return cre_DS;
	}
	
	 
		public int getRepUnSelectScore(String arl, String operator, String concretevalue) 
		{
			if(Rep_DS.size()!=0)
			{
			for(Repair_template rt:Rep_DS)
			{
				if (rt.getOriginalARL().equals(arl) && rt.getOperator().equals(operator)) {
					if(rt.getConcreteValue() !=null &&  concretevalue!=null)
					{
						if(rt.getConcreteValue().equals(concretevalue))
							return rt.getUnSelect_count();
						else return 0;
					}
					else if(rt.getConcreteValue()==null && concretevalue==null)
						return rt.getUnSelect_count();
					else
						return 0;
				}
			}
			}
			return 0;
		}
		public int getRepSelectScore(String arl, String operator, String concretevalue) 
		{
			if(Rep_DS.size()!=0)
			{
			for(Repair_template rt:Rep_DS)
			{
				if (rt.getOriginalARL().equals(arl) && rt.getOperator().equals(operator)) {
					if(rt.getConcreteValue() !=null &&  concretevalue!=null)
					{
						if(rt.getConcreteValue().equals(concretevalue))
							return rt.getSelect_count();
						else return 0;
					}
					else if(rt.getConcreteValue()==null && concretevalue==null)
						return rt.getSelect_count();
					else
						return 0;
				}
			}
			}
			return 0;
		}*/
	
	 
}
