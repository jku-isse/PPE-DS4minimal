package at.jku.isse.designspace.git.service;

import at.jku.isse.designspace.artifactconnector.core.IArtifactProvider;
import at.jku.isse.designspace.artifactconnector.core.IService;
import at.jku.isse.designspace.artifactconnector.core.endpoints.grpc.service.ServiceResponse;
import at.jku.isse.designspace.artifactconnector.core.exceptions.IdentiferFormatException;
import at.jku.isse.designspace.artifactconnector.core.model.BaseElementType;
import at.jku.isse.designspace.artifactconnector.core.idcache.IdCache;
import at.jku.isse.designspace.core.events.Operation;
import at.jku.isse.designspace.core.model.*;
import at.jku.isse.designspace.core.service.ServiceRegistry;
import at.jku.isse.designspace.core.service.WorkspaceService;
import at.jku.isse.designspace.artifactconnector.core.updatememory.UpdateMemory;
import at.jku.isse.designspace.artifactconnector.core.updateservice.UpdateManager;
import at.jku.isse.designspace.git.api.*;
import at.jku.isse.designspace.git.api.core.IGitAPI;
import at.jku.isse.designspace.git.api.github.api.GithubAPI;
import at.jku.isse.designspace.git.model.GitBaseElementType;
import at.jku.isse.designspace.git.updateservice.GitConnection;
import at.jku.isse.designspace.git.updateservice.GitChangePatcher;
import at.jku.isse.designspace.git.updateservice.IGitChangePatcher;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Service("gitService")
@Slf4j
@DependsOn({"controleventengine"})
@ConditionalOnExpression(value = "${git.enabled:false}")
public class GitService implements IGitService, ServiceProvider, IArtifactProvider {

    public final static  String SERVICE_ID_TO_DESIGNSPACE_ID_CACHE_ID = "GitId2DesignspaceIdCache";
    public final static String ISSUE_MIDFIX = "issues";
    public final static String PULL_REQUEST_MIDFIX = "pulls";
    public final static String BRANCH_MIDFIX = "branches";
    public final static String COMMIT_MIDFIX = "commits";
    public final static String TAG_MIDFIX = "tags";

    @Autowired
    private WorkspaceService workspaceService;

    private static boolean isInitialized;
    private static boolean syncDone;

    private boolean fileAreCommitSpecific;
    private boolean loadFileContent;
    private boolean loadCommentsAndLinks;
    private boolean loadPullRequest;
    private boolean loadIssues;
    private boolean initFilesWithHistory;
    private boolean performSync;

    private IGitAPI githubAPI;

    @Autowired
    private UpdateManager updateManager;

    @Autowired
    private UpdateMemory updateMemory;

    private Workspace workspace;
    private IdCache idCache;
    private IGitChangePatcher changePatcher;
    private GitConnection gitConnection;

    private String webhookForwardAddress;
    private ArrayList<String> loadedRepositories;

    public GitService(UpdateManager updateManager, UpdateMemory updateMemory) {
        ServiceRegistry.registerService(this);
        this.updateManager = updateManager;
        this.updateMemory = updateMemory;

        isInitialized = false;
        syncDone = false;
    }

    public String getName(){
        return "GitService";
    }
    public String getVersion(){
        return "1.0.0";
    }
    public int getPriority(){
        return 104;
    }
    public boolean isPersistenceAware(){
        return true;
    }

    @Override
    public void initialize() {
        log.debug("---GIT SERVICE");
        Properties props = new Properties();
        try {
            FileReader reader = new FileReader("./application.properties");
            props.load(reader);
            GithubAPI githubAPI = new GithubAPI(props.getProperty("git.username", "").trim(), props.getProperty("git.token", "").trim());
            this.githubAPI = githubAPI;
            initFlags(props);
            init(workspaceService, updateManager, updateMemory, githubAPI);
        } catch (IOException ioe) {
            log.debug("GIT-SERVICE: The running directory did not contain an application.properties file, Service cannot be initialized!");
        }
    }

    public boolean isIsInitialized() {
        return GitService.isInitialized;
    }

    private void initFlags(Properties props) {
        try {
            this.loadFileContent = Boolean.parseBoolean(props.getProperty("git.files.load", "false"));
            this.fileAreCommitSpecific = Boolean.parseBoolean(props.getProperty("git.files.commit_specific", "false"));
            this.webhookForwardAddress = props.getProperty("git.webhook.forwardAddress", "").trim();
            if (this.webhookForwardAddress != null) {
                this.webhookForwardAddress.trim();
            }
            this.loadCommentsAndLinks = Boolean.parseBoolean(props.getProperty("git.comments_and_links", "true"));
            this.loadedRepositories = parseLoadedRepos(props.getProperty("git.sync.repos", "[]"));
            this.loadPullRequest = Boolean.parseBoolean(props.getProperty("git.sync.load_pullrequests", "true"));
            this.loadIssues = Boolean.parseBoolean(props.getProperty("git.sync.load_issues", "true"));
            this.initFilesWithHistory = Boolean.parseBoolean(props.getProperty("git.sync.load_fileshistory", "true"));
            this.performSync = Boolean.parseBoolean(props.getProperty("git.sync", "false"));
        } catch (Exception e) {
            this.loadFileContent = false;
            this.fileAreCommitSpecific = false;
            this.webhookForwardAddress = "";
            this.loadCommentsAndLinks = true;
            this.loadedRepositories = new ArrayList<>();
            this.loadPullRequest = true;
            this.loadIssues = true;
            this.initFilesWithHistory = true;
            this.performSync = false;
        }
    }

    private ArrayList<String> parseLoadedRepos(String reposToSync) {
        ArrayList<String> repoNames = new ArrayList<>();
        if (reposToSync.length() > 2) {
            String list = reposToSync.substring(1, reposToSync.length() - 1);
            if (list.equals("*")) {
                Optional<List<IGitRepository>> repositories = this.githubAPI.getRepositories();
                if (repositories.isPresent()) {
                    for (IGitRepository repository : repositories.get()) {
                        repoNames.add(repository.getName());
                    }
                }
            } else {
                for (String repoName : list.split(";")) {
                    if (!repoName.contains(",")) {
                        repoNames.add(repoName);
                    } else {
                        log.debug("GIT-SERVICE: Repository Names cannot contain commas");
                        log.debug("GIT-SERVICE: Repos should be provided like this [repo1;repo2;repo3]");
                    }
                }
            }
        }
        return repoNames;
    }

    private void init(WorkspaceService workspaceService, UpdateManager updateManager, UpdateMemory updateMemory, IGitAPI githubAPI) {
        assert githubAPI != null && updateMemory != null && updateManager != null && workspaceService != null;

        this.githubAPI = githubAPI;
        this.workspaceService = workspaceService;
        this.workspace = WorkspaceService.PUBLIC_WORKSPACE;
        this.updateManager = updateManager;
        this.updateMemory = updateMemory;
        this.idCache = new IdCache(this.workspace, SERVICE_ID_TO_DESIGNSPACE_ID_CACHE_ID);
        this.changePatcher = new GitChangePatcher(this);
        
        //ToDo: We need a an interface for updates that can be implemented by every service
        //this.gitConnection = new GitConnection();

        this.workspace.concludeTransaction();

        this.gitConnection = new GitConnection("github", this, this.changePatcher);
        log.debug("Git-Service: Workspace Connection initialized");
        this.updateManager.establishReactiveConnection(this.gitConnection);
        /*
        this.updateManager.getActionObservable().
                filter(action -> action.getServerKind() == ServiceConnection.ServerKind.GIT).distinct()
                .forEach(gitAction -> {
                    this.githubAPI.getChangeFactory().createChanges(gitAction.getUpdatedValue());
                    this.workspace.concludeTransaction();

                    //---------------------This is for the separate thread solution---------------------
                    /*
                    switch (gitAction.getGitActionKind()) {
                        this.githubAPI.getChangeFactory().createChanges(gitAction);

                        case BRANCH: this.changePatcher.applyBranchChange(gitAction.getUpdatedValue()); break;
                        case COMMIT: this.changePatcher.applyCommitChange(gitAction.getUpdatedValue()); break;
                        case COMMENT: this.changePatcher.applyCommentChange(gitAction.getUpdatedValue()); break;
                        case ISSUE: this.changePatcher.applyIssueChange(gitAction.getUpdatedValue()); break;
                        case USER: this.changePatcher.applyUserChange(gitAction.getUpdatedValue()); break;
                        case BOOLEAN: this.changePatcher.applyBooleanChange(gitAction.getUpdatedValue()); break;
                        case PULL: this.changePatcher.applyPullRequestChange(gitAction.getUpdatedValue()); break;
                        case MAP: this.changePatcher.applyMapChange(gitAction.getUpdatedValue()); break;
                        case STRING: this.changePatcher.applyStringChange(gitAction.getUpdatedValue()); break;
                        case INTEGER: this.changePatcher.applyIntegerChange(gitAction.getUpdatedValue()); break;
                    }

                });
        */

        //creating webhooks for all subscribed repositories
        for (String repositoryName : loadedRepositories) {
            log.debug("Git-Service: " + this.githubAPI.createWebhook(repositoryName, this.webhookForwardAddress + "/github/webhook/"));
        }

        for (GitBaseElementType gbet : GitBaseElementType.values()) {
        	gbet.getType(); //init this type so its known in designspace immediately upon starting        	
        }
        
        isInitialized = true;

        if (!this.loadedRepositories.isEmpty() && performSync) {
            log.debug("Git-Service: Sync on start is active (This may take some time)");
            for (String repositoryName : loadedRepositories) {
                log.debug("Git-Service: Downloading " + repositoryName + " to Designspace");
                getRepo(repositoryName, true);
                log.debug("Git-Service: Downloaded " + repositoryName + " to Designspace");
            }
        } else {
            log.debug("Git-Service: Sync on start is inactive (On Demand fetching)");
        }

        syncDone = true;
    }

    private void subscribeToRepository(String name) {
        if (!this.loadedRepositories.contains(name)) {
            this.loadedRepositories.add(name);
            log.debug("Git-Service: " + this.githubAPI.createWebhook(name, this.webhookForwardAddress + "/github/webhook/"));
        }
    }

    private Optional<GitIdentifier> matchIdentifierType(String typeName) {
        return Optional.ofNullable(GitIdentifier.valueOf(typeName));
    }

    @Override
    public ServiceResponse getServiceResponse(String id, String identifierType) {
        checkInitialized();

        GitIdentifier gitIdentifier = GitIdentifier.valueOf(identifierType);
        if (identifierType != null) {
            try {
                Optional<Instance> gitInstance = getArtifact(id, gitIdentifier);
                if (gitInstance.isPresent()) {
                    this.workspace.concludeTransaction();
                    return new ServiceResponse(ServiceResponse.SUCCESS, "Git", "Successful fetch", gitInstance.get().id().toString());
                }
                return new ServiceResponse(ServiceResponse.UNAVAILABLE, "Git", "No artifact with for given identifier", "");
            } catch (IdentiferFormatException e) {
                return new ServiceResponse(ServiceResponse.INVALID, "Git", "The format of the identifier was unexpected", "");
            }
        }

        return new ServiceResponse(ServiceResponse.UNKNOWN, "Git", "The request id was invalid", "");
    }

    @Override
    public ServiceResponse[] getServiceResponse(Set<String> ids, String identifierType) {
        ArrayList<ServiceResponse> serviceResponses = new ArrayList<>();

        for (String id : ids) {
            serviceResponses.add(getServiceResponse(id, identifierType));
        }

        return (ServiceResponse[]) serviceResponses.toArray();
    }
    
	@Override
	public ServiceResponse getServiceResponse(String id, String identifierType, boolean doForceRefetch) {
		return getServiceResponse(id, identifierType);
		//TODO: handle forcefetching
	}

	@Override
	public ServiceResponse[] getServiceResponse(Set<String> ids, String identifierType, boolean doForceRefetch) {
		return getServiceResponse(ids, identifierType);
		//TODO: handle forcefetching
	}

    @Override
    public Optional<Instance> getArtifact(String identifier, GitIdentifier identifierType) throws IdentiferFormatException {
        switch(identifierType) {
            case GitUserId:
                return getUser(identifier);
            case GitIssueId:
                String[] splitted = identifier.split("/");
                if (splitted.length == 3) {
                    String repoName = splitted[0];
                    String issueKey = splitted[2];
                    try {
                        return getIssue(repoName, Integer.parseInt(issueKey));
                    } catch (NumberFormatException ne) {
                        throw new IdentiferFormatException();
                    }
                } else {
                    throw new IdentiferFormatException();
                }
            case GitCommitSha:
                String[] splitted1 = identifier.split("/");
                if (splitted1.length == 3) {
                    String repoName = splitted1[0];
                    String commitSha = splitted1[2];
                    return getCommit(repoName, commitSha);
                }
                log.debug("Git-Service: getArtifact() was given an invalid identifier for idType commit");
                return Optional.empty();
            case GitFile:
                String[] splitted2 = identifier.split("/");
                if (splitted2.length == 3) {
                    String repoName = splitted2[0];
                    String filePath = splitted2[2];
                    return getFile(repoName, filePath);
                }
                throw new IdentiferFormatException();
            case GitProjectName:
                return getProject(identifier);
            case GitRepositoryName:
                return getRepo(identifier, true);
        }


        return Optional.empty();
    }

    @Override
    public Optional<Instance> getRepo(String repoName, boolean withLinks) {
        checkInitialized();

        if (!isInitialized) {
            return Optional.empty();
        }

        Optional<Instance> instance_ = this.searchForInstance(repoName);
        if (instance_.isPresent()) {
            if (isFullyFetched(instance_.get())) {
                return instance_;
            }
        }

        Optional<IGitRepository> repository_ = this.githubAPI.getRepository(repoName);
        if (repository_.isPresent()) {
            Instance repo = transferRepo(repository_.get(), withLinks);
            if (repo == null) {
                log.debug("GIT SERVICE: Something went wrong during the creation of an repository");
                return Optional.empty();
            }
            subscribeToRepository(repoName);
            return Optional.of(repo);
        } else {
            log.debug("GIT SERVICE: The repository you requested does not exist on the server");
        }

        return Optional.empty();
    }

    @Override
    public Optional<Instance> getIssue(String repoName, int issueKey) {
        checkInitialized();

        if (!isInitialized) {
            return Optional.empty();
        }

        String uniqueId = repoName + "/" + ISSUE_MIDFIX + "/" + issueKey;
        Optional<Instance> issueInstance_ = searchForInstance(uniqueId);
        if (issueInstance_.isPresent()) {
            if (isFullyFetched(issueInstance_.get())) {
                return issueInstance_;
            }
        }

        Optional<IGitIssue> issue = this.githubAPI.getIssue(repoName, issueKey);
        if (issue.isPresent()) {
            Instance issueInstance;

            subscribeToRepository(repoName);

            Optional<IGitPullRequest> pullRequest_ = issue.get().getAsPullRequest();
            if (pullRequest_.isPresent()) {
                issueInstance = transferPullRequest(pullRequest_.get());
            } else {
                issueInstance = transferIssue(issue.get());
            }

            if (issueInstance == null) {
                log.debug("GIT SERVICE: Something went wrong during the creation of an issue");
                return Optional.empty();
            }

            return Optional.of(issueInstance);
        } else {
            log.debug("GIT SERVICE: The issue you requested does not exist on the server");
        }

        return Optional.empty();
    }

    @Override
    public Optional<List<Instance>> getIssues(String repoName) {
        checkInitialized();

        if (!isInitialized) {
            return Optional.empty();
        }

        Optional<List<IGitIssue>> issues = this.githubAPI.getIssues(repoName);
        ArrayList<Instance> instances = new ArrayList<>();
        if (issues.isPresent()) {
            subscribeToRepository(repoName);
            for (IGitIssue issue : issues.get()) {
                String uniqueId = repoName + "/" + ISSUE_MIDFIX + "/" + issue.getKey();
                Optional<Instance> existingIssue = searchForInstance(uniqueId);
                if (existingIssue.isEmpty() || !isFullyFetched(existingIssue.get())) {
                    Instance createdInstance = transferIssue(issue);
                    instances.add(createdInstance);
                }
            }
            return Optional.of(instances);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Instance> getPullRequest(String repoName, int key) {
        checkInitialized();

        if (!isInitialized) {
            return Optional.empty();
        }

        String uniqueId = repoName + "/" + PULL_REQUEST_MIDFIX + "/" + key;
        Optional<Instance> pullRequestInstance = searchForInstance(uniqueId);
        if (pullRequestInstance.isPresent()) {
            if (isFullyFetched(pullRequestInstance.get())) {
                return pullRequestInstance;
            }
        }

        Optional<IGitPullRequest> request = this.githubAPI.getPullRequest(repoName, key);
        if (request.isPresent()) {
            subscribeToRepository(repoName);
            Instance requestInstance = transferPullRequest(request.get());
            if (requestInstance == null) {
                log.debug("GIT SERVICE: Something went wrong during the creation of a pull request");
                return Optional.empty();
            }
            return Optional.of(requestInstance);
        } else {
            log.debug("GIT SERVICE: The pull request you requested does not exist on the server");
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<Instance>> getPullRequests(String repoName) {
        checkInitialized();

        if (!isInitialized) {
            return Optional.empty();
        }

        Optional<List<IGitPullRequest>> requests = this.githubAPI.getPullRequests(repoName);
        ArrayList<Instance> instances = new ArrayList<>();
        if (requests.isPresent()) {
            subscribeToRepository(repoName);
            for (IGitPullRequest request : requests.get()) {
                String uniqueId = repoName + "/" + PULL_REQUEST_MIDFIX + "/" + request.getKey();
                Optional<Instance> existingPullRequest = searchForInstance(uniqueId);
                if (existingPullRequest.isEmpty() || !isFullyFetched(existingPullRequest.get())) {
                    Instance createdInstance = transferPullRequest(request);
                    instances.add(createdInstance);
                }
            }
            return Optional.of(instances);
        }
        return Optional.empty();
    }


    @Override
    public Optional<Instance> getUser(String userId) {
        checkInitialized();

        if (!isInitialized) {
            return Optional.empty();
        }

        Optional<Instance> user_ = searchForInstance(userId);
        if (user_.isPresent()) {
            if (isFullyFetched(user_.get())) {
                return user_;
            }
        }

        Optional<IGitUser> user = this.githubAPI.getUser(userId);
        if (user.isPresent()) {
            Instance userInstance = transferUser(user.get());
            if (userInstance == null) {
                log.debug("GIT SERVICE: Something went wrong during the creation of a user");
                return Optional.empty();
            }
            return Optional.of(userInstance);
        } else {
            log.debug("GIT SERVICE: The user you requested does not exist on the server");
        }

        return Optional.empty();
    }

    @Override
    public Optional<Instance> getBranch(String repoName, String branchName) {
        checkInitialized();

        if (!isInitialized) {
            return Optional.empty();
        }

        String uniqueId = repoName + "/" + BRANCH_MIDFIX + "/" + branchName;
        Optional<Instance> branchInstance_ = searchForInstance(uniqueId);
        if (branchInstance_.isPresent()) {
            if (isFullyFetched(branchInstance_.get())) {
                return branchInstance_;
            }
        }

        Optional<IGitBranch> branch = this.githubAPI.getBranch(repoName, branchName);
        if (branch.isPresent()) {
            subscribeToRepository(repoName);
            Instance branchInstance = transferBranch(branch.get());
            if (branchInstance == null) {
                log.debug("GIT SERVICE: Something went wrong during the creation of a branch");
                return Optional.empty();
            }
            return Optional.of(branchInstance);
        } else {
            log.debug("GIT SERVICE: The branch you requested does not exist on the server");
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<Instance>> getBranches(String repoName) {
        checkInitialized();

        if (!isInitialized) {
            return Optional.empty();
        }

        Optional<List<IGitBranch>> branches = this.githubAPI.getBranches(repoName);
        ArrayList<Instance> instances = new ArrayList<>();
        if (branches.isPresent()) {
            subscribeToRepository(repoName);
            for (IGitBranch branch : branches.get()) {
                String uniqueId = repoName + "/" + BRANCH_MIDFIX + "/" + branch.getName();
                Optional<Instance> existingBranch = searchForInstance(uniqueId);
                if (existingBranch.isEmpty() || !isFullyFetched(existingBranch.get())) {
                    Instance createdInstance = transferBranch(branch);
                    instances.add(createdInstance);
                }
            }
            return Optional.of(instances);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Instance> getCommit(String repoName, String sha) {
        checkInitialized();

        if (!isInitialized) {
            return Optional.empty();
        }

        String uniqueId = repoName + "/" + COMMIT_MIDFIX + "/" + sha;
        Optional<Instance> commitInstance_ = searchForInstance(uniqueId);
        if (commitInstance_.isPresent()) {
            if (isFullyFetched(commitInstance_.get())) {
                return commitInstance_;
            }
        }

        Optional<IGitCommit> commit = this.githubAPI.getCommit(repoName, sha);
        if (commit.isPresent()) {
            subscribeToRepository(repoName);
            Instance commitInstance = transferCommit(commit.get());
            if (commitInstance == null) {
                log.debug("GIT SERVICE: Something went wrong during the creation of a commit");
                return Optional.empty();
            }
            return Optional.of(commitInstance);
        } else {
            log.debug("GIT SERVICE: The commit you requested does not exist on the server");
        }

        return Optional.empty();
    }

    @Override
    public Optional<List<Instance>> getCommits(String repoName) {
        checkInitialized();

        if (!isInitialized) {
            return Optional.empty();
        }

        Optional<List<IGitCommit>> commits = this.githubAPI.getCommits(repoName);
        ArrayList<Instance> instances = new ArrayList<>();
        if (commits.isPresent()) {
            subscribeToRepository(repoName);
            for (IGitCommit commit : commits.get()) {
                String uniqueId = repoName + "/" + COMMIT_MIDFIX + "/" + commit.getSha();
                Optional<Instance> existingCommit = searchForInstance(uniqueId);
                if (existingCommit.isEmpty() || !isFullyFetched(existingCommit.get())) {
                    Instance createdInstance = transferCommit(commit);
                    instances.add(createdInstance);
                } else {
                    instances.add(existingCommit.get());
                }
            }
            return Optional.of(instances);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Instance> getProject(String projectName) {
        checkInitialized();

        if (!isInitialized) {
            return Optional.empty();
        }

        Optional<Instance> project_ = searchForInstance(projectName);
        if (project_.isPresent()) {
            if (isFullyFetched(project_.get())) {
                return project_;
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<Instance> getFile(String repositoryName, String contentPath) {
        checkInitialized();

        if (!isInitialized) {
            return Optional.empty();
        }

        Optional<Instance> file_ = searchForInstance(repositoryName + "/" + contentPath);
        if (file_.isPresent()) {
            if (isFullyFetched(file_.get())) {
                return file_;
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<Instance> getTag(String repoName, String tagName) {
        checkInitialized();

        if (!isInitialized) {
            return Optional.empty();
        }

        String uniqueId = repoName + "/" + TAG_MIDFIX + "/" + tagName;
        Optional<Instance> tag = searchForInstance(uniqueId);
        //ToDo: implement the tags
        return tag;
    }

    @Override
    public Optional<Instance> getAllRepos() {
        checkInitialized();
        return Optional.empty();
    }

    @Override
    public Optional<IGitAPI> getAPI() {
        return Optional.of(this.githubAPI);
    }

    @Override
    public boolean isIssueFetched(String repoName, int key) {
        checkInitialized();

        if (!isInitialized) {
            return false;
        }

        String uniqueId = repoName + "/" + ISSUE_MIDFIX + "/" + key;
        return searchForInstance(uniqueId).isPresent();
    }

    public Optional<GitConnection> getUpdateConnection() {
        checkInitialized();
        if (!isInitialized) {
            return Optional.empty();
        }
        return Optional.of(this.gitConnection);
    }

    @Override
    public Instance transferRepo(IGitRepository repo, boolean withLinks) {
        assert repo != null;
        checkInitialized();

        if (!isInitialized) {
            return null;
        }

        Optional<Instance> repo_ = searchForInstance(repo.getName());
        if (repo_.isPresent()) {
            if (isFullyFetched(repo_.get())) {
                return repo_.get();
            }
            return transferRepo(repo, repo_.get(), withLinks);
        }

        String name = repo.getName();
        if (name == null) {
            name = repo.getURI();
        }

        Instance repoInstance = WorkspaceService.createInstance(this.workspace, name, GitBaseElementType.GIT_REPOSITORY.getType());
        return transferRepo(repo, repoInstance, withLinks);
    }

    private Instance transferRepo(IGitRepository repository, Instance repoInstance, boolean withLinks) {
        assert repository != null && repoInstance != null;
        InstanceType instanceType = GitBaseElementType.GIT_REPOSITORY.getType();
        assert repoInstance.getInstanceType() == instanceType;

        repoInstance.getPropertyAsSingle(BaseElementType.FULLY_FETCHED).set(true);
        this.idCache.addEntry(repository.getName(), repoInstance.id());

        repoInstance.getPropertyAsSingle(BaseElementType.ID).set(repository.getName());
        repoInstance.getPropertyAsSingle(BaseElementType.KEY).set(repository.getName());

        repoInstance.getPropertyAsSingle(GitBaseElementType.DESCRIPTION).set(repository.getDescription());
        repoInstance.getPropertyAsSingle(GitBaseElementType.HOME_PAGE).set(repository.getHomepage());
        repoInstance.getPropertyAsSingle(GitBaseElementType.HTML_URL).set(repository.getHTMLUrl());
        repoInstance.getPropertyAsSingle(GitBaseElementType.URI).set(repository.getURI());

        IGitUser owner = repository.getOwner();
        if (owner != null) {
            Instance ownerInstance = transferUser(owner);
            if (ownerInstance != null) {
                repoInstance.getPropertyAsSingle(GitBaseElementType.OWNER).set(ownerInstance);
            }
        }

        ListProperty<Instance> projects = repoInstance.getPropertyAsList(GitBaseElementType.PROJECTS);
        projects.clear();
        for (IGitProject project : repository.getProjects()) {
            Instance projectInstance = transferProject(project);
            if (projectInstance != null) {
                projects.add(projectInstance);
            }
        }

        MapProperty<Instance> branches = repoInstance.getPropertyAsMap(GitBaseElementType.BRANCHES);
        branches.clear();
        for (IGitBranch branch : repository.getBranches()) {
            Instance branchInstance = transferBranch(branch);
            if (branchInstance != null) {
                branches.put(branch.getName(), branchInstance);
            }
        }

        if (withLinks) {

            if (loadIssues) {
                ListProperty<Instance> issues = repoInstance.getPropertyAsList(GitBaseElementType.ISSUES);
                issues.clear();
                for (IGitIssue issue : repository.getIssues()) {
                    Instance issueInstance = transferIssue(issue);
                    if (issueInstance != null) {
                        issues.add(issueInstance);
                    }
                }
            }

            if (loadPullRequest) {
                ListProperty<Instance> pullRequests = repoInstance.getPropertyAsList(GitBaseElementType.PULL_REQUESTS);
                pullRequests.clear();
                for (IGitPullRequest pullRequest : repository.getPullRequests()) {
                    Instance pullRequestInstance = transferPullRequest(pullRequest);
                    if (pullRequestInstance != null) {
                        pullRequests.add(pullRequestInstance);
                    }
                }
            }

            ListProperty<Instance> commits = repoInstance.getPropertyAsList(GitBaseElementType.COMMITS);
            commits.clear();
            for (IGitCommit commit : repository.getCommits()) {
                Instance commitInstance = transferCommit(commit);
                if (commitInstance != null) {
                    commits.add(commitInstance);
                }
            }

        }

        return repoInstance;
    }

    @Override
    public Instance transferBranch(IGitBranch branch) {
        assert branch != null;
        checkInitialized();

        if (!isInitialized) {
            return null;
        }

        Optional<Instance> branch_ = searchForInstance(branch.getOwner() + "/" + BRANCH_MIDFIX + "/" + branch.getName());
        if (branch_.isPresent()) {
            if (isFullyFetched(branch_.get())) {
                return branch_.get();
            }
            return transferBranch(branch, branch_.get());
        }
        Instance branchInstance = WorkspaceService.createInstance(this.workspace, branch.getName(), GitBaseElementType.GIT_BRANCH.getType());
        return transferBranch(branch, branchInstance);
    }

    private Instance transferBranch(IGitBranch branch, Instance branchInstance) {
        assert branch != null && branchInstance != null;
        InstanceType instanceType = GitBaseElementType.GIT_BRANCH.getType();
        assert branchInstance.getInstanceType() == instanceType;

        String ownerName = branch.getOwner();
        String name = branch.getName();

        if (ownerName != null && name != null) {
            String uniqueId = ownerName + "/" + BRANCH_MIDFIX + "/" + name;
            branchInstance.getPropertyAsSingle(BaseElementType.FULLY_FETCHED).set(true);
            this.idCache.addEntry(uniqueId, branchInstance.id());

            branchInstance.getPropertyAsSingle(BaseElementType.ID).set(uniqueId);
            Optional<Instance> repo = getRepo(ownerName, false);
            if (repo.isPresent()) {
                branchInstance.getPropertyAsSingle(GitBaseElementType.OWNER).set(repo.get());
            }
        }

        return branchInstance;
    }

    @Override
    public Instance transferProject(IGitProject project) {
        assert project != null;
        checkInitialized();

        if (!isInitialized) {
            return null;
        }

        Optional<Instance> project_ = searchForInstance(project.getName());
        if (project_.isPresent()) {
            if (isFullyFetched(project_.get())) {
                return project_.get();
            }
            return transferProject(project, project_.get());
        }
        Instance projectInstance = WorkspaceService.createInstance(this.workspace, project.getName(), GitBaseElementType.GIT_PROJECT.getType());
        return transferProject(project, projectInstance);
    }


    private Instance transferProject(IGitProject project, Instance projectInstance) {
        assert project != null && projectInstance != null;
        InstanceType instanceType = GitBaseElementType.GIT_PROJECT.getType();
        assert projectInstance.getInstanceType() == instanceType;

        projectInstance.getPropertyAsSingle(BaseElementType.FULLY_FETCHED).set(true);
        this.idCache.addEntry(project.getName(), projectInstance.id());

        projectInstance.getPropertyAsSingle(BaseElementType.ID).set(project.getName());
        projectInstance.getPropertyAsSingle(BaseElementType.KEY).set(project.getName());

        projectInstance.getPropertyAsSingle(GitBaseElementType.HTML_URL).set(project.getHTMLUrl());
        projectInstance.getPropertyAsSingle(GitBaseElementType.URI).set(project.getURI());
        projectInstance.getPropertyAsSingle(GitBaseElementType.BODY).set(project.getBody());
        projectInstance.getPropertyAsSingle(GitBaseElementType.STATE).set(project.getState());

        IGitUser creator = project.getCreator();
        if (creator != null) {
            Instance creatorInstance = transferUser(creator);
            if (creatorInstance != null) {
                projectInstance.getPropertyAsSingle(GitBaseElementType.CREATOR).set(creatorInstance);
            }
        }

        return projectInstance;
    }

    @Override
    public Instance transferUser(IGitUser user) {
        assert user != null;
        checkInitialized();

        if (!isInitialized) {
            return null;
        }

        Optional<Instance> user_ = searchForInstance(user.getUserId());
        if (user_.isPresent()) {
            if (isFullyFetched(user_.get())) {
                return user_.get();
            }
            return transferUser(user, user_.get());
        }
        Instance userInstance = WorkspaceService.createInstance(this.workspace, user.getUserId(), GitBaseElementType.GIT_USER.getType());
        return transferUser(user, userInstance);
    }

    private Instance transferUser(IGitUser user, Instance userInstance) {
        assert user != null && userInstance != null;
        InstanceType instanceType = GitBaseElementType.GIT_USER.getType();
        assert userInstance.getInstanceType() == instanceType;

        String userId = user.getUserId();

        if (userId != null) {
            userInstance.getPropertyAsSingle(BaseElementType.FULLY_FETCHED).set(true);
            this.idCache.addEntry(user.getUserId(), userInstance.id());

            userInstance.getPropertyAsSingle(BaseElementType.ID).set(userId);
            userInstance.getPropertyAsSingle(BaseElementType.KEY).set(userId);

            userInstance.getPropertyAsSingle(GitBaseElementType.BIO).set(user.getBio());
            userInstance.getPropertyAsSingle(GitBaseElementType.COMPANY).set(user.getCompany());
            userInstance.getPropertyAsSingle(GitBaseElementType.EMAIL).set(user.getEmail());
            userInstance.getPropertyAsSingle(GitBaseElementType.HTML_URL).set(user.getHTMLUrl());
            userInstance.getPropertyAsSingle(GitBaseElementType.URI).set(user.getURI());
            userInstance.getPropertyAsSingle(GitBaseElementType.LOCATION).set(user.getLocation());
            userInstance.getPropertyAsSingle(GitBaseElementType.TYPE).set(user.getType());

            userInstance.getPropertyAsSingle(GitBaseElementType.PRIVATE_REPO_COUNT).set(Integer.toUnsignedLong(user.getPrivateRepoCount()));
            userInstance.getPropertyAsSingle(GitBaseElementType.PUBLIC_REPO_COUNT).set(Integer.toUnsignedLong(user.getPublicRepoCount()));
        }

        return userInstance;
    }

    @Override
    public Instance transferIssue(IGitIssue issue) {
        assert issue != null;
        checkInitialized();

        if (!isInitialized) {
            return null;
        }

        String repository = issue.getRepository();
        Integer issueKey = issue.getKey();

        if (repository != null && issueKey != null) {
            String uniqueId = issue.getRepository() + "/" + ISSUE_MIDFIX + "/" + issue.getKey();
            Optional<Instance> issue_ = searchForInstance(uniqueId);
            if (issue_.isPresent()) {
                if (isFullyFetched(issue_.get())) {
                    return issue_.get();
                }
                return transferIssue(issue, issue_.get());
            }

            String name = issue.getTitle();
            if (name == null) {
                name = uniqueId;
            }

            Instance issueInstance = WorkspaceService.createInstance(this.workspace, name, GitBaseElementType.GIT_ISSUE.getType());
            return transferIssue(issue, issueInstance);
        }
        return null;
    }

    private Instance transferIssue(IGitIssue issue, Instance issueInstance) {
        assert issue != null && issueInstance != null;
        InstanceType instanceType = GitBaseElementType.GIT_ISSUE.getType();
        assert issueInstance.getInstanceType() == instanceType;

        String repository = issue.getRepository();
        Integer issueKey = issue.getKey();

        if (repository != null && issueKey != null) {

            log.debug("GIT-SERVICE: Fetching issue " + issueKey + " from " + repository);

            String uniqueId = repository + "/" + ISSUE_MIDFIX + "/" + issue.getKey();

            issueInstance.getPropertyAsSingle(BaseElementType.FULLY_FETCHED).set(true);
            issueInstance.setName(uniqueId);
            this.idCache.addEntry(uniqueId, issueInstance.id());

            issueInstance.getPropertyAsSingle(BaseElementType.ID).set(uniqueId);
            issueInstance.getPropertyAsSingle(BaseElementType.KEY).set(String.valueOf(issueKey));

            issueInstance.getPropertyAsSingle(GitBaseElementType.BODY).set(issue.getBody());
            issueInstance.getPropertyAsSingle(GitBaseElementType.TITLE).set(issue.getTitle());
            issueInstance.getPropertyAsSingle(GitBaseElementType.HTML_URL).set(issue.getHTMLUrl());
            issueInstance.getPropertyAsSingle(GitBaseElementType.URI).set(issue.getURI());
            issueInstance.getPropertyAsSingle(GitBaseElementType.STATE).set(issue.getState());

            Optional<Instance> repo = getRepo(repository, false);
            if (repo.isPresent()) {
                issueInstance.getPropertyAsSingle(GitBaseElementType.REPOSITORY).set(repo.get());
            }

            Instant closedAt = issue.closedAt();
            if (closedAt != null) {
                issueInstance.getPropertyAsSingle(GitBaseElementType.CLOSED_AT).set(issue.closedAt().toString());
            }

            IGitUser reporter = issue.getReportedBy();
            if (reporter != null) {
                Instance reporterInstance = transferUser(reporter);
                if (reporterInstance != null) {
                    issueInstance.getPropertyAsSingle(GitBaseElementType.REPORTED_BY).set(reporterInstance);
                }
            }

            IGitUser assignee = issue.getAssignee();
            if (assignee != null) {
                Instance assigneeInstance = transferUser(assignee);
                if (assigneeInstance != null) {
                    issueInstance.getPropertyAsSingle(GitBaseElementType.ASSIGNEE).set(assigneeInstance);
                }
            }

            MapProperty<String> projectStatus = issueInstance.getPropertyAsMap(GitBaseElementType.PROJECT_STATUS);
            projectStatus.clear();
            Map<String, String> projectStats = issue.getProjectStatus();
            for (String project : projectStats.keySet()) {
                projectStatus.put(project, projectStats.get(project));
            }

            ListProperty<String> labels = issueInstance.getPropertyAsList(GitBaseElementType.LABELS);
            labels.clear();
            String[] tags = issue.getTags();
            if (tags != null) {
                labels.addAll(Arrays.asList(tags));
            }


            if (loadCommentsAndLinks) {
                ListProperty<Instance> comments = issueInstance.getPropertyAsList(GitBaseElementType.COMMENTS);
                comments.clear();
                for (IGitComment comment : issue.getComments()) {
                    Instance commentInstance = transferComment(comment);
                    if (commentInstance != null) {
                        comments.add(commentInstance);
                    }
                }

                //linked issues should not be deleted as there might already exist
                //issues in this list linked from the other issue
                ListProperty<Instance> linkedIssues = issueInstance.getPropertyAsList(GitBaseElementType.LINKED_ISSUES);
                for (int key : issue.getLinkedIssueKeys()) {
                    String uniqueIssueId = repository + "/" + ISSUE_MIDFIX + "/" + key;
                    Instance linkedIssueInstance = getInstanceOrCreatePlaceholder(uniqueIssueId, String.valueOf(key), uniqueIssueId, GitBaseElementType.GIT_ISSUE);
                    if (linkedIssueInstance != null) {
                        ListProperty<Instance> linksOfOther = linkedIssueInstance.getPropertyAsList(GitBaseElementType.LINKED_ISSUES);
                        linksOfOther.add(issueInstance);
                        linkedIssues.add(linkedIssueInstance);
                    }
                }
            }

        }

        Optional<Instance> repo = getRepo(issue.getRepository(), false);
        if (repo.isPresent()) {
            repo.get().getPropertyAsList(GitBaseElementType.ISSUES).add(issueInstance);
        }

        return issueInstance;
    }

    @Override
    public Instance transferPullRequest(IGitPullRequest pullRequest) {
        assert pullRequest != null;
        checkInitialized();

        if (!isInitialized) {
            return null;
        }

        String correspondingIssueId = pullRequest.getRepository() + "/" + ISSUE_MIDFIX + "/" + pullRequest.getKey();
        String uniqueId = pullRequest.getRepository() + "/" + PULL_REQUEST_MIDFIX + "/" + pullRequest.getKey();

        Optional<Instance> pullRequest_ = searchForInstance(uniqueId);
        if (pullRequest_.isPresent()) {
            if (isFullyFetched(pullRequest_.get())) {
                return pullRequest_.get();
            }
            return transferPullRequest(pullRequest, pullRequest_.get());
        }

        Optional<Instance> issue_ = searchForInstance(correspondingIssueId);
        if (issue_.isPresent()) {
            issue_.get().setInstanceType(GitBaseElementType.GIT_PULL_REQUEST.getType());
            return transferPullRequest(pullRequest, issue_.get());
        }

        Instance issueInstance = WorkspaceService.createInstance(this.workspace, uniqueId, GitBaseElementType.GIT_PULL_REQUEST.getType());
        return transferPullRequest(pullRequest, issueInstance);
    }

    private Instance transferPullRequest(IGitPullRequest pullRequest, Instance pullRequestInstance) {
        assert pullRequest != null && pullRequestInstance != null;
        InstanceType instanceType = GitBaseElementType.GIT_PULL_REQUEST.getType();
        assert pullRequestInstance.getInstanceType() == instanceType;

        String repository = pullRequest.getRepository();
        Integer key = pullRequest.getKey();

        //Issues are a subtype of PullRequest
        transferIssue(pullRequest, pullRequestInstance);

        String uniqueId = repository + "/" + PULL_REQUEST_MIDFIX + "/" + pullRequest.getKey();
        idCache.addEntry(uniqueId, pullRequestInstance.id());
        pullRequestInstance.getPropertyAsSingle(GitBaseElementType.PULL_ID).set(uniqueId);

        if (repository != null && key != null) {

            log.debug("GIT-SERVICE: Fetching pull request " + key + " from " + repository);

            pullRequestInstance.getPropertyAsSingle(BaseElementType.FULLY_FETCHED).set(true);

            if (pullRequest.getFromBranch() != null) {
                pullRequestInstance.getPropertyAsSingle(GitBaseElementType.FROM_BRANCH).set(pullRequest.getFromBranch());
            }

            if (pullRequest.getDestinationBranch() != null) {
                pullRequestInstance.getPropertyAsSingle(GitBaseElementType.TO_BRANCH).set(pullRequest.getDestinationBranch());
            }

            if (pullRequest.getMergedAt() != null) {
                pullRequestInstance.getPropertyAsSingle(GitBaseElementType.MERGED_AT).set(pullRequest.getMergedAt().toString());
            }

            IGitUser mergedBy = pullRequest.getMergedBy();
            if (mergedBy != null) {
                Instance mergedByInstance = transferUser(mergedBy);
                if (mergedByInstance != null) {
                    pullRequestInstance.getPropertyAsSingle(GitBaseElementType.MERGED_BY).set(mergedByInstance);
                }
            }

            IGitCommit baseCommit = pullRequest.getBase();
            if (baseCommit != null) {
                Instance baseCommitInstance = transferCommit(baseCommit);
                if (baseCommitInstance != null) {
                    pullRequestInstance.getPropertyAsSingle(GitBaseElementType.BASE).set(baseCommitInstance);
                }
            }

            IGitCommit headCommit = pullRequest.getHead();
            if (headCommit != null) {
                Instance headCommitInstance = transferCommit(headCommit);
                if (headCommitInstance != null) {
                    pullRequestInstance.getPropertyAsSingle(GitBaseElementType.HEAD).set(headCommitInstance);
                }
            }

            // there might already exist entries in linked commits,
            // because when commits are created those links are entered
            ListProperty<Instance> commits = pullRequestInstance.getPropertyAsList(GitBaseElementType.COMMITS);
            List<IGitCommit> realCommits = pullRequest.getCommits();
            for (IGitCommit commit : realCommits) {
                Instance commitInstance = transferCommit(commit);
                if (commitInstance != null) {
                    commits.add(commitInstance);
                }
            }

            ListProperty<Instance> requestedReviewers = pullRequestInstance.getPropertyAsList(GitBaseElementType.REQUESTED_REVIEWERS);
            requestedReviewers.clear();
            for (IGitUser user : pullRequest.getRequestedReviewers()) {
                Instance userInstance = transferUser(user);
                if (userInstance != null) {
                    requestedReviewers.add(userInstance);
                }
            }

        }

        Optional<Instance> repo = getRepo(pullRequest.getRepository(), false);
        if (repo.isPresent()) {
            repo.get().getPropertyAsList(GitBaseElementType.PULL_REQUESTS).add(pullRequestInstance);
        }

        return pullRequestInstance;
    }

    @Override
    public Instance transferPullRequestReview(IGitPullRequestReview review) {
        assert review != null;
        checkInitialized();

        if (!isInitialized) {
            return null;
        }

        Optional<Instance> review_ = searchForInstance(review.getHTMLUrl());
        if (review_.isPresent()) {
            if (isFullyFetched(review_.get())) {
                return review_.get();
            }
            return transferPullRequestReview(review, review_.get());
        }
        Instance reviewInstance = WorkspaceService.createInstance(this.workspace, "review_" + review.getAuthor().getName(), GitBaseElementType.GIT_REVIEW.getType());
        return transferPullRequestReview(review, reviewInstance);
    }

    private Instance transferPullRequestReview(IGitPullRequestReview pullRequestReview, Instance reviewInstance) {
        assert pullRequestReview != null && reviewInstance != null;
        InstanceType instanceType = GitBaseElementType.GIT_REVIEW.getType();
        assert reviewInstance.getInstanceType() == instanceType;

        reviewInstance.getPropertyAsSingle(BaseElementType.FULLY_FETCHED).set(true);
        this.idCache.addEntry(pullRequestReview.getHTMLUrl(), reviewInstance.id());

        reviewInstance.getPropertyAsSingle(BaseElementType.ID).set(pullRequestReview.getHTMLUrl());
        reviewInstance.getPropertyAsSingle(BaseElementType.KEY).set(String.valueOf(pullRequestReview.getKey()));

        reviewInstance.getPropertyAsSingle(GitBaseElementType.BODY).set(pullRequestReview.getBody());
        reviewInstance.getPropertyAsSingle(GitBaseElementType.HTML_URL).set(pullRequestReview.getHTMLUrl());
        reviewInstance.getPropertyAsSingle(GitBaseElementType.URI).set(pullRequestReview.getURI());

        IGitUser user = pullRequestReview.getAuthor();
        if (user != null) {
            Instance userInstance = transferUser(user);
            if (userInstance != null) {
                reviewInstance.getPropertyAsSingle(GitBaseElementType.AUTHOR).set(userInstance);
            }
        }

        IGitPullRequest pullRequest = pullRequestReview.getOwner();
        if (pullRequest != null) {
            Instance pullRequestInstance = transferPullRequest(pullRequest);
            if (pullRequestInstance != null) {
                reviewInstance.getPropertyAsSingle(GitBaseElementType.PULL_REQUESTS).set(pullRequestInstance);
            }
        }


        return reviewInstance;
    }

    @Override
    public Instance transferCommit(IGitCommit commit) {
        assert commit != null;
        checkInitialized();

        if (!isInitialized) {
            return null;
        }

        String uniqueId = commit.getRepository() + "/" + COMMIT_MIDFIX + "/" + commit.getSha();
        Optional<Instance> commit_ = searchForInstance(uniqueId);
        if (commit_.isPresent()) {
            if (isFullyFetched(commit_.get())) {
                return commit_.get();
            }
            return transferCommit(commit, commit_.get());
        }
        Instance commitInstance = WorkspaceService.createInstance(this.workspace, "commit_" + commit.getAuthor().getUserId(), GitBaseElementType.GIT_COMMIT.getType());
        return transferCommit(commit, commitInstance);
    }

    private Instance transferCommit(IGitCommit commit, Instance commitInstance) {
        assert commit != null && commitInstance != null;
        InstanceType instanceType = GitBaseElementType.GIT_COMMIT.getType();
        assert commitInstance.getInstanceType() == instanceType;

        String repository = commit.getRepository();
        String sha = commit.getSha();

        if (repository!=null && sha!=null) {

            log.debug("GIT-SERVICE: Fetching commit " + sha + " from " + repository);

            String uniqueId = repository + "/" + COMMIT_MIDFIX + "/" + sha;
            commitInstance.getPropertyAsSingle(BaseElementType.FULLY_FETCHED).set(true);
            this.idCache.addEntry(uniqueId, commitInstance.id());

            commitInstance.getPropertyAsSingle(BaseElementType.ID).set(commit.getHTMLUrl());
            commitInstance.getPropertyAsSingle(BaseElementType.KEY).set(commit.getHTMLUrl());

            commitInstance.getPropertyAsSingle(GitBaseElementType.COMMIT_MESSAGE).set(commit.getCommitMessage());
            commitInstance.getPropertyAsSingle(GitBaseElementType.HTML_URL).set(commit.getHTMLUrl());
            commitInstance.getPropertyAsSingle(GitBaseElementType.URI).set(commit.getURI());

            commitInstance.getPropertyAsSingle(GitBaseElementType.TOTAL_ADDITIONS).set(Integer.toUnsignedLong(commit.getTotalAdditions()));
            commitInstance.getPropertyAsSingle(GitBaseElementType.TOTAL_DELETIONS).set(Integer.toUnsignedLong(commit.getTotalDeletions()));

            Optional<Instance> repo = getRepo(repository, false);
            if (repo.isPresent()) {
                commitInstance.getPropertyAsSingle(GitBaseElementType.REPOSITORY).set(repo.get());
            }

            IGitUser author = commit.getAuthor();
            if (author != null) {
                Instance authorInstance = transferUser(author);
                if (authorInstance != null) {
                    commitInstance.getPropertyAsSingle(GitBaseElementType.AUTHOR).set(authorInstance);
                }
            }

            IGitUser committer = commit.getCommitter();
            if (committer != null) {
                Instance committerInstance = transferUser(committer);
                if (committerInstance != null) {
                    commitInstance.getPropertyAsSingle(GitBaseElementType.COMMITTER).set(committerInstance);
                }
            }


            if (repo.isPresent()) {
                Instance repoInstance = repo.get();
                MapProperty<Instance> branches = repoInstance.getPropertyAsMap(GitBaseElementType.BRANCHES);
                ListProperty<String> commitBranches = commitInstance.getPropertyAsList(GitBaseElementType.BRANCHES);

                if (loadFileContent && !fileAreCommitSpecific) {
                    for (String branchName : branches.keySet()) {
                        if (githubAPI.isCommitInBranch(repoInstance.name(), branchName, sha)) {
                            commitBranches.add(branchName);
                        }
                    }
                }

                ListProperty<Instance> files = commitInstance.getPropertyAsList(GitBaseElementType.FILES);
                files.clear();
                for (IGitFile file : commit.getFiles()) {
                    List<String> allBranches = new ArrayList<>(branches.keySet());
                    Instance fileInstance;

                    if (initFilesWithHistory) {
                        fileInstance = transferFile(file, allBranches, true);
                    } else {
                        fileInstance = transferFile(file, allBranches, syncDone);
                    }

                    if (fileInstance != null) {
                        files.add(fileInstance);
                    }
                }
            }

            if (loadCommentsAndLinks) {
                ListProperty<Instance> comments = commitInstance.getPropertyAsList(GitBaseElementType.COMMENTS);
                comments.clear();
                for (IGitComment comment : commit.getComments()) {
                    Instance commentInstance = transferComment(comment);
                    if (commentInstance != null) {
                        comments.add(commentInstance);
                    }
                }

                ListProperty<Instance> linkedIssues = commitInstance.getPropertyAsList(GitBaseElementType.LINKED_ISSUES);
                linkedIssues.clear();
                for (int issueKey : commit.getLinkedIssueKeys()) {
                    String uniqueIssueId = repository + "/" + ISSUE_MIDFIX + "/" + issueKey;
                    Instance linkedIssueInstance = getInstanceOrCreatePlaceholder(uniqueIssueId, String.valueOf(issueKey), uniqueIssueId, GitBaseElementType.GIT_ISSUE);

                    if (linkedIssueInstance != null) {
                        linkedIssueInstance.getPropertyAsList(GitBaseElementType.COMMITS).add(commitInstance);
                        linkedIssues.add(linkedIssueInstance);
                    }
                }
            }
        }

        Optional<Instance> repo = getRepo(commit.getRepository(), false);
        if (repo.isPresent()) {
            repo.get().getPropertyAsList(GitBaseElementType.COMMITS).add(commitInstance);
        }

        return commitInstance;
    }

    @Override
    public Instance transferComment(IGitComment comment) {
        assert comment != null;
        checkInitialized();

        if (!isInitialized) {
            return null;
        }

        Optional<Instance> comment_ = searchForInstance(comment.getURI());
        if (comment_.isPresent()) {
            if (isFullyFetched(comment_.get())) {
                return comment_.get();
            }
            return transferComment(comment, comment_.get());
        }
        Instance commentInstance = WorkspaceService.createInstance(this.workspace, "comment_" + comment.getAuthor().getUserId(), GitBaseElementType.GIT_COMMENT.getType());
        return transferComment(comment, commentInstance);
    }

    private Instance transferComment(IGitComment comment, Instance commentInstance) {
        assert comment != null && commentInstance != null;
        InstanceType instanceType = GitBaseElementType.GIT_COMMENT.getType();
        assert commentInstance.getInstanceType() == instanceType;

        commentInstance.getPropertyAsSingle(BaseElementType.FULLY_FETCHED).set(true);
        this.idCache.addEntry(comment.getURI(), commentInstance.id());

        commentInstance.getPropertyAsSingle(BaseElementType.ID).set(comment.getURI());
        commentInstance.getPropertyAsSingle(BaseElementType.KEY).set(String.valueOf(comment.getKey()));

        commentInstance.getPropertyAsSingle(GitBaseElementType.BODY).set(comment.getBody());
        commentInstance.getPropertyAsSingle(GitBaseElementType.HTML_URL).set(comment.getHTMLUrl());
        commentInstance.getPropertyAsSingle(GitBaseElementType.URI).set(comment.getURI());

        IGitUser author = comment.getAuthor();
        if (author != null) {
            Instance authorInstance = transferUser(author);
            if (authorInstance != null) {
                commentInstance.getPropertyAsSingle(GitBaseElementType.AUTHOR).set(authorInstance);
            }
        }

        return commentInstance;
    }

    public Instance transferFile(IGitFile file, List<String> branches, boolean update) {
        assert file != null;
        checkInitialized();

        if (!isInitialized) {
            return null;
        }

        String fileName = file.getName();
        Optional<Instance> file_ = searchForInstance(fileName);
        if (file_.isPresent()) {
            if (isFullyFetched(file_.get()) && !update) {
                return file_.get();
            }
            return transferFile(file, branches, file_.get());
        }
        Instance fileInstance = WorkspaceService.createInstance(this.workspace, fileName, GitBaseElementType.GIT_FILE.getType());
        return transferFile(file, branches, fileInstance);
    }

    private Instance transferFile(IGitFile file, List<String> branches, Instance fileInstance) {
        assert file != null && fileInstance != null;
        InstanceType instanceType = GitBaseElementType.GIT_FILE.getType();
        assert fileInstance.getInstanceType() == instanceType;

        fileInstance.getPropertyAsSingle(BaseElementType.FULLY_FETCHED).set(true);
        log.debug("GIT-SERVICE: Fetching file " + file.getName());

        if (this.fileAreCommitSpecific) {
            this.idCache.addEntry(file.getFullPath(), fileInstance.id());
        } else {
            this.idCache.addEntry(file.getName(), fileInstance.id());
        }

        fileInstance.getPropertyAsSingle(BaseElementType.ID).set(file.getName());
        fileInstance.getPropertyAsSingle(BaseElementType.KEY).set(file.getName());
        fileInstance.getPropertyAsSingle(GitBaseElementType.FULL_PATH).set(file.getFullPath());

        if (loadFileContent) {
            MapProperty<String> contents = fileInstance.getPropertyAsMap(GitBaseElementType.CONTENT);
            if (!syncDone && !initFilesWithHistory) {
                //in case the file history is not of importance
                //we only fetch the current version of a file in a branch once
                //instead fetching a new version for every commit
                for (String branch : branches) {
                    String content = file.getContent(branch);
                    contents.put(branch, content);
                }
            } else {
                //here we fetch a new version of the file for every commit.
                //This is necessary for updating, but also in case the entire
                //file history should be contained in the Designspace
                String content = file.getContent();
                if (branches != null) {
                    for (String branch : branches) {
                        contents.put(branch, content);
                    }
                } else if (fileAreCommitSpecific) {
                    //in case files are commit specific,
                    //every file is seen as a different instance
                    //than the same file from another commit.
                    contents.put("content", content);
                }
            }
        }

        return fileInstance;
    }

    @Override
    public Instance getInstanceOrCreatePlaceholder(String id, String key, String name, GitBaseElementType type) {
        assert id != null && key != null && name != null;
        checkInitialized();

        if (!isInitialized) {
            return null;
        }

        Optional<Instance> repo = searchForInstance(id);
        if (repo.isPresent()) {
            return repo.get();
        } else {
            Instance newInstance = WorkspaceService.createInstance(this.workspace, name, type.getType());
            newInstance.getPropertyAsSingle(GitBaseElementType.ID).set(id);
            newInstance.getPropertyAsSingle(GitBaseElementType.KEY).set(key);
            newInstance.getPropertyAsSingle(BaseElementType.FULLY_FETCHED).set(false);
            this.idCache.addEntry(name, newInstance.id());
            return newInstance;
        }
    }

    private boolean isFullyFetched(Instance instance) {
        assert instance != null;
        if (instance.hasProperty(BaseElementType.FULLY_FETCHED)) {
            Property<?> property = instance.getProperty(BaseElementType.FULLY_FETCHED);
            if (property != null) {
                Object fullyFetched = property.get();
                if (fullyFetched != null) {
                    return (boolean) fullyFetched;
                }
            }
        }
        return false;
    }

    @Override
    public Optional<Instance> searchForInstance(String id) {
        assert id != null;
        checkInitialized();

        if (!isInitialized) {
            return Optional.empty();
        }

        Id designspaceId = idCache.getDesignspaceId(id);
        if(designspaceId == null) {
            return Optional.empty();
        }
        return Optional.of(workspace.findElement(designspaceId));
    }

    public UpdateManager getUpdateManager() {
        return this.updateManager;
    }

    private void checkInitialized() {
        if (!isInitialized) {
            initialize();
        }
    }

    @Override
    public InstanceType getArtifactInstanceType() {
        return GitBaseElementType.GIT_ISSUE.getType();
    }

	@Override
	public Set<InstanceType> getArtifactInstanceTypes() {
		return Set.of(GitBaseElementType.GIT_ISSUE.getType());
	}

	@Override
	public Map<InstanceType, List<String>> getSupportedIdentifier() {
		return Map.of(GitBaseElementType.GIT_ISSUE.getType(), List.of(GitBaseElementType.GIT_ISSUE.getType().toString().toLowerCase()));
	}



	@Override
	public void handleServiceRequest(Workspace workspace, Collection<Operation> operations) {
		// TODO Auto-generated method stub		
	}
    
    
}
