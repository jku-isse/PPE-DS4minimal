package at.jku.isse.designspace.rule.model;

import at.jku.isse.designspace.core.model.Cardinality;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.service.WorkspaceService;

public class DerivedPropertyRuleSetType extends DerivedPropertyRuleType{

    protected DerivedPropertyRuleSetType(Workspace workspace, InstanceType metaInstanceType, InstanceType contextInstanceType, String name, String rule, InstanceType... superTypes) {
        super(workspace, metaInstanceType, contextInstanceType, name, rule, superTypes);
    }

    public String className() { return ReservedNames.DERIVED_PROPERTY_SET_RULE_TYPE_CLASS_NAME; }

    public Rule instantiate(String name, Instance contextInstance) {
        return DerivedPropertySetRule.create(workspace, this, name, contextInstance);
    }

    //****************************************************************************
    //*** META Type
    //****************************************************************************

    static public InstanceType DERIVED_PROPERTY_SET_RULE = null;

    static public void buildType() {
        DERIVED_PROPERTY_SET_RULE = WorkspaceService.createInstanceType(
                WorkspaceService.PUBLIC_WORKSPACE,
                ReservedNames.DERIVED_PROPERTY_RULE_SET_TYPE_NAME,
                WorkspaceService.PUBLIC_WORKSPACE.TYPES_FOLDER,
                DERIVED_PROPERTY_RULE);
        DERIVED_PROPERTY_SET_RULE.createPropertyType(ReservedNames.RESULT, Cardinality.SET, WorkspaceService.PUBLIC_WORKSPACE.ELEMENT);
    }
}
