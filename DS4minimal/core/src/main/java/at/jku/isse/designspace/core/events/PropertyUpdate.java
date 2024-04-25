package at.jku.isse.designspace.core.events;

import java.util.Date;

import at.jku.isse.designspace.core.model.Element;
import at.jku.isse.designspace.core.model.Id;
import at.jku.isse.designspace.core.model.Property;

/**
 * Describes a change to the value of the property.
 */
public abstract class PropertyUpdate extends ElementUpdate {

    Object indexOrKey;
    public PropertyUpdate(Id elementId, String name, Object value, Object indexOrKey) {
        super(elementId, name, value);
        this.indexOrKey = indexOrKey;

        if (value==null) return;
        if (value instanceof Element) return;
        if (value instanceof Property) return;
        if (value instanceof Id) return;
        if (value instanceof String) return;
        if (value instanceof Long || value instanceof Integer) return;
        if (value instanceof Boolean) return;
        if (value instanceof Double) return;
        if (value instanceof Date) return;

        throw new IllegalArgumentException("property value has illegal type "+value);
    }

    public Object indexOrKey() {
        return indexOrKey;
    }


    public abstract Operation transform(int offset);
    public abstract Operation invert();
}
