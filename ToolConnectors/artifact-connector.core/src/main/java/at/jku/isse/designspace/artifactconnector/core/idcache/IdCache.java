package at.jku.isse.designspace.artifactconnector.core.idcache;

import java.util.HashSet;
import java.util.Set;

import at.jku.isse.designspace.artifactconnector.core.model.BaseElementType;
import at.jku.isse.designspace.core.model.Id;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.Property;
import at.jku.isse.designspace.core.model.Workspace;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IdCache implements IIdCache {

    private Instance cache;

    public IdCache(Workspace workspace, String serviceIdToDesignspaceToIdCacheId) {

        //trying to find a cache
        for(Instance cur : workspace.debugInstances()) {
            Property property = cur.getProperty("id");
            if(property != null) {
                if(property.get() != null && property.get().equals(serviceIdToDesignspaceToIdCacheId)) {
                    cache = cur;
                }
            }
        }

        if(cache==null) {
            //no past cache has been found
            cache = workspace.createInstance(BaseElementType.ELEMENT_ID_CACHE.getType(), serviceIdToDesignspaceToIdCacheId);
            cache.getPropertyAsSingle("id").set(serviceIdToDesignspaceToIdCacheId);
        } else {
            //past cache has been found
            log.debug("GIT-SERVICE: Successfully reconnected to stored cache");
            cache.getPropertyAsMap("map").get();
        }

    }

    @Override
    public Id getDesignspaceId(String serviceId) {
        Object id = cache.getPropertyAsMap("map").get(serviceId);

        if (id == null) {
            return null;
        }

        return Id.of(Long.parseLong((String) id));
    }

    @Override
    public void addEntry(String serviceId, Id designspaceId) {
        cache.getPropertyAsMap("map").put(serviceId, designspaceId.toString());
    }

    @Override
    public Set<Id> getAllInstanceIds() {
        HashSet<Id> set = new HashSet<>();
        cache.getPropertyAsMap("map").values().forEach(id -> set.add(Id.of(Long.parseLong((String) id))));
        return set;
    }

    @Override
    public Set<String> getAllServiceIds() {
        HashSet<String> set = new HashSet<>();
        cache.getPropertyAsMap("map").keySet().forEach(value -> set.add((String) value));
        return set;
    }

}
