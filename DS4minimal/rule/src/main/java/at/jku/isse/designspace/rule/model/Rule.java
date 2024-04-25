package at.jku.isse.designspace.rule.model;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import at.jku.isse.designspace.core.events.ElementCreate;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.Property;
import at.jku.isse.designspace.core.model.SetProperty;
import at.jku.isse.designspace.core.model.Workspace;

/**
 * a CRE (Consistency Rule Evaluation) is the evaluation of a CRD on an instance. The CRE holds the evaluation results
 * (i.e., most significantly if the instance is consistent with regard to the CRD). A CRE remains alive for as long
 * as both instance and CRD exist (i.e., the CRE is not re-created with every evaluation but rather the existing
 * evaluation is updated with every re-evaluation
 */
public class Rule extends Instance {

    static public Rule create(Workspace workspace, RuleType ruleType, String name, Instance contextInstance) {
        return new Rule(workspace, ruleType, name, contextInstance);
    }

    protected Rule(Workspace workspace, RuleType ruleType, String name, Instance contextInstance) {
        super(workspace, ruleType, name);
        getPropertyAsSingle(ReservedNames.CONTEXT_INSTANCE).set(contextInstance);
        getPropertyAsSingle(ReservedNames.RESULT).set(false);
        ruleType.getPropertyAsMap(ReservedNames.RULE_EVALUATIONS_BY_CONTEXT_INSTANCE).put(contextInstance.id().toString(), this);
    }
    protected Rule(Workspace workspace, ElementCreate elementCreate) { super(workspace, elementCreate); }

    public String className() { return ReservedNames.RULE_CLASS_NAME; }


    @Override
    public void delete() {
        ruleDefinition().getPropertyAsMap(ReservedNames.RULE_EVALUATIONS_BY_CONTEXT_INSTANCE).remove(contextInstance().id().toString());
        isDeleted = true;
        //super.delete();
    }

    //****************************************************************************
    //*** Basics
    //****************************************************************************

    public Instance contextInstance() {
        return (Instance) getPropertyAsSingle(ReservedNames.CONTEXT_INSTANCE).get();
    }

    public boolean result() {
        return (boolean) getPropertyAsSingle(ReservedNames.RESULT).get();
    }
    public void setResult(Object result) {
        if(result instanceof Set) {
            Set<Object> setResult = (Set<Object>) result;
            for (Object o : setResult)
                getPropertyAsSet(ReservedNames.RESULT).add(o);
        } else if(result instanceof List) {
            List<Object> listResult = (List<Object>) result;
            getPropertyAsList(ReservedNames.RESULT).clear();
            for (Object o : listResult)
                getPropertyAsList(ReservedNames.RESULT).add(o);
        }
        else
            getPropertyAsSingle(ReservedNames.RESULT).set(result);
    }

    public String evaluationError() { return (String)getProperty(ReservedNames.EVALUATION_ERROR).get(); }
    public void setEvaluationError(String evaluationError) { getPropertyAsSingle(ReservedNames.EVALUATION_ERROR).set(evaluationError); }
    public boolean hasEvaluationError() {
        return evaluationError()!=null;
    }

    public RuleType ruleDefinition() { return (RuleType) getInstanceType(); }

    @Override public String toString() {
        return "Rule{"+workspace.name()+"}[result="+result()+",context="+ contextInstance()+",type="+ getInstanceType()+"]";
    }


    //****************************************************************************
    //*** Scope
    //****************************************************************************

    public void addPropertyToScope(Property property) {
        if (property == null) return;
    	SetProperty ruleEvaluations = (SetProperty)property.subProperty(ReservedNames.RULE_EVALUATIONS_IN_SCOPE);
        if (ruleEvaluations==null) {
            ruleEvaluations=property.createSetSubProperty(property.name+"/"+ReservedNames.RULE_EVALUATIONS_IN_SCOPE, workspace.INSTANCE_TYPE.getPropertyType(ReservedNames.RULE_EVALUATIONS_IN_SCOPE));
        }
        ruleEvaluations.add(this);
    }
    public void removePropertyFromScope(Property property) {
        if (property == null) return;
    	SetProperty ruleEvaluations = (SetProperty)property.subProperty(ReservedNames.RULE_EVALUATIONS_IN_SCOPE);
        ruleEvaluations.remove(this);
    }
    public void setPropertiesInScope(Set<Property> newPropertiesInScope) throws ClassCastException{
        Set<Property> oldPropertiesInScope = (Set<Property>) getPropertyAsSet(ReservedNames.PROPERTIES_IN_SCOPE).stream().filter(o -> o instanceof Property).collect(Collectors.toSet());

        for (Property property : oldPropertiesInScope) {
            if (!newPropertiesInScope.contains(property)) {
                removePropertyFromScope(property);
            }
        }
        for (Property property : newPropertiesInScope) {
            if (!oldPropertiesInScope.contains(property)) {
                addPropertyToScope(property);
            }
        }

    }
}