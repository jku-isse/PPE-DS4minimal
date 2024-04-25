package at.jku.isse.designspace.core.model;

import static at.jku.isse.designspace.core.model.ReservedNames.ENUMERATION_CLASS_NAME;

import at.jku.isse.designspace.core.events.ElementCreate;

/**
 * Enumerations are special InstanceTypes and the various literals are Instances of those InstanceTypes
 */
public class Enumeration extends Instance {

    static public Enumeration create(Workspace workspace, EnumerationType enumerationType, String name) { return new Enumeration(workspace, enumerationType, name); }

    protected Enumeration(Workspace workspace, InstanceType instanceType, String name) { super(workspace, instanceType, name); }
    protected Enumeration(Workspace workspace, ElementCreate elementCreate) { super(workspace, elementCreate); }

    public EnumerationType enumerationType() { return (EnumerationType) getInstanceType(); }

    public String className() { return ENUMERATION_CLASS_NAME; }
}
