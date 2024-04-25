package at.jku.isse.designspace.core.events;

import at.jku.isse.designspace.core.model.Cardinality;
import at.jku.isse.designspace.core.model.Id;

/**
 * Describes the creation of a property. For any given property, this is the first change to be published and
 * this change is published once only.
 */
public class PropertyCreate extends ElementUpdate {

    Id propertyTypeId;
    Cardinality cardinality;

    public PropertyCreate(Id elementId, String name, Cardinality cardinality, Id propertyTypeId) {
        super(elementId, name, null);
        this.propertyTypeId = propertyTypeId;
        this.cardinality = cardinality;
    }

    public Id propertyTypeId() { return this.propertyTypeId; }
    public Cardinality cardinality() { return this.cardinality; }

    @Override
    public Operation clone() {
        var clone = new PropertyCreate(elementId, name, cardinality, propertyTypeId);
        clone.isAutoCreated = true;
        return clone;
    }

    @Override
    public Operation invert() {
        var invert = new PropertyDelete(elementId, name, this);
        invert.isAutoCreated = true;
        return invert;
    }



    public String toString() { return "PropertyCreate{elementId="+elementId+",name="+name+",cardinality="+cardinality+",propertyTypeId="+propertyTypeId+"}"; }
}
