package at.jku.isse.designspace.jira.restclient.connector;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IJiraTicketService {

	/**
	 * Queries the server for an issue with the given id,
	 * and the returns the json-data as map.
	 * if not item with the given id was found null is returned.
	 * 
	 * @param id
	 * @return Map<String, Object>
	 * @throws IOException
	 */
	Map<String, Object> getArtifact(String id, boolean withHistory) ;
	
	/**
	 * Returns a list containing maps for all issues
	 * currently stored in the server.
	 * s
	 * @return ArrayList<Object>
	 * @throws IOException
	 */
	List<Object> getAllArtifacts(boolean withHistory) ;
	
	/**
	 * Returns only those artifacts, which were updated after the given timestamp.
	 * 
	 * @param timestamp
	 * @return ArrayList<Object>
	 * @throws IOException
	 */
	List<Object> getAllUpdatedArtifacts(Timestamp timestamp) ;
	
	/**
	 * Returns the JSON-Data of the statusType with the given id.
	 * 
	 * @param statusId
	 * @return Map<String, Object>
	 * @throws IOException
	 */
	Map<String, Object> getStatus(String statusId) ;
	
	/**
	 * Returns the JSON-Data of every status.
	 * 
	 * @return ArrayList
	 * @throws IOException
	 */
	List<Object> getEveryStatus();
	
	
	/**
	 * Returns the JSON-Data of the issueType with the given id.
	 * 
	 * @param issueTypeId
	 * @return Map<String, Object>
	 * @throws IOException
	 */
	Map<String, Object> getIssueType(String issueTypeId);
	
	/**
	 * Returns the JSON-Data of all issueTypes.
	 * 
	 * @return ArrayList
	 * @throws IOException
	 */
	List<Object> getIssueTypes();
	
	
	/**
	 * Returns the JSON-Data of the projectType with the given id.
	 * 
	 * @param projectId
	 * @return Map<String, Object>
	 * @throws IOException
	 */
	Map<String, Object> getProject(String projectId);
	
	/**
	 * Returns the JSON-Data of all projects.
	 * 
	 * @return ArrayList
	 * @throws IOException
	 */
	List<Object> getProjects();
	
	
	/**
	 * Returns the JSON-Data of the userType with the given id.
	 * 
	 * @param userKey
	 * @return Map<String, Object>
	 * @throws IOException
	 */
	Map<String, Object> getUser(String userKey);
			
	/**
	 * Returns the JSON-Data of the all users.
	 * 
	 * @return ArrayList
	 * @throws IOException
	 */
	List<Object> getUsers();
			
	
	/**
	 * Returns the JSON-Data of the optionType with the given id.
	 * 
	 * @param optionId
	 * @return Map<String, Object>
	 * @throws IOException
	 */
	Map<String, Object> getOption(String optionId);
	
	/**
	 * returns the JSON-Data the all options.
	 * 
	 * @return String
	 * @throws IOException
	 */
	List<Object> getOptions();
	
	/**
	 * Returns the JSON-Data of the priorityType with the given id.
	 * 
	 * @param priorityId
	 * @return Map<String, Object>
	 * @throws IOException
	 */
	Map<String, Object> getPriority(String priorityId);
	
	/**
	 * Returns the JSON-Data of all priorities.
	 * 
	 * @return ArrayList
	 * @throws IOException
	 */
	List<Object> getPriorities();
	
	/**
	 * Returns the JSON-Data of the versionType with the given id.
	 * 
	 * @param versionId
	 * @return Map<String, Object>
	 * @throws IOException
	 */
	Map<String, Object> getVersion(String versionId);
	
	/**
	 * Returns the JSON-Data of all versions.
	 * 
	 * @return ArrayList
	 * @throws IOException
	 */
	List<Object> getVersions();
	
	
	/**
	 * Returns the JSON-Data of the linkType with the given id.
	 * 
	 * @return Map<String, Object>
	 * @throws IOException
	 */
	Map<String, Object> getLinkTypes();


	/**
	 * Returns the resolution with the given id.
	 *
	 * @param id
	 * @return
	 */
	Map<String, Object> getResolution(String id);


	/**
	 * Returns a list with all resolutions.
	 *
	 * @return
	 * @throws IOException
	 */
	List<Object> getResolutions();


	/**
	 * Returns the namesSchema for the whole server structures,
	 * which contains a map for mapping from fieldName to fieldId.
	 * 
	 * @return Map<String, Object>
	 * @throws IOException
	 */
	Map<String, Object> getNames();
	
	/**
	 * returns the schema for the whole server structures,
	 * which contains a map for mapping from fieldId to type.
	 * 
	 * @return Map<String, Object>
	 * @throws IOException
	 */
	Map<String, Object> getSchema();
	
	/**
	 * Fetches the corresponding id for the given key.
	 * This method is slow it should not be used for
	 * getting the the key on a large scale.
	 * 
	 * @param key
	 * @return String
	 * @throws IOException
	 */
	String getArtifactIdFromKeyServer(String key);

	/**
	 * Tries to find the key in the
	 * runtime cache, the corresponding issue has
	 * not been fetched yet, the search won't be successful.
	 *
	 * @param key
	 * @return String
	 * @throws IOException
	 */
	String getArtifactIdFromKeyCache(String key);

	/**
	 * Creates a webhook, which sends updates to the
	 * address provided.
	 *
	 * @param calleeAddress
	 * @return
	 */
	Map<String, Object> createWebhook(String calleeAddress);

	/**
	 * Fetches the id and the key for every artifact.
	 *
	 * @return
	 */
	List<Object> getSkeletonForEveryArtifact();

	Collection<Map<String, Object>> getArtifactsFromJQL(String jql);

}
