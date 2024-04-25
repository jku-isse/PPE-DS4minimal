package at.jku.isse.designspace.jira.model;

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
import at.jku.isse.designspace.core.model.MapProperty;
import at.jku.isse.designspace.core.model.Property;
import at.jku.isse.designspace.core.model.ReservedNames;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.service.WorkspaceService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum JiraBaseElementType implements IElementTypeGetter {

	
	
    /**
     *
     * future aritfact types and their subtypes are to be defined here
     * in case of item having relationships
     *
     */

    JIRA_ARTIFACT("jira_core") {
        @Override
        public InstanceType getType() {
            if(!JIRA_ARTIFACT.isInitialized) {
                JIRA_ARTIFACT.tryLoadingTypeDefinitions();
                if (JIRA_ARTIFACT.isInitialized) return JIRA_ARTIFACT.instanceType;

                InstanceType type = JIRA_ARTIFACT.instanceType = WorkspaceService.createInstanceType(workspace, JIRA_ARTIFACT.name, workspace.TYPES_FOLDER, BaseElementType.ARTIFACT.getType());
                cache.getPropertyAsMap(MAPPING).put(JIRA_ARTIFACT.name, Long.toString(type.id().value()));
                JIRA_ARTIFACT.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, JIRA_ARTIFACT.instanceType, DESCRIPTION, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JIRA_ARTIFACT.instanceType, STATUS_, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JIRA_ARTIFACT.instanceType, ISSUE_TYPE, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JIRA_ARTIFACT.instanceType, PRIORITY_, Cardinality.SINGLE, Workspace.STRING);

                WorkspaceService.createPropertyType(workspace, JIRA_ARTIFACT.instanceType, CREATOR, Cardinality.SINGLE, JiraBaseElementType.JIRA_USER.getType());
                WorkspaceService.createPropertyType(workspace, JIRA_ARTIFACT.instanceType, PROJECT_, Cardinality.SINGLE, JiraBaseElementType.JIRA_PROJECT.getType());
                WorkspaceService.createPropertyType(workspace, JIRA_ARTIFACT.instanceType, ASSIGNEE, Cardinality.SINGLE, JiraBaseElementType.JIRA_USER.getType());
                WorkspaceService.createPropertyType(workspace, JIRA_ARTIFACT.instanceType, REPORTER, Cardinality.SINGLE, JiraBaseElementType.JIRA_USER.getType());
              // WorkspaceService.createPropertyType(workspace, JIRA_ARTIFACT.instanceType, PARENT, Cardinality.SINGLE, JIRA_ARTIFACT.instanceType);

                WorkspaceService.createPropertyType(workspace, JIRA_ARTIFACT.instanceType, LINKS_OUTGOING, Cardinality.SET, BaseElementType.ARTIFACT.getType());
                WorkspaceService.createPropertyType(workspace, JIRA_ARTIFACT.instanceType, LINKS_INCOMING, Cardinality.SET, BaseElementType.ARTIFACT.getType());
                WorkspaceService.createOpposablePropertyType(workspace, JIRA_ARTIFACT.instanceType, SUBTASKS, Cardinality.SET, JIRA_ARTIFACT.instanceType, PARENT, Cardinality.SINGLE);
                WorkspaceService.createOpposablePropertyType(workspace, JIRA_ARTIFACT.instanceType, EPICCHILDREN, Cardinality.SET, JIRA_ARTIFACT.instanceType, EPICPARENT, Cardinality.SINGLE);

                return type;
            } else {
                return JIRA_ARTIFACT.instanceType;
            }
        }

    }, JIRA_USER("user") {
        @Override
        public InstanceType getType() {
            if(!JIRA_USER.isInitialized) {
                JIRA_USER.tryLoadingTypeDefinitions();
                if (JIRA_USER.isInitialized) return JIRA_USER.instanceType;

                InstanceType type = JIRA_USER.instanceType = WorkspaceService.createInstanceType(workspace, JIRA_USER.name, workspace.TYPES_FOLDER);
                cache.getPropertyAsMap(MAPPING).put(JIRA_USER.name, Long.toString(type.id().value()));
                JIRA_USER.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, JIRA_USER.instanceType, ID, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JIRA_USER.instanceType, KEY, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JIRA_USER.instanceType, NAME, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JIRA_USER.instanceType, ADDRESS, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JIRA_USER.instanceType, DISPLAY_NAME, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JIRA_USER.instanceType, LINK, Cardinality.SINGLE, Workspace.STRING);

                MapProperty<String> propertyMetadata = JIRA_USER.instanceType.getPropertyAsMap(ReservedNames.INSTANCETYPE_PROPERTY_METADATA);
                propertyMetadata.put(JiraSchemaConverter.RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE + ID, ID);
                propertyMetadata.put(JiraSchemaConverter.RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE + KEY, KEY);
                propertyMetadata.put(JiraSchemaConverter.RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE + ADDRESS, ADDRESS);
                propertyMetadata.put(JiraSchemaConverter.RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE + "self", LINK);
                propertyMetadata.put(JiraSchemaConverter.RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE + NAME, NAME);
                propertyMetadata.put(JiraSchemaConverter.RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE + DISPLAY_NAME, DISPLAY_NAME);

                return type;
            } else {
                return JIRA_USER.instanceType;
            }
        }

    }, JIRA_PROJECT("project") {
        @Override
        public InstanceType getType() {
            if(!JIRA_PROJECT.isInitialized) {
                JIRA_PROJECT.tryLoadingTypeDefinitions();
                if (JIRA_PROJECT.isInitialized) return JIRA_PROJECT.instanceType;

                InstanceType type = JIRA_PROJECT.instanceType = WorkspaceService.createInstanceType(workspace, JIRA_PROJECT.name, workspace.TYPES_FOLDER);
                cache.getPropertyAsMap(MAPPING).put(JIRA_PROJECT.name, Long.toString(type.id().value()));
                JIRA_PROJECT.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, JIRA_PROJECT.instanceType, ID, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JIRA_PROJECT.instanceType, KEY, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JIRA_PROJECT.instanceType, DESCRIPTION, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JIRA_PROJECT.instanceType, LINK, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JIRA_PROJECT.instanceType, NAME, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JIRA_PROJECT.instanceType, PROJECT_TYPE_KEY, Cardinality.SINGLE, Workspace.STRING);

                MapProperty<String> propertyMetadata = JIRA_PROJECT.instanceType.getPropertyAsMap(ReservedNames.INSTANCETYPE_PROPERTY_METADATA);
                propertyMetadata.put(JiraSchemaConverter.RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE + ID, ID);
                propertyMetadata.put(JiraSchemaConverter.RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE + KEY, KEY);
                propertyMetadata.put(JiraSchemaConverter.RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE + DESCRIPTION, DESCRIPTION);
                propertyMetadata.put(JiraSchemaConverter.RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE + "self", LINK);
                propertyMetadata.put(JiraSchemaConverter.RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE + NAME, NAME);
                propertyMetadata.put(JiraSchemaConverter.RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE + PROJECT_TYPE_KEY, PROJECT_TYPE_KEY);

                return type;
            } else {
                return JIRA_PROJECT.instanceType;
            }
        }
    }, JIRA_RESOLUTION("resolution") {
        @Override
        public InstanceType getType() {
            if (!JIRA_RESOLUTION.isInitialized) {
                JIRA_RESOLUTION.tryLoadingTypeDefinitions();
                InstanceType type = JIRA_RESOLUTION.instanceType = WorkspaceService.createInstanceType(workspace, JIRA_RESOLUTION.name, workspace.TYPES_FOLDER);
                cache.getPropertyAsMap(MAPPING).put(JIRA_RESOLUTION.name, Long.toString(type.id().value()));
                JIRA_RESOLUTION.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, JIRA_RESOLUTION.instanceType, ID, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JIRA_RESOLUTION.instanceType, KEY, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JIRA_RESOLUTION.instanceType, DESCRIPTION, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JIRA_RESOLUTION.instanceType, LINK, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JIRA_RESOLUTION.instanceType, NAME, Cardinality.SINGLE, Workspace.STRING);

                MapProperty<String> propertyMetadata = JIRA_RESOLUTION.instanceType.getPropertyAsMap(ReservedNames.INSTANCETYPE_PROPERTY_METADATA);
                propertyMetadata.put(JiraSchemaConverter.RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE + ID, ID);
                propertyMetadata.put(JiraSchemaConverter.RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE + KEY, KEY);
                propertyMetadata.put(JiraSchemaConverter.RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE + DESCRIPTION, DESCRIPTION);
                propertyMetadata.put(JiraSchemaConverter.RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE + "self", LINK);
                propertyMetadata.put(JiraSchemaConverter.RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE + NAME, NAME);

                return type;
            } else {
                return JIRA_RESOLUTION.instanceType;
            }
        }
    }, JIRA_VERSION("version") {
        @Override
        public InstanceType getType() {
            if (!JIRA_VERSION.isInitialized) {
                JIRA_VERSION.tryLoadingTypeDefinitions();
                if (JIRA_VERSION.isInitialized) return JIRA_VERSION.instanceType;

                InstanceType type = JIRA_VERSION.instanceType = WorkspaceService.createInstanceType(workspace, JIRA_VERSION.name, workspace.TYPES_FOLDER);
                cache.getPropertyAsMap(MAPPING).put(JIRA_VERSION.name, Long.toString(type.id().value()));
                JIRA_VERSION.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, JIRA_VERSION.instanceType, ID, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JIRA_VERSION.instanceType, KEY, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JIRA_VERSION.instanceType, DESCRIPTION, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JIRA_VERSION.instanceType, LINK, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, JIRA_VERSION.instanceType, NAME, Cardinality.SINGLE, Workspace.STRING);

                MapProperty<String> propertyMetadata = JIRA_VERSION.instanceType.getPropertyAsMap(ReservedNames.INSTANCETYPE_PROPERTY_METADATA);
                propertyMetadata.put(JiraSchemaConverter.RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE + ID, ID);
                propertyMetadata.put(JiraSchemaConverter.RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE + KEY, KEY);
                propertyMetadata.put(JiraSchemaConverter.RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE + DESCRIPTION, DESCRIPTION);
                propertyMetadata.put(JiraSchemaConverter.RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE + SELF, LINK);
                propertyMetadata.put(JiraSchemaConverter.RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE + NAME, NAME);

                return type;
            } else {
                return JIRA_VERSION.instanceType;
            }
        }
    }, JIRA_COMPONENT("component") {
            @Override
            public InstanceType getType() {
                if (!JIRA_COMPONENT.isInitialized) {
                    JIRA_COMPONENT.tryLoadingTypeDefinitions();
                    if (JIRA_COMPONENT.isInitialized) return JIRA_COMPONENT.instanceType;

                    InstanceType type = JIRA_COMPONENT.instanceType = WorkspaceService.createInstanceType(workspace, JIRA_COMPONENT.name, workspace.TYPES_FOLDER);
                    cache.getPropertyAsMap(MAPPING).put(JIRA_COMPONENT.name, Long.toString(type.id().value()));
                    JIRA_COMPONENT.isInitialized = true;

                    WorkspaceService.createPropertyType(workspace, JIRA_COMPONENT.instanceType, ID, Cardinality.SINGLE, Workspace.STRING);
                    WorkspaceService.createPropertyType(workspace, JIRA_COMPONENT.instanceType, KEY, Cardinality.SINGLE, Workspace.STRING);
                    WorkspaceService.createPropertyType(workspace, JIRA_COMPONENT.instanceType, DESCRIPTION, Cardinality.SINGLE, Workspace.STRING);
                    WorkspaceService.createPropertyType(workspace, JIRA_COMPONENT.instanceType, LINK, Cardinality.SINGLE, Workspace.STRING);
                    WorkspaceService.createPropertyType(workspace, JIRA_COMPONENT.instanceType, NAME, Cardinality.SINGLE, Workspace.STRING);

                    MapProperty<String> propertyMetadata = JIRA_COMPONENT.instanceType.getPropertyAsMap(ReservedNames.INSTANCETYPE_PROPERTY_METADATA);
                    propertyMetadata.put(JiraSchemaConverter.RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE + ID, ID);
                    propertyMetadata.put(JiraSchemaConverter.RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE + DESCRIPTION, DESCRIPTION);
                    propertyMetadata.put(JiraSchemaConverter.RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE + SELF, LINK);
                    propertyMetadata.put(JiraSchemaConverter.RESERVED_PROPERTY_PREFIX_ORIGINFIELDTYPE + NAME, NAME);

                    return type;
                } else {
                    return JIRA_COMPONENT.instanceType;
                }
        }
    };
	
    private String name;
    private boolean isInitialized;
    private InstanceType instanceType;
    public static Folder typeFolder;    
    public static String JIRA_TYPES_FOLDER_NAME = "jira";
    private static Instance cache;
    private static Workspace workspace = null;
    private static final String JIRA_SUBTYPE_ID_CACHE_ID = "jiraSubTypeIdCacheId";
    public static final String SERVICE_ID_TO_DESIGNSPACE_ID_CACHE_ID = "JiraId2DesignspaceIdCache";

    JiraBaseElementType(String name) {
        this.name = name;
        this.isInitialized = false;
        this.instanceType = null;
    }

    public String getDesignSpaceShortTypeName() {
        return this.name;
    }

    private void tryLoadingTypeDefinitions() {


    	
    	
        if (workspace == null) {
            //loading config
            Properties props = new Properties();
            String workspaceName;

            try {
                props = PropertiesLoaderUtils.loadAllProperties("application.properties");
                workspaceName = props.getProperty("jira_workspace_name");
            } catch (IOException e) {
                log.debug("JIRA-SERVICE : The application.properties file was not found in the resources folder of the main module");
                workspaceName = "jira_default_workspace";
            }

            if(workspaceName==null) {
                workspace = WorkspaceService.PUBLIC_WORKSPACE;
            } else {
                String finalWorkspaceName = workspaceName;
                Optional<Workspace> workspace_ = WorkspaceService.allWorkspaces().stream().filter(w -> w.name().equals(finalWorkspaceName)).findAny();
                if (!workspace_.isPresent()) {
                    workspace = WorkspaceService.PUBLIC_WORKSPACE;
                } else {
                    workspace = workspace_.get();
                }
            }

        }
        
    	Folder typesFolder = workspace.TYPES_FOLDER;
        JiraBaseElementType.typeFolder = typesFolder.subfolder(JIRA_TYPES_FOLDER_NAME);
        //creating the folder should it not exist
        if (typeFolder == null) {
        	JiraBaseElementType.typeFolder = WorkspaceService.createSubfolder(workspace, workspace.TYPES_FOLDER, JIRA_TYPES_FOLDER_NAME);
        }

        if(cache==null) {

            //trying to find a cache
            for (Instance cur : workspace.debugInstances()) {
                Property property = cur.getProperty("id");
                if (property != null) {
                    if (property.get() != null && property.get().equals(JIRA_SUBTYPE_ID_CACHE_ID)) {
                        cache = cur;
                    }
                }
            }

            if (cache == null) {
                //no past cache has been found
                log.debug("JIRA-SERVICE: No subtype id cache was found");
                cache = workspace.createInstance(BaseElementType.ELEMENT_ID_CACHE.getType(), JIRA_SUBTYPE_ID_CACHE_ID);
                cache.getPropertyAsSingle("id").set(JIRA_SUBTYPE_ID_CACHE_ID);
                for (JiraBaseElementType type : JiraBaseElementType.values()) {
                    cache.getPropertyAsMap(MAPPING).put(type.name, type.getType().id().toString());
                }
                workspace.concludeTransaction();
                log.debug("JIRA-SERVICE: New subtype id cache has been created");
            } else {
                //past cache has been found
                for (JiraBaseElementType type : JiraBaseElementType.values()) {
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
                log.debug("JIRA-SERVICE: Successfully reconnected to subtype id cache");
            }
        }
    }

    public final static String ADDRESS = "emailAddress", KEY = "key", NAME = "name", FIELDS = "fields", ISSUE_LINKS = "issuelinks",
            LINK = BaseElementType.LINK, STATUS_ = "status", DISPLAY_NAME = "displayName", ASSIGNEE = "assignee", SELF = "self", ACTIVE = "active",
            DESCRIPTION = "description", ID = "id", CREATOR = "creator", PROJECT_TYPE_KEY = "project", REPORTER = "reporter", SUMMARY = "summary",
            ISSUE_TYPE = "issueType", PARENT = "parent", PRIORITY_ = "priority", ARRAY_ = "array", PROJECT_ = "project", SUBTASKS = "subtasks",
            MAPPING = "map", LINKED_ISSUES = "linkedIssues", LINKS_OUTGOING = "linksOutgoing", LINKS_INCOMING = "linksIncoming",
            OUTWARD_ISSUE = "outwardIssue", INWARD_ISSUE = "inwardIssue", TYPE = "type", OUTWARD = "outward", INWARD = "inward",
            CREATED = "created", PARENT_CHANGE = "Parent", LINK_CHANGE = "Link", FIELD = "field", FIELD_ID = "fieldId", TO_CHANGE = "to",
            FIELD_TYPE = "fieldtype", FROM_STRING_CHANGE = "fromString", TO_STRING_CHANGE = "toString", FROM_CHANGE = "from",
            CHANGELOG = "changelog", HISTORIES = "histories", ITEMS = "items", ACCOUNT_ID = "accountId",
    		EPICPARENT = "epicParent", EPICCHILDREN = "epicChildren";

}


