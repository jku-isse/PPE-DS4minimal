package at.jku.isse.designspace.jira.updateservice;

import java.time.Instant;
import java.util.Optional;

import at.jku.isse.designspace.artifactconnector.core.exceptions.IdentiferFormatException;
import at.jku.isse.designspace.artifactconnector.core.exceptions.PropertyNotFoundException;
import at.jku.isse.designspace.artifactconnector.core.updatememory.UpdateMemory;
import at.jku.isse.designspace.artifactconnector.core.updateservice.core.action.UpdateAction;
import at.jku.isse.designspace.artifactconnector.core.updateservice.core.connection.PollingConnection;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.jira.service.IArtifactPusher;
import at.jku.isse.designspace.jira.service.IJiraService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JiraUpdateAction extends UpdateAction<ChangeLogItem> {

    private IJiraService jiraService;
    private UpdateMemory updateMemory;
    private ChangeLogItem changeLogItem;

    public JiraUpdateAction(ChangeLogItem changeLogItem, IJiraService jiraService, UpdateAction.ActionKind actionKind, PollingConnection.ServerKind serverKind) {
        super(changeLogItem.getTimestamp(), actionKind, serverKind);
        this.changeLogItem = changeLogItem;
        this.jiraService = jiraService;
        this.updateMemory = jiraService.getUpdateMemory();
    }

    public ChangeLogItem getUpdatedValue() {
        return changeLogItem;
    }

    @Override
    public void applyUpdate() {

        IArtifactPusher artifactPusher = jiraService.getArtifactPusher();

        if (artifactPusher != null) {
            ChangeLogItem updatedValue = getUpdatedValue();

            if (updatedValue != null) {
                ChangeLogItem changeLogItem = updatedValue;

                // In case we get an update for an Artifact we check if a corresponding instance already exists
                Optional<Instance> instance = jiraService.findInstance(changeLogItem.getCorrespondingArtifactId());
                this.updateMemory.setLastUpdateTime("Jira", Instant.now());

                if (instance.isPresent()) {
                    // If the instance exists, we apply the update
                    try {
                        updatedValue.applyChange(instance.get(), jiraService);
                        log.debug("JIRA-SERVICE: " + updatedValue + " has been created!");
                        jiraService.getWorkspace().concludeTransaction();
                    } catch (PropertyNotFoundException e) {
                        log.debug("JIRA-SERVICE: The ChangeLogItem " + changeLogItem.getId() + " was not applied, because of the following exception :" + e);
                    }
                } else {
                    try {
                        // If it does not exist, we just fetch the artifact --> it will be up to date for sure
                        Optional<Instance> fetchedInstance = jiraService.getArtifact(changeLogItem.getCorrespondingArtifactId(), IJiraService.JiraIdentifier.JiraIssueKey, false);
                        if (!fetchedInstance.isPresent()) {
                            log.debug("JIRA-SERVICE: The updated instance ( " + changeLogItem.getCorrespondingArtifactId() + ") could not be found on the server");
                        }
                    } catch (IdentiferFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


}
