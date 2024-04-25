package at.jku.isse.designspace.artifactconnector.core.updateservice;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import at.jku.isse.designspace.artifactconnector.core.updateservice.core.action.UpdateAction;
import at.jku.isse.designspace.artifactconnector.core.updateservice.core.connection.PollingConnection;
import at.jku.isse.designspace.artifactconnector.core.updateservice.core.connection.ReactiveConnection;
import at.jku.isse.designspace.artifactconnector.core.updateservice.core.connection.ServiceConnection;
import at.jku.isse.designspace.core.events.Operation;
import at.jku.isse.designspace.core.model.ServiceProvider;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.service.ServiceRegistry;
import io.reactivex.Observable;
import lombok.extern.slf4j.Slf4j;

/**
 * The UpdateManager allows services to register an UpdateSource.
 * UpdateSources can be push or poll based, in case of the later
 * updates they are managed by a threapool.
 *
 * All updates provided by Update sources are asynchronously applied to the Designspace.
 */
@Slf4j
@Service
public class UpdateManager implements ServiceProvider {

    private Map<String, Future> taskStates;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ExecutorService service = Executors.newCachedThreadPool();
    private Observable<UpdateAction> actionObservable;

    private boolean init = false;    

    public UpdateManager() {
    	ServiceRegistry.registerService(this);
    	taskStates = new HashMap<>();
        actionObservable = null;
    }
    
	@Override
	public String getName() {
		return "UpdateManager";
	}

	@Override
	public String getVersion() {
		return "1.0.0";
	}

	@Override
	public int getPriority() {
		return 110;
	}

	@Override
	public boolean isPersistenceAware() {
		return true;
	}
    
    @Override
	public void initialize() {
        if (!this.init) {
            this.init = true;
            log.debug("UPDATE-MANAGER: Starting service");
            if (this.actionObservable != null) {
                this.actionObservable.forEach(UpdateAction::applyUpdate);
            }
        }
    }

    /**
     * this call will block in case the service currently writes the queue
     * @return
     */
    public Observable<UpdateAction> getActionObservable() {
        return actionObservable;
    }

    public Future<?> scheduleOneTimeExecution(Runnable runnable) {
        return this.scheduler.schedule(runnable, 1, TimeUnit.SECONDS);
    }

    public void establishReactiveConnection(ReactiveConnection reactiveConnection) {
        if(actionObservable==null) {
            this.actionObservable = reactiveConnection.accessPublishedActions();
        } else {
            this.actionObservable = actionObservable.mergeWith(reactiveConnection.accessPublishedActions());
        }
    }

    public void establishServerConnection(PollingConnection pollingConnection) {
        taskStates.put(pollingConnection.getServerName(), scheduler.scheduleAtFixedRate(pollingConnection, 60, pollingConnection.getPollInterval(), TimeUnit.SECONDS));
        addConnection(pollingConnection);
    }

    public void stopServerConnection(PollingConnection pollingConnection) {
        pollingConnection.stop();
        taskStates.get(pollingConnection.getServerName()).cancel(false);
        taskStates.remove(pollingConnection.getServerName());
    }

    public void startServerConnection(PollingConnection pollingConnection) {
        Future future;
        if ((future = taskStates.get(pollingConnection.getServerName())) == null) {
            future.cancel(false);
            taskStates.remove(pollingConnection.getServerName());
        }

        pollingConnection.setToActive();
        taskStates.put(pollingConnection.getServerName(), scheduler.scheduleAtFixedRate(pollingConnection, 60, pollingConnection.getPollInterval(), TimeUnit.SECONDS));
    }

    private void addConnection(ServiceConnection serviceConnection) {
        if(actionObservable==null) {
            this.actionObservable = serviceConnection.accessPublishedActions();
        } else {
            this.actionObservable = actionObservable.mergeWith(serviceConnection.accessPublishedActions());
        }
    }

    public synchronized void stopUpdateManager() {
        this.scheduler.shutdown();
    }

	@Override
	public void handleServiceRequest(Workspace workspace, Collection<Operation> operations) {
		// TODO Auto-generated method stub
		
	}

}
