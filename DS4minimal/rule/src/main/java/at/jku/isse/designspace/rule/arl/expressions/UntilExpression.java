package at.jku.isse.designspace.rule.arl.expressions;

import java.util.HashSet;

import at.jku.isse.designspace.rule.arl.evaluator.EvaluationNode;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.arl.repair.RepairSingleValueOption;
import at.jku.isse.designspace.rule.arl.repair.SequenceRepairNode;

public class UntilExpression extends TemporalExpression {

    private Expression<Boolean> stopCondition;

    public UntilExpression(Expression<Boolean> rule, Expression<Boolean> stopCondition) {
        super(rule);
        this.stopCondition = stopCondition;
        this.stopCondition.setParent(this);
    }

    @Override
    public String getARL() {
        return "UNTIL(" + rule.getARL() + "," + stopCondition.getARL() + ")";
    }

    @Override
    public String getOriginalARL(int indentation, boolean isOnNewLine) {
    	String whitespace = createWhitespace(indentation);
    	String whitespaceBegin = isOnNewLine ? whitespace : "";
        return whitespaceBegin+"until(" + rule.getOriginalARL(indentation+2, false) + ",\r\n" +
    			whitespace+stopCondition.getOriginalARL(indentation+2, true) + "\r\n" +
    			whitespace+ ")";
    }
    
	@Override 
	public String getLocalARL() { return "UNTIL";	}

    @Override
    protected EvaluationNode evaluate(HashSet scopeElements, Expression rule, EvaluationNodeHistoryKey key, EvaluationNodeHistory historyNode) {
        EvaluationNode stopNode = stopCondition.evaluate(scopeElements);

        if (stopNode.resultValue.equals(true)) {
            EvaluationNode node = rule.evaluate(scopeElements);
            EvaluationNode newNode = new EvaluationNode(this, true, stopNode, node);
            historyNode.setLastEvaluation(newNode); //the end condition is fulfilled
            if (canTerminate(key)) historyNode.setTerminated();
            return newNode;
        }

        //first evaluation or if the trigger is true again
        if (!historyNode.isTerminated()) {
            //if the trigger is true, we evaluate the rule
            EvaluationNode node = rule.evaluate(scopeElements);

            EvaluationNode newNode = new EvaluationNode(this, false, stopNode, node);
            historyNode.setLastEvaluation(newNode);
            if (node.resultValue.equals(false) && canTerminate(key)) {
                historyNode.setTerminated();
            }
            return newNode;
        }

        return historyNode.getLastEvaluation();
    }


    @Override
    public void generateRepairTree(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
        RepairNode node = new SequenceRepairNode(parent);
        evaluationNode.children[0].generateRepairTree(node, expectedValue);
        evaluationNode.children[1].generateRepairTree(node, expectedValue);
    }
}
