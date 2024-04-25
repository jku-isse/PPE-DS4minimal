package at.jku.isse.designspace.azure.updateservice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;

import at.jku.isse.designspace.azure.model.AzureBaseElementType;
import at.jku.isse.designspace.azure.model.WorkItemComment;
import at.jku.isse.designspace.azure.model.WorkItemLink;
import at.jku.isse.designspace.azure.service.IAzureService;
import at.jku.isse.designspace.azure.service.ItemMapper;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.Property;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AzureChangePatcher {

    private IAzureService azureService;
    private Map<String, String> linkId2Name;
    public static Map<String, String> fieldMapper = new HashMap<>();

    { 
    fieldMapper.put(ItemMapper.AzureFields.TITLE, AzureBaseElementType.TITLE);
    fieldMapper.put(ItemMapper.AzureFields.STATE, AzureBaseElementType.STATE);
    fieldMapper.put(ItemMapper.AzureFields.ASSIGNED_TO, AzureBaseElementType.ASSIGNEE);
    fieldMapper.put(ItemMapper.AzureFields.PRIORITY, AzureBaseElementType.PRIORITY);
    fieldMapper.put(ItemMapper.AzureFields.DESCRIPTION, AzureBaseElementType.WITEM_DESCRIPTION);
    fieldMapper.put(ItemMapper.AzureFields.CATEGORY, AzureBaseElementType.CATEGORY);
    fieldMapper.put(ItemMapper.AzureFields.REVIEW_CRITERIA, AzureBaseElementType.REVIEW_CRITERIA);
    fieldMapper.put(ItemMapper.AzureFields.REVIEW_FINDINGS, AzureBaseElementType.REVIEW_FINDINGS);
    fieldMapper.put(ItemMapper.AzureFields.REVIEW_MANDASSIGNEE1, AzureBaseElementType.REVIEW_MANDASSIGNEE1);
    fieldMapper.put(ItemMapper.AzureFields.REVIEW_SCHEDULEDDATE, AzureBaseElementType.REVIEW_SCHEDULEDDATE);
    fieldMapper.put(ItemMapper.AzureFields.VERIFICATION_CRITERIA, AzureBaseElementType.VERIFICATION_CRITERIA);
    }
    
    public AzureChangePatcher(IAzureService azureService, Map<String, String> linkId2Name) {
        this.azureService = azureService;
       this.linkId2Name = linkId2Name;
    }
    
  
    public void applyWorkItemChange(List<AzureChange> changeList) {
        for(AzureChange<?> singleChange : changeList) {
            String workItemId = ItemMapper.getArtifactIdentifier(singleChange.getProjectName() ,String.valueOf(singleChange.getWorkItemId()));
            Optional<Instance> subjectToChange_ = azureService.searchForInstance(workItemId);
            if(subjectToChange_.isPresent()) {
                Instance subjectToChange = subjectToChange_.get();

                switch(singleChange.getChangeType()) {
                    case FIELD_MODIFY:
                        applyFieldChanges(singleChange, subjectToChange, workItemId);
                        break;
                    case RELATION_ADD:
                        addRelations(singleChange, subjectToChange, workItemId);
                        break;
                    case RELATION_REMOVE:
                        removeRelations(singleChange, subjectToChange, workItemId);
                        break;
                    case NEW_COMMENT:
                        addNewCommentToWorkItem((WorkItemComment) singleChange.getNewValue(), subjectToChange);
                        break;
                    case COMMENT_UPDATE:
                        updateComment(subjectToChange, singleChange.getWorkItemId(), Integer.parseInt(singleChange.getNewValue().toString()));
                        break;
                    case COMMENT_DELETE:
                        deleteCommentFromWorkItem(subjectToChange, Integer.parseInt(singleChange.getNewValue().toString()));
                        break;
                    default:
                        log.debug("AZURE-CHANGE-PATCHER: Change Type not supported yet");
                        break;
                }
            } else {
                log.debug("AZURE-CHANGE-PATCHER: Could not find work item with the given id");
            }
        }

    }
    
    private void setUserToField(Property prop, String userEmail) {
    	if (userEmail.contains("<")) {
    		userEmail = userEmail.split("<")[1];
            userEmail = userEmail.substring(0, userEmail.length() - 1);
    	}    	
    	Optional<Instance> userInstance = azureService.searchForInstance(userEmail);
        if(userInstance.isPresent()) {
            prop.set(userInstance.get());            
        } else {            
            userInstance = azureService.transferUserByEmail(userEmail);
            if(userInstance.isPresent()) {                
                prop.set(userInstance.get());
            } else {
                log.debug("AZURE-CHANGE-PATCHER: Failed to fetch the user from the server");
            }
        }
    }

    private void applyFieldChanges(AzureChange change, Instance subjectToChange, String workItemId) {
        Object newValue = change.getNewValue();
        String propertyName = fieldMapper.get(change.getFieldName());
        if(subjectToChange.hasProperty(propertyName)) {
            if (propertyName.equals(AzureBaseElementType.ASSIGNEE) || propertyName.equals(AzureBaseElementType.REVIEW_MANDASSIGNEE1)) {
                String userEmail = newValue.toString();
                setUserToField(subjectToChange.getProperty(propertyName), userEmail);
                log.debug("AZURE-CHANGE-PATCHER: Update on work item id: " + workItemId + ", property: " + propertyName + ", value: " + newValue.toString());
//                Optional<Instance> userInstance = azureService.searchForInstance(userEmail);
//                if(userInstance.isPresent()) {
//                    subjectToChange.getProperty(propertyName).set(userInstance.get());
//                    log.debug("AZURE-CHANGE-PATCHER: Assignee on work item id: " + workItemId + " has changed to " + userEmail);
//                } else {
//                    log.debug("AZURE-CHANGE-PATCHER: User not found. Fetching...");
//                    userInstance = azureService.transferUserByEmail(userEmail);
//                    if(userInstance.isPresent()) {
//                        log.debug("AZURE-CHANGE-PATCHER: Successfully fetched the user");
//                        subjectToChange.getProperty(propertyName).set(userInstance.get());
//                    } else {
//                        log.debug("AZURE-CHANGE-PATCHER: Failed to fetch the user from the server");
//                    }
//                }
//            } else if(propertyName.equals(AzureBaseElementType.STATE)) {
//                String stateName = newValue.toString();
//                Optional<Instance> stateInstance = azureService.searchForInstance(stateName);
//                if(stateInstance.isPresent()) {
//                    subjectToChange.getProperty(propertyName).set(stateInstance.get());
//                } else {
//                    log.debug("AZURE-CHANGE-PATCHER: Couldn't find the state");
//                }
            } else {
                switch (newValue.getClass().getSimpleName()) {
                    case "String":
                        subjectToChange.getProperty(propertyName).set(newValue.toString());
                        break;
                    case "Integer":
                        subjectToChange.getProperty(propertyName).set(Integer.toUnsignedLong((int) newValue));
                        break;
                }
                log.debug("AZURE-CHANGE-PATCHER: Update on work item id: " + workItemId + ", property: " + propertyName + ", value: " + newValue.toString());
            }
        } else {
            log.debug("AZURE-CHANGE-PATCHER: Property that has changed was not found in the instance");
        }
    }

    private void addRelations(AzureChange change, Instance subjectToChange, String workItemId) {
        boolean isSubjectFullyFetched = subjectToChange.getPropertyAsSingle(AzureBaseElementType.FULLY_FETCHED).getValue().equals(true);
        if(isSubjectFullyFetched) {
            WorkItemLink workItemLink = (WorkItemLink) change.getNewValue();
            String relatedItemId = change.getProjectName() + "/" + workItemLink.getRelatedWorkItemId();
            Optional<Instance> relatedWorkItem = azureService.searchForInstance(relatedItemId);
            String pName = AzureBaseElementType.convertLinkTypeNameToProperty(linkId2Name.get(workItemLink.getLinkType()));
            if (pName == null) {
            	log.warn(String.format("Unknown property for tracelinktype %s", workItemLink.getLinkType()));
            } else if (subjectToChange.hasProperty(pName)) {
            	
            	if (relatedWorkItem.isPresent()) {
            		subjectToChange.getPropertyAsSet(pName).add(relatedWorkItem.get());
//                	Optional<Instance> workItemLinkInstance_ = azureService.transferWorkItemLink(workItemLink, change.getProjectName());
//                    if (workItemLinkInstance_.isPresent()) {
//                        subjectToChange.getPropertyAsList("relatedItems").add(workItemLinkInstance_.get());
//                        log.debug("AZURE-CHANGE-PATCHER: Added a related item " + relatedItemId + " to work item " + workItemId);
//                    }
                } else {
                    log.debug("AZURE-CHANGE-PATCHER: Could not find the related work item with the given id. Fetching...");
                    relatedWorkItem = azureService.transferAzureWorkItem(change.getProjectName() , workItemLink.getRelatedWorkItemId(), false);
                    if (relatedWorkItem.isPresent()) {
                    	subjectToChange.getPropertyAsSet(pName).add(relatedWorkItem.get());
                        //creating a work item link instance and adding it to the list of relations
//                        Optional<Instance> workItemLinkInstance_ = azureService.transferWorkItemLink(workItemLink, change.getProjectName());
//                        if (workItemLinkInstance_.isPresent()) {
//                            subjectToChange.getPropertyAsList(AzureBaseElementType.RELATED_ITEMS).add(workItemLinkInstance_.get());
//                            log.debug("AZURE-CHANGE-PATCHER: Added a related item " + relatedItemId + " to work item " + workItemId);
//                        } else {
//                            log.debug("AZURE-CHANGE-PATCHER: Could not create a link between items");
//                        }
                    } else {
                        log.debug("AZURE-CHANGE-PATCHER: Could not find the related work item at the AzureDevOpsServices server" + change.getProjectName() +" "+ workItemLink.getRelatedWorkItemId());
                    }
                }
            	
            } else {
            	log.warn(String.format("Workitem %s doesn't have tracelink property %s", subjectToChange, pName));
            }            
        } else {
            log.debug("AZURE-CHANGE-PATCHER: The Subject to change is not fully fetched!");
        }
    }

    private void removeRelations(AzureChange change, Instance subjectToChange, String workItemId) {
    	boolean isSubjectFullyFetched = subjectToChange.getPropertyAsSingle(AzureBaseElementType.FULLY_FETCHED).getValue().equals(true);
    	if(isSubjectFullyFetched) {
    		WorkItemLink workItemLink = (WorkItemLink) change.getNewValue();
    		String pName = AzureBaseElementType.convertLinkTypeNameToProperty(linkId2Name.get(workItemLink.getLinkType()));
    		if (pName == null) {
    			log.warn(String.format("Unknown property for tracelinktype %s", workItemLink.getLinkType()));
    		} else if (subjectToChange.hasProperty(pName)) {
    			String relatedItemId = change.getProjectName() + "/" + workItemLink.getRelatedWorkItemId();
    			Optional<Instance> relatedWorkItem = azureService.searchForInstance(relatedItemId);
    			if (relatedWorkItem.isPresent()) {
    				subjectToChange.getPropertyAsSet(pName).remove(relatedWorkItem.get());
    			}
    			//                Instance relatedWorkItemInstance = relatedWorkItem.get();
    			//                List relatedWorkItemsList = subjectToChange.getPropertyAsList(AzureBaseElementType.RELATED_ITEMS).get();
    			//                int i = 0;
    			//                int foundAtIndex = -1;
    			//                for (Object item : relatedWorkItemsList) {
    			//                    Instance relationInstance = (Instance) item;
    			//                    Optional<Instance> linkTypeInstance = azureService.searchForInstance(workItemLink.getLinkType());
    			//                    if (linkTypeInstance.isPresent()) {
    			//                        boolean isCorrectLinkType = relationInstance.getPropertyAsSingle(AzureBaseElementType.LINK_TYPE).get() == linkTypeInstance.get();
    			//                        boolean isRelated = relationInstance.getPropertyAsSingle(AzureBaseElementType.LINK_TO).get() == relatedWorkItemInstance;
    			//                        if (isCorrectLinkType && isRelated) {
    			//                            foundAtIndex = i;
    			//                            break;
    			//                        }
    			//                    } else {
    			//                        log.debug("AZURE-CHANGE-PATCHER: Could not find the link type to the related item");
    			//                    }
    			//                    i++;
    			//                }
    			//                if (foundAtIndex > -1) {
    			//                    Instance removedInstance = (Instance) subjectToChange.getPropertyAsList(AzureBaseElementType.RELATED_ITEMS).remove(foundAtIndex);
    			//                    Instance linkTo = (Instance) removedInstance.getPropertyAsSingle(AzureBaseElementType.LINK_TO).get();
    			//                    String linkToWorkItemId = linkTo.getPropertyAsSingle(AzureBaseElementType.ID).getValue().toString();
    			//                    log.debug("AZURE-CHANGE-PATCHER: Removed work item " + linkToWorkItemId + " with link type "
    			//                            + removedInstance.getPropertyAsSingle(AzureBaseElementType.LINK_TYPE).value + " from " + workItemId);
    			//                } else {
    			//                    log.debug("AZURE-CHANGE-PATCHER: Could not find the relation");
    			//                }
    			else {
    				log.debug("AZURE-CHANGE-PATCHER: Could not find the related work item with the given id.");
    			}
    		} else {
    			log.warn(String.format("Workitem %s doesn't have tracelink property %s", subjectToChange, pName));
    		}  
    	} else {
    		log.debug("AZURE-CHANGE-PATCHER: The Subject to change is not fully fetched, ignoring update!");
    	}
    }

    private void addNewCommentToWorkItem(WorkItemComment newComment, Instance workItemInstance) {
        if(workItemInstance.hasProperty("comments")) {
            Optional<Instance> newCommentInstance = azureService.transferComment(newComment);
            if(newCommentInstance.isPresent()) {
                log.debug("AZURE-CHANGE-PATCHER: Added new comment to work item " + workItemInstance.getProperty("id").getValue());
            }
            else {
                log.debug("AZURE-CHANGE-PATCHER: Something went wrong when creating new comment instance");
            }
        }
    }

    private void deleteCommentFromWorkItem(Instance subjectToChange, int commentIdToDelete) {
        if(subjectToChange.hasProperty("comments")) {
            List<Instance> commentList = subjectToChange.getPropertyAsList("comments").get();
            int i = 0;
            int foundAtIndex = -1;
            for(Instance commentInstance : commentList) {
                if(Integer.parseInt(commentInstance.getPropertyAsSingle("id").getValue().toString())  == commentIdToDelete) {
                    foundAtIndex = i;
                }
                i++;
            }
            if(foundAtIndex > -1) {
                subjectToChange.getPropertyAsList("comments").remove(foundAtIndex);
                log.debug("AZURE-CHANGE-PATCHER: Removed comment from work item " + subjectToChange.getPropertyAsSingle("id").getValue());
            } else {
                log.debug("AZURE-CHANGE-PATCHER: Couldn't find the comment to delete");
            }
        }
    }

    private void updateComment(Instance subjectToChange, int workItemId, int commentId) {
        //fetch comment from API and update it
        Optional<Instance> commentInstance_ = azureService.searchForInstance(String.valueOf(commentId));
        if(commentInstance_.isPresent()) {
            if(azureService.updateComment(commentInstance_.get(), workItemId, commentId)) {
                log.debug("AZURE-CHANGE-PATCHER: Updated comment id: " + commentId);
            } else {
                log.debug("AZURE-CHANGE-PATCHER: Couldn't update the comment id: " + commentId);
            }
        } else {
            log.debug("AZURE-CHANGE-PATCHER: Couldn't find the comment to update");
        }
    }

    /*
    Deleting a work item in Azure Devops Services is only going to move the work item in the recycle bin where it can be restored.
    So we're only going to extract work item id from the webhook message, find the work item in Designspace and mark it as deleted.
    */
    public void deleteOrRestoreWorkItem(AzureChange change) {
        String workItemId = change.getProjectName() + "/" + change.getWorkItemId();
        Optional<Instance> workItemInstance_ = azureService.searchForInstance(workItemId);
        if(workItemInstance_.isPresent()) {
            Instance workItemInstance = workItemInstance_.get();
            if(change.getChangeType() == AzureChange.ChangeType.WORK_ITEM_DELETE) {
                workItemInstance.getPropertyAsSingle("deleted").set(true);
                log.debug("AZURE-CHANGE-PATCHER: work item " + workItemId + " deleted");
            }
            else {
                workItemInstance.getPropertyAsSingle("deleted").set(false);
                log.debug("AZURE-CHANGE-PATCHER: work item " + workItemId + " restored");
            }
        } else {
            log.debug("AZURE-CHANGE-PATCHER: Couldn't find the item to delete within Designspace");
        }
    }


    public boolean workItemExistsAndFullyFetched(int workItemId, String project) {
        String designspaceId = ItemMapper.getArtifactIdentifier(project, String.valueOf(workItemId));
        Optional<Instance> workItemInstance_ = azureService.searchForInstance(designspaceId);
        if(workItemInstance_.isPresent() && isFullyFetched(workItemInstance_.get())) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isFullyFetched(Instance instance) {
        return instance.getPropertyAsSingle(AzureBaseElementType.FULLY_FETCHED).getValue().equals(true);
    }


    public void transferWorkItem(JsonNode revisionNode) {
//        log.debug("AZURE-CHANGE-PATCHER: The work item " +
//                ItemMapper.getArtifactIdentifier(workItem.getProject(), String.valueOf(workItem.getId()))+ " is being transfered to Designspace...");
    	azureService.transferWorkItem(revisionNode);
//    	if(azureService.transferWorkItem(revisionNode)) {
//            log.debug("AZURE-CHANGE-PATCHER: Work item successfully transfered");
//        } else {
//            log.debug("AZURE-CHANGE-PATCHER: Something went wrong when creating the work item");
//        }
    }
}
