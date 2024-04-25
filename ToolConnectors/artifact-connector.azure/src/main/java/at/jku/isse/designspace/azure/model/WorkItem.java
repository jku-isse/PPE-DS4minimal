package at.jku.isse.designspace.azure.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class WorkItem {
    private int id;
    private String project;
    private String organizationName;
    private String workItemType;
    private String title;
    private String state;
    private String description;
    private int priority = -1;
    private String category = "Unknown";
    private List<WorkItemLink> relatedItems = new ArrayList<>();;
    private String assignee;
    private String creator;
    private int commentCount;

//    public WorkItem() {
//        this.relatedItems = new ArrayList<>();
//    }

//    public int getId() {
//        return id;
//    }
//
//    public void setId(int id) {
//        this.id = id;
//    }
//
//    public String getProject() {
//        return project;
//    }
//
//    public String getOrganizationName() {
//        return organizationName;
//    }
//
//    public void setOrganizationName(String organizationName) {
//        this.organizationName = organizationName;
//    }
//
//    public void setProject(String project) {
//        this.project = project;
//    }
//
//    public String getWorkItemType() {
//        return workItemType;
//    }
//
//    public void setWorkItemType(String workItemType) {
//        this.workItemType = workItemType;
//    }
//
//    public String getTitle() {
//        return title;
//    }
//
//    public void setTitle(String title) {
//        this.title = title;
//    }
//
//    public String getState() {
//        return state;
//    }
//
//    public void setState(String state) {
//        this.state = state;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public int getPriority() {
//        return priority;
//    }
//
//    public void setPriority(int priority) {
//        this.priority = priority;
//    }
//
//    public List<WorkItemLink> getRelatedItems() {
//        return relatedItems;
//    }
//
//    public void addRelatedItem(WorkItemLink relatedItem) {
//        this.relatedItems.add(relatedItem);
//    }
//
//    public String getAssignee() {
//        return assignee;
//    }
//
//    public void setAssignee(String assignee) {
//        this.assignee = assignee;
//    }
//
//    public String getCreator() {
//        return creator;
//    }
//
//    public void setCreator(String creator) {
//        this.creator = creator;
//    }
//
//    public int getCommentCount() {
//        return commentCount;
//    }
//
//    public void setCommentCount(int commentCount) {
//        this.commentCount = commentCount;
//    }
}
