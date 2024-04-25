package at.jku.isse.designspace.jira.config;


import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;

import at.jku.isse.designspace.artifactconnector.core.converter.IConverter;
import at.jku.isse.designspace.artifactconnector.core.idcache.IIdCache;
import at.jku.isse.designspace.artifactconnector.core.idcache.IdCache;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.service.WorkspaceService;
import at.jku.isse.designspace.jira.model.JiraBaseElementType;
import at.jku.isse.designspace.jira.model.JiraSchemaConverter;
import at.jku.isse.designspace.jira.restclient.connector.IJiraTicketService;
import at.jku.isse.designspace.jira.restclient.connector.JiraTicketService;
import at.jku.isse.designspace.jira.service.HistoryManager;
import at.jku.isse.designspace.jira.service.IArtifactPusher;
import at.jku.isse.designspace.jira.service.IHistoryManager;
import at.jku.isse.designspace.jira.service.JiraCloudArtifactPusher;
import at.jku.isse.designspace.jira.updateservice.IChangeLogItemFactory;
import at.jku.isse.designspace.jira.updateservice.JiraCloudChangeLogItemFactory;

public class JiraServiceConfig extends AbstractModule {

    private IJiraTicketService jiraTicketService;

    private Workspace workspace;
    private IConverter converter;
    private InstanceType curSchema;
    private IArtifactPusher artifactPusher;
    private IIdCache idCache;
    private IChangeLogItemFactory changeLogItemFactory;
    private IHistoryManager historyManager;

    private String SCHEMA_ID = "jira_core_schema";
    private String NAMES_ID = "jira_core_names";
    private String LINK_TYPES_ID = "jira_link_types";

    private final String[] fields = {};

    private boolean syncWithServerAtCreation;
    private String resourcesPath;

    public JiraServiceConfig(Properties props, boolean loadedFromFile) {
    	this.syncWithServerAtCreation = (!loadedFromFile) && Boolean.parseBoolean(props.getProperty("jira.sync","false"));
        this.resourcesPath = props.getProperty("jira.resources.path");

        if (this.resourcesPath == null) {
            this.resourcesPath = "./";
        } else {
            this.resourcesPath = this.resourcesPath.trim();
        }

        this.jiraTicketService = new JiraTicketService(new JiraTicketServiceConfig(props, fields));
        this.workspace = WorkspaceService.PUBLIC_WORKSPACE;
        this.converter = new JiraSchemaConverter(workspace);
        this.idCache = new IdCache(workspace, JiraBaseElementType.SERVICE_ID_TO_DESIGNSPACE_ID_CACHE_ID);

        Optional<InstanceType> schema_ = converter.findSchema(SCHEMA_ID);
        Map<String, Object> schemaMap = fetchOrLoadCoreSchema();
        Map<String, Object> linkTypes = fetchOrLoadLinkTypes();
        Map<String, Object> namesLabelMap = fetchOrLoadNames();

        if (!schema_.isPresent()) {
            this.curSchema = converter.createSchema(schemaMap, linkTypes, namesLabelMap, SCHEMA_ID);
        } else {
            this.curSchema = schema_.get();
            converter.synchronizeSchemata(this.curSchema, schemaMap, linkTypes);
        }

        this.artifactPusher = new JiraCloudArtifactPusher(workspace, curSchema, idCache);
        this.changeLogItemFactory = new JiraCloudChangeLogItemFactory(artifactPusher, jiraTicketService, curSchema);
        this.historyManager = new HistoryManager(artifactPusher, changeLogItemFactory, curSchema, workspace);
    }

    @Override
    protected void configure() {
        bind(IArtifactPusher.class).toInstance(artifactPusher);
        bind(IChangeLogItemFactory.class).toInstance(changeLogItemFactory);
        bind(IHistoryManager.class).toInstance(historyManager);
        bind(InstanceType.class).toInstance(curSchema);
        bind(Workspace.class).toInstance(workspace);
        bind(IJiraTicketService.class).toInstance(jiraTicketService);
        bind(IConverter.class).toInstance(converter);
        bind(IIdCache.class).toInstance(idCache);
        bind(Boolean.class).toInstance(syncWithServerAtCreation);
    }

    private Map<String, Object> fetchOrLoadCoreSchema() {
    	String localPath = this.resourcesPath + SCHEMA_ID + ".json";
        try {
            
            Map<String, Object> schema = createMapFromString(localPath);
            //if (schema == null) throw new Exception();
            return schema;
        } catch (Exception e) {
            Map<String, Object> schema = this.jiraTicketService.getSchema();
            if (schema == null) {
            	throw new RuntimeException("Could not fetch schema neither from local file nor server");
            } else {
            	createFileFromString(schema, localPath);
            	return schema;
            }
        }

    }

    private Map<String, Object> fetchOrLoadNames() {

        try {
            String localPath = this.resourcesPath + NAMES_ID + ".json";
            Map<String, Object> schema = createMapFromString(localPath);
            if (schema == null) throw new Exception();
            return schema;
        } catch (Exception e) {
            Map<String, Object> schema = this.jiraTicketService.getNames();
            String localPath = this.resourcesPath + NAMES_ID + ".json";
            createFileFromString(schema, localPath);
            return schema;
        }

    }

    private Map<String, Object> fetchOrLoadLinkTypes() {

        try {
            String localPath = this.resourcesPath + LINK_TYPES_ID + ".json";
            Map<String, Object> linkTypes = createMapFromString(localPath);
            if (linkTypes == null) throw new Exception();
            return linkTypes;
        } catch (Exception e) {
            Map<String, Object> linkTypes = this.jiraTicketService.getLinkTypes();
            String localPath =this.resourcesPath + LINK_TYPES_ID + ".json";
            createFileFromString(linkTypes, localPath);
            return linkTypes;
        }

    }

    public Map<String, Object> createMapFromString(String localPath) throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        try {
            File file = new File(localPath);
            if (!file.exists()) {
                throw new Exception("Schema File "+localPath+" does not exists");
            }
            return mapper.readValue(file, Map.class);
        } catch (IOException e) {
            throw e;
        }
    }

    public static void createFileFromString(Map<String, Object> map, String localPath) {
        ObjectMapper mapper = new ObjectMapper();
        try {
           // String userDir = System.getProperty("user.dir", "");
            //String path = userDir + "\\" + localPath;
            File file = new File(localPath);
            if (!file.exists()) {
                file.createNewFile();
            }
            mapper.writeValue(file, map);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
