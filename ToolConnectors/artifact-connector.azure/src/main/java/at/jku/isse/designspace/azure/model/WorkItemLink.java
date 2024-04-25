package at.jku.isse.designspace.azure.model;

public class WorkItemLink {
    private String linkType;
    private int workItemId;

    public WorkItemLink(String linkType, int workItemId) {
        this.linkType = linkType;
        this.workItemId = workItemId;
    }

    public String getLinkType() {
        return linkType;
    }

    public int getRelatedWorkItemId() {
        return workItemId;
    }
}
