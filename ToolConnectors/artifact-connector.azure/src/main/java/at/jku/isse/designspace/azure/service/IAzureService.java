package at.jku.isse.designspace.azure.service;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;

import at.jku.isse.designspace.artifactconnector.core.IArtifactProvider;
import at.jku.isse.designspace.azure.model.WorkItemComment;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;

public interface IAzureService extends IArtifactProvider {

    Optional<List<Instance>> getAllWorkItems();

    Optional<Instance> transferAzureWorkItem(String project, int azureId,  boolean doForceRefetch);

    Optional<Instance> transferUserByEmail(String email);

    Optional<Instance> transferUserByDescriptor(String userDescriptor);

    //Optional<Instance> transferComment(String commentId);

  //  Optional<Instance> transferWorkItemLink(WorkItemLink workItemLink, String projectName);

    Optional<Instance> transferComment(WorkItemComment comment);

    Optional<Instance> searchForInstance(String id);

    InstanceType generateWorkItemInstanceType(String workItemType);

    boolean updateComment(Instance commentInstance, int workItemId, int commentId);

	boolean transferWorkItem(JsonNode revisionNode);
    
	//public Set<String> getProvidedTypes(); // FIXME needs to be provided by any Service
}
