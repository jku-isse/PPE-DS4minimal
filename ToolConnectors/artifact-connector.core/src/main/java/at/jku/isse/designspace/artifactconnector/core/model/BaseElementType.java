package at.jku.isse.designspace.artifactconnector.core.model;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import org.springframework.core.io.support.PropertiesLoaderUtils;

import at.jku.isse.designspace.artifactconnector.core.converter.IElementTypeGetter;
import at.jku.isse.designspace.core.model.Cardinality;
import at.jku.isse.designspace.core.model.Element;
import at.jku.isse.designspace.core.model.Id;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.Property;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.service.WorkspaceService;
import lombok.extern.slf4j.Slf4j;


/**
 *
 * This enumeration contains the basic instanceTypes,
 * which services of this Designspace extension
 * should use as a basis to create their customized Artifacts.
 *
 */
@Slf4j
public enum BaseElementType implements IElementTypeGetter {


    ARTIFACT("artifact"){
        @Override
        public InstanceType getType(){
            if (!ARTIFACT.isInitialized) {
                ARTIFACT.tryLoadingTypeDefinitions();
                if (ARTIFACT.isInitialized) return ARTIFACT.instanceType;

                InstanceType type = ARTIFACT.instanceType = WorkspaceService.createInstanceType(workspace, ARTIFACT.name, workspace.TYPES_FOLDER);
                cache.getPropertyAsMap(MAPPING).put(ARTIFACT.name, Long.toString(type.id().value()));
                ARTIFACT.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, ARTIFACT.instanceType, ID, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, ARTIFACT.instanceType, KEY, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, ARTIFACT.instanceType, LINK, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, ARTIFACT.instanceType, SERVICE, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, ARTIFACT.instanceType, FULLY_FETCHED, Cardinality.SINGLE, Workspace.BOOLEAN);

                return type;
            } else {
                return ARTIFACT.instanceType;
            }
        }
    }, ELEMENT_ID_CACHE("artifact_schema_cache") {
        @Override
        public InstanceType getType(){
            if (!ELEMENT_ID_CACHE.isInitialized) {
                if (workspace == null) {
                    workspace = ELEMENT_ID_CACHE.getWorkspace();
                }

                InstanceType type = ELEMENT_ID_CACHE.instanceType = WorkspaceService.createInstanceType(workspace, ELEMENT_ID_CACHE.name, workspace.TYPES_FOLDER);
                ELEMENT_ID_CACHE.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, ELEMENT_ID_CACHE.instanceType, ID, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, ELEMENT_ID_CACHE.instanceType, MAPPING, Cardinality.MAP, Workspace.STRING);

                return type;
            } else {
                return ELEMENT_ID_CACHE.instanceType;
            }
        }
    }, ARTIFACT_SCHEMA("artifact_schema") {
        @Override
        public InstanceType getType(){
            if (!ARTIFACT_SCHEMA.isInitialized) {
                ARTIFACT_SCHEMA.tryLoadingTypeDefinitions();
                if (ARTIFACT_SCHEMA.isInitialized) return ARTIFACT_SCHEMA.instanceType;

                InstanceType type = ARTIFACT_SCHEMA.instanceType = WorkspaceService.createInstanceType(workspace, ARTIFACT_SCHEMA.name, workspace.TYPES_FOLDER);
                cache.getPropertyAsMap(MAPPING).put(ARTIFACT_SCHEMA.name, Long.toString(type.id().value()));
                ARTIFACT_SCHEMA.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, ARTIFACT_SCHEMA.instanceType, SCHEMA_ID, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, ARTIFACT_SCHEMA.instanceType, ROOT, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, ARTIFACT_SCHEMA.instanceType, FIELD_TO_TYPE, Cardinality.MAP, Workspace.STRING);

                return type;
            } else {
                return ARTIFACT_SCHEMA.instanceType;
            }
        }
    }, UPDATE_MEMORY("update_memory") {
        @Override
        public InstanceType getType(){
            if (!UPDATE_MEMORY.isInitialized) {
                UPDATE_MEMORY.tryLoadingTypeDefinitions();
                if (UPDATE_MEMORY.isInitialized) return UPDATE_MEMORY.instanceType;

                InstanceType type = UPDATE_MEMORY.instanceType = WorkspaceService.createInstanceType(workspace, UPDATE_MEMORY.name, workspace.TYPES_FOLDER);
                cache.getPropertyAsMap(MAPPING).put(UPDATE_MEMORY.name, Long.toString(type.id().value()));
                UPDATE_MEMORY.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, UPDATE_MEMORY.instanceType, ID, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, UPDATE_MEMORY.instanceType, SERVICE_UPDATE_TIME, Cardinality.MAP, Workspace.STRING);

                return type;
            } else {
                return UPDATE_MEMORY.instanceType;
            }
        }
    };

    private final String name;
    private boolean isInitialized;
    private InstanceType instanceType;
    private static Instance cache;
    private static final String SUBTYPE_ID_CACHE = "generalSubTypeIdCacheId";

    private static Workspace workspace = null;
    BaseElementType(String name) {
        this.name = name;
        this.isInitialized = false;
        this.instanceType = null;
    }

    private Workspace getWorkspace() {
        Properties props;
        String workspaceName;

        try {
            props = PropertiesLoaderUtils.loadAllProperties("application.properties");
            workspaceName = props.getProperty("high_level_workspacename");
        } catch (IOException e) {
            log.debug("ARTIFACT-CONNECTOR : The application.properties file was not found in the resources folder of the main module");
            workspaceName = "high_level_default_workspace";
        }

        if(workspaceName==null) {
            return WorkspaceService.PUBLIC_WORKSPACE;
        } else {
            String finalWorkspaceName = workspaceName;
            Optional<Workspace> workspace_ = WorkspaceService.allWorkspaces().stream().filter(w -> w.name().equals(finalWorkspaceName)).findAny();
            if (!workspace_.isPresent()) {
                return WorkspaceService.PUBLIC_WORKSPACE;
            } else {
                return workspace_.get();
            }
        }
    }

    /**
     * try reloading already persisted instanceTpyes into the enumeration.
     */
    private void tryLoadingTypeDefinitions() {

        if (workspace == null) {
            workspace = getWorkspace();
        }

        if(cache==null) {

            //trying to find a cache
            for (Instance cur : workspace.debugInstances()) {
                Property property = cur.getProperty("id");
                if (property != null) {
                    if (property.get() != null && property.get().equals(SUBTYPE_ID_CACHE)) {
                        cache = cur;
                    }
                }
            }

            if (cache == null) {
                //no past cache has been found
                log.debug("ARTIFACT-CONNECTOR: No subtype id cache was found");
                cache = workspace.createInstance(BaseElementType.ELEMENT_ID_CACHE.getType(), SUBTYPE_ID_CACHE);
                cache.getPropertyAsSingle("id").set(SUBTYPE_ID_CACHE);
                for (BaseElementType type : BaseElementType.values()) {
                    cache.getPropertyAsMap(MAPPING).put(type.name, type.getType().id().toString());
                }
                workspace.concludeTransaction();
                log.debug("ARTIFACT-CONNECTOR: New subtype id cache has been created");
            } else {
                //past cache has been found
                for (BaseElementType type : BaseElementType.values()) {
                    Object object = cache.getPropertyAsMap(MAPPING).get(type.name);
                    if (object != null) {
                        Long id = Long.parseLong(object.toString());
                        Element element = workspace.findElement(Id.of(id));
                        if (element != null) {
                            type.instanceType = (InstanceType) element;
                            type.isInitialized = true;
                        }
                    }
                }
                log.debug("ARTIFACT-CONNECTOR: Successfully reconnected to subtype id cache");
            }
        }
    }

    public final static String
            KEY = "key", NAME = "name", ID = "id", ISSUE_TYPE = "issueType", ARRAY_ = "array",
            SOURCE_ROLE = "sourceRole", DESTINATION_ROLE = "destinationRole", SOURCE = "source",
            LINKS_OUTGOING = "linksOutgoing", SCHEMA_ID = "schemaId", FULLY_FETCHED = "fullyFetched",
            MAPPING = "map", FIELD_TO_TYPE = "fieldToType", ROOT = "root", LAST_UPDATE = "lastUpdate",
            TARGET = "target", SERVICE = "service", LINKS_INCOMING = "linksIncoming",
            SERVICE_UPDATE_TIME = "serviceUpdateTime", LINK = "html_url";

}


