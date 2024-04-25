package at.jku.isse.designspace.azure.updateservice;

public class AzureChange<T> {
    private int workItemId;
    private String fieldName;
    private String projectName;
    private ChangeType changeType;
    private T newValue;

    public AzureChange(int workItemId, String fieldName, String projectName, ChangeType changeType,
                       T newValue) {
        this.workItemId = workItemId;
        this.changeType = changeType;
        this.fieldName = fieldName;
        this.projectName = projectName;
        this.newValue = newValue;
    }

    public AzureChange(int workItemId, String projectName, ChangeType changeType, T newValue) {
        this.workItemId = workItemId;
        this.projectName = projectName;
        this.changeType = changeType;
        this.newValue = newValue;
    }

    public AzureChange(int workItemId, String projectName, ChangeType changeType) {
        this.workItemId = workItemId;
        this.projectName = projectName;
        this.changeType = changeType;
    }

    public int getWorkItemId() {
        return this.workItemId;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public String getProjectName() {
        return this.projectName;
    }

    public T getNewValue() {
        return this.newValue;
    }

    public ChangeType getChangeType() {
        return this.changeType;
    }

    public static enum ChangeType {
        NEW_COMMENT,
        COMMENT_DELETE,
        FIELD_MODIFY,
        RELATION_ADD,
        RELATION_REMOVE,
        COMMENT_UPDATE,
        WORK_ITEM_DELETE,
        WORK_ITEM_RESTORE;

        private ChangeType() {
        }
    }
}
