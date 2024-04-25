package at.jku.isse.designspace.artifactconnector.core.updateservice.core.action;

import java.sql.Timestamp;

import at.jku.isse.designspace.artifactconnector.core.updateservice.core.connection.PollingConnection;

public abstract class UpdateAction<T> {

    public enum ActionKind {
        ARTIFACT_CREATION_ACTION, UPDATE_ACTION, DELETE_ACTION, REFRESH_ACTION;
    }

    private PollingConnection.ServerKind serverKind;
    private ActionKind actionKind;
    private Timestamp timestamp;

    public UpdateAction(Timestamp timestamp, ActionKind actionKind, PollingConnection.ServerKind serverKind) {
        this.actionKind = actionKind;
        this.timestamp = timestamp;
        this.serverKind = serverKind;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public ActionKind getActionKind() {
        return actionKind;
    }

    public abstract T getUpdatedValue();

    public PollingConnection.ServerKind getServerKind() { return serverKind;}

    public abstract void applyUpdate();

}