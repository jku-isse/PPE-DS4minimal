package at.jku.isse.designspace.git.service;

import at.jku.isse.designspace.git.api.*;
import at.jku.isse.designspace.git.api.core.IGitAPI;
import at.jku.isse.designspace.git.api.core.webhookparser.IChangeFactory;
import at.jku.isse.designspace.git.api.github.restclient.IGithubRestClient;

import java.time.Instant;
import java.util.*;

public class GitMockApiManager implements IGitAPI {

    public Map<String, IGitRepository> repoStorage;
    public Map<String, IGitProject> projects;
    public Map<String, IGitUser> users;

    public GitMockApiManager() {
        this.repoStorage = new HashMap<>();
        this.projects = new HashMap<>();
        this.projects = new HashMap<>();
        this.users = new HashMap<>();

        //-----------------------MOCK USERS-----------------------
        IGitUser user = new User("dyson51", "Miles Dyson", "miles.dyson@cyberdyne.com", "Los Angeles", "Cause for Judgement Day", "Cyberdyne Systems", "http://github.com/MilesDyson", "sampleType", 10, 2);
        IGitUser user1 = new User("connor75", "John Connor", "none", "Nevada", "Constantly targeted for termination and fond of open source", "Human Resistance", "http://github.com/JohnConnor", "sampleType", 33, 4);
        IGitUser user2 = new User("davie02","Dave Reviewer", "none", "New York", "Very active open source member, loves reviewing stuff", "Human Resistance", "http://github.com/DaveReviewer", "sampleType", 33, 4);

        this.users.put(user.getUserId(), user);
        this.users.put(user1.getUserId(), user1);
        this.users.put(user2.getUserId(), user2);

        //----------------------MOCK PROJECT----------------------
        IGitProject project = new Project("Skynet_General", "This is test content", user, "In Progress", "http://www.skynet_general.at");
        ArrayList<IGitProject> projects = new ArrayList<>();
        projects.add(project);

        this.projects.put(project.getName(), project);

        //-------------------------FILES--------------------------
        IGitFile file = new File("test.txt", "C:/Users/User/Designspace/test.txt");
        IGitFile file1 = new File("test1.txt", "C:/Users/User/Designspace/test1.txt");
        IGitFile file2 = new File("test2.txt", "C:/Users/User/Designspace/test2.txt");
        IGitFile file3 = new File("test3.txt", "C:/Users/User/Designspace/test3.txt");

        //-----------------------REPOSITORY-----------------------
        Repository repository = new Repository("SkynetCPU", "A repo containing all plans of the Skynet CPU", "http://github.com/SkynetCPU", "http://github.com/SkynetCPU", "http://github.com/SkynetCPU", user, null, null, projects, null, null);
        Repository repository1 = new Repository("T800Hack", "A repo containing John Connors T800 hack", "http://github.com/T800Hack", "http://github.com/T800Hack", "http://github.com/T800Hack", user1, null, null, projects, null, null);

        //------------------------BRANCHES-------------------------
        IGitBranch branch1_r = new Branch(1, repository.getName(), "main", "http://github.com/SkynetCPU/branch/main");
        IGitBranch branch1_r1 = new Branch(1, repository.getName(), "main", "http://github.com/T800Hack/branch/main");
        IGitBranch branch2_r1 = new Branch(2, repository.getName(), "main", "http://github.com/T800Hack/branch/main");

        //-----------------------MOCK ISSUES-----------------------
        IGitIssue issue_r = new Issue(1, "Some thing is broken", "TestIssue1", "http://github.com/SkynetCPU/issue/1", Instant.now(), null, user1, user1, repository.getName(), null);
        IGitIssue issue1_r = new Issue(1, "Something is not right", "TestIssue2", "http://github.com/T800Hack/issue/1", Instant.now(), null, user1, user1, repository1.getName(), null);
        IGitIssue issue2_r = new Issue(2, "Something wen completely wrong", "TestIssue3", "http://github.com/SkynetCPU/issue/2", Instant.now(), null, user1, user1, repository.getName(),null);

        IGitIssue issue_r1 = new Issue(3, "Obsessive nail biting", "Issue1", "http://github.com/SkynetCPU/issue/3", Instant.now(), null, user, user, repository.getName(),null);
        IGitIssue issue1_r1 = new Issue(2, "Civilians were hurt", "Issue2", "http://github.com/T800Hack/issue/2", Instant.now(), null, user, user, repository1.getName(),null);
        IGitIssue issue2_r1 = new Issue(4, "The singing does not stop", "Issue3", "http://github.com/SkynetCPU/issue/4", Instant.now(), null, user, user, repository.getName(),null);

        //------------------------COMMITS-------------------------
        IGitCommit commit_r = new Commit(repository.getName(), "ah123Bjdfk", "http://github.com/SkynetCPU/commit/1", "This is a test commit", 120, 300, List.of(file), user, user, null);
        IGitCommit commit1_r = new Commit(repository.getName(), "ah123Esdgh", "http://github.com/SkynetCPU/commit/2", "This is another test commit", 120, 300, List.of(file1), user, user, null);

        IGitCommit commit_r1 = new Commit(repository1.getName(), "ah123Cjdsu", "http://github.com/T800Hack/commit/1", "This fixes the the singing issue", 120, 300, List.of(file2), user, user, null);
        IGitCommit commit1_r1 = new Commit(repository1.getName(), "ah123Dztr", "http://github.com/T800Hack/commit/2", "This fixes the nail biting ", 120, 300, List.of(file3), user, user, null);

        //---------------------PULL REQUESTS----------------------
        IGitPullRequest pullRequest = new PullRequest(1, "testRequest", "http://github.com/Skynet/pullreqest/1", true, Instant.now(), user, user, commit_r, commit1_r, List.of(user2), List.of(issue_r, issue1_r, issue2_r), repository.getName());
        IGitPullRequest pullRequest1 = new PullRequest(1, "minorFixRequest", "http://github.com/T800Hack/pullreqest/1", true, Instant.now(), user1, user1, commit_r1, commit1_r1, List.of(user2), List.of(issue_r1, issue2_r1), repository1.getName());

        repository.addIssue(issue_r);
        repository.addIssue(issue1_r);
        repository.addIssue(issue2_r);
        repository.addCommit(commit1_r);
        repository.addCommit(commit_r);
        repository.addPullRequest(pullRequest);
        repository.addBranch(branch1_r);

        repository1.addIssue(issue_r1);
        repository1.addIssue(issue1_r1);
        repository1.addIssue(issue2_r1);
        repository1.addCommit(commit_r1);
        repository1.addCommit(commit1_r1);
        repository1.addPullRequest(pullRequest1);
        repository1.addBranch(branch1_r1);
        repository1.addBranch(branch2_r1);

        this.repoStorage.put(repository.getName(), repository);
        this.repoStorage.put(repository1.getName(), repository1);
    }

    @Override
    public Optional<IGitIssue> getIssue(String repositoryName, int key) {
        IGitRepository repository = this.repoStorage.get(repositoryName);
        if (repository != null) {
            return repository.getIssues().stream().filter(issue -> issue.getKey() == key).findAny();
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<IGitIssue>> getIssues(String repositoryName) {
        IGitRepository repository = this.repoStorage.get(repositoryName);
        if (repository != null) {
            return Optional.of(repository.getIssues());
        }
        return Optional.empty();
    }

    @Override
    public Optional<IGitPullRequest> getPullRequest(String repositoryName, int key) {
        IGitRepository repository = this.repoStorage.get(repositoryName);
        if (repository != null) {
            return repository.getPullRequests().stream().filter(req -> req.getKey() == key).findAny();
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<IGitPullRequest>> getPullRequests(String repositoryName) {
        IGitRepository repository = this.repoStorage.get(repositoryName);
        if (repository != null) {
            return Optional.of(repository.getPullRequests());
        }
        return Optional.empty();
    }

    @Override
    public Optional<IGitCommit> getCommit(String repositoryName, String sha) {
        IGitRepository repository = this.repoStorage.get(repositoryName);
        if (repository != null) {
            return repository.getCommits().stream().filter(commit -> commit.getSha() == sha).findAny();
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<IGitCommit>> getCommits(String repositoryName) {
        IGitRepository repository = this.repoStorage.get(repositoryName);
        if (repository != null) {
            return Optional.of(repository.getCommits());
        }
        return Optional.empty();
    }

    @Override
    public Optional<IGitFile> getFile(String s, String s1) {
        return Optional.empty();
    }

    @Override
    public Optional<IGitBranch> getBranch(String repositoryName, String branchName) {
        IGitRepository repository = this.repoStorage.get(repositoryName);
        if (repository != null) {
            return repository.getBranches().stream().filter(branch -> branch.getName().equals(branchName)).findAny();
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<IGitBranch>> getBranches(String repositoryName) {
        IGitRepository repository = this.repoStorage.get(repositoryName);
        if (repository != null) {
            return Optional.of(repository.getBranches());
        }
        return Optional.empty();
    }

    @Override
    public Optional<IGitProject> getProject(String projectName) {
        return Optional.ofNullable(this.projects.get(projectName));
    }

    @Override
    public Optional<IGitRepository> getRepository(String repositoryName) {
        return Optional.ofNullable(this.repoStorage.get(repositoryName));
    }

    @Override
    public Optional<List<IGitRepository>> getRepositories() {
        return Optional.empty();
    }

    @Override
    public Optional<IGitUser> getUser(String userId) {
        return Optional.ofNullable(this.users.get(userId));
    }

    @Override
    public Optional<List<IGitComment>> getIssueComments(String repositoryName, int key) {
        Optional<IGitIssue> issue = this.getIssue(repositoryName, key);
        if (issue.isPresent()) {
            return Optional.ofNullable(issue.get().getComments());
        }
        return Optional.empty();
    }

    @Override
    public IChangeFactory getChangeFactory() {
        return null;
    }

    @Override
    public IGithubRestClient getGitRestClient() {
        return null;
    }

    @Override
    public boolean isCommitInBranch(String s, String s1, String s2) {
        return false;
    }

    @Override
    public String createWebhook(String s, String s1) {
        return null;
    }

    @Override
    public String deleteWebhook(String s, String s1) {
        return null;
    }

    @Override
    public String setWebhookActive(String s, String s1, boolean b) {
        return null;
    }


    class Branch implements IGitBranch {

        private int key;
        private String repositoryName;
        private String name;
        private String commit;
        private String protectionURL;

        public Branch(int key, String repositoryName, String name, String commit) {
            this.key = key;
            this.repositoryName = repositoryName;
            this.name = name;
            this.commit = commit;
        }

        @Override
        public int getKey() {
            return key;
        }

        @Override
        public String getOwner() {
            return repositoryName;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getHeadSha() {
            return commit;
        }

    }

    class Repository implements IGitRepository {

        private String name;
        private String description;
        private String homepage;
        private String html_url;
        private IGitUser owner;
        private String git_url;
        private List<IGitIssue> issues;
        private List<IGitPullRequest> pullRequests;
        private List<IGitProject> projects;
        private List<IGitCommit> commits;
        private List<IGitBranch> branches;

        public Repository(String name, String description, String homepage, String html_url, String git_url,
                          IGitUser owner, List<IGitIssue> issues, List<IGitPullRequest> pullRequests, List<IGitProject> projects,
                          List<IGitCommit> commits, List<IGitBranch> branches) {
            this.name = name;
            this.description = description;
            this.homepage = homepage;
            this.html_url = html_url;
            this.owner = owner;
            this.git_url = git_url;
            this.issues = issues;
            this.pullRequests = pullRequests;
            this.projects = projects;
            this.commits = commits;
            this.branches = branches;

            if (this.issues == null) {
                this.issues = new ArrayList<>();
            }

            if (this.commits == null) {
                this.commits = new ArrayList<>();
            }

            if (this.pullRequests == null) {
                this.pullRequests = new ArrayList<>();
            }

            if (this.branches == null) {
                this.branches = new ArrayList<>();
            }

            if (this.projects == null) {
                this.projects = new ArrayList<>();
            }

        }

        void addIssue(IGitIssue issue) {
            this.issues.add(issue);
        }

        void addCommit(IGitCommit commit) {
            this.commits.add(commit);
        }

        void addPullRequest(IGitPullRequest pullRequest) {
            this.pullRequests.add(pullRequest);
        }

        void addProject(IGitProject project) {
            this.projects.add(project);
        }

        void addBranch(IGitBranch branch) {
            this.branches.add(branch);
        }

        @Override
        public String getDescription() {
            return this.description;
        }

        @Override
        public String getHomepage() {
            return this.homepage;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getHTMLUrl() {
            return this.html_url;
        }

        @Override
        public String getURI() {
            return this.html_url;
        }

        @Override
        public String getGitUrl() {
            return this.git_url;
        }

        @Override
        public IGitUser getOwner() {
            return this.owner;
        }

        @Override
        public List<IGitTag> getTags() {
            return new ArrayList<>();
        }

        @Override
        public List<IGitIssue> getIssues() {
            return this.issues;
        }

        @Override
        public List<IGitPullRequest> getPullRequests() {
            return this.pullRequests;
        }

        @Override
        public List<IGitProject> getProjects() {
            return this.projects;
        }

        @Override
        public List<IGitCommit> getCommits() {
            return this.commits;
        }

        @Override
        public List<IGitBranch> getBranches() {
            return this.branches;
        }

    }

    class Issue implements IGitIssue {

        private int key;

        private List<IGitComment> comments;
        private String body;
        private String title;
        private String html_url;
        private Instant closedAt;
        private IGitPullRequest linkedPullRequest;
        private IGitUser reportedBy;
        private IGitUser closedBy;
        private String repositoryName;
        private String projectName;

        public Issue(int key, String body, String title, String html_url, Instant closedAt,
                     IGitPullRequest linkedPullRequest, IGitUser reportedBy, IGitUser closedBy, 
                     String repositoryName, List<IGitComment> comments) {
            this.key = key;
            this.body = body;
            this.title = title;
            this.html_url = html_url;
            this.closedAt = closedAt;
            this.linkedPullRequest = linkedPullRequest;
            this.reportedBy = reportedBy;
            this.closedBy = closedBy;
            this.repositoryName = repositoryName;
            this.comments = comments;

            if (this.comments == null) {
                this.comments = new ArrayList<>();
            }
        }

        @Override
        public int getKey() {
            return this.key;
        }

        @Override
        public String getId() {
            return null;
        }

        @Override
        public String getBody() {
            return this.body;
        }

        @Override
        public String getTitle() {
            return this.title;
        }

        @Override
        public String getHTMLUrl() {
            return this.html_url;
        }

        @Override
        public String getURI() {
            return this.html_url;
        }

        @Override
        public String getState() {
            return "state";
        }

        @Override
        public Map<String, String> getProjectStatus() {
            return new HashMap<>();
        }

        @Override
        public Instant closedAt() {
            return this.closedAt;
        }

        @Override
        public String[] getTags() {
            return new String[0];
        }

        @Override
        public IGitUser getAssignee() {
            return this.reportedBy;
        }

        @Override
        public IGitUser getReportedBy() {
            return this.reportedBy;
        }

        @Override
        public String getRepository() {
            return repositoryName;
        }

        @Override
        public List<IGitComment> getComments() {
            return this.comments;
        }

        @Override
        public List<IGitCommit> getCommits() {
            return new ArrayList<>();
        }

        @Override
        public int[] getLinkedIssueKeys() {
            return new int[0];
        }

        @Override
        public Optional<IGitPullRequest> getAsPullRequest() {
            return Optional.empty();
        }

        @Override
        public IGitRepository getRepositoryObject() {
            return null;
        }

        @Override
        public List<IGitIssue> getLinkedArtifacts() {
            return null;
        }
    }

    class User implements IGitUser {

        private String userId;
        private String name;
        private String email;
        private String location;
        private String bio;
        private String company;
        private String html_url;
        private String type;
        private int publicRepoCount;
        private int privateRepoCount;

        public User(String userId, String name, String email, String location, String bio, String company,
                    String html_url, String type, int publicRepoCount, int privateRepoCount) {
            this.userId = userId;
            this.name = name;
            this.email = email;
            this.location = location;
            this.bio = bio;
            this.company = company;
            this.html_url = html_url;
            this.type = type;
            this.publicRepoCount = publicRepoCount;
            this.privateRepoCount = privateRepoCount;
        }

        @Override
        public String getUserId() {
            return this.userId;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getEmail() {
            return this.email;
        }

        @Override
        public String getLocation() {
            return this.location;
        }

        @Override
        public String getBio() {
            return this.bio;
        }

        @Override
        public String getCompany() {
            return this.company;
        }

        @Override
        public String getHTMLUrl() {
            return this.html_url;
        }

        @Override
        public String getURI() {
            return this.html_url;
        }

        @Override
        public String getType() {
            return this.type;
        }

        @Override
        public int getPublicRepoCount() {
            return this.publicRepoCount;
        }

        @Override
        public int getPrivateRepoCount() {
            return this.privateRepoCount;
        }
    }

    class Project implements IGitProject {

        private String name;
        private String body;
        private IGitUser creator;
        private String state;
        private String html_url;

        public Project(String name, String body, IGitUser creator, String state, String html_url) {
            this.name = name;
            this.body = body;
            this.creator = creator;
            this.state = state;
            this.html_url = html_url;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getBody() {
            return this.body;
        }

        @Override
        public IGitUser getCreator() {
            return this.creator;
        }

        @Override
        public String getState() {
            return this.state;
        }

        @Override
        public String getHTMLUrl() {
            return this.html_url;
        }

        @Override
        public String getURI() {
            return this.html_url;
        }
    }

    class PullRequest implements IGitPullRequest {

        private int key;
        private String name;
        private String html_url;
        private boolean isMergeable;
        private Instant mergedAt;
        private IGitUser reportedBy;
        private IGitUser mergedBy;
        private IGitCommit head;
        private IGitCommit base;
        private List<IGitUser> requestedReviewers;
        private List<IGitIssue> linkedIssues;
        private String repositoryName;

        public PullRequest(int key, String name, String html_url, boolean isMergeable, Instant mergedAt, IGitUser reportedBy, IGitUser mergedBy,
                           IGitCommit head, IGitCommit base, List<IGitUser> requestedReviewers, List<IGitIssue> linkedIssues, String repositoryName) {
            this.key = key;
            this.name = name;
            this.html_url = html_url;
            this.isMergeable = isMergeable;
            this.mergedAt = mergedAt;
            this.reportedBy = reportedBy;
            this.mergedBy = mergedBy;
            this.head = head;
            this.base = base;
            this.requestedReviewers = requestedReviewers;
            this.linkedIssues = linkedIssues;
            this.repositoryName = repositoryName;

            if (this.linkedIssues == null) {
                this.linkedIssues = new ArrayList<>();
            }

            if (this.requestedReviewers == null) {
                this.requestedReviewers = new ArrayList<>();
            }
        }

        @Override
        public int getKey() {
            return key;
        }

        @Override
        public String getId() {
            return null;
        }

        @Override
        public String getBody() {
            return "This is a typical pull request";
        }

        @Override
        public String getTitle() {
            return name;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getHTMLUrl() {
            return this.html_url;
        }

        @Override
        public String getURI() {
            return this.html_url;
        }

        @Override
        public String getState() {
            return "state";
        }

        @Override
        public Map<String, String> getProjectStatus() {
            return new HashMap<>();
        }

        @Override
        public Instant closedAt() {
            return Instant.now();
        }

        @Override
        public String[] getTags() {
            return new String[0];
        }

        @Override
        public IGitUser getAssignee() {
            return this.reportedBy;
        }

        @Override
        public IGitUser getReportedBy() {
            return reportedBy;
        }

        @Override
        public Instant getMergedAt() {
            return this.mergedAt;
        }

        @Override
        public IGitUser getMergedBy() {
            return mergedBy;
        }

        @Override
        public String getFromBranch() {
            return null;
        }

        @Override
        public String getDestinationBranch() {
            return null;
        }

        @Override
        public IGitCommit getHead() {
            return this.head;
        }

        @Override
        public IGitCommit getBase() {
            return this.base;
        }

        @Override
        public String getRepository() {
            return repositoryName;
        }

        @Override
        public List<IGitComment> getComments() {
            return new ArrayList<>();
        }

        @Override
        public int[] getLinkedIssueKeys() {
            return new int[0];
        }

        @Override
        public Optional<IGitPullRequest> getAsPullRequest() {
            return Optional.empty();
        }

        @Override
        public IGitRepository getRepositoryObject() {
            return null;
        }

        @Override
        public List<IGitIssue> getLinkedArtifacts() {
            return null;
        }

        @Override
        public List<IGitCommit> getCommits() {
            return new ArrayList<>();
        }

        @Override
        public List<IGitUser> getRequestedReviewers() {
            return this.requestedReviewers;
        }

    }

    class File implements IGitFile {

        private String name;
        private String fullPath;

        public File(String name, String fullPath) {
            this.name = name;
            this.fullPath = fullPath;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getRepository() {
            return "repository";
        }

        @Override
        public String getLocalPath() {
            return null;
        }

        @Override
        public String getContent() {
            return "Lorem ipsum dolor est";
        }

        @Override
        public String getContent(String s) {
            return null;
        }

        @Override
        public String getFullPath() {
            return this.fullPath;
        }
    }

    class Commit implements IGitCommit {

        private String repositoryName;
        private String html_url;
        private String commitMessage;
        private String sha;
        private int totalDeletions;
        private int totalAdditions;
        private List<IGitFile> files;
        private List<IGitComment> comments;
        private IGitUser author;
        private IGitUser committer;

        public Commit(String repositoryName, String sha, String html_url, String commitMessage, int totalDeletions,
                      int totalAdditions, List<IGitFile> files, IGitUser author, IGitUser committer, List<IGitComment> comments) {
            this.repositoryName = repositoryName;
            this.sha = sha;
            this.html_url = html_url;
            this.commitMessage = commitMessage;
            this.totalDeletions = totalDeletions;
            this.totalAdditions = totalAdditions;
            this.files = files;
            this.comments = comments;
            this.author = author;
            this.committer = committer;

            if (files == null) {
                this.files = new ArrayList<>();
            }

            if (this.comments == null) {
                this.comments = new ArrayList<>();
            }
        }

        @Override
        public String getSha() {
            return sha;
        }

        @Override
        public String getRepository() {
            return repositoryName;
        }

        @Override
        public String getHTMLUrl() {
            return this.html_url;
        }

        @Override
        public String getURI() {
            return this.html_url;
        }

        @Override
        public String getCommitMessage() {
            return this.commitMessage;
        }

        @Override
        public int getTotalDeletions() {
            return this.totalDeletions;
        }

        @Override
        public int getTotalAdditions() {
            return this.totalAdditions;
        }

        @Override
        public String[] getBranches() {
            return new String[0];
        }

        @Override
        public List<IGitFile> getFiles() {
            return this.files;
        }

        @Override
        public IGitUser getAuthor() {
            return this.author;
        }

        @Override
        public IGitUser getCommitter() {
            return this.committer;
        }

        @Override
        public List<IGitComment> getComments() {
            return this.comments;
        }

        @Override
        public int[] getLinkedIssueKeys() {
            return new int[0];
        }
    }
}
