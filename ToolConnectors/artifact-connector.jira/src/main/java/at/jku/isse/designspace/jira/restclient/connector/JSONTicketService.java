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

public class JSONTicketService implements IJiraTicketService {


    private final String FOLDER = "jira_dump/";

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


    /**
     *
     * This class will look for a folder called jira_dump in the running directory,
     * files are expected to be existent in this folder.
     *
     * @param issueDumpFileNames
     * @param issueLinkTypesFileName
     * @param namesAndSchemaFileName
     */
    public JSONTicketService(String[] issueDumpFileNames, String issueLinkTypesFileName, String namesAndSchemaFileName,
                             String versionsFileName, String issueTypesFileName, String prioritiesFileName,
                             String projectsFileName, String statusFileName, String usersFileName) {

        Map<String, Object> map;
        String line;

        ArrayList<Map<String, Object>> issues = new ArrayList<>();
        this.artifactMapKey = new HashMap<>();
        this.artifactMapId = new HashMap<>();
        this.versions = new HashMap<>();
        this.projects = new HashMap<>();
        this.issueLinkTypes = new HashMap<>();
        this.priorities = new HashMap<>();
        this.names = new HashMap<>();
        this.schema = new HashMap<>();
        this.status = new HashMap<>();
        this.users = new HashMap<>();
        this.issueTypes = new HashMap<>();

        if (issueDumpFileNames != null) {
            try {

                int i = 0;
                for (String issueDumpFileName : issueDumpFileNames) {
                    StringBuilder sb = new StringBuilder();
                    BufferedReader br = new BufferedReader(new FileReader(FOLDER + issueDumpFileName));
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    br.close();
                    map = jsonToMap(sb.toString());
                    issues.addAll((ArrayList<Map<String, Object>>) map.get("issues"));
                }

                issues.forEach(issue -> {
                    this.artifactMapKey.put((String) (issue).get("key"), issue);
                    this.artifactMapId.put((String) (issue).get("id"), issue);
                });

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(projectsFileName != null) {
            try {
                StringBuilder sb = new StringBuilder();
                issues.clear();
                BufferedReader br = new BufferedReader(new FileReader(FOLDER + projectsFileName));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                map = jsonToMap(sb.toString());
                issues.addAll((ArrayList<Map<String, Object>>) map.get("data"));

                issues.forEach(project -> {
                    this.projects.put((String) (project).get("id"), project);
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (issueLinkTypesFileName != null) {
            try {
                StringBuilder sb = new StringBuilder();
                issues.clear();
                BufferedReader br = new BufferedReader(new FileReader(FOLDER + issueLinkTypesFileName));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                map = jsonToMap(sb.toString());
                issues.addAll((ArrayList<Map<String, Object>>) map.get("data"));

                issues.forEach(project -> {
                    this.issueLinkTypes.put((String) (project).get("id"), project);
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(issueLinkTypesFileName != null) {
            try {
                StringBuilder sb = new StringBuilder();
                issues.clear();
                BufferedReader br = new BufferedReader(new FileReader(FOLDER + issueTypesFileName));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                map = jsonToMap(sb.toString());
                issues.addAll((ArrayList<Map<String, Object>>) map.get("data"));

                issues.forEach(project -> {
                    this.issueTypes.put((String) (project).get("id"), project);
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (prioritiesFileName != null) {
            try {
                StringBuilder sb = new StringBuilder();
                issues.clear();
                BufferedReader br = new BufferedReader(new FileReader(FOLDER + prioritiesFileName));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                map = jsonToMap(sb.toString());
                issues.addAll((ArrayList<Map<String, Object>>) map.get("data"));

                issues.forEach(project -> {
                    this.priorities.put((String) (project).get("id"), project);
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (usersFileName != null) {
            try {
                StringBuilder sb = new StringBuilder();
                issues.clear();
                BufferedReader br = new BufferedReader(new FileReader(FOLDER + usersFileName));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                map = jsonToMap(sb.toString());
                issues.addAll((ArrayList<Map<String, Object>>) map.get("data"));

                issues.forEach(user -> {
                    this.users.put((String) (user).get("key"), user);
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (versionsFileName != null) {
            try {
                StringBuilder sb = new StringBuilder();
                issues.clear();
                BufferedReader br = new BufferedReader(new FileReader(FOLDER + versionsFileName));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                map = jsonToMap(sb.toString());
                issues.addAll((ArrayList<Map<String, Object>>) map.get("data"));

                issues.forEach(version -> {
                    this.versions.put((String) version.get("id"), version);
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (statusFileName != null) {
            try {
                StringBuilder sb = new StringBuilder();
                issues.clear();
                BufferedReader br = new BufferedReader(new FileReader(FOLDER + statusFileName));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                map = jsonToMap(sb.toString());
                issues.addAll((ArrayList<Map<String, Object>>) map.get("data"));

                issues.forEach(status -> {
                    this.status.put((String) status.get("id"), status);
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (namesAndSchemaFileName != null) {
            try {
                StringBuilder sb = new StringBuilder();
                BufferedReader br = new BufferedReader(new FileReader(FOLDER + namesAndSchemaFileName));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                map = jsonToMap(sb.toString());
                this.names = (Map<String, Object>) map.get("names");
                this.schema = (Map<String, Object>) map.get("schema");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Map<String, Object> getArtifact(String id, boolean withChangeLog) {
        return (Map<String, Object>) artifactMapId.get(id);
    }

    @Override
    public ArrayList<Object> getAllArtifacts(boolean withChangeLog) {
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
        Object artifactMap = artifactMapKey.get(key);
        if (artifactMap != null) {
            return (String) ((HashMap<String, Object>) artifactMap).get("id");
        }
        return null;
    }

    @Override
    public String getArtifactIdFromKeyCache(String key) {
        Object artifactMap = artifactMapKey.get(key);
        if (artifactMap != null) {
            return (String) ((HashMap<String, Object>) artifactMap).get("id");
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
