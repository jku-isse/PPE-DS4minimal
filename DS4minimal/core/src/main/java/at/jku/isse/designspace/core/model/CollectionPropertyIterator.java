package at.jku.isse.designspace.core.model;

import java.util.Iterator;

public class CollectionPropertyIterator<T> implements Iterator<T>
{
    private Iterator iterator;
    private Element element;

    public CollectionPropertyIterator(Element element, Iterator a) {
        this.iterator = a;
        this.element = element;
    }

    @Override
    public boolean hasNext() {
        return this.iterator.hasNext();
    }

    @Override
    public T next() {

        Object value = this.iterator.next();
        return (T) (value instanceof Id ? element.workspace.findElement((Id)value):value);
    }
}
