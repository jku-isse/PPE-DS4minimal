package at.jku.isse.designspace.jira.model;

import at.jku.isse.designspace.artifactconnector.core.converter.ISchemaCache;
import at.jku.isse.designspace.artifactconnector.core.model.BaseElementType;
import at.jku.isse.designspace.core.model.Id;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.Property;
import at.jku.isse.designspace.core.model.Workspace;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JiraSchemaCache implements ISchemaCache {

    private static final String SCHEMA_CACHE_ID = "designspaceSchemaIdCache";
    private Instance cache;
    private Workspace workspace;

    public JiraSchemaCache(Workspace workspace) {
        this.workspace = workspace;

        //trying to find a cache
        for(Instance cur : workspace.debugInstances()) {
            Property property = cur.getProperty("id");
            if(property != null) {
                if(property.get() != null && property.get().equals(SCHEMA_CACHE_ID)) {
                    cache = cur;
                }
            }
        }

        if(cache==null) {
            //no past cache has been found
            cache = workspace.createInstance(BaseElementType.ELEMENT_ID_CACHE.getType(), SCHEMA_CACHE_ID);
            cache.getPropertyAsSingle(BaseElementType.ID).set(SCHEMA_CACHE_ID);
            workspace.concludeTransaction();
        } else {
            //past cache has been found
            log.debug("ARTIFACT_CONNECTOR: Successfully reconnected to schema cache");
        }
    }

    @Override
    public void addSchema(InstanceType instanceType, String schemaId) {
        cache.getPropertyAsMap(BaseElementType.MAPPING).put(schemaId, Long.toString(instanceType.id().value()));
    }

    @Override
    public InstanceType getSchema(String schemaId) {
        Object object = cache.getPropertyAsMap(BaseElementType.MAPPING).get(schemaId);
        if(object != null) {
            String longString = object.toString();
            InstanceType instanceType = workspace.findElement(Id.of(Long.parseLong(longString)));
            if(instanceType!=null) {
                return instanceType;
            }
        }
        return null;
    }

}
