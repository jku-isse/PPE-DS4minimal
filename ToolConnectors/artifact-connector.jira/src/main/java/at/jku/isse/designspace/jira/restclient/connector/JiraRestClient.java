package at.jku.isse.designspace.jira.restclient.connector;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.function.Function;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import com.atlassian.jira.rest.client.api.IssueRestClient.Expandos;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JiraRestClient implements IJiraRestClient {

    private static final EnumSet<Expandos> DEFAULT_EXPANDS = EnumSet.of(Expandos.NAMES, Expandos.CHANGELOG, Expandos.SCHEMA, Expandos.TRANSITIONS);
    private static final Function<Expandos, String> EXPANDO_TO_PARAM = from -> from.name().toLowerCase();

    private CloseableHttpClient httpClient;
    private Header header;
	private URI baseUri;

	public JiraRestClient(URI uri, String username, String password) {
		this.baseUri = uri;
		this.httpClient = HttpClients.custom().build();

		byte[] credentials = (username + ':' + password).getBytes();
		String authentication = new String(Base64.encodeBase64(credentials));
		this.header = new BasicHeader("Authorization", "Basic " + authentication);
	}
	
	public String getIssue(String issueKey, String[] fields, boolean withHistory) {
		StringBuilder fields_sb = new StringBuilder();
		fields_sb.append("fields=");
		if(fields!=null) {
			for (int i = 0; i < fields.length; i++) {
				fields_sb.append(fields[i]);
				if (i + 1 != fields.length) {
					fields_sb.append(",%20");
				}
			}
		}

		URI uri;
		if (withHistory) {
			uri = URI.create(baseUri + "/rest/api/2/issue/" + issueKey + "?expand=changelog&" + fields_sb);
		} else {
			uri = URI.create(baseUri + "/rest/api/2/issue/" + issueKey + "?" + fields_sb);
		}

		return getAndParse(uri);
	}
	
	public String fetchUpdatedIssuesSince(Timestamp startFrom, int startAt, int maxResults, String[] fields) {

		LocalDateTime serverDate = startFrom.toLocalDateTime();
		String date = serverDate.getYear() + "-" + serverDate.getMonthValue() + "-" + serverDate.getDayOfMonth();
		String time  = serverDate.getHour() + ":" + serverDate.getMinute();

		StringBuilder fields_sb = new StringBuilder();
		fields_sb.append("fields=");

		if(fields!=null) {
			for (int i = 0; i < fields.length; i++) {
				fields_sb.append(fields[i]);
				if (i + 1 != fields.length) fields_sb.append(",%20");
			}
		}

		URI uri = URI.create(baseUri + "/rest/api/2/search?jql=updated%3E%27" + date + "%20" + time + "%27&expand=changelog&startAt=" + startAt + "&maxResults=" + maxResults + "&" + fields);
		return getAndParse(uri);
	}
	
	public String getIssues(int startAt, int maxResults, String[] fields, boolean withHistory) {

		StringBuilder fields_sb = new StringBuilder();
		fields_sb.append("fields=");
		if(fields!=null) {
			for (int i = 0; i < fields.length; i++) {
				fields_sb.append(fields[i]);
				if (i + 1 != fields.length) fields_sb.append(",");
			}
		}

		URI uri;
		if (withHistory) {
			uri = URI.create(baseUri + "/rest/api/2/search?" + "expand=changelog&" + "startAt=" + startAt + "&maxResults=" + maxResults + "&" + fields_sb);
		} else {
			uri = URI.create(baseUri + "/rest/api/2/search?startAt=" + startAt + "&maxResults=" + maxResults + "&" + fields_sb);
		}

		return getAndParse(uri);
	}
	
	public String getJQLResult(String jql) {		
		URI uri = URI.create(baseUri + "/rest/api/latest/search?jql="+URLEncoder.encode(jql, StandardCharsets.UTF_8));
		return getAndParse(uri);
	}
	
	public String getNamesAndScheme() {		
		URI uri = URI.create(baseUri + "/rest/api/2/search?expand=names,schema&startAt=0&maxResults=1");
		return getAndParse(uri);
	}
	
	public String getStatus(String id) {
		URI uri = URI.create(baseUri + "/rest/api/2/status/" + id);       
		return executeAndLogExceptions(uri, id);
	}
	
	public String getUser(String key) {
		URI uri = URI.create(baseUri + "/rest/api/2/user?key=" + key);       
		return executeAndLogExceptions(uri, key);
	}
	
	public String getProject(String id) {
		URI uri = URI.create(baseUri + "/rest/api/2/project/" + id);       
		return executeAndLogExceptions(uri, id);
	}
	
	public String getIssueType(String id) {	
		URI uri = URI.create(baseUri + "/rest/api/2/issuetype/" + id); 		
		return executeAndLogExceptions(uri, id);
	}
	
	public String getVersion(String id) {
		URI uri = URI.create(baseUri + "/rest/api/2/version/" + id);
		return getAndParse(uri);
	}
	
	public String getPriority(String id) {
		URI uri = URI.create(baseUri + "/rest/api/2/priority/" + id);
		return getAndParse(uri);
	}
	
	public String getOption(String id) {
		URI uri = URI.create(baseUri + "/rest/api/2/customFieldOption/" + id);
		return getAndParse(uri);
	}
	
	public String getIssueLinkTypes() {
		URI uri = URI.create(baseUri + "/rest/api/2/issueLinkType");
		return getAndParse(uri);
	}

	@Override
	public String getSkeletonForEveryIssue() {
		URI uri = URI.create(baseUri + "/rest/api/2/search?fields=id,%20key");
		return getAndParse(uri);
	}

	@Override
	public String createWebhook(String calleeAddress, String name) {
		String body =
		"{\n" +
			"\"name\": \"" + name + "\",\n" +
			"\"url\": \"" + calleeAddress + "\",\n" +
			"\"events\":[\"jira:issue_created\", \"jira:issue_updated\", \"jira:issue_deleted\", \"issuelink_created\", \"issuelink_deleted\", \"worklog_created\", \"worklog_updated\", \"worklog_deleted\", \"comment_created\", \"comment_updated\", \"comment_deleted\"],\n" +
			"\"excludeIssueDetails\" : false \n" +
		"}";
		URI uri = URI.create(baseUri + "/rest/webhooks/1.0/webhook");
		post(uri, body);
		return "";
	}

	@Override
	public String getWebhooks() {
		URI uri = URI.create(baseUri + "/rest/webhooks/1.0/webhook");
		return getAndParse(uri);
	}

	@Override
	public void deleteResource(String url) {
		URI uri = URI.create(url);
		delete(uri);
	}

	protected final String getAndParse(final URI uri) {
		try {
			HttpUriRequest request = RequestBuilder.get().setUri(uri).setHeader(header).build();
			try(CloseableHttpResponse response = httpClient.execute(request)) {
				String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
				EntityUtils.consume(response.getEntity());
				return responseString;
			}
		} catch (IOException e) {
			log.warn(e.getMessage());
			return "{\"error_msg\":\"The response from the server could not be claimed\"}";
		}
	}

	private String post(URI uri, String body) {
		try {
			HttpUriRequest request = RequestBuilder.post()
					.setUri(uri)
					.addHeader("Content-Type", "application/json")
					.setEntity(new StringEntity(body))
					.setHeader(header)
					.build();
			try(CloseableHttpResponse response = httpClient.execute(request)) {
				String responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
				EntityUtils.consume(response.getEntity());
				return responseString;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return "error";
		}
	}

	private String delete(URI uri) {
		try {
			HttpUriRequest request = RequestBuilder.delete()
					.setUri(uri)
					.setHeader(header)
					.build();
			try(CloseableHttpResponse response = httpClient.execute(request)) {
				String responseString = "hook was deleted";
				if (response.getEntity() != null) {
					responseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
				}
				EntityUtils.consume(response.getEntity());
				return responseString;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return "error";
		}
	}

	private String executeAndLogExceptions(URI uri, String id) {
		return getAndParse(uri);
	}

	@Override
	public String getEveryStatus() {
		URI uri = URI.create(baseUri + "/rest/api/2/status");
		return getAndParse(uri);
	}

	@Override
	public String getProjects() {
		URI uri = URI.create(baseUri + "/rest/api/2/project");
		return getAndParse(uri);
	}

	@Override
	public String getIssueTypes() {
		URI uri = URI.create(baseUri + "/rest/api/2/issuetype");
		return getAndParse(uri);
	}

	@Override
	public String getVersions(String projectId) {
		URI uri = URI.create(baseUri + "/rest/api/2/project/" + projectId + "/versions");
		return getAndParse(uri);
	}

	@Override
	public String getPriorities() {
		URI uri = URI.create(baseUri + "/rest/api/2/priority");
		return getAndParse(uri);
	}

	@Override
	public String getOptions() {
		// TODO Auto-generated method stub
		return "Not implemented";
	}

	@Override
	public String getUsers() {
		URI uri = URI.create(baseUri + "/rest/api/2/users");
		return getAndParse(uri);
	}

}
