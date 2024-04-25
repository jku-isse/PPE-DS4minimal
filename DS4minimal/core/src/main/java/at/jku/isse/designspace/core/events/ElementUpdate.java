package at.jku.isse.designspace.core.events;

import org.springframework.util.Assert;

import at.jku.isse.designspace.core.model.Element;
import at.jku.isse.designspace.core.model.Id;

/**
 * Describes the change of a property value. This event is also raised when a property is created or deleted.
 */
public abstract class ElementUpdate extends Operation {

    /**
     * The name of the property that was updated
     */
    protected String name;

    /**
     * The value of the property after it was updated, or null if it currently has no value
     */
    protected Object value;

    public ElementUpdate(Id elementId, String name, Object value) {
        super(elementId);
        Assert.notNull(name, "Property name must not be null");

        if (value instanceof Element) value = ((Element) value).id();

        this.name = name;
        this.value = value;
    }

    public String name() { return name; }
    public Object value() {
        return value;
    }
}
