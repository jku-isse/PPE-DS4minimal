package at.jku.isse.designspace.jira.updateservice.changemanagment;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import at.jku.isse.designspace.artifactconnector.core.model.BaseElementType;
import at.jku.isse.designspace.artifactconnector.core.updateservice.core.action.UpdateAction;
import at.jku.isse.designspace.artifactconnector.core.updateservice.core.connection.ReactiveConnection;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.jira.service.IJiraService;
import at.jku.isse.designspace.jira.updateservice.ChangeLogItem;
import at.jku.isse.designspace.jira.updateservice.IChangeLogItemFactory;
import at.jku.isse.designspace.jira.updateservice.JiraCreationAction;
import at.jku.isse.designspace.jira.updateservice.JiraUpdateAction;
import at.jku.isse.designspace.jira.updateservice.RelationChangeLogItem;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JiraWebhookConnection extends ReactiveConnection implements IJiraWebhookConnection {

    private IJiraService jiraService;

    private IChangeLogItemFactory changeLogItemFactory;

    private static final String WEBHOOK_EVENT = "webhookEvent";

    private static final String ISSUE_UPDATED_EVENT_VALUE = "jira:issue_updated";
    private static final String ISSUE_CREATED_EVENT_VALUE = "jira:issue_created";
    private static final String ISSUE_LINK_CREATED_EVENT_VALUE = "issuelink_created";
    private static final String ISSUE_LINK_DELETED_EVENT_VALUE = "issuelink_deleted";

    public JiraWebhookConnection(IJiraService jiraService, IChangeLogItemFactory changeLogItemFactory) {
        super("jira_server", ServerKind.JIRA);
        this.changeLogItemFactory = changeLogItemFactory;
        this.jiraService = jiraService;
    }

    @Override
    public List<ChangeLogItem> parseChanges(Map<String, Object> webhookUpdate) {
        List<ChangeLogItem> changes = new ArrayList<>();
        String createdTime = Timestamp.from(Instant.now()).toString();

        if (webhookUpdate != null) {
            Object updateKind = webhookUpdate.get(WEBHOOK_EVENT);
            Object timestamp_ = webhookUpdate.get("timestamp");

            try {
                createdTime = new Timestamp(Long.parseLong(timestamp_.toString())).toString();
            } catch (Exception e) {
            }

            if (updateKind != null) {
                if (updateKind.equals(ISSUE_UPDATED_EVENT_VALUE)) {

                    Object issue_ = webhookUpdate.get("issue");
                    Object changeLog_ = webhookUpdate.get("changelog");

                    if (changeLog_ != null && issue_ != null) {
                        Map<String, Object> issue = (Map<String, Object>) issue_;
                        Map<String, Object> changeLog = (Map<String, Object>) changeLog_;

                        String id = issue.get(BaseElementType.ID).toString();
                        String key = issue.get(BaseElementType.KEY).toString();
                        changeLog.put("created", Timestamp.from(Instant.now()).toString());

                        ArrayList<Map<String, Object>> histories = new ArrayList();
                        histories.add(changeLog);
                        changeLog.put("histories", histories);
                        issue.put("changelog", changeLog);

                        ArrayList<ChangeLogItem> items = new ArrayList<>();
                        for (Map<String, Object> change : histories) {
                            items.addAll(changeLogItemFactory.createChangeLog(change, id, key));
                        }

                        if (items != null) {
                            for (ChangeLogItem change : items) {
                                change.setTimeCreated(createdTime);
                                raiseUpdateAction(change);
                            }
                        }

                    }
                }
            }

            if (updateKind.equals(ISSUE_CREATED_EVENT_VALUE)) {
                Object issue_ = webhookUpdate.get("issue");
                if (issue_ != null) {
                    Map<String, Object> issueData = (Map<String, Object>) issue_;
                    raiseCreationAction(issueData);
                }
            }

            boolean linkCreation = updateKind.equals(ISSUE_LINK_CREATED_EVENT_VALUE);
            boolean linkDeletion = updateKind.equals(ISSUE_LINK_DELETED_EVENT_VALUE);

            if (linkCreation || linkDeletion) {
                Object issueLink_ = webhookUpdate.get("issueLink");
                if (issueLink_ != null) {
                    Map<String, Object> issueLink = (Map<String, Object>) issueLink_;

                    Object linkType_ = issueLink.get("issueLinkType");
                    if (linkType_ != null) {
                        Map<String, Object> linkType = (Map<String, Object>) linkType_;
                        Object isSubtask = linkType.get("isSubTaskLinkType");

                        Object sourceId = issueLink.get("sourceIssueId");
                        Object destinationId = issueLink.get("destinationIssueId");

                        RelationChangeLogItem sourceChange = new RelationChangeLogItem();
                        RelationChangeLogItem destinationChange = new RelationChangeLogItem();

                        String[] timeSplitted = createdTime.split("\\+");
                        if (timeSplitted.length > 1) {
                            createdTime = timeSplitted[0];
                        }

                        sourceChange.setTimeCreated(createdTime);
                        sourceChange.setField("link");

                        destinationChange.setTimeCreated(createdTime);
                        destinationChange.setField("link");

                        if (sourceId != null) {
                            Optional<Instance> sourceInstance = jiraService.getArtifactPusher().findArtifact(sourceId.toString());

                            Object sourceKey = null;

                            if (sourceInstance.isPresent() && sourceInstance.get().hasProperty(BaseElementType.KEY)) {
                                sourceKey = sourceInstance.get().getProperty(BaseElementType.KEY).get();
                            }

                            if (linkCreation) {
                                destinationChange.setFromId(null);
                                destinationChange.setFromKey(null);
                                destinationChange.setToId(sourceId.toString());
                                if (sourceKey != null) {
                                    destinationChange.setToKey(sourceKey.toString());
                                }
                            } else {
                                destinationChange.setFromId(sourceId.toString());
                                if (sourceKey != null) {
                                    destinationChange.setFromKey(sourceKey.toString());
                                }
                                destinationChange.setToId(null);
                                destinationChange.setToKey(null);
                            }

                            if (sourceInstance.isPresent()) {
                                //we only add these changes when the artifact has not been freshly fetched
                                changes.add(sourceChange);
                            } else {
                                jiraService.getArtifactPusher().createPlaceholderArtifact(sourceId.toString(), sourceId.toString(), this.jiraService.getCurSchema());
                            }
                        }

                        if (destinationId != null) {
                            Optional<Instance> destinationInstance = jiraService.getArtifactPusher().findArtifact(destinationId.toString());

                            Object destinationKey = null;
                            if (destinationInstance.isPresent() && destinationInstance.get().hasProperty(BaseElementType.KEY)) {
                                destinationKey = destinationInstance.get().getProperty(BaseElementType.KEY).get();
                            }

                            if (linkCreation) {
                                sourceChange.setFromId(null);
                                sourceChange.setFromKey(null);
                                sourceChange.setToId(destinationId.toString());
                                if (destinationKey != null) {
                                    sourceChange.setToKey(destinationKey.toString());
                                }
                            } else {
                                sourceChange.setFromId(destinationId.toString());
                                if (destinationKey != null) {
                                    sourceChange.setFromKey(destinationKey.toString());
                                }
                                sourceChange.setToId(null);
                                sourceChange.setToKey(null);
                            }

                            if (destinationInstance.isPresent()) {
                                //we only add these changes when the artifact has not been freshly fetched
                                changes.add(destinationChange);
                            } else {
                                jiraService.getArtifactPusher().createPlaceholderArtifact(destinationId.toString(), destinationId.toString(), this.jiraService.getCurSchema());
                            }
                        }

                        sourceChange.setArtifactId(sourceId.toString());
                        destinationChange.setArtifactId(destinationId.toString());
                        sourceChange.setCorrespondingArtifactId(sourceId.toString());
                        sourceChange.setCorrespondingArtifactIdInSource(sourceId.toString());
                        destinationChange.setCorrespondingArtifactId(destinationId.toString());
                        destinationChange.setCorrespondingArtifactIdInSource(destinationId.toString());

                        sourceChange.setArtifactIsSource(true);
                        destinationChange.setArtifactIsSource(false);

                        if (isSubtask != null) {
                            boolean subtask = (boolean) isSubtask;

                            if (subtask) {
                                changes.remove(destinationChange);
                                sourceChange.setField("subtask");
                                sourceChange.setSourceRole("is SUBTASK of");
                                sourceChange.setDestinationRole("is PARENT of");
                            } else {
                                sourceChange.setField("link");
                                destinationChange.setField("link");

                                Object outwardRelation = linkType.get("outwardName");
                                Object inwardRelation = linkType.get("inwardName");

                                if (outwardRelation != null) {
                                    sourceChange.setSourceRole(outwardRelation.toString());
                                    destinationChange.setSourceRole(outwardRelation.toString());
                                }

                                if (inwardRelation != null) {
                                    sourceChange.setDestinationRole(inwardRelation.toString());
                                    destinationChange.setDestinationRole(inwardRelation.toString());
                                }
                            }
                        }

                        changes.forEach(change -> raiseUpdateAction(change));
                    }
                }
            }
        }

        return changes;
    }

    /**
     * This can be used to inform the listeners about the creation of an Artifact
     */
    private void raiseCreationAction(Map<String, Object> issueData) {
        publishedActions.onNext(new JiraCreationAction(issueData, jiraService, Timestamp.from(Instant.now()), UpdateAction.ActionKind.ARTIFACT_CREATION_ACTION, serverKind));
        log.debug("JIRA-SERVICE : An update created the artifact with the key " + issueData.get(BaseElementType.KEY));
    }

    /**
     * This can be used to inform the listeners about a fine grained change of an Artifact
     */
    private void raiseUpdateAction(ChangeLogItem changeLogItem) {
        try{
            publishedActions.onNext(new JiraUpdateAction(changeLogItem, jiraService, UpdateAction.ActionKind.UPDATE_ACTION, serverKind));
            log.debug(serverKind + " : An update was applied to the artifact with the key " + changeLogItem.getCorrespondingArtifactIdInSource());
            log.debug("JIRA-CHANGE: timestamp: " + changeLogItem.getTimestamp());
        } catch (NullPointerException e) {
            log.debug("JIRA-SERVICE : An update failed");
        }
    }

}
