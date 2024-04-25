package at.jku.isse.designspace.artifactconnector.core.updateservice.core.connection;


import java.sql.Timestamp;

public abstract class PollingConnection extends ServiceConnection implements Runnable {

    protected boolean active;
    protected Timestamp lastFetch;
    protected int pollInterval;

    protected String serverName;
    protected ServerKind serverKind;

    public PollingConnection(String serverName, ServerKind serverKind, int pollInterval) {
        super(serverName, serverKind);
        this.lastFetch = null;
        this.pollInterval = pollInterval;
        active = true;
    }

    public PollingConnection(String serverName, ServerKind serverKind, Timestamp lastFetch, int pollInterval) {
        super(serverName, serverKind);
        this.lastFetch = lastFetch;
        this.pollInterval = pollInterval;
        active = true;
    }

    public void stop() {
        this.active = false;
    }

    public void setToActive() {
        this.active = true;
    }

    public boolean isActive() {
        return active;
    }

    public int getPollInterval() {
        return pollInterval;
    }

}
