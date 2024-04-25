package at.jku.isse.designspace.core.model;

import java.util.Set;

import at.jku.isse.designspace.core.events.ElementCreate;

/**
 * InstanceType is the types of an Instances. The InstanceType must have a name and specifies a set of
 * properties an instance may have in form of PropertyType. Instances use InstanceType to enforce their well-formedness.
 * @see Instance
 */
public class MetaInstance extends InstanceType {

    public MetaInstance(Workspace workspace) {
        super(workspace);
        elementChanged( new ElementCreate(this.id(), className(), Id.of(1)) );
        workspace.state.load(this);
    }

    public MetaInstance(Workspace workspace, InstanceType instanceType) {
        super(workspace);
        elementChanged(new ElementCreate(this.id(), className(), instanceType.id()));
        workspace.state.load(this);
    }

    public void define(String name, InstanceType superType) {
        new MapProperty(this, ReservedNames.PROPERTY_TYPES, MetaProperty.propertiesPropertyType);
        addPropertyType(ReservedNames.NAME, MetaProperty.namePropertyType);
        addPropertyType(ReservedNames.INSTANCE_OF, MetaProperty.instanceOfPropertyType);
        addPropertyType(ReservedNames.INSTANCES, MetaProperty.instancesPropertyType);
        addPropertyType(ReservedNames.SUPER_TYPES, MetaProperty.superTypesPropertyType);
        addPropertyType(ReservedNames.SUB_TYPES, MetaProperty.subTypesPropertyType);
        addPropertyType(ReservedNames.INSTANTIATION_CLASS, MetaProperty.instantiationClassPropertyType);
        addPropertyType(ReservedNames.OWNERSHIP_PROPERTY, MetaProperty.ownershipPropertyType);
        addPropertyType(ReservedNames.INSTANCETYPE_PROPERTY_METADATA, MetaProperty.instantTypePropertyMetadata);
        addPropertyType(ReservedNames.PROPERTY_TYPES, MetaProperty.propertiesPropertyType);
        addPropertyType(ReservedNames.Authorized_Users, MetaProperty.authorizedUsersType); 


        new SingleProperty(this, ReservedNames.NAME, name, MetaProperty.namePropertyType);
        new SingleProperty(this, ReservedNames.INSTANCE_OF, null, MetaProperty.instanceOfPropertyType);
        new SetProperty(this, ReservedNames.INSTANCES, MetaProperty.instancesPropertyType);
        new SetProperty(this, ReservedNames.SUPER_TYPES, MetaProperty.superTypesPropertyType);
        new SetProperty(this, ReservedNames.SUB_TYPES, MetaProperty.subTypesPropertyType);
        new SetProperty(this, ReservedNames.OWNERSHIP_PROPERTY, MetaProperty.ownershipPropertyType);
        new SetProperty(this, ReservedNames.Authorized_Users, MetaProperty.authorizedUsersType);
        new MapProperty(this, ReservedNames.INSTANCETYPE_PROPERTY_METADATA, MetaProperty.instantTypePropertyMetadata);

        if (superType!=null) getPropertyAsSet(ReservedNames.SUPER_TYPES).add(superType);
    }
    public void defineWithoutPropertyTypes(String name, InstanceType superType) {

        new MapProperty(this, ReservedNames.PROPERTY_TYPES, MetaProperty.propertiesPropertyType);
        new SingleProperty(this, ReservedNames.NAME, name, MetaProperty.namePropertyType);
        new SingleProperty(this, ReservedNames.INSTANCE_OF, null, MetaProperty.instanceOfPropertyType);
        new SetProperty(this, ReservedNames.INSTANCES, MetaProperty.instancesPropertyType);
        new SetProperty(this, ReservedNames.SUPER_TYPES, MetaProperty.superTypesPropertyType);
        new SetProperty(this, ReservedNames.SUB_TYPES, MetaProperty.subTypesPropertyType);

        if (superType!=null) getPropertyAsSet(ReservedNames.SUPER_TYPES).add(superType);
    }
    public Set<PropertyType> getPropertyTypes(boolean includingReserved) {
        return super.getPropertyTypes(includingReserved);
    }
    public PropertyType getPropertyType(String propertyName) {
       return super.getPropertyType(propertyName);
    }
    public PropertyType addPropertyType(String propertyName, PropertyType propertyType) {
        return super.addPropertyType(propertyName, propertyType);
    }

    public PropertyType createOpposablePropertyType(String nameA, Cardinality cardinalityA, MetaInstance instanceTypeB, String nameB, Cardinality cardinalityB) {
        throw new IllegalArgumentException("meta elements cannot be modified");
    }

    public Set<InstanceType> getAllSuperTypes() {
        return null;
    }

    public InstanceType getInstanceType() {
        return null;
    }

    public boolean matchesWorkspace(Workspace workspace) { return true; }

    public SetProperty<Instance> instancesIncludingSubtypes() { return null; }

    public boolean isTypeOf(MetaInstance instanceType) { return true; }
    public boolean isKindOf(MetaInstance instanceType) { return true; }

    public Id id() { return id; }


    private PropertyType getPropertyTypeOfSubType(String propertyName) {
        return null;
    }


    public void delete() { throw new IllegalArgumentException("meta elements cannot be modified"); }
    public void setName(String name) { throw new IllegalArgumentException("meta elements cannot be modified"); }

    public void deletePropertyType(String propertyName) { throw new IllegalArgumentException("meta elements cannot be modified"); }

    public SingleProperty createSingleProperty(String propertyName, Object value, PropertyType propertyType) { throw new IllegalArgumentException("meta elements cannot be modified"); }
    public SetProperty createSetProperty(String propertyName, PropertyType propertyType)  { throw new IllegalArgumentException("meta elements cannot be modified"); }
    public ListProperty createListProperty(String propertyName, PropertyType propertyType) { throw new IllegalArgumentException("meta elements cannot be modified"); }
    public MapProperty createMapProperty(String propertyName, PropertyType propertyType)  { throw new IllegalArgumentException("meta elements cannot be modified"); }
}