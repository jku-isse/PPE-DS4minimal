package at.jku.isse.designspace.git.service;

import at.jku.isse.designspace.artifactconnector.core.IArtifactProvider;
import at.jku.isse.designspace.artifactconnector.core.endpoints.grpc.service.ServiceResponse;
import at.jku.isse.designspace.artifactconnector.core.exceptions.IdentiferFormatException;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.git.api.*;
import at.jku.isse.designspace.git.api.core.IGitAPI;
import at.jku.isse.designspace.git.model.GitBaseElementType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IGitService extends IArtifactProvider {

    enum GitIdentifier {
        GitRepositoryName, GitIssueId, GitUserId,
        GitCommitSha, GitProjectName, GitFile
    }

    Optional<Instance> getArtifact(String identifier, GitIdentifier gitIdentifier) throws IdentiferFormatException;

    Optional<Instance> getRepo(String repoName, boolean withLinks);

    Optional<Instance> getIssue(String repoName, int issueId);

    Optional<List<Instance>> getIssues(String repoName);

    Optional<Instance> getPullRequest(String repoName, int requestId);

    Optional<List<Instance>> getPullRequests(String repoName);

    Optional<Instance> getUser(String userId);

    Optional<Instance> getFile(String repositoryName, String localPath);

    Optional<Instance> getBranch(String repositoryName, String branchName);

    Optional<List<Instance>> getBranches(String repoName);

    Optional<Instance> getCommit(String repositoryName, String sha);

    Optional<List<Instance>> getCommits(String repoName);

    Optional<Instance> getProject(String projectName);

    Optional<Instance> getTag(String repositoryName, String tagName);

    Optional<Instance> getAllRepos();

    Optional<IGitAPI> getAPI();

    boolean isIssueFetched(String repoName, int key);
    Optional<Instance> searchForInstance(String id);

    Instance transferIssue(IGitIssue issue);

    Instance transferCommit(IGitCommit commit);

    Instance transferUser(IGitUser user);

    Instance transferPullRequestReview(IGitPullRequestReview requestReview);

    Instance transferComment(IGitComment comment);

    Instance transferProject(IGitProject project);

    Instance transferPullRequest(IGitPullRequest pullRequest);

    Instance transferBranch(IGitBranch branch);

    Instance transferRepo(IGitRepository repository, boolean withLinks);

    Instance getInstanceOrCreatePlaceholder(String issueId, String issueKey, String name, GitBaseElementType type);


}
