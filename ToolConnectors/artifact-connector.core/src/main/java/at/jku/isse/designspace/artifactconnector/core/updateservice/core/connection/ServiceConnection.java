package at.jku.isse.designspace.artifactconnector.core.updateservice.core.connection;

import at.jku.isse.designspace.artifactconnector.core.updateservice.core.action.UpdateAction;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public abstract class ServiceConnection {

    public enum ServerKind {
        JIRA, JAMA, POLARION, GIT;
    }

    protected PublishSubject<UpdateAction> publishedActions;
    protected String serverName;
    protected ServerKind serverKind;

    public ServiceConnection(String serverName, ServerKind serverKind) {
        publishedActions = PublishSubject.create();
        this.serverKind = serverKind;
        this.serverName = serverName;
    }

    public Observable<UpdateAction> accessPublishedActions() {
        return publishedActions;
    }

    public String getServerName() {
        return serverName;
    }

}
