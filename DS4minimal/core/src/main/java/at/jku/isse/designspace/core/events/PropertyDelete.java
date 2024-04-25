package at.jku.isse.designspace.core.events;

import at.jku.isse.designspace.core.model.Id;

/**
 * Describes the deletion of a property. For any given property, this is the last change to be published and
 * this change is published once only.
 */
public class PropertyDelete extends ElementUpdate {
    PropertyCreate originProperty;

    public PropertyDelete(Id elementId, String name, PropertyCreate originProperty) {
        super(elementId, name, null);
        this.originProperty = originProperty;
    }

    @Override
    public Operation clone() {
        var clone = new PropertyDelete(elementId, name, originProperty);
        clone.isAutoCreated = true;
        return clone;
    }

    @Override
    public Operation invert() {
        var invert = originProperty.clone();
        invert.isAutoCreated = true;
        return invert;
    }

    public String toString() { return "PropertyDelete{elementId="+elementId+",name="+name+"}"; }
}
