package at.jku.isse.designspace.core.model;

import java.util.HashMap;

import org.springframework.util.Assert;

import at.jku.isse.designspace.core.events.ElementUpdate;
import at.jku.isse.designspace.core.events.Operation;
import at.jku.isse.designspace.core.events.PropertyCreate;
import at.jku.isse.designspace.core.events.PropertyDelete;

/**
 * Represents the value of an arbitrary, single element property (reference or primitive)
 */
public class Property<T> {
    private boolean isDummy = false;
    public boolean isDummy() {
        return isDummy;
    }
    public HashMap<Long, Operation> executedOperations = new HashMap<Long, Operation>();
    public Element element;
    public String name;
    private T value;
    public boolean isDeleted=false;
    public boolean isSubproperty=false;

    public boolean isContainer = false;

    public void setValue(T value){
        this.value = value;
    }
    public T getValue(){
        return value;
    }

    public PropertyType propertyType = null;

    public Property(Element element, String name, T value, PropertyType propertyType) {
        this(element, name, propertyType);
        set(value);
    }
    public Property(Element element, String name, T value, PropertyType propertyType, boolean cacheOnly) {
        this(element, name, propertyType, cacheOnly);
        set(value);
    }

    public Property(Element element, String name, PropertyType propertyType, boolean cacheOnly) {
        Assert.isTrue(name != null, "property name must not be null");
        Assert.isTrue(!(value instanceof Property), "property value should not be a property");
        this.element = element;
        this.name = name;
        this.propertyType = propertyType;
        this.isContainer = propertyType != null && propertyType.isContainer();
        if (!cacheOnly) {
            var operation = new PropertyCreate(element.id(), name, propertyType.cardinality(), propertyType.id());
            propertyChanged(operation);
        }
        element.workspace.state.load(this);
    }

    public Property(Element element, String name, PropertyType propertyType) {
        this(element, name, propertyType, false);
    }

    public Property(Element element, PropertyCreate operation) {
        this.element = element;
        this.name = operation.name();
        if ((Id) operation.propertyTypeId()!=null) this.propertyType = (PropertyType) element.workspace.findElement( (Id) operation.propertyTypeId() );
    }

    public Property(String name, T value){
        this.name = name;
        this.value = value;
        this.isDummy = true;
    }

    public String getName(){
        return name;
    }

    protected Property(Element element) {
        this.element = element;
    }

    public PropertyType propertyType() {
        return propertyType;
    }

    public String id() {
        return element.id().toString()+"."+name;
    }

    /**
     * Delete a property. This does not actually delete it as we need to preserve the history but marks it as
     * deleted
     */
    public void delete() {
        isDeleted = true;
        propertyChanged( new PropertyDelete(element.id(), name, (PropertyCreate)this.element.state.getPropertyState(name,false).creationNode.data) );
    }
    public boolean isDeleted() { return isDeleted; }

    /**
     * event triggered by a property change (operation)
     */
    public void propertyChanged(Operation operation) {
        //to save this operation that it is already executed here,
        //otherwise it will be executed two times, on creation and on receiving it from the server
        //only a problem for list properties
        this.executedOperations.put(operation.id(), operation);
        this.element.elementChanged(operation);
    }

    public Property subProperty(String subPropertyName) {
        return element.getProperty(name+"/"+subPropertyName);
    }
    public boolean hasSubProperty(String subPropertyName) {
        return element.hasProperty(name+"/"+subPropertyName);
    }

    public SingleProperty createSingleSubProperty(String propertyName, Object value, PropertyType propertyType) {
        Assert.isTrue(propertyType!=null, "property needs a property type");
        Assert.isTrue(propertyName.contains("/"), "Subproperty name must contain a slash '/' to identify property/subproperty");
        SingleProperty property = new SingleProperty(element, propertyName, value, propertyType);
        property.isSubproperty=true;
        //element.addProperty(property);
        return property;
    }
    public SetProperty createSetSubProperty(String propertyName, PropertyType propertyType) {
        Assert.isTrue(propertyType!=null, "property needs a property type");
        SetProperty property = new SetProperty(element, propertyName, propertyType);
        //element.addProperty(property);
        return property;
    }
    public ListProperty createListSubProperty(String propertyName, PropertyType propertyType) {
        Assert.isTrue(propertyType!=null, "property needs a property type");
        ListProperty property = new ListProperty(element, propertyName, propertyType);
        //element.addProperty(property);
        return property;
    }
    public MapProperty createMapSubProperty(String propertyName, PropertyType propertyType) {
        Assert.isTrue(propertyType!=null, "property needs a property type");
        MapProperty property = new MapProperty(element, propertyName, propertyType);
        //element.addProperty(property);
        return property;
    }


    public void reconstruct(ElementUpdate operation) {}
    public void reconstruct() {}

    public T get() {
        return value;
    }
    public boolean set(T value) { return false; }

    static boolean preventOppositeRecursion=false;
    protected void addToOpposite(Object oppositeElement) {
        if (oppositeElement==null) return;
        if (propertyType==null) return;
        if (preventOppositeRecursion) return;
        preventOppositeRecursion=true;
        try {
            PropertyType opposedPropertyType = propertyType.opposedPropertyType();
            if (opposedPropertyType!=null) {
                String opposedPropertyName = opposedPropertyType.name();
                switch (opposedPropertyType.cardinality()) {
                    case SINGLE:
                        SingleProperty opposedSingleProperty = ((Element) oppositeElement).getPropertyAsSingle(opposedPropertyName);
                        opposedSingleProperty.set(element);
                        break;
                    case SET:
                        SetProperty opposedSetProperty = ((Element) oppositeElement).getPropertyAsSet(opposedPropertyName);
                        opposedSetProperty.add(element);
                        break;
                    case LIST:
                        throw new IllegalArgumentException("opposable reference should be changed in the list first to ensure correct ordering");
                    case MAP:
                        throw new IllegalArgumentException("opposable reference should be changed in the map first to ensure correct key");
                    default:
                        //derived values should not have opposables
                        throw new IllegalArgumentException("Unrecognized opposable property type");
                }
            }
        }
        finally {
            preventOppositeRecursion=false;
        }
    }
    protected void removeFromOpposite(Object oppositeElement) {
        if (oppositeElement==null) return;
        if (propertyType==null) return;
        if (preventOppositeRecursion) return;
        preventOppositeRecursion=true;
        try {
            PropertyType opposedPropertyType = propertyType.opposedPropertyType();
            if (opposedPropertyType!=null) {
                String opposedPropertyName = opposedPropertyType.name();
                switch (opposedPropertyType.cardinality()) {
                    case SINGLE:
                        Property opposedSingleProperty = ((Element)oppositeElement).getPropertyAsSingle(opposedPropertyName);
                        opposedSingleProperty.set(null);
                        break;
                    case SET:
                        SetProperty opposedPropertySetValue = ((Element)oppositeElement).getPropertyAsSet(opposedPropertyName);
                        opposedPropertySetValue.remove(element);
                        break;
                    case LIST:
                        ListProperty opposedPropertyListProperty = ((Element)oppositeElement).getPropertyAsList(opposedPropertyName);
                        opposedPropertyListProperty.remove(element);
                        break;
                    case MAP:
                        MapProperty opposedPropertyMapValue = ((Element)oppositeElement).getPropertyAsMap(opposedPropertyName);
                        opposedPropertyMapValue.removeValue(element);
                        break;
                    default:
                        //derived values should not have opposables
                        throw new IllegalArgumentException("Unrecognized opposable property type");
                }
            }
        }
        finally {
            preventOppositeRecursion=false;
        }
    }
    public String toString() {
        return element.toString()+"."+name+"="+value;
    }
}