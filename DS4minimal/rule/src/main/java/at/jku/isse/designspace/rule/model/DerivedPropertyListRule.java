package at.jku.isse.designspace.rule.model;

import java.util.List;

import at.jku.isse.designspace.core.events.ElementCreate;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.Workspace;


/**
 *  DerivedProperty for Lists
 */
public class DerivedPropertyListRule extends DerivedPropertyRule {

    protected DerivedPropertyListRule(Workspace workspace, RuleType ruleType, String name, Instance contextInstance) {
        super(workspace, ruleType, name, contextInstance);
        getPropertyAsSingle(ReservedNames.IS_CONSISTENT).set(false);
    }

    protected DerivedPropertyListRule(Workspace workspace, ElementCreate elementCreate) {
        super(workspace, elementCreate);
    }


    static public DerivedPropertyListRule create(Workspace workspace, RuleType ruleType, String name, Instance contextInstance) {
        return new DerivedPropertyListRule(workspace, ruleType, name, contextInstance);
    }
    public String className() { return ReservedNames.DERIVED_PROPERTY_LIST_RULE_CLASS_NAME; }


    //****************************************************************************
    //*** Basics
    //****************************************************************************

    public void setResult(Object result) {
        super.setResult(result);
        String propertyName = derivedPropertyRuleDefinition().name();
        List<Object> listResult = (List<Object>) result;
        var list = contextInstance().getPropertyAsList(propertyName);
        list.clear();
        for (Object o : listResult)
            list.add(o);
        evaluateRulesInScope(propertyName); // to update result of CREs
    }

    public DerivedPropertyRuleType derivedPropertyRuleDefinition() { return (DerivedPropertyRuleListType)ruleDefinition(); }

    @Override public String toString() {
        return "DerivedPropertyRule{"+workspace.name()+"}[result="+getPropertyAsList(ReservedNames.RESULT).get()+",context="+ contextInstance()+",type="+getInstanceType()+"]";
    }
}