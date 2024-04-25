package at.jku.isse.designspace.azure.api;

import java.util.List;

public interface IAzureApi {

	String getProjectName();

	String getOrganizationName();

    byte[] getWorkItem(String projectName, int id);

	byte[] getAllWorkItems(List<Integer> ids);

	byte[] getAllWorkItemIds();

	byte[] getProject(String projectId);

	byte[] getComments(int workItemId);

	byte[] getWorkItemRelationTypes();

    byte[] getComment(int workItemId, int commentId);

    byte[] getWorkItemUpdates(int workItemId);

	byte[] getWorkItemTransitions(int[] ids);

	byte[] getWorkItemTypeStates(String workItemType);

	byte[] getWorkItemTypeCategories();

	byte[] getWorkItemTypesField(String workItemType);

	byte[] getWorkItemTypes();

    byte[] getUserByDescriptor(String userDescriptor);

    byte[] getUserByEmail(String email);

    byte[] getProjectFields();

	byte[] getSpecificWorkItemType(String workItemType);
}