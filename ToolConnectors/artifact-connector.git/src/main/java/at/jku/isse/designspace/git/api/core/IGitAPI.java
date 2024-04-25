package at.jku.isse.designspace.git.api.core;

import at.jku.isse.designspace.git.api.*;
import at.jku.isse.designspace.git.api.core.webhookparser.IChangeFactory;
import at.jku.isse.designspace.git.api.github.restclient.IGithubRestClient;

import java.util.List;
import java.util.Optional;

public interface IGitAPI {

    Optional<IGitIssue> getIssue(String repositoryName, int key);

    Optional<List<IGitIssue>> getIssues(String repositoryName);

    Optional<IGitPullRequest> getPullRequest(String repositoryName, int key);

    Optional<List<IGitPullRequest>> getPullRequests(String repositoryName);

    Optional<IGitCommit> getCommit(String repositoryName, String sha);

    Optional<List<IGitCommit>> getCommits(String repositoryName);

    Optional<IGitFile> getFile(String repositoryName, String contentPath);

    Optional<IGitBranch> getBranch(String repositoryName, String branchName);

    Optional<List<IGitBranch>> getBranches(String repositoryName);

    Optional<IGitProject> getProject(String projectName);

    Optional<IGitRepository> getRepository(String repositoryName);

    Optional<List<IGitRepository>> getRepositories();

    Optional<IGitUser> getUser(String userName);

    Optional<List<IGitComment>> getIssueComments(String repsitoryName, int issueKey);

    IChangeFactory getChangeFactory();

    IGithubRestClient getGitRestClient();

    boolean isCommitInBranch(String repositoryName, String branch, String sha);

    String createWebhook(String repositoryName, String forwardAddress);

    String deleteWebhook(String repositoryName, String forwardAddress);

    String setWebhookActive(String repositoryName, String forwardAddress, boolean active);

}
