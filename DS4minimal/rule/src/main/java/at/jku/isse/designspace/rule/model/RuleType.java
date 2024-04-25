package at.jku.isse.designspace.rule.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.util.Assert;

import at.jku.isse.designspace.core.events.ElementCreate;
import at.jku.isse.designspace.core.model.Cardinality;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.MapProperty;
import at.jku.isse.designspace.core.model.SetProperty;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.service.WorkspaceService;

/**
 * a rule type is the definition of a rule for an instance type. The rule type holds the rule (a string) and
 * the context instance type to which the rule applies. There should only be one rule type for a given rule and
 * context instance type. A rule type can create and delete rules as instances of the context instance type are
 * created and deleted. A rule type may also hold a rule error message if the rule is invalid (ill-formed). Hence,
 * a rule type has as many rule (evaluations) as there are instances for the rule type's context instance type
 */
public class RuleType extends InstanceType {

    /**
     * creates a rule type (definition) for a given context instance type
     */
    static public RuleType create(Workspace workspace, InstanceType contextInstanceType, String name, String rule) {
        if (name==null) throw new IllegalArgumentException("name should not be null");
        if (rule==null) throw new IllegalArgumentException("rule should not be null");
        if (contextInstanceType==null) throw new IllegalArgumentException("contextInstanceType should not be null");
        if (!contextInstanceType.matchesWorkspace(workspace)) throw new IllegalArgumentException("contextInstanceType is from a different workspace");

        RuleType ruleType = new RuleType(workspace, workspace.its(META_RULE), contextInstanceType, name, rule, workspace.INSTANCE_TYPE);

        contextInstanceType.getFolder().addElement(ruleType);

        return ruleType;
    }

    /**
     * returns all rule types (definitions) that match the given context or one of its supertypes
     */
    static public Collection<RuleType> allRuleTypes(InstanceType contextInstanceType) {
        if (contextInstanceType==null) throw new IllegalArgumentException("context instance type should not be null");

        HashSet<RuleType> allRuleTypes = new HashSet<>();
        allRuleTypes.addAll( (Set<RuleType>)contextInstanceType.getPropertyAsValueOrElse(ReservedNames.RULE_TYPES, () -> new HashSet<>()) );

        for (InstanceType superType : contextInstanceType.getAllSuperTypes())
            allRuleTypes.addAll( (Set<RuleType>)superType.getPropertyAsValueOrElse(ReservedNames.RULE_TYPES, () -> new HashSet<>()) );

        return allRuleTypes;
    }


    //****************************************************************************
    //*** CONSTRUCTOR
    //****************************************************************************

    protected RuleType(Workspace workspace, InstanceType metaInstanceType, InstanceType contextInstanceType, String name, String rule, InstanceType... superTypes) {
        super(workspace, metaInstanceType, name, superTypes);
        setRule(rule);
        setContextInstanceType(contextInstanceType);
    }
    protected RuleType(Workspace workspace, ElementCreate elementCreate) { super(workspace, elementCreate); }

    public String className() { return ReservedNames.RULE_TYPE_CLASS_NAME; }
    public Instance instantiate(String name) { throw new IllegalArgumentException("use instantiate(String name, Instance contextInstance)"); }
    public Rule instantiate(String name, Instance contextInstance) { return new Rule(workspace, this, name, contextInstance); }


    //****************************************************************************
    //*** BASICS
    //****************************************************************************

    public String rule() {
        return (String) getProperty(ReservedNames.RULE).get();
    }
    public void setRule(String rule) {
        getPropertyAsSingle(ReservedNames.RULE).set(rule);
    }
    public String getRuleXML() {
        return (String) getProperty(ReservedNames.RULE_XML).get();
    }
    public void setRuleXML(String ruleXML) {
        getPropertyAsSingle(ReservedNames.RULE_XML).set(ruleXML);
    }

    public String ruleError() {
        return (String) getProperty(ReservedNames.RULE_ERROR).get();
    }
    public void setRuleError(String ruleError) {
        getPropertyAsSingle(ReservedNames.RULE_ERROR).set(ruleError);
    }
    public boolean hasRuleError() {
        return ruleError()!=null;
    }

    @Override public String toString() {
        return "RuleType{"+workspace.name()+"}["+"rule="+rule()+",contextType="+contextInstanceType()+"]";
    }


    //****************************************************************************
    //*** CONTEXT INSTANCE TYPE
    //****************************************************************************

    public InstanceType contextInstanceType() {
        return (InstanceType) getProperty(ReservedNames.CONTEXT_INSTANCE_TYPE).get();
    }
    public void setContextInstanceType(InstanceType contextInstanceType) {
        getPropertyAsSingle(ReservedNames.CONTEXT_INSTANCE_TYPE).set(contextInstanceType);
    }

    public SetProperty<Rule> ruleEvaluations() { return (SetProperty) instances(); }
    public Rule ruleEvaluation(Instance contextInstance) {
        Assert.notNull(contextInstance, "Context instance must not be null");

        MapProperty ruleEvaluations = getPropertyAsMap(ReservedNames.RULE_EVALUATIONS_BY_CONTEXT_INSTANCE);
        Rule cre = (Rule)ruleEvaluations.get(contextInstance.id().toString());
        return cre;
    }

    //****************************************************************************
    //*** META Type
    //****************************************************************************

    static public InstanceType META_RULE = null;
    static public InstanceType RULE = null;

    static public void buildType() {
        META_RULE = WorkspaceService.createInstanceType(
                WorkspaceService.PUBLIC_WORKSPACE,
                ReservedNames.META_RULE_TYPE_NAME,
                WorkspaceService.PUBLIC_WORKSPACE.TYPES_FOLDER,
                WorkspaceService.PUBLIC_WORKSPACE.META_INSTANCE_TYPE);

        RULE = WorkspaceService.createInstanceType(
                WorkspaceService.PUBLIC_WORKSPACE,
                ReservedNames.RULE_TYPE_NAME,
                WorkspaceService.PUBLIC_WORKSPACE.TYPES_FOLDER,
                WorkspaceService.PUBLIC_WORKSPACE.INSTANCE_TYPE);


        META_RULE.createPropertyType(ReservedNames.RULE, Cardinality.SINGLE, WorkspaceService.PUBLIC_WORKSPACE.STRING);
        META_RULE.createPropertyType(ReservedNames.RULE_XML, Cardinality.SINGLE, WorkspaceService.PUBLIC_WORKSPACE.STRING);
        META_RULE.createOpposablePropertyType(ReservedNames.CONTEXT_INSTANCE_TYPE, Cardinality.SINGLE, WorkspaceService.PUBLIC_WORKSPACE.META_INSTANCE_TYPE, ReservedNames.RULE_TYPES, Cardinality.SET, true);
        META_RULE.createPropertyType(ReservedNames.RULE_ERROR, Cardinality.SINGLE, WorkspaceService.PUBLIC_WORKSPACE.STRING);
        META_RULE.createPropertyType(ReservedNames.RULE_EVALUATIONS_BY_CONTEXT_INSTANCE, Cardinality.MAP, RULE);

        RULE.createPropertyType(ReservedNames.IS_CONSISTENT, Cardinality.SINGLE, WorkspaceService.PUBLIC_WORKSPACE.BOOLEAN);
        RULE.createOpposablePropertyType(ReservedNames.CONTEXT_INSTANCE, Cardinality.SINGLE, WorkspaceService.PUBLIC_WORKSPACE.INSTANCE_TYPE, ReservedNames.RULE_EVALUATIONS_IN_CONTEXT, Cardinality.SET, true);
        RULE.createPropertyType(ReservedNames.EVALUATION_ERROR, Cardinality.SINGLE, WorkspaceService.PUBLIC_WORKSPACE.STRING);
        RULE.createOpposablePropertyType(ReservedNames.PROPERTIES_IN_SCOPE, Cardinality.SET, WorkspaceService.PUBLIC_WORKSPACE.INSTANCE_TYPE, ReservedNames.RULE_EVALUATIONS_IN_SCOPE, Cardinality.SET, true);
    }
}

