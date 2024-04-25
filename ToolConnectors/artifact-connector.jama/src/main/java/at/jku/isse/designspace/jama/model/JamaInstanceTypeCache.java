package at.jku.isse.designspace.jama.model;

import java.util.Set;

import at.jku.isse.designspace.artifactconnector.core.model.BaseElementType;
import at.jku.isse.designspace.core.model.Element;
import at.jku.isse.designspace.core.model.Id;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.Workspace;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JamaInstanceTypeCache {

    public static final String JAMA_INSTANCE_TYPES_ID_CACHE = "JAMA_INSTANCE_TYPE_ID_CACHE";
    private Instance cache;
    private Workspace workspace;

    public JamaInstanceTypeCache(Workspace workspace) {
        Set<Instance> instances = workspace.debugInstances();
        this.workspace = workspace;

        cache = workspace.debugInstanceFindByName(JAMA_INSTANCE_TYPES_ID_CACHE);
//        
//        
//        for(Instance currentInstance : instances) {
//            Property property = currentInstance.getProperty(BaseElementType.ID);
//
//            if(property != null) {
//                if(property.get() != null && property.get().equals(JAMA_INSTANCE_TYPES_ID_CACHE)) {
//                    cache = currentInstance;
//                    break;
//                }
//            }
//        }

        if(cache == null) {
            //no past cache has been found
            cache = workspace.createInstance(BaseElementType.ELEMENT_ID_CACHE.getType(), JAMA_INSTANCE_TYPES_ID_CACHE);
            cache.getPropertyAsSingle(BaseElementType.ID).set(JAMA_INSTANCE_TYPES_ID_CACHE);
            workspace.concludeTransaction();
        } else {
            //past cache has been found
            log.debug("JAMA-SERVICE: Successfully reconnected to stored Instance Type cache");
        }

    }

    public Id getInstanceId(String itemTypeId){
        Object id = cache.getPropertyAsMap(BaseElementType.MAPPING).get(itemTypeId);

        if(id == null){
            return null;
        }

        return Id.of(Long.parseLong((String) id));
    }

    public InstanceType getInstanceType(String itemTypeId) {
        Id id = getInstanceId(itemTypeId);
        if (id != null) {
            Element element = this.workspace.findElement(id);
            if (element != null) {
                try {
                    return (InstanceType) element;
                } catch (ClassCastException ce) {
                    log.debug("JAMA SERVICE: JamaInstanceTypeCache: getInstanceType found an element that was not an InstanceType!");
                    return null;
                }
            }
        }

        return null;
    }

    public void putInstanceId(String itemTypeId, Id designspaceId){
        cache.getPropertyAsMap(BaseElementType.MAPPING).put(itemTypeId, designspaceId.toString());
    }

}
