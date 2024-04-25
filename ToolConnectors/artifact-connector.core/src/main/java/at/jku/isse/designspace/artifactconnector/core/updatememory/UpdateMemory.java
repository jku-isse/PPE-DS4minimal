package at.jku.isse.designspace.artifactconnector.core.updatememory;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;

import org.springframework.stereotype.Service;

import at.jku.isse.designspace.artifactconnector.core.model.BaseElementType;
import at.jku.isse.designspace.core.events.Operation;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.MapProperty;
import at.jku.isse.designspace.core.model.Property;
import at.jku.isse.designspace.core.model.ServiceProvider;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.service.ServiceRegistry;
import at.jku.isse.designspace.core.service.WorkspaceService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UpdateMemory implements ServiceProvider {

    private static final String JIRA_UPDATE_MEMORY_ID = "ArtifactConnectorUpdateMemory";

    private static boolean isInitialized = false;

    private Instance updateMemory;
    private Workspace workspace;

    public UpdateMemory() {
    	ServiceRegistry.registerService(this);
    }
    
	@Override
	public String getName() {
		return "UpdateMemory";
	}

	@Override
	public String getVersion() {
		return "1.0.0";
	}

	@Override
	public int getPriority() {
		return 101;
	}

	@Override
	public boolean isPersistenceAware() {
		return true;
	}

    @Override
	public void initialize() {
        this.workspace = WorkspaceService.PUBLIC_WORKSPACE;
        Set<Instance> instances = this.workspace.debugInstances();

        //trying to find a cache
        for(Instance cur : instances) {
            Property property = cur.getProperty(BaseElementType.ID);
            if(property != null) {
                if(property.get() != null && property.get().equals(JIRA_UPDATE_MEMORY_ID)) {
                    updateMemory = cur;
                    break;
                }
            }
        }

        if(updateMemory ==null) {
            //no updateMemory has been found
            updateMemory = workspace.createInstance(BaseElementType.UPDATE_MEMORY.getType(), JIRA_UPDATE_MEMORY_ID);
            updateMemory.getPropertyAsSingle(BaseElementType.ID).set(JIRA_UPDATE_MEMORY_ID);
            workspace.concludeTransaction();
        } else {
            //past cache has been found
            log.debug("UPDATE-MEMORY: Successfully connected to UPDATE MEMORY");
        }
        isInitialized = true;
    }

    public void setLastUpdateTime(String server, Instant update) {
        if (!isInitialized) {
            initialize();
        }

        assert update != null && server != null;
        MapProperty map = this.updateMemory.getPropertyAsMap(BaseElementType.SERVICE_UPDATE_TIME);
        map.put(server, update.toString());
    }

    public Instant getLastUpdateTime(String server) {
        if (!isInitialized) {
            initialize();
        }

        MapProperty map = this.updateMemory.getPropertyAsMap(BaseElementType.SERVICE_UPDATE_TIME);
        Object lastUpdate = map.get(server);
        if (lastUpdate != null) {
            return Instant.parse(lastUpdate.toString());
        }
        return null;
    }

	@Override
	public void handleServiceRequest(Workspace workspace, Collection<Operation> operations) {
		// TODO Auto-generated method stub
		
	}



}
