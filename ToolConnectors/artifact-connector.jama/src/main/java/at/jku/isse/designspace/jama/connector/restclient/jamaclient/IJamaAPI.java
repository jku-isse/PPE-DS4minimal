package at.jku.isse.designspace.jama.connector.restclient.jamaclient;

import java.util.List;
import java.util.Map;

public interface IJamaAPI {

    Map<String, Object> getJamaItem(String docKey);

    Map<String, Object> getJamaItem(int id);

    Map<String, Object> getAllItemsMappedToKey();

    Map<Integer, Object> getAllItemsMappedToId();

    Map<String, Object> getJamaUser(int userId);

    Map<String, Object> getJamaProject(int projectId);

    Map<String, Object> getPickList(int pickListId);

    Map<Integer, Object> getAllPickLists();

    Map<String, Object> getItemType(int itemTypeId);

    Map<Integer, Object> getAllItemTypes();

    Map<String, Object> getPickListOption(int pickListId, int optionId);

    Map<Integer, Object> getAllPickListOptions();

    Map<String, Object> getRelease(int releaseId);

	Map<Integer, Object> getAllReleasesMappedToId();

	Map<Integer, Object> getAllProjectsMappedToId();

	List<Map<String, Object>> getItemsViaFilter(int filterId) throws Exception;

}
