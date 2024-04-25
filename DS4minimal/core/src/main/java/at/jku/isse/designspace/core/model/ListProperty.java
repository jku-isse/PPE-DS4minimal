package at.jku.isse.designspace.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import at.jku.isse.designspace.core.events.ElementUpdate;
import at.jku.isse.designspace.core.events.PropertyCreate;
import at.jku.isse.designspace.core.events.PropertyDelete;
import at.jku.isse.designspace.core.events.PropertyUpdateAdd;
import at.jku.isse.designspace.core.events.PropertyUpdateRemove;
import at.jku.isse.designspace.core.events.PropertyUpdateSet;

public class ListProperty<T> extends CollectionProperty<T> {

    public ListProperty(Element element, String name, PropertyType propertyType) {
        super(element, name, propertyType);
        this.setValue(new ArrayList());
    }

    public ListProperty(Element element, String name, PropertyType propertyType, boolean cacheOnly) {
        super(element, name, propertyType, cacheOnly);
        this.setValue(new ArrayList());
    }

    public ListProperty(Element element, PropertyCreate operation) {
        super(element, operation);
        this.setValue(new ArrayList());
    }

    protected ListProperty(Element element) {
        super(element);
    }


    public void reconstruct(ElementUpdate operation) {
        Object v = operation.value();
        if (v instanceof Id) v = ((Element) this.element).workspace.findElement((Id) v);

        if (operation instanceof PropertyUpdateAdd) {
            int index = (int) ((PropertyUpdateAdd) operation).indexOrKey();
            if (index < 0)
                ((ArrayList) this.getValue()).add(v);
            else
                ((ArrayList) this.getValue()).add(index, v);
        } else if (operation instanceof PropertyUpdateRemove) {
            int index = (int) ((PropertyUpdateRemove) operation).indexOrKey();
            if (index < 0){
                ((ArrayList) this.getValue()).remove(v);
            }
            else{
                var removedItem = ((ArrayList) this.getValue()).remove(index);
                if(v != null && removedItem != v){
                    throw new IllegalStateException("incorrect item removed at index "+index+": It was: "+removedItem+" - Should be: "+v);
                }
            }
        } else if (operation instanceof PropertyUpdateSet) {
            int index = (int) ((PropertyUpdateSet) operation).indexOrKey();
            if (index < 0)
                throw new IllegalStateException("list set operation requires index during reconstruct");
            else
                ((ArrayList) this.getValue()).set(index, v);
        } else if (operation instanceof PropertyDelete) {
            this.isDeleted = true;
        }
    }

    public List<T> get() {
        //reconstruct();
        return Collections.unmodifiableList((ArrayList)this.getValue());
    }

    public T get(int index) {
        //reconstruct();
        Object v = ((ArrayList)this.getValue()).get(index);
        return (T) v;
    }

    public boolean add(T value) {
        //reconstruct();
        if (value instanceof Element && !((Element)value).matchesWorkspace(this.element.workspace)) throw new IllegalArgumentException("value "+value.toString()+" is not of this workspace");
        if (propertyType!=null && !propertyType.isAssignable(value))
            throw new IllegalArgumentException("property value "+value+" is not compatible with referenced property type "+propertyType.referencedInstanceType());
        // check if the to be added element is already contained elsewhere if this property is a container
        if (value instanceof Element && this.isContainer && ((Element<?>) value).checkPropertiesForContainment(this.propertyType.opposedPropertyType(), element))
            throw new IllegalArgumentException("Set not possible. Containment for " + value + " already set");
        ((ArrayList)this.getValue()).add( value );

        propertyChanged( new PropertyUpdateAdd(element.id(), name, value, this.getValue().size()-1) );

        addToOpposite(value);
        return true;
    }

    public boolean add(int index, T value) {
        //reconstruct();
        if (value instanceof Element && !((Element)value).matchesWorkspace(this.element.workspace)) throw new IllegalArgumentException("value "+value.toString()+" is not of this workspace");
        if (propertyType!=null && !propertyType.isAssignable(value))
            throw new IllegalArgumentException("property value "+value+" is not compatible with referenced property type "+propertyType.referencedInstanceType());

        ((ArrayList)this.getValue()).add(index, value );

        propertyChanged( new PropertyUpdateAdd(element.id(), name, value, index));

        addToOpposite(value);
        return true;
    }

    public T set(int index, T value) {
        //reconstruct();
        if (value instanceof Element && !((Element)value).matchesWorkspace(this.element.workspace)) throw new IllegalArgumentException("value "+value.toString()+" is not of this workspace");
        if (propertyType!=null && !propertyType.isAssignable(value))
            throw new IllegalArgumentException("property value "+value+" is not compatible with referenced property type "+propertyType.referencedInstanceType());

        Object oldValue = ((ArrayList)this.getValue()).set(index, value );
        if (oldValue==value) return value;

        propertyChanged( new PropertyUpdateSet(element.id(), name, value, index) );

        removeFromOpposite(oldValue);
        addToOpposite(value);
        return (T) oldValue;
    }

    public boolean remove(Object value) {
        //reconstruct();
        if (value instanceof Element && !((Element)value).matchesWorkspace(this.element.workspace)) throw new IllegalArgumentException("value "+value.toString()+" is not of this workspace");
        if (propertyType!=null && !propertyType.isAssignable(value))
            throw new IllegalArgumentException("property value "+value+" is not compatible with referenced property type "+propertyType.referencedInstanceType());

        int firstIndexOfValue = ((ArrayList) this.getValue()).indexOf(value);
        if ( !((ArrayList)this.getValue()).remove( value )) return false;

        var operation = new PropertyUpdateRemove(element.id(), name, value, firstIndexOfValue);
        propertyChanged(operation);

        removeFromOpposite(value);
        return true;
    }

    public T remove(int index) {
        //reconstruct();
        Object oldValue = ((ArrayList)this.getValue()).remove(index);

        propertyChanged( new PropertyUpdateRemove(element.id(), name, oldValue, index) );

        removeFromOpposite(oldValue);

        return (T) oldValue;
    }

    public int indexOf(T value) {
        //reconstruct();
        return ((ArrayList)this.getValue()).indexOf( value );
    }

    public int lastIndexOf(T value) {
        //reconstruct();
        return ((ArrayList)this.getValue()).lastIndexOf( value );
    }

    public boolean contains(Object value) {
        //reconstruct();
        return ((ArrayList)this.getValue()).contains( value );
    }
}
