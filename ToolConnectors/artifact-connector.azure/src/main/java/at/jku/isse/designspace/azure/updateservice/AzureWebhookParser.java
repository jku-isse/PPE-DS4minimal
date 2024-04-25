package at.jku.isse.designspace.azure.updateservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import at.jku.isse.designspace.azure.model.WorkItemComment;
import at.jku.isse.designspace.azure.model.WorkItemLink;

public class AzureWebhookParser {
    public static void parseJson(byte[] input, AzureChangePatcher changePatcher) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readTree(input);
            String eventType = rootNode.get("eventType").textValue();
            JsonNode resourceNode = rootNode.get("resource");
            JsonNode revisionNode = resourceNode.get("revision");
            int workItemId = revisionNode.get("id").intValue();
            String projectName = revisionNode.get("fields").get("System.TeamProject").textValue();
            switch(eventType) {
                case "workitem.updated":
                    if(changePatcher.workItemExistsAndFullyFetched(workItemId, projectName)) {
                        List<AzureChange> workItemChanges = handleUpdated(resourceNode);
                        changePatcher.applyWorkItemChange(workItemChanges);
                    } else {
                        //WorkItem workItem = createWorkItemObject(revisionNode, workItemId, projectName);
                        changePatcher.transferWorkItem(revisionNode);
                    }
                    break;
                case "workitem.deleted":
                    changePatcher.deleteOrRestoreWorkItem(new AzureChange(workItemId, projectName, AzureChange.ChangeType.WORK_ITEM_DELETE));
                    break;
                case "workitem.restored":
                    changePatcher.deleteOrRestoreWorkItem(new AzureChange(workItemId, projectName, AzureChange.ChangeType.WORK_ITEM_RESTORE));
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static List<AzureChange> handleUpdated(JsonNode resourceNode) {
        List<AzureChange> changeList = new ArrayList<>();
        int workItemId = resourceNode.get("workItemId").intValue();
        JsonNode revisionNode = resourceNode.get("revision");
        String projectName = revisionNode.get("fields").get("System.TeamProject").textValue();
        AtomicBoolean commentCountEncountered = new AtomicBoolean(false);
        boolean relationsEncountered = false;

        //iterate through changes in work item fields
        if(resourceNode.has("fields")) {
            JsonNode fieldsChangedNode = resourceNode.get("fields");

            //iteration part
            Iterator<Map.Entry<String, JsonNode>> fields = fieldsChangedNode.fields();
            fields.forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                //check if the fields that were changed are present in the list of relevant fields
                if(getRelevantFields().contains(fieldName)) {
                    JsonNode newValueNode = entry.getValue().get("newValue");
                    AzureChange<?> azureChange = null;
                    if(fieldName.equals("System.AssignedTo")){
                        String userMail = extractEmail(newValueNode.textValue());
                        azureChange = new AzureChange<>(workItemId, fieldName, projectName, AzureChange.ChangeType.FIELD_MODIFY, userMail);
                    } else if(fieldName.equals("System.CommentCount")) {
                        commentCountEncountered.set(true);
                        int oldValue = entry.getValue().get("oldValue").intValue(); //old number of comments in the work item
                        if(oldValue < newValueNode.intValue()) {
                            //new comment has been added
                            WorkItemComment newComment = handleNewComment(revisionNode);
                            azureChange = new AzureChange<>(workItemId, projectName, AzureChange.ChangeType.NEW_COMMENT, newComment);
                        } else {
                            //a comment has been deleted
                            int idToDelete = revisionNode.get("commentVersionRef").get("commentId").intValue();
                            azureChange = new AzureChange<>(workItemId, projectName, AzureChange.ChangeType.COMMENT_DELETE, idToDelete);
                        }
                    } else {
                        if (newValueNode.isTextual()) {
                            azureChange = new AzureChange<>(workItemId, fieldName, projectName, AzureChange.ChangeType.FIELD_MODIFY, newValueNode.textValue());
                        } else if (newValueNode.isNumber()) {
                            //TODO: consider double numbers?
                            azureChange = new AzureChange<>(workItemId, fieldName, projectName, AzureChange.ChangeType.FIELD_MODIFY, newValueNode.intValue());
                        }
                    }
                    changeList.add(azureChange);
                }
            });
        }

        //check changes in relations
        if(resourceNode.has("relations")) {
            relationsEncountered = true;
            JsonNode relationsNode = resourceNode.get("relations");

            if(relationsNode.has("added")) {
                ArrayNode relationsAdded = (ArrayNode) relationsNode.get("added");
                processRelatedWorkItems(relationsAdded, workItemId, projectName, AzureChange.ChangeType.RELATION_ADD, changeList);
            }

            if(relationsNode.has("removed")) {
                ArrayNode relationsRemoved = (ArrayNode) relationsNode.get("removed");
                processRelatedWorkItems(relationsRemoved, workItemId, projectName, AzureChange.ChangeType.RELATION_REMOVE, changeList);
            }
        }

        if(!commentCountEncountered.get() && revisionNode.has("commentVersionRef") && !relationsEncountered) {
            //a comment has been updated
            JsonNode commentVersionRefNode = revisionNode.get("commentVersionRef");
            int commentId = commentVersionRefNode.get("commentId").intValue();
            AzureChange<Integer> azureChange = new AzureChange<>(workItemId, projectName, AzureChange.ChangeType.COMMENT_UPDATE, commentId);
            changeList.add(azureChange);
        }
        return changeList;
    }

    private static void processRelatedWorkItems(ArrayNode arrayNode, int workItemId, String projectName, AzureChange.ChangeType changeType, List<AzureChange> changeList) {
        for (JsonNode arrayItem : arrayNode) {
            String workItemUrl = arrayItem.get("url").textValue();
            String[] split = workItemUrl.split("/");
            int relatedWorkItemId = Integer.parseInt(split[split.length - 1]);
            String relationType = arrayItem.get("rel").textValue();
            WorkItemLink workItemLink = new WorkItemLink(relationType, relatedWorkItemId);
            AzureChange<WorkItemLink> azureChange = new AzureChange<>(workItemId, projectName, changeType, workItemLink);
            changeList.add(azureChange);
        }
    }

//    private static WorkItem createWorkItemObject(JsonNode revisionNode, int workItemId, String projectName) {
//        WorkItem workItem = new WorkItem();
//       // workItem.setId(workItemId);
//        workItem.setProject(projectName);
//        JsonNode fieldsNode = revisionNode.get("fields");
//       // workItem.setTitle(fieldsNode.get("System.Title").textValue());
//        workItem.setWorkItemType(fieldsNode.get("System.WorkItemType").textValue());
//        workItem.setState(fieldsNode.get("System.State").textValue());
//       // if (fieldsNode.get("Microsoft.VSTS.Common.Priority") != null)
//       // 	workItem.setPriority(fieldsNode.get("Microsoft.VSTS.Common.Priority").intValue());
//        if (fieldsNode.get("System.Description") != null)
//        	workItem.setDescription(fieldsNode.get("System.Description").textValue());
//        workItem.setCommentCount(fieldsNode.get("System.CommentCount").intValue());
//        workItem.setCreator(extractEmail(fieldsNode.get("System.CreatedBy").textValue()));
//        if(fieldsNode.get("System.AssignedTo") != null) {
//            workItem.setAssignee(extractEmail(fieldsNode.get("System.AssignedTo").textValue()));
//        }
//
//        ArrayNode relations = (ArrayNode) revisionNode.get("relations");
//        if (relations != null) {
//        	for(JsonNode item : relations) {
//        		String linkType = item.get("rel").textValue();
//        		String[] splitUrl = item.get("url").textValue().split("/");
//        		int relatedItemId = Integer.parseInt(splitUrl[splitUrl.length - 1]);
//        		workItem.getRelatedItems().add(new WorkItemLink(linkType, relatedItemId));
//        	}
//        }
//        Pattern pattern = Pattern.compile("https://(.*?)\\.");
//        Matcher matcher = pattern.matcher(revisionNode.get("url").textValue());
//        if(matcher.find()) {
//            String orgname = matcher.group(1);
//            workItem.setOrganizationName(orgname);
//        }
//
//        return workItem;
//    }

    private static WorkItemComment handleNewComment(JsonNode revisionNode) {
        int commentId = revisionNode.get("commentVersionRef").get("commentId").intValue();
        int workItemId =  revisionNode.get("id").intValue();
        JsonNode fieldsNode = revisionNode.get("fields");
        String commentText = fieldsNode.get("System.History").textValue();
        String changedDate = fieldsNode.get("System.ChangedDate").textValue();
        String createdBy = extractEmail(fieldsNode.get("System.ChangedBy").textValue());
        return new WorkItemComment(commentId, workItemId, commentText, createdBy, changedDate);
    }

    private static String extractEmail(String input) {
        /*
        //extract email from the string using RegEx (the email is enclosed in <>)
        Pattern pattern = Pattern.compile("<(.*?)>");
        Matcher matcher = pattern.matcher(input);
        if(matcher.find()) {
            String userEmail = matcher.group(1);
        }*/

        String userEmail = input.split("<")[1];
        userEmail = userEmail.substring(0, userEmail.length() - 1);
        return userEmail;
    }

    
    private static List<String> getRelevantFields() {
    	if (relevantFields == null) {
    		relevantFields = new ArrayList<>(AzureChangePatcher.fieldMapper.keySet());
        	relevantFields.add("System.CommentCount");
    	}
    	return relevantFields;
    }
    
    private static List<String> relevantFields = null;
   
    		//List.of("System.State", "System.Title",
            //"Microsoft.VSTS.Common.Priority", "System.AssignedTo", "System.CommentCount", "System.Description"); 
}
