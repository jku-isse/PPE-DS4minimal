package at.jku.isse.designspace.jama.connector.restclient.jamaclient;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;

import at.jku.isse.designspace.artifactconnector.core.model.BaseElementType;
import at.jku.isse.designspace.artifactconnector.core.monitoring.IProgressObserver;
import at.jku.isse.designspace.artifactconnector.core.monitoring.ProgressEntry;
import at.jku.isse.designspace.jama.utility.AccessToolsJSON;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JamaAPIMock extends SchemaProvider implements IJamaAPI {

    private Map<String, Object> keyToJamaItem;
    private Map<Integer, Object> idToJamaItem;
    //private Map<Integer, Object> idToItemType;
    //private Map<Integer, Object> idToPickList;
    //private Map<Integer, Object> idToPickListOption;
    private Map<Integer, Object> idToProject;
    private Map<Integer, Object> idToRelease;
    private Map<Integer, Object> idToUser;
    private Map<String, Object> nameToUser;
    private Map<String, Object> nameToProject;

    private final String ITEM_TYPE_FILE_PATH = "./data/itemTypes.json";
    private final String PICKLIST_FILE_PATH = "./data/pickLists.json";
    private final String PICKLIST_OPTION_FILE_PATH = "./data/pickListOptions.json";
    private final String ITEMS_FILE_1_PATH = "./data/jamaItems_1.json";
    private final String ITEMS_FILE_2_PATH = "./data/jamaItems_2.json";
    private final String PROJECTS_FILE_PATH = "./data/projects.json";
    private final String RELEASES_FILE_PATH = "./data/releases.json";
    private final String USERS_FILE_PATH = "./data/users.json";

    public JamaAPIMock( IProgressObserver obs) {

    	super(null,  obs);
    	
        this.keyToJamaItem = new HashMap<>();
        //this.idToItemType = new HashMap<>();
        this.idToJamaItem = new HashMap<>();
        //this.idToPickList = new HashMap<>();
        //this.idToPickListOption = new HashMap<>();
        this.idToProject = new HashMap<>();
        this.idToRelease = new HashMap<>();
        this.idToUser = new HashMap<>();
        this.nameToUser = new HashMap<>();
        this.nameToProject = new HashMap<>();

        try {
            Properties props = new Properties();        

            File jamaItemsFile = new File(ITEMS_FILE_1_PATH);
            Map<String, Object> jamaItems1Map = new ObjectMapper().readValue(jamaItemsFile, Map.class);
            ArrayList<Object> jamaItems1 = (ArrayList<Object>) jamaItems1Map.get("rows");

            for (Object jamaItem : jamaItems1) {
                Map<String, Object> curJamaItem = (Map<String, Object>) jamaItem;
                Optional<Integer> itemId = getIdFromDoc(curJamaItem);
                Optional<String> itemKey = getKeyFromDoc(curJamaItem);

                if (itemId.isPresent() && itemKey.isPresent()) {
                    Map<String, Object> curJamaItemDoc = (Map<String, Object>) curJamaItem.get("doc");
                    this.idToJamaItem.put(itemId.get(), curJamaItemDoc);
                    this.keyToJamaItem.put(itemKey.get(), curJamaItemDoc);
                }
            }

            jamaItemsFile = new File(ITEMS_FILE_2_PATH);
            Map<String, Object> jamaItems2Map = new ObjectMapper().readValue(jamaItemsFile, Map.class);
            ArrayList<Object> jamaItems2 = (ArrayList<Object>) jamaItems2Map.get("rows");

            for (Object jamaItem : jamaItems2) {
                Map<String, Object> curJamaItem = (Map<String, Object>) jamaItem;
                Optional<Integer> itemId = getIdFromDoc(curJamaItem);
                Optional<String> itemKey = getKeyFromDoc(curJamaItem);

                if (itemId.isPresent() && itemKey.isPresent()) {
                    Map<String, Object> curJamaItemDoc = (Map<String, Object>) curJamaItem.get("doc");
                    this.idToJamaItem.put(itemId.get(), curJamaItemDoc);
                    this.keyToJamaItem.put(itemKey.get(), curJamaItemDoc);
                }
            }

            File releasesFile = new File(RELEASES_FILE_PATH);
            Map<String, Object> releasesMap = new ObjectMapper().readValue(releasesFile, Map.class);
            ArrayList<Object> releases = (ArrayList<Object>) releasesMap.get("releases");

            for (Object release : releases) {
                Map<String, Object> curRelease = (Map<String, Object>) release;
                Optional<Integer> releaseId = getIdFromDoc(curRelease);
                if (releaseId.isPresent()) {
                    Map<String, Object> releasesDoc = (Map<String, Object>) curRelease.get("doc");
                    this.idToRelease.put(releaseId.get(), releasesDoc.get("data"));
                }
            }

            File projectsFile = new File(PROJECTS_FILE_PATH);
            Map<String, Object> projectsMap = new ObjectMapper().readValue(projectsFile, Map.class);
            ArrayList<Object> projects = (ArrayList<Object>) projectsMap.get("projects");

            for (Object project : projects) {
                Map<String, Object> curProject = (Map<String, Object>) project;
                Optional<Integer> projectId = getIdFromDoc(curProject);
                if (projectId.isPresent()) {
                    Map<String, Object> projectDoc = (Map<String, Object>) curProject.get("doc");
                    this.idToProject.put(projectId.get(), projectDoc.get("data"));
                }
            }

            File usersFile = new File(USERS_FILE_PATH);
            Map<String, Object> usersMap = new ObjectMapper().readValue(usersFile, Map.class);
            ArrayList<Object> users = (ArrayList<Object>) usersMap.get("users");

            for (Object user : users) {
                Map<String, Object> curUser = (Map<String, Object>) user;
                Optional<Integer> userId = getIdFromDoc(curUser);
                if (userId.isPresent()) {
                    Map<String, Object> userDoc = (Map<String, Object>) curUser.get("doc");
                    this.idToUser.put(userId.get(), userDoc.get("data"));
                    Object userName = userDoc.get(BaseElementType.NAME);
                    if (userName != null) {
                        this.nameToUser.put(userName.toString(),  userDoc.get("data"));
                    }
                }
            }

        } catch (Exception exception) {

           log.debug("JAMA-Service: JamaAPIMock encountered problems loading the mock data! " );
           log.debug("JAMA_SERVICE: The following exception was thrown:");
           log.debug(exception.getMessage());

        }

    }

    private Optional<Integer> getIdFromDoc(Map<String, Object> document) {
        Object doc_ =  document.get("doc");

        if (doc_ != null) {
            Map<String, Object> doc = (Map<String, Object>) doc_;
            Object data_ = doc.get("data");

            if (data_ != null) {
                Map<String, Object> data = (Map<String, Object>) data_;

                try {
                    return Optional.of(Integer.parseInt(data.get("id").toString()));
                } catch (NumberFormatException ne) {
                    return Optional.empty();
                }
            }
        }

        return Optional.empty();
    }

    private Optional<String> getKeyFromDoc(Map<String, Object> document) {
        Object doc_ =  document.get("doc");

        if (doc_ != null) {
            Map<String, Object> doc = (Map<String, Object>) doc_;
            Object data_ = doc.get("data");

            if (data_ != null) {
                Map<String, Object> data = (Map<String, Object>) data_;

                try {
                    return Optional.of(data.get("documentKey").toString());
                } catch (NumberFormatException ne) {
                    return Optional.empty();
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public Map<String, Object> getJamaItem(String docKey) {
        Object item = keyToJamaItem.get(docKey);
        if (item != null) {
        	ProgressEntry pe = dispatchAtomicFinishedActivity("Fetching Item for key: "+docKey);
            return (Map<String, Object>) item;
        }
        return null;
    }

    @Override
    public Map<String, Object> getJamaItem(int id) {    	
    	Object item = idToJamaItem.get(id);
        if (item != null) {
        	ProgressEntry pe = dispatchAtomicFinishedActivity("Fetching Item for id: "+id);
            return (Map<String, Object>) item;
        }
        return null;
    }

    @Override
    public Map<String, Object> getAllItemsMappedToKey() {
        return this.keyToJamaItem;
    }

    @Override
    public Map<Integer, Object> getAllItemsMappedToId() {
        return this.idToJamaItem;
    }

    @Override
    public Map<String, Object> getJamaUser(int userId) {
        Object user = idToUser.get(userId);
        if (user != null) {
            return (Map<String, Object>) user;
        }
        return null;
    }

    @Override
    public Map<String, Object> getJamaProject(int projectId) {
        Object project = idToProject.get(projectId);
        if (project != null) {
            return (Map<String, Object>) project;
        }
        return null;
    }

    @Override
    public Map<String, Object> getPickList(int pickListId) {
        Object pickList = idToPickList.get(pickListId);
        if (pickList != null) {
            return (Map<String, Object>) pickList;
        }
        return null;
    }

    @Override
    public Map<Integer, Object> getAllPickLists() {
        return this.idToPickList;
    }

    @Override
    public Map<String, Object> getItemType(int itemTypeId) {
        return (Map<String, Object>) this.idToItemType.get(itemTypeId);
    }

    @Override
    public Map<Integer, Object> getAllItemTypes() {
        return this.idToItemType;
    }

    @Override
    public Map<String, Object> getPickListOption(int pickListId, int optionId) {
        Object pickListOption = idToPickListOption.get(pickListId);
        if (pickListOption != null) {
            return (Map<String, Object>) pickListOption;
        }
        return null;
    }

    @Override
    public Map<Integer, Object> getAllPickListOptions() {
        return this.idToPickListOption;
    }

    @Override
    public Map<String, Object> getRelease(int releaseId) {
        return AccessToolsJSON.accessMap(this.idToRelease, releaseId);
    }

    private ArrayList<Object> createJSONListFromString(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        reader.lines().forEach(l -> sb.append(l));
        reader.close();
        return new ObjectMapper().readValue(sb.toString(), ArrayList.class);
    }

	@Override
	public Map<Integer, Object> getAllReleasesMappedToId() {
		return this.idToRelease;
	}
	
	@Override
	public Map<Integer, Object> getAllProjectsMappedToId() {
		return this.idToProject;
	}

	@Override
	public List<Map<String, Object>> getItemsViaFilter(int filterId) throws Exception {
		// not implemented
		return Collections.emptyList();
	}

}
