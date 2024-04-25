package at.jku.isse.designspace.core.events;

import at.jku.isse.designspace.core.model.Id;

/**
 * Describes an add operation for a set or list property.
 */
public class PropertyUpdateAdd extends PropertyUpdate {

    public PropertyUpdateAdd(Id elementId, String name, Object value, Object indexOrKey) {
        super(elementId, name, value, indexOrKey);
    }

    @Override
    public PropertyUpdate transform(int offset) {
        if(!(indexOrKey instanceof Integer)){
            throw new IllegalArgumentException("Operation can not be transformed!");
        }
        var transform = new PropertyUpdateAdd(elementId, name, value, (int)indexOrKey+offset);
        transform.isAutoCreated = true;
        return transform;
    }
    @Override
    public Operation clone() {
        var clone = new PropertyUpdateAdd(elementId, name, value, indexOrKey);
        clone.isAutoCreated = true;
        return clone;
    }

    @Override
    public Operation invert() {
        var invert = new PropertyUpdateRemove(elementId, name, value, indexOrKey);
        invert.isAutoCreated = true;
        return invert;
    }


    public String toString() { return "PropertyUpdateAdd{elementId=" + elementId + ",name=" + name + ",value=" + value + ",indexOrKey=" + indexOrKey + "}"; }


}
