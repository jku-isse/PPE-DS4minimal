package at.jku.isse.designspace.azure.idcache;

import java.util.Set;

import at.jku.isse.designspace.azure.model.AzureBaseElementType;
import at.jku.isse.designspace.core.model.Id;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.Property;
import at.jku.isse.designspace.core.model.Workspace;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IdCache {

    private static final String AZURE_ID_TO_DESIGNSPACE_ID_CACHE_ID = "AzureId2DesignspaceIdCache";
    private Instance cache;

    //TODO: use dependency injection to inject an instance of IdCache in other places
    public IdCache(Workspace workspace) {
        Set<Instance> instances = workspace.debugInstances();

        for(Instance currentInstance : instances) {
            Property property = currentInstance.getProperty(AzureBaseElementType.ID);

            if(property != null) {
                if(property.get() != null && property.get().equals(AZURE_ID_TO_DESIGNSPACE_ID_CACHE_ID)) {
                    cache = currentInstance;
                    break;
                }
            }
        }

        if(cache == null) {
            //no past cache has been found
            cache = workspace.createInstance(AzureBaseElementType.ELEMENT_ID_CACHE.getType(), AZURE_ID_TO_DESIGNSPACE_ID_CACHE_ID);
            cache.getPropertyAsSingle(AzureBaseElementType.ID).set(AZURE_ID_TO_DESIGNSPACE_ID_CACHE_ID);
            workspace.concludeTransaction();
        } else {
            //past cache has been found
            log.debug("AZURE-SERVICE: Successfully reconnected to stored cache");
        }

    }

    public Id getInstanceId(String azureId){
        Object id = cache.getPropertyAsMap(AzureBaseElementType.MAPPING).get(azureId);

        if(id == null){
            return null;
        }

        return Id.of(Long.parseLong((String) id));
    }

    public void putInstanceId(String azureId, Id designspaceId){
        cache.getPropertyAsMap(AzureBaseElementType.MAPPING).put(azureId, designspaceId.toString());
    }
}
