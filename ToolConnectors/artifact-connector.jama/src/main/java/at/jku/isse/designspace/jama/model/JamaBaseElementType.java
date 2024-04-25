package at.jku.isse.designspace.jama.model;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import org.springframework.core.io.support.PropertiesLoaderUtils;

import at.jku.isse.designspace.artifactconnector.core.converter.IElementTypeGetter;
import at.jku.isse.designspace.artifactconnector.core.model.BaseElementType;
import at.jku.isse.designspace.core.model.Cardinality;
import at.jku.isse.designspace.core.model.Element;
import at.jku.isse.designspace.core.model.Folder;
import at.jku.isse.designspace.core.model.Id;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.Property;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.service.WorkspaceService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum JamaBaseElementType implements IElementTypeGetter {

    JAMA_CORE_ITEM("jama_item") {
        @Override
        public InstanceType getType() {
            if(!JAMA_CORE_ITEM.isInitialized) {
                tryLoadingTypeDefinitions();
                if (JAMA_CORE_ITEM.isInitialized) return JAMA_CORE_ITEM.instanceType;

                InstanceType type = JAMA_CORE_ITEM.instanceType = WorkspaceService.createInstanceType(workspace, JAMA_CORE_ITEM.name, jamaTypeFolder, BaseElementType.ARTIFACT.getType());
                cache.getPropertyAsMap(MAPPING).put(JAMA_CORE_ITEM.name, Long.toString(type.id().value()));
                JAMA_CORE_ITEM.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, JAMA_CORE_ITEM.instanceType, GLOBAL_ID, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JAMA_CORE_ITEM.instanceType, CREATED_DATE, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JAMA_CORE_ITEM.instanceType, MODIFIED_DATE, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JAMA_CORE_ITEM.instanceType, LAST_ACTIVITY, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JAMA_CORE_ITEM.instanceType, DESCRIPTION, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JAMA_CORE_ITEM.instanceType, ITEM_TYPE_NAME, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JAMA_CORE_ITEM.instanceType, ITEM_TYPE_SHORT, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JAMA_CORE_ITEM.instanceType, STATUS, Cardinality.SINGLE, Workspace.STRING);

                WorkspaceService.createPropertyType(workspace, JAMA_CORE_ITEM.instanceType, CREATED_BY, Cardinality.SINGLE, JamaBaseElementType.JAMA_USER.getType());
                WorkspaceService.createPropertyType(workspace, JAMA_CORE_ITEM.instanceType, MODIFIED_BY, Cardinality.SINGLE, JamaBaseElementType.JAMA_USER.getType());
                WorkspaceService.createPropertyType(workspace, JAMA_CORE_ITEM.instanceType, PROJECT, Cardinality.SINGLE, JamaBaseElementType.JAMA_PROJECT.getType());
                WorkspaceService.createPropertyType(workspace, JAMA_CORE_ITEM.instanceType, ITEM_TYPE, Cardinality.SINGLE, JamaBaseElementType.JAMA_ITEM_TYPE.getType());

                WorkspaceService.createOpposablePropertyType(workspace, JAMA_CORE_ITEM.instanceType, UPSTREAM, Cardinality.SET, JAMA_CORE_ITEM.instanceType, DOWNSTREAM, Cardinality.SET);

                return type;
            } else {
                return JAMA_CORE_ITEM.instanceType;
            }
        }

    }, JAMA_USER("jama_user") {
        @Override
        public InstanceType getType() {
            if(!JAMA_USER.isInitialized) {
                tryLoadingTypeDefinitions();
                if (JAMA_USER.isInitialized) return JAMA_USER.instanceType;

                InstanceType type = JAMA_USER.instanceType = WorkspaceService.createInstanceType(workspace, JAMA_USER.name, jamaTypeFolder, BaseElementType.ARTIFACT.getType());
                cache.getPropertyAsMap(MAPPING).put(JAMA_USER.name, Long.toString(type.id().value()));
                JAMA_USER.isInitialized = true;

                //TODO: Complete the USER type definition
                WorkspaceService.createPropertyType(workspace, JAMA_USER.instanceType, USER_ID, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JAMA_USER.instanceType, USERNAME, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JAMA_USER.instanceType, FIRST_NAME, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JAMA_USER.instanceType, LAST_NAME, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JAMA_USER.instanceType, EMAIL, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JAMA_USER.instanceType, PHONE, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JAMA_USER.instanceType, TITLE, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JAMA_USER.instanceType, LOCATION, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JAMA_USER.instanceType, TITLE, Cardinality.SINGLE, Workspace.STRING);

                return type;
            } else {
                return JAMA_USER.instanceType;
            }
        }

    }, JAMA_RELEASE("jama_release") {
        @Override
        public InstanceType getType() {
            if(!JAMA_RELEASE.isInitialized) {
                tryLoadingTypeDefinitions();
                if (JAMA_RELEASE.isInitialized) return JAMA_RELEASE.instanceType;

                InstanceType type = JAMA_RELEASE.instanceType = WorkspaceService.createInstanceType(workspace, JAMA_RELEASE.name, jamaTypeFolder, BaseElementType.ARTIFACT.getType());
                cache.getPropertyAsMap(MAPPING).put(JAMA_RELEASE.name, Long.toString(type.id().value()));
                JAMA_RELEASE.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, JAMA_RELEASE.instanceType, ARCHIVED, Cardinality.SINGLE, Workspace.BOOLEAN);
                WorkspaceService.createPropertyType(workspace, JAMA_RELEASE.instanceType, RELEASE_DATE, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JAMA_RELEASE.instanceType, DESCRIPTION, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JAMA_RELEASE.instanceType, TYPE, Cardinality.SINGLE, Workspace.STRING);

                return type;
            } else {
                return JAMA_RELEASE.instanceType;
            }
        }

    }, JAMA_ITEM_TYPE("jama_item_type") {
        @Override
        public InstanceType getType() {
            if(!JAMA_ITEM_TYPE.isInitialized) {
                tryLoadingTypeDefinitions();
                if (JAMA_ITEM_TYPE.isInitialized) return JAMA_ITEM_TYPE.instanceType;

                InstanceType type = JAMA_ITEM_TYPE.instanceType = WorkspaceService.createInstanceType(workspace, JAMA_ITEM_TYPE.name, jamaTypeFolder, BaseElementType.ARTIFACT.getType());
                cache.getPropertyAsMap(MAPPING).put(JAMA_ITEM_TYPE.name, Long.toString(type.id().value()));
                JAMA_ITEM_TYPE.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, JAMA_ITEM_TYPE.instanceType, DISPLAY, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JAMA_ITEM_TYPE.instanceType, TYPE_KEY, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JAMA_ITEM_TYPE.instanceType, DESCRIPTION, Cardinality.SINGLE, Workspace.STRING);

                return type;
            } else {
                return JAMA_ITEM_TYPE.instanceType;
            }
        }

    },  JAMA_PROJECT("jama_project") {
        @Override
        public InstanceType getType() {
            if (!JAMA_PROJECT.isInitialized) {
                tryLoadingTypeDefinitions();
                if (JAMA_PROJECT.isInitialized) return JAMA_PROJECT.instanceType;

                InstanceType type = JAMA_PROJECT.instanceType = WorkspaceService.createInstanceType(workspace, JAMA_PROJECT.name, jamaTypeFolder, BaseElementType.ARTIFACT.getType());
                cache.getPropertyAsMap(MAPPING).put(JAMA_PROJECT.name, Long.toString(type.id().value()));
                JAMA_PROJECT.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, JAMA_PROJECT.instanceType, UPDATED_ON, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JAMA_PROJECT.instanceType, MODIFIED_DATE, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JAMA_PROJECT.instanceType, CREATED_DATE, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JAMA_PROJECT.instanceType, DESCRIPTION, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JAMA_PROJECT.instanceType, STATUS, Cardinality.SINGLE, Workspace.STRING);

                WorkspaceService.createPropertyType(workspace, JAMA_PROJECT.instanceType, PROJECT_MANAGER, Cardinality.SINGLE, JamaBaseElementType.JAMA_USER.getType());
                WorkspaceService.createPropertyType(workspace, JAMA_PROJECT.instanceType, CREATED_BY, Cardinality.SINGLE, JamaBaseElementType.JAMA_USER.getType());
                WorkspaceService.createPropertyType(workspace, JAMA_PROJECT.instanceType, MODIFIED_BY, Cardinality.SINGLE, JamaBaseElementType.JAMA_USER.getType());
                WorkspaceService.createPropertyType(workspace, JAMA_PROJECT.instanceType, LAST_MODIFIED_BY, Cardinality.SINGLE, JamaBaseElementType.JAMA_USER.getType());
                WorkspaceService.createPropertyType(workspace, JAMA_PROJECT.instanceType, PROJECT__GROUP, Cardinality.SINGLE, JamaBaseElementType.PROJECT_GROUP.getType());
                WorkspaceService.createPropertyType(workspace, JAMA_PROJECT.instanceType, PARENT_PROJECT, Cardinality.SINGLE, JamaBaseElementType.JAMA_PROJECT.getType());

                return type;
            } else {
                return JAMA_PROJECT.instanceType;
            }
        }
    }, PROJECT_GROUP("jama_project_group") {
        @Override
        public InstanceType getType() {
            if (!PROJECT_GROUP.isInitialized) {
                tryLoadingTypeDefinitions();
                if (PROJECT_GROUP.isInitialized) return PROJECT_GROUP.instanceType;

                InstanceType type = PROJECT_GROUP.instanceType = WorkspaceService.createInstanceType(workspace, PROJECT_GROUP.name, jamaTypeFolder, BaseElementType.ARTIFACT.getType());
                cache.getPropertyAsMap(MAPPING).put(PROJECT_GROUP.name, Long.toString(type.id().value()));
                PROJECT_GROUP.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, PROJECT_GROUP.instanceType, PROJECTS, Cardinality.LIST, JamaBaseElementType.JAMA_PROJECT.getType());

                return type;
            } else {
                return PROJECT_GROUP.instanceType;
            }
        }
    }, JAMA_PICKLIST_OPTION("jama_picklist_option") {
        @Override
        public InstanceType getType() {
            if (!JAMA_PICKLIST_OPTION.isInitialized) {
                tryLoadingTypeDefinitions();
                if (JAMA_PICKLIST_OPTION.isInitialized) return JAMA_PICKLIST_OPTION.instanceType;

                InstanceType type = JAMA_PICKLIST_OPTION.instanceType = WorkspaceService.createInstanceType(workspace, JAMA_PICKLIST_OPTION.name, jamaTypeFolder);
                cache.getPropertyAsMap(MAPPING).put(JAMA_PICKLIST_OPTION.name, Long.toString(type.id().value()));
                JAMA_PICKLIST_OPTION.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, JAMA_PICKLIST_OPTION.instanceType, PICKLIST, Cardinality.SINGLE, Workspace.INTEGER);
                WorkspaceService.createPropertyType(workspace, JAMA_PICKLIST_OPTION.instanceType, VALUE, Cardinality.SINGLE, Workspace.STRING);

                return type;
            } else {
                return JAMA_PICKLIST_OPTION.instanceType;
            }
        }
    }, JAMA_MULTI_PICKLIST_OPTION("jama_multi_picklist_option") {
        @Override
        public InstanceType getType() {
            if (!JAMA_PICKLIST_OPTION.isInitialized) {
                tryLoadingTypeDefinitions();
                if (JAMA_PICKLIST_OPTION.isInitialized) return JAMA_PICKLIST_OPTION.instanceType;

                InstanceType type = JAMA_PICKLIST_OPTION.instanceType = WorkspaceService.createInstanceType(workspace, JAMA_PICKLIST_OPTION.name, jamaTypeFolder);
                cache.getPropertyAsMap(MAPPING).put(JAMA_PICKLIST_OPTION.name, Long.toString(type.id().value()));
                JAMA_PICKLIST_OPTION.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, JAMA_PICKLIST_OPTION.instanceType, PICKLIST, Cardinality.SINGLE, Workspace.INTEGER);
                WorkspaceService.createPropertyType(workspace, JAMA_PICKLIST_OPTION.instanceType, VALUE, Cardinality.LIST, Workspace.STRING);

                return type;
            } else {
                return JAMA_PICKLIST_OPTION.instanceType;
            }
        }
    };

    private String name;
    private boolean isInitialized;
    private InstanceType instanceType;

    private static Folder jamaTypeFolder;
    private static boolean loadedFromPersistence = false;
    private static Instance cache;
    private static Workspace workspace = null;

    private static final String JAMA_TYPES_FOLDER_NAME = "jama";
    private static final String JAMA_SUB_TYPE_ID_CACHE_ID = "jamaSubTypeIdCacheId";

    JamaBaseElementType(String name) {
        this.name = name;
        this.isInitialized = false;
        this.instanceType = null;
    }
    
    public String getDesignSpaceShortTypeName() {
        return this.name;
    }

    public static boolean wasLoadedByPersistence() {
        tryLoadingTypeDefinitions();
        return loadedFromPersistence;
    }

    public static Folder getJamaTypeFolder() {
        tryLoadingTypeDefinitions();
        return jamaTypeFolder;
    }

    private static void tryLoadingTypeDefinitions() {

        if (workspace == null) {
            //loading config
            Properties props;
            String workspaceName;

            try {
                props = PropertiesLoaderUtils.loadAllProperties("application.properties");
                workspaceName = props.getProperty("jama_workspace_name");
            } catch (IOException e) {
                log.debug("JAMA-SERVICE : The application.properties file was not found in the resources folder of the main module");
                workspaceName = "jama_default_workspace";
            }

            if(workspaceName==null) {
                workspace = WorkspaceService.PUBLIC_WORKSPACE;
            } else {
                String finalWorkspaceName = workspaceName;
                Optional<Workspace> workspace_ = WorkspaceService.allWorkspaces().stream().filter(w -> w.name().equals(finalWorkspaceName)).findAny();
                workspace = workspace_.orElseGet(() -> WorkspaceService.PUBLIC_WORKSPACE);
            }

            //trying to find the jama type folder
            Folder typesFolder = workspace.TYPES_FOLDER;
            JamaBaseElementType.jamaTypeFolder = typesFolder.subfolder(JAMA_TYPES_FOLDER_NAME);

            //creating the folder should it not exist
            if (jamaTypeFolder == null) {
                JamaBaseElementType.jamaTypeFolder = WorkspaceService.createSubfolder(workspace, workspace.TYPES_FOLDER, JAMA_TYPES_FOLDER_NAME);
            }

        }

        if(cache==null) {

            //trying to find a cache
            for (Instance cur : workspace.debugInstances()) {
                Property<?> property = cur.getProperty("id");
                if (property != null) {
                    if (property.get() != null && property.get().equals(JAMA_SUB_TYPE_ID_CACHE_ID)) {
                        cache = cur;
                    }
                }
            }

            if (cache == null) {
                //no past cache has been found
                log.debug("JAMA-SERVICE: No subtype id cache was found");
                cache = workspace.createInstance(BaseElementType.ELEMENT_ID_CACHE.getType(), JAMA_SUB_TYPE_ID_CACHE_ID);
                cache.getPropertyAsSingle("id").set(JAMA_SUB_TYPE_ID_CACHE_ID);
                for (JamaBaseElementType type : JamaBaseElementType.values()) {
                    cache.getPropertyAsMap(MAPPING).put(type.name, type.getType().id().toString());
                }
                workspace.concludeTransaction();
                log.debug("JAMA-SERVICE: New subtype id cache has been created");
            } else {
                //past cache has been found
                for (JamaBaseElementType type : JamaBaseElementType.values()) {
                    Object object = cache.getPropertyAsMap(MAPPING).get(type.name);
                    if (object != null) {
                        long id = Long.parseLong(object.toString());
                        Element<?> element = workspace.findElement(Id.of(id));
                        if (element != null) {
                            type.instanceType = (InstanceType) element;
                            type.isInitialized = true;
                        }
                    }
                }
                loadedFromPersistence = true;
                log.debug("JAMA-SERVICE: Successfully reconnected to subtype id cache");
            }
        }
    }

    public final static String DESCRIPTION = "description", //ID = "id",
    		MAPPING = "map", USER_ID = "userId", DOCUMENTKEY = "documentKey",
            ITEM_TYPE = "itemType", GLOBAL_ID = "globalId", CREATED_DATE = "createdDate", STATUS = "status", VALUE = "value",
            PROJECT = "project", LAST_ACTIVITY = "lastActivityDate", UPSTREAM = "upstream", DOWNSTREAM = "downstream",
            CREATED_BY = "createdByJamaUser", UPDATED_ON = "updatedOn", SYSTEM_TYPE = "system", LABEL = "label", PICKLIST = "picklist",
            TYPE_NAME = "fieldType", PROJECT_MANAGER = "projectManager", PROJECT__GROUP = "projectGroup", LAST_MODIFIED_BY = "lastModifiedBy",
            PARENT_PROJECT = "parentProject", MODIFIED_DATE = "lastModifiedDate", PROJECTS = "projects", ITEM_TYPE_NAME = "typeName", ITEM_TYPE_SHORT = "typeKey",
            FIRST_NAME = "firstName", LAST_NAME = "lastName", EMAIL = "email", PHONE = "phone", TITLE = "title", LOCATION = "location",
            USERNAME = "username", MODIFIED_BY = "modifiedByJamaUser", DISPLAY = "display", TYPE_KEY = "typeKey", FIELDS = "fields",
            ARCHIVED = "archived", RELEASE_DATE = "releaseDate", TYPE = "type", PROJECT_KEY = "projectKey", NAME = "name";

}


