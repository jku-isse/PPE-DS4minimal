package at.jku.isse.designspace.artifactconnector.core.updateservice.core.connection;

import at.jku.isse.designspace.artifactconnector.core.updateservice.core.action.UpdateAction;

public abstract class ReactiveConnection extends ServiceConnection {

    public ReactiveConnection(String serverName, ServerKind serverKind) {
        super(serverName, serverKind);
    }

    protected void addActionToUpdateQueue(UpdateAction action) {
        this.publishedActions.onNext(action);
    }

}
