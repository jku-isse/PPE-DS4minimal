package at.jku.isse.designspace.rule.arl.expressions;

import java.util.HashSet;
import java.util.Objects;

import at.jku.isse.designspace.rule.arl.evaluator.EvaluationNode;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.arl.repair.RepairSingleValueOption;
import at.jku.isse.designspace.rule.arl.repair.SequenceRepairNode;

public class EverytimeExpression extends TemporalExpression {

    private Expression<Boolean> trigger;

    public EverytimeExpression(Expression<Boolean> trigger, Expression<Boolean> rule) {
        super(rule);
        this.trigger = trigger;
        this.trigger.setParent(this);
    }

    @Override
    public String getARL() {
        return "EVERYTIME(" + trigger.getARL() + "," + rule.getARL() + ")";
    }

    @Override
    public String getOriginalARL(int indentation, boolean isOnNewLine) {
    	String whitespace = createWhitespace(indentation);
    	String whitespaceBegin = isOnNewLine ? whitespace : "";
        return whitespaceBegin+"everytime(" + trigger.getOriginalARL(indentation+2, false) + ",\r\n" +
    			whitespace+ rule.getOriginalARL(indentation+2, true) + ")";
    }

    @Override 
	public String getLocalARL() { return "EVERYTIME";	}
    
    @Override
    protected EvaluationNode evaluate(HashSet scopeElements, Expression rule, EvaluationNodeHistoryKey key, EvaluationNodeHistory historyNode) {
        EvaluationNode triggerNode = trigger.evaluate(scopeElements);

        if (triggerNode.resultValue.equals(false) && !historyNode.isTriggered()) {
            return new EvaluationNode(this, true, triggerNode);
        }

        //first evaluation or if the trigger is true again
        if (triggerNode.resultValue.equals(true)) {

            if (!historyNode.isTriggered() && !historyNode.isTerminated())
                resetAllTemporalChildExressionsRecursively(key);


            historyNode.setTriggered();
            //if the trigger is true, we evaluate the rule
            EvaluationNode node = rule.evaluate(scopeElements);

            //if the rule is false the first time, wait for it to be true next time
            if (node.resultValue.equals(false)) {
                EvaluationNode newNode = new EvaluationNode(this, false, triggerNode, node);
                historyNode.setLastEvaluation(newNode); //add it to the history, but do not consider the evaluation
                if (canTerminate(key))
                    historyNode.setTerminated();
                return newNode;
            } else {
                EvaluationNode newNode = new EvaluationNode(this, true, triggerNode, node);
                historyNode.setLastEvaluation(newNode); //it is true now, right when the trigger is true, so we return true
                historyNode.reset();
                return newNode;
            }
        }

        EvaluationNode previousNode = historyNode.getLastEvaluation();
        //if not yet terminated, we continue the evaluation
        if (historyNode.isTriggered() && !historyNode.isTerminated()) {
            EvaluationNode node = rule.evaluate(scopeElements);

            //if it is the second execution after a trigger, we evaluate the rule one last time
            if (previousNode.resultValue.equals(false)) {
                if (node.resultValue.equals(false)) {
                    EvaluationNode newNode = new EvaluationNode(this, false, triggerNode, node);
                    historyNode.setLastEvaluation(newNode);
                    if (canTerminate(key)) historyNode.setTerminated();
                    return newNode;
                } else {
                    EvaluationNode newNode = new EvaluationNode(this, true, triggerNode,node);
                    historyNode.setLastEvaluation(newNode); //it is true now, right when the trigger is true, so we return true
                    historyNode.reset();
                    return newNode;
                }
            } else {
                EvaluationNode newNode = new EvaluationNode(this, node.resultValue, triggerNode, node);
                historyNode.setLastEvaluation(newNode); //it is true now, right when the trigger is true, so we return true
                if (node.resultValue.equals(false) && canTerminate(key))
                    historyNode.setTerminated(); //it used to be true and now it is violated
                if (node.resultValue.equals(true)) {
                    getAllChildTemporalExpressionsRecursively(this)
                            .map(tempExpr -> new EvaluationNodeHistoryKey(key.instance, tempExpr, getIteratorScopeChain(tempExpr)))
                            .map(childKey -> TemporalExpression.evaluationNodes.get(childKey))
                            .filter(Objects::nonNull)
                            .filter(childHistoryNode -> childHistoryNode != null && childHistoryNode.isTerminated())
                            .forEach(childHistoryNode -> childHistoryNode.reset());
                    historyNode.reset();
                }
                return newNode;
            }
        }

        return previousNode;
    }

    @Override
    public void generateRepairTree(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
        boolean aValue = (Boolean)evaluationNode.children[0].resultValue;
        if (evaluationNode.children.length > 1) {
        	boolean bValue = (Boolean)evaluationNode.children[1].resultValue;
        	if(expectedValue.getExpectedEvaluationResult()){
        		RepairNode node = new SequenceRepairNode(parent);
        		// repair b
        		if(aValue && !bValue)
        			evaluationNode.children[1].generateRepairTree(node, expectedValue);
        		// repair a
        		else if(!aValue)
        			evaluationNode.children[0].generateRepairTree(node, expectedValue);
        	}
        } else
        	return; // if there is only trigger active yet, nothing to repair.
    }
}