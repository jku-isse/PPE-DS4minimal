package at.jku.isse.designspace.core.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.util.Assert;

import at.jku.isse.designspace.core.events.ElementCreate;

/**
 * InstanceType are the types of an Instances; and the types of types (meta). The InstanceType must have a name and
 * specifies a set of properties. It may also declare special PropertyTypes properties which define properties for
 * their instances (e.g., the Folder type declares parentFolder as a property type; all instances of Folder will thus
 * automatically get a parentFolder property). @see Instance
 */
public class InstanceType extends Element<InstanceType> {

    //************************************************************************
    //****** Constructors
    //************************************************************************

    public InstanceType(Workspace workspace, InstanceType metaInstanceType, String name, InstanceType ... superTypes)  {
        super(workspace, metaInstanceType, name);

        if (superTypes!=null && superTypes.length>0) {
            List<InstanceType> superTypeList = Arrays.asList(superTypes).stream().filter(Objects::nonNull).collect(Collectors.toList());
            // TODO: prevent cyclic definition is not necessary, since it is currently not possible to define a cycle
//            for(InstanceType superType : superTypeList) {
//                if(superType == this || superType.allSuperTypes().contains(this)) {
//                    PublicWorkspace.logger.error("Cyclic SuperType definition encountered, type: <{}>, superType: <{}>, allSuperTypes: <{}>", name, superType, allSuperTypes());
//                    throw new IllegalArgumentException("Cyclic SuperType definition encountered");
//                }
//            }
            getPropertyAsSet(ReservedNames.SUPER_TYPES).addAll(superTypeList);
            for (InstanceType superType : superTypeList) superType.getPropertyAsSet(ReservedNames.SUB_TYPES).add(this);
        }
    }
    //constructor for the construction of meta elements (for which there are no instance types)
    public InstanceType(Workspace workspace) {
        super(workspace);
    }
    //constructor for reconstructing elements from operations (e.g., replay repository)
    protected InstanceType(Workspace workspace, ElementCreate elementCreate) {
        super(workspace, elementCreate);
    }


    /**
     * deletes this type and all its instances. Since correct versioning does not allow for the actual deletion of
     * elements, they are marked deleted instead
     */
    @Override
    public void delete() {
        for (Instance instance : new HashSet<>(instances())) {
            instance.delete();
        }
        super.delete();
        for(PropertyType propertyType: getPropertyTypes(false,false)) {
            if (!propertyType.isDeleted)
                propertyType.delete();
        }
    }


    //************************************************************************
    //****** Properties made into Methods
    //************************************************************************


    /**
     * sets the meta instance type of this type
     */
    public void setInstanceType(InstanceType instanceType) {
        Assert.notNull(instanceType, "InstanceType must not be null");
        getProperty(ReservedNames.INSTANCE_OF).set(instanceType);
    }

    /**
     * @return the super types of this type. The root super type is ELEMENT
     */
    public Set<InstanceType> superTypes() {
        var property = getPropertyAsSet(ReservedNames.SUPER_TYPES);
        if(property == null){
            return new HashSet<>();
        }
        return (Set<InstanceType>)property.getValue();
    }

    /**
     * @return the sub types of this type
     */
    public Set<InstanceType> subTypes() {
        var property = getPropertyAsSet(ReservedNames.SUB_TYPES);
        if(property == null){
            return new HashSet<>();
        }
        return (Set<InstanceType>)property.getValue();
    }

    /**
     * @return the super types of this type and the super types of all its super types (recursively)
     */
    public Set<InstanceType> getAllSuperTypes() {
        Set<InstanceType> allSuperTypes = new HashSet<>();
        Set<InstanceType> directSuperTypes = superTypes();
        if(directSuperTypes!=null) {
            allSuperTypes.addAll(directSuperTypes);
            for(InstanceType superType : directSuperTypes) {
                var superTypesOfSuperType = superType.getAllSuperTypes();
                if (superTypesOfSuperType == null) {
                    continue;
                }
                allSuperTypes.addAll(superTypesOfSuperType);
            }
        }
        return allSuperTypes;
    }
    /**
     * @return the sub types of this type and the sub types of all its sub types (recursively)
     */
    public Set<InstanceType> getAllSubTypes() {
        Set<InstanceType> allSubTypes = new HashSet<>();
        Set<InstanceType> directSubTypes = subTypes();
        if(directSubTypes!=null) {
            allSubTypes.addAll(directSubTypes);
            for(InstanceType subType : directSubTypes) {
                var subTypesOfSubType = subType.getAllSubTypes();
                if (subTypesOfSubType == null) {
                    continue;
                }
                allSubTypes.addAll(subTypesOfSubType);
            }
        }
        return allSubTypes;
    }

    /**
     * @return instances of this type
     */
    public SetProperty<Instance> instances() {
        return getPropertyAsSet(ReservedNames.INSTANCES);
    }
    /**
     * @return instances of this type and instances of all its sub types
     * //TODO: Bug, calling from a BaseType adds all SubType Instances to BaseType::instances() - fixed in Java-SDK
     */
    public SetProperty<Instance> instancesIncludingSubtypes() {
        SetProperty<Instance> instances = instances();
        for (InstanceType subType : subTypes()) {
            instances.addAll( subType.instancesIncludingSubtypes() );
        }
        return instances;
    }

    // TODO: This needs a code review - changed from SetProperty<Instance> to Stream<Instance>
    /**
     * @return instances of this type and instances of all its sub types
     */
    public Stream<Instance> instancesIncludingThoseOfSubtypes() {
        Stream<Instance> instances = instances().stream();
        for (InstanceType subType : subTypes()) {
            instances = Stream.concat(instances, subType.instancesIncludingThoseOfSubtypes() );
        }
        return instances;
    }

    /**
     * @return true if this type equals the given type
     */
    public boolean isTypeOf(InstanceType instanceType) {
        if (instanceType == null) return false;
        return (instanceType.id().equals(id()));
    }
    /**
     * @return true if this type equals the given type or is a subtype thereof (e.g., Folder is a kind of Instance)
     */
    public boolean isKindOf(InstanceType instanceType) {
        if (instanceType == null) return false;
        if (instanceType.id().equals(id())) return true;

        var parentInstanceTypes = superTypes();
        for(InstanceType parentInstanceType : parentInstanceTypes) {
            if(parentInstanceType.isKindOf(instanceType)) {
                return true;
            }
        }

            return false;
    }

    /**
     * @return qualified name composed of the hierarchy of folder names and this type name
     */
    public String getQualifiedName() {
        Folder folder = (Folder) getPropertyAsValueOrException(ReservedNames.CONTAINED_FOLDER, IllegalStateException::new);
        if(folder != null) {
            return folder.getQualifiedName() + ReservedNames.QUALIFIED_NAME_SEPARATOR + name();
        } else if(this.isDeleted){
            return "DELETED: [ " +name() +" ]";
        }
        return name();

    }

    //************************************************************************
    //****** Property Type
    //************************************************************************

    /**
     * Retrieves the property type (see @PropertyType) of this type with the given name. Instance of this type are allowed to define
     * properties only of there is a corresponding property type.
     * @param propertyName The name of the property whose type we  like to retrieve.
     */
    public PropertyType getPropertyType(String propertyName) {
        Assert.notNull(propertyName, "Property name must not be null");

        MapProperty<PropertyType> properties = getPropertyAsMap(ReservedNames.PROPERTY_TYPES);
        Assert.notNull(properties, ReservedNames.PROPERTY_TYPES + " must not be null");
        if (properties.containsKey(propertyName)) {
            return properties.get(propertyName);
        }
        // look for matching propertyType in all superTypes
        for (InstanceType superType : superTypes()) {
            PropertyType superPropertyType = superType.getPropertyType(propertyName);
            if (superPropertyType != null) return superPropertyType;
        }

        return null;
    }

    /**
     * looks for an existing property type in all subtypes, recursively
     * @param propertyName
     * @return
     */
    private PropertyType propertyTypeOfSubType(String propertyName) {
        Assert.notNull(propertyName, "Property name must not be null");

        MapProperty properties = getPropertyAsMap(ReservedNames.PROPERTY_TYPES);
        Assert.notNull(propertyName, ReservedNames.PROPERTY_TYPES + " must not be null");
        if (properties.containsKey(propertyName)) return (PropertyType) properties.get(propertyName);

        for(InstanceType subType : subTypes()) {
            PropertyType subPropertyType = subType.propertyTypeOfSubType(propertyName);
            if(subPropertyType != null) return subPropertyType;
        }

        return null;
    }

    // returns propertyType of this type and all super types
    public Set<PropertyType> getPropertyTypes(boolean includingReserved) {
        return getPropertyTypes(includingReserved, true);
    }

    public Set<PropertyType> getPropertyTypes(boolean includingReserved, boolean includingSuperTypes) {
        Set<PropertyType> propertyTypes = new HashSet<>();

        if (includingSuperTypes) {
            // property types of this instanceType and all super types
            var superTypes = superTypes();
            if (superTypes != null && !superTypes.isEmpty()) {
                for (InstanceType superType : superTypes) {
                    if (superType != null) {
                        Set<PropertyType> superPropertyTypes = superType.getPropertyTypes(includingReserved);
                        if (superPropertyTypes != null && !superPropertyTypes.isEmpty()) {
                            propertyTypes.addAll(superPropertyTypes);
                        }
                    }
                }
            }
        } // else -> property types of this InstanceType only

        MapProperty<PropertyType> properties = getPropertyAsMap(ReservedNames.PROPERTY_TYPES);
        Assert.notNull(properties, ReservedNames.PROPERTY_TYPES + " should not be null");
        for (PropertyType propertyType : properties.values()) {
            if (propertyType.name().lastIndexOf("@") != 0 || includingReserved) {
                propertyTypes.add(propertyType);
            }
        }
        return propertyTypes;
    }

    /**
     * create a property type for this type with the given name, cardinality, and reference type
     */
    public PropertyType createPropertyType(String name, Cardinality cardinality, InstanceType referencedInstanceType) {
        return createPropertyType(name, cardinality, referencedInstanceType, false);
    }
    /**
     * create a property type for this type with the given name, cardinality, and reference type
     * the property will only be visible, if it is not optional or if it is optional and set
     */
    public PropertyType createPropertyType(String name, Cardinality cardinality, InstanceType referencedInstanceType, boolean isOptional) {
        return addPropertyType(name, new PropertyType(workspace, this, name, cardinality, referencedInstanceType, (String)null, isOptional));
    }
    /**
     * create a property type for this type with the given name, cardinality, reference type, native type, and derived rule (the latter two are optional)
     */
    public PropertyType createPropertyType(String name, Cardinality cardinality, InstanceType referencedInstanceType, String nativeType) {
        return createPropertyType(name, cardinality, referencedInstanceType, nativeType, false);
    }
    public PropertyType createPropertyType(String name, Cardinality cardinality, InstanceType referencedInstanceType, String nativeType, boolean isOptional) {
        return addPropertyType(name, new PropertyType(workspace, this, name, cardinality, referencedInstanceType, nativeType, isOptional));
    }

    /***
     * Create a contained PropertyType for this type where a reference to the contained InstanceType is provided.
     * @param name the given name of the Property on this side
     * @param cardinality the cardinality for this property
     * @param referencedInstanceType the instance type of the contained instances
     * @param nameB the containment property that will be created on the given instance
     * @return the created PropertyType
     */
    public PropertyType createContainmentPropertyType(String name, Cardinality cardinality, InstanceType referencedInstanceType, String nameB){
        PropertyType opposablePropertyType = referencedInstanceType.addPropertyType(nameB, new PropertyType(workspace, referencedInstanceType, nameB, Cardinality.SINGLE, this, (String)null, false,false,true));
        return addPropertyType(name, new PropertyType(workspace, this, name, cardinality, referencedInstanceType,opposablePropertyType, false,true,false));
    }

    /**
     * create an opposable property type for this type where a reference to another property type is provided. These
     * two types are then opposable (e.g., changing the super type also changes the sub type)
     */
    public PropertyType createOpposablePropertyType(String nameA, Cardinality cardinalityA, InstanceType instanceTypeB, String nameB, Cardinality cardinalityB) {
        return createOpposablePropertyType(nameA, cardinalityA, false, instanceTypeB, nameB, cardinalityB, false);
    }
    public PropertyType createOpposablePropertyType(String nameA, Cardinality cardinalityA, boolean isOptionalA, InstanceType instanceTypeB, String nameB, Cardinality cardinalityB, boolean isOptionalB) {
        PropertyType opposablePropertyType = instanceTypeB.addPropertyType(nameB, new PropertyType(workspace, instanceTypeB, nameB, cardinalityB, this, (String)null, isOptionalB));
        return addPropertyType(nameA, new PropertyType(workspace, this, nameA, cardinalityA, instanceTypeB, opposablePropertyType, isOptionalA));
    }
    public PropertyType createOpposablePropertyType(String nameA, Cardinality cardinalityA, boolean isOptionalA, InstanceType instanceTypeB, String nameB, Cardinality cardinalityB) {
        return createOpposablePropertyType(nameA, cardinalityA, isOptionalA, instanceTypeB, nameB, cardinalityB, false);
    }
    public PropertyType createOpposablePropertyType(String nameA, Cardinality cardinalityA, InstanceType instanceTypeB, String nameB, Cardinality cardinalityB, boolean isOptionalB) {
        return createOpposablePropertyType(nameA, cardinalityA, false, instanceTypeB, nameB, cardinalityB, isOptionalB);
    }

    protected PropertyType addPropertyType(String propertyName, PropertyType propertyType) {
        Assert.notNull(propertyType, "Property must not be null");
        // look for an existing propertyType in the current type and in all superTypes and make sure it does not exist
        Assert.notNull(getPropertyAsMap(ReservedNames.PROPERTY_TYPES),"propertyTypes container must exist");
        Assert.isNull(getPropertyType(propertyName), "The property <" + propertyName + "> was already declared in a (super)-InstanceType, therefore it cannot be declared again in type " + name());
        // look for matching propertyType in all subTypes and make sure it does not exist
        getPropertyAsMap(ReservedNames.PROPERTY_TYPES).put(propertyName,propertyType);
        return propertyType;
    }

    /**
     * Removes any property defined by the given name from this instanceType.
     * If no such property exists, does nothing.
     * @param propertyName The name of the property you wish to undefine
     * @param deleteProperties if set properties of the given property type of all the instances  are also deleted
     */
    public void deletePropertyType(String propertyName, boolean deleteProperties) {
        Assert.notNull(propertyName, "Property name must not be null");
        PropertyType property = getPropertyType(propertyName);
        if (property != null) {
            property.delete();
            if (deleteProperties) {
                // delete all the defined property with the given propertyName for all instances of this instanceType
                for (Instance instance : new HashSet<>(instances())) {
                    instance.getProperty(propertyName).delete();
                }
            }
        }
    }

    //************************************************************************
    //****** Helper
    //************************************************************************

    public String className() { return ReservedNames.INSTANCE_TYPE_CLASS_NAME; }
    public Instance instantiate(String name) { return Instance.create(workspace, this, name); }

    public String toString() { return "<InstanceType-"+name()+"{"+id+"}>"; }
}