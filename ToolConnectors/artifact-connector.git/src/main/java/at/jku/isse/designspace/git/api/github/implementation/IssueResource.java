package at.jku.isse.designspace.git.api.github.implementation;

import at.jku.isse.designspace.git.api.*;
import at.jku.isse.designspace.git.api.core.InsufficientDataException;
import at.jku.isse.designspace.git.api.core.MapResource;
import at.jku.isse.designspace.git.api.github.restclient.IGithubRestClient;

import java.time.Instant;
import java.util.*;

public class IssueResource extends MapResource implements IGitIssue {

    private final static String ADDED_TO_PROJECT_EVENT = "added_to_project";
    private final static String MOVED_TO_PROJECT_COLUMN_EVENT = "moved_columns_in_project";
    private final static String REMOVED_FROM_PROJECT_EVENT = "removed_from_project";

    private final String repositoryName;
    private final Integer issueKey;

    private List<IGitComment> comments;
    private IGitPullRequest pullRequest;

    public IssueResource(IGithubRestClient source, String repositoryName, Integer issueKey) {
        super(source);
        assert repositoryName != null && issueKey != null;
        this.issueKey = issueKey;
        this.repositoryName = repositoryName;
    }

    public IssueResource(IGithubRestClient source, String repositoryName, Map<String, Object> data) throws InsufficientDataException {
        super(source, data);
        this.repositoryName = repositoryName;
        this.issueKey = getKey();
    }

    @Override
    public int getKey() {
        return accessIntegerProperty("number");
    }

    @Override
    public String getId() {
        return repositoryName + "/issues/" + issueKey;
    }

    @Override
    public String getBody() {
        return accessStringProperty("body");
    }

    @Override
    public String getTitle() {
        return accessStringProperty("title");
    }

    @Override
    public String getURI() {
        return accessStringProperty("uri");
    }

    @Override
    public String getState() {
        return accessStringProperty("state");
    }

    /*
        There may be multiple projects for this issue, which is why there can also be
        multiple stats. The Map returns project-status pairs.
     */
    @Override
    public Map<String, String> getProjectStatus() {
        Object projectStatus_ = this.getResource().get("project_status");
        if (projectStatus_ != null) {
            return (Map<String, String>) projectStatus_;
        } else {
            getStatusAndProjectFromLatestTimelineUpdate();
            projectStatus_ = this.getResource().get("project_status");
            if (projectStatus_ != null) {
                return (Map<String, String>) projectStatus_;
            }
        }
        return null;
    }

    @Override
    public String getHTMLUrl() {
        return accessStringProperty("html_url");
    }

    @Override
    public Instant closedAt() {
        String closedAt = accessStringProperty("closedAt");
        if (closedAt != null) {
            return Instant.parse(closedAt);
        }
        return null;
    }

    @Override
    public String[] getTags() {
        Map<String, Object> resource = getResource();
        if (resource != null) {
            Object tags_ = resource.get("labels");
            if (tags_ != null) {
                ArrayList<Map<String, Object>> tags = (ArrayList<Map<String, Object>>) tags_;
                String[] tagList = new String[tags.size()];
                for (int i = 0; i < tagList.length; i++) {
                    Map<String, Object> tag = tags.get(i);
                    tagList[i] = (String) tag.get("name");
                }
                return tagList;
            }
        }
        return new String[0];
    }

    @Override
    public IGitUser getAssignee() {
        Map<String, Object> resource = getResource();
        if (resource != null) {
            Object incompleteUser_ = resource.get("assignee");
            if (incompleteUser_ != null) {
                Map<String, Object> incompleteUser = (Map<String, Object>) incompleteUser_;
                try {
                    return new UserResource(this.getSource(), incompleteUser);
                } catch (InsufficientDataException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public IGitUser getReportedBy() {
        Map<String, Object> resource = getResource();
        if (resource != null) {
            Object incompleteUser_ = resource.get("user");
            if (incompleteUser_ != null) {
                Map<String, Object> incompleteUser = (Map<String, Object>) incompleteUser_;
                try {
                    return new UserResource(this.getSource(), incompleteUser);
                } catch (InsufficientDataException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public String getRepository() {
        return this.repositoryName;
    }

    @Override
    public List<IGitComment> getComments() {
        if (this.comments == null) {
            IssueCommentsResource issueCommentsResource = new IssueCommentsResource(this.getSource(), this.repositoryName, issueKey);
            this.comments = issueCommentsResource.getResources();
        }
        return this.comments;
    }

    @Override
    public List<IGitCommit> getCommits() {
        //not implemented
        return new ArrayList<>();
    }

    @Override
    public int[] getLinkedIssueKeys() {
        ArrayList<Integer> keys = new ArrayList<>();

        String description = getBody();
        if (description != null) {
            keys.addAll(getKeysFromText(description));
        }

        List<IGitComment> comments = this.getComments();
        if (comments != null) {
            for (IGitComment comment : comments) {
                String body = comment.getBody();
                if (body != null) {
                    keys.addAll(getKeysFromText(body));
                }
            }
        }

        return keys.stream().distinct().mapToInt(i->i).toArray();
    }

    @Override
    public Optional<IGitPullRequest> getAsPullRequest() {
        if (this.pullRequest == null) {
            Map<String, Object> resource = this.getResource();
            if (resource != null && resource.containsKey("pull_request")) {
                this.pullRequest = new PullRequestResource(this.getSource(), this.repositoryName, this.issueKey);
            }
        }
        if (this.pullRequest == null) {
            return Optional.empty();
        }
        return Optional.of(pullRequest);
    }

    @Override
    public IGitRepository getRepositoryObject() {
        //ToDo: implement
        return null;
    }

    @Override
    public List<IGitIssue> getLinkedArtifacts() {
        //ToDo: implement
        return null;
    }

    private ArrayList<Integer> getKeysFromText(String text) {
        assert text != null;
        String[] splits = text.split("#");
        ArrayList<Integer> keys = new ArrayList<>();
        for (int i=1; i<splits.length; i++) {
            StringBuilder number = new StringBuilder();
            for (int j=0; j<splits[i].length(); j++) {
                char cur = splits[i].charAt(j);
                if (cur >= '0' && cur <= '9') {
                    number.append(cur);
                } else {
                    break;
                }
            }
            if (number.length() > 0) {
                keys.add(Integer.parseInt(number.toString()));
            }
        }
        return keys;
    }

    @Override
    protected Map<String, Object> load(IGithubRestClient source) {
        return source.getIssue(repositoryName, issueKey);
    }

    private void getStatusAndProjectFromLatestTimelineUpdate() {
        ArrayList<Map<String, Object>> timeline = this.source.getIssueTimeline(this.repositoryName, this.issueKey);
        Map<String, String> projectStatus = new HashMap<>();
        this.getResource().put("project_status", projectStatus);
        if (timeline != null) {
            for (Map<String, Object> event : timeline) {
                if (event.containsKey("event")) {
                    if (event.get("event").equals(ADDED_TO_PROJECT_EVENT) |
                            event.get("event").equals(MOVED_TO_PROJECT_COLUMN_EVENT)) {
                        if (event.containsKey("project_card")) {
                            Map<String, Object> projectCard = (Map<String, Object>) event.get("project_card");
                            String projectUri = (String) projectCard.get("project_url");
                            String columnName = (String) projectCard.get("column_name");
                            projectStatus.put(projectUri, columnName);
                        }
                    } else if (event.get("event").equals(REMOVED_FROM_PROJECT_EVENT)) {
                        if (event.containsKey("project_card")) {
                            Map<String, Object> projectCard = (Map<String, Object>) event.get("project_card");
                            String projectUri = (String) projectCard.get("project_url");
                            projectStatus.remove(projectUri);
                        }
                    }
                }
            }
        }
    }

    public String getString() {
        StringBuilder sb = new StringBuilder();

        sb.append("{ \n");

        sb.append("key = ").append(this.getKey()).append(",\n");
        sb.append("title = ").append(this.getTitle()).append(",\n");
        sb.append("body = ").append(this.getBody()).append(",\n");
        sb.append("labels = ").append(Arrays.toString(this.getTags())).append(",\n");
        sb.append("html_url = ").append(this.getHTMLUrl()).append(",\n");

        Map<String, String> projectStatus = this.getProjectStatus();
        if (projectStatus != null) {
            sb.append("projectStatus = ").append(projectStatus).append(",\n");
        }

        IGitUser reporter = this.getReportedBy();
        if (reporter != null) {
            sb.append("reported_by = ").append(reporter.getUserId()).append(",\n");
        }

        int[] linkedIssues = this.getLinkedIssueKeys() ;
        sb.append("linkedIssues = [ \n");
        for (int key : linkedIssues) {
            sb.append(key).append("\n");
        }
        sb.append("], \n");

        String[] tags = this.getTags();
        sb.append("tags = [ \n");
        for (String tag : tags) {
            sb.append(tag).append("\n");
        }
        sb.append("], \n");

        List<IGitComment> comments = this.getComments() ;
        sb.append("comments = [ \n");
        for (IGitComment comment : comments) {
            sb.append("{ \n");
            sb.append("author : ").append(comment.getAuthor().getUserId()).append(",\n");
            sb.append("body : ").append(comment.getBody()).append(",\n");
            sb.append("} \n");
        }
        sb.append("]");
        sb.append("\n }");

        return sb.toString();
    }



}
