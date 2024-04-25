package at.jku.isse.designspace.rule.checker;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import at.jku.isse.designspace.core.events.Operation;
import at.jku.isse.designspace.core.events.PropertyUpdate;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.Property;
import at.jku.isse.designspace.core.model.SetProperty;
import at.jku.isse.designspace.rule.arl.evaluator.EvaluationNode;
import at.jku.isse.designspace.rule.arl.evaluator.RuleEvaluation;
import at.jku.isse.designspace.rule.arl.evaluator.RuleEvaluationIterationMetadata;
import at.jku.isse.designspace.rule.arl.evaluator.RuleEvaluationListener;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.model.DerivedPropertyRule;
import at.jku.isse.designspace.rule.model.ReservedNames;
import at.jku.isse.designspace.rule.model.Rule;
import at.jku.isse.designspace.rule.model.RuleType;
import at.jku.isse.designspace.rule.service.RuleService;
import lombok.extern.slf4j.Slf4j;

/**
 * An implementation of {@link RuleEvaluator} doing nothing. This basically only
 * serves as placeholder for actual, future implementations and is necessary to successfully
 * fulfill any autowiring dependencies on this interface.
 */
@Slf4j
abstract public class RuleEvaluator {

	Map<Rule,RuleEvaluationIterationMetadata> changedCREs = new HashMap<>();
	 //   HashSet<Rule> changedCREs = new HashSet();
	LinkedList<RuleEvaluationListener> listeners = new LinkedList<>();
	
	public void registerListener(RuleEvaluationListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}
	
    /**
     * when a rule is created then rule evaluations need to be created for every instance of the rule's context type.
     * This also applies to instances of subtypes.
     */
    public void ruleCreated(RuleType ruleType) {
        if (ruleType.isDeleted()) return;    //possible if rule is deleted in the same transaction as it was created

        for (Instance instance : ruleType.contextInstanceType().instancesIncludingSubtypes()) {
            Rule cre = evaluationCreated(ruleType, instance);
            if (cre!=null) changedCREs.computeIfAbsent(cre, k -> new RuleEvaluationIterationMetadata(cre));
        }
    }

    /**
     * when a rule is updated then rule evaluations need to be deleted/created anew if the rule's context type changed
     * or rule evaluations need to be re-evaluated if the rule's condition changed.
     */
    public void ruleUpdated(RuleType ruleType, PropertyUpdate op) {
        if (ruleType.isDeleted()) return;    //possible if rule is deleted in the same transaction as it was modified

        if (op.name().equals(ReservedNames.RULE)) {
        	ruleType.ruleEvaluations().stream().forEach(cre->changedCREs.computeIfAbsent(cre, k -> new RuleEvaluationIterationMetadata(cre)).addOperation(op));
        }
        else if (op.name().equals(ReservedNames.CONTEXT_INSTANCE_TYPE)) {
            ruleDeleted(ruleType);
            ruleCreated(ruleType);
        }

    }

    /**
     * when a rule is deleted then all rule evaluations need to be deleted where the evaluations's rule matches
     * the rule.
     */
    public void ruleDeleted(RuleType ruleType) {
        if (ruleType.isDeleted()) return;    //possible if rule is deleted in the same transaction as it was created

        for (Rule rule : new HashSet<Rule>(ruleType.ruleEvaluations())) {
            rule.delete();
            changedCREs.remove(rule);
        }
    }


    /**
     * when an instance is created then rule evaluations need to be created for every rule definition where the
     * rule's context type matches the instance's type
     */
    public void instanceCreated(Instance instance) {
        if (instance.isDeleted()) return;    //possible if instance is deleted in the same transaction as it was created

        InstanceType instanceType = instance.getInstanceType();
        //Check if this instance's instanceType is the context of any rule definition
        for (RuleType ruleType : RuleType.allRuleTypes(instanceType)) {
            Rule rule = evaluationCreated(ruleType, instance);
            if (rule!=null) changedCREs.computeIfAbsent(rule, k -> new RuleEvaluationIterationMetadata(rule));
        }
    }

    /**
     * when a property of an instance is updated then those rule evaluations need to be updated where the evaluations's
     * scope contains the property
     */
    public void instanceUpdated(Instance instance, PropertyUpdate op) {
        if (instance.isDeleted()) return;    //possible if instance is deleted in the same transaction as it was modified

        Property property = instance.getProperty(op.name());
        if (property==null) return;

        updateDerivedPropertyRule(instance, Set.of(op));
        if (!property.hasSubProperty(ReservedNames.RULE_EVALUATIONS_IN_SCOPE)) return;
        for (Rule rule : new HashSet<>((Set<Rule>)property.subProperty(ReservedNames.RULE_EVALUATIONS_IN_SCOPE).get())) {
            //FIXME: FOR PERFORMANCE IMPROVEMENT LETS NOT EVAL HERE AND NOT USE DERIVED PROPERTIES FOR NOW
        	//evaluate(rule);  // must evaluate subProperties here as their results may be used by the derived properties
            //if(changedCREs.contains(rule))
            //    changedCREs.remove(rule);
        	changedCREs.computeIfAbsent(rule, k -> new RuleEvaluationIterationMetadata(rule)).addOperation(op);
        	
        	// TODO: lets also capture which property changes lead to the rule evaluation, 
        	// so we collect all possible sources that result in further violation, neutral, or (towards) fixing
        	// CRS: SetOf:<PropertyChanges>
        }

    }
    
//    public Set<Rule> getRulesToBeEvaluated(Instance instance, String propertyName) {
//        if (instance.isDeleted()) 
//        	return Collections.emptySet();    //possible if instance is deleted in the same transaction as it was modified
//        Property property = instance.getProperty(propertyName);
//        if (property==null) 
//        	return Collections.emptySet();
//        updateDerivedPropertyRule(instance);
//        if (!property.hasSubProperty(ReservedNames.RULE_EVALUATIONS_IN_SCOPE)) 
//        	return Collections.emptySet();
//        
//        return ((Set<Rule>) property.subProperty(ReservedNames.RULE_EVALUATIONS_IN_SCOPE).get()).stream().collect(Collectors.toSet());                
//    }
//
//    public void evaluateRules(Set<Rule> rules) {
//    	rules.stream().peek(rule -> evaluate(rule))
//    		.forEach(rule -> {
//    	           if(changedCREs.contains(rule))
//    	                changedCREs.remove(rule);
//    		});
//    }

    public void updateDerivedPropertyRule(Instance instance, Collection<Operation> ops){
        // adds derived properties from other instances that have this property in their scope
        for(Property subProperty : instance.getProperties()) {
            if(subProperty.name.contains(ReservedNames.RULE_EVALUATIONS_IN_SCOPE)){
                SetProperty subProperties = (SetProperty) instance.getProperty(subProperty.name);
                for(Object rule : subProperties.get()) {
                    if(rule instanceof DerivedPropertyRule) { // adds affected DerivedProperties to be re-evaluated
                        DerivedPropertyRule derivedProp = (DerivedPropertyRule) rule;
                        DerivedPropertyRule ruleLocal = RuleService.currentWorkspace.its(derivedProp);
                        evaluate(ruleLocal);                        
                        changedCREs.computeIfAbsent(ruleLocal, k -> new RuleEvaluationIterationMetadata(ruleLocal)).addOperations(ops);
                    }
                }
            }
        }

    }


    /**
     * when an instance is deleted then all rule evaluations need to be deleted where the evaluation's context refers
     * to this instance
     */
    public void instanceDeleted(Instance instance) {
        SetProperty<Rule> rules = instance.getPropertyAsSet(ReservedNames.RULE_EVALUATIONS_IN_CONTEXT);
        Set<Rule> newRules = new HashSet<>(rules);// I added this to prevent "concurrentmodificationexception"
        for (Rule rule : newRules) {
            changedCREs.remove(rule);
            if (!rule.isDeleted()) evaluationDeleted(rule);
        }
    }

    public Rule evaluationCreated(RuleType crd, Instance instance) {
        Rule cr = crd.ruleEvaluation(instance);
        if (cr==null) cr=crd.instantiate(crd.name(), instance);
        return cr;
    }

    public void evaluationDeleted(Rule rule) {
        rule.delete();
    }

    public void evaluateAll() {    	
        Set<RuleEvaluationIterationMetadata> reim = changedCREs.values().stream()
        		.filter(ruleMeta -> ruleMeta.getRule()!=null && !ruleMeta.getRule().isDeleted())
        		.map(ruleMeta -> {
        			ruleMeta.update(evaluate(ruleMeta.getRule()));
                    log.debug("{};------>evaluate;{}", RuleService.currentWorkspace, ruleMeta.getRule());
                    if(ruleMeta.getRule() instanceof DerivedPropertyRule) {
                        updateDerivedPropertyRule(ruleMeta.getRule().contextInstance(), ruleMeta.getEvaluationTriggers());
                    }
                    return ruleMeta;
        		})
        		.collect(Collectors.toSet());
//    	for (RuleEvaluationIterationMetadata ruleMeta : new HashSet<>(changedCREs.values())) {
//            if (ruleMeta.getRule()!=null && !ruleMeta.getRule().isDeleted()) {
//                ruleMeta.update(evaluate(ruleMeta.getRule()));
//                RuleService.logger.debug("{};------>evaluate;{}", RuleService.currentWorkspace, ruleMeta.getRule());
//                if(ruleMeta.getRule() instanceof DerivedPropertyRule) {
//                    updateDerivedPropertyRule(ruleMeta.getRule().contextInstance(), ruleMeta.getEvaluationTriggers());
//                }
//            }
//        }
        listeners.stream().forEach(listener -> listener.signalRuleEvaluationFinished(reim));
        changedCREs.clear();
    }
    abstract public Entry<RuleEvaluation, Boolean> evaluate(Rule cre);

    public  RepairNode repairTree(Rule cre){return  null;}
    public  EvaluationNode evaluationTree(Rule cre){return  null;}
}

