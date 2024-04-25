package at.jku.isse.designspace.azure.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import at.jku.isse.designspace.artifactconnector.core.monitoring.IProgressObserver;
import at.jku.isse.designspace.artifactconnector.core.monitoring.ProgressEntry;
import at.jku.isse.designspace.artifactconnector.core.monitoring.ProgressEntry.Status;
import at.jku.isse.designspace.azure.api.IAzureApi;
import at.jku.isse.designspace.azure.idcache.IdCache;
import at.jku.isse.designspace.azure.model.AzureBaseElementType;
import at.jku.isse.designspace.azure.model.WorkItemComment;
import at.jku.isse.designspace.core.model.Id;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.Workspace;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ItemMapper {

    private IdCache idCache;
    private IAzureApi azureApi;
    private ObjectMapper mapper;
    private IProgressObserver obs;

    public ItemMapper(IdCache idCache, IAzureApi azureApi, IProgressObserver obs) {
        this.idCache = idCache;
        this.azureApi = azureApi;
        this.mapper = new ObjectMapper();
        this.obs = obs;
    }

    public Instance fetchAndMapWorkItem(String project, int azureId, Workspace workspace) {
        Instance instance = null;
        ProgressEntry pe = dispatchNewStartedActivity("Fetching Workitem: "+project+"/"+azureId);
        byte[] workItemJson = azureApi.getWorkItem(project, azureId);

        try {
            JsonNode rootNode = mapper.readTree(workItemJson);
            if(rootNode != null) {
                instance = mapWorkItemFromJson(rootNode, workspace, false);
            }
            pe.setStatus(Status.Completed);
        } catch (IOException e) {
            e.printStackTrace();
            pe.setStatusAndComment(Status.Failed, e.getMessage());
        }

        return instance;
    }

    //create an instance of type User and add it to idCache
    private Instance createUserInstance(Workspace workspace, JsonNode userNode) {
        String userId = userNode.get("uniqueName").textValue();
        String userDescriptor = userNode.get("descriptor").textValue();
        Instance userInstance = workspace.createInstance(AzureBaseElementType.AZURE_USER.getType(), "userInstance_" + userId);
        userInstance.getPropertyAsSingle("userDescriptor").set(userDescriptor);
        userInstance.getPropertyAsSingle(AzureBaseElementType.DISPLAY_NAME).set(userNode.get("displayName").textValue());
        userInstance.getPropertyAsSingle(AzureBaseElementType.URL).set("https://vssps.dev.azure.com/" + azureApi.getOrganizationName()
            + "/_apis/graph/users/" + userDescriptor);
        userInstance.getPropertyAsSingle(AzureBaseElementType.EMAIL).set(userId);
        //putting both email address and user descriptor into cache
        idCache.putInstanceId(userId, userInstance.id());
        idCache.putInstanceId(userDescriptor, userInstance.id());
        return userInstance;
    }

    //create a project instance if there isn't already one in designspace and assign the work item to that project
    private boolean assignToProject(String projectName, Workspace workspace, Instance instance) {
        Instance projectInstance = searchForInstance(projectName, workspace);
        if(projectInstance == null) {
            try {
                JsonNode projectJson = mapper.readTree(azureApi.getProject(projectName));
                String projectIdentifier = projectJson.get("name").textValue();
                projectInstance = workspace.createInstance(AzureBaseElementType.AZURE_PROJECT.getType(), "projectInstance");
                projectInstance.getPropertyAsSingle(AzureBaseElementType.ID).set(projectJson.get("id").textValue());
                projectInstance.getPropertyAsSingle(AzureBaseElementType.PROJECT_NAME).set(projectIdentifier);
                if (projectJson.get("description") != null)
                	projectInstance.getPropertyAsSingle(AzureBaseElementType.PROJECT_DESCRIPTION).set(projectJson.get("description").textValue());
                projectInstance.getPropertyAsSingle(AzureBaseElementType.URL).set(projectJson.get("_links").get("web").get("href").textValue());
                idCache.putInstanceId(projectIdentifier, projectInstance.id());
            } catch (IOException e) {
                log.debug("AZURE-SERVICE: Couldn't find or create the project instance");
                return false;
            }
        }
        instance.getPropertyAsSingle(AzureBaseElementType.PROJECT).set(projectInstance);
        return true;
    }

    private void fetchAndCreateCommentInstances(Instance instance, Workspace workspace, int workItemId) {
        try {
            JsonNode rootNode = mapper.readTree(azureApi.getComments(workItemId));
            ArrayNode commentsArray = (ArrayNode) rootNode.get("comments");
            for (JsonNode arrayItem : commentsArray) {
                Instance commentInstance = workspace.createInstance(AzureBaseElementType.WORKITEM_COMMENT.getType(),
                        "commentInstance" + Id.newId().toString());
                int commentId = arrayItem.get("id").intValue();
                commentInstance.getPropertyAsSingle(AzureBaseElementType.ID).set(Integer.toUnsignedLong(commentId));
                commentInstance.getPropertyAsSingle(AzureBaseElementType.WITEM_ID).set(String.valueOf(azureApi.getProjectName() + "/"
                        + arrayItem.get("workItemId").intValue()));
                commentInstance.getPropertyAsSingle(AzureBaseElementType.COMMENT_TEXT).set(arrayItem.get("text").textValue());
                commentInstance.getPropertyAsSingle(AzureBaseElementType.DATE_CREATED).set(arrayItem.get("createdDate").textValue());
                commentInstance.getPropertyAsSingle(AzureBaseElementType.URL).set(arrayItem.get("url").textValue());

                JsonNode createdByNode = arrayItem.get("createdBy");
                //lookup an existing user instance
                Instance createdByUserInstance = searchForInstance(createdByNode.get("id").textValue(), workspace);
                if (createdByUserInstance != null) {
                    commentInstance.getPropertyAsSingle("createdBy").set(createdByUserInstance);
                } else {
                    createdByUserInstance = createUserInstance(workspace, createdByNode);
                    commentInstance.getPropertyAsSingle("createdBy").set(createdByUserInstance);
                }
                //add the comment to the instances comment list and id cache
                instance.getPropertyAsList(AzureBaseElementType.COMMENTS).add(commentInstance);
                idCache.putInstanceId(String.valueOf(commentId), commentInstance.id());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //check if the item is present in idcache and return an instance
    private Instance searchForInstance(String id, Workspace workspace) {
        Id designspaceId = idCache.getInstanceId(id);
        if(designspaceId == null) {
            return null;
        }
        return workspace.findElement(designspaceId);
    }

    private Instance mapWorkItemFromJson(JsonNode rootNode, Workspace workspace, boolean isFromRevision) {
        Instance instance;
        //"fields" node contains all the information about the item
        JsonNode fieldsNode = rootNode.get("fields");

        //check if server returned a json response
        if(fieldsNode == null) {
            return null;
        }

        //check if there's an existing instance and if it already exists within designspace, we're assuming that it's a skeleton item
        String workItemId = isFromRevision ? 
        		String.valueOf(rootNode.get("id").intValue())
        		: String.valueOf(fieldsNode.get("System.Id").intValue()); //cannot get a value with textValue() on numerical types
        String projectName = fieldsNode.get(AzureFields.PROJECT).textValue();
        String workItemDesignspaceId = getArtifactIdentifier(projectName, workItemId); 
        instance = searchForInstance(workItemDesignspaceId, workspace);
        if(instance == null) {
            instance = workspace.createInstance(AzureBaseElementType.AZURE_WORKITEM.getType(), workItemId+"-"+fieldsNode.get(AzureFields.TITLE).textValue());
        }

        instance.getPropertyAsSingle(AzureBaseElementType.ID).set(workItemDesignspaceId);
        instance.getPropertyAsSingle(AzureBaseElementType.TITLE).set(fieldsNode.get(AzureFields.TITLE).textValue());
        if (fieldsNode.get(AzureFields.PRIORITY) != null)
        	instance.getPropertyAsSingle(AzureBaseElementType.PRIORITY).set(Long.valueOf(fieldsNode.get(AzureFields.PRIORITY).intValue()));        
        if (fieldsNode.get(AzureFields.CATEGORY) != null)
        	instance.getPropertyAsSingle(AzureBaseElementType.CATEGORY).set(fieldsNode.get(AzureFields.CATEGORY).textValue());        
        if (fieldsNode.get(AzureFields.STATE) != null)
            instance.getPropertyAsSingle(AzureBaseElementType.STATE).set(fieldsNode.get(AzureFields.STATE).textValue());
        

        //ATTENTION: users are differently handled in a revision originating json structure:
        Instance createdByUserInstance = getOrCreateUserFromJson(fieldsNode.get(AzureFields.CREATED_BY), isFromRevision, workspace) ;        
        instance.getPropertyAsSingle(AzureBaseElementType.CREATOR).set(createdByUserInstance);

        //look if there's an "assignedTo" field in json response
        JsonNode assignedToNode = fieldsNode.get(AzureFields.ASSIGNED_TO);
        if (assignedToNode != null) {
            //look into idcache for entry
            Instance assignedToUserInstance = getOrCreateUserFromJson(assignedToNode, isFromRevision, workspace);             		
            instance.getPropertyAsSingle(AzureBaseElementType.ASSIGNEE).set(assignedToUserInstance);
        }
        assignToProject(projectName, workspace, instance);

        //set the rest of the fields
        instance.getPropertyAsSingle(AzureBaseElementType.WITEM_TYPE).set(fieldsNode.get(AzureFields.WITEM_TYPE).textValue());
        
        instance.getPropertyAsSingle(AzureBaseElementType.FULLY_FETCHED).set(true);
        instance.getPropertyAsSingle(AzureBaseElementType.DELETED).set(false);
        if (fieldsNode.get(AzureFields.DESCRIPTION) != null) {
            instance.getPropertyAsSingle(AzureBaseElementType.WITEM_DESCRIPTION).set(fieldsNode.get(AzureFields.DESCRIPTION).textValue());
        }
        String url = null;
        if (isFromRevision) {
        	url = recreateItemUrlFromRevision(rootNode, projectName, workItemId);            
        } else {
        	url = rootNode.get("_links").get("html").get("href").textValue();
        }
        instance.getPropertyAsSingle(AzureBaseElementType.URL).set(url);

        if (fieldsNode.get(AzureFields.REVIEW_CRITERIA) != null) {
        	instance.getPropertyAsSingle(AzureBaseElementType.REVIEW_CRITERIA).set(fieldsNode.get(AzureFields.REVIEW_CRITERIA).textValue());
        }
        if (fieldsNode.get(AzureFields.REVIEW_FINDINGS) != null) {
        	instance.getPropertyAsSingle(AzureBaseElementType.REVIEW_FINDINGS).set(fieldsNode.get(AzureFields.REVIEW_FINDINGS).textValue());
        }
        if (fieldsNode.get(AzureFields.REVIEW_SCHEDULEDDATE) != null) {
        	instance.getPropertyAsSingle(AzureBaseElementType.REVIEW_SCHEDULEDDATE).set(fieldsNode.get(AzureFields.REVIEW_SCHEDULEDDATE).textValue());
        }
        JsonNode mandassignee = fieldsNode.get(AzureFields.REVIEW_MANDASSIGNEE1);
        if (mandassignee != null) {
            Instance mandassigneeInstance = getOrCreateUserFromJson(mandassignee, isFromRevision, workspace);         
        	instance.getPropertyAsSingle(AzureBaseElementType.REVIEW_MANDASSIGNEE1).set(mandassigneeInstance);
        } 
        if (fieldsNode.get(AzureFields.VERIFICATION_CRITERIA) != null) {
        	instance.getPropertyAsSingle(AzureBaseElementType.VERIFICATION_CRITERIA).set(fieldsNode.get(AzureFields.VERIFICATION_CRITERIA).textValue());
        }
        
        //check related items
        ArrayNode relationsNode = (ArrayNode) rootNode.get("relations");
        if (relationsNode != null) {
            for (JsonNode item : relationsNode) {
                String relurl = item.get("url").textValue();
                //work item id is at the end of url
                String[] splitUrl = relurl.split("/");
                String linkedItemId = getArtifactIdentifier(projectName , splitUrl[splitUrl.length - 1]);
                Instance relatedItemInstance = searchForInstance(linkedItemId, workspace);

                if (relatedItemInstance == null) {
                    //skeleton workitem instance will be created
                    relatedItemInstance = workspace.createInstance(AzureBaseElementType.AZURE_WORKITEM.getType(), linkedItemId);
                    relatedItemInstance.getPropertyAsSingle(AzureBaseElementType.ID).set(linkedItemId);
                    relatedItemInstance.getPropertyAsSingle(AzureBaseElementType.FULLY_FETCHED).set(false);
                    idCache.putInstanceId(linkedItemId, relatedItemInstance.id());
                }
                //item exists within DSpace => add it to the list of relations                
                String linkType = item.get("rel").textValue();
                String linkName = linkId2Name.get(linkType);
                if (linkName != null) {
                	String linkProperty = AzureBaseElementType.convertLinkTypeNameToProperty(linkName);
                	instance.getPropertyAsSet(linkProperty).add(relatedItemInstance);
                } else {
                	log.debug(String.format("AZURE-SERVICE: Could not resolve the link type <%s> ",linkType));
                }                
            }
        }

        //get comments if there are any
        JsonNode commentCount = fieldsNode.get(AzureFields.COMMENT_COUNT);
        if (commentCount != null && commentCount.intValue() > 0) {
        	// there will be no comment info in revision originating json structure.
            fetchAndCreateCommentInstances(instance, workspace, Integer.parseInt(workItemId));
        }

        //add an entry into the idCache
        idCache.putInstanceId(workItemDesignspaceId, instance.id());
        workspace.concludeTransaction();
        return instance;
    }
    
    private String recreateItemUrlFromRevision(JsonNode revisionNode, String projectName, String id) {
    	
    	Pattern pattern = Pattern.compile("https://(.*?)\\.");
        Matcher matcher = pattern.matcher(revisionNode.get("url").textValue());
        if(matcher.find()) {
            String orgname = matcher.group(1);
            return "https://dev.azure.com/" + orgname + "/" + projectName + "/_apis/wit/workItems/" + id;    
        }
        else //JSON API but better than nothing
        	return  revisionNode.get("_links").get("parent").get("href").textValue();        
    }
    
    private Instance getOrCreateUserFromJson(JsonNode userNode, boolean isFromRevision, Workspace workspace) {
    	Instance userInstance = null;
    	if (isFromRevision) {
    		String userEmail = userNode.textValue().split("<")[1];
            userEmail = userEmail.substring(0, userEmail.length() - 1);
        	userInstance = fetchAndMapUserByEmail(userEmail, workspace);
        } else {        	
        	//look into idcache, if the user already exists, then use the existing instance
        	userInstance = searchForInstance(userNode.get("descriptor").textValue(), workspace);
        	if (userInstance == null) {
        		userInstance = createUserInstance(workspace, userNode);
        	}
        }
    	return userInstance;
    }

    public boolean mapWorkItemFromRevision(JsonNode revisionNode, Workspace workspace) {
    	Instance instance = mapWorkItemFromJson(revisionNode, workspace, true);
    	return true;
    }

    public static String getArtifactIdentifier(String projectName, String workItemId) {
        return projectName + "/" + workItemId;
    }

    public List<Instance> fetchAndMapAllWorkItems(Workspace workspace) {
    	ProgressEntry pe = dispatchNewStartedActivity("Fetching all items ids");
        byte[] workItemIdList = azureApi.getAllWorkItemIds();
        
        ArrayList<Integer> ids = new ArrayList<Integer>();
        byte[] allWorkItemsResponse;
        List<Instance> instances = new LinkedList<>();
        ProgressEntry peIt = null;
        try {
            JsonNode rootNode = mapper.readTree(workItemIdList);
            ArrayNode workItemIdsArray = (ArrayNode) rootNode.get("workItems");
            pe.setStatusAndComment(Status.InProgress, String.format("Loading %s items", workItemIdsArray.size()));
            //workItems node is an array
            workItemIdsArray.forEach(obj -> {
                JsonNode item = (JsonNode) obj;
                ids.add(item.get("id").intValue());
            });

            //System.out.println(ids);

            while (!ids.isEmpty()) {
            	int pos = ids.size() > 200 ? 200 : ids.size(); 
            	peIt = dispatchNewStartedActivity(String.format("Batch Fetching %s items", pos));
            	instances.addAll(fetchSublist(ids.subList(0, pos), workspace)); 
            	ids.subList(0, pos).clear();
            	peIt.setStatus(Status.Completed);
            }
            
//            allWorkItemsResponse = azureApi.getAllWorkItems(ids);
//            JsonNode workItemsResponse = mapper.readTree(allWorkItemsResponse);
//            System.out.println(workItemsResponse);
//
//            int workItemCount = workItemsResponse.get("count").intValue();
//            instances = new Instance[workItemCount];
//
//            ArrayNode workItemsArray = (ArrayNode) workItemsResponse.get("value");
//            int i = 0;
//            for (JsonNode arrayItem : workItemsArray) {
//                instances[i] = mapWorkItemFromJson(arrayItem, workspace);
//                i++;
//            }

            return instances;
        } catch (IOException e) {
            e.printStackTrace();
            if (peIt != null)
            	peIt.setStatusAndComment(Status.Failed, e.getMessage());
        }

        return instances;
    }
    
    private List<Instance> fetchSublist(List<Integer> itemIds, Workspace workspace) throws IOException {
    	List<Instance> instances = new LinkedList<Instance>();
    	
    	byte[]  allWorkItemsResponse = azureApi.getAllWorkItems(itemIds);
        JsonNode workItemsResponse = mapper.readTree(allWorkItemsResponse);
       // System.out.println(workItemsResponse);

        int workItemCount = workItemsResponse.get("count").intValue();

        ArrayNode workItemsArray = (ArrayNode) workItemsResponse.get("value");
        int i = 0;
        for (JsonNode arrayItem : workItemsArray) {
            instances.add(mapWorkItemFromJson(arrayItem, workspace, false));            
        }
        return instances;
    }

    //cannot use createUserInstance() method, because field names are different
    public Instance fetchAndMapUserByDescriptor(String userDescriptor, Workspace workspace) {
        log.debug("AZURE-SERVICE: Fetching user " + userDescriptor);
        byte[] apiResponse = azureApi.getUserByDescriptor(userDescriptor);
        Instance userInstance = null;

        try {
            JsonNode rootNode = mapper.readTree(apiResponse);
            if(rootNode != null) {
                String userMail = rootNode.get("mailAddress").textValue();
                String userDescriptorFromResponse = rootNode.get("descriptor").textValue();
                userInstance = workspace.createInstance(AzureBaseElementType.AZURE_USER.getType(), "userInstance_" + userMail);
                userInstance.getPropertyAsSingle(AzureBaseElementType.EMAIL).set(userMail);
                userInstance.getPropertyAsSingle(AzureBaseElementType.DISPLAY_NAME).set(rootNode.get("displayName").textValue());
                userInstance.getPropertyAsSingle("userDescriptor").set(userDescriptorFromResponse);
                userInstance.getPropertyAsSingle(AzureBaseElementType.URL).set(rootNode.get("url").textValue());
                idCache.putInstanceId(userDescriptorFromResponse, userInstance.id());
                idCache.putInstanceId(userMail, userInstance.id());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return userInstance;
    }

    public Instance fetchAndMapUserByEmail(String email, Workspace workspace) {
        log.debug("AZURE-SERVICE: Fetching user " + email);
        byte[] apiResponse = azureApi.getUserByEmail(email);
        Instance userInstance = null;

        try {
            JsonNode rootNode = mapper.readTree(apiResponse);
            if(rootNode != null && rootNode.get("count").intValue() > 0) {
                JsonNode userNode = rootNode.get("value").get(0);
                JsonNode propertiesNode = userNode.get("properties");
                String userDescriptor = userNode.get("subjectDescriptor").textValue();
                
                //check if exists
                userInstance = searchForInstance(userDescriptor, workspace);
            	if (userInstance == null) { //if not, then create and cache
            		String userMail = propertiesNode.get("Mail").get("$value").textValue();

            		userInstance = workspace.createInstance(AzureBaseElementType.AZURE_USER.getType(), "userInstance_" + userMail);
            		userInstance.getPropertyAsSingle(AzureBaseElementType.EMAIL).set(userMail);
            		userInstance.getPropertyAsSingle(AzureBaseElementType.DISPLAY_NAME).set(userNode.get("providerDisplayName").textValue());
            		userInstance.getPropertyAsSingle("userDescriptor").set(userDescriptor);
            		userInstance.getPropertyAsSingle(AzureBaseElementType.URL).set("https://vssps.dev.azure.com/" + azureApi.getOrganizationName()
            		+ "/_apis/Graph/Users/" + userDescriptor);
            		idCache.putInstanceId(userDescriptor, userInstance.id());
            		idCache.putInstanceId(userMail, userInstance.id());
            	}
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return userInstance;
    }

    public Instance createCommentInstance(WorkItemComment comment, Workspace workspace) {
        int commentId = comment.getId();
        String workItemId = String.valueOf(azureApi.getProjectName() + "/" + comment.getWorkItemId());
        Instance commentInstance = workspace.createInstance(AzureBaseElementType.WORKITEM_COMMENT.getType(), "commentInstance" + Id.newId().toString());
        commentInstance.getPropertyAsSingle(AzureBaseElementType.ID).set(Integer.toUnsignedLong(commentId));
        commentInstance.getPropertyAsSingle(AzureBaseElementType.WITEM_ID).set(workItemId);
        commentInstance.getPropertyAsSingle(AzureBaseElementType.COMMENT_TEXT).set(comment.getText());
        commentInstance.getPropertyAsSingle(AzureBaseElementType.DATE_CREATED).set(comment.getCreatedDate());
        commentInstance.getPropertyAsSingle(AzureBaseElementType.URL).set("https://dev.azure.com/" + azureApi.getOrganizationName() + azureApi.getProjectName()
            + "/_apis/wit/workitems/" + comment.getWorkItemId() + "/comments/" + commentId);

        Instance authorInstance = searchForInstance(comment.getCreatedBy(), workspace);
        if(authorInstance != null) {
            commentInstance.getPropertyAsSingle(AzureBaseElementType.CREATOR).set(authorInstance);
        } else {
            log.debug("AZURE-SERVICE: Couldn't find the user while creating a new comment instance");
        }
        Instance workItemInstance = searchForInstance(workItemId, workspace);
        if(workItemInstance != null) {
            workItemInstance.getPropertyAsList(AzureBaseElementType.COMMENTS).add(commentInstance);
        } else {
            log.debug("AZURE-SERVICE: Coudn't find the work item associated with the comment");
        }
        idCache.putInstanceId(String.valueOf(commentId), commentInstance.id());
        return commentInstance;
    }

    public boolean updateCommentInstance(Instance commentInstance, int workItemId, int commentId) {
        try {
            JsonNode rootNode = mapper.readTree(azureApi.getComment(workItemId, commentId));
            commentInstance.getPropertyAsSingle(AzureBaseElementType.COMMENT_TEXT).set(rootNode.get("text").textValue());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
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

    
    Map<String, String> linkId2Name = new HashMap<>();
    
    public Map<String, String> initRelationTypes(Workspace workspace) {
    	ProgressEntry pe = dispatchNewStartedActivity("Fetching Relationship types");
    	try {
            JsonNode rootNode = mapper.readTree(azureApi.getWorkItemRelationTypes());
            if(rootNode.get("count").intValue() > 0) {
                ArrayNode valueArray = (ArrayNode) rootNode.get("value");
                for(JsonNode arrayItem : valueArray) {               	
                    String linkId = arrayItem.get("referenceName").textValue();
                    String linkName = arrayItem.get("name").textValue();
                    linkId2Name.put(linkId, linkName);
                }
                pe.setStatusAndComment(Status.Completed, String.format("Loaded %s relationsship types", linkId2Name.size()));
            } else
            	pe.setStatusAndComment(Status.Completed, "Without error but no relations found.");
        } catch (IOException e) {
            e.printStackTrace();
            pe.setStatusAndComment(Status.Failed, e.getMessage());
            return Collections.emptyMap();
        }
        return linkId2Name;
    }


    public static class AzureFields {
        public static final String 
        		STATE = "System.State", 
        		TITLE = "System.Title", 
        		PRIORITY = "Microsoft.VSTS.Common.Priority",
        		CATEGORY = "Microsoft.VSTS.CMMI.RequirementType",
        		WITEM_TYPE = "System.WorkItemType", 
        		PROJECT = "System.TeamProject", 
        		CREATED_BY = "System.CreatedBy", 
        		ASSIGNED_TO = "System.AssignedTo",
        		COMMENT_COUNT = "System.CommentCount",
        		DESCRIPTION = "System.Description",
        		REVIEW_MANDASSIGNEE1 = "Microsoft.VSTS.CMMI.RequiredAttendee1", 
        		REVIEW_SCHEDULEDDATE = "Custom.Scheduled", 
        		REVIEW_CRITERIA = "Microsoft.VSTS.CMMI.Purpose",
        		REVIEW_FINDINGS = "Microsoft.VSTS.CMMI.Minutes",
        		VERIFICATION_CRITERIA = "Custom.VerificationCriteria";
        		
    }
}
