package at.jku.isse.designspace.core.controlflow.controlevents;

public class AutoCommitChangeEvent extends ControlEvent {

    private static final StorageEventType TYPE = StorageEventType.AUTO_COMMIT_CHANGE;

    private long workspaceId;
    private boolean toValue;

    public AutoCommitChangeEvent(Long workspaceId, boolean toValue) {
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
