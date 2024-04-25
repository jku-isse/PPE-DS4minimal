package at.jku.isse.designspace.core.model;

import java.util.Collection;

import at.jku.isse.designspace.core.events.ElementCreate;
import at.jku.isse.designspace.core.events.Operation;

/**
 * A PropertyType defines a Property - its referenced type and its cardinality. When added to an instanceType
 * then its Instances will have a Property of this type.
 * {@link InstanceType} definitions. It is an essential building block of types.
 */
public class MetaPropertyType extends PropertyType {

    String name=null;
    Cardinality cardinality=null;
    boolean isOptional=false;
    InstanceType referencedInstanceType=null;


    public MetaPropertyType(Workspace workspace, String name, Cardinality cardinality, boolean isOptional) {
        super(workspace);

        this.name=name;
        this.cardinality=cardinality;
        this.isOptional=isOptional;

        elementChanged(new ElementCreate(this.id(), className(), workspace.META_PROPERTY_TYPE.id()));
        workspace.state.load(this);
    }

    public MetaPropertyType(Workspace workspace, String name, Cardinality cardinality) {
        this(workspace, name, cardinality, false);
    }
    public MetaPropertyType(Workspace workspace, String name, Cardinality cardinality, InstanceType referencedInstanceType) {
        this(workspace, name, cardinality, false, referencedInstanceType);
    }
    
    public MetaPropertyType(Workspace workspace, String name, Cardinality cardinality, InstanceType referencedInstanceType, PropertyType opposedPropertyType, boolean isContainer, boolean isContained) {
        super(workspace);
        elementChanged(new ElementCreate(this.id(), className(), workspace.META_PROPERTY_TYPE.id()));
        workspace.state.load(this);

        new SingleProperty(this, ReservedNames.NAME, name, workspace.GENERIC_SINGLE_PROPERTY_TYPE);
        new SingleProperty(this, ReservedNames.CARDINALITY, cardinality.toString(), workspace.GENERIC_SINGLE_PROPERTY_TYPE);
        new SingleProperty(this, ReservedNames.ISOPTIONAL, isOptional, workspace.GENERIC_SINGLE_PROPERTY_TYPE);
        new SingleProperty(this, ReservedNames.REFERENCED_INSTANCE_TYPE, referencedInstanceType, workspace.GENERIC_SINGLE_PROPERTY_TYPE);
        new SingleProperty(this, ReservedNames.NATIVE_TYPE, null, workspace.GENERIC_SINGLE_PROPERTY_TYPE);
        new SingleProperty(this, ReservedNames.DERIVED_RULE, null, workspace.GENERIC_SINGLE_PROPERTY_TYPE);
        new SingleProperty(this, ReservedNames.OPPOSED_PROPERTY_TYPE, opposedPropertyType, workspace.GENERIC_SINGLE_PROPERTY_TYPE);
        if(opposedPropertyType != null) {
            opposedPropertyType.getPropertyAsSingle(ReservedNames.OPPOSED_PROPERTY_TYPE).set(this);
        }
        new SingleProperty(this, ReservedNames.IS_CONTAINER, isContainer, workspace.GENERIC_SINGLE_PROPERTY_TYPE);
        new SingleProperty(this, ReservedNames.IS_CONTAINED, isContained, workspace.GENERIC_SINGLE_PROPERTY_TYPE);
        this.name = name;
        this.cardinality = cardinality;
        this.isOptional = false;
        this.referencedInstanceType = referencedInstanceType;
    }

    public MetaPropertyType(Workspace workspace, String name, Cardinality cardinality, boolean isOptional, InstanceType referencedInstanceType) {
        super(workspace);
        elementChanged(new ElementCreate(this.id(), className(), workspace.META_PROPERTY_TYPE.id()));
        workspace.state.load(this);

        new SingleProperty(this, ReservedNames.NAME, name, workspace.GENERIC_SINGLE_PROPERTY_TYPE);
        new SingleProperty(this, ReservedNames.CARDINALITY, cardinality.toString(), workspace.GENERIC_SINGLE_PROPERTY_TYPE);
        new SingleProperty(this, ReservedNames.ISOPTIONAL, isOptional, workspace.GENERIC_SINGLE_PROPERTY_TYPE);
        new SingleProperty(this, ReservedNames.REFERENCED_INSTANCE_TYPE, referencedInstanceType, workspace.GENERIC_SINGLE_PROPERTY_TYPE);
        new SingleProperty(this, ReservedNames.NATIVE_TYPE, null, workspace.GENERIC_SINGLE_PROPERTY_TYPE);
        new SingleProperty(this, ReservedNames.DERIVED_RULE, null, workspace.GENERIC_SINGLE_PROPERTY_TYPE);
        new SingleProperty(this, ReservedNames.OPPOSED_PROPERTY_TYPE, null, workspace.GENERIC_SINGLE_PROPERTY_TYPE);

        this.name=name;
        this.cardinality=cardinality;
        this.isOptional=isOptional;
        this.referencedInstanceType=referencedInstanceType;
    }

    public String name() { return this.name; }
    public Cardinality cardinality() { return this.cardinality; }
    public boolean isOptional() { return this.isOptional; }
    public InstanceType referencedInstanceType() { return this.referencedInstanceType; }

    public Collection<Property> properties() {
        return null;
    }
    public Collection<String> propertyNames() {
        return null;
    }

    @Override public String toString() { return "<MetaPropertyType{"+id+"}>"; }

    public boolean matchesWorkspace(Workspace workspace) { return true; }
    public boolean isAssignable(Object value) { return true; }

    public SetProperty<InstanceType> superTypes(){ return null; }

    public void delete() { throw new IllegalArgumentException("meta elements cannot be modified"); }
    public boolean isDeleted() { return false; }
    public void setInstanceType(InstanceType instanceType) { throw new IllegalArgumentException("meta elements cannot be modified"); }
    public void setName(String name) { throw new IllegalArgumentException("meta elements cannot be modified"); }
    public Folder getFolder() { return null; }
    public InstanceType getInstanceType() {return null; }
    public SetProperty<Instance> instances() { return new SetProperty<>(this); }

    public void reconstruct(Operation operation) { throw new IllegalArgumentException("meta elements cannot be modified"); }
    public String nativeType() { return null; }
    public MetaPropertyType opposedPropertyType() { return null; }
    public Evaluator derivedPropertyEvaluator() { return null; }
    public MetaPropertyType subPropertyType(String propertyName)  { return null; }

    public SingleProperty createSingleProperty(String propertyName, Object value, PropertyType propertyType) { throw new IllegalArgumentException("meta elements cannot be modified"); }
    public SetProperty createSetProperty(String propertyName, PropertyType propertyType)  { throw new IllegalArgumentException("meta elements cannot be modified"); }
    public ListProperty createListProperty(String propertyName, PropertyType propertyType) { throw new IllegalArgumentException("meta elements cannot be modified"); }
    public MapProperty createMapProperty(String propertyName, PropertyType propertyType)  { throw new IllegalArgumentException("meta elements cannot be modified"); }
}