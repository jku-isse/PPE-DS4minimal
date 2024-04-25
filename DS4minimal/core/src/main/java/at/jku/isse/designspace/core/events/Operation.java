package at.jku.isse.designspace.core.events;

import org.springframework.util.Assert;

import at.jku.isse.designspace.core.model.Id;

/**
 * Describes an abstract change that concerns a specific element. Subclasses specify more concrete instances of such changes.
 */
public abstract class Operation extends Event {
    protected final Id elementId;
    protected long conclusionId = -1;
    protected boolean isAutoCreated = false;
    public long deletedGroupId = -1;

    public Operation(Id elementId) {
        super();
        Assert.notNull(elementId, "Element must not be null");
        this.elementId = elementId;
    }

    public void conclude(long conclusionId){ this.conclusionId = conclusionId;}
    public boolean isConcluded(){
        return conclusionId != -1;
    }
    public boolean isAutoCreated(){
        return isAutoCreated;
    }
    public long getConclusionId(){
        return conclusionId;
    }
    public Id elementId() {
        return elementId;
    }

    @Override
    public String toString() {
        return super.toString() + "{" + elementId + "}";
    }

    public abstract Operation clone();
    public abstract Operation invert();
}
