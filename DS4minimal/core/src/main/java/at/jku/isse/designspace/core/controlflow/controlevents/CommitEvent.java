package at.jku.isse.designspace.core.controlflow.controlevents;

public class CommitEvent extends ControlEvent {

    public static final StorageEventType TYPE = StorageEventType.COMMIT;

    private long workspaceId;

    public CommitEvent(Long workspaceId) {
        super(TYPE);
        assert workspaceId != null;
        this.workspaceId = workspaceId;
    }

    public long getWorkspaceId() {
        return workspaceId;
    }

}
