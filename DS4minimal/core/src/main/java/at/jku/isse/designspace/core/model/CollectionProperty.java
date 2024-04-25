package at.jku.isse.designspace.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import at.jku.isse.designspace.core.events.PropertyCreate;

public abstract class CollectionProperty<T> extends Property<Collection<T>> implements Collection<T> {

    protected CollectionProperty(Element element, String propertyName, PropertyType propertyType) {
        super(element, propertyName, propertyType);
    }

    protected CollectionProperty(Element element, String propertyName, PropertyType propertyType, boolean cacheOnly) {
        super(element, propertyName, propertyType, cacheOnly);
    }

    protected CollectionProperty(Element element, PropertyCreate operation) {
        super(element, operation);
    }

    protected CollectionProperty(Element element) {
        super(element);
    }

    @Override
    public boolean addAll(Collection c) {
        boolean added = false;
        for (Object value : c) {
            added = add((T) value) || added;
        }
        return added;
    }

    @Override
    public boolean retainAll(Collection c) {
        boolean retained = false;
        for (Object value : ((Collection)this.getValue())) {
            if ( !c.contains(value) )
                retained = remove(value) || retained;
        }
        return retained;
    }

    @Override
    public boolean removeAll(Collection c) {
        boolean removed = false;
        for (Object value : c) {
            removed = remove(value) || removed;
        }
        return removed;
    }

    @Override
    public boolean containsAll(Collection c) {
        for (Object value : c) {
            if (contains(value)) return false;
        }
        return true;
    }

    @Override
    public void clear() {
        removeAll((new ArrayList((Collection)this.getValue())));
    }

    @Override
    public int size() {
        //if (reconstructPropertyOperationCache!=null) reconstruct();
        return ((Collection)this.getValue()).size();
    }

    @Override
    public boolean isEmpty() {
        //if (reconstructPropertyOperationCache!=null) reconstruct();
        return ((Collection)this.getValue()).isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        //if (reconstructPropertyOperationCache!=null) reconstruct();
        return new CollectionPropertyIterator<T>(((Element)element), ((Collection)this.getValue()).iterator());
    }

    @Override
    public Object[] toArray() {
        //if (reconstructPropertyOperationCache!=null) reconstruct();
        return ((Collection)this.getValue()).toArray();
    }

    @Override
    public Object[] toArray(Object[] a) {
        //if (reconstructPropertyOperationCache!=null) reconstruct();
        return ((Collection)this.getValue()).toArray(a);
    }
}
