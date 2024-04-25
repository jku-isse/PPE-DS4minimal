package at.jku.isse.designspace.rule.arl.evaluator;

import java.util.Set;

public interface RuleEvaluationListener {

	
	public void signalRuleEvaluationFinished(Set<RuleEvaluationIterationMetadata> iterationMetadata);
	
	
}
