package at.jku.isse.designspace.core.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import at.jku.isse.designspace.core.events.ElementUpdate;
import at.jku.isse.designspace.core.events.PropertyCreate;
import at.jku.isse.designspace.core.events.PropertyDelete;
import at.jku.isse.designspace.core.events.PropertyUpdateAdd;
import at.jku.isse.designspace.core.events.PropertyUpdateRemove;

public class SetProperty<T> extends CollectionProperty<T> {

    public SetProperty(Element element, String name, PropertyType propertyType) {
        super(element, name, propertyType);
        init();

    }

    public SetProperty(Element element, PropertyCreate operation) {
        super(element, operation);
        init();
    }

    public SetProperty(Element element, String name, PropertyType propertyType, boolean cacheOnly) {
        super(element, name, propertyType, cacheOnly);
        init();
    }
    protected SetProperty(Element element) { super(element); }

    private void init(){
        setValue(new LinkedHashSet<>());
    }

    public void reconstruct(ElementUpdate operation) {
        Object v = operation.value();
        if (v instanceof Id) v = ((Element) this.element).workspace.findElement((Id) v);

        if (operation instanceof PropertyUpdateAdd)
            ((HashSet) this.getValue()).add(v);
        else if (operation instanceof PropertyUpdateRemove)
            ((HashSet) this.getValue()).remove(v);
        else if (operation instanceof PropertyDelete)
            this.isDeleted = true;

    }

    public Set get() {
        //reconstruct();
        return Collections.unmodifiableSet((HashSet)this.getValue());
    }

    public boolean add(T value) {
        //reconstruct();
        if (value instanceof Element && !((Element)value).matchesWorkspace(this.element.workspace)) throw new IllegalArgumentException("value "+value.toString()+" is not of this workspace");
        if (propertyType!=null && !propertyType.isAssignable(value))
            throw new IllegalArgumentException("property value "+value+" is not compatible with referenced property type "+propertyType.referencedInstanceType());
        if (value instanceof Element && this.isContainer && ((Element<?>) value).checkPropertiesForContainment(this.propertyType.opposedPropertyType(), element))
            throw new IllegalArgumentException("Set not possible. Containment for " + value + " already set");
        if ( !((HashSet)this.getValue()).add( value ) ) return false;

        propertyChanged( new PropertyUpdateAdd(element.id(), name, value, -1) );

        addToOpposite(value);
        return true;
    }

    public boolean remove(Object value) {
        return remove(value, -1);
    }

    public boolean remove(Object value, long deleteGroupId) {
        //reconstruct();
        if (value instanceof Element && !((Element)value).matchesWorkspace(this.element.workspace)) throw new IllegalArgumentException("value "+value.toString()+" is not of this workspace");

        if ( !((HashSet)this.getValue()).remove( value ) ) return false;

        var operation =  new PropertyUpdateRemove(element.id(), name, value, -1);
        operation.deletedGroupId = deleteGroupId;
        propertyChanged(operation);

        removeFromOpposite(value);
        return true;
    }

    public boolean contains(Object value) {
        //reconstruct();
        return ((HashSet)this.getValue()).contains( value );
    }
}
