package at.jku.isse.designspace.rule.model;

import java.util.Set;

import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.Workspace;


/**
 *  DerivedProperty for sets
 */
public class DerivedPropertySetRule extends DerivedPropertyRule {

    protected DerivedPropertySetRule(Workspace workspace, RuleType ruleType, String name, Instance contextInstance) {
        super(workspace, ruleType, name, contextInstance);
        getPropertyAsSingle(ReservedNames.IS_CONSISTENT).set(false);
    }


    static public DerivedPropertySetRule create(Workspace workspace, RuleType ruleType, String name, Instance contextInstance) {
        return new DerivedPropertySetRule(workspace, ruleType, name, contextInstance);
    }
    public String className() { return ReservedNames.DERIVED_PROPERTY_SET_RULE_CLASS_NAME; }


    //****************************************************************************
    //*** Basics
    //****************************************************************************

    public void setResult(Object result) {
        super.setResult(result);
        String propertyName = derivedPropertyRuleDefinition().name();
        Set<Object> setResult = (Set<Object>) result;
        for (Object o : setResult)
            contextInstance().getPropertyAsSet(propertyName).add(o);
        evaluateRulesInScope(propertyName); // to update result of CREs
    }

    public DerivedPropertyRuleType derivedPropertyRuleDefinition() { return (DerivedPropertyRuleSetType)ruleDefinition(); }

    @Override public String toString() {
        return "DerivedPropertyRule{"+workspace.name()+"}[result="+getPropertyAsSet(ReservedNames.RESULT).get()+",context="+ contextInstance()+",type="+getInstanceType()+"]";
    }
}