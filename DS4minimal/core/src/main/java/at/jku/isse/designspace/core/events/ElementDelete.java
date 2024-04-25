package at.jku.isse.designspace.core.events;

import at.jku.isse.designspace.core.model.Id;

/**
 * Describes the deletion of an element. This will be the last event ever published by any element and only published once.
 */
public final class ElementDelete extends Operation {
    ElementCreate originElement;

    public ElementDelete(Id elementId, ElementCreate originElement) {
        super(elementId);
        this.originElement = originElement;
    }

    @Override
    public Operation clone() {
        var clone = new ElementDelete(elementId, originElement);
        clone.isAutoCreated = true;
        return clone;
    }

    @Override
    public Operation invert() {
        var invert = originElement.clone();
        invert.isAutoCreated = true;
        return invert;
    }

    public String toString() { return "ElementDelete{elementId="+elementId+"}"; }
}
