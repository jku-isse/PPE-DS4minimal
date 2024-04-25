package at.jku.isse.designspace.jama.service;


import java.io.FileReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import at.jku.isse.designspace.artifactconnector.core.IArtifactProvider;
import at.jku.isse.designspace.artifactconnector.core.endpoints.grpc.service.ServiceResponse;
import at.jku.isse.designspace.artifactconnector.core.idcache.IdCache;
import at.jku.isse.designspace.artifactconnector.core.model.BaseElementType;
import at.jku.isse.designspace.artifactconnector.core.monitoring.IProgressObserver;
import at.jku.isse.designspace.artifactconnector.core.monitoring.ProgressEntry;
import at.jku.isse.designspace.artifactconnector.core.monitoring.ProgressEntry.Status;
import at.jku.isse.designspace.core.events.Operation;
import at.jku.isse.designspace.core.model.Id;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.Property;
import at.jku.isse.designspace.core.model.ServiceProvider;
import at.jku.isse.designspace.core.model.SetProperty;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.service.ServiceRegistry;
import at.jku.isse.designspace.core.service.WorkspaceService;
import at.jku.isse.designspace.jama.connector.restclient.httpconnection.ApacheHttpClient;
import at.jku.isse.designspace.jama.connector.restclient.httpconnection.JamaClient;
import at.jku.isse.designspace.jama.connector.restclient.jamaclient.IJamaAPI;
import at.jku.isse.designspace.jama.connector.restclient.jamaclient.JamaAPI;
import at.jku.isse.designspace.jama.connector.restclient.jamaclient.JamaAPIMock;
import at.jku.isse.designspace.jama.model.JamaBaseElementType;
import at.jku.isse.designspace.jama.model.JamaSchemaConverter;
import at.jku.isse.designspace.jama.replaying.JamaActivity;
import at.jku.isse.designspace.jama.replaying.JamaActivity.ObjectTypes;
import at.jku.isse.designspace.jama.updateservice.CacheStatus;
import at.jku.isse.designspace.jama.updateservice.ChangeStreamPoller;
import at.jku.isse.designspace.jama.updateservice.InstanceBasedCacheStatus;
import at.jku.isse.designspace.jama.updateservice.MonitoringScheduler;
import at.jku.isse.designspace.jama.utility.AccessToolsJSON;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@DependsOn({"controleventengine"})
//@ConditionalOnExpression(value = "${jama.enabled:true}") //--> always active, but loads Mock data if not enabled
public class JamaService implements IJamaService, ServiceProvider, IArtifactProvider {

	@Autowired
	private IProgressObserver obs;
	
    private IdCache idCache;
    private Workspace workspace;

    private String jamaBaseUrl;
    private IJamaAPI jamaAPI;
    protected JamaSchemaConverter jamaSchemaConverter;
    private MonitoringScheduler ms;
    
//    private UpdateManager updateManager;
//    private UpdateMemory updateMemory;

    private int itemsTransferred;

    private String originServer = "http://localhost:8080";
    
    private Map<Integer, Object> pickListOptions;
    
    private Set<JamaActivity> completeHistory = new HashSet<>();

    public JamaService() { //UpdateManager updateManager, UpdateMemory updateMemory) {
        ServiceRegistry.registerService(this);
    }
    
    public String getName(){
        return "JamaService";
    }
    public String getVersion(){
        return "1.0.0";
    }
    public int getPriority(){
        return 107;
    }
    public boolean isPersistenceAware(){
        return true;
    }

    @Override
    public void initialize() {
        log.debug("---JAMA-SERVICE");
        ProgressEntry initService = dispatchNewStartedActivity("Initializing Connector");
        this.workspace = WorkspaceService.PUBLIC_WORKSPACE;
        this.idCache = new IdCache(this.workspace, "JamaId2DesignspaceIdCache");
        this.jamaSchemaConverter = new JamaSchemaConverter(this.workspace);

        Properties props = new Properties();
        try {
            FileReader reader = new FileReader("./application.properties");
            props.load(reader);
            
            
            if (Boolean.parseBoolean(props.getProperty("jama.mock", "false"))) {            	
                	this.jamaAPI = new JamaAPIMock(obs); 
            } else {            
            	JamaClient jc = new JamaClient(new ApacheHttpClient(), props.getProperty("jama.serverURI"), props.getProperty("jama.user"), props.getProperty("jama.password"));
            	this.jamaAPI = new JamaAPI(jc, obs);
            	originServer = props.getProperty("jama.serverURI").trim();
            	
            	int pollInterval = Integer.parseInt(props.getProperty("jama.pollIntervalInMinutes", "-1"));            	
            	if (pollInterval > 0) { // setup polling            		
            		CacheStatus cs = new InstanceBasedCacheStatus(InstanceBasedCacheStatus.getInstance(workspace));
            		ms = new MonitoringScheduler();
            		Arrays.asList(props.getProperty("jama.pollProjectIds", "")            				
            				.split(","))
            		.stream()
            		.map(pId -> pId.strip())
            		.filter(pId -> pId.matches("\\d+")) // checks for positive integer, yes there is no library that matches for pos INTEGERS only
            		.forEach(pId -> {            		
            			int pIntId = Integer.parseInt(pId);            			
            			ChangeStreamPoller csp = new ChangeStreamPoller(jc, this, pIntId, cs, pollInterval);            			
            			ms.registerAndStartTask(csp);
            			dispatchAtomicFinishedActivity("Scheduled Polling for Updates for Project "+pIntId);
            		});
            	}  
            }
            this.pickListOptions = new HashMap<>();
            Map<Integer, Object> pickListOptions = this.jamaAPI.getAllPickListOptions();

            for (Integer key : pickListOptions.keySet()) {
                Map<String, Object> optionMap = AccessToolsJSON.accessMap(pickListOptions, key);
                this.pickListOptions.put(key, optionMap);
            }

            //call for initializing the Jama related InstanceTypes
            JamaBaseElementType.getJamaTypeFolder();
            fetchAndTransferAllItemTypes();

            if (Boolean.parseBoolean(props.getProperty("jama.sync", "false"))) {
            	fetchAndTransferAllItems();
            }
                       
        } catch (Exception ioe) {
        	initService.setStatusAndComment(Status.Failed, ioe.getMessage());
            log.warn("JAMA-SERVICE:Service cannot be initialized!",ioe);
        }

        //call for initializing the Jama related InstanceTypes
        JamaBaseElementType.getJamaTypeFolder();
        initService.setStatus(Status.Completed);
    }

    private void fetchAndTransferAllItemTypes() {
        Map<Integer, Object> itemTypes = this.jamaAPI.getAllItemTypes();
        for (Object itemType_ : itemTypes.values()) {
            try {
                Map<String, Object> itemType = (Map<String, Object>) itemType_;
                this.getJamaSchemaConverter().getOrCreateJamaItemType(itemType);
            } catch (ClassCastException ce) {
                log.debug("JAMA-SERVICE: The injection of IJamaAPI provided an incorrect format for getAllItemTypes()");
            }
        }
    }

    private void fetchAndTransferAllItems() {
        
        Map<Integer, Object> idReleaseMap = jamaAPI.getAllReleasesMappedToId();
        for (Object item : idReleaseMap.values()) {
            try {
                transferRelease((Map<String, Object>) item);
            } catch (ClassCastException ce) {
                log.debug("JAMA-SERVICE: The injection of IJamaAPI provided an incorrect format for getAllReleasesMappedToId()");
            }
        }
        Map<Integer, Object> idProjectMap = jamaAPI.getAllProjectsMappedToId();
        for (Object item : idProjectMap.values()) {
            try {
                transferProject((Map<String, Object>) item);
            } catch (ClassCastException ce) {
                log.debug("JAMA-SERVICE: The injection of IJamaAPI provided an incorrect format for getAllProjectsMappedToId()");
            }
        }
    	
    	Map<Integer, Object> idItemMap = jamaAPI.getAllItemsMappedToId();
        for (Object item : idItemMap.values()) {
            try {
                transferItem((Map<String, Object>) item);
            } catch (ClassCastException ce) {
                log.debug("JAMA-SERVICE: The injection of IJamaAPI provided an incorrect format for getAllItemsMappedToId()");
            }
        }

    }

    public Set<Instance> getAllKnownJamaItems() {
    	return JamaBaseElementType.JAMA_CORE_ITEM.getType().instancesIncludingThoseOfSubtypes().collect(Collectors.toSet());
    }
    
    private Optional<Instance> findInstance(String serviceId) {
        Id id = this.idCache.getDesignspaceId(serviceId);
        if (id != null) {
            return Optional.ofNullable(this.workspace.findElement(id));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Instance> getJamaItem(String identifier, JamaIdentifiers idType) {
        switch(idType) {
		case JamaItemDocKey:
			 return getJamaItem(identifier);			 
		case JamaItemId:
			try {
                Integer id = Integer.parseInt(identifier);
                return getJamaItem(id);
            } catch (NumberFormatException ne) {
                log.debug("JAMA-SERVICE: The given identifier could not be parsed to an int (Incorrect idDescriptor or identifier)");
            }
		case JamaProjectId:
			log.warn("JAMA-SERVICE: fetching Projects not supported yet");
			break;
		case JamaReleaseId:
			log.warn("JAMA-SERVICE: fetching Releases not supported yet");
			break;		                    	    
        case JamaFilterId:
        	try {
                Integer id = Integer.parseInt(identifier);
             // as we dont know what is inside the filter, we always fetch and override 
                List<Instance> fetched = fetchViaFilter(id);
                if (fetched != null && fetched.size() >0 )
                	return Optional.of(fetched.get(0));
            } catch (NumberFormatException ne) {
                log.debug("JAMA-SERVICE: The given identifier for a jama filter could not be parsed to an int (Incorrect idDescriptor or identifier)");
            }        	        	
        	break;
        }
        return Optional.empty();
    }
        
    public List<Instance> forceRefetch(String identifier, JamaIdentifiers idType) {
        List<Instance> fetchedItems = new LinkedList<>();
    	switch(idType) {
		case JamaItemDocKey:			
			Map<String, Object> result = this.jamaAPI.getJamaItem(identifier);
            Instance inst = transferItem(result);			 
            fetchedItems =  inst != null ? List.of(inst) : Collections.emptyList();
		case JamaItemId:
			try {
                Integer id = Integer.parseInt(identifier);
                Map<String, Object> result2 = this.jamaAPI.getJamaItem(id);
                Instance inst2 = transferItem(result2);			 
                fetchedItems =  inst2 != null ? List.of(inst2) : Collections.emptyList();
            } catch (NumberFormatException ne) {
                log.debug("JAMA-SERVICE: The given identifier could not be parsed to an int (Incorrect idDescriptor or identifier)");
            }
		case JamaProjectId:
			log.warn("JAMA-SERVICE: fetching Projects not supported yet");
			break;
		case JamaReleaseId:
			log.warn("JAMA-SERVICE: fetching Releases not supported yet");
			break;		                    	    
        case JamaFilterId:
        	try {
                Integer id = Integer.parseInt(identifier);
                fetchedItems =  fetchViaFilter(id);
            } catch (NumberFormatException ne) {
                log.debug("JAMA-SERVICE: The given identifier for a jama filter could not be parsed to an int (Incorrect idDescriptor or identifier)");
            }        	        	
        	break;
        }
    	if (!fetchedItems.isEmpty()) {
    		this.workspace.concludeTransaction();
    	}
    	return fetchedItems;    	       
    }
    
    private List<Instance> fetchViaFilter(int filterId) {
    	try {
			List<Map<String, Object>> items = this.jamaAPI.getItemsViaFilter(filterId);
			return items.stream()
			.map(entry -> transferItem(entry))
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
		} catch (Exception e) {			
			e.printStackTrace();
		}
    	return Collections.emptyList();
    }

    private Optional<Instance> getJamaItem(String key) {
        String identifier = "JamaItem/" + key;
        Optional<Instance> instance_ = findInstance(identifier);
        if (instance_.isPresent()) {
            if (instance_.get().getPropertyAsValue(BaseElementType.FULLY_FETCHED).equals(true)) {
                return instance_;
            }
            Map<String, Object> result = this.jamaAPI.getJamaItem(key);
            return Optional.of(transferItem(result));
        }

        Map<String, Object> result = this.jamaAPI.getJamaItem(key);
        if (result == null) {
        	log.info("Jama Item not found: "+key);
        	return Optional.empty();
        } else
        	return Optional.ofNullable(transferItem(result));
    }

    private Optional<Instance> getJamaItem(Integer id) {

        String identifier = "JamaItem/" + id;
        Optional<Instance> instance_ = findInstance(identifier);
        if (instance_.isPresent()) {
            if (instance_.get().getPropertyAsValue(BaseElementType.FULLY_FETCHED).equals(true)) {
                return instance_;
            }
        }
        Map<String, Object> result = this.jamaAPI.getJamaItem(id);
        if (result == null) {
        	log.info("Jama Item not found: "+id);
        	return Optional.empty();
        } else 
        	return Optional.ofNullable(transferItem(result));

    }
    
    
    public void updateItems(List<JamaActivity> jamaUpdates) {
		Map<ObjectTypes, Set<Integer>> action2Item = jamaUpdates.stream()
				.filter(jdo -> jdo instanceof JamaActivity)
				.map(JamaActivity.class::cast)
				.map(ja -> { return new AbstractMap.SimpleEntry<ObjectTypes, Integer>(ja.getObjectType(), ja.getItemId()); })
				.collect(Collectors.groupingBy(AbstractMap.SimpleEntry::getKey, Collectors.mapping(AbstractMap.SimpleEntry::getValue, Collectors.toSet())));		
		processItemContentUpdate(action2Item.getOrDefault(ObjectTypes.ITEM, Collections.EMPTY_SET));
		processItemRelationUpdate(action2Item.getOrDefault(ObjectTypes.RELATIONSHIP, Collections.EMPTY_SET));
		workspace.concludeTransaction();
    }

    private void processItemContentUpdate(Set<Integer> changedItems) {
    	changedItems.stream()
    		.map(id -> this.jamaAPI.getJamaItem(id))
    		.filter(jdo -> jdo != null)
    		.forEach(jdo -> transferItem(jdo));
    }
    
    private void processItemRelationUpdate(Set<Integer> changedItems) {
    	changedItems.stream()
		.map(id -> this.jamaAPI.getJamaItem(id))
		.filter(jdo -> jdo != null)
		.forEach(jdo -> transferItem(jdo)); //TODO: separate up/downstream fetching from main data fetching
    }
    
    private Instance transferItem(Map<String, Object> itemJson) {
        Map<String, Object> data = AccessToolsJSON.accessMap(itemJson, "data");

        List<Object> upstream = AccessToolsJSON.accessArray(itemJson, "upstream");
        List<Object> downstream = AccessToolsJSON.accessArray(itemJson, "downstream");

        if (data != null) {
            int itemId = AccessToolsJSON.accessInteger(data, "id");
            String designspaceItemId = "JamaItem/" + itemId;


            int itemTypeId = AccessToolsJSON.accessInteger(data, "itemType");
            String designspaceItemTypeId = "JamaItemType/" + itemTypeId;

            //ToDo: Take care of customfields
            Optional<InstanceType> instanceType = this.getJamaSchemaConverter().getJamaInstanceType(designspaceItemTypeId);
            if (instanceType.isEmpty()) {
                Map<String, Object> itemType = this.jamaAPI.getItemType(itemTypeId);
                if (itemType == null) {
                    log.debug("JAMA-SERVICE: Cannot find the the itemType with the id " + designspaceItemTypeId);
                    return null;
                }
                instanceType = this.getJamaSchemaConverter().getOrCreateJamaItemType(itemType);
                if (instanceType.isEmpty()) {
                    log.debug("JAMA-SERVICE: Ran into problems while creating InstanceType for the itemType with the id " + designspaceItemTypeId);
                    return null;
                }
            }

            Optional<Instance> item_ = findInstance(designspaceItemId);
            Instance item;
            if (!item_.isPresent()) {
                item = this.workspace.createInstance(instanceType.get(), designspaceItemId);
            } else {
                item = item_.get();
                item.setInstanceType(instanceType.get());
            }

            Optional<Instance> itemTypeInstance = findInstance(designspaceItemTypeId);
            if (itemTypeInstance.isPresent()) {
                item.getPropertyAsSingle(JamaBaseElementType.ITEM_TYPE).set(itemTypeInstance.get());
                item.getPropertyAsSingle(JamaBaseElementType.ITEM_TYPE_SHORT).set(itemTypeInstance.get().name());
                item.getPropertyAsSingle(JamaBaseElementType.ITEM_TYPE_NAME).set(itemTypeInstance.get().getPropertyAsValue(JamaBaseElementType.DISPLAY).toString());
            } else {
                Map<String, Object> itemTypeDataMap = this.jamaAPI.getItemType(itemTypeId);
                if (itemTypeDataMap != null) {
                    //Map<String, Object> itemTypeDataMap = AccessToolsJSON.accessMap(itemTypeMap, "data");
                    Instance itemType = transferItemType(itemTypeDataMap);
                    item.getPropertyAsSingle(JamaBaseElementType.ITEM_TYPE).set(itemType);
                    item.getPropertyAsSingle(JamaBaseElementType.ITEM_TYPE_SHORT).set(itemType.name());
                    item.getPropertyAsSingle(JamaBaseElementType.ITEM_TYPE_NAME).set(itemType.getPropertyAsValue(JamaBaseElementType.DISPLAY).toString());
                }
            }


            item.getPropertyAsSingle(BaseElementType.ID).set(itemId+"");
            item.getPropertyAsSingle(JamaBaseElementType.GLOBAL_ID).set(AccessToolsJSON.accessString(data, JamaBaseElementType.GLOBAL_ID));

            String documentKey = AccessToolsJSON.accessString(data, JamaBaseElementType.DOCUMENTKEY);
            item.getPropertyAsSingle(BaseElementType.KEY).set(documentKey);

            int projectId = AccessToolsJSON.accessInteger(data, "project");
            item.getPropertyAsSingle(BaseElementType.LINK).set(originServer+"/perspective.req#/items/"+itemId+"?project="+projectId);
            mapProjectToField(projectId, JamaBaseElementType.PROJECT, item);

            item.getPropertyAsSingle(JamaBaseElementType.CREATED_DATE).set(AccessToolsJSON.accessString(data, JamaBaseElementType.CREATED_DATE));
            item.getPropertyAsSingle(JamaBaseElementType.MODIFIED_DATE).set(AccessToolsJSON.accessString(data, JamaBaseElementType.MODIFIED_DATE));
            item.getPropertyAsSingle(JamaBaseElementType.LAST_ACTIVITY).set(AccessToolsJSON.accessString(data, JamaBaseElementType.LAST_ACTIVITY));

            Integer createdById = AccessToolsJSON.accessInteger(data, "createdBy");
            if (createdById != null) {
                mapUserToField(createdById, JamaBaseElementType.CREATED_BY, item);
            }

            Integer modifiedById = AccessToolsJSON.accessInteger(data, "modifiedBy");
            if (modifiedById != null) {
                mapUserToField(modifiedById, JamaBaseElementType.MODIFIED_BY, item);
            }

            Map<String, Object> fields = AccessToolsJSON.accessMap(data, JamaBaseElementType.FIELDS);

            for (Map.Entry<String, Object> field : fields.entrySet()) {
            	Optional<String> propOpt = this.getJamaSchemaConverter().getPropertyForFieldName(designspaceItemTypeId, field.getKey());
            	String fieldName = field.getKey();
            	
                if (!field.getKey().equals(BaseElementType.ID) && propOpt.isPresent() && item.hasProperty(propOpt.get())) {
                    Property property = item.getProperty(propOpt.get());
                    if (!property.name.equals(BaseElementType.ID)) {
                        Optional<String> jamaType_ = this.getJamaSchemaConverter().getJamaTypeForFieldNameOfItemType(designspaceItemTypeId, property.name);
                        if (jamaType_.isPresent()) {
                            String jamaType = jamaType_.get();

                            try {
                                switch (jamaType) {
                                    case "USER":
                                        Integer userId = AccessToolsJSON.accessInteger(fields, fieldName);
                                        mapUserToField(userId, property.name, item);
                                        break;
                                    case "PROJECT":
                                        Integer project_Id = AccessToolsJSON.accessInteger(fields, fieldName);
                                        mapProjectToField(project_Id, property.name, item);
                                        break;
                                    case "MULTI-LOOKUP":
                                        SetProperty lookUpValues = item.getPropertyAsSet(property.name);
                                        ArrayList<Object> lookUpIds = AccessToolsJSON.accessArray(fields, fieldName);
                                        for (Object lookUpId : lookUpIds) {
                                            lookUpValues.add(lookUpId);
                                        }
                                        break;
                                    case "LOOKUP":
                                        Integer lookUpId = AccessToolsJSON.accessInteger(fields, fieldName);
                                        if (lookUpId == -1) // not found, lookups are always positive
                                        	break;
                                        Optional<String> optionName = getPickListOptionName(lookUpId);
                                        if (optionName.isPresent()) {
                                            item.getPropertyAsSingle(property.name).set(optionName.get());
                                        } else {
                                        	log.warn(String.format("Lookup value %s not found for property %s of artifact type %s", lookUpId, property.name, designspaceItemTypeId));
                                        }
                                        break;
                                    case "RELEASE":
                                        Integer releaseId = AccessToolsJSON.accessInteger(fields, fieldName);
                                        mapReleaseToField(releaseId, property.name, item);
                                        break;
                                    case "DATE":
                                    case "URL_STRING":
                                    case "CALCULATED":
                                    case "STEPS":
                                    case "TEST_CASE_STATUS":	
                                    case "TEST_RUN_RESULTS":
                                    case "STRING":
                                    case "TEXT":
                                        String stringValue = AccessToolsJSON.accessString(fields, fieldName);
                                        item.getPropertyAsSingle(property.name).set(stringValue);
                                        break;
                                    case "BOOLEAN":
                                        Boolean boolValue = AccessToolsJSON.accessBoolean(fields, fieldName);
                                        item.getPropertyAsSingle(property.name).set(boolValue);
                                        break;
                                    case "INTEGER":
                                        Integer intValue = AccessToolsJSON.accessInteger(fields, fieldName);
                                        item.getPropertyAsSingle(property.name).set(intValue);
                                        break;
                                    default:
                                        break;
                                }
                            } catch (Exception e) {
                                log.debug("JAMA-SERVICE: transfering the property " + property.name + " failed for the item " + designspaceItemId + "!");
                            }
                        } else {
                        	log.debug("JAMA-SERVICE: didnt find property type for fieldname and propertyname: "+fieldName+" :: "+property.name);	
                        }
                    }
                } else {
                	//log.debug(item.getPropertyNames().toString());
                	String mapped = propOpt.isPresent() ? propOpt.get() : "Unmapped";
                	log.trace("JAMA-SERVICE: didnt find property for fieldname: "+fieldName+" mapped to: "+mapped);
                }                	
            }


             if (downstream != null) {
                //ToDo: Taking care of relationships
                SetProperty<Instance> relatesTo = item.getPropertyAsSet(JamaBaseElementType.DOWNSTREAM);
                relatesTo.clear(); // as we also use this to fetch in updates
                for (Object id : downstream) {
                    Instance artifact = findArtifactOrCreatePlaceholder("JamaItem/" + id, id+"");
                    //if (!relatesTo.contains(artifact)) {
                        relatesTo.add(artifact);
                    //}
                }
            }

            if (upstream != null) {
                SetProperty<Instance> relatedBy = item.getPropertyAsSet(JamaBaseElementType.UPSTREAM);
                relatedBy.clear(); // as we also use this to fetch in updates
                for (Object id : upstream) {
                    Instance artifact = findArtifactOrCreatePlaceholder("JamaItem/" + id, id+"");
                   // if (!relatedBy.contains(artifact)) {
                        relatedBy.add(artifact);
                    //}
                }
            }

            this.idCache.addEntry(designspaceItemId, item.id());
            if (documentKey != null) {
                this.idCache.addEntry("JamaItem/" + documentKey, item.id());
            }

            log.debug("JAMA-SERVICE: Transferred the item with the id " + designspaceItemId);

            item.getPropertyAsSingle(BaseElementType.FULLY_FETCHED).set(true);
            
            ArrayList<Object> history = AccessToolsJSON.accessArray(itemJson, "history");
            if (history != null) processHistory(history);
            
            return item;
        }

        log.debug("JAMA-SERVICE: Failed while transfering item (method called with invalid json format!)");
        return null;
    }
    
    private void processHistory(ArrayList<Object> history) {
    	history.stream().map(obj -> (Map<String, Object>)obj)
    		.map(jsonMap -> JamaActivity.fromJson(jsonMap))
    		.filter(Objects::nonNull)
    		.forEach(ja -> this.getCompleteHistory().add(ja));
    }

    private Instance findArtifactOrCreatePlaceholder(String id, String intId) {
        Instance artifactInstance = findArtifactInstance(id);
        if (artifactInstance != null) {
            return artifactInstance;
        } else {
            artifactInstance = this.workspace.createInstance(JamaBaseElementType.JAMA_CORE_ITEM.getType(), id);
            artifactInstance.getPropertyAsSingle(BaseElementType.ID).set(intId);
            artifactInstance.getPropertyAsSingle(BaseElementType.FULLY_FETCHED).set(false);
            this.idCache.addEntry(id, artifactInstance.id());
        }
        return artifactInstance;
    }

    private Instance findArtifactInstance(String id) {
        Id designspaceId = this.idCache.getDesignspaceId(id);
        if (designspaceId != null) {
            Instance artifactInstance = this.workspace.findElement(designspaceId);
            if (artifactInstance != null && artifactInstance.hasProperty(BaseElementType.FULLY_FETCHED)) {
                return artifactInstance;
            }
        }
        return null;
    }

    public Optional<String> getPickListOptionName(int optionId) {
        Map<String, Object> optionMap = AccessToolsJSON.accessMap(this.pickListOptions, optionId);
        if (optionMap != null) {
            String optionName = AccessToolsJSON.accessString(optionMap, JamaBaseElementType.NAME);
            if (optionName != null) {
                return Optional.of(optionName);
            }
        }
        return Optional.empty();
    }

    public void mapReleaseToField(int releaseId, String fieldName, Instance item) {
    	if (releaseId < 0) {
       	 item.getPropertyAsSingle(fieldName).set(null);
       	 return;
       }
    	
    	String designspaceReleaseId = "JamaRelease/" + releaseId;
        Optional<Instance> jamaRelease = findInstance(designspaceReleaseId);
        if (jamaRelease.isPresent()) {
            item.getPropertyAsSingle(fieldName).set(jamaRelease.get());
        } else {
            Map<String, Object> releaseMap = this.jamaAPI.getRelease(releaseId);
            if (releaseMap != null) {
            	Map<String, Object> releaseDataMap = releaseMap.containsKey("data") ? AccessToolsJSON.accessMap(releaseMap, "data") : releaseMap;
                Instance releaseInstance = this.transferRelease(releaseDataMap);
                item.getPropertyAsSingle(fieldName).set(releaseInstance);
            }
        }
    }

    // only needed for replaying of events/history/activities
    public void mapReleaseToField(String releaseName, String fieldName, Instance item) {
    	if (releaseName == null || releaseName.length() == 0) {
          	 item.getPropertyAsSingle(fieldName).set(null);
          	 return;
        } else {
        	Instance release = name2Release.get(releaseName);
        	if (release != null)
        		item.getPropertyAsSingle(fieldName).set(release);
        	else
        		log.warn("Could not resolve release: "+releaseName);
    	}
    }
    
    public void mapUserToField(int userId, String fieldName, Instance item) {
        if (userId < 0) {
        	 item.getPropertyAsSingle(fieldName).set(null);
        	 return;
        }
    	String designspaceUserId = "JamaUser/" + userId;
        Optional<Instance> jamaUser = findInstance(designspaceUserId);
        if (jamaUser.isPresent()) {
            item.getPropertyAsSingle(fieldName).set(jamaUser.get());
        } else {
            Map<String, Object> userMap = this.jamaAPI.getJamaUser(userId);
            if (userMap != null) {
                //Map<String, Object> projectDataMap = AccessToolsJSON.accessMap(userMap, "data");
                Instance userInstance = this.transferUser(userMap);
                item.getPropertyAsSingle(fieldName).set(userInstance);
            }
        }
    }

    public void mapProjectToField(int projectId, String fieldName, Instance item) {
    	if (projectId < 0) {
       	 item.getPropertyAsSingle(fieldName).set(null);
       	 return;
       }
    	
    	String designspaceProjectId = "JamaProject/" + projectId;
        Optional<Instance> projectInstance = findInstance(designspaceProjectId);

        if (projectInstance.isPresent()) {
            item.getProperty(fieldName).set(projectInstance.get());
        } else {
            Map<String, Object> projectMap = this.jamaAPI.getJamaProject(projectId);
            if (projectMap != null) {                
                Instance project = transferProject(projectMap);
                item.getPropertyAsSingle(fieldName).set(project);
            }
        }
    }

    private Instance transferUser(Map<String, Object> userJson) {
        int userId = AccessToolsJSON.accessInteger(userJson, "id");
        String designspaceUserId = "user_" + userId;
        Optional<Instance> user_ = findInstance(designspaceUserId);

        if (user_.isPresent()) {
            return user_.get();
        }

        String userName = AccessToolsJSON.accessString(userJson, JamaBaseElementType.USERNAME);
        Instance user = WorkspaceService.createInstance(this.workspace, userName, JamaBaseElementType.JAMA_USER.getType());

        user.getPropertyAsSingle(BaseElementType.ID).set(designspaceUserId);
        user.getPropertyAsSingle(BaseElementType.KEY).set(designspaceUserId);

        user.getPropertyAsSingle(JamaBaseElementType.USERNAME).set(userName);
        user.getPropertyAsSingle(JamaBaseElementType.USER_ID).set(AccessToolsJSON.accessString(userJson, JamaBaseElementType.USER_ID));
        user.getPropertyAsSingle(JamaBaseElementType.LAST_NAME).set(AccessToolsJSON.accessString(userJson, JamaBaseElementType.LAST_NAME));
        user.getPropertyAsSingle(JamaBaseElementType.FIRST_NAME).set(AccessToolsJSON.accessString(userJson, JamaBaseElementType.FIRST_NAME));
        user.getPropertyAsSingle(JamaBaseElementType.EMAIL).set(AccessToolsJSON.accessString(userJson, JamaBaseElementType.EMAIL));
        user.getPropertyAsSingle(JamaBaseElementType.PHONE).set(AccessToolsJSON.accessString(userJson, JamaBaseElementType.PHONE));
        user.getPropertyAsSingle(JamaBaseElementType.TITLE).set(AccessToolsJSON.accessString(userJson, JamaBaseElementType.TITLE));
        user.getPropertyAsSingle(JamaBaseElementType.LOCATION).set(AccessToolsJSON.accessString(userJson, JamaBaseElementType.LOCATION));

        this.idCache.addEntry(designspaceUserId, user.id());
        return user;
    }

    private Instance transferProject(Map<String, Object> projectJson) {
        Map<String, Object> fields = AccessToolsJSON.accessMap(projectJson, "fields");
        String projectName = AccessToolsJSON.accessString(fields, "name");
        Instance project = this.workspace.createInstance(JamaBaseElementType.JAMA_PROJECT.getType(), projectName);

        int projectId = AccessToolsJSON.accessInteger(projectJson, "id");
        String designspaceProjectId = "JamaProject/" + projectId;

        project.getPropertyAsSingle(BaseElementType.ID).set(designspaceProjectId);
        project.getPropertyAsSingle(JamaBaseElementType.DESCRIPTION).set(AccessToolsJSON.accessString(fields, JamaBaseElementType.DESCRIPTION));
        project.getPropertyAsSingle(BaseElementType.NAME).set(projectName);
        project.getPropertyAsSingle(JamaBaseElementType.CREATED_DATE).set(AccessToolsJSON.accessString(projectJson, JamaBaseElementType.CREATED_DATE));
        project.getPropertyAsSingle(JamaBaseElementType.MODIFIED_DATE).set(AccessToolsJSON.accessString(projectJson, JamaBaseElementType.MODIFIED_DATE));
        project.getPropertyAsSingle(BaseElementType.KEY).set(AccessToolsJSON.accessString(projectJson, JamaBaseElementType.PROJECT_KEY));

        Integer createdById = AccessToolsJSON.accessInteger(projectJson, "createdBy");
        if (createdById != null) {
            mapUserToField(createdById, JamaBaseElementType.CREATED_BY, project);
        }

        Integer modifiedById = AccessToolsJSON.accessInteger(projectJson, "modifiedBy");
        if (modifiedById != null) {
            mapUserToField(modifiedById, JamaBaseElementType.MODIFIED_BY, project);
        }

        this.idCache.addEntry(designspaceProjectId, project.id());
        return project;
    }

    private Instance transferItemType(Map<String, Object> itemTypeJson) {
        String displayName = AccessToolsJSON.accessString(itemTypeJson, "typeKey");
        Instance itemType = this.workspace.createInstance(JamaBaseElementType.JAMA_ITEM_TYPE.getType(), displayName);

        int itemTypeId = AccessToolsJSON.accessInteger(itemTypeJson, "id");
        String designspaceItemTypeId = "JamaItemType/" + itemTypeId;

        itemType.getPropertyAsSingle(BaseElementType.ID).set(designspaceItemTypeId);
        itemType.getPropertyAsSingle(BaseElementType.KEY).set(designspaceItemTypeId);
        itemType.getPropertyAsSingle(JamaBaseElementType.DISPLAY).set(AccessToolsJSON.accessString(itemTypeJson, JamaBaseElementType.DISPLAY));
        itemType.getPropertyAsSingle(JamaBaseElementType.TYPE_KEY).set(AccessToolsJSON.accessString(itemTypeJson, JamaBaseElementType.TYPE_KEY));
        itemType.getPropertyAsSingle(JamaBaseElementType.DESCRIPTION).set(AccessToolsJSON.accessString(itemTypeJson, JamaBaseElementType.DESCRIPTION));

        this.idCache.addEntry(designspaceItemTypeId, itemType.id());
        return itemType;
    }

    private Map<String, Instance> name2Release = new HashMap<>();
    
    private Instance transferRelease(Map<String, Object> releaseJson) {
        String name = AccessToolsJSON.accessString(releaseJson, "name");
        Instance release = this.workspace.createInstance(JamaBaseElementType.JAMA_RELEASE.getType(), name);

        int releaseId = AccessToolsJSON.accessInteger(releaseJson, "id");
        String designspaceReleaseId = "JamaRelease/" + releaseId;

        release.getPropertyAsSingle(BaseElementType.ID).set(designspaceReleaseId);
        release.getPropertyAsSingle(BaseElementType.KEY).set(designspaceReleaseId);

        release.getPropertyAsSingle(JamaBaseElementType.RELEASE_DATE).set(AccessToolsJSON.accessString(releaseJson, JamaBaseElementType.RELEASE_DATE));
        String descr = AccessToolsJSON.accessString(releaseJson, JamaBaseElementType.DESCRIPTION);
        release.getPropertyAsSingle(JamaBaseElementType.DESCRIPTION).set(descr);
        release.getPropertyAsSingle(JamaBaseElementType.TYPE).set(AccessToolsJSON.accessString(releaseJson, JamaBaseElementType.TYPE));
        release.getPropertyAsSingle(JamaBaseElementType.ARCHIVED).set(AccessToolsJSON.accessBoolean(releaseJson, JamaBaseElementType.ARCHIVED));

        this.idCache.addEntry(designspaceReleaseId, release.id());
        name2Release.put(name, release);
        return release;
    }


    @Override
    public InstanceType getArtifactInstanceType() {
        return JamaBaseElementType.JAMA_CORE_ITEM.getType();
    }




    private ServiceResponse getNontransactionalServiceResponse(String id, String identifierType, boolean forceRefetch) {
    	if (id != null) {
        	try {
                JamaIdentifiers idType = IJamaService.JamaIdentifiers.valueOf(identifierType);
                Optional<Instance> issueInstance = null;
                if (forceRefetch) {
                	List<Instance> resp = forceRefetch(id, idType);
                	issueInstance = resp.size() > 0 ? Optional.ofNullable(resp.get(0)) : Optional.empty();
                } else {
                	issueInstance = getJamaItem(id, idType);
                }
                if (issueInstance.isPresent()) {
                    
                    return new ServiceResponse(ServiceResponse.SUCCESS, "Jama", "Successful fetch", issueInstance.get().id().toString());
                }

                return new ServiceResponse(ServiceResponse.UNAVAILABLE, "Jama", "No issue with given key", "");
        	} catch (Exception e) {
        		e.printStackTrace();
        		return new ServiceResponse(ServiceResponse.UNKNOWN, "Jama", "The request id was invalid", "");
        	}
        }
        else {
            return new ServiceResponse(ServiceResponse.UNKNOWN, "Jama", "The request id was null", "");
        }
    }
    
    @Override
    public ServiceResponse getServiceResponse(String id, String identifierType) {
    	return getServiceResponse(id, identifierType, false);
    }
    
    @Override
    public ServiceResponse getServiceResponse(String id, String identifierType, boolean forceRefetch) {
    	ServiceResponse resp =  getNontransactionalServiceResponse(id, identifierType, forceRefetch);
    	if (resp.getKind()==ServiceResponse.SUCCESS) {
    		this.workspace.concludeTransaction();
    	}
    	return resp;
    }
    
    @Override
    public ServiceResponse[] getServiceResponse(Set<String> ids, String identifierType, boolean forceRefetch) {
    	ProgressEntry pe = dispatchNewStartedActivity(String.format("Batch Loading %s items", ids.size()));
    	AtomicInteger successCount = new AtomicInteger(0);
        List<ServiceResponse> serviceResponses = ids.stream()
        	.map(id -> getNontransactionalServiceResponse(id, identifierType, forceRefetch))
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
    public ServiceResponse[] getServiceResponse(Set<String> ids, String identifierType) {
        return getServiceResponse(ids, identifierType, false);
    }

    @Override
	public Set<InstanceType> getArtifactInstanceTypes() {
		return Set.of(JamaBaseElementType.JAMA_CORE_ITEM.getType(), JamaBaseElementType.JAMA_PROJECT.getType(), JamaBaseElementType.JAMA_RELEASE.getType());
	}

	@Override
	public Map<InstanceType, List<String>> getSupportedIdentifier() {
		Map<InstanceType, List<String>> identifierTypes = new HashMap<>();
		identifierTypes.put(JamaBaseElementType.JAMA_CORE_ITEM.getType(), List.of(JamaIdentifiers.JamaItemId.toString(), JamaIdentifiers.JamaItemDocKey.toString(), JamaIdentifiers.JamaFilterId.toString()));
		//identifierTypes.put(JamaBaseElementType.JAMA_PROJECT.getType(), List.of(JamaIdentifiers.JamaProjectId.toString()));
		//identifierTypes.put(JamaBaseElementType.JAMA_RELEASE.getType(), List.of(JamaIdentifiers.JamaReleaseId.toString()));
		return identifierTypes;
	}

	public Set<JamaActivity> getCompleteHistory() {
		return completeHistory;
	}

	public JamaSchemaConverter getJamaSchemaConverter() {
		return jamaSchemaConverter;
	}

	protected ProgressEntry dispatchNewStartedActivity(String activity) {
		ProgressEntry pe = new ProgressEntry("JamaConnector", activity, Status.Started);
		if (obs != null)
			obs.dispatchNewEntry(pe);
		return pe;
	}
	
	protected ProgressEntry dispatchAtomicFinishedActivity(String activity) {
		ProgressEntry pe = new ProgressEntry("JamaConnector", activity, Status.Completed);
		if (obs != null)
			obs.dispatchNewEntry(pe);
		return pe;
	}

	@Override
	public void handleServiceRequest(Workspace workspace, Collection<Operation> operations) {
		// TODO Auto-generated method stub
		
	}
}
