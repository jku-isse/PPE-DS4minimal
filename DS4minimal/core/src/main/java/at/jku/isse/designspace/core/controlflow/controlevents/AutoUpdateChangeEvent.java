package at.jku.isse.designspace.core.controlflow.controlevents;

public class AutoUpdateChangeEvent extends ControlEvent {

    private static final StorageEventType TYPE = StorageEventType.AUTO_UPDATE_CHANGE;

    protected long workspaceId;
    protected boolean toValue;

    public AutoUpdateChangeEvent(Long workspaceId, boolean toValue) {
        super(TYPE);
        assert workspaceId != null;
        this.workspaceId = workspaceId;
        this.toValue = toValue;
    }

    public long getWorkspaceId() {
        return workspaceId;
    }

    public boolean getToValue() {
        return toValue;
    }

}
