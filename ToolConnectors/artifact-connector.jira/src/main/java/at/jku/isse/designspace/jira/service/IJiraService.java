package at.jku.isse.designspace.jira.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import at.jku.isse.designspace.artifactconnector.core.IArtifactProvider;
import at.jku.isse.designspace.artifactconnector.core.exceptions.IdentiferFormatException;
import at.jku.isse.designspace.artifactconnector.core.exceptions.InvalidSchemaException;
import at.jku.isse.designspace.artifactconnector.core.updatememory.UpdateMemory;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.jira.restclient.connector.IJiraTicketService;
import at.jku.isse.designspace.jira.updateservice.ChangeLogItem;
import at.jku.isse.designspace.jira.updateservice.IChangeLogItemFactory;
import at.jku.isse.designspace.jira.updateservice.changemanagment.JiraWebhookConnection;
import jakarta.validation.constraints.NotNull;



public interface IJiraService extends IArtifactProvider {

    enum JiraIdentifier {
        JiraIssueId, JiraIssueKey, JiraProjectId;
    }

    boolean isAutoUpdateActive();

    IHistoryManager getHistoryManager();

    IChangeLogItemFactory getChangeLogItemFactory();

    void enableAutoUpdate();

    void disableAutoUpdate();

    void pushServerToWorkspace();

    void pushServerToWorkspaceWithHistory();

    int applyUpdates(@NotNull Collection<ChangeLogItem> changeLogItems);

    boolean applyUpdate(@NotNull ChangeLogItem changeLogItem);

    Optional<InstanceType> createSchema(@NotNull Map<String, Object> schemaMap,@NotNull Map<String, Object> issueLinkTypes, @NotNull Map<String, Object> namesAndLabel, @NotNull String serverId);

    Optional<InstanceType> findSchema(@NotNull String serverId);

    boolean activateSchema(@NotNull String serverId) throws InvalidSchemaException;

    void activateSchema(@NotNull InstanceType schema) throws InvalidSchemaException;

    /**
     * Looks for the jira instance with the given key in Designspace.
     *
     * @param identifier
     * @return
     */
    Optional<Instance> findInstance(@NotNull String identifier);

    /**
     * Looks for the jira instance with the given key in the associated workspace
     * and on the server, if not found.
     * @param identifier
     * @return
     */
    Optional<Instance> getArtifact(@NotNull String identifier, JiraIdentifier identifierType, boolean forceRefetch) throws IdentiferFormatException;

    /**
     * Looks for the jira instance with the given key in the associated workspace
     * and on the server, if not found. Fetches a played back version of the instance and applies
     * every history event step by step and returns the instance with the history stored in Designspace.
     * @param identifier
     * @return
     */
    Optional<Instance> getIssueWithEventHistory(@NotNull String identifier);

    Optional<Instance> createInstance(Map<String, Object> dataMap, InstanceType instanceType, boolean update, boolean withIssueLinks);

    List<Instance> getAllExistingArtifacts();

    IArtifactPusher getArtifactPusher();

    IJiraTicketService getJiraTicketService();

    Optional<JiraWebhookConnection> getReactiveConnection();

    Workspace getWorkspace();

    UpdateMemory getUpdateMemory();

    InstanceType getCurSchema();

}
