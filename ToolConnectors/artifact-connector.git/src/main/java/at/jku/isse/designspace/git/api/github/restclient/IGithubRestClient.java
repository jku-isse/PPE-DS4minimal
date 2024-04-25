package at.jku.isse.designspace.git.api.github.restclient;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Map;

public interface IGithubRestClient {

    Map<String, Object> getIssue(String repositoryName, int key);

    ArrayList<Map<String, Object>> getIssues(String repositoryName);

    ArrayList<Map<String, Object>> getIssueTimeline(String repositoryName, int key);

    Map<String, Object> getPullRequest(String repositoryName, int key);

    ArrayList<Map<String, Object>>  getPullRequests(String repositoryName);

    Map<String, Object> getCommit(String repositoryName, String sha);

    ArrayList<Map<String, Object>> getCommits(String repositoryName, int pullRequestKey);

    ArrayList<Map<String, Object>> getCommits(String repositoryName);

    ArrayList<Map<String, Object>>  getCommitsSince(String repositoryName, ZonedDateTime zonedDateTime);

    Map<String, Object> getBranch(String repositoryName, String branchName);

    ArrayList<Map<String, Object>>  getBranches(String repositoryName);

    Map<String, Object> getProject(int projectId);

    Map<String, Object> getProject(String projectName);

    Map<String, Object> getProjectColumn(int columnId);

    Map<String, Object> getRepository(String repositoryName);

    Map<String, Object> getFile(String repositoryName, String localPath);

    Map<String, Object> getFile(String repositoryName, String filePath, String branch);

    ArrayList<Map<String, Object>> getWebhooks(String repositoryName);

    ArrayList<Map<String, Object>> getRepositories();

    ArrayList<Map<String, Object>> getIssueComments(String repositoryName, int issueKey);

    ArrayList<Map<String, Object>> getCommitComments(String repositoryName, String sha);

    ArrayList<Map<String, Object>> getTags(String repositoryName);

    Map<String, Object> getUser(String userId);

    Map<String, Object> getCompareResponse(String repositoryName, String branch, String sha);

    Map<String, Object> getMapResponse(String uri);

    ArrayList<Map<String, Object>> getListResponse(String uri);

    String deleteWebhook(String repositoryName, String forwardAddress);

    String createWebHook(String repositoryName, String forwardAddress);

    String setWebhookActive(String repositoryName, String forwardAddress, boolean active);

}
