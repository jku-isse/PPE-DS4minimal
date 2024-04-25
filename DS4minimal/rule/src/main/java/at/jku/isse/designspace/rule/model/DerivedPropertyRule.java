package at.jku.isse.designspace.rule.model;

import java.util.Set;

import at.jku.isse.designspace.core.events.ElementCreate;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.rule.service.RuleService;


/**
 * a CRE (Consistency Rule Evaluation) is the evaluation of a CRD on an instance. The CRE holds the evaluation results
 * (i.e., most significantly if the instance is consistent with regard to the CRD). A CRE remains alive for as long
 * as both instance and CRD exist (i.e., the CRE is not re-created with every evaluation but rather the existing
 * evaluation is updated with every re-evaluation
 */
public class DerivedPropertyRule extends Rule {



    protected DerivedPropertyRule(Workspace workspace, RuleType ruleType, String name, Instance contextInstance) {
        super(workspace, ruleType, name, contextInstance);
        getPropertyAsSingle(ReservedNames.IS_CONSISTENT).set(false);
    }

    protected DerivedPropertyRule(Workspace workspace, ElementCreate elementCreate) {
        super(workspace, elementCreate);
    }

    public String className() { return ReservedNames.DERIVED_PROPERTY_RULE_CLASS_NAME; }


    //****************************************************************************
    //*** Basics
    //****************************************************************************

    /**
     * Checks for CREs that are in the scope of this property, then evaluates them.
     * @param propertyName derived property name
     */
    protected void evaluateRulesInScope(String propertyName){
        if(contextInstance().getProperty(propertyName).hasSubProperty(ReservedNames.RULE_EVALUATIONS_IN_SCOPE)){
            Set<Object> rulesInContext = (Set<Object>) contextInstance().
                    getProperty(propertyName).subProperty(ReservedNames.RULE_EVALUATIONS_IN_SCOPE).get();
            for (Object rule : rulesInContext){
                if(rule instanceof ConsistencyRule) {
                    ConsistencyRule rule1 = (ConsistencyRule) rule;
                    RuleService.evaluator.evaluate(rule1);
                }
            }
        }
    }

    public DerivedPropertyRuleType derivedPropertyRuleDefinition() { return (DerivedPropertyRuleType)ruleDefinition(); }

    @Override public String toString() {
        return "DerivedPropertyRule{"+workspace.name()+"}[result="+getPropertyAsSingle(ReservedNames.RESULT).get()+",context="+ contextInstance()+",type="+getInstanceType()+"]";
    }
}