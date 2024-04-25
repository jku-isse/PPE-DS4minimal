package at.jku.isse.designspace.core.events;

import java.io.Serializable;

import at.jku.isse.designspace.core.helper.Time;

/**
 * <p>Describes a significant action within the domain that has already happened.
 * All events originate from within the domain model. The targets of these events
 * are not known to the objects raising them.</p>
 * <p><strong>Note:</strong> Concrete subclasses of this instanceType should be
 * named in past tense of the action they describe, e.g.: <i>InstanceAdded</i></p>
 */
public abstract class Event implements Serializable {
    /**
     * This timestamp can be used to create instances,
     * with timestamps from the source.
     * If this timestamp is initialized it is always used
     */
    private static long ID=0;
    private static final long initOffset = 11000;
    private static boolean isInitialized = false;
    public static boolean isInitialized(){return isInitialized;}
    private static boolean isCleanState = false;
    public static boolean isCleanState(){return isCleanState;}

    private static boolean hasIdOffset = false;

    /**
     * The time at which the change occurred
     */
    protected long timestamp;
    protected long id = 0;

    public Event() {
        id = ID++;
        timestamp = Time.currentTime();
        if(isCleanState){
            isCleanState = false;
        }
    }

    public long timestamp() {
        return timestamp;
    }
    public long id() { return id; }


    @Override
    public String toString() { return getClass().getSimpleName(); }

    public static void setInitialized(){
        if(ID >= initOffset){
            throw new IllegalStateException("Initialization produces more events than accepted!");
        }
        isInitialized = true;
        isCleanState = true;
        ID = initOffset;
    }

    public static void offsetId(long offset) {
        if (hasIdOffset == false && isInitialized) {
            ID += offset;
            hasIdOffset = true;
        }
    }
}
