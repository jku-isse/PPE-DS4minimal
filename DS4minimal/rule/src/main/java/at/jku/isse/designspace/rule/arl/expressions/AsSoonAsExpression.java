package at.jku.isse.designspace.rule.arl.expressions;

import java.util.HashSet;

import at.jku.isse.designspace.rule.arl.evaluator.EvaluationNode;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.arl.repair.RepairSingleValueOption;
import at.jku.isse.designspace.rule.arl.repair.SequenceRepairNode;

public class AsSoonAsExpression extends TemporalExpression {

    private Expression<Boolean> trigger;

    public AsSoonAsExpression(Expression<Boolean> trigger, Expression<Boolean> rule) {
        super(rule);
        this.trigger = trigger;
        this.setParent(this);
    }

    @Override
    public String getARL() {
        return "ASSOONAS(" + trigger.getARL() + "," + rule.getARL() + ")";
    }

    @Override
    public String getOriginalARL(int indentation, boolean isOnNewLine) {
    	String whitespace = createWhitespace(indentation);
    	String whitespaceBegin = isOnNewLine ? whitespace : "";
        return whitespaceBegin+"asSoonAs(" + trigger.getOriginalARL(indentation+2, false) + ",\r\n"+
    			whitespace + rule.getOriginalARL(indentation+2, true) +"\r\n"+ 
    			whitespace+")";
    }
    
    @Override 
	public String getLocalARL() { return "AS SOON AS"; }

    @Override
    protected EvaluationNode evaluate(HashSet scopeElements, Expression rule, EvaluationNodeHistoryKey key, EvaluationNodeHistory historyNode) {
        EvaluationNode triggerNode = trigger.evaluate(scopeElements);

        //first evaluation or if the trigger is true again
        if (!historyNode.isTriggered() || triggerNode.resultValue.equals(true)) {

            //if the trigger is false, we just ignore the rule for now and return true
            if (triggerNode.resultValue.equals(false)) {
                EvaluationNode newNode = new EvaluationNode(this, true, triggerNode);
                historyNode.setLastEvaluation(newNode);
                return newNode;
            }
            //else {
// FIXME testing the disabling of resetting upon trigger reeval.
            //                if (historyNode.isTriggered() && !historyNode.isTerminated()) //if the trigger becomes true again, reset
//                	resetAllTemporalChildExressionsRecursively(key);
//            }

            historyNode.setTriggered();
            //if the trigger is true, we evaluate the rule
            EvaluationNode node = rule.evaluate(scopeElements);

            //if the rule is false the first time, wait for it to be true next time
            if (node.resultValue.equals(false)) {
                EvaluationNode newNode = new EvaluationNode(this, false, triggerNode, node);
                historyNode.setLastEvaluation(newNode); //add it to the history, but do not consider the evaluation
                return newNode;
            } else {
                EvaluationNode newNode = new EvaluationNode(this, true, triggerNode, node);
                historyNode.setLastEvaluation(newNode); //it is true now, right when the trigger is true, so we return true
                if (canTerminate(key)) historyNode.setTerminated();
                return newNode;
            }
        } else {
            EvaluationNode previousNode = historyNode.getLastEvaluation();
            //if not yet terminated, we continue the evaluation
            if (!historyNode.isTerminated() && historyNode.isTriggered()) {
                EvaluationNode node = rule.evaluate(scopeElements);
                EvaluationNode newNode;
                if (node.resultValue.equals(false)) {
                    newNode = new EvaluationNode(this, false, triggerNode, node);
                    historyNode.setLastEvaluation(newNode);

                } else {
                    newNode = new EvaluationNode(this, true, triggerNode, node);
                    historyNode.setLastEvaluation(newNode); //it is true now, right when the trigger is true, so we return true

                }

                if (canTerminate(key))
                    historyNode.setTerminated();
                return newNode;
            }

            return previousNode;
        }
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
