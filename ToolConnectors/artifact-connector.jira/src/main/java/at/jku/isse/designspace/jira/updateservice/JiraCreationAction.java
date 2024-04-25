package at.jku.isse.designspace.jira.updateservice;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import at.jku.isse.designspace.artifactconnector.core.model.BaseElementType;
import at.jku.isse.designspace.artifactconnector.core.updatememory.UpdateMemory;
import at.jku.isse.designspace.artifactconnector.core.updateservice.core.action.UpdateAction;
import at.jku.isse.designspace.artifactconnector.core.updateservice.core.connection.PollingConnection;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.Property;
import at.jku.isse.designspace.jira.service.IArtifactPusher;
import at.jku.isse.designspace.jira.service.IJiraService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JiraCreationAction extends UpdateAction<Map<String, Object>> {

    private Map<String, Object> artifact;

    private UpdateMemory updateMemory;

    private IJiraService jiraService;

    public JiraCreationAction(Map<String, Object> artifact, IJiraService jiraService, Timestamp timestamp, ActionKind actionKind, PollingConnection.ServerKind serverKind) {
        super(timestamp, actionKind, serverKind);
        this.artifact = artifact;
        this.jiraService = jiraService;
        this.updateMemory = jiraService.getUpdateMemory();
    }

    @Override
    public Map<String, Object> getUpdatedValue() {
        return artifact;
    }

    @Override
    public void applyUpdate() {
        Map<String, Object> artifact = getUpdatedValue();
        if (artifact != null) {
            IArtifactPusher artifactPusher = jiraService.getArtifactPusher();
            if (artifactPusher != null) {
                Optional<Instance> instance = artifactPusher.findArtifact(artifact.get(BaseElementType.ID).toString());

                // Creation actions have to be filtered, because jira updates contain every changed item,
                // therefore it is not clear, which of them are really new ones.

                Map<String, Object> updatedValue = getUpdatedValue();
                if (updatedValue != null) {
                    if (instance.isEmpty()) {
                        jiraService.createInstance(getUpdatedValue(), jiraService.getCurSchema(), false, true);
                        log.debug("JIRA-SERVICE: " + updatedValue + " has been created!");
                    } else {
                        Property fullyFetchedProp = instance.get().getProperty(BaseElementType.FULLY_FETCHED);
                        if (fullyFetchedProp != null) {
                            if (!((boolean) fullyFetchedProp.get())) {
                                jiraService.createInstance(updatedValue, jiraService.getCurSchema(), true, true);
                                log.debug("JIRA-SERVICE: " + updatedValue + " has been created!");
                            }
                        }
                    }
                    this.updateMemory.setLastUpdateTime("Jira", Instant.now());
                    jiraService.getWorkspace().concludeTransaction();
                }
            }
        }
    }

}
