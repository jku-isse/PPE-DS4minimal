package at.jku.isse.designspace.rule.arl.expressions;

import java.util.HashSet;

import at.jku.isse.designspace.rule.arl.evaluator.EvaluationNode;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.arl.repair.RepairSingleValueOption;
import at.jku.isse.designspace.rule.arl.repair.SequenceRepairNode;

public class NextExpression extends TemporalExpression {


    public NextExpression(Expression<Boolean> rule) {
        super(rule);
    }

    @Override
    public String getARL() {
        return "NEXT(" + rule.getARL() + ")";
    }

    @Override
    public String getOriginalARL(int indentation, boolean isOnNewLine) {
    	String whitespace = isOnNewLine ? createWhitespace(indentation) : "";
    	return whitespace+"next(" + rule.getOriginalARL(indentation+2, false) + ")";
    }
    
    @Override 
	public String getLocalARL() { return "NEXT";	}

    @Override
    protected EvaluationNode evaluate(HashSet scopeElements, Expression rule, EvaluationNodeHistoryKey key, EvaluationNodeHistory historyNode) {
        //first evaluation just triggers it
        EvaluationNode node = rule.evaluate(scopeElements);

        if (!evaluationNodes.containsKey(key)) { //first evaluation for this instance
            EvaluationNode newNode = new EvaluationNode(this, true, node);
            historyNode.setLastEvaluation(newNode);
            historyNode.setTriggered();
            evaluationNodes.put(key, historyNode); //add it to the history, but do not consider the evaluation
            return newNode; //first evaluation is always true because there is no regression
        }

        if (!historyNode.isTerminated()) {
            EvaluationNode newNode = new EvaluationNode(this, node.resultValue, node);
            historyNode.setLastEvaluation(newNode);
            if (canTerminate(key))
                historyNode.setTerminated();
            return newNode;
        }
        return historyNode.getLastEvaluation();
    }

    //Next cannot have a trigger
//    @Override
//    public EvaluationNode evaluateWithTrigger(HashSet scopeElements, Expression trigger, Expression rule, EvaluationNodeHistoryKey key, EvaluationNodeHistory historyNode) {
//        return null;
//    }

    @Override
    public void generateRepairTree(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
        RepairNode node = new SequenceRepairNode(parent);
        evaluationNode.children[0].generateRepairTree(node, expectedValue);
    }
}
