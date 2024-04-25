package at.jku.isse.designspace.rule.arl.evaluator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.rule.arl.exception.ChangeExecutionException;
import at.jku.isse.designspace.rule.arl.expressions.AsTypeExpression;
import at.jku.isse.designspace.rule.arl.expressions.Expression;
import at.jku.isse.designspace.rule.arl.expressions.IteratorExpression;
import at.jku.isse.designspace.rule.arl.expressions.OperationCallExpression;
import at.jku.isse.designspace.rule.arl.expressions.PropertyCallExpression;
import at.jku.isse.designspace.rule.arl.expressions.VariableExpression;
import at.jku.isse.designspace.rule.arl.parser.ArlType;
import at.jku.isse.designspace.rule.arl.repair.AbstractRepairAction;
import at.jku.isse.designspace.rule.arl.repair.AlternativeRepairNode;
import at.jku.isse.designspace.rule.arl.repair.ConsistencyRepairAction;
import at.jku.isse.designspace.rule.arl.repair.Operator;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.arl.repair.RepairSingleValueOption;
import at.jku.isse.designspace.rule.arl.repair.RestrictionNode;
import at.jku.isse.designspace.rule.model.ConsistencyRule;
import at.jku.isse.designspace.rule.model.ReservedNames;

public class EvaluationNode {
    public Expression expression;
    public Object resultValue;
    public EvaluationNode[] children;
    public RepairNode repairTree;
    public HashSet scopeElements;
    public EvaluationNode parentEvalNode;
    public boolean isVariable; // means it will be kept as variable and not value.
    
    private int repairGap = 0;
    private boolean isOnRepairPath = false;
    private List<AbstractRepairAction> repairs = new LinkedList<>();
    public EvaluationNode(Expression e, Object r, EvaluationNode... c) {
        this.expression = e;
        this.resultValue = r;
        this.children = c;
        this.isVariable=false;
        setParentRelationship();
    }
    public EvaluationNode(Expression e, Object r, HashSet scopeElements, EvaluationNode... c) {
        this.expression = e;
        this.resultValue = r;
        this.scopeElements = scopeElements;
        this.children = c;
        setParentRelationship();
    }
    public EvaluationNode(Expression e, Object r, java.util.List<EvaluationNode> list) {
        this.expression = e;
        this.resultValue = r;
        this.children = (EvaluationNode[]) list.toArray(new EvaluationNode[list.size()]);
        setParentRelationship();
    }

    public String toString() { return expression.toString()+" == "+resultValue; }

    public boolean isVariable() {
		return isVariable;
	}
	public void setisVariable(boolean isVariable) {
		this.isVariable = isVariable;
	}
	
	public void incrementRepairGap() {
		repairGap++;
	}
	
	public void decrementRepairGap() {
		repairGap--;
	}
	
	public int getRepairGap() {
		return repairGap;
	}
	
	public RestrictionNode getBaseItemValue()
	{
		if(this.expression instanceof PropertyCallExpression)
		{
			this.setisVariable(true);
			RestrictionNode prop=this.expression.generateRestrictions(this, this.parentEvalNode.expression);
			RestrictionNode val=this.children[0].getBaseItemValue();
			return val.setNextNodeFluent(prop);
			//return this.children[0].getBaseItemValue();
		}
		else if(this.expression instanceof AsTypeExpression
				|| this.expression.toString().startsWith("FIRST") 
				|| this.expression.toString().startsWith("ASLIST"))
		{
			return this.children[0].getBaseItemValue();
		}
		else if(this.expression instanceof IteratorExpression)
		{
			for(EvaluationNode child: this.children)
			{
				if(child.expression instanceof PropertyCallExpression)
				{
					child.setisVariable(true);
					RestrictionNode prop=child.expression.generateRestrictions(child, this.expression);
					RestrictionNode val=child.getBaseItemValue();
					return val.setNextNodeFluent(prop);
				}
				else if(child.expression instanceof AsTypeExpression
						|| child.expression.toString().startsWith("FIRST") 
						|| child.expression.toString().startsWith("ASLIST"))
				{
					return child.getBaseItemValue();
				}
			}
			return null;
		}
		else if(this.expression instanceof VariableExpression)
		{
			Instance ins=(Instance) this.resultValue;
			if(ins.getPropertyNames().contains("workItemType"))
				return new RestrictionNode.ValueNode(ins.getPropertyAsValue("workItemType").toString()+" "+ins.name());
			else
				return new RestrictionNode.ValueNode(ins.name());
		}
		else
			return null;
	}
	public RestrictionNode generateValueBasedRestriction()
	{
		Collection resultColl=(Collection)this.resultValue;
		if(resultColl.size()>0)
		{
			Instance ins1=(Instance)resultColl.toArray()[0];
			if(resultColl.size()==1)
			{
				return new RestrictionNode.ValueNode(ins1.name());
			}
			else
			{
				Instance ins2=(Instance)resultColl.toArray()[1];
				RestrictionNode c1=new RestrictionNode.ValueNode(ins1.name());
				RestrictionNode c2=new RestrictionNode.ValueNode(ins2.name());
				RestrictionNode.OrNode rest=null;
				if(resultColl.size()==2)
				{
					rest=new RestrictionNode.OrNode(c1, c2);
				}
				else
				{
					for(int i=2;i<resultColl.size();i++)
					{
						RestrictionNode left=rest.getLhs();
						RestrictionNode right=rest.getLhs();
						Instance ins=(Instance)resultColl.toArray()[i];
						c2=new RestrictionNode.OrNode(left,new RestrictionNode.ValueNode(ins.name()));
						rest=new RestrictionNode.OrNode(right, c2);
					}
				}
				return rest;
			}
		}
		else
			return null;
	}
//	public void setExpectedValue(RepairSingleValueOption expectedValue) {
//		this.expectedValue = expectedValue;		
//	}
	
	public void markAsOnRepairPath() {
		if (!isOnRepairPath) { //not yet marked
			this.isOnRepairPath = true;
			if (this.parentEvalNode != null)
				this.parentEvalNode.markAsOnRepairPath(); // propagate upwards
		}
	}
	
	public void clearRepairPathInclChildren() {
		this.isOnRepairPath = false;
		this.repairs.clear();
		for (EvaluationNode child : children) {
			child.clearRepairPathInclChildren();
			
		}
	}
	
	public void addRepair(AbstractRepairAction repair) {
		this.repairs.add(repair);
	}
	
	public List<AbstractRepairAction> getRepairs() {
		return repairs;
	}
	
	public boolean isMarkedAsOnRepairPath() {
		return isOnRepairPath;
	}
	
	public RepairNode generateRepairTree(Object cre){
        if(this.repairTree == null){
            this.repairTree = new AlternativeRepairNode(null);
            this.repairTree.setInconsistency(cre);
            Instance instance = ((ConsistencyRule)cre).getPropertyAsInstance(ReservedNames.CONTEXT_INSTANCE);
            new ConsistencyRepairAction(this.repairTree,null, null, new RepairSingleValueOption(Operator.REMOVE, instance),this);
        }
        expression.generateRepairTree(repairTree,RepairSingleValueOption.TRUE,this);

        this.repairTree.flattenRepairTree();
        return this.repairTree;

    }





    public boolean generateRepairs(Object cre){
         if (resultValue != null && resultValue instanceof Boolean) {   
        	// an ARL constraint might not result in a boolean value, but might end in a hashset, which cant be repaired but should not throw an exception either
        	if((Boolean) resultValue)
        		return true;
        	if(this.repairTree==null)
        		generateRepairTree(cre);
        	//ConsistencyUtils.printRepairTree(this.repairTree);
        	return (Boolean) this.resultValue;
        } else
        	return false;
    }


    public void generateRepairTree(RepairNode parent, RepairSingleValueOption expectedValue){
        if(expectedValue!= null)
            expression.generateRepairTree(parent,expectedValue,this);

    }
    private void executeSequenceRepairs(RepairNode repairNode, HashSet scopeElements) throws ChangeExecutionException {
        if (!(Boolean) this.expression.evaluate(scopeElements).resultValue)
            for (RepairNode rn: repairNode.getChildren()){
                if(rn.isExecutable())
                    rn.execute();
                else{
                    if(rn.getNodeType() == RepairNode.Type.SEQUENCE)
                        executeSequenceRepairs(rn, scopeElements);
                    else
                        executeAlternativeRepairs(rn, scopeElements);
                }
            }
    }

    private void executeAlternativeRepairs(RepairNode repairNode, HashSet scopeElements) throws ChangeExecutionException {
        if (!(Boolean) this.expression.evaluate(scopeElements).resultValue)
            for (RepairNode rn: repairNode.getChildren()){
                if(rn.isExecutable()){
                    rn.execute();
                    if ((Boolean) this.expression.evaluate(scopeElements).resultValue) {
                        break;
                    }
                }
                else{
                    if(rn.getNodeType() == RepairNode.Type.SEQUENCE)
                        executeSequenceRepairs(rn, scopeElements);
                    else
                        executeAlternativeRepairs(rn, scopeElements);
                }
            }
    }



    public void addNode(EvaluationNode node) {
        List<EvaluationNode> arrlist
                = new ArrayList<>(
                Arrays.asList(this.children));
        arrlist.add(node);
        this.children = arrlist.toArray(this.children);
    }

    public Instance getInstanceValue() {
        if(children.length == 0)
            return (Instance) this.resultValue;
        else if(this.expression instanceof OperationCallExpression){
            OperationCallExpression operationCallExpression = (OperationCallExpression) this.expression;
            if(operationCallExpression.getOperation().equalsIgnoreCase("any")) {
                return (Instance) this.resultValue;
            }
        }
        else if(children[0].expression instanceof PropertyCallExpression) {
            if (children[0].resultValue == null)
                return null;
            if (children[0].resultValue instanceof Instance)
                return (Instance) children[0].resultValue;
        }
        return children[0].getInstanceValue();
    }

    public List<Instance> getInstanceCollectionValue(){
        if(children.length == 0)
            return new ArrayList<>((Collection) this.resultValue);
        else if(this.expression instanceof PropertyCallExpression){
            PropertyCallExpression propertyCallExpression = (PropertyCallExpression) this.expression;
            if(!propertyCallExpression.getResultType().collection.equals(ArlType.CollectionKind.SINGLE)) {
                return new ArrayList<>((Collection) this.resultValue);
            }
        }
        return children[0].getInstanceCollectionValue();
    }
    
    private void setParentRelationship() {
    	if (children == null) return;
    	for (EvaluationNode child : children) {
    		if (child != null)
    			child.parentEvalNode = this;
    	}
    }
    
   

}
