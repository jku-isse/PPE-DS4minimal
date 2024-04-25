package at.jku.isse.designspace.rule.arl.expressions;

import java.util.HashSet;

import at.jku.isse.designspace.rule.arl.evaluator.EvaluationNode;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.arl.repair.RepairSingleValueOption;
import at.jku.isse.designspace.rule.arl.repair.SequenceRepairNode;

public class DefExpression<RT> extends Expression<RT> {

    protected String variableName;
    protected Expression<RT> bodyExpression;
    protected TypeExpression typeExpression;

    EvaluationNode variableNode;
    EvaluationNode inNode;

    public DefExpression(String variable, TypeExpression type,Expression body) {
        super();
        this.variableName = variable;
        this.typeExpression = type;
        this.bodyExpression = body;
        this.bodyExpression.setParent(this);
        this.resultType = type.value;
        //InstanceService.createProperty(null,type.value,variableName, Cardinality.MANDATORY_SET);
    }

    @Override
    public EvaluationNode evaluate(HashSet scopeElements)  {
        inNode = this.bodyExpression.evaluate(scopeElements);
        return new EvaluationNode(this, inNode.resultValue, variableNode, inNode);
    }

    @Override
    public RT evaluate(Expression child) {return null;}

    @Override
    public Object getValueForVariable(Expression variable) {
        if (((VariableExpression<?>) variable).name.equals(variableName))
            return this.variableNode.resultValue;
        else
            return parent.getValueForVariable(variable);
    }

    @Override
    public String getARL() {
        return "Def(" + this.variableName + " : "  + this.typeExpression + " = "+ this.bodyExpression.getARL()+")";
    }
    
    @Override
    public String getOriginalARL(int indentation, boolean isOnNewLine) {
    	String whitespace = createWhitespace(indentation);
        return whitespace+"def " + this.variableName + " : "  + this.typeExpression + " = "+ this.bodyExpression.getARL();
    }
    
    @Override 
	public String getLocalARL() { return "Def";	}

    @Override
    public void generateRepairTree(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
        RepairNode node = new SequenceRepairNode(parent);
        evaluationNode.children[2].generateRepairTree(node,expectedValue);
    }
}