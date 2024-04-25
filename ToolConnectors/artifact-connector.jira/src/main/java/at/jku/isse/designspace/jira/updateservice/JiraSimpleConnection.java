package at.jku.isse.designspace.jira.updateservice;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.springframework.core.io.support.PropertiesLoaderUtils;

import at.jku.isse.designspace.artifactconnector.core.idcache.IdCache;
import at.jku.isse.designspace.artifactconnector.core.model.BaseElementType;
import at.jku.isse.designspace.artifactconnector.core.updateservice.core.action.UpdateAction;
import at.jku.isse.designspace.artifactconnector.core.updateservice.core.connection.PollingConnection;
import at.jku.isse.designspace.core.service.WorkspaceService;
import at.jku.isse.designspace.jira.model.JiraBaseElementType;
import at.jku.isse.designspace.jira.restclient.connector.IJiraTicketService;
import at.jku.isse.designspace.jira.service.IJiraService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JiraSimpleConnection extends PollingConnection {

    private int POLL_INTERVAL_SECONDS = 2*60;

    private IJiraTicketService jiraTicketService;
    private Set<String> alreadyCreated;
    private IJiraService jiraService;

    /**
     *
     * The Jira connector is a pooling connector constantly feeding the queue with
     * new information about the server.
     *
     */
    public JiraSimpleConnection(String serverName, IJiraService jiraService, IJiraTicketService jiraTicketService, Timestamp lastFetch) {
        super(serverName, ServerKind.JIRA, lastFetch, 20);

        Properties props = new Properties();
        try {
            FileReader reader = new FileReader("./application.properties");
            props.load(reader);
        } catch (IOException ioe) {
            try {
                props = PropertiesLoaderUtils.loadAllProperties("application.properties");
            } catch (FileNotFoundException e) {
                log.debug("JiraConnection: properties file could not be found!");
            } catch (IOException e) {
                log.debug("JiraConnection: properties file could not be opened!");
            }
        }

        Object property = props.get("jira.poll.interval");
        if (property != null) {
            try {
                this.POLL_INTERVAL_SECONDS = Integer.parseInt(property.toString());
                this.POLL_INTERVAL_SECONDS = this.POLL_INTERVAL_SECONDS * 60;
                log.debug("JiraConnection: The poll interval has been set to " + this.POLL_INTERVAL_SECONDS);
            } catch (NumberFormatException numberFormatException) {
                log.debug("JiraConnection: interval must be an integer (properties)!");
            }
        }

        this.pollInterval = POLL_INTERVAL_SECONDS;
        IdCache idCache = new IdCache(WorkspaceService.PUBLIC_WORKSPACE, JiraBaseElementType.SERVICE_ID_TO_DESIGNSPACE_ID_CACHE_ID);
        this.alreadyCreated = idCache.getAllServiceIds();
        this.jiraTicketService = jiraTicketService;
        this.jiraService = jiraService;
    }

    /**
     *
     * in some cases we might already have stored some artifacts,
     * which can be specified in a set in order to not perform a redundant fetch.
     * This is is necessary in the case of jira, because it always returns
     * artifacts attached to updates made.
     *
     */
    public JiraSimpleConnection(String serverName, Timestamp lastFetch, IJiraTicketService jiraTicketService) {
        super(serverName, ServerKind.JIRA, lastFetch, 20);
        IdCache idCache = new IdCache(WorkspaceService.PUBLIC_WORKSPACE, JiraBaseElementType.SERVICE_ID_TO_DESIGNSPACE_ID_CACHE_ID);
        this.alreadyCreated = idCache.getAllServiceIds();
        this.jiraTicketService = jiraTicketService;
    }

    /**
     * This can be used to inform the listeners about the creation of an Artifact
     */
    private void raiseSimpleAction(Map<String, Object> artifact) {
        publishedActions.onNext(new JiraSimpleAction(artifact, jiraService, UpdateAction.ActionKind.ARTIFACT_CREATION_ACTION, serverKind));
        String key = artifact.get(BaseElementType.KEY).toString();
        log.debug("JIRA-SERVICE : An update created the artifact with the key " + key);
    }

    private void fetchDelta() {
        if(lastFetch == null) {
            lastFetch = Timestamp.from(Instant.now());
        }

        try {
            Timestamp newLastFetch = Timestamp.from(Instant.now());
            List<Object> issues = jiraTicketService.getAllUpdatedArtifacts(lastFetch);

            for (Object issue : issues) {
                Map<String, Object> issueData = (Map<String, Object>) issue;
                raiseSimpleAction(issueData);
            }

            lastFetch = newLastFetch;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        if (active) {
            fetchDelta();
            WorkspaceService.PUBLIC_WORKSPACE.concludeTransaction();
        }
    }

}
