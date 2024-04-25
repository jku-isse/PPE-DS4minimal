package at.jku.isse.designspace.core.controlflow.controlevents;

public class UpdateEvent extends ControlEvent {

    public static final StorageEventType TYPE = StorageEventType.UPDATE;

    private long workspaceId;

    public UpdateEvent(Long workspaceId) {
        super(TYPE);
        assert workspaceId != null;
        this.workspaceId = workspaceId;
    }
    
    public long getWorkspaceId() {
        return workspaceId;
    }

}
