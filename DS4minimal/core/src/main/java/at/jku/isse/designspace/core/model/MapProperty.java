package at.jku.isse.designspace.core.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import at.jku.isse.designspace.core.events.ElementUpdate;
import at.jku.isse.designspace.core.events.PropertyCreate;
import at.jku.isse.designspace.core.events.PropertyDelete;
import at.jku.isse.designspace.core.events.PropertyUpdateAdd;
import at.jku.isse.designspace.core.events.PropertyUpdateRemove;

public class MapProperty<T> extends Property<Map<String,T>> {

    public MapProperty(Element element, String name, PropertyType propertyType) {
        super(element, name, propertyType);
        init();
    }

    public MapProperty(Element element, String name, PropertyType propertyType, boolean cacheOnly) {
        super(element, name, propertyType, cacheOnly);
        init();
    }

    public MapProperty(Element element, PropertyCreate operation) {
        super(element, operation);
        init();
    }

    protected MapProperty(Element element) {
        super(element);
    }

    private void init(){
        setValue(new LinkedHashMap<>());
    }


    public void reconstruct(ElementUpdate operation) {
        Object v = operation.value();
        if (v instanceof Id) v = ((Element) this.element).workspace.findElement((Id) v);

        if (operation instanceof PropertyUpdateAdd)
            ((HashMap) this.getValue()).put(((PropertyUpdateAdd) operation).indexOrKey(), v);
        else if (operation instanceof PropertyUpdateRemove)
            ((HashMap) this.getValue()).remove(((PropertyUpdateRemove) operation).indexOrKey());
        else if (operation instanceof PropertyDelete)
            this.isDeleted = true;
    }

    public Map get() {
        reconstruct();
        return Collections.unmodifiableMap((HashMap)this.getValue());
    }

    public T get(String key) {
        //if (reconstructPropertyOperationCache!=null) reconstruct();
        Object value = ((HashMap)this.getValue()).get(key);
        return (T) value;
    }

    public T put(String key, T value) {
        //if (reconstructPropertyOperationCache!=null) reconstruct();
        if (value instanceof Element && !((Element)value).matchesWorkspace(this.element.workspace)) throw new IllegalArgumentException("value "+value.toString()+" is not of this workspace");
        if (propertyType!=null && !propertyType.isAssignable(value))
            throw new IllegalArgumentException("property value "+value+" is not compatible with referenced property type "+propertyType.referencedInstanceType());
        // check if the to be added element is already contained elsewhere if this property is a container
        if (value instanceof Element && this.isContainer && ((Element<?>) value).checkPropertiesForContainment(this.propertyType.opposedPropertyType(), element))
            throw new IllegalArgumentException("Set not possible. Containment for " + value + " already set");
        Object oldValue = ((HashMap)this.getValue()).put(key, value/*(value instanceof Element?element.id():value)*/ );
        if (value==oldValue) return value;

        propertyChanged( new PropertyUpdateAdd(element.id(), name, value/*(value instanceof Element?((Element)value).id():value)*/, key) );

        removeFromOpposite(oldValue);
        addToOpposite(value);
        return (T) oldValue;
    }

    public T remove(String key) {
        //if (reconstructPropertyOperationCache!=null) reconstruct();
        Object oldValue = ((HashMap)this.getValue()).remove(key);
        if (oldValue==null) return null;

        propertyChanged( new PropertyUpdateRemove(element.id(), name, oldValue, key ) );

        removeFromOpposite(oldValue);
        return (T) oldValue;
    }

    public T removeValue(T value) {
        //if (reconstructPropertyOperationCache!=null) reconstruct();
        for (Map.Entry entry : (Set<Map.Entry>) new HashSet(((HashMap)this.getValue()).entrySet())) {
            if (entry.getValue().equals(value)) remove((String)entry.getKey());
        }
        return null;
    }

    public boolean containsKey(String key) {
        //if (reconstructPropertyOperationCache!=null) reconstruct();
        return ((HashMap)this.getValue()).containsKey(key);
    }

    public boolean containsValue(Object value) {
        //if (reconstructPropertyOperationCache!=null) reconstruct();
        return ((HashMap)this.getValue()).containsValue(value);
    }

    public int size() {
        //if (reconstructPropertyOperationCache!=null) reconstruct();
        return ((HashMap)this.getValue()).size();
    }

    public boolean isEmpty() {
        //if (reconstructPropertyOperationCache!=null) reconstruct();
        return ((HashMap)this.getValue()).isEmpty();
    }

    public void clear() {
        for (String key : new HashSet<>(keySet())) remove(key);
    }

    public Set<String> keySet() {
        //if (reconstructPropertyOperationCache!=null) reconstruct();
        return ((HashMap)this.getValue()).keySet();
    }

    public HashSet<T> values() {
        //if (reconstructPropertyOperationCache!=null) reconstruct();
        return new HashSet(((HashMap)this.getValue()).values());
    }

}
