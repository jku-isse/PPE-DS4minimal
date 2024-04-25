package at.jku.isse.designspace.jira.updateservice;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;

import at.jku.isse.designspace.artifactconnector.core.updateservice.core.action.UpdateAction;
import at.jku.isse.designspace.artifactconnector.core.updateservice.core.connection.PollingConnection;
import at.jku.isse.designspace.jira.service.IJiraService;

public class JiraSimpleAction extends UpdateAction<Map<String, Object>> {

    private Map<String, Object> issue;
    private IJiraService jiraService;

    public JiraSimpleAction(Map<String, Object> issue, IJiraService jiraService, ActionKind actionKind, PollingConnection.ServerKind serverKind) {
        super(Timestamp.from(Instant.now()), actionKind, serverKind);
        this.issue = issue;
        this.jiraService = jiraService;
    }

    @Override
    public Map<String, Object> getUpdatedValue() {
        return issue;
    }

    @Override
    public void applyUpdate() {
        Map<String, Object> issue = getUpdatedValue();
        if (issue != null) {
            if (issue != null) {
                this.jiraService.createInstance(issue, jiraService.getArtifactInstanceType(), true, true);
            }
        }
    }

}
