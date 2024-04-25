package at.jku.isse.designspace.core.events;

import at.jku.isse.designspace.core.model.Id;

/**
 * Describes a remove operation for a list, set, or map property.
 */
public class PropertyUpdateRemove extends PropertyUpdate {

    public PropertyUpdateRemove(Id elementId, String name, Object value, Object indexOrKey) {
        super(elementId, name, value, indexOrKey);
    }

    public Object indexOrKey() { return indexOrKey; }

    @Override
    public PropertyUpdate transform(int offset) {
        if (!(indexOrKey instanceof Integer)) {
            throw new IllegalArgumentException("Operation can not be transformed!");
        }
        var transform = new PropertyUpdateRemove(elementId, name, value, (int)indexOrKey+offset);
        transform.isAutoCreated = true;
        return transform;
    }

    @Override
    public Operation clone() {
        var clone = new PropertyUpdateRemove(elementId, name, value, indexOrKey);
        clone.isAutoCreated = true;
        return clone;
    }

    @Override
    public Operation invert() {
        var invert = new PropertyUpdateAdd(elementId, name, value, indexOrKey);
        invert.isAutoCreated = true;
        return invert;
    }

    public String toString() { return "PropertyUpdateRemove{elementId=" + elementId + ",name=" + name + ",value=" + value + ",indexOrKey=" + indexOrKey + "}"; }
}

