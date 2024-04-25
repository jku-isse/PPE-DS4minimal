package at.jku.isse.designspace.jira.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import at.jku.isse.designspace.artifactconnector.core.IArtifactProvider;
import at.jku.isse.designspace.artifactconnector.core.IService;
import at.jku.isse.designspace.artifactconnector.core.endpoints.grpc.service.IResponder;
import at.jku.isse.designspace.artifactconnector.core.endpoints.grpc.service.ServiceResponse;
import at.jku.isse.designspace.artifactconnector.core.exceptions.InvalidSchemaException;
import at.jku.isse.designspace.artifactconnector.core.updatememory.UpdateMemory;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.jira.restclient.connector.IJiraTicketService;
import at.jku.isse.designspace.jira.updateservice.ChangeLogItem;
import at.jku.isse.designspace.jira.updateservice.IChangeLogItemFactory;
import at.jku.isse.designspace.jira.updateservice.changemanagment.JiraWebhookConnection;

@Service
@ConditionalOnExpression(value = "not ${jira.enabled:false}")
public class JiraServiceMock implements IJiraService, IResponder, IService, IArtifactProvider {

    public JiraServiceMock() {
    }

    @Override
    public boolean isAutoUpdateActive() {
        return false;
    }

    @Override
    public IHistoryManager getHistoryManager() {
        return null;
    }

    @Override
    public IChangeLogItemFactory getChangeLogItemFactory() {
        return null;
    }

    @Override
    public void enableAutoUpdate() {

    }

    @Override
    public void disableAutoUpdate() {

    }

    @Override
    public void pushServerToWorkspace() {

    }

    @Override
    public void pushServerToWorkspaceWithHistory() {

    }

    @Override
    public int applyUpdates(Collection<ChangeLogItem> changeLogItems) {
        return 0;
    }

    @Override
    public boolean applyUpdate(ChangeLogItem changeLogItem) {
        return false;
    }

    @Override
    public Optional<InstanceType> createSchema(Map<String, Object> schemaMap, Map<String, Object> issueLinkTypes, Map<String, Object> namesLabelMap, String serverId) {
        return Optional.empty();
    }


    @Override
    public Optional<InstanceType> findSchema(String serverId) {
        return Optional.empty();
    }

    @Override
    public boolean activateSchema(String serverId) throws InvalidSchemaException {
        return false;
    }

    @Override
    public void activateSchema(InstanceType schema) throws InvalidSchemaException {

    }

    @Override
    public Optional<Instance> findInstance(String identifier) {
        return Optional.empty();
    }

    @Override
    public Optional<Instance> getArtifact(String identifier, JiraIdentifier identifierType, boolean forceRefetch) {
        return Optional.empty();
    }

    @Override
    public Optional<Instance> getIssueWithEventHistory(String identifier) {
        return Optional.empty();
    }

    @Override
    public Optional<Instance> createInstance(Map<String, Object> dataMap, InstanceType instanceType, boolean update, boolean withIssueLinks) {
        return null;
    }

    @Override
    public List<Instance> getAllExistingArtifacts() {
        return new ArrayList<>();
    }

    @Override
    public IArtifactPusher getArtifactPusher() {
        return null;
    }

    @Override
    public IJiraTicketService getJiraTicketService() {
        return null;
    }

    @Override
    public Optional<JiraWebhookConnection> getReactiveConnection() {
        return Optional.empty();
    }

    @Override
    public Workspace getWorkspace() {
        return null;
    }

    @Override
    public UpdateMemory getUpdateMemory() {
        return null;
    }

    @Override
    public InstanceType getCurSchema() {
        return null;
    }

    @Override
    public void initialize() {

    }

    @Override
    public ServiceResponse getServiceResponse(String id, String service) {
        return new ServiceResponse(ServiceResponse.UNKNOWN, service, "This is a service mock! Activate the Jira Service!", "");
    }

    @Override
    public ServiceResponse[] getServiceResponse(Set<String> ids, String identifierType) {
        ArrayList<ServiceResponse> serviceResponses = new ArrayList<>();

        for (String id : ids) {
            serviceResponses.add(getServiceResponse(id, identifierType));
        }

        return (ServiceResponse[]) serviceResponses.toArray();
    }

    @Override
    public InstanceType getArtifactInstanceType() {
        return null;
    }
    
	@Override
	public Set<InstanceType> getArtifactInstanceTypes() {
		return Collections.emptySet();
	}

	@Override
	public Map<InstanceType, List<String>> getSupportedIdentifier() {
		return Collections.emptyMap();
	}

	@Override
	public ServiceResponse getServiceResponse(String id, String identifierType, boolean doForceRefetch) {
		return getServiceResponse(id, identifierType);
	}

	@Override
	public ServiceResponse[] getServiceResponse(Set<String> ids, String identifierType, boolean doForceRefetch) {
		return getServiceResponse(ids, identifierType);
	}
	
	

}