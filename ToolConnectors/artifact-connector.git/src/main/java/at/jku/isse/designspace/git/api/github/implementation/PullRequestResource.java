package at.jku.isse.designspace.git.api.github.implementation;

import at.jku.isse.designspace.git.api.*;
import at.jku.isse.designspace.git.api.core.InsufficientDataException;
import at.jku.isse.designspace.git.api.core.MapResource;
import at.jku.isse.designspace.git.api.github.restclient.IGithubRestClient;

import java.time.Instant;
import java.util.*;

public class PullRequestResource extends MapResource implements IGitPullRequest {

    private final static String ADDED_TO_PROJECT_EVENT = "added_to_project";
    private final static String MOVED_TO_PROJECT_COLUMN_EVENT = "moved_columns_in_project";
    private final static String REMOVED_FROM_PROJECT_EVENT = "removed_from_project";

    private String repositoryName;
    private Integer key;

    private List<IGitCommit> commits;
    private List<IGitComment> comments;

    public PullRequestResource(IGithubRestClient source, String repositoryName, Integer key) {
        super(source);
        assert repositoryName != null && key != null;
        this.repositoryName = repositoryName;
        this.key = key;
    }

    public PullRequestResource(IGithubRestClient source, String repositoryName, Map<String, Object> data) throws InsufficientDataException {
        super(source, data);
        assert repositoryName != null;
        this.repositoryName = repositoryName;
        this.key = getKey();
        if (this.key == null) throw new InsufficientDataException();
    }

    @Override
    public int getKey() {
        return accessIntegerProperty("number");
    }

    @Override
    public String getId() {
        return repositoryName + "/issues/" + key;
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
    public String getName() {
        return accessStringProperty("name");
    }

    @Override
    public String getHTMLUrl() {
        return accessStringProperty("html_url");
    }

    @Override
    public String getURI() {
        return accessStringProperty("url");
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
                ArrayList tags = (ArrayList) tags_;
                String[] tagList = new String[tags.size()];
                for (int i = 0; i < tagList.length; i++) {
                    Map<String, Object> tag = (Map<String, Object>) tags.get(i);
                    tagList[i] = (String) tag.get("name");
                }
                return tagList;
            }
        }
        return null;
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
            Object incompleteUser_ = resource.get("reported_by");
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
            IssueCommentsResource issueCommentsResource = new IssueCommentsResource(this.getSource(), this.repositoryName, this.getKey());
            this.comments = issueCommentsResource.getResources();
        }
        return this.comments;
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

        return keys.stream().mapToInt(i->i).toArray();
    }

    @Override
    public Optional<IGitPullRequest> getAsPullRequest() {
        return Optional.of(this);
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
    public Instant getMergedAt() {
        String merged_at = accessStringProperty("merged_at");
        if (merged_at != null) {
            return Instant.parse(merged_at);
        }
        return null;
    }

    @Override
    public IGitUser getMergedBy() {
        Map<String, Object> resource = getResource();
        if (resource != null) {
            Object incompleteUser_ = resource.get("merged_by");
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
    public String getFromBranch() {
        Map<String, Object> resource = getResource();
        if (resource != null) {
            Object headCommit_ = resource.get("head");
            if (headCommit_ != null) {
                Map<String, Object> headCommit = (Map<String, Object>) headCommit_;
                Object branch_ = headCommit.get("ref");
                if (branch_ != null) {
                    return (String) branch_;
                }
            }
        }
        return null;
    }

    @Override
    public String getDestinationBranch() {
        Map<String, Object> resource = getResource();
        if (resource != null) {
            Object headCommit_ = resource.get("base");
            if (headCommit_ != null) {
                Map<String, Object> headCommit = (Map<String, Object>) headCommit_;
                Object branch_ = headCommit.get("ref");
                if (branch_ != null) {
                    return (String) branch_;
                }
            }
        }
        return null;
    }

    @Override
    public IGitCommit getHead() {
        Map<String, Object> resource = getResource();
        if (resource != null) {
            Object headCommit_ = resource.get("head");
            if (headCommit_ != null) {
                //headCommit does not contain all required information like files for example,
                //which is why we have to fetch it again
                Map<String, Object> headCommit = (Map<String, Object>) headCommit_;
                Object sha_ = headCommit.get("sha");

                if (sha_ != null) {
                    if (getMergedBy() != null) {
                        return new CommitResource(this.getSource(), this.repositoryName, new String[]{getFromBranch(), getDestinationBranch()},(String) sha_);
                    }
                    return new CommitResource(this.getSource(), this.repositoryName, new String[]{getFromBranch()},(String) sha_);
                }
            }
        }
        return null;
    }

    @Override
    public IGitCommit getBase() {
        Map<String, Object> resource = getResource();
        if (resource != null) {
            Object baseCommit_ = resource.get("base");
            if (baseCommit_ != null) {
                //baseCommit does not contain all required information like files for example,
                //which is why we have to fetch it again
                Map<String, Object> baseCommit = (Map<String, Object>) baseCommit_;
                Object sha_ = baseCommit.get("sha");

                if (sha_ != null) {
                    return new CommitResource(this.getSource(), this.repositoryName, new String[]{getFromBranch(), getDestinationBranch()}, (String) sha_);
                }
            }
        }
        return null;
    }

    @Override
    public List<IGitCommit> getCommits() {
        if (this.commits == null) {
            CommitsResource commitsResource;
            if (getMergedBy() != null) {
                commitsResource = new CommitsResource(this.getSource(), this.repositoryName, this.key, new String[]{getFromBranch(), getDestinationBranch()});
            } else {
                commitsResource = new CommitsResource(this.getSource(), this.repositoryName, this.key, new String[]{getFromBranch()});
            }
            this.commits = commitsResource.getResources();
        }
        return this.commits;
    }

    @Override
    public List<IGitUser> getRequestedReviewers() {
        Map<String, Object> resource = getResource();
        if (resource != null) {
            Object requestedReviewers_ = resource.get("requestedReviewers");
            if (requestedReviewers_ != null) {
                ArrayList<IGitUser> users = new ArrayList<>();
                ArrayList<Map<String, Object>> rawUsers = (ArrayList<Map<String, Object>>) requestedReviewers_;
                for (Map<String, Object> rawUser : rawUsers) {
                    try {
                        UserResource userResource =  new UserResource(this.getSource(), rawUser);
                        users.add(userResource);
                    } catch (InsufficientDataException e) {
                        e.printStackTrace();
                    }
                }
                return users;
            }
        }
        return new ArrayList<>();
    }

    @Override
    protected Map<String, Object> load(IGithubRestClient source) {
        return source.getPullRequest(repositoryName, key);
    }

    private void getStatusAndProjectFromLatestTimelineUpdate() {
        ArrayList<Map<String, Object>> timeline = this.source.getIssueTimeline(this.repositoryName, this.key);
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
                            projectStatus.put("project", projectUri);
                            projectStatus.put("status", columnName);
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
        sb.append("repository = ").append(this.getRepository()).append(",\n");
        sb.append("mergedAt = ").append(this.getMergedAt()).append(",\n");

        IGitCommit base = this.getBase();
        sb.append("baseCommit = { \n");
        sb.append("sha = ").append(base.getSha()).append(",\n");
        sb.append("totalAdditions = ").append(base.getTotalAdditions()).append(",\n");
        sb.append("totalDeletions = ").append(base.getTotalDeletions()).append(",\n");
        sb.append("} \n");

        IGitCommit head = this.getHead();
        sb.append("headCommit = { \n");
        sb.append("sha = ").append(head.getSha()).append(",\n");
        sb.append("totalAdditions = ").append(head.getTotalAdditions()).append(",\n");
        sb.append("totalDeletions = ").append(head.getTotalDeletions()).append(",\n");
        sb.append("} \n");

        IGitUser reporter = this.getReportedBy();
        if (reporter != null) {
            sb.append("reported_by = ").append(reporter.getUserId()).append(",\n");
        }

        List<IGitUser> reviewers = this.getRequestedReviewers() ;
        sb.append("reviewers = [ \n");
        for (IGitUser reviewer : reviewers) {
            sb.append("{ \n");
            sb.append("userId = ").append(reviewer.getUserId()).append("\n");
            sb.append("} \n");
        }
        sb.append("], \n");

        int[] linkedIssues = this.getLinkedIssueKeys() ;
        sb.append("linkedIssues = [ \n");
        for (int key : linkedIssues) {
            sb.append(key).append("\n ");
        }
        sb.append("], \n");

        List<IGitComment> comments = this.getComments() ;
        sb.append("comments = [ \n");
        for (IGitComment comment : comments) {
            sb.append("{ \n");
            sb.append("author = ").append(comment.getAuthor().getUserId()).append(",\n");
            sb.append("body = ").append(comment.getBody()).append(",\n");
            sb.append("} \n");
        }
        sb.append("]");
        sb.append("\n }");

        return sb.toString();
    }
}
