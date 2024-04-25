package at.jku.isse.designspace.azure.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

import at.jku.isse.designspace.artifactconnector.core.IArtifactProvider;
import at.jku.isse.designspace.artifactconnector.core.IService;
import at.jku.isse.designspace.artifactconnector.core.endpoints.grpc.service.IResponder;
import at.jku.isse.designspace.artifactconnector.core.endpoints.grpc.service.ServiceResponse;
import at.jku.isse.designspace.azure.model.WorkItemComment;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;

@Service
@ConditionalOnExpression(value = "not ${azure.enabled:false}")
public class AzureServiceMock implements IAzureService, IResponder, IService, IArtifactProvider {

    @Override
    public InstanceType getArtifactInstanceType() {
        return null;
    }

    @Override
    public void initialize() {

    }

    @Override
    public ServiceResponse getServiceResponse(String id, String service) {
        return new ServiceResponse(ServiceResponse.UNAVAILABLE, service, "This is a Mock! Activate the AzureService!", "");
    }

    @Override
    public ServiceResponse[] getServiceResponse(Set<String> ids, String identifierType) {
        return new ServiceResponse[0];
    }

	@Override
	public ServiceResponse getServiceResponse(String id, String identifierType, boolean doForceRefetch) {
		return getServiceResponse(id, identifierType);
	}

	@Override
	public ServiceResponse[] getServiceResponse(Set<String> ids, String identifierType, boolean doForceRefetch) {
		return getServiceResponse(ids, identifierType);
	}
    
    @Override
    public Optional<List<Instance>> getAllWorkItems() {
        return Optional.empty();
    }

    @Override
    public Optional<Instance> transferAzureWorkItem(String project, int azureId, boolean doForceRefetch) {
        return Optional.empty();
    }

    @Override
    public Optional<Instance> transferUserByEmail(String email) {
        return Optional.empty();
    }

    @Override
    public Optional<Instance> transferUserByDescriptor(String userDescriptor) {
        return Optional.empty();
    }

//    @Override
//    public Optional<Instance> transferWorkItemLink(WorkItemLink workItemLink, String projectName) {
//        return Optional.empty();
//    }

    @Override
    public Optional<Instance> transferComment(WorkItemComment comment) {
        return Optional.empty();
    }

    @Override
    public Optional<Instance> searchForInstance(String id) {
        return Optional.empty();
    }

    @Override
    public InstanceType generateWorkItemInstanceType(String workItemType) {
        return null;
    }

    @Override
    public boolean updateComment(Instance commentInstance, int workItemId, int commentId) {
        return false;
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
	public boolean transferWorkItem(JsonNode revisionNode) {		
		return false;
	}



}
