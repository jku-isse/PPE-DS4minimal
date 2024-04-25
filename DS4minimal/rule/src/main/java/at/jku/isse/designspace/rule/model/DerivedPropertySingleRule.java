package at.jku.isse.designspace.rule.model;

import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.Workspace;


/**
 *  DerivedProperty for singles
 */
public class DerivedPropertySingleRule extends DerivedPropertyRule {

    protected DerivedPropertySingleRule(Workspace workspace, RuleType ruleType, String name, Instance contextInstance) {
        super(workspace, ruleType, name, contextInstance);
        getPropertyAsSingle(ReservedNames.IS_CONSISTENT).set(false);
    }

    static public DerivedPropertySingleRule create(Workspace workspace, RuleType ruleType, String name, Instance contextInstance) {
        return new DerivedPropertySingleRule(workspace, ruleType, name, contextInstance);
    }
    public String className() { return ReservedNames.DERIVED_PROPERTY_SINGLE_RULE_CLASS_NAME; }


    //****************************************************************************
    //*** Basics
    //****************************************************************************

    public void setResult(Object result) {
        super.setResult(result);
        String propertyName = derivedPropertyRuleDefinition().name();
        contextInstance().getPropertyAsSingle(propertyName).set(result);
        evaluateRulesInScope(propertyName); // to update result of CREs
    }



    public DerivedPropertyRuleType derivedPropertyRuleDefinition() { return (DerivedPropertyRuleSingleType)ruleDefinition(); }

    @Override public String toString() {
        return "DerivedPropertyRule{"+workspace.name()+"}[result="+getPropertyAsSingle(ReservedNames.RESULT).get()+",context="+ contextInstance()+",type="+getInstanceType()+"]";
    }
}