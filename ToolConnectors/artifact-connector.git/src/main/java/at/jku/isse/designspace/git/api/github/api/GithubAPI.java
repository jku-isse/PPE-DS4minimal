package at.jku.isse.designspace.git.api.github.api;

import at.jku.isse.designspace.git.api.*;
import at.jku.isse.designspace.git.api.core.IGitAPI;
import at.jku.isse.designspace.git.api.core.InsufficientDataException;
import at.jku.isse.designspace.git.api.core.webhookparser.IChangeFactory;
import at.jku.isse.designspace.git.api.github.implementation.*;
import at.jku.isse.designspace.git.api.github.restclient.GitHubGithubRestClient;
import at.jku.isse.designspace.git.api.github.restclient.IGithubRestClient;
import at.jku.isse.designspace.git.api.github.webhookparser.GitHubChangeFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GithubAPI implements IGitAPI {

    private final GitHubGithubRestClient gitHubGitRestClient;
    private final GitHubChangeFactory gitHubChangeFactory;

    public GithubAPI(String username, String accessToken) {
        this.gitHubGitRestClient = new GitHubGithubRestClient(username, accessToken);
        this.gitHubChangeFactory = new GitHubChangeFactory(this.gitHubGitRestClient);
    }

    @Override
    public Optional<IGitIssue> getIssue(String repositoryName, int key) {
        IssueResource issueResource = new IssueResource(this.gitHubGitRestClient, repositoryName, key);
        if (issueResource.existsOnServer()) {
            return Optional.of(issueResource);
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<IGitIssue>> getIssues(String repositoryName) {
        ArrayList<IGitIssue> issues = new ArrayList<>();
        ArrayList<Map<String, Object>> rawIssues = this.gitHubGitRestClient.getIssues(repositoryName);

        for (Map<String, Object> rawIssue : rawIssues) {
            try {
                IssueResource issueResource = new IssueResource(this.gitHubGitRestClient, repositoryName, rawIssue);
                issues.add(issueResource);
            } catch (InsufficientDataException e) {
                System.out.println("GithubApi: The Rest Response contains insufficient data");
            }
        }

        return Optional.of(issues);
    }

    @Override
    public Optional<IGitPullRequest> getPullRequest(String repositoryName, int key) {
        PullRequestResource pullRequestsResource = new PullRequestResource(this.gitHubGitRestClient, repositoryName, key);
        if (pullRequestsResource.existsOnServer()) {
            return Optional.of(pullRequestsResource);
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<IGitPullRequest>> getPullRequests(String repositoryName) {
        ArrayList<IGitPullRequest> pullRequests = new ArrayList<>();
        ArrayList<Map<String, Object>> rawPullRequests = this.gitHubGitRestClient.getPullRequests(repositoryName);

        for (Map<String, Object> rawPullRequest : rawPullRequests) {
            try {
                PullRequestResource pullRequestResource = new PullRequestResource(this.gitHubGitRestClient, repositoryName, rawPullRequest);
                pullRequests.add(pullRequestResource);
            } catch (InsufficientDataException e) {
                System.out.println("GithubApi: The Rest Response contains insufficient data");
            }
        }

        return Optional.of(pullRequests);
    }

    @Override
    public Optional<IGitCommit> getCommit(String repositoryName, String sha) {
        CommitResource commitResource = new CommitResource(this.gitHubGitRestClient, repositoryName, new String[0], sha);
        if (commitResource.existsOnServer()) {
            return Optional.of(commitResource);
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<IGitCommit>> getCommits(String repositoryName) {
        ArrayList<IGitCommit> commits = new ArrayList<>();
        ArrayList<Map<String, Object>> rawCommits = this.gitHubGitRestClient.getCommits(repositoryName);

        for (Map<String, Object> rawCommit : rawCommits) {
            Object sha = rawCommit.get("sha");
            if (sha != null) {
                CommitResource commitResource = new CommitResource(this.gitHubGitRestClient, repositoryName, new String[0], (String) sha);
                commits.add(commitResource);
            }
        }

        return Optional.of(commits);
    }

    @Override
    public Optional<IGitFile> getFile(String repositoryName, String contentPath) {
        FileResource fileResource = new FileResource(this.gitHubGitRestClient, repositoryName, contentPath);
        return Optional.of(fileResource);
    }

    @Override
    public Optional<IGitBranch> getBranch(String repositoryName, String branchName) {
        return Optional.empty();
    }

    @Override
    public Optional<List<IGitBranch>> getBranches(String repositoryName) {
        return Optional.empty();
    }

    @Override
    public Optional<IGitProject> getProject(String projectName) {
        return Optional.empty();
    }

    @Override
    public Optional<IGitRepository> getRepository(String repositoryName) {
        RepositoryResource repositoryResource = new RepositoryResource(this.gitHubGitRestClient, repositoryName);
        if (repositoryResource.existsOnServer()) {
            return Optional.of(repositoryResource);
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<IGitRepository>> getRepositories() {
        RepositoriesResource repositoriesResource = new RepositoriesResource(this.gitHubGitRestClient);
        return Optional.of(repositoriesResource.getResources());
    }

    @Override
    public Optional<IGitUser> getUser(String userName) {
        UserResource usersResource = new UserResource(this.gitHubGitRestClient, userName);
        if (usersResource.existsOnServer()) {
            return Optional.of(usersResource);
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<IGitComment>> getIssueComments(String repositoryName, int issueKey) {
        IssueCommentsResource commentResource = new IssueCommentsResource(this.gitHubGitRestClient, repositoryName, issueKey);
        return Optional.of(commentResource.getResources());
    }

    @Override
    public IChangeFactory getChangeFactory() {
        return this.gitHubChangeFactory;
    }

    @Override
    public String createWebhook(String repositoryName, String forwardAddress) {
        return this.gitHubGitRestClient.createWebHook(repositoryName, forwardAddress);
    }

    @Override
    public String deleteWebhook(String repositoryName, String forwardAddress) {
        return this.gitHubGitRestClient.deleteWebhook(repositoryName, forwardAddress);
    }

    @Override
    public String setWebhookActive(String repositoryName, String forwardAddress, boolean active) {
        return this.gitHubGitRestClient.setWebhookActive(repositoryName, forwardAddress, active);
    }

    @Override
    public IGithubRestClient getGitRestClient() {
        return this.gitHubGitRestClient;
    }

    @Override
    public boolean isCommitInBranch(String repositoryName, String branch, String sha) {
        Map<String, Object> compareResponse = this.gitHubGitRestClient.getCompareResponse(repositoryName, branch, sha);
        if (compareResponse != null) {
            Object status = compareResponse.get("status");
            return status.equals("identical") || status.equals("behind");
        }
        return false;
    }

}
