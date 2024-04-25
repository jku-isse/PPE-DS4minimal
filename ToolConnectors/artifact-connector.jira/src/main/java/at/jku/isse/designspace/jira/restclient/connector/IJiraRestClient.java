package at.jku.isse.designspace.jira.restclient.connector;

import java.sql.Timestamp;

public interface IJiraRestClient {

	String getIssue(final String issueKey, String[] fields,  boolean withHistory);
	
	String fetchUpdatedIssuesSince(Timestamp startFrom, int startAt, int maxResults, String[] fields);
	
	String getIssues(int startAt, int maxResults, String[] fields, boolean withHistory);
	
	String getNamesAndScheme();
	
	String getStatus(String id);
	
	String getEveryStatus();
	
	String getUser(String key);
	
	String getUsers();
	
	String getProject(String id);
	
	String getProjects();
	
	String getIssueType(String id);
	
	String getIssueTypes();
	
	String getVersion(String id);
	
	String getVersions(String projectId);
	
	String getPriority(String id);
	
	String getPriorities();
	
	String getOption(String id);
	
	String getOptions();
	
	String getIssueLinkTypes();

	String getSkeletonForEveryIssue();

	String createWebhook(String calleeAddress, String name);

	String getWebhooks();

	void deleteResource(String url);
}
