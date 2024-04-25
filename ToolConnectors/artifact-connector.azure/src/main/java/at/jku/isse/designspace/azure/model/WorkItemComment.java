package at.jku.isse.designspace.azure.model;

public class WorkItemComment {
    private int id;
    private int workItemId;
    private String text;
    private String createdBy;
    private String createdDate;

    public WorkItemComment(int id, int workItemId, String text, String createdBy, String createdDate) {
        this.id = id;
        this.workItemId = workItemId;
        this.text = text;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
    }

    public int getId() {
        return id;
    }

    public int getWorkItemId() {
        return workItemId;
    }

    public String getText() {
        return text;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getCreatedDate() {
        return createdDate;
    }
}
