package at.jku.isse.designspace.core.controlflow.controlevents;

public class ParentChangeEvent extends ControlEvent {

    private static final StorageEventType TYPE = StorageEventType.PARENT_CHANGE;

    protected long workspaceId;
    protected long parentWorkspaceId;

    public ParentChangeEvent(Long workspaceId, Long parentWorkspaceId) {
        super(TYPE);
        assert workspaceId != null && parentWorkspaceId != null;
        this.workspaceId = workspaceId;
        this.parentWorkspaceId = workspaceId;
    }

    public long getWorkspaceId() {
        return workspaceId;
    }

    public long getParentWorkspaceId() {
        return parentWorkspaceId;
    }

}
