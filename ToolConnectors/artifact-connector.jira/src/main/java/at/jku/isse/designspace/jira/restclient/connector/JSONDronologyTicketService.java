package at.jku.isse.designspace.jira.restclient.connector;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

public class JSONDronologyTicketService implements IJiraTicketService {

    private Map<String, Object> artifactMapKey;
    private Map<String, Object> artifactMapId;
    private Map<String, Object> versions;
    private Map<String, Object> projects;
    private Map<String, Object> status;
    private Map<String, Object> priorities;
    private Map<String, Object> users;
    private Map<String, Object> issueTypes;
    private Map<String, Object> issueLinkTypes;
    private Map<String, Object> names;
    private Map<String, Object> schema;

    public JSONDronologyTicketService() {
        init("");
    }

    public JSONDronologyTicketService(String jiraDataFolderPath) {
        init(jiraDataFolderPath);
    }

    private void init(String jiraDataFolderPath) {
        Map<String, Object> map;
        StringBuilder sb = new StringBuilder();
        String line;

        try {
            BufferedReader br = new BufferedReader(new FileReader(jiraDataFolderPath + "/Dronology_items.json"));
            while((line=br.readLine())!=null) {sb.append(line);}
            br.close();
            map = jsonToMap(sb.toString());
            ArrayList<Object> issues = (ArrayList<Object>) map.get("issues");
            issues.addAll((ArrayList<Object>) map.get("issues"));


            sb = new StringBuilder();
            br = new BufferedReader (new FileReader(jiraDataFolderPath + "/Dronology_items_1.json"));
            while((line=br.readLine())!=null) {sb.append(line);}
            br.close();
            map = jsonToMap(sb.toString());
            issues.addAll((ArrayList<Object>) map.get("issues"));

            sb = new StringBuilder();
            br = new BufferedReader (new FileReader(jiraDataFolderPath + "/namesAndSchema.json"));
            while((line=br.readLine())!=null) {sb.append(line);}
            br.close();
            map = jsonToMap(sb.toString());
            names = (Map<String, Object>) map.get("names");
            schema = (Map<String, Object>) map.get("schema");

            artifactMapKey = new HashMap<>();
            artifactMapId = new HashMap<>();
            versions = new HashMap<>();
            projects = new HashMap<>();
            issueLinkTypes = new HashMap<>();
            priorities = new HashMap<>();
            status = new HashMap<>();
            users = new HashMap<>();
            issueTypes = new HashMap<>();


            issues.forEach( issue -> {
                artifactMapKey.put((String) ((Map<String, Object>) issue).get("key"), issue);
                artifactMapId.put((String) ((Map<String, Object>) issue).get("id"), issue);
            });

            sb = new StringBuilder();
            issues.clear();
            br = new BufferedReader (new FileReader(jiraDataFolderPath + "/issueLinkTypes.json"));
            while((line=br.readLine())!=null) {sb.append(line);}
            br.close();
            map = jsonToMap(sb.toString());
            issues.addAll((ArrayList<Object>) map.get("data"));

            issues.forEach( project -> {
                issueLinkTypes.put((String) ((Map<String, Object>) project).get("id") ,project);
            });


            sb = new StringBuilder();
            issues.clear();
            br = new BufferedReader (new FileReader(jiraDataFolderPath + "/versions.json"));
            while((line=br.readLine())!=null) {sb.append(line);}
            br.close();
            map = jsonToMap(sb.toString());
            issues.addAll((ArrayList<Object>) map.get("data"));

            issues.forEach( version -> {
                versions.put((String) ((Map<String, Object>) version).get("id"), version);
            });


            sb = new StringBuilder();
            issues.clear();
            br = new BufferedReader (new FileReader(jiraDataFolderPath + "/projects.json"));
            while((line=br.readLine())!=null) {sb.append(line);}
            br.close();
            map = jsonToMap(sb.toString());
            issues.addAll((ArrayList<Object>) map.get("data"));

            issues.forEach( project -> {
                projects.put((String) ((Map<String, Object>) project).get("id") ,project);
            });


            sb = new StringBuilder();
            issues.clear();
            br = new BufferedReader (new FileReader(jiraDataFolderPath + "/issueTypes.json"));
            while((line=br.readLine())!=null) {sb.append(line);}
            br.close();
            map = jsonToMap(sb.toString());
            issues.addAll((ArrayList<Object>) map.get("data"));

            issues.forEach( project -> {
                issueTypes.put((String) ((Map<String, Object>) project).get("id") ,project);
            });


            sb = new StringBuilder();
            issues.clear();
            br = new BufferedReader (new FileReader(jiraDataFolderPath + "/priorities.json"));
            while((line=br.readLine())!=null) {sb.append(line);}
            br.close();
            map = jsonToMap(sb.toString());
            issues.addAll((ArrayList<Object>) map.get("data"));

            issues.forEach( project -> {
                priorities.put((String) ((Map<String, Object>) project).get("id") ,project);
            });


            sb = new StringBuilder();
            issues.clear();
            br = new BufferedReader (new FileReader(jiraDataFolderPath + "/users.json"));
            while((line=br.readLine())!=null) {sb.append(line);}
            br.close();
            map = jsonToMap(sb.toString());
            issues.addAll((ArrayList<Object>) map.get("data"));

            issues.forEach( user -> {
                users.put((String) ((Map<String, Object>) user).get("key"), user);
            });


            sb = new StringBuilder();
            issues.clear();
            br = new BufferedReader (new FileReader(jiraDataFolderPath + "/issueTypes.json"));
            while((line=br.readLine())!=null) {sb.append(line);}
            br.close();
            map = jsonToMap(sb.toString());
            issues.addAll((ArrayList<Object>) map.get("data"));

            issues.forEach( issueType -> {
                users.put((String) ((Map<String, Object>) issueType).get("id"), issueType);
            });


            issues.clear();
            br = new BufferedReader (new FileReader(jiraDataFolderPath + "/versions.json"));
            while((line=br.readLine())!=null) {sb.append(line);}
            br.close();
            map = jsonToMap(sb.toString());
            issues.addAll((ArrayList<Object>) map.get("data"));

            issues.forEach( version -> {
                versions.put((String) ((Map<String, Object>) version).get("id"), version);
            });


            sb = new StringBuilder();
            issues.clear();
            br = new BufferedReader (new FileReader(jiraDataFolderPath + "/status.json"));
            while((line=br.readLine())!=null) {sb.append(line);}
            br.close();
            map = jsonToMap(sb.toString());
            issues.addAll((ArrayList<Object>) map.get("data"));

            issues.forEach( status -> {
                this.status.put((String) ((Map<String, Object>) status).get("id"), status);
            });


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, Object> getArtifact(String id, boolean withHistory) {
        return (Map<String, Object>) artifactMapId.get(id);
    }

    @Override
    public ArrayList<Object> getAllArtifacts(boolean withHistory) {
        ArrayList<Object> issues = new ArrayList<>();
        issues.addAll(artifactMapId.values());
        return issues;
    }

    @Override
    public ArrayList<Object> getAllUpdatedArtifacts(Timestamp timestamp)  {
        ArrayList<Object> issues = new ArrayList<>();
        return issues;
    }

    @Override
    public Map<String, Object> getStatus(String statusId) {
        return (Map<String, Object>) status.get(statusId);
    }

    @Override
    public ArrayList<Object> getEveryStatus() {
        ArrayList<Object> status = new ArrayList<>();
        status.addAll(this.status.values());
        return status;
    }

    @Override
    public Map<String, Object> getIssueType(String issueTypeId)  {
        return (Map<String, Object>) issueTypes.get(issueTypeId);
    }

    @Override
    public ArrayList<Object> getIssueTypes(){
        ArrayList<Object> issueTypes = new ArrayList<>();
        issueTypes.addAll(this.issueTypes.values());
        return issueTypes;
    }

    @Override
    public Map<String, Object> getProject(String projectId) {
        return (Map<String, Object>) projects.get(projectId);
    }

    @Override
    public ArrayList<Object> getProjects()  {
        ArrayList<Object> projects = new ArrayList<>();
        projects.addAll(this.projects.values());
        return projects;
    }

    @Override
    public Map<String, Object> getUser(String userKey)  {
        return (Map<String, Object>) users.get(userKey);
    }

    @Override
    public ArrayList<Object> getUsers()  {
        ArrayList<Object> users = new ArrayList<>();
        users.addAll(this.users.values());
        return users;
    }

    @Override
    public Map<String, Object> getOption(String optionId)  {
        return null;
    }

    @Override
    public ArrayList<Object> getOptions() {
        return null;
    }

    @Override
    public Map<String, Object> getPriority(String priorityId) {
        return (Map<String, Object>) priorities.get(priorityId);
    }

    @Override
    public ArrayList<Object> getPriorities() {
        ArrayList<Object> priorities = new ArrayList<>();
        priorities.addAll(this.priorities.values());
        return priorities;
    }

    @Override
    public Map<String, Object> getVersion(String versionId) {
        return (Map<String, Object>) versions.get(versionId);
    }

    @Override
    public ArrayList<Object> getVersions() {
        ArrayList<Object> versions = new ArrayList<>();
        versions.addAll(this.versions.values());
        return versions;
    }

    @Override
    public Map<String, Object> getLinkTypes() {

        Map<String, Object> result = new HashMap<>();

        issueLinkTypes.values().forEach(linkType -> {
            result.put((String) ((Map<String, Object>) linkType).get("id"), linkType);
        });

        return result;
    }

    @Override
    public Map<String, Object> getResolution(String id) {
        return null;
    }

    @Override
    public ArrayList<Object> getResolutions() {
        return null;
    }

    @Override
    public Map<String, Object> getNames() {
        return names;
    }

    @Override
    public Map<String, Object> getSchema() {
        return schema;
    }

    @Override
    public String getArtifactIdFromKeyServer(String key) {
        Object mapObject = artifactMapKey.get(key);
        if (mapObject != null) {
            return (String) ((Map<String, Object>) mapObject).get("id");
        }
        return null;
    }

    @Override
    public String getArtifactIdFromKeyCache(String key) {
        Object mapObject = artifactMapKey.get(key);
        if (mapObject != null) {
            return (String) ((Map<String, Object>) mapObject).get("id");
        }
        return null;
    }

    @Override
    public Map<String, Object> createWebhook(String calleeAddress) {
        return null;
    }

    @Override
    public List<Object> getSkeletonForEveryArtifact() {
        return getAllArtifacts(true);
    }

    private static Map<String, Object> jsonToMap(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, new TypeReference<Map<String, Object>>(){});
        } catch (IOException e) {
        }
        return null;
    }

	@Override
	public Collection<Map<String, Object>> getArtifactsFromJQL(String jql) {
		return Collections.emptyList();
	}

}
