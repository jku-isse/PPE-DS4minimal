package at.jku.isse.designspace.core.events;

import at.jku.isse.designspace.core.model.Id;


/**
 * Describes a set operation for a single, list, or map property.
 */
public class PropertyUpdateSet extends PropertyUpdate {

    public PropertyUpdateSet(Id elementId, String name, Object value, Object indexOrKey) {
        super(elementId, name, value, indexOrKey);
    }

    @Override
    public PropertyUpdate transform(int offset) {
        return null;
    }
    @Override
    public Operation clone() {
        var clone = new PropertyUpdateSet(elementId, name, value, indexOrKey);
        clone.isAutoCreated = true;
        return clone;
    }

    @Override
    public Operation invert() {
        return clone();
    }

    public String toString() { return "PropertyUpdateSet{elementId=" + elementId + ",name=" + name + ",value=" + value + ",indexOrKey=" + indexOrKey + "}"; }
}