package at.jku.isse.designspace.git.service;

import at.jku.isse.designspace.artifactconnector.core.IArtifactProvider;
import at.jku.isse.designspace.artifactconnector.core.IService;
import at.jku.isse.designspace.artifactconnector.core.endpoints.grpc.service.IResponder;
import at.jku.isse.designspace.artifactconnector.core.endpoints.grpc.service.ServiceResponse;
import at.jku.isse.designspace.artifactconnector.core.exceptions.IdentiferFormatException;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.git.api.*;
import at.jku.isse.designspace.git.api.core.IGitAPI;
import at.jku.isse.designspace.git.model.GitBaseElementType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service("gitService")
@ConditionalOnExpression(value = "not ${git.enabled:false}")
public class GitServiceMock implements IGitService, IResponder, IService, IArtifactProvider {

    @Override
    public InstanceType getArtifactInstanceType() {
        return null;
    }

    @Override
    public void initialize() {

    }

    @Override
    public ServiceResponse getServiceResponse(String id, String service) {
        return new ServiceResponse(ServiceResponse.UNAVAILABLE, service, "This is a Mock! Activate the GitService!", "");
    }

    @Override
    public ServiceResponse[] getServiceResponse(Set<String> id, String identifierType) {
        return new ServiceResponse[0];
    }

    @Override
    public Optional<Instance> getArtifact(String identifier, GitIdentifier gitIdentifier) throws IdentiferFormatException {
        return Optional.empty();
    }

    @Override
    public Optional<Instance> getRepo(String repoName, boolean withLinks) {
        return Optional.empty();
    }

    @Override
    public Optional<Instance> getIssue(String repoName, int issueId) {
        return Optional.empty();
    }

    @Override
    public Optional<List<Instance>> getIssues(String repoName) {
        return Optional.empty();
    }

    @Override
    public Optional<Instance> getPullRequest(String repoName, int requestId) {
        return Optional.empty();
    }

    @Override
    public Optional<List<Instance>> getPullRequests(String repoName) {
        return Optional.empty();
    }

    @Override
    public Optional<Instance> getUser(String userId) {
        return Optional.empty();
    }

    @Override
    public Optional<Instance> getFile(String repositoryName, String localPath) {
        return Optional.empty();
    }

    @Override
    public Optional<Instance> getBranch(String repositoryName, String branchName) {
        return Optional.empty();
    }

    @Override
    public Optional<List<Instance>> getBranches(String repoName) {
        return Optional.empty();
    }

    @Override
    public Optional<Instance> getCommit(String repositoryName, String sha) {
        return Optional.empty();
    }

    @Override
    public Optional<List<Instance>> getCommits(String repoName) {
        return Optional.empty();
    }

    @Override
    public Optional<Instance> getProject(String projectName) {
        return Optional.empty();
    }

    @Override
    public Optional<Instance> getTag(String repositoryName, String tagName) {
        return Optional.empty();
    }

    @Override
    public Optional<Instance> getAllRepos() {
        return Optional.empty();
    }

    @Override
    public Optional<IGitAPI> getAPI() {
        return Optional.empty();
    }

    @Override
    public boolean isIssueFetched(String repoName, int key) {
        return false;
    }

    @Override
    public Optional<Instance> searchForInstance(String id) {
        return Optional.empty();
    }

    @Override
    public Instance transferIssue(IGitIssue issue) {
        return null;
    }

    @Override
    public Instance transferCommit(IGitCommit commit) {
        return null;
    }

    @Override
    public Instance transferUser(IGitUser user) {
        return null;
    }

    @Override
    public Instance transferPullRequestReview(IGitPullRequestReview requestReview) {
        return null;
    }

    @Override
    public Instance transferComment(IGitComment comment) {
        return null;
    }

    @Override
    public Instance transferProject(IGitProject project) {
        return null;
    }

    @Override
    public Instance transferPullRequest(IGitPullRequest pullRequest) {
        return null;
    }

    @Override
    public Instance transferBranch(IGitBranch branch) {
        return null;
    }

    @Override
    public Instance transferRepo(IGitRepository repository, boolean withLinks) {
        return null;
    }

    @Override
    public Instance getInstanceOrCreatePlaceholder(String issueId, String issueKey, String name, GitBaseElementType type) {
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
		return new ServiceResponse(ServiceResponse.UNAVAILABLE, identifierType, "This is a Mock! Activate the GitService!", "");	    
	}

	@Override
	public ServiceResponse[] getServiceResponse(Set<String> ids, String identifierType, boolean doForceRefetch) {
		return getServiceResponse(ids, identifierType);
	}

}