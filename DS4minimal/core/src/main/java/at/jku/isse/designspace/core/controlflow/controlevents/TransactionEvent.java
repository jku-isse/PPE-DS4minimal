package at.jku.isse.designspace.core.controlflow.controlevents;

public class TransactionEvent extends ControlEvent {

    public static final StorageEventType TYPE = StorageEventType.TRANSACTION;

    private long workspaceId;

    public TransactionEvent(Long workspaceId) {
        super(TYPE);
        assert workspaceId != null;
        this.workspaceId = workspaceId;
    }

    public long getWorkspaceId() {
        return workspaceId;
    }

}
