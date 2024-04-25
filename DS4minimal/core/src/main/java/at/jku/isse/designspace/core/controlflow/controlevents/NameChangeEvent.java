package at.jku.isse.designspace.core.controlflow.controlevents;

public class NameChangeEvent extends ControlEvent {

    private static final StorageEventType TYPE = StorageEventType.NAME_CHANGE;

    private long workspaceId;
    private String toValue;

    public NameChangeEvent(Long workspaceId, String toValue) {
        super(TYPE);
        assert workspaceId != null;
        this.workspaceId = workspaceId;
        this.toValue = toValue;
    }

    public long getWorkspaceId() {
        return workspaceId;
    }

    public String getToValue() {
        return toValue;
    }
}
