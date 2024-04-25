package at.jku.isse.designspace.azure.model;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import org.springframework.core.io.support.PropertiesLoaderUtils;

import at.jku.isse.designspace.core.model.Cardinality;
import at.jku.isse.designspace.core.model.Element;
import at.jku.isse.designspace.core.model.Folder;
import at.jku.isse.designspace.core.model.Id;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.Property;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.service.WorkspaceService;
import at.jku.isse.designspace.rule.checker.ConsistencyUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum AzureBaseElementType implements IElementTypeGetter {

    AZURE_WORKITEM("azure_workitem") {
        @Override
        public InstanceType getType() {
            if(!AZURE_WORKITEM.isInitialized) {
                AZURE_WORKITEM.tryLoadingTypeDefinitions();
                if(AZURE_WORKITEM.isInitialized) return AZURE_WORKITEM.instanceType;

                InstanceType type = AZURE_WORKITEM.instanceType = WorkspaceService.createInstanceType(workspace, AZURE_WORKITEM.name, typeFolder);
                cache.getPropertyAsMap(MAPPING).put(AZURE_WORKITEM.name, Long.toString(type.id().value()));
                AZURE_WORKITEM.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, AZURE_WORKITEM.instanceType, ID, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, AZURE_WORKITEM.instanceType, URL, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, AZURE_WORKITEM.instanceType, FULLY_FETCHED, Cardinality.SINGLE, Workspace.BOOLEAN);
                WorkspaceService.createPropertyType(workspace, AZURE_WORKITEM.instanceType, DELETED, Cardinality.SINGLE, Workspace.BOOLEAN);
                WorkspaceService.createPropertyType(workspace, AZURE_WORKITEM.instanceType, TITLE, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, AZURE_WORKITEM.instanceType, WITEM_DESCRIPTION, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, AZURE_WORKITEM.instanceType, PRIORITY, Cardinality.SINGLE, Workspace.INTEGER);
                WorkspaceService.createPropertyType(workspace, AZURE_WORKITEM.instanceType, CATEGORY, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, AZURE_WORKITEM.instanceType, PROJECT, Cardinality.SINGLE, AzureBaseElementType.AZURE_PROJECT.getType());

                WorkspaceService.createPropertyType(workspace, AZURE_WORKITEM.instanceType, WITEM_TYPE, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, AZURE_WORKITEM.instanceType, STATE, Cardinality.SINGLE, Workspace.STRING);

                WorkspaceService.createPropertyType(workspace, AZURE_WORKITEM.instanceType, ASSIGNEE, Cardinality.SINGLE, AzureBaseElementType.AZURE_USER.getType());
                WorkspaceService.createPropertyType(workspace, AZURE_WORKITEM.instanceType, CREATOR, Cardinality.SINGLE, AzureBaseElementType.AZURE_USER.getType());

                WorkspaceService.createPropertyType(workspace, AZURE_WORKITEM.instanceType, REVIEW_CRITERIA, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, AZURE_WORKITEM.instanceType, REVIEW_FINDINGS, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, AZURE_WORKITEM.instanceType, REVIEW_MANDASSIGNEE1, Cardinality.SINGLE, AzureBaseElementType.AZURE_USER.getType());
                WorkspaceService.createPropertyType(workspace, AZURE_WORKITEM.instanceType, REVIEW_SCHEDULEDDATE, Cardinality.SINGLE, Workspace.STRING);
                
                WorkspaceService.createPropertyType(workspace, AZURE_WORKITEM.instanceType, VERIFICATION_CRITERIA, Cardinality.SINGLE, Workspace.STRING);
                
                linkId2name.values().stream()
                	.map(lname -> convertLinkTypeNameToProperty(lname) )
                	.filter(Objects::nonNull)
                	.forEach(propname -> {
                		WorkspaceService.createPropertyType(workspace, AZURE_WORKITEM.instanceType, propname, Cardinality.SET, type); 
                	});
                
                // Related work items
               // WorkspaceService.createPropertyType(workspace, AZURE_WORKITEM.instanceType, RELATED_ITEMS, Cardinality.LIST, AZURE_WORKITEM_LINK.getType());

                // Work item comments
                WorkspaceService.createPropertyType(workspace, AZURE_WORKITEM.instanceType, COMMENTS, Cardinality.LIST, WORKITEM_COMMENT.getType());

                // non repairable properties
                ConsistencyUtils.setPropertyRepairable(AZURE_WORKITEM.instanceType, WITEM_TYPE, false);
                return type;
            }
            else {
                return AZURE_WORKITEM.instanceType;
            }
        }
    }, AZURE_USER("user") {
        @Override
        public InstanceType getType() {
            if(!AZURE_USER.isInitialized) {
                AZURE_USER.tryLoadingTypeDefinitions();
                if(AZURE_USER.isInitialized) return AZURE_USER.instanceType;

                InstanceType type = AZURE_USER.instanceType = WorkspaceService.createInstanceType(workspace, AZURE_USER.name, typeFolder);
                cache.getPropertyAsMap(MAPPING).put(AZURE_USER.name, Long.toString(type.id().value()));
                AZURE_USER.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, AZURE_USER.instanceType, EMAIL, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, AZURE_USER.instanceType, DISPLAY_NAME, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, AZURE_USER.instanceType, USER_DESCRIPTOR, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, AZURE_USER.instanceType, URL, Cardinality.SINGLE, Workspace.STRING);

                return type;
            } else {
                return AZURE_USER.instanceType;
            }
        }

    }, AZURE_PROJECT("project") {
        @Override
        public InstanceType getType() {
            if(!AZURE_PROJECT.isInitialized) {
                AZURE_PROJECT.tryLoadingTypeDefinitions();
                if(AZURE_PROJECT.isInitialized) return AZURE_PROJECT.instanceType;

                InstanceType type = AZURE_PROJECT.instanceType = WorkspaceService.createInstanceType(workspace, AZURE_PROJECT.name, typeFolder);
                cache.getPropertyAsMap(MAPPING).put(AZURE_PROJECT.name, Long.toString(type.id().value()));
                AZURE_PROJECT.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, AZURE_PROJECT.instanceType, ID, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, AZURE_PROJECT.instanceType, NAME, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, AZURE_PROJECT.instanceType, PROJECT_DESCRIPTION, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, AZURE_PROJECT.instanceType, URL, Cardinality.SINGLE, Workspace.STRING);

                return type;
            } else {
                return AZURE_PROJECT.instanceType;
            }
        }
    }, WORKITEM_COMMENT("workItem_comment") {
        @Override
        public InstanceType getType() {
            if(!WORKITEM_COMMENT.isInitialized) {
                WORKITEM_COMMENT.tryLoadingTypeDefinitions();
                if(WORKITEM_COMMENT.isInitialized) return WORKITEM_COMMENT.instanceType;

                InstanceType type = WORKITEM_COMMENT.instanceType = WorkspaceService.createInstanceType(workspace, WORKITEM_COMMENT.name, typeFolder);
                cache.getPropertyAsMap(MAPPING).put(WORKITEM_COMMENT.name, Long.toString(type.id().value()));
                WORKITEM_COMMENT.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, WORKITEM_COMMENT.instanceType, ID, Cardinality.SINGLE, Workspace.INTEGER);
                WorkspaceService.createPropertyType(workspace, WORKITEM_COMMENT.instanceType, WITEM_ID, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, WORKITEM_COMMENT.instanceType, COMMENT_TEXT, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, WORKITEM_COMMENT.instanceType, CREATOR, Cardinality.SINGLE, AzureBaseElementType.AZURE_USER.getType());
                WorkspaceService.createPropertyType(workspace, WORKITEM_COMMENT.instanceType, DATE_CREATED, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, WORKITEM_COMMENT.instanceType, URL, Cardinality.SINGLE, Workspace.STRING);

                return type;
            } else {
                return WORKITEM_COMMENT.instanceType;
            }
        }
    }, ELEMENT_ID_CACHE("artifact_id_cache") {
        @Override
        public InstanceType getType(){
            if (!ELEMENT_ID_CACHE.isInitialized) {
                if(workspace == null) {
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
    };

    private String name;
    private boolean isInitialized;
    private InstanceType instanceType;
    private static Workspace workspace = null;
    public static Folder typeFolder;
    private static Instance cache;
    private static final String AZURE_TYPES_ID_CACHE_ID = "azureTypesIdCacheId";
    public static final String AZURE_TYPES_FOLDER_NAME = "azure";
    private static boolean loadedFromPersistence = false;

    public final static String ID = "id", MAPPING = "map", URL = "html_url", FULLY_FETCHED = "fullyFetched",
    TITLE = "title", PRIORITY = "priority", PROJECT = "project", WITEM_TYPE = "workItemType", STATE = "state",
    ASSIGNEE = "assignedTo", CREATOR = "createdBy", COMMENTS = "comments",
    DISPLAY_NAME = "displayName", EMAIL = "emailAddress", WITEM_ID = "workItemId", COMMENT_TEXT = "text",
    DATE_CREATED = "createdDate", PROJECT_NAME = "name", PROJECT_DESCRIPTION = "description", NAME = "name",
    REFERENCE_NAME = "referenceName", 
    CATEGORY = "category", COLOR = "color",
    USER_DESCRIPTOR = "userDescriptor", WITEM_DESCRIPTION = "description", DELETED = "deleted",
    REVIEW_MANDASSIGNEE1 = "reviewAssignee1", REVIEW_SCHEDULEDDATE = "reviewScheduled", REVIEW_CRITERIA = "reviewCriteria",
    REVIEW_FINDINGS = "reviewFindings", VERIFICATION_CRITERIA = "verificationCriteria";

    AzureBaseElementType(String name) {
        this.name = name;
        this.isInitialized = false;
        this.instanceType = null;
    }

    private Workspace getWorkspace() {
        Properties props = new Properties();
        String workspaceName;

        try {
            props = PropertiesLoaderUtils.loadAllProperties("application.properties");
            workspaceName = props.getProperty("azure_workspace_name");
        } catch (IOException e) {
            log.debug("AZURE-SERVICE: The application.properties file was not found in the resources folder or the main module");
            workspaceName = "azure_default_workspace";
        }

        if(workspaceName == null) {
            return WorkspaceService.PUBLIC_WORKSPACE;
        } else {
            String finalWorkspaceName = workspaceName;
            Optional<Workspace> workspace_ = WorkspaceService.allWorkspaces().stream().filter(w -> w.name().equals(finalWorkspaceName)).findAny();
            if(workspace_.isPresent()) {
                return workspace_.get();
            } else {
                return WorkspaceService.PUBLIC_WORKSPACE;
            }
        }
    }
    
    public static String convertLinkTypeNameToProperty(String linkType) {
    	return linkType.replace(" ", "").toLowerCase()+"Items";
    }

    private void tryLoadingTypeDefinitions() {
        //find workspace
        if(workspace == null) {
            workspace = getWorkspace();
        }
        
        Folder typesFolder = workspace.TYPES_FOLDER;
        AzureBaseElementType.typeFolder = typesFolder.subfolder(AZURE_TYPES_FOLDER_NAME);
        //creating the folder should it not exist
        if (typeFolder == null) {
            AzureBaseElementType.typeFolder = WorkspaceService.createSubfolder(workspace, workspace.TYPES_FOLDER, AZURE_TYPES_FOLDER_NAME);
        }

        if(cache==null) {

            //trying to find a cache
            for (Instance cur : workspace.debugInstances()) {
                Property property = cur.getProperty("id");
                if (property != null) {
                    if (property.get() != null && property.get().equals(AZURE_TYPES_ID_CACHE_ID)) {
                        cache = cur;
                    }
                }
            }

            if (cache == null) {
                //no past cache has been found
                log.debug("AZURE-SERVICE: No subtype id cache was found");
                cache = workspace.createInstance(ELEMENT_ID_CACHE.getType(), AZURE_TYPES_ID_CACHE_ID);
                cache.getPropertyAsSingle("id").set(AZURE_TYPES_ID_CACHE_ID);
                for (AzureBaseElementType type : AzureBaseElementType.values()) {
                    cache.getPropertyAsMap(MAPPING).put(type.name, type.getType().id().toString());
                }
                workspace.concludeTransaction();
                log.debug("AZURE-SERVICE: New subtype id cache has been created");
            } else {
                //past cache has been found
                for (AzureBaseElementType type : AzureBaseElementType.values()) {
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
                loadedFromPersistence = true;
                log.debug("AZURE-SERVICE: Successfully reconnected to subtype id cache");
            }
        }
    }
    
    
    private static Map<String,String> linkId2name;
    
    public static void setLinkTypeMap(Map<String,String> id2name) {
    	linkId2name = id2name;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }
}
