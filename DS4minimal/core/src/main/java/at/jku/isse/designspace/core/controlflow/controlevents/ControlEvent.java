package at.jku.isse.designspace.core.controlflow.controlevents;

import at.jku.isse.designspace.core.events.Event;

public abstract class ControlEvent extends Event {

    protected StorageEventType type;
    protected long time;

    public ControlEvent(StorageEventType type) {
        assert type != null;
        this.time = System.currentTimeMillis();
        this.type = type;
        this.time = time;
    }

    public StorageEventType getType() {
        return type;
    }

    public long getTime() {
        return time;
    }

}
