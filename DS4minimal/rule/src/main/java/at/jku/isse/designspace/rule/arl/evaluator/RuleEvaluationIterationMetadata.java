package at.jku.isse.designspace.rule.arl.evaluator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import at.jku.isse.designspace.core.events.Operation;
import at.jku.isse.designspace.rule.model.Rule;
import lombok.Data;

/**
 * @author Christoph Mayr-Dorn
 * 
 * Collects all the operations that triggered a rule evaluation, 
 * and whether that re-evaluation caused a different rule fulfillment outcome
 */
@Data
public class RuleEvaluationIterationMetadata {

	final Rule rule;
	@SuppressWarnings("rawtypes")
	RuleEvaluation eval;
	Boolean hasEvaluationOutcomeChanged;
	Set<Operation> evaluationTriggers = new HashSet<>();
	
	public void addOperation(Operation op) {
		evaluationTriggers.add(op);
	}
	
	public void addOperations(Collection<Operation> ops ) {
		evaluationTriggers.addAll(ops);
	}
	
	public void update(Entry<RuleEvaluation, Boolean> evalOutcome) {
		this.eval = evalOutcome.getKey();
		this.hasEvaluationOutcomeChanged = evalOutcome.getValue();
	}
}
