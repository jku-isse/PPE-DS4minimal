package at.jku.isse.designspace.git.api.core.changemanagement;

public class GitChange<T> {

    public enum ModificationType {MAP_ADD, MAP_REMOVE, LIST_ADD, LIST_ENTRY_MODIFY, LIST_REMOVE, ASSIGNMENT, CREATION}
    public enum ChangeType {ISSUE, PULL_REQUEST, COMMIT, BRANCH, REPOSITORY};

    protected String id;
    protected int key;

    protected String fieldName;
    protected String repoName;

    protected ChangeType changeType;
    protected ModificationType modificationType;
    protected T newValue;

    public GitChange(String id, int key, ChangeType changeType, String fieldName, String repoName, ModificationType modificationType, T newValue) {
        this.id = id;
        this.key = key;
        this.changeType = changeType;
        this.fieldName = fieldName;
        this.newValue = newValue;
        this.repoName = repoName;
        this.modificationType = modificationType;
    }

    public String getId() {
        return id;
    }

    public int getKey() {
        return key;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public ModificationType getModificationType() {
        return modificationType;
    }

    public String getRepoName() {
        return repoName;
    }

    public T getNewValue() {
        return newValue;
    }

    public String getFieldName() {
        return this.fieldName;
    }

}
