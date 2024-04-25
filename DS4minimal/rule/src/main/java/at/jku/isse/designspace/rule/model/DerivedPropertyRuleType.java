package at.jku.isse.designspace.rule.model;

import at.jku.isse.designspace.core.events.ElementCreate;
import at.jku.isse.designspace.core.model.Cardinality;
import at.jku.isse.designspace.core.model.InstanceType;
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
public class DerivedPropertyRuleType extends RuleType {
    /**
     * creates a consistency rule definition (type) for a given context
     */
    static public DerivedPropertyRuleType create(Workspace workspace, InstanceType contextInstanceType, String name, Cardinality cardinality, String rule) {
        if (name==null) throw new IllegalArgumentException("name should not be null");
        if (rule==null) throw new IllegalArgumentException("rule should not be null");
        if (contextInstanceType==null) throw new IllegalArgumentException("contextInstanceType should not be null");
        if (contextInstanceType.getPropertyType(name)==null) throw new IllegalArgumentException("contextInstanceType does not declare a property with name "+name);
        if (!contextInstanceType.matchesWorkspace(workspace)) throw new IllegalArgumentException("contextInstanceType is from a different workspace");
        DerivedPropertyRuleType crd = null;
        switch (cardinality) {
            case SINGLE:
                crd = new DerivedPropertyRuleSingleType(workspace, workspace.its(META_DERIVED_PROPERTY_RULE), contextInstanceType,
                        name, rule, workspace.its(DerivedPropertyRuleSingleType.DERIVED_PROPERTY_SINGLE_RULE));
                break;
            case SET:
                crd = new DerivedPropertyRuleSetType(workspace, workspace.its(META_DERIVED_PROPERTY_RULE), contextInstanceType,
                        name, rule, workspace.its(DerivedPropertyRuleSetType.DERIVED_PROPERTY_SET_RULE));
                break;
            case LIST:
                crd = new DerivedPropertyRuleListType(workspace, workspace.its(META_DERIVED_PROPERTY_RULE), contextInstanceType,
                        name, rule, workspace.its(DerivedPropertyRuleListType.DERIVED_PROPERTY_LIST_RULE));
                break;
            default:
        }



        contextInstanceType.getFolder().addElement(crd);

        log.debug("{};*****createDerivedPropertyRule {}", workspace, crd);
        return crd;
    }


    //****************************************************************************
    //*** CONSTRUCTOR
    //****************************************************************************

    protected DerivedPropertyRuleType(Workspace workspace, InstanceType metaInstanceType, InstanceType contextInstanceType, String name, String rule, InstanceType... superTypes) {
        super(workspace, metaInstanceType, contextInstanceType, name, rule, superTypes);
    }
    protected DerivedPropertyRuleType(Workspace workspace, ElementCreate elementCreate) { super(workspace, elementCreate); }

    public String className() { return ReservedNames.DERIVED_PROPERTY_RULE_TYPE_CLASS_NAME; }



    //****************************************************************************
    //*** BASICS
    //****************************************************************************

    @Override public String toString() {
        return "DerivedPropertyRule{"+workspace.name()+"}["+"rule="+rule()+",contextInstanceType="+contextInstanceType()+",name="+name()+"]";
    }

    //****************************************************************************
    //*** META Type
    //****************************************************************************

    static public InstanceType META_DERIVED_PROPERTY_RULE = null;
    static public InstanceType DERIVED_PROPERTY_RULE = null;

    static public void buildType() {
        META_DERIVED_PROPERTY_RULE = WorkspaceService.createInstanceType(
                WorkspaceService.PUBLIC_WORKSPACE,
                ReservedNames.META_DERIVED_PROPERTY_RULE_TYPE_NAME,
                WorkspaceService.PUBLIC_WORKSPACE.TYPES_FOLDER,
                META_RULE);

        DERIVED_PROPERTY_RULE = WorkspaceService.createInstanceType(
                WorkspaceService.PUBLIC_WORKSPACE,
                ReservedNames.DERIVED_PROPERTY_RULE_TYPE_NAME,
                WorkspaceService.PUBLIC_WORKSPACE.TYPES_FOLDER,
                RULE);
    }
}

