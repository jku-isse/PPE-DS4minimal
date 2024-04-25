package at.jku.isse.designspace.core.events;

import at.jku.isse.designspace.core.model.Id;

/**
 * Describes the creation of an element. For any given element, this is the first change to be published and
 * this change is published once only per element.
 */
public final class ElementCreate<T> extends Operation {

    String className;
    Id instanceTypeId;

    public ElementCreate(Id elementId, String className, Id instanceTypeId) {
        super(elementId);
        this.instanceTypeId = instanceTypeId;
        this.className = className;
    }

    public Id instanceTypeId() {
        return instanceTypeId;
    }
    public String className() {
        return className;
    }

    @Override
    public Operation clone() {
        var clone = new ElementCreate(elementId, className, instanceTypeId);
        clone.isAutoCreated = true;
        return clone;
    }

    @Override
    public Operation invert() {
        var invert = new ElementDelete(elementId, this);
        invert.isAutoCreated = true;
        return invert;
    }

    public String toString() { return "ElementCreate{elementId="+elementId+",className="+className+",instanceTypeId="+instanceTypeId+"}"; }
}