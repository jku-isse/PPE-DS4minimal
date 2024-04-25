package at.jku.isse.designspace.rule.model;

import at.jku.isse.designspace.core.events.ElementCreate;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.Workspace;

/**
 * a CRE (Consistency Rule Evaluation) is the evaluation of a CRD on an instance. The CRE holds the evaluation results
 * (i.e., most significantly if the instance is consistent with regard to the CRD). A CRE remains alive for as long
 * as both instance and CRD exist (i.e., the CRE is not re-created with every evaluation but rather the existing
 * evaluation is updated with every re-evaluation
 */
public class ConsistencyRule extends Rule implements Comparable<ConsistencyRule>{

    static public ConsistencyRule create(Workspace workspace, ConsistencyRuleType consistencyRuleDefinition, String name, Instance contextInstance) {
        return new ConsistencyRule(workspace, consistencyRuleDefinition, name, contextInstance);
    }

    protected ConsistencyRule(Workspace workspace, ConsistencyRuleType consistencyRuleType, String name, Instance contextInstance) {
        super(workspace, consistencyRuleType, name, contextInstance);
        getPropertyAsSingle(ReservedNames.IS_CONSISTENT).set(false);
    }
    protected ConsistencyRule(Workspace workspace, ElementCreate elementCreated) { super(workspace, elementCreated); }

    public String className() { return ReservedNames.CONSISTENCY_RULE_CLASS_NAME; }


    //****************************************************************************
    //*** Basics
    //****************************************************************************

    public boolean isConsistent() { return isDeleted() ? isDeleted(): (boolean)getPropertyAsSingle(ReservedNames.IS_CONSISTENT).get(); }

    public void setResult(Object result) {
        super.setResult(result);

        if (result instanceof Boolean)
            getPropertyAsSingle(ReservedNames.IS_CONSISTENT).set((boolean)result);
        else
            setEvaluationError("rule evaluation does not result in a boolean value. Presently it is " + result);
    }

    public ConsistencyRuleType consistencyRuleDefinition() { return (ConsistencyRuleType)ruleDefinition(); }

    @Override public String toString() {
        return "ConsistencyRule{"+workspace.name()+"}[isConsistent="+isConsistent()+",context="+ contextInstance()+",type="+getInstanceType()+"]";
    }

    @Override public boolean equals(Object o) {
        if (o==null || getClass() != o.getClass()) return false;
        if (o==this) return true;
        return this.hashCode() == o.hashCode();
    }

//    @Override
//    public int hashCode() {
//        Object consistent =  propertyAsValueOrNull(ReservedNames.IS_CONSISTENT);
//        if(consistent == null)
//            consistent = false;
//        return Objects.hash(consistent,name(),isDeleted(),id(),workspace);
//    }

    @Override
    public int compareTo(ConsistencyRule o) {
        return this.toString().compareTo(o.toString());
    }
}