package at.jku.isse.designspace.core.model;

import java.util.Collection;

import at.jku.isse.designspace.core.events.Operation;

public interface WorkspaceListener {

    /**
     * updated is used when a workspace publishes operations or receives operations from other workspaces (e.g., typically
     * send from a child or parent workspace as part of an auto update or auto commit).
     * @param operations
     */
    public void handleUpdated(Collection<Operation> operations);
}