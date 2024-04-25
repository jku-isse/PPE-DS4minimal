package at.jku.isse.designspace.core.controlflow.controlevents;

import at.jku.isse.designspace.core.events.Operation;

public class OperationEvent extends ControlEvent {

    public static final StorageEventType TYPE = StorageEventType.OPERATION;

    private Operation operation;
    private long workspaceId;

    public OperationEvent(Operation operation, long workspaceId) {
        super(TYPE);
        assert operation != null;
        this.operation = operation;
        this.workspaceId = workspaceId;
    }

    public Operation getOperation() {
        return operation;
    }

    public long getWorkspaceId() {
        return workspaceId;
    }

}
