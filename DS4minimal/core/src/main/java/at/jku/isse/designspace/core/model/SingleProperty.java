package at.jku.isse.designspace.core.model;

import at.jku.isse.designspace.core.events.ElementUpdate;
import at.jku.isse.designspace.core.events.PropertyCreate;
import at.jku.isse.designspace.core.events.PropertyDelete;
import at.jku.isse.designspace.core.events.PropertyUpdateSet;

/**
 * Represents the value of a single element property (reference or primitive)
 */
public class SingleProperty<T> extends Property<T> {

    public SingleProperty(Element element, String name, T value, PropertyType propertyType) {
        super(element, name, value, propertyType);
    }
    public SingleProperty(Element element, String name, T value, PropertyType propertyType, boolean cacheOnly) {
        super(element, name, value, propertyType, cacheOnly);
    }

    public SingleProperty(Element element, PropertyCreate operation) {
        super(element, operation);
    }

    public SingleProperty(Element element) {
        super(element);
    }


    public void reconstruct(ElementUpdate operation) {
        Object v = operation.value();

        if (v instanceof Id) {
            v = ((Element) this.element).workspace.findElement((Id) v);
        }

        if (operation instanceof PropertyUpdateSet)
            setValue((T)v);
        else if (operation instanceof PropertyDelete)
            this.isDeleted = true;
    }


    public boolean set(Object value){
        return set(value, -1);
    }
    public boolean set(Object value, int deleteGroupId) {
        //reconstruct();
        if (value instanceof Element && !((Element)value).matchesWorkspace(this.element.workspace)) throw new IllegalArgumentException("value "+value.toString()+" is not of this workspace");

        if (this.getValue()==value) return false;

        if (propertyType==null) {
            this.setValue((T)value);
            var operation = new PropertyUpdateSet(element.id(), name, this.getValue(), -1);
            operation.deletedGroupId = deleteGroupId;
            propertyChanged(operation);
        }
        else if (propertyType.isPrimitive()) {
            if (propertyType!=null && !propertyType.isAssignable(value))
                throw new IllegalArgumentException("property value "+value+" is not compatible with referenced property type "+propertyType.referencedInstanceType());
            this.setValue((T)value);
            var operation = new PropertyUpdateSet(element.id(), name, this.getValue(), -1);
            operation.deletedGroupId = deleteGroupId;
            propertyChanged(operation);
        }
        else {
            // check if this element is already contained elsewhere if this property is contained
            if (value != null && propertyType.isContained() && this.element.checkPropertiesForContainment(propertyType, value)) {
                throw new IllegalArgumentException("Set not possible. Containment for " + element + " already set");
            }
            // check if the to be set element is already contained elsewhere if this property is a container
            if (value instanceof Element && this.isContainer && ((Element<?>) value).checkPropertiesForContainment(this.propertyType.opposedPropertyType(), element))
                throw new IllegalArgumentException("Set not possible. Containment for " + value + " already set");
            // The following assertion was once commented out, since it caused issues with multi-inheritance (4diac)
            if (propertyType!=null && !propertyType.isAssignable(value))
                throw new IllegalArgumentException("property value " + value + " is not compatible with referenced property type " + propertyType.referencedInstanceType());
            removeFromOpposite(this.getValue());

            this.setValue((T)value); //(value instanceof Element?((Element)value).id():value);
            var operation = new PropertyUpdateSet(element.id(), name, this.getValue(), -1);
            operation.deletedGroupId = deleteGroupId;
            propertyChanged(operation);

            addToOpposite(value);
        }
        return true;
    }
}