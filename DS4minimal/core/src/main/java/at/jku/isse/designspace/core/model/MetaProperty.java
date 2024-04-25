package at.jku.isse.designspace.core.model;

import static at.jku.isse.designspace.core.model.ReservedNames.Authorized_Users;
import static at.jku.isse.designspace.core.model.ReservedNames.CARDINALITY;
import static at.jku.isse.designspace.core.model.ReservedNames.DERIVED_RULE;
import static at.jku.isse.designspace.core.model.ReservedNames.INSTANCES;
import static at.jku.isse.designspace.core.model.ReservedNames.INSTANCETYPE_PROPERTY_METADATA;
import static at.jku.isse.designspace.core.model.ReservedNames.INSTANCE_OF;
import static at.jku.isse.designspace.core.model.ReservedNames.INSTANTIATION_CLASS;
import static at.jku.isse.designspace.core.model.ReservedNames.ISOPTIONAL;
import static at.jku.isse.designspace.core.model.ReservedNames.IS_CONTAINED;
import static at.jku.isse.designspace.core.model.ReservedNames.IS_CONTAINER;
import static at.jku.isse.designspace.core.model.ReservedNames.NAME;
import static at.jku.isse.designspace.core.model.ReservedNames.NATIVE_TYPE;
import static at.jku.isse.designspace.core.model.ReservedNames.OPPOSED_PROPERTY_TYPE;
import static at.jku.isse.designspace.core.model.ReservedNames.OWNERSHIP_PROPERTY;
import static at.jku.isse.designspace.core.model.ReservedNames.OWNER_INSTANCE_TYPE;
import static at.jku.isse.designspace.core.model.ReservedNames.PROPERTY_OWNER;
import static at.jku.isse.designspace.core.model.ReservedNames.PROPERTY_TYPES;
import static at.jku.isse.designspace.core.model.ReservedNames.REFERENCED_INSTANCE_TYPE;
import static at.jku.isse.designspace.core.model.ReservedNames.SUB_TYPES;
import static at.jku.isse.designspace.core.model.ReservedNames.SUPER_TYPES;

import at.jku.isse.designspace.core.events.ElementCreate;
import at.jku.isse.designspace.core.events.Operation;

/**
 * InstanceType is the types of an Instances. The InstanceType must have a name and specifies a set of
 * properties an instance may have in form of PropertyType. Instances use InstanceType to enforce their well-formedness.
 * @see Instance
 */
public class MetaProperty extends InstanceType {


    static public PropertyType propertyTypeDeclarationPropertyType=null;

    static public PropertyType namePropertyType=null;
    static public PropertyType instanceOfPropertyType=null;
    static public PropertyType instancesPropertyType=null;
    static public PropertyType superTypesPropertyType=null;
    static public PropertyType subTypesPropertyType=null;
    static public PropertyType instantiationClassPropertyType=null;
    static public PropertyType ownershipPropertyType =null;
    static public PropertyType instantTypePropertyMetadata = null; // allows to define arbitrary properties/info at the InstanceType level (i.e., properties/info that is different for each InstanceType element. 

    static public PropertyType ownerInstancePropertyType=null;
    static public PropertyType cardinalityPropertyType=null;
    static public PropertyType isOptionalPropertyType=null;
    static public PropertyType referencedInstanceTypePropertyType=null;
    static public PropertyType nativeTypePropertyType=null;
    static public PropertyType derivedRulePropertyType=null;
    static public PropertyType opposedPropertyTypePropertyType=null;
    static public PropertyType isContainerPropertyType = null;
    static public PropertyType isContainedPropertyType = null;
    static public PropertyType authorizedUsersType=null;

    static public PropertyType propertiesPropertyType = null;
    static public PropertyType propertyOwner = null;


    public MetaProperty(Workspace workspace) {
        super(workspace);
        elementChanged(new ElementCreate(this.id(), className(), Id.of(1)));
        workspace.state.load(this);
    }

    public void define(String name) {
        propertyTypeDeclarationPropertyType = new MetaPropertyType(workspace, "PropertyTypeDeclaration", Cardinality.SINGLE, null);

        namePropertyType = new MetaPropertyType(workspace, NAME, Cardinality.SINGLE, workspace.STRING);
        instanceOfPropertyType = new MetaPropertyType(workspace, INSTANCE_OF, Cardinality.SINGLE, workspace.META_INSTANCE_TYPE);
        instancesPropertyType = new MetaPropertyType(workspace, INSTANCES, Cardinality.SET, workspace.ELEMENT);
        superTypesPropertyType = new MetaPropertyType(workspace, SUPER_TYPES, Cardinality.SET, workspace.ELEMENT);
        subTypesPropertyType = new MetaPropertyType(workspace, SUB_TYPES, Cardinality.SET, workspace.ELEMENT);
        instantiationClassPropertyType = new MetaPropertyType(workspace, INSTANTIATION_CLASS, Cardinality.SINGLE, workspace.STRING);
        ownershipPropertyType = new MetaPropertyType(workspace, OWNERSHIP_PROPERTY, Cardinality.SET, workspace.STRING);
        authorizedUsersType = new MetaPropertyType(workspace, OWNERSHIP_PROPERTY, Cardinality.SET, workspace.STRING);
        instantTypePropertyMetadata = new MetaPropertyType(workspace, INSTANCETYPE_PROPERTY_METADATA, Cardinality.MAP, workspace.STRING);

        ownerInstancePropertyType = new MetaPropertyType(workspace, OWNER_INSTANCE_TYPE, Cardinality.SINGLE, workspace.META_INSTANCE_TYPE);
        cardinalityPropertyType = new MetaPropertyType(workspace, CARDINALITY, Cardinality.SINGLE, workspace.STRING);
        isOptionalPropertyType = new MetaPropertyType(workspace, ISOPTIONAL, Cardinality.SINGLE, workspace.BOOLEAN);
        referencedInstanceTypePropertyType = new MetaPropertyType(workspace, REFERENCED_INSTANCE_TYPE, Cardinality.SINGLE, workspace.META_INSTANCE_TYPE);
        nativeTypePropertyType = new MetaPropertyType(workspace, NATIVE_TYPE, Cardinality.SINGLE, workspace.STRING);
        derivedRulePropertyType = new MetaPropertyType(workspace, DERIVED_RULE, Cardinality.SINGLE, workspace.STRING);
        opposedPropertyTypePropertyType = new MetaPropertyType(workspace, OPPOSED_PROPERTY_TYPE, Cardinality.SINGLE, workspace.META_PROPERTY_TYPE);

        isContainerPropertyType = new MetaPropertyType(workspace, IS_CONTAINER, Cardinality.SINGLE, workspace.BOOLEAN);
        isContainedPropertyType = new MetaPropertyType(workspace, IS_CONTAINED, Cardinality.SINGLE, workspace.BOOLEAN);
        // containment for the propertyTypes
        propertyOwner = new MetaPropertyType(workspace,PROPERTY_OWNER, Cardinality.SINGLE,workspace.META_INSTANCE_TYPE,null,false,true);
        propertiesPropertyType =  new MetaPropertyType(workspace, PROPERTY_TYPES, Cardinality.MAP, workspace.META_PROPERTY_TYPE,propertyOwner,true,false);


        new MapProperty(this, PROPERTY_TYPES, propertiesPropertyType);
        addPropertyType(PROPERTY_OWNER,propertyOwner);
        addPropertyType(NAME, namePropertyType);
        addPropertyType(OWNERSHIP_PROPERTY, ownershipPropertyType);
        addPropertyType(INSTANCE_OF, instanceOfPropertyType);
        addPropertyType(OWNER_INSTANCE_TYPE, ownerInstancePropertyType);
        addPropertyType(Authorized_Users, authorizedUsersType);
        addPropertyType(CARDINALITY, cardinalityPropertyType);
        addPropertyType(ISOPTIONAL, isOptionalPropertyType);
        addPropertyType(REFERENCED_INSTANCE_TYPE, referencedInstanceTypePropertyType);
        addPropertyType(NATIVE_TYPE, nativeTypePropertyType);
        addPropertyType(DERIVED_RULE, derivedRulePropertyType);
        addPropertyType(OPPOSED_PROPERTY_TYPE, opposedPropertyTypePropertyType);       
        addPropertyType(INSTANCETYPE_PROPERTY_METADATA, instantTypePropertyMetadata);
        addPropertyType(IS_CONTAINER, isContainerPropertyType);
        addPropertyType(IS_CONTAINED, isContainedPropertyType);


        new SingleProperty(this, NAME, name, namePropertyType);
        new SingleProperty(this, INSTANCE_OF, null, instanceOfPropertyType);
        new SetProperty(this, INSTANCES, instancesPropertyType);
        new SetProperty(this, SUPER_TYPES, superTypesPropertyType);
        new SetProperty(this, SUB_TYPES, subTypesPropertyType);
        new SetProperty(this, OWNERSHIP_PROPERTY, ownershipPropertyType);
        new SetProperty(this, Authorized_Users, authorizedUsersType);
        new MapProperty(this, INSTANCETYPE_PROPERTY_METADATA, instantTypePropertyMetadata);
    }


    public boolean matchesWorkspace(Workspace workspace) { return true; }

    public SetProperty<Instance> instancesIncludingSubtypes() { return null; }

    public String toString() { return "<MetaPropertyInstanceType{"+id+"}>"; }

    public boolean isDeleted() { return false; }
    public Folder getFolder() { return null; }

    public void delete() { throw new IllegalArgumentException("meta elements cannot be modified"); }
    public void setInstanceType(InstanceType instanceType) { throw new IllegalArgumentException("meta elements cannot be modified"); }
    public void setName(String name) { throw new IllegalArgumentException("meta elements cannot be modified"); }
    public void reconstruct(Operation operation) { throw new IllegalArgumentException("meta elements cannot be modified"); }

    public PropertyType createPropertyType(String name, Cardinality cardinality, MetaProperty referencedInstanceType) { throw new IllegalArgumentException("meta elements cannot be modified"); }
    public PropertyType createPropertyType(String name, Cardinality cardinality, MetaProperty referencedInstanceType, String nativeType, String derivedRule) { throw new IllegalArgumentException("meta elements cannot be modified"); }
    public PropertyType createOpposablePropertyType(String nameA, Cardinality cardinalityA, MetaProperty instanceTypeB, String nameB, Cardinality cardinalityB)  { throw new IllegalArgumentException("meta elements cannot be modified"); }
    public void deletePropertyType(String propertyName) { throw new IllegalArgumentException("meta elements cannot be modified"); }
    public String getQualifiedName() { return null; }

    public SingleProperty createSingleProperty(String propertyName, Object value, PropertyType propertyType) { throw new IllegalArgumentException("meta elements cannot be modified"); }
    public SetProperty createSetProperty(String propertyName, PropertyType propertyType)  { throw new IllegalArgumentException("meta elements cannot be modified"); }
    public ListProperty createListProperty(String propertyName, PropertyType propertyType) { throw new IllegalArgumentException("meta elements cannot be modified"); }
    public MapProperty createMapProperty(String propertyName, PropertyType propertyType)  { throw new IllegalArgumentException("meta elements cannot be modified"); }
}