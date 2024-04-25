package at.jku.isse.designspace.rule.arl.expressions;

import java.util.HashSet;

import at.jku.isse.designspace.rule.arl.evaluator.EvaluationNode;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.arl.repair.RepairSingleValueOption;
import at.jku.isse.designspace.rule.arl.repair.SequenceRepairNode;

public class AlwaysExpression extends TemporalExpression {


    public AlwaysExpression(Expression<Boolean> rule) {
        super(rule);
    }

    @Override
    public String getARL() {
        return "ALWAYS(" + rule.getARL() + ")";
    }

    @Override
    public String getOriginalARL(int indentation, boolean isOnNewLine) {
    	String whitespace = isOnNewLine ? createWhitespace(indentation) : "";
    	return whitespace+"always(" + rule.getOriginalARL(indentation+2, false) + ")";
    }
    
    @Override 
	public String getLocalARL() { return "ALWAYS"; }

    @Override
    protected EvaluationNode evaluate(HashSet scopeElements, Expression rule, EvaluationNodeHistoryKey key, EvaluationNodeHistory historyNode) {
        EvaluationNode node = rule.evaluate(scopeElements);

        if (historyNode.getLastEvaluation() == null) { //first evaluation for this instance
            EvaluationNode newNode = new EvaluationNode(this, node.resultValue, node);
            historyNode.setLastEvaluation(newNode);
            historyNode.setTriggered();
            evaluationNodes.put(key, historyNode);
            if (node.resultValue.equals(false))
                historyNode.setTerminated(); //if true, the expression was met successfully
            return newNode;
        }

        //after it is terminated, return the same result forever
        if (!historyNode.isTerminated()) {
            if (node.resultValue.equals(true)) {
                EvaluationNode newNode = new EvaluationNode(this, node.resultValue, node);
                historyNode.setLastEvaluation(newNode);
                return newNode;
            }

            EvaluationNode newNode = new EvaluationNode(this, node.resultValue, node);
            historyNode.setLastEvaluation(newNode);
            if (node.resultValue.equals(false))
                historyNode.setTerminated();
            return newNode;
        }
        return historyNode.getLastEvaluation();
    }

    @Override
    public void generateRepairTree(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
        RepairNode node = new SequenceRepairNode(parent);
        evaluationNode.children[0].generateRepairTree(node, expectedValue);
    }
}