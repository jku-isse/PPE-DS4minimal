package at.jku.isse.designspace.git.api.github.webhookparser;

import at.jku.isse.designspace.git.api.*;
import at.jku.isse.designspace.git.api.core.InsufficientDataException;
import at.jku.isse.designspace.git.api.core.changemanagement.GitChange;
import at.jku.isse.designspace.git.api.core.changemanagement.listener.*;
import at.jku.isse.designspace.git.api.core.webhookparser.IChangeFactory;
import at.jku.isse.designspace.git.api.github.GithubTypes;
import at.jku.isse.designspace.git.api.github.implementation.*;
import at.jku.isse.designspace.git.api.github.restclient.IGithubRestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GitHubChangeFactory implements IChangeFactory {

    private static final String ASSIGNED = "assigned";
    private static final String UNASSIGNED = "unassigned";
    private static final String CLOSED = "closed";
    private static final String OPENED = "opened";
    private static final String CREATED = "created";
    private static final String EDITED = "edited";
    private static final String LABELED = "labeled";
    private static final String DELETED = "deleted";
    private static final String SYNCHRONIZED = "synchronize";
    private static final String PUSHED = "pusher";
    private static final String REOPENED = "reopened";
    private static final String UNLABELED = "unlabeled";

    private final List<IIntegerChangeListener> integerChangeListenerListeners;
    private final List<IBooleanChangeListener> booleanChangeListeners;
    private final List<IStringChangeListener> stringChangeListeners;
    private final List<IIssueChangeListener> issueChangeListeners;
    private final List<IPullRequestChangeListener> pullRequestChangeListeners;
    private final List<ICommitChangeListener> commitChangeListeners;
    private final List<ICommentChangeListener> commentChangeListeners;
    private final List<IRepositoryChangeListener> repositoryChangeListeners;
    private final List<IBranchChangeListener> branchChangeListeners;
    private final List<IUserChangeListener> userChangeListeners;
    private final List<IMapChangeListener> mapChangeListeners;

    private final IGithubRestClient gitRestClient;

    public GitHubChangeFactory(IGithubRestClient gitRestClient) {
        this.gitRestClient = gitRestClient;

        this.integerChangeListenerListeners = new ArrayList<>();
        this.booleanChangeListeners = new ArrayList<>();
        this.stringChangeListeners = new ArrayList<>();
        this.issueChangeListeners = new ArrayList<>();
        this.pullRequestChangeListeners = new ArrayList<>();
        this.commentChangeListeners = new ArrayList<>();
        this.commitChangeListeners = new ArrayList<>();
        this.repositoryChangeListeners = new ArrayList<>();
        this.branchChangeListeners = new ArrayList<>();
        this.userChangeListeners = new ArrayList<>();
        this.mapChangeListeners = new ArrayList<>();
    }

    @Override
    public void createChanges(Object updateResponse) {

        if (updateResponse instanceof Map){
            Map<String, Object> updateMap = (Map<String, Object>) updateResponse;

            String repository = null;
            if (updateMap.containsKey(GithubTypes.REPOSITORY.getName())) {
                Map<String, Object> repo = (Map<String, Object>) updateMap.get(GithubTypes.REPOSITORY.getName());
                repository = (String) repo.get("name");
            }

            //issue changes
            if (updateMap.containsKey(GithubTypes.ISSUE.getName()) || updateMap.containsKey(GithubTypes.PULL_REQUEST.getName())) {
                String mapAccess = "issue";
                GitChange.ChangeType changeType = GitChange.ChangeType.ISSUE;
                if (updateMap.containsKey(GithubTypes.PULL_REQUEST.getName())) {
                    changeType = GitChange.ChangeType.PULL_REQUEST;
                    mapAccess = "pull_request";
                }

                Map<String, Object> issue = (Map<String, Object>) updateMap.get(mapAccess);
                int key = (int) issue.get("number");
                String id = changeType.name() + "_" + key;
                if (updateMap.containsKey("action")) {
                    String action = (String) updateMap.get("action");
                    switch (action) {
                        case ASSIGNED:
                            if (updateMap.containsKey("assignee")) {
                                Map<String, Object> assignee = (Map<String, Object>) updateMap.get("assignee");
                                String login = (String) assignee.get("login");
                                System.out.println(login + " was assigned to issue " + key + " in repo " + repository);

                                try {
                                    UserResource userResource = new UserResource(this.gitRestClient, assignee);
                                    GitChange<IGitUser> userChange = new GitChange<>(id, key, changeType, "assignee", repository, GitChange.ModificationType.ASSIGNMENT, userResource);
                                    notifyUserListeners(userChange);
                                    return;
                                } catch (InsufficientDataException e) {
                                    e.printStackTrace();
                                }
                            }
                        case UNASSIGNED:
                            System.out.println("unassigned");
                            GitChange<IGitUser> userChange = new GitChange<>("issue_" + key, key, changeType, "assignee", repository, GitChange.ModificationType.ASSIGNMENT, null);
                            notifyUserListeners(userChange);
                            return;
                        case CLOSED:
                            System.out.println("issue " + key + " in repo " + repository + "has been closed");
                            GitChange<String> stringChange = new GitChange<>(id, key, changeType, "state", repository, GitChange.ModificationType.ASSIGNMENT, "closed");
                            notifyStringListeners(stringChange);
                            return;
                        case CREATED:
                            if (updateMap.containsKey("comment")) {
                                Map<String, Object> comment = (Map<String, Object>) updateMap.get("comment");
                                if (comment.containsKey("user")) {
                                    Map<String, Object> user = (Map<String, Object>) comment.get("user");
                                    String userLogin = (String) user.get("login");
                                    System.out.println("Issue " + key + " of repo " + repository +
                                            " was commented by " + userLogin + " --> " + comment.get("body"));

                                    try {
                                        CommentResource commentResource = new CommentResource(this.gitRestClient, comment);
                                        GitChange<IGitComment> commentChange = new GitChange<>(id, key, changeType, "comments", repository, GitChange.ModificationType.LIST_ADD, commentResource);
                                        notifyCommentChangeListeners(commentChange);
                                        return;
                                    } catch (InsufficientDataException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        case OPENED:
                            try {
                                IssueResource issueResource = new IssueResource(this.gitRestClient, repository, issue);
                                //immediate loading to not slow down user
                                //issueResource.getCommits();
                                GitChange<IGitIssue> issueChange = new GitChange<>(id, key, changeType, null, repository, GitChange.ModificationType.CREATION, issueResource);
                                notifyIssueListeners(issueChange);
                                return;
                            } catch (InsufficientDataException e) {
                                e.printStackTrace();
                            }
                        case REOPENED:
                            System.out.println("issue " + key + " in repo " + repository + "has been closed");
                            GitChange<String> stringChange1 = new GitChange<>(id, key, changeType, "state", repository, GitChange.ModificationType.ASSIGNMENT, "open");
                            notifyStringListeners(stringChange1);
                            return;
                        case DELETED:
                            if (updateMap.containsKey("comment")) {
                                Map<String, Object> comment = (Map<String, Object>) updateMap.get("comment");
                                if (comment.containsKey("user")) {
                                    try {
                                        CommentResource commentResource = new CommentResource(this.gitRestClient, comment);
                                        GitChange<IGitComment> commentChange = new GitChange<>(id, key, changeType, "comments", repository, GitChange.ModificationType.LIST_REMOVE, commentResource);
                                        notifyCommentChangeListeners(commentChange);
                                        return;
                                    } catch (InsufficientDataException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        case EDITED:
                            if (updateMap.containsKey("comment")) {
                                Map<String, Object> comment = (Map<String, Object>) updateMap.get("comment");
                                if (comment.containsKey("user")) {
                                    try {
                                        CommentResource commentResource = new CommentResource(this.gitRestClient, comment);
                                        GitChange<IGitComment> commentChange = new GitChange<>(id, key, changeType,"comments", repository, GitChange.ModificationType.LIST_ENTRY_MODIFY, commentResource);
                                        notifyCommentChangeListeners(commentChange);
                                        return;
                                    } catch (InsufficientDataException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else if (updateMap.containsKey("changes") && issue.containsKey("body")) {
                                String description = (String) issue.get("body");
                                GitChange<String> stringChange2 = new GitChange<>(id, key, changeType,"body", repository, GitChange.ModificationType.ASSIGNMENT, description);
                                notifyStringListeners(stringChange2);
                                return;
                            }
                        case LABELED:
                            if (updateMap.containsKey("label")) {
                                Map<String, Object> label = (Map<String, Object>) updateMap.get("label");
                                String labelName = (String) label.get("name");
                                GitChange<String> stringChange3 = new GitChange<>(id, key, changeType,"labels", repository, GitChange.ModificationType.LIST_ADD, labelName);
                                notifyStringListeners(stringChange3);
                                return;
                            }
                        case UNLABELED:
                            if (updateMap.containsKey("label")) {
                                Map<String, Object> label = (Map<String, Object>) updateMap.get("label");
                                String labelName = (String) label.get("name");
                                GitChange<String> stringChange4 =  new GitChange<>(id, key, changeType,"labels", repository, GitChange.ModificationType.LIST_REMOVE, labelName);
                                notifyStringListeners(stringChange4);
                                return;
                            }
                        case SYNCHRONIZED:
                            System.out.println("synchronized");

                            String oldHeadCommitSha = (String) updateMap.get("before");
                            String newHeadCommitSha = (String) updateMap.get("after");
                            Map<String, Object> pullRequestMap = (Map<String, Object>) updateMap.get("pull_request");
                            Map<String, Object> headMap = (Map<String, Object>) pullRequestMap.get("head");
                            String branch = (String) headMap.get("ref");

                            CommitResource headResource = new CommitResource(this.gitRestClient, repository, new String[]{branch}, newHeadCommitSha);

                            //immediate loading to not slow down user
                            //headResource.getComments();
                            GitChange<IGitCommit> headChange = new GitChange<>(id, key, changeType, "head", repository, GitChange.ModificationType.ASSIGNMENT, headResource);

                            ArrayList<Map<String, Object>> rawCommits = this.gitRestClient.getCommits(repository, key);
                            ArrayList<GitChange<IGitCommit>> changes = new ArrayList<>() {{ add(headChange); }};

                            boolean newCommit = false;
                            for (Map<String, Object> rawCommit : rawCommits) {
                                String curSha = (String) rawCommit.get("sha");
                                if (newCommit) {
                                    CommitResource commitResource = new CommitResource(this.gitRestClient, repository, new String[]{branch}, curSha);
                                    //immediate loading to not slow down user
                                    //commitResource.getComments();
                                    changes.add(new GitChange<>(id, key, changeType,"commits", repository, GitChange.ModificationType.LIST_ADD, commitResource));
                                } else {
                                    if (curSha.equals(oldHeadCommitSha)) {
                                        newCommit = true;
                                    }
                                }
                            }

                            for (GitChange<IGitCommit> commitChange : changes) {
                                notifyCommitChangeListeners(commitChange);
                            }
                            return;
                        default:
                            System.out.println("Issue/Pull could not be parsed!");
                    }

                }
            }

            //any commit comment change
            if (updateMap.containsKey(GithubTypes.COMMENT.getName()) && !updateMap.containsKey(GithubTypes.ISSUE.getName())) {
                Map<String, Object> comment = (Map) updateMap.get(GithubTypes.COMMENT.getName());

                if (updateMap.containsKey("action")) {
                    String action = (String) updateMap.get("action");
                    GitChange.ModificationType modificationType = GitChange.ModificationType.LIST_ADD;
                    if (action.equals(EDITED)) {
                        modificationType = GitChange.ModificationType.LIST_ENTRY_MODIFY;
                    } else if (action.equals(DELETED)) {
                        modificationType = GitChange.ModificationType.LIST_REMOVE;
                    }

                    if (comment.containsKey("commit_id")) {
                        String commitSha = (String) comment.get("commit_id");
                        try {
                            CommentResource commentResource = new CommentResource(this.gitRestClient, comment);
                            GitChange<IGitComment> commentChange = new GitChange<>(commitSha, -1, GitChange.ChangeType.COMMIT, "comments", repository, modificationType, commentResource);
                            notifyCommentChangeListeners(commentChange);
                            return;
                        } catch (InsufficientDataException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            //project status changes
            if (updateMap.containsKey(GithubTypes.PROJECT_CARD.getName())) {
                Map<String, Object> projectCard_ = (Map<String, Object>) updateMap.get(GithubTypes.PROJECT_CARD.getName());
                if (projectCard_.containsKey("content_url")) {
                    String contentUrl = (String) projectCard_.get("content_url");
                    String projectUrl = (String) projectCard_.get("project_url");

                    String[] contentUrlParts = contentUrl.split("/");
                    String[] projectUrlParts = projectUrl.split("/");

                    String issueKey = contentUrlParts[contentUrlParts.length - 1];
                    String projectIdString = projectUrlParts[projectUrlParts.length - 1];
                    try {

                        Integer columnId = (Integer) projectCard_.get("column_id");
                        Map<String, Object> column = this.gitRestClient.getProjectColumn(columnId);
                        Map<String, Object> project = this.gitRestClient.getProject(Integer.parseInt(projectIdString));

                        if (column != null && project != null) {
                            String projectName = (String) project.get("name");
                            String columnName = (String) column.get("name");
                            HashMap<String, Object> status = new HashMap<>() {{
                                put(projectName, columnName);
                            }};
                            GitChange<Map<String, Object>> mapChange =
                                    new GitChange<>(issueKey, Integer.parseInt(issueKey), GitChange.ChangeType.ISSUE, "projectStatus", repository, GitChange.ModificationType.MAP_ADD, status);
                            notifyMapChangeListeners(mapChange);
                            return;
                        }
                    } catch (NumberFormatException ne) {
                        System.out.println("The change format is no longer valid");
                    }
                }
            }

            //Updates on repository by pushes (merges)
            if (updateMap.containsKey(PUSHED)) {
                String branchURI = (String) updateMap.get("ref");
                String[] parts = branchURI.split("/");
                String branchName = parts[parts.length - 1];

                boolean created = (boolean) updateMap.get(CREATED);
                boolean deleted = (boolean) updateMap.get(DELETED);

                if (created) {
                    BranchResource branchResource = new BranchResource(this.gitRestClient, repository, branchName);
                    GitChange<IGitBranch> branchChange = new GitChange<>(branchName, -1, GitChange.ChangeType.REPOSITORY, "branches", repository, GitChange.ModificationType.LIST_REMOVE, branchResource);
                    notifyBranchChangeListeners(branchChange);
                    return;
                } else if (deleted) {
                    GitChange<IGitBranch> branchChange = new GitChange<>(branchName, -1, GitChange.ChangeType.REPOSITORY, "branches", repository, GitChange.ModificationType.LIST_REMOVE, null);
                    notifyBranchChangeListeners(branchChange);
                    return;
                } else {
                    ArrayList<GitChange<IGitCommit>> changes = new ArrayList<>();
                    if (updateMap.containsKey("commits")) {
                        ArrayList<Map<String, Object>> commits = (ArrayList<Map<String, Object>>) updateMap.get("commits");

                        for (Map<String, Object> commit : commits) {
                            Object commitSha_ = commit.get("id");
                            if (commitSha_ != null) {
                                CommitResource commitResource = new CommitResource(this.gitRestClient, repository, new String[]{branchName}, (String) commitSha_);
                                //immediate loading to not slow down user
                                //commitResource.getComments();
                                changes.add(new GitChange<>(branchName, -1, GitChange.ChangeType.REPOSITORY,"commits", repository, GitChange.ModificationType.LIST_ADD, commitResource));
                            }
                        }
                        for (GitChange<IGitCommit> change : changes) {
                            notifyCommitChangeListeners(change);
                        }
                    }
                    return;
                }

            }
        }

    }

    private void notifyIssueListeners(GitChange<IGitIssue> issueChange) {
        this.issueChangeListeners.forEach(l -> l.update(issueChange));
    }

    public void notifyPullRequestListeners(GitChange<IGitPullRequest> pullRequestChange) {
        this.pullRequestChangeListeners.forEach(l -> l.update(pullRequestChange));
    }

    public void notifyCommentChangeListeners(GitChange<IGitComment> commentChange) {
        this.commentChangeListeners.forEach(l -> l.update(commentChange));
    }

    public void notifyUserListeners(GitChange<IGitUser> userChange) {
        this.userChangeListeners.forEach(l -> l.update(userChange));
    }

    public void notifyBranchChangeListeners(GitChange<IGitBranch> branchChange) {
        this.branchChangeListeners.forEach(l -> l.update(branchChange));
    }

    public void notifyCommitChangeListeners(GitChange<IGitCommit> commitChange) {
        this.commitChangeListeners.forEach(l -> l.update(commitChange));
    }

    public void notifyRepositoryChangeListeners(GitChange<IGitRepository> repositoryChange) {
        this.repositoryChangeListeners.forEach(l -> l.update(repositoryChange));
    }

    public void notifyMapChangeListeners(GitChange<Map<String, Object>> mapChange) {
        this.mapChangeListeners.forEach(l -> l.update(mapChange));
    }

    public void notifyStringListeners(GitChange<String> stringChange) {
        this.stringChangeListeners.forEach(l -> l.update(stringChange));
    }

    public void notifyIntegerListeners(GitChange<Integer> integerChange) {
        this.integerChangeListenerListeners.forEach(l -> l.update(integerChange));
    }

    public void notifyBooleanListeners(GitChange<Boolean> booleanChange) {
        this.booleanChangeListeners.forEach(l -> l.update(booleanChange));
    }

    @Override
    public void addIssueChangeListener(IIssueChangeListener issueChangeListener) {
        this.issueChangeListeners.add(issueChangeListener);
    }

    @Override
    public void addPullRequestChangeListener(IPullRequestChangeListener pullRequestChangeListener) {
        this.pullRequestChangeListeners.add(pullRequestChangeListener);
    }

    @Override
    public void addCommitChangeListener(ICommitChangeListener commitChangeListener) {
        this.commitChangeListeners.add(commitChangeListener);
    }

    @Override
    public void addCommentChangeListener(ICommentChangeListener commentChangeListener) {
        this.commentChangeListeners.add(commentChangeListener);
    }

    @Override
    public void addUserChangeListener(IUserChangeListener userChangeListener) {
        this.userChangeListeners.add(userChangeListener);
    }

    @Override
    public void addBranchChangeListener(IBranchChangeListener branchChangeListener) {
        this.branchChangeListeners.add(branchChangeListener);
    }

    @Override
    public void addRepositoryChangeListener(IRepositoryChangeListener repositoryChangeListener) {
        this.repositoryChangeListeners.add(repositoryChangeListener);
    }

    @Override
    public void addMapChangeListener(IMapChangeListener mapChangeListener) {
        this.mapChangeListeners.add(mapChangeListener);
    }

    @Override
    public void addStringChangeListener(IStringChangeListener stringChangeListener) {
        this.stringChangeListeners.add(stringChangeListener);
    }

    @Override
    public void addIntegerChangeListener(IIntegerChangeListener integerChangeListener) {
        this.integerChangeListenerListeners.add(integerChangeListener);
    }

    @Override
    public void addBooleanChangeListener(IBooleanChangeListener booleanChangeListener) {
        this.booleanChangeListeners.add(booleanChangeListener);
    }

}
