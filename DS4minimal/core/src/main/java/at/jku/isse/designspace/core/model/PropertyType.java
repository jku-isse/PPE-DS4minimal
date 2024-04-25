package at.jku.isse.designspace.core.model;

import java.util.Date;

import org.springframework.util.Assert;

import at.jku.isse.designspace.core.events.ElementCreate;
import at.jku.isse.designspace.core.service.WorkspaceService;

/**
 * A PropertyType defines a Property - its referenced type and its cardinality. When added to an instanceType
 * then its Instances will have a Property of this type.
 * {@link InstanceType} definitions. It is an essential building block of types.
 */
public class PropertyType<T extends Element<T>> extends Element<PropertyType> {

    protected Evaluator evaluator = null;

    public PropertyType(Workspace workspace, InstanceType ownerInstanceType, String name, Cardinality cardinality, InstanceType referencedInstanceType, PropertyType opposedPropertyType, boolean isOptional, boolean isContainer,boolean isContained) {
        this(workspace, ownerInstanceType, name, cardinality, referencedInstanceType, opposedPropertyType, isOptional);
        if (isContainer) {
            getPropertyAsSingle(ReservedNames.IS_CONTAINER).set(true);
        }
        if (isContained) {
            getPropertyAsSingle(ReservedNames.IS_CONTAINED).set(true);
        }

    }

    public PropertyType(Workspace workspace, InstanceType ownerInstanceType, String name, Cardinality cardinality, InstanceType referencedInstanceType, String nativeType, boolean isOptional, boolean isContainer,boolean isContained) {
        this(workspace, ownerInstanceType, name, cardinality, referencedInstanceType, nativeType, isOptional);
        if (isContainer) {
            getPropertyAsSingle(ReservedNames.IS_CONTAINER).set(true);
        }
        if (isContained) {
            getPropertyAsSingle(ReservedNames.IS_CONTAINED).set(true);
        }
    }

    public PropertyType(Workspace workspace, InstanceType ownerInstanceType, String name, Cardinality cardinality, InstanceType referencedInstanceType, String nativeType) {
        super(workspace, workspace.its(WorkspaceService.PUBLIC_WORKSPACE.META_PROPERTY_TYPE), name);

        getPropertyAsSingle(ReservedNames.OWNER_INSTANCE_TYPE).set(ownerInstanceType);
        getPropertyAsSingle(ReservedNames.CARDINALITY).set(cardinality.toString());
        getPropertyAsSingle(ReservedNames.REFERENCED_INSTANCE_TYPE).set(referencedInstanceType);
        getPropertyAsSingle(ReservedNames.NATIVE_TYPE).set(nativeType);
    }
    public PropertyType(Workspace workspace, InstanceType ownerInstanceType, String name, Cardinality cardinality, InstanceType referencedInstanceType, String nativeType, boolean isOptional) {
        this(workspace, ownerInstanceType, name, cardinality, referencedInstanceType, nativeType);
        getPropertyAsSingle(ReservedNames.ISOPTIONAL).set(isOptional);
    }

    public PropertyType(Workspace workspace, InstanceType instanceType, String name, Cardinality cardinality, InstanceType referencedInstanceType, PropertyType opposedPropertyType) {
        this(workspace, instanceType, name, cardinality, referencedInstanceType, (String)null);

        Assert.isTrue(!((opposedPropertyType.cardinality()==Cardinality.LIST) && (cardinality == Cardinality.LIST)), "Opposing lists are not supported");
        Assert.isTrue(!((opposedPropertyType.cardinality()==Cardinality.MAP) && (cardinality == Cardinality.MAP)), "Opposing maps are not supported");
        Assert.isTrue(!((opposedPropertyType.cardinality()==Cardinality.LIST) && (cardinality == Cardinality.MAP)), "Opposing list and map are not supported");
        Assert.isTrue(!((opposedPropertyType.cardinality()==Cardinality.MAP) && (cardinality == Cardinality.LIST)), "Opposing list and map are not supported");

        getPropertyAsSingle(ReservedNames.OPPOSED_PROPERTY_TYPE).set(opposedPropertyType);
        opposedPropertyType.getPropertyAsSingle(ReservedNames.OPPOSED_PROPERTY_TYPE).set(this);
    }
    public PropertyType(Workspace workspace, InstanceType instanceType, String name, Cardinality cardinality, InstanceType referencedInstanceType, PropertyType opposedPropertyType, boolean isOptional) {
        this(workspace, instanceType, name, cardinality, referencedInstanceType, opposedPropertyType);
        getPropertyAsSingle(ReservedNames.ISOPTIONAL).set(isOptional);
    }

    public PropertyType(Workspace workspace) {
        super(workspace);
    }
    protected PropertyType(Workspace workspace, ElementCreate elementCreate) { super(workspace, elementCreate); }

    public Cardinality cardinality() {
        return Cardinality.valueOf((String) getProperty(ReservedNames.CARDINALITY).get());
    }
    public boolean isOptional() {
        return (boolean) getProperty(ReservedNames.ISOPTIONAL).get();
    }

    public InstanceType referencedInstanceType() {return (InstanceType) getPropertyAsSingle(ReservedNames.REFERENCED_INSTANCE_TYPE).get(); }
    public String nativeType() {
        return (String) getProperty(ReservedNames.NATIVE_TYPE).get();
    }

    public PropertyType opposedPropertyType() {
        return (PropertyType) getPropertyAsSingle(ReservedNames.OPPOSED_PROPERTY_TYPE).get();
    }

    public SetProperty<InstanceType> superTypes() {
        Property superTypes = getProperty(ReservedNames.SUPER_TYPES);
        if (superTypes==null) return null; //needed for metatype
        return (SetProperty)superTypes;
    }
    public void setInstanceType(InstanceType instanceType) {
        Assert.notNull(instanceType, "InstanceType must not be null");
        getProperty(ReservedNames.INSTANCE_OF).set(instanceType);
    }

    /**
     * Retrieves the PropertyType of an InstanceType with the given name. The Instance of the InstanceType
     * must have a corresponding property.
     * @param propertyName The name of the property whose type we  like to retrieve.
     */
    public PropertyType subPropertyType(String propertyName) {
        Assert.notNull(propertyName, "Property name must not be null");

        MapProperty properties = getPropertyAsMap(ReservedNames.PROPERTY_TYPES);
        Assert.notNull(properties, ReservedNames.PROPERTY_TYPES + " must not be null");
        if (properties.containsKey(propertyName))
            return (PropertyType) properties.get(propertyName);

        // look for matching propertyType in all superTypes
        for (InstanceType superType : superTypes()) {
            PropertyType superPropertyType = superType.getPropertyType(propertyName);
            if (superPropertyType != null) return superPropertyType;
        }
        return null;
    }

    public boolean isPrimitive() {
        InstanceType referencedInstanceType = referencedInstanceType();
        if (referencedInstanceType==null) return true;
        return (referencedInstanceType.equals(workspace.STRING) || referencedInstanceType.equals(workspace.BOOLEAN) ||
                referencedInstanceType.equals(workspace.INTEGER) || referencedInstanceType.equals(workspace.REAL) ||
                referencedInstanceType.equals(workspace.DATE));
    }

    public boolean isHidden() {
        return name().startsWith("@");
    }

    public boolean isContainer() {
        Object value = getPropertyAsValueOrNull(ReservedNames.IS_CONTAINER);
        return value != null && value.equals(true);
    }

    public boolean isContained() {
        Object value = getPropertyAsValueOrNull(ReservedNames.IS_CONTAINED);
        return value != null && value.equals(true);
    }

    public boolean isAssignable(Object value) {
        InstanceType referencedInstanceType = referencedInstanceType();
        if (referencedInstanceType==null || value==null) return true;

        if (value instanceof String)
            return (workspace.STRING.isKindOf(referencedInstanceType));
        else if (value instanceof Boolean)
            return (workspace.BOOLEAN.isKindOf(referencedInstanceType));
        else if (value instanceof Long || value instanceof Integer)
            return (workspace.INTEGER.isKindOf(referencedInstanceType));
        else if (value instanceof Double)
            return (workspace.REAL.isKindOf(referencedInstanceType));
        else if (value instanceof Date)
            return (workspace.DATE.isKindOf(referencedInstanceType));
        if (value instanceof Element) {
            InstanceType instanceType = ((Element) value).getInstanceType();
            if (instanceType==null) return true;
            return instanceType.isKindOf(referencedInstanceType);
        }
        return false;
    }

    public String className() { return ReservedNames.PROPERTY_TYPE_CLASS_NAME; }

    public String toString() { return "<PropertyType-"+name()+"{"+id+"}>"; }
}