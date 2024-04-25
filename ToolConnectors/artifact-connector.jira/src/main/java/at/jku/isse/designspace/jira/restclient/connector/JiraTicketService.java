package at.jku.isse.designspace.jira.restclient.connector;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class JiraTicketService implements IJiraTicketService {

	private static final int ISSUES_PER_ACCESS=1000;
	private final static String WEBHOOK_NAME = "artifactrelations";

	private JiraRestClient rawExtension;
	private String[] fields;
	private Map<String, String> key2id;

	public JiraTicketService(AbstractModule jiraTicketServiceConfig) {
		Injector injector = Guice.createInjector(jiraTicketServiceConfig);
		this.rawExtension = injector.getInstance(JiraRestClient.class);
		this.fields = injector.getInstance(String[].class);
		this.key2id = new HashMap<>();

		if (fields.length != 0) {
			boolean id = false, key = false;
			for (String field : fields) {
				if (field.equals("id")) {
					id = true;
				}
				if (field.equals("key")) {
					key = true;
				}
			}

			if (!id) {
				String[] newFields = new String[fields.length + 1];
				for (int i = 0; i < fields.length; i++) {
					newFields[i] = fields[i];
				}
				newFields[newFields.length - 1] = "id";
				fields = newFields;
			}

			if (!key) {
				String[] newFields = new String[fields.length + 1];
				for (int i = 0; i < fields.length; i++) {
					newFields[i] = fields[i];
				}
				newFields[newFields.length - 1] = "key";
				fields = newFields;
			}
		}
	}
	
	@Override
	public Collection<Map<String, Object>> getArtifactsFromJQL(String jql) {
		String json = rawExtension.getJQLResult(jql);
		Map<String, Object> result =  jsonToMap(json);
		if (result.containsKey("issues")) {
			Collection<Map<String,Object>> issues = (Collection<Map<String, Object>>) result.get("issues");
			issues.stream().forEach(issue -> key2id.put((String) issue.get("key"), (String) issue.get("id")));			
			return issues;
		}
		else return Collections.emptyList();
	}
	
	@Override
	public Map<String, Object> getArtifact(String key, boolean withHistory) {
		String json = rawExtension.getIssue(key, fields, withHistory);
		Map<String, Object> issue =  jsonToMap(json);
		key2id.put((String) issue.get("key"), (String) issue.get("id"));
		return issue;
	}


	@Override
	public List<Object> getSkeletonForEveryArtifact() {
		String json = rawExtension.getSkeletonForEveryIssue();
		Map<String, Object> jsonMap = jsonToMap(json);
		List<Object> issues = (List<Object>) jsonMap.get("issues");
		for (Object o : issues) {
			Map<String, Object> issue = (Map<String, Object>) o;
			key2id.put((String) issue.get("key"), (String) issue.get("id"));
		}
		return issues;
	}

	private static Map<String, Object> jsonToMap(String json) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(json, new TypeReference<Map<String, Object>>() {
			});
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return null;
	}
	
	private static List<Object> jsonToArrayList(String json) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(json, new TypeReference<List<Object>>(){});
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object> getAllArtifacts(boolean withHistory) {

		String json = rawExtension.getIssues(0, ISSUES_PER_ACCESS, fields, withHistory);
		Map<String, Object> jsonMap = jsonToMap(json);
		List<Object> issues = (List<Object>) jsonMap.get("issues");
		
		int totalIssues = (Integer) jsonMap.get("total");
		int fetchedIssues = ISSUES_PER_ACCESS;
		
		while(fetchedIssues<totalIssues) {
			json = rawExtension.getIssues(fetchedIssues, ISSUES_PER_ACCESS, fields, withHistory);
			jsonMap = jsonToMap(json);
			issues.addAll((List<Object>) jsonMap.get("issues"));
			fetchedIssues = fetchedIssues + ISSUES_PER_ACCESS;
		}

		for (Object o : issues) {
			Map<String, Object> issue = (Map<String, Object>) o;
			key2id.put((String) issue.get("key"), (String) issue.get("id"));
		}

		return issues;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getNames() {
		String json = rawExtension.getNamesAndScheme();
		Map<String, Object> names = (Map<String, Object>) jsonToMap(json).get("names");
		
		//TO-DO: look for a more generic solution like String.contains()
		//because Jira only return the value Fix Version's
		//which is then never used instead of the two below
		names.put("fixVersions", "Fix Version");
		
		return names;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getSchema() {
		String json = rawExtension.getNamesAndScheme();
		Map<String, Object> schema = (Map<String, Object>) jsonToMap(json).get("schema");
		return schema;
	}

	@Override
	public String getArtifactIdFromKeyServer(String key) {
		String[] fields = {"id"};
		String json = rawExtension.getIssue(key, fields, false);
		Map<String, Object> issue =  jsonToMap(json);
		return (String) issue.get("id");
	}

	@Override
	public String getArtifactIdFromKeyCache(String key) {
		return key2id.get(key);
	}

	@Override
	public Map<String, Object> createWebhook(String calleeAddress) {
		List<Object> hooks = getWebhooks();

		if (!hooks.isEmpty()) {
			for (Object hook_ : hooks) {
				Map<String, Object>	hook = (Map<String, Object>) hook_;
				if (hook.containsKey("self") && hook.containsKey("name")
						&& WEBHOOK_NAME.equals(hook.get("name"))) {

					Object url = hook.get("self");
					if (url != null) {
						rawExtension.deleteResource(url.toString());
						break;
					}
				}
			}
		}

		rawExtension.createWebhook(calleeAddress, WEBHOOK_NAME);
		return null;
	}

	private List<Object> getWebhooks() {
		List<Object> webhookList = jsonToArrayList(rawExtension.getWebhooks());
		return webhookList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getLinkTypes() {
		String json = rawExtension.getIssueLinkTypes();
		List<Object> linkTypes = (List<Object>) jsonToMap(json).get("issueLinkTypes");
		Map<String, Object> result = new HashMap<String, Object>();

		linkTypes.forEach(linkType -> {
			result.put((String) ((Map<String, Object>) linkType).get("id"), linkType);
		});
		
		return result;
	}

	@Override
	public Map<String, Object> getResolution(String id) {
		return null;
	}

	@Override
	public List<Object> getResolutions() {
		return null;
	}

	@Override
	public Map<String, Object> getStatus(String statusId) {
		return jsonToMap(rawExtension.getStatus(statusId));
	}

	@Override
	public Map<String, Object> getIssueType(String issueTypeId) {
		return jsonToMap(rawExtension.getIssueType(issueTypeId));
	}

	@Override
	public Map<String, Object> getProject(String projectId) {
		return jsonToMap(rawExtension.getProject(projectId));
	}

	@Override
	public Map<String, Object> getUser(String userKey) {
		return jsonToMap(rawExtension.getUser(userKey));
	}

	@Override
	public Map<String, Object> getPriority(String priorityId) {
		return jsonToMap(rawExtension.getPriority(priorityId));
	}

	@Override
	public Map<String, Object> getVersion(String versionId) {
		return jsonToMap(rawExtension.getVersion(versionId));
	}

	@Override
	public Map<String, Object> getOption(String optionId) {
		return jsonToMap(rawExtension.getOption(optionId));
	}
	
	@Override
	public List<Object> getEveryStatus() {
		List<Object> statusList = jsonToArrayList(rawExtension.getEveryStatus());
		return statusList;
	}

	@Override
	public List<Object> getIssueTypes() {
		List<Object> issueTypes = jsonToArrayList(rawExtension.getIssueTypes());
		return issueTypes;
	}

	@Override
	public List<Object> getProjects() {
		List<Object> projects = jsonToArrayList(rawExtension.getProjects());
		return projects;
	}

	@Override
	public List<Object> getUsers()  {
		String str = rawExtension.getUsers();
		System.out.println(str);
		List<Object> users = jsonToArrayList(str);
		return users;	
	}

	@Override
	public List<Object> getPriorities() {
		List<Object> priorities = jsonToArrayList(rawExtension.getPriorities());
		return priorities;
	}

	@Override
	public List<Object> getOptions() {
		return jsonToArrayList(rawExtension.getOptions());
	}

	@Override
	public List<Object> getVersions() {
		
		List<Object> versions = new ArrayList<Object>();
		Map<String, Object> pm;
		
		for(Object p : getProjects()) {
			pm = (Map<String, Object>) p;
			versions.addAll(jsonToArrayList(rawExtension.getVersions((String) pm.get("id"))));
		}
		
		return versions;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Object> getAllUpdatedArtifacts(Timestamp timestamp) {
		
		String json = rawExtension.fetchUpdatedIssuesSince(timestamp, 0, ISSUES_PER_ACCESS, fields);
		Map<String, Object> jsonMap = jsonToMap(json);
		List<Object> issues = (List<Object>) jsonMap.getOrDefault("issues", Collections.EMPTY_LIST);
		
		int totalIssues = (Integer) jsonMap.getOrDefault("total", 0);
		int fetchedIssues = ISSUES_PER_ACCESS;
		
		while(fetchedIssues<totalIssues) {
			json = rawExtension.getIssues(fetchedIssues, ISSUES_PER_ACCESS, fields, true);
			jsonMap = jsonToMap(json);
			issues.addAll((List<Object>) jsonMap.get("issues"));
			fetchedIssues = fetchedIssues + ISSUES_PER_ACCESS;
		}

		for (Object o : issues) {
			Map<String, Object> issue = (Map<String, Object>) o;
			key2id.put((String) issue.get("key"), (String) issue.get("id"));
		}

		return issues;
	}

	
}
