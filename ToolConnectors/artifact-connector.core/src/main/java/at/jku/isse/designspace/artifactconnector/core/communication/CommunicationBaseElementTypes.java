package at.jku.isse.designspace.artifactconnector.core.communication;

import at.jku.isse.designspace.artifactconnector.core.converter.IElementTypeGetter;
import at.jku.isse.designspace.artifactconnector.core.model.BaseElementType;
import at.jku.isse.designspace.core.model.Cardinality;
import at.jku.isse.designspace.core.model.Element;
import at.jku.isse.designspace.core.model.Id;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.Property;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.service.WorkspaceService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum CommunicationBaseElementTypes implements IElementTypeGetter {

    FETCH_REQUEST("fetch_request") {
        @Override
        public InstanceType getType() {

            FETCH_REQUEST.tryLoadingTypeDefinitions();
            if(!FETCH_REQUEST.isInitialized) {
                InstanceType type = FETCH_REQUEST.instanceType = WorkspaceService.createInstanceType(workspace, FETCH_REQUEST.name, workspace.TYPES_FOLDER);
                FETCH_REQUEST.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, FETCH_REQUEST.instanceType, FETCH_ID, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, FETCH_REQUEST.instanceType, SERVICE, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, FETCH_REQUEST.instanceType, ARTIACT_IDENTIFIER, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, FETCH_REQUEST.instanceType, PROJECT_ID, Cardinality.SINGLE, Workspace.STRING);

                return type;
            } else {
                return FETCH_REQUEST.instanceType;
            }
        }
    }, SUCCESS_RESPONSE("success_response") {
        @Override
        public InstanceType getType() {
            SUCCESS_RESPONSE.tryLoadingTypeDefinitions();
            if(!SUCCESS_RESPONSE.isInitialized) {
                InstanceType type = SUCCESS_RESPONSE.instanceType = WorkspaceService.createInstanceType(workspace, SUCCESS_RESPONSE.name, workspace.TYPES_FOLDER);
                SUCCESS_RESPONSE.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, SUCCESS_RESPONSE.instanceType, FETCH_ID, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, SUCCESS_RESPONSE.instanceType, ARTIACT_IDENTIFIER, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, SUCCESS_RESPONSE.instanceType, SERVICE, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, SUCCESS_RESPONSE.instanceType, ARTIFACT, Cardinality.SINGLE, BaseElementType.ARTIFACT.getType());

                return type;
            } else {
                return SUCCESS_RESPONSE.instanceType;
            }
        }
    }, SERVICE_UNAVAILABLE_RESPONSE("service_unavailable_response") {
        @Override
        public InstanceType getType() {

            SERVICE_UNAVAILABLE_RESPONSE.tryLoadingTypeDefinitions();
            if(!SERVICE_UNAVAILABLE_RESPONSE.isInitialized) {
                InstanceType type = SERVICE_UNAVAILABLE_RESPONSE.instanceType = WorkspaceService.createInstanceType(workspace, SERVICE_UNAVAILABLE_RESPONSE.name, workspace.TYPES_FOLDER);
                SERVICE_UNAVAILABLE_RESPONSE.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, SERVICE_UNAVAILABLE_RESPONSE.instanceType, MESSAGE, Cardinality.SINGLE, Workspace.STRING);

                return type;
            } else {
                return SERVICE_UNAVAILABLE_RESPONSE.instanceType;
            }
        }
    }, NOT_FOUND_RESPONSE("not_found_response") {
        @Override
        public InstanceType getType() {

            NOT_FOUND_RESPONSE.tryLoadingTypeDefinitions();
            if(!NOT_FOUND_RESPONSE.isInitialized) {
                InstanceType type = NOT_FOUND_RESPONSE.instanceType = WorkspaceService.createInstanceType(workspace, NOT_FOUND_RESPONSE.name, workspace.TYPES_FOLDER);
                NOT_FOUND_RESPONSE.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, NOT_FOUND_RESPONSE.instanceType, MESSAGE, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, NOT_FOUND_RESPONSE.instanceType, SERVICE, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, NOT_FOUND_RESPONSE.instanceType, ARTIACT_IDENTIFIER, Cardinality.SINGLE, Workspace.STRING);

                return type;
            } else {
                return NOT_FOUND_RESPONSE.instanceType;
            }
        }
    }, TIMOUT_RESPONSE("timeout_response") {
        @Override
        public InstanceType getType() {

            TIMOUT_RESPONSE.tryLoadingTypeDefinitions();
            if(!TIMOUT_RESPONSE.isInitialized) {
                InstanceType type = TIMOUT_RESPONSE.instanceType = WorkspaceService.createInstanceType(workspace, TIMOUT_RESPONSE.name, workspace.TYPES_FOLDER);
                TIMOUT_RESPONSE.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, TIMOUT_RESPONSE.instanceType, FETCH_ID, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, TIMOUT_RESPONSE.instanceType, SERVICE, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, TIMOUT_RESPONSE.instanceType, ARTIACT_IDENTIFIER, Cardinality.SINGLE, Workspace.STRING);

                return type;
            } else {
                return TIMOUT_RESPONSE.instanceType;
            }
        }
    }, ELEMENT_ID_CACHE("idCache") {
        @Override
        public InstanceType getType() {
            if (!ELEMENT_ID_CACHE.isInitialized) {
                InstanceType type = ELEMENT_ID_CACHE.instanceType = WorkspaceService.createInstanceType(workspace, ELEMENT_ID_CACHE.name, workspace.TYPES_FOLDER);
                ELEMENT_ID_CACHE.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, ELEMENT_ID_CACHE.instanceType, ID, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, ELEMENT_ID_CACHE.instanceType, MAPPING, Cardinality.MAP, Workspace.STRING);

                return type;
            } else {
                return ELEMENT_ID_CACHE.instanceType;
            }
        }
    };

    private String name;
    private boolean isInitialized;
    private InstanceType instanceType;
    public final static String TYPENAME_PREFIX = "polarion_";
    private static Workspace workspace = WorkspaceService.PUBLIC_WORKSPACE;
    private static Instance cache;
    private static final String SUBTYPE_ID_CACHE = "communicationSubTypeIdCacheId";

    CommunicationBaseElementTypes(String name) {
        this.name = name;
        this.isInitialized = false;
        this.instanceType = null;
    }

    public static void initAll() {
        for (CommunicationBaseElementTypes type : CommunicationBaseElementTypes.values()) {
            type.getType();
        }
       // workspace.concludeTransaction(); IMPORTANT: this is called from the init services which does transaction conclusion itself, hence must not be called here.
    }

    private void tryLoadingTypeDefinitions() {
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
                log.debug("ARTIFACT-CONNECTOR: No communication subtype id cache was found");
                cache = workspace.createInstance(CommunicationBaseElementTypes.ELEMENT_ID_CACHE.getType(), SUBTYPE_ID_CACHE);
                cache.getPropertyAsSingle("id").set(SUBTYPE_ID_CACHE);
                for (CommunicationBaseElementTypes type : CommunicationBaseElementTypes.values()) {
                    cache.getPropertyAsMap("map").put(type.name, type.getType().id().toString());
                }
                workspace.concludeTransaction();
                log.debug("ARTIFACT-CONNECTOR: New communication subtype id cache has been created");
            } else {
                //past cache has been found
                for (CommunicationBaseElementTypes type : CommunicationBaseElementTypes.values()) {
                    Object object = cache.getPropertyAsMap("map").get(type.name);
                    if (object != null) {
                        Long id = Long.parseLong(object.toString());
                        Element element = workspace.findElement(Id.of(id));
                        if (element != null) {
                            type.instanceType = (InstanceType) element;
                            type.isInitialized = true;
                        }
                    }
                }
                log.debug("ARTIFACT-CONNECTOR: Successfully reconnected to communication subtype id cache");
            }
        }
    }

    public final static String
            ID="id", MAPPING="map", FETCH_ID="fetchId", ARTIACT_IDENTIFIER="artifactIdentifier", SERVICE="service", MESSAGE="message", PROJECT_ID="projectId", ARTIFACT="artifact";
}
