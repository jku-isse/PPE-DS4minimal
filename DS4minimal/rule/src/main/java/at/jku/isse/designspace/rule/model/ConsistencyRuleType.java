package at.jku.isse.designspace.rule.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import at.jku.isse.designspace.core.events.ElementCreate;
import at.jku.isse.designspace.core.model.Cardinality;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.MapProperty;
import at.jku.isse.designspace.core.model.SetProperty;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.service.WorkspaceService;
import lombok.extern.slf4j.Slf4j;

/**
 * a CRD (Consistency Rule Definition) is the definition of a consistency rule for an instance type. The CRD holds the
 * rule (a string) and the context instance type. There should only be one CRD for a given rule and CRD. A CRD
 * can create and delete CREs (Consitency Rule Evaluations) as instances are created and deleted. A CRD may also hold
 * a rule error message if the rule is invalid. Hence, a CRD has as many rule evaluations as there are instances for
 * the CRD's context instance type
 */
@Slf4j
public class ConsistencyRuleType extends RuleType {

    /**
     * creates a consistency rule definition (type) for a given context
     */
    static public ConsistencyRuleType create(Workspace workspace, InstanceType contextInstanceType, String name, String rule) {
        if (name==null) throw new IllegalArgumentException("name should not be null");
        if (rule==null) throw new IllegalArgumentException("rule should not be null");
        if (contextInstanceType==null) throw new IllegalArgumentException("contextInstanceType should not be null");
        if (!contextInstanceType.matchesWorkspace(workspace)) throw new IllegalArgumentException("contextInstanceType is from a different workspace");
        ConsistencyRuleType existingCrd = consistencyRuleTypeExists(workspace,name,contextInstanceType,rule);

        //throw new IllegalArgumentException("a ConsistencyRuleType with the given name OR with the given rule and context already exists in this workspace.");
        if (existingCrd != null) {
           log.debug("{};*****reuseeConsistencyRule {}", workspace, existingCrd);
            return existingCrd;
        }

        ConsistencyRuleType crd = new ConsistencyRuleType(workspace, workspace.its(META_CONSISTENCY_RULE_TYPE), contextInstanceType, name, rule, workspace.its(CONSISTENCY_RULE_TYPE));

        contextInstanceType.getFolder().addElement(crd);

        log.debug("{};*****createConsistencyRule {}", workspace, crd);
        return crd;
    }

    /**
     * Checks if there is a consistency rule type created in this workspace with the same rule and context
     * @param workspace
     * @param context
     * @param rule
     * @return true if there is already a rule type that matches the conditions given
     */
    static public ConsistencyRuleType consistencyRuleTypeExists(Workspace workspace, String name,InstanceType context, String rule){
        Collection<InstanceType> ruleDefinitions = workspace.its(ConsistencyRuleType.CONSISTENCY_RULE_TYPE).subTypes();
        if(ruleDefinitions.isEmpty())
            return null;
        if(ruleDefinitions.stream().filter(inst -> !inst.isDeleted).count() == 0)
            return null;
        for(ConsistencyRuleType crd: ruleDefinitions.stream()
        								.filter(inst -> !inst.isDeleted)
        								.filter(ConsistencyRuleType.class::isInstance)
        								.map(ConsistencyRuleType.class::cast)
        								.collect(Collectors.toSet()) ){            
            if (crd.name().equalsIgnoreCase(name) && crd.contextInstanceType().equals(context) && crd.rule().equals(rule))
                return crd;
        }
        return null;
    }       

    /**
     * returns all consistency rule definitions that match the given context or one of its supertypes
     */
    static public Collection<ConsistencyRuleType> allConsistencyRuleDefinitions(InstanceType contextInstanceType) {
        Collection<ConsistencyRuleType> ruleDefinitions = new HashSet();
        if(contextInstanceType==null) {
            return ruleDefinitions;
        }

        SetProperty ruleDefinitionProperty = contextInstanceType.getPropertyAsSet(ReservedNames.RULE_TYPES);
        if (ruleDefinitionProperty!=null) {
            ruleDefinitions.addAll(ruleDefinitionProperty.get());
        }

        // TODO: verify that this recursion will terminate
        contextInstanceType.superTypes().forEach(superType -> ruleDefinitions.addAll(allConsistencyRuleDefinitions(superType)));

        return ruleDefinitions;
    }


    //****************************************************************************
    //*** CONSTRUCTOR
    //****************************************************************************

    protected ConsistencyRuleType(Workspace workspace, InstanceType metaInstanceType, InstanceType contextInstanceType, String name, String rule, InstanceType... superTypes) {
        super(workspace, metaInstanceType, contextInstanceType, name, rule, superTypes);
    }
    protected ConsistencyRuleType(Workspace workspace, ElementCreate elementCreate) { super(workspace, elementCreate); }

    public String className() { return ReservedNames.CONSISTENCY_RULE_TYPE_CLASS_NAME; }
    public Rule instantiate(String name, Instance contextInstance) { return ConsistencyRule.create(workspace, this, name, contextInstance); }


    //****************************************************************************
    //*** BASICS
    //****************************************************************************

    public SetProperty<ConsistencyRule> consistencyRuleEvaluations() { return (SetProperty) instances(); }

    public ConsistencyRule consistencyRuleEvaluation(Instance contextInstance) {
        MapProperty ruleEvaluations = getPropertyAsMap(ReservedNames.RULE_EVALUATIONS_BY_CONTEXT_INSTANCE);
        ConsistencyRule cre = (ConsistencyRule) ruleEvaluations.get(contextInstance.id().toString());
        return cre;
    }

    @Override public String toString() {
        return "ConsistencyRuleType{"+workspace.name()+"}["+"rule="+rule()+",contextType="+contextInstanceType()+"]";
    }


    //****************************************************************************
    //*** META Type
    //****************************************************************************

    static public InstanceType META_CONSISTENCY_RULE_TYPE = null;
    static public InstanceType CONSISTENCY_RULE_TYPE = null;

    static public void buildType() {
        META_CONSISTENCY_RULE_TYPE = WorkspaceService.createInstanceType(
                WorkspaceService.PUBLIC_WORKSPACE,
                ReservedNames.META_CONSISTENCY_RULE_TYPE_NAME,
                WorkspaceService.PUBLIC_WORKSPACE.TYPES_FOLDER,
                META_RULE);

        CONSISTENCY_RULE_TYPE = WorkspaceService.createInstanceType(
                WorkspaceService.PUBLIC_WORKSPACE,
                ReservedNames.CONSISTENCY_RULE_TYPE_NAME,
                WorkspaceService.PUBLIC_WORKSPACE.TYPES_FOLDER,
                RULE);
        CONSISTENCY_RULE_TYPE.createPropertyType(ReservedNames.RESULT, Cardinality.SINGLE, WorkspaceService.PUBLIC_WORKSPACE.ELEMENT);

    }
}

