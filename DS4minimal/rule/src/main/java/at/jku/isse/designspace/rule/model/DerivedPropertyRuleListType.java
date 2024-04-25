package at.jku.isse.designspace.rule.model;

import at.jku.isse.designspace.core.events.ElementCreate;
import at.jku.isse.designspace.core.model.Cardinality;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.service.WorkspaceService;

public class DerivedPropertyRuleListType extends DerivedPropertyRuleType{

    protected DerivedPropertyRuleListType(Workspace workspace, InstanceType metaInstanceType, InstanceType contextInstanceType, String name, String rule, InstanceType... superTypes) {
        super(workspace, metaInstanceType, contextInstanceType, name, rule, superTypes);
    }

    protected DerivedPropertyRuleListType(Workspace workspace, ElementCreate elementCreate) {
        super(workspace, elementCreate);
    }

    public String className() { return ReservedNames.DERIVED_PROPERTY_LIST_RULE_TYPE_CLASS_NAME; }

    public Rule instantiate(String name, Instance contextInstance) {
        return DerivedPropertyListRule.create(workspace, this, name, contextInstance);
    }

    //****************************************************************************
    //*** META Type
    //****************************************************************************

    static public InstanceType DERIVED_PROPERTY_LIST_RULE = null;

    static public void buildType() {
        DERIVED_PROPERTY_LIST_RULE = WorkspaceService.createInstanceType(
                WorkspaceService.PUBLIC_WORKSPACE,
                ReservedNames.DERIVED_PROPERTY_RULE_LIST_TYPE_NAME,
                WorkspaceService.PUBLIC_WORKSPACE.TYPES_FOLDER,
                DERIVED_PROPERTY_RULE);
        DERIVED_PROPERTY_LIST_RULE.createPropertyType(ReservedNames.RESULT, Cardinality.LIST, WorkspaceService.PUBLIC_WORKSPACE.ELEMENT);
    }
}
