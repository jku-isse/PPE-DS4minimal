package at.jku.isse.designspace.azure.service;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Guice;
import com.google.inject.Injector;

import at.jku.isse.designspace.artifactconnector.core.IArtifactProvider;
import at.jku.isse.designspace.artifactconnector.core.endpoints.grpc.service.ServiceResponse;
import at.jku.isse.designspace.artifactconnector.core.monitoring.IProgressObserver;
import at.jku.isse.designspace.artifactconnector.core.monitoring.ProgressEntry;
import at.jku.isse.designspace.artifactconnector.core.monitoring.ProgressEntry.Status;
//import at.jku.isse.designspace.artifactconnector.core.endpoints.grpc.service.GrpcArtifactService;
import at.jku.isse.designspace.azure.api.IAzureApi;
import at.jku.isse.designspace.azure.idcache.IdCache;
import at.jku.isse.designspace.azure.model.AzureBaseElementType;
import at.jku.isse.designspace.azure.model.WorkItemComment;
import at.jku.isse.designspace.azure.model.generator.AzureInstanceTypeGenerator;
import at.jku.isse.designspace.azure.updateservice.AzureChangePatcher;
import at.jku.isse.designspace.azure.updateservice.AzureWebhookParser;
import at.jku.isse.designspace.core.events.Operation;
import at.jku.isse.designspace.core.model.Id;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.ServiceProvider;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.service.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@DependsOn({"controleventengine"})
@ConditionalOnExpression(value = "${azure.enabled:false}")
public class AzureService implements IAzureService, ServiceProvider, IArtifactProvider {

	@Autowired
	private IProgressObserver obs; 
	
    private IAzureApi azureApi;
    private Workspace workspace;
    private IdCache idCache;
    private ItemMapper itemMapper;
    private AzureChangePatcher changePatcher;
    private AzureInstanceTypeGenerator instanceTypeGenerator;

    private boolean isInitialized = false;

    private boolean initWithFetching;
    
    private BlockingQueue<byte[]> updateQueue = new LinkedBlockingQueue<>();

    //@Autowired
    //private WorkspaceService workspaceService;

    /*@Inject
    public AzureService(IAzureApi azureApi, Workspace workspace, IdCache idCache) {
        this.azureApi = azureApi;
        this.workspace = workspace;
        this.idCache = idCache;
        this.itemMapper = new ItemMapper(this.idCache, this.azureApi);
        isInitialized = false;
        log.debug("---AZURE SERVICE");
    }*/

    public AzureService() {
        ServiceRegistry.registerService(this);
    }

    public String getName(){
        return "AzureService";
    }
    public String getVersion(){
        return "1.0.0";
    }
    public int getPriority(){
        return 106;
    }
    public boolean isPersistenceAware(){
        return true;
    }
    
    /*
    @EventListener
    public void onApplicationReadyEvent(ApplicationReadyEvent applicationReadyEvent) {
        if (!Event.isInitialized()) {
            Event.setInitialized();
        }
        initialize();
    }
     */

    @Override
    public void initialize() {
        log.debug("---AZURE SERVICE");
        ProgressEntry initService = dispatchNewStartedActivity("Initializing Connector");
        Properties props = new Properties();
 //       GrpcArtifactService.addServiceResponder("Azure", this);

        try {
            FileReader reader = new FileReader("./application.properties");
            props.load(reader);
            reader.close();
        } catch (IOException ioe) {
            try {
                props = PropertiesLoaderUtils.loadAllProperties("application.properties");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Injector injector = Guice.createInjector(new AzureServiceConfig());

        this.azureApi = injector.getInstance(IAzureApi.class);
        this.workspace = injector.getInstance(Workspace.class);
        this.idCache = injector.getInstance(IdCache.class);
        this.itemMapper = new ItemMapper(this.idCache, this.azureApi, obs);
        Map<String, String> linkId2name = itemMapper.initRelationTypes(workspace);
        if(linkId2name.isEmpty()) {
            log.debug("AZURE-SERVICE: Initialized work item relation types failed");
        }
        AzureBaseElementType.setLinkTypeMap(linkId2name);
        this.changePatcher = new AzureChangePatcher(this, linkId2name);
        this.instanceTypeGenerator = new AzureInstanceTypeGenerator(this);

//        if(itemMapper.initWorkItemTypes(workspace)) {
//            log.debug("AZURE-SERVICE: Initialized work item types and their states");
//        }
//
//        if(itemMapper.initWorkItemTypeCategories(workspace)) {
//            log.debug("AZURE-SERVICE: Assigned work item types into categories");
//        }
//

        if (props.getProperty("azure.initWithWorkItems").equals("true")) {
            log.debug("AZURE-SERVICE: Fetching work items from Azure DevOps server");
            initWithFetching = true;
            Optional<List<Instance>> workItems = getAllWorkItems();

            if(workItems.isPresent()) {
                log.debug("AZURE-SERVICE: Work items loaded");
                List<Instance> workItemsArray = workItems.get();
                //TODO: delete this debug output when no longer needed
//                for (Instance workItem : workItemsArray) {
//                    System.out.println("work item Azure id: " + workItem.getPropertyAsSingle("id").value);
//                    System.out.println("work item title: " + workItem.getPropertyAsSingle("title").value);
//                    System.out.println("work item DSpace id: " + workItem.id());
//                    System.out.println("idCache id: " + idCache.getInstanceId(workItem.getPropertyAsSingle("id").value.toString()));
//                }
            }
        }

        new Thread(new UpdateSynchronizer(updateQueue)).start();
        
        log.debug("---AzureService initialized");
        workspace.concludeTransaction();
        isInitialized = true;

        /*InstanceType generatedInstanceType = generateWorkItemInstanceType("task");
        Collection<String> propNames = generatedInstanceType.getPropertyNames();
        for(String item : propNames) {
            System.out.println(item);
        }


        Optional<Instance> workItem4 = transferAzureWorkItem("TestProject",4);
        if(workItem4.isPresent()) {
            System.out.println("yes");
        }
        Optional<Instance> workItem37 = transferAzureWorkItem("TestProject",37);
        if(workItem4.isPresent()) {
            System.out.println("yes");
        }
         */
        initService.setStatus(Status.Completed);
    }


    private void checkInitialized() {
        if (!isInitialized) {
            initialize();
        }
    }

    @Override
    public Optional<List<Instance>> getAllWorkItems() {
       return Optional.of(itemMapper.fetchAndMapAllWorkItems(workspace));
    }

    @Override
    public Optional<Instance> transferAzureWorkItem(String project, int workItemId, boolean doForceRefetch) {
        checkInitialized();

        if(!isInitialized) {
            return Optional.empty();
        }

        Instance azureItemInstance;

        //check the cache if there's already an entry for the corresponding azure id
        Id dspaceId = idCache.getInstanceId(ItemMapper.getArtifactIdentifier(project, ""+workItemId));
        if (dspaceId != null) {
            azureItemInstance = workspace.findElement(dspaceId);
            if(azureItemInstance.getPropertyAsValue(AzureBaseElementType.FULLY_FETCHED).equals(true) && !doForceRefetch) {
                return Optional.of(azureItemInstance);
            } else if (azureItemInstance.getPropertyAsValue(AzureBaseElementType.FULLY_FETCHED).equals(true) && doForceRefetch) {
            	Instance instance = azureItemInstance;
            	itemMapper.linkId2Name.values().stream().filter(Objects::nonNull).forEach(linkName -> {
                	String linkProperty = AzureBaseElementType.convertLinkTypeNameToProperty(linkName);
                	instance.getPropertyAsSet(linkProperty).clear();
                });
            }
        }

        //fetch and map from API. Should there be no such item, just return Optional.empty()
        azureItemInstance = itemMapper.fetchAndMapWorkItem(project, workItemId, workspace);
        //no response from API
        if(azureItemInstance == null) {
            return Optional.empty();
        }

        return Optional.of(azureItemInstance);
    }

    //transfering a work item from webhook
    @Override
    public boolean transferWorkItem(JsonNode revisionNode) {
//        String designspaceId = ItemMapper.getArtifactIdentifier(projectName, ""+workItemId);
//        Instance workItemInstance;
//        Optional<Instance> workItemInstance_ = searchForInstance(designspaceId);
//        if(workItemInstance_.isPresent()) {
//            //fill the work item
//            workItemInstance = workItemInstance_.get();
//            log.debug("AZURE-SERVICE: Found a skeleton instance, filling instance with data...");
//        } else {
//            //create a new instance
//            workItemInstance = workspace.createInstance(AzureBaseElementType.AZURE_WORKITEM.getType(), workItemObj.getTitle());
//            log.debug("AZURE-SERVICE: Creating a new work item instance...");
//        }
        return itemMapper.mapWorkItemFromRevision(revisionNode, workspace);
    }

    @Override
    public Optional<Instance> transferUserByEmail(String email) {
        checkInitialized();

        if(!isInitialized) {
            return Optional.empty();
        }

        Optional<Instance> userInstance_ = searchForInstance(email);
        if(userInstance_.isPresent()) {
            return userInstance_;
        }

        Instance userInstance = itemMapper.fetchAndMapUserByEmail(email, workspace);
        if(userInstance == null) {
            return Optional.empty();
        }
        return Optional.of(userInstance);
    }

    @Override
    public Optional<Instance> transferUserByDescriptor(String userDescriptor) {
        checkInitialized();

        if(!isInitialized) {
            return Optional.empty();
        }

        Optional<Instance> userInstance_ = searchForInstance(userDescriptor);
        if(userInstance_.isPresent()) {
            return userInstance_;
        }

        Instance userInstance = itemMapper.fetchAndMapUserByDescriptor(userDescriptor, workspace);
        if(userInstance == null) {
            return Optional.empty();
        }
        return Optional.of(userInstance);
    }

//    @Override
//    public Optional<Instance> transferWorkItemLink(WorkItemLink workItemLink, String projectName) {
//        checkInitialized();
//
//        if(!isInitialized) {
//            return Optional.empty();
//        }
//
//        Instance workItemLinkInstance = itemMapper.createWorkItemLinkInstance(workItemLink, projectName, workspace);
//        if(workItemLinkInstance == null) {
//            return Optional.empty();
//        }
//        return Optional.of(workItemLinkInstance);
//    }

    @Override
    public Optional<Instance> transferComment(WorkItemComment comment) {
        checkInitialized();

        if(!isInitialized) {
            return Optional.empty();
        }


        Optional<Instance> commentInstance_ = searchForInstance(String.valueOf(comment.getId()));
        if(commentInstance_.isPresent()) {
            return commentInstance_;
        }

        Instance commentInstance = itemMapper.createCommentInstance(comment, workspace);
        if(commentInstance == null) {
            return Optional.empty();
        }
        return Optional.of(commentInstance);
    }

    @Override
    public boolean updateComment(Instance commentInstance, int workItemId, int commentId) {
        return itemMapper.updateCommentInstance(commentInstance, workItemId, commentId);
    }



    private ServiceResponse getNonConcludedItem(String id, String service, boolean doForceRefetch) {
    	checkInitialized();

        String[] splitted = id.split("/");
        if (splitted.length == 2) {
            String project = splitted[0];
            String azureWorkItemId = splitted[1];
            if (project != null && azureWorkItemId != null) {
                try {
                    int intId = Integer.parseInt(azureWorkItemId);
                    Optional<Instance> workItemInstance = transferAzureWorkItem(project, intId, doForceRefetch);
                    if (workItemInstance.isPresent()) {
                        
                        return new ServiceResponse(ServiceResponse.SUCCESS, service, "Successful fetch", workItemInstance.get().id().toString());
                    } else
                        return new ServiceResponse(ServiceResponse.UNAVAILABLE, service, "Unable to fetch with given project: "+project+" and id: "+azureWorkItemId, "");
                } catch (Exception e) {
                    e.printStackTrace();                	
                	return new ServiceResponse(ServiceResponse.INVALID, service, "No item with given project: "+project+" and id: "+azureWorkItemId, "");
                }
            }
        }

        return new ServiceResponse(ServiceResponse.UNKNOWN, service, "The request id was invalid as unable to obtain project and id from string: "+id , "");
    }
    
    @Override
    public ServiceResponse getServiceResponse(String id, String service) {
        return getServiceResponse(id, service, false);
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
    
	@Override
	public ServiceResponse getServiceResponse(String id, String identifierType, boolean doForceRefetch) {
		ServiceResponse resp = getNonConcludedItem(id, identifierType, doForceRefetch);
        if (resp.getKind()==ServiceResponse.SUCCESS)
        	this.workspace.concludeTransaction();
        return resp;
	}

	@Override
	public ServiceResponse[] getServiceResponse(Set<String> ids, String identifierType) {
		return getServiceResponse(ids, identifierType, false);
	}

    @Override
    public InstanceType generateWorkItemInstanceType(String workItemType) {
        return instanceTypeGenerator.generateInstanceType(azureApi.getSpecificWorkItemType(workItemType), azureApi.getProjectFields(), workspace);
    }

    @Override
    public Optional<Instance> searchForInstance(String id) {
        assert id != null;
        checkInitialized();

        if (!isInitialized) {
            return Optional.empty();
        }

        Id designspaceId = idCache.getInstanceId(id);
        if(designspaceId == null) {
            return Optional.empty();
        }
        return Optional.of(workspace.findElement(designspaceId));
    }

    @Override
    public InstanceType getArtifactInstanceType() {
        return AzureBaseElementType.AZURE_WORKITEM.getType();
    }

	@Override
	public Set<InstanceType> getArtifactInstanceTypes() {
		return Set.of(AzureBaseElementType.AZURE_WORKITEM.getType());
	}

	@Override
	public Map<InstanceType, List<String>> getSupportedIdentifier() {
		return Map.of(AzureBaseElementType.AZURE_WORKITEM.getType(), List.of(AzureBaseElementType.AZURE_WORKITEM.getType().name().toString().toLowerCase()));
	}

	protected ProgressEntry dispatchNewStartedActivity(String activity) {
		ProgressEntry pe = new ProgressEntry("AzureConnector", activity, Status.Started);
		if (obs != null)
			obs.dispatchNewEntry(pe);
		return pe;
	}
	
	protected ProgressEntry dispatchAtomicFinishedActivity(String activity) {
		ProgressEntry pe = new ProgressEntry("AzureConnector", activity, Status.Completed);
		if (obs != null)
			obs.dispatchNewEntry(pe);
		return pe;
	}

	@Override
	public void handleServiceRequest(Workspace workspace, Collection<Operation> operations) {
		// TODO Auto-generated method stub
		
	}
	
    public void processWebhook(byte[] json) {
//        AzureWebhookParser.parseJson(json, changePatcher);
//        workspace.concludeTransaction();
    	updateQueue.add(json);
    }

	@Override
	protected void finalize() throws Throwable {
		if (updateQueue != null)
			updateQueue.add(new byte[] {});
	}

	private class UpdateSynchronizer implements Runnable {
	    BlockingQueue<byte[]> queue;
	    
	    public UpdateSynchronizer(BlockingQueue<byte[]> queue) {
	    	this.queue = queue;
	    }
	    
		public void run() {
	        try {
	            while (true) {
	            	byte[] json = queue.take();
	                if (json.length == 0) { // poison pill
	                	log.info("AZURE Service UpdateSynchronizer shutting down.");
	                	return;
	                } else {
	                	AzureWebhookParser.parseJson(json, changePatcher);
	                	workspace.concludeTransaction();
	                }
	            }
	        } catch (InterruptedException e) {
	            Thread.currentThread().interrupt();
	        }
	    }
	}
		
	
}
