package at.jku.isse.designspace.jira.service;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import com.google.inject.Guice;
import com.google.inject.Injector;

import at.jku.isse.designspace.artifactconnector.core.IArtifactProvider;
import at.jku.isse.designspace.artifactconnector.core.communication.CommunicationBaseElementTypes;
import at.jku.isse.designspace.artifactconnector.core.converter.IConverter;
//import at.jku.isse.designspace.artifactconnector.core.endpoints.grpc.service.GrpcArtifactService;
import at.jku.isse.designspace.artifactconnector.core.endpoints.grpc.service.ServiceResponse;
import at.jku.isse.designspace.artifactconnector.core.exceptions.IdentiferFormatException;
import at.jku.isse.designspace.artifactconnector.core.exceptions.InvalidSchemaException;
import at.jku.isse.designspace.artifactconnector.core.idcache.IIdCache;
import at.jku.isse.designspace.artifactconnector.core.model.BaseElementType;
import at.jku.isse.designspace.artifactconnector.core.monitoring.IProgressObserver;
import at.jku.isse.designspace.artifactconnector.core.monitoring.ProgressEntry;
import at.jku.isse.designspace.artifactconnector.core.monitoring.ProgressEntry.Status;
import at.jku.isse.designspace.artifactconnector.core.updatememory.UpdateMemory;
import at.jku.isse.designspace.artifactconnector.core.updateservice.UpdateManager;
import at.jku.isse.designspace.artifactconnector.core.updateservice.core.connection.PollingConnection;
import at.jku.isse.designspace.core.events.Operation;
import at.jku.isse.designspace.core.model.Id;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.ServiceProvider;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.service.ServiceRegistry;
import at.jku.isse.designspace.jira.config.JiraServiceConfig;
import at.jku.isse.designspace.jira.model.JiraBaseElementType;
import at.jku.isse.designspace.jira.restclient.connector.IJiraTicketService;
import at.jku.isse.designspace.jira.updateservice.ChangeLogItem;
import at.jku.isse.designspace.jira.updateservice.IChangeLogItemFactory;
import at.jku.isse.designspace.jira.updateservice.JiraSimpleConnection;
import at.jku.isse.designspace.jira.updateservice.changemanagment.JiraWebhookConnection;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@DependsOn({"controleventengine"})
@ConditionalOnExpression(value = "${jira.enabled:false}")
public class JiraService implements IJiraService, ServiceProvider, IArtifactProvider {

	@Autowired
	private IProgressObserver obs;
	
    private IConverter jiraSchemaConverter;
    private IIdCache idCache;
    private IJiraTicketService jiraTicketService;

    private InstanceType jiraSchema;

    private PollingConnection pollingConnection;
    private JiraWebhookConnection reactiveConnection;
    private IHistoryManager historyManager;
    private IChangeLogItemFactory changeLogItemFactory;

    private UpdateManager updateManager;
    private UpdateMemory updateMemory;

    private IArtifactPusher artifactPusher;

    private Workspace workspace;
    private boolean isInitialized = false;
    private boolean autoUpdate = true;

    public JiraService(UpdateManager updateManager, UpdateMemory updateMemory) {
        ServiceRegistry.registerService(this);
        this.updateMemory = updateMemory;
        this.updateManager = updateManager;
        this.isInitialized = false;
    }

    public String getName(){
        return "JiraService";
    }
    public String getVersion(){
        return "1.0.0";
    }
    public int getPriority(){
        return 103;
    }
    public boolean isPersistenceAware(){
        return true;
    }
    
    @Override
    public InstanceType getArtifactInstanceType() {
        checkInitialized();
        return JiraBaseElementType.JIRA_ARTIFACT.getType();
    }

    @Override
    public Set<InstanceType> getArtifactInstanceTypes() {
    	checkInitialized();
    	return Set.of(JiraBaseElementType.JIRA_ARTIFACT.getType());
    }

    @Override
    public Map<InstanceType, List<String>> getSupportedIdentifier() {
    	checkInitialized();
    	return Map.of(JiraBaseElementType.JIRA_ARTIFACT.getType(), List.of(IJiraService.JiraIdentifier.JiraIssueKey.toString(), IJiraService.JiraIdentifier.JiraIssueId.toString()));
    }

    @Override
    public void initialize() {
        log.debug("---JIRA SERVICE");
        Properties props = new Properties();
        ProgressEntry initService = dispatchNewStartedActivity("Initializing Connector");
        
        try {
            FileReader reader = new FileReader("./application.properties");
            props.load(reader);

            CommunicationBaseElementTypes.initAll();
            isInitialized = true; // needed this early as other pushServerToWorkspace results in an infinite loop.

            for (JiraBaseElementType gbet : JiraBaseElementType.values()) {
                gbet.getType(); //init this type so its known in DesignSpace immediately upon starting
            }

            //we have to check for the jira updateMemory
            Instant lastUpdate = this.updateMemory.getLastUpdateTime("Jira");

            Injector injector = Guice.createInjector(new JiraServiceConfig(props, lastUpdate != null));
            this.jiraTicketService = injector.getInstance(IJiraTicketService.class);
            this.workspace = injector.getInstance(Workspace.class);
            this.jiraSchemaConverter = injector.getInstance(IConverter.class);
            this.jiraSchema = injector.getInstance(InstanceType.class);
            this.idCache = injector.getInstance(IIdCache.class);
            this.artifactPusher = injector.getInstance(IArtifactPusher.class);
            this.changeLogItemFactory = injector.getInstance(IChangeLogItemFactory.class);
            this.historyManager = injector.getInstance(IHistoryManager.class);

            Boolean syncWithServer = injector.getInstance(Boolean.class);

            //ToDo: Implement NewHistoryManager
            //this.historyManager = new HistoryManager(curSchema, workspace, jiraConnector, this);


            if (syncWithServer) {
                //in case syncing is active an there has not been an update we fetch all
                if (lastUpdate == null) {
                    pushServerToWorkspace();
                }
            }

            if (lastUpdate == null) {
                lastUpdate = Instant.now();
                this.updateMemory.setLastUpdateTime("Jira", Instant.now());
                this.workspace.concludeTransaction();
            }

            //ToDo: Adapt update mechanism, the IJiraService interface should be used instead of the Jira Service class
            //      Since those mechanisms use the ArtifactPusher, this class should first be implemented!


            String updateConfig = props.getProperty("jira.update.config");
            if (updateConfig != null) {
                updateConfig.trim();
                if (updateConfig.equals("poll")) {
                    this.pollingConnection = new JiraSimpleConnection("jira_server", this, jiraTicketService, Timestamp.from(lastUpdate));
                    this.updateManager.establishServerConnection(pollingConnection);
                    dispatchAtomicFinishedActivity("Scheduled polling for updates");
                } else if (updateConfig.equals("webhook")){
                    Object webhookAddress = props.get("jira.webhook.forwardAddress");
                    if (webhookAddress != null) {
                        this.reactiveConnection = new JiraWebhookConnection(this, this.changeLogItemFactory);
                        this.updateManager.establishReactiveConnection(this.reactiveConnection);
                        this.jiraTicketService.createWebhook(webhookAddress.toString().trim() + "/jira/webhook");
                        dispatchAtomicFinishedActivity("Created webhook for receiving updates");
                    }
                }
            }
         //   GrpcArtifactService.addServiceResponder(JiraBaseElementType.JIRA_ARTIFACT.getDesignSpaceShortTypeName(), this);
            initService.setStatus(Status.Completed);
        } catch (IOException ioe) {
        	initService.setStatusAndComment(Status.Failed, "Running directory did not contain an application.properties file");
            log.debug("JIRA-SERVICE: The running directory did not contain an application.properties file, Service cannot be initialized!");
        }
    }
    
	@Override
	public ServiceResponse getServiceResponse(String id, String identifierType) {
		return getServiceResponse(id, identifierType, false);
	}

	@Override
	public ServiceResponse[] getServiceResponse(Set<String> ids, String identifierType) {
		return getServiceResponse(ids, identifierType, false);
	}

    @Override
    public ServiceResponse getServiceResponse(String id, String identifierType, boolean doForceRefetch) {
        ServiceResponse resp = getNonConcludedItem(id, identifierType, doForceRefetch);
        if (resp.getKind()==ServiceResponse.SUCCESS)
            this.workspace.concludeTransaction();
        return resp;
    }


    @Override
    public ServiceResponse[] getServiceResponse(Set<String> ids, String identifierType, boolean doForceRefetch) {
    	ProgressEntry pe = dispatchNewStartedActivity(String.format("Batch Loading %s items", ids.size()));
     	AtomicInteger successCount = new AtomicInteger(0);
         List<ServiceResponse> serviceResponses = ids.stream()
        		 .map(id -> getNonConcludedItem(id, identifierType, doForceRefetch))
         	.peek(resp -> {
         		if (resp.getKind()==ServiceResponse.SUCCESS)
         			successCount.incrementAndGet();
         	})
         	.collect(Collectors.toList());
        if (successCount.get() > 0) { 
     	   pe.setStatusAndComment(Status.Completed, String.format("Batch Loaded %s items", successCount.get()));
     	   this.workspace.concludeTransaction();
        } else {
     	   pe.setStatusAndComment(Status.Failed, "Unable to load any of those items:"+ids.toString());
        }
        return serviceResponses.toArray(new ServiceResponse[0]);
    }

    private ServiceResponse getNonConcludedItem(String id, String identifierType, boolean forceRefetch) {
        checkInitialized();

        if (!isInitialized) {
            return null;
        }

        JiraIdentifier jiraIdentifier = JiraIdentifier.valueOf(identifierType);
        if (jiraIdentifier != null) {
            if (id != null) {
                try {
                    Optional<Instance> issueInstance = getArtifact(id, jiraIdentifier, forceRefetch);
                    if (issueInstance.isPresent()) {
                       // this.workspace.concludeTransaction();
                        return new ServiceResponse(ServiceResponse.SUCCESS, "Jira", "Successful fetch", issueInstance.get().id().toString());
                    }
                    return new ServiceResponse(ServiceResponse.UNAVAILABLE, "Jira", "No issue with given key", "");
                } catch (IdentiferFormatException e) {
                    return new ServiceResponse(ServiceResponse.UNKNOWN, "Jira", "The request id format was invalid", "");
                }
            } else {
                return new ServiceResponse(ServiceResponse.UNKNOWN, "Jira", "The request id was null", "");
            }
        } else {
            return new ServiceResponse(ServiceResponse.UNKNOWN, "Jira", "The request identifierType was invalid", "");
        }
    }

    @Override
    public boolean isAutoUpdateActive() {
        checkInitialized();
        return autoUpdate;
    }

    @Override
    public IHistoryManager getHistoryManager() {
        //ToDo: return NewHistoryManager
        //return this.historyManager;
        return null;
    }

    @Override
    public IChangeLogItemFactory getChangeLogItemFactory() {
        checkInitialized();
        return this.changeLogItemFactory;
    }

    @Override
    public void enableAutoUpdate() {
        checkInitialized();
        this.autoUpdate = true;
    }

    @Override
    public void disableAutoUpdate() {
        checkInitialized();
        this.autoUpdate = false;
    }

    @Override
    public void pushServerToWorkspace() {
        checkInitialized();
        if (isInitialized) {
            log.debug("JIRA-SERVICE: Fetching connected server to workspace");
            List<Object> issues = jiraTicketService.getAllArtifacts(true);
            for (Object issue : issues) {
                //ToDo: Implement NewArtifactPusher
                //artifactPusher.pushArtifact(artifact, curSchema);
                try {
                    createInstance((Map<String, Object>) issue, jiraSchema, true, true);
                } catch (Exception e) {
                    log.debug("JIRA-SERVICE: The JiraRestClient provided an unexpected input for the transferIssue method! TransferIssue only accepts maps!");
                }
            }
            workspace.concludeTransaction();
        }
    }

    @Override
    public void pushServerToWorkspaceWithHistory() {
        checkInitialized();
        if (isInitialized) {
            //ToDo: Implement NewHistoryManager
            //historyManager.pushEntireDatabaseToDesignspace();
            workspace.concludeTransaction();
        }
    }

    @Override
    public int applyUpdates(Collection<ChangeLogItem> changeLogItems) {
        checkInitialized();

        if (!isInitialized) {
            return 0;
        }

        //ToDo: Implement NewHistoryManager
        //return historyManager.applyUpdates(changeLogItems);
        return -1;
    }

    @Override
    public boolean applyUpdate(ChangeLogItem changeLogItem) {
        checkInitialized();

        if (!isInitialized) {
            return false;
        }

        return historyManager.applyUpdate(changeLogItem);
    }

    @Override
    public Optional<InstanceType> createSchema(Map<String, Object> schemaMap, Map<String, Object> issueLinkTypes, Map<String, Object> namesLabelMap, String serverId) {
        checkInitialized();

        if (!isInitialized) {
            return Optional.empty();
        }

        return Optional.of(jiraSchemaConverter.createSchema(schemaMap, issueLinkTypes, namesLabelMap, serverId));
    }

    @Override
    public Optional<InstanceType> findSchema(String serverId) {
        checkInitialized();

        if (!isInitialized) {
            return Optional.empty();
        }

        return jiraSchemaConverter.findSchema(serverId);
    }

    @Override
    public boolean activateSchema(String serverId) throws InvalidSchemaException {
        checkInitialized();
        if (isInitialized) {
            Optional<InstanceType> schema = jiraSchemaConverter.findSchema(serverId);
            if (schema.isPresent()) {
                if (schema.get().isTypeOf(JiraBaseElementType.JIRA_ARTIFACT.getType())) {
                    throw new InvalidSchemaException("You can only use schemata that inherit from the super type " + JiraBaseElementType.JIRA_ARTIFACT.getType().name());
                }
                activateSchema(schema.get());
                return true;
            }
        }
        return false;
    }

    @Override
    public void activateSchema(InstanceType schema) throws InvalidSchemaException {
        checkInitialized();
        if (isInitialized) {
            if (schema.isTypeOf(JiraBaseElementType.JIRA_ARTIFACT.getType())) {
                throw new InvalidSchemaException("You can only use schemata that inherit from the super type " + JiraBaseElementType.JIRA_ARTIFACT.getType().name());
            }
            this.jiraSchema = schema;
            //ToDo: Implement NewArtifactPusher and NewHistoryManager
            //this.artifactPusher = new JiraArtifactPusher(workspace, schema);
            //this.historyManager = new HistoryManager(schema, workspace, jiraConnector, this);
        }
    }


    @Override
    public Optional<Instance> getArtifact(String identifier, JiraIdentifier identifierType, boolean forceRefetch) throws IdentiferFormatException {

        switch (identifierType) {
            case JiraProjectId:
                log.debug("JIRA-SERVICE: Fetching projects is not yet implemented!");
                return Optional.empty();
            case JiraIssueKey: case JiraIssueId:
                Id instanceId = this.idCache.getDesignspaceId(identifier);
                Instance instance = this.workspace.findElement(instanceId);
                boolean update = false;
                if (instance != null) {
                    //at least a placeholder exists
                    if (instance.hasProperty(BaseElementType.FULLY_FETCHED)) {
                        Object fullyFetchedValue = instance.getProperty(BaseElementType.FULLY_FETCHED).get();
                        if (fullyFetchedValue != null && fullyFetchedValue.equals(true) && !forceRefetch) {
                            return Optional.of(instance);
                        }
                        update = true;
                    }
                }
                ProgressEntry pe = dispatchNewStartedActivity("Fetching Jira Item "+identifier);
                Map<String, Object> issueData = this.jiraTicketService.getArtifact(identifier, true);              
                if (issueData != null) {
                	
                	Optional<Instance> optInst = createInstance(issueData, jiraSchema, update, true);
                	if (optInst.isPresent()) {
                		Instance issue = optInst.get();
                		// if this is an epic, we need another call to fetch the epic relations
                		if (((String)issue.getPropertyAsValueOrElse(JiraBaseElementType.ISSUE_TYPE, () -> "UNKNOWN")).equalsIgnoreCase("Epic")) {
                			pe.setStatusAndComment(Status.InProgress, "Fetching now epic children");
                			String key = (String) issue.getPropertyAsValue(JiraBaseElementType.KEY);
                			int count = fetchEpicChildren(key, issue).size();
                			//"'epic link'=PVCSX-19298"
                			pe.setStatusAndComment(Status.Completed, String.format("Fetched %s epic children", count));
                		} else  
                			pe.setStatus(Status.Completed);
                	} else
                		pe.setStatusAndComment(Status.Failed, "Could not retrieve issue data");
                	return optInst;
                	
                } else {
                	pe.setStatusAndComment(Status.Failed, "Could not retrieve issue data");
                }
                return Optional.empty();
            default:
                return Optional.empty();
        }

    }
    
    private Set<Instance> fetchEpicChildren(String epicKey, Instance epic) { // fetches up to maxbatchsize (i.e., 50) epic children
    	return this.jiraTicketService.getArtifactsFromJQL("'epic link'="+epicKey)
    			.stream()
    			.map(subElement -> createInstance(subElement, jiraSchema, true, true))
    			.filter(optEl -> optEl.isPresent())
    			.map(optEl -> optEl.get())
    			.map(child -> { 
    				epic.getPropertyAsSet(JiraBaseElementType.EPICCHILDREN).add(child);
    				return child; 
    				})
    			.collect(Collectors.toSet());
    }

    @Override
    public Optional<Instance> createInstance(Map<String, Object> dataMap, InstanceType instanceType, boolean update, boolean withIssueLinks) {
        return this.artifactPusher.createInstance(dataMap, instanceType, update, withIssueLinks);
    }

    //Jira id's are not unique across different types of data objects.
    //While only one issue can have the id 10000, there might also exist a project with that id.
    //The solution simply appends the name of the instance type to the id.
    //For issues this is not true, because we want to be able to find them using just their native id.
    private String reformatId(String id, InstanceType instanceType) {
        if (instanceType != jiraSchema && instanceType != BaseElementType.ARTIFACT.getType()) {
            id = instanceType.name() + "_" + id;
        }
        return id;
    }

    @Override
    public Optional<Instance> getIssueWithEventHistory(String identifier) {
        return Optional.empty();
    }

    @Override
    public List<Instance> getAllExistingArtifacts() {
        return null;
    }

    @Override
    public Optional<Instance> findInstance(String issueId) {
        Id instanceId = this.idCache.getDesignspaceId(issueId);
        Instance instance = this.workspace.findElement(instanceId);
        return Optional.ofNullable(instance);
    }

    @Override
    public IArtifactPusher getArtifactPusher() {
        checkInitialized();

        if (!isInitialized) {
            return null;
        }

        return this.artifactPusher;
    }

    @Override
    public IJiraTicketService getJiraTicketService() {
        checkInitialized();

        if (!isInitialized) {
            return null;
        }

        return this.jiraTicketService;
    }

    @Override
    public Optional<JiraWebhookConnection> getReactiveConnection() {
        checkInitialized();

        if (!isInitialized) {
            return Optional.empty();
        }

        return Optional.ofNullable(this.reactiveConnection);
    }

    @Override
    public Workspace getWorkspace() {
        checkInitialized();

        if (!isInitialized) {
            return null;
        }

        return workspace;
    }

    @Override
    public UpdateMemory getUpdateMemory() {
        checkInitialized();

        if (!isInitialized) {
            return null;
        }

        return updateMemory;
    }

    @Override
    public InstanceType getCurSchema() {
        checkInitialized();

        if (!isInitialized) {
            return null;
        }

        return jiraSchema;
    }

    private void checkInitialized() {
        if (!isInitialized) {
            initialize();
        }
    }

	protected ProgressEntry dispatchNewStartedActivity(String activity) {
		ProgressEntry pe = new ProgressEntry("JiraConnector", activity, Status.Started);
		if (obs != null)
			obs.dispatchNewEntry(pe);
		return pe;
	}
	
	protected ProgressEntry dispatchAtomicFinishedActivity(String activity) {
		ProgressEntry pe = new ProgressEntry("JiraConnector", activity, Status.Completed);
		if (obs != null)
			obs.dispatchNewEntry(pe);
		return pe;
	}

	@Override
	public void handleServiceRequest(Workspace workspace, Collection<Operation> operations) {
		// TODO Auto-generated method stub
		
	}

}
