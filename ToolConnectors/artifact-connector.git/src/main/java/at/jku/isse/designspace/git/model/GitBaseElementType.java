package at.jku.isse.designspace.git.model;

import at.jku.isse.designspace.artifactconnector.core.converter.IElementTypeGetter;
import at.jku.isse.designspace.artifactconnector.core.model.BaseElementType;

import at.jku.isse.designspace.core.model.*;
import at.jku.isse.designspace.core.service.WorkspaceService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

@Slf4j
public enum GitBaseElementType implements IElementTypeGetter {

    GIT_REPOSITORY("git_repository") {
        @Override
        public InstanceType getType() {
            if(!GIT_REPOSITORY.isInitialized) {
                tryLoadingTypeDefinitions();
                if (GIT_REPOSITORY.isInitialized) return GIT_REPOSITORY.instanceType;

                InstanceType type = GIT_REPOSITORY.instanceType = WorkspaceService.createInstanceType(workspace, GIT_REPOSITORY.name, workspace.TYPES_FOLDER, BaseElementType.ARTIFACT.getType());
                cache.getPropertyAsMap(MAPPING).put(GIT_REPOSITORY.name, Long.toString(type.id().value()));
                GIT_REPOSITORY.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, GIT_REPOSITORY.instanceType, DESCRIPTION, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, GIT_REPOSITORY.instanceType, HOME_PAGE, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, GIT_REPOSITORY.instanceType, HTML_URL, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, GIT_REPOSITORY.instanceType, URI, Cardinality.SINGLE, Workspace.STRING);

                WorkspaceService.createPropertyType(workspace, GIT_REPOSITORY.instanceType, OWNER, Cardinality.SINGLE, GitBaseElementType.GIT_USER.getType());

                WorkspaceService.createPropertyType(workspace, GIT_REPOSITORY.instanceType, ISSUES, Cardinality.LIST, GitBaseElementType.GIT_ISSUE.getType());
                WorkspaceService.createPropertyType(workspace, GIT_REPOSITORY.instanceType, PULL_REQUESTS, Cardinality.LIST, GitBaseElementType.GIT_PULL_REQUEST.getType());
                WorkspaceService.createPropertyType(workspace, GIT_REPOSITORY.instanceType, PROJECTS, Cardinality.LIST, GitBaseElementType.GIT_PROJECT.getType());
                WorkspaceService.createPropertyType(workspace, GIT_REPOSITORY.instanceType, COMMITS, Cardinality.LIST, GitBaseElementType.GIT_COMMIT.getType());

                WorkspaceService.createPropertyType(workspace, GIT_REPOSITORY.instanceType, BRANCHES, Cardinality.MAP, GitBaseElementType.GIT_BRANCH.getType());

                return type;
            } else {
                return GIT_REPOSITORY.instanceType;
            }
        }

    }, GIT_USER("git_user") {
        @Override
        public InstanceType getType() {
            if(!GIT_USER.isInitialized) {
                tryLoadingTypeDefinitions();
                if (GIT_USER.isInitialized) return GIT_USER.instanceType;

                InstanceType type = GIT_USER.instanceType = WorkspaceService.createInstanceType(workspace, GIT_USER.name, workspace.TYPES_FOLDER, BaseElementType.ARTIFACT.getType());
                cache.getPropertyAsMap(MAPPING).put(GIT_USER.name, Long.toString(type.id().value()));
                GIT_USER.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, GIT_USER.instanceType, USER_ID, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, GIT_USER.instanceType, EMAIL, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, GIT_USER.instanceType, LOCATION, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, GIT_USER.instanceType, BIO, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, GIT_USER.instanceType, COMPANY, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, GIT_USER.instanceType, URI, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, GIT_USER.instanceType, HTML_URL, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, GIT_USER.instanceType, TYPE, Cardinality.SINGLE, Workspace.STRING);

                WorkspaceService.createPropertyType(workspace, GIT_USER.instanceType, PUBLIC_REPO_COUNT, Cardinality.SINGLE, Workspace.INTEGER);
                WorkspaceService.createPropertyType(workspace, GIT_USER.instanceType, PRIVATE_REPO_COUNT, Cardinality.SINGLE, Workspace.INTEGER);

                return type;
            } else {
                return GIT_USER.instanceType;
            }
        }

    }, GIT_PULL_REQUEST("git_pull_request") {
        @Override
        public InstanceType getType() {
            if(!GIT_PULL_REQUEST.isInitialized) {
                tryLoadingTypeDefinitions();
                if (GIT_PULL_REQUEST.isInitialized) return GIT_PULL_REQUEST.instanceType;

                InstanceType type = GIT_PULL_REQUEST.instanceType = WorkspaceService.createInstanceType(workspace, GIT_PULL_REQUEST.name, workspace.TYPES_FOLDER, GitBaseElementType.GIT_ISSUE.getType());
                cache.getPropertyAsMap(MAPPING).put(GIT_PULL_REQUEST.name, Long.toString(type.id().value()));
                GIT_PULL_REQUEST.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, GIT_PULL_REQUEST.instanceType, PULL_ID, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, GIT_PULL_REQUEST.instanceType, MERGEABLE, Cardinality.SINGLE, Workspace.BOOLEAN);
                WorkspaceService.createPropertyType(workspace, GIT_PULL_REQUEST.instanceType, FROM_BRANCH, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, GIT_PULL_REQUEST.instanceType, TO_BRANCH, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, GIT_PULL_REQUEST.instanceType, MERGED_AT, Cardinality.SINGLE, Workspace.STRING);

                WorkspaceService.createPropertyType(workspace, GIT_PULL_REQUEST.instanceType, MERGED_BY, Cardinality.SINGLE, GitBaseElementType.GIT_USER.getType());

                WorkspaceService.createPropertyType(workspace, GIT_PULL_REQUEST.instanceType, BASE, Cardinality.SINGLE, GitBaseElementType.GIT_COMMIT.getType());
                WorkspaceService.createPropertyType(workspace, GIT_PULL_REQUEST.instanceType, HEAD, Cardinality.SINGLE, GitBaseElementType.GIT_COMMIT.getType());

                WorkspaceService.createPropertyType(workspace, GIT_PULL_REQUEST.instanceType, REQUESTED_REVIEWERS, Cardinality.LIST, GitBaseElementType.GIT_USER.getType());

                return type;
            } else {
                return GIT_PULL_REQUEST.instanceType;
            }
        }

    }, GIT_COMMIT("git_commit") {
        @Override
        public InstanceType getType() {
            if(!GIT_COMMIT.isInitialized) {
                tryLoadingTypeDefinitions();
                if (GIT_COMMIT.isInitialized) return GIT_COMMIT.instanceType;

                InstanceType type = GIT_COMMIT.instanceType = WorkspaceService.createInstanceType(workspace, GIT_COMMIT.name, workspace.TYPES_FOLDER, BaseElementType.ARTIFACT.getType());
                cache.getPropertyAsMap(MAPPING).put(GIT_COMMIT.name, Long.toString(type.id().value()));
                GIT_COMMIT.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, GIT_COMMIT.instanceType, URI, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, GIT_COMMIT.instanceType, HTML_URL, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, GIT_COMMIT.instanceType, COMMIT_MESSAGE, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, GIT_COMMIT.instanceType, TOTAL_DELETIONS, Cardinality.SINGLE, Workspace.INTEGER);
                WorkspaceService.createPropertyType(workspace, GIT_COMMIT.instanceType, TOTAL_ADDITIONS, Cardinality.SINGLE, Workspace.INTEGER);

                WorkspaceService.createPropertyType(workspace, GIT_COMMIT.instanceType, AUTHOR, Cardinality.SINGLE, GitBaseElementType.GIT_USER.getType());
                WorkspaceService.createPropertyType(workspace, GIT_COMMIT.instanceType, COMMITTER, Cardinality.SINGLE, GitBaseElementType.GIT_USER.getType());

                WorkspaceService.createPropertyType(workspace, GIT_COMMIT.instanceType, REPOSITORY, Cardinality.SINGLE, GitBaseElementType.GIT_REPOSITORY.getType());

                WorkspaceService.createPropertyType(workspace, GIT_COMMIT.instanceType, BRANCHES, Cardinality.LIST, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, GIT_COMMIT.instanceType, COMMENTS, Cardinality.LIST, GitBaseElementType.GIT_COMMENT.getType());
                WorkspaceService.createPropertyType(workspace, GIT_COMMIT.instanceType, FILES, Cardinality.LIST, GitBaseElementType.GIT_FILE.getType());
                WorkspaceService.createPropertyType(workspace, GIT_COMMIT.instanceType, LINKED_ISSUES, Cardinality.LIST, GitBaseElementType.GIT_ISSUE.getType());

                return type;
            } else {
                return GIT_COMMIT.instanceType;
            }
        }

    }, GIT_ISSUE("git_issue") {
        @Override
        public InstanceType getType() {
            if(!GIT_ISSUE.isInitialized) {
                tryLoadingTypeDefinitions();
                if (GIT_ISSUE.isInitialized) return GIT_ISSUE.instanceType;

                InstanceType type = GIT_ISSUE.instanceType = WorkspaceService.createInstanceType(workspace, GIT_ISSUE.name, workspace.TYPES_FOLDER, BaseElementType.ARTIFACT.getType());
                cache.getPropertyAsMap(MAPPING).put(GIT_ISSUE.name, Long.toString(type.id().value()));
                GIT_ISSUE.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, GIT_ISSUE.instanceType, URI, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, GIT_ISSUE.instanceType, BODY, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, GIT_ISSUE.instanceType, TITLE, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, GIT_ISSUE.instanceType, HTML_URL, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, GIT_ISSUE.instanceType, STATE, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, GIT_ISSUE.instanceType, CLOSED_AT, Cardinality.SINGLE, Workspace.STRING);

                WorkspaceService.createPropertyType(workspace, GIT_ISSUE.instanceType, LABELS, Cardinality.LIST, Workspace.STRING);

                WorkspaceService.createPropertyType(workspace, GIT_ISSUE.instanceType, ASSIGNEE, Cardinality.SINGLE, GitBaseElementType.GIT_USER.getType());
                WorkspaceService.createPropertyType(workspace, GIT_ISSUE.instanceType, REPORTED_BY, Cardinality.SINGLE, GitBaseElementType.GIT_USER.getType());
                WorkspaceService.createPropertyType(workspace, GIT_ISSUE.instanceType, LINKED_PULL_REQUEST, Cardinality.SINGLE, GitBaseElementType.GIT_PULL_REQUEST.getType());
                WorkspaceService.createPropertyType(workspace, GIT_ISSUE.instanceType, REPOSITORY, Cardinality.SINGLE, GitBaseElementType.GIT_REPOSITORY.getType());

                WorkspaceService.createPropertyType(workspace, GIT_ISSUE.instanceType, COMMITS, Cardinality.LIST, GitBaseElementType.GIT_COMMIT.getType());
                WorkspaceService.createPropertyType(workspace, GIT_ISSUE.instanceType, COMMENTS, Cardinality.LIST, GitBaseElementType.GIT_COMMENT.getType());
                WorkspaceService.createPropertyType(workspace, GIT_ISSUE.instanceType, LINKED_ISSUES, Cardinality.LIST, GitBaseElementType.GIT_ISSUE.getType());
                WorkspaceService.createPropertyType(workspace, GIT_ISSUE.instanceType, PROJECT_STATUS, Cardinality.MAP, Workspace.STRING);

                return type;
            } else {
                return GIT_ISSUE.instanceType;
            }
        }
    },  GIT_FILE("git_file") {
        @Override
        public InstanceType getType() {
            if(!GIT_FILE.isInitialized) {
                tryLoadingTypeDefinitions();
                if (GIT_FILE.isInitialized) return GIT_FILE.instanceType;

                InstanceType type = GIT_FILE.instanceType = WorkspaceService.createInstanceType(workspace, GIT_FILE.name, workspace.TYPES_FOLDER, BaseElementType.ARTIFACT.getType());
                cache.getPropertyAsMap(MAPPING).put(GIT_FILE.name, Long.toString(type.id().value()));
                GIT_FILE.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, GIT_FILE.instanceType, FULL_PATH, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, GIT_FILE.instanceType, CONTENT, Cardinality.MAP, Workspace.STRING);

                return type;
            } else {
                return GIT_FILE.instanceType;
            }
          }
    },  GIT_PROJECT("git_project") {
        @Override
        public InstanceType getType() {
            if (!GIT_PROJECT.isInitialized) {
                tryLoadingTypeDefinitions();
                if (GIT_PROJECT.isInitialized) return GIT_PROJECT.instanceType;

                InstanceType type = GIT_PROJECT.instanceType = WorkspaceService.createInstanceType(workspace, GIT_PROJECT.name, workspace.TYPES_FOLDER, BaseElementType.ARTIFACT.getType());
                cache.getPropertyAsMap(MAPPING).put(GIT_PROJECT.name, Long.toString(type.id().value()));
                GIT_PROJECT.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, GIT_PROJECT.instanceType, BODY, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, GIT_PROJECT.instanceType, CREATOR, Cardinality.SINGLE, GitBaseElementType.GIT_USER.getType());
                WorkspaceService.createPropertyType(workspace, GIT_PROJECT.instanceType, STATE, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, GIT_PROJECT.instanceType, HTML_URL, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, GIT_PROJECT.instanceType, URI, Cardinality.SINGLE, Workspace.STRING);

                return type;
            } else {
                return GIT_PROJECT.instanceType;
            }
        }
    }, GIT_BRANCH("git_branch") {
        @Override
        public InstanceType getType() {
            if (!GIT_BRANCH.isInitialized) {
                tryLoadingTypeDefinitions();
                if (GIT_BRANCH.isInitialized) return GIT_BRANCH.instanceType;

                InstanceType type = GIT_BRANCH.instanceType = WorkspaceService.createInstanceType(workspace, GIT_BRANCH.name, workspace.TYPES_FOLDER, BaseElementType.ARTIFACT.getType());
                cache.getPropertyAsMap(MAPPING).put(GIT_BRANCH.name, Long.toString(type.id().value()));
                GIT_BRANCH.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, GIT_BRANCH.instanceType, OWNER, Cardinality.SINGLE, GitBaseElementType.GIT_REPOSITORY.getType());
                WorkspaceService.createPropertyType(workspace, GIT_BRANCH.instanceType, PROTECTION_URL, Cardinality.SINGLE, Workspace.STRING);

                return type;
            } else {
                return GIT_BRANCH.instanceType;
            }
        }
    }, GIT_COMMENT("git_comment") {
        @Override
        public InstanceType getType() {
            if (!GIT_COMMENT.isInitialized) {
                tryLoadingTypeDefinitions();
                if (GIT_COMMENT.isInitialized) return GIT_COMMENT.instanceType;

                InstanceType type = GIT_COMMENT.instanceType = WorkspaceService.createInstanceType(workspace, GIT_COMMENT.name, workspace.TYPES_FOLDER, BaseElementType.ARTIFACT.getType());
                cache.getPropertyAsMap(MAPPING).put(GIT_COMMENT.name, Long.toString(type.id().value()));
                GIT_COMMENT.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, GIT_COMMENT.instanceType, BODY, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, GIT_COMMENT.instanceType, HTML_URL, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, GIT_COMMENT.instanceType, URI, Cardinality.SINGLE, Workspace.STRING);

                WorkspaceService.createPropertyType(workspace, GIT_COMMENT.instanceType, AUTHOR, Cardinality.SINGLE, GitBaseElementType.GIT_USER.getType());

                return type;
            } else {
                return GIT_COMMENT.instanceType;
            }
        }
    }, GIT_REVIEW("git_review") {
        @Override
        public InstanceType getType() {
            if (!GIT_REVIEW.isInitialized) {
                tryLoadingTypeDefinitions();
                if (GIT_REVIEW.isInitialized) return GIT_REVIEW.instanceType;

                InstanceType type = GIT_REVIEW.instanceType = WorkspaceService.createInstanceType(workspace, GIT_REVIEW.name, workspace.TYPES_FOLDER, BaseElementType.ARTIFACT.getType());
                cache.getPropertyAsMap(MAPPING).put(GIT_REVIEW.name, Long.toString(type.id().value()));
                GIT_REVIEW.isInitialized = true;

                WorkspaceService.createPropertyType(workspace, GIT_REVIEW.instanceType, BODY, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, GIT_REVIEW.instanceType, HTML_URL, Cardinality.SINGLE, Workspace.STRING);
                WorkspaceService.createPropertyType(workspace, GIT_REVIEW.instanceType, URI, Cardinality.SINGLE, Workspace.STRING);

                WorkspaceService.createPropertyType(workspace, GIT_REVIEW.instanceType, AUTHOR, Cardinality.SINGLE, GitBaseElementType.GIT_USER.getType());
                WorkspaceService.createPropertyType(workspace, GIT_REVIEW.instanceType, PULL_REQUESTS, Cardinality.SINGLE, GitBaseElementType.GIT_PULL_REQUEST.getType());

                return type;
            } else {
                return GIT_REVIEW.instanceType;
            }
        }
    };

    private String name;
    private boolean isInitialized;
    private InstanceType instanceType;

    private static boolean loadedFromPersistence = false;
    private static Instance cache;
    private static Workspace workspace = null;
    private static final String GIT_SUB_TYPE_ID_CACHE_ID = "gitSubTypeIdCacheId";

    GitBaseElementType(String name) {
        this.name = name;
        this.isInitialized = false;
        this.instanceType = null;
    }

    public static boolean wasLoadedByPersistence() {
        tryLoadingTypeDefinitions();
        return loadedFromPersistence;
    }

    private static void tryLoadingTypeDefinitions() {

        if (workspace == null) {
            //loading config
            Properties props = new Properties();
            String workspaceName;

            try {
                props = PropertiesLoaderUtils.loadAllProperties("application.properties");
                workspaceName = props.getProperty("git_workspace_name").trim();
            } catch (IOException e) {
                log.debug("GIT-SERVICE : The application.properties file was not found in the resources folder of the main module");
                workspaceName = "git_default_workspace";
            }

            if(workspaceName==null) {
                workspace = WorkspaceService.PUBLIC_WORKSPACE;
            } else {
                String finalWorkspaceName = workspaceName;
                Optional<Workspace> workspace_ = WorkspaceService.allWorkspaces().stream().filter(w -> w.name().equals(finalWorkspaceName)).findAny();
                if (!workspace_.isPresent()) {
                    workspace = WorkspaceService.PUBLIC_WORKSPACE;
                } else {
                    workspace = workspace_.get();
                }
            }

        }

        if(cache==null) {

            //trying to find a cache
            for (Instance cur : workspace.debugInstances()) {
                Property property = cur.getProperty("id");
                if (property != null) {
                    if (property.get() != null && property.get().equals(GIT_SUB_TYPE_ID_CACHE_ID)) {
                        cache = cur;
                    }
                }
            }

            if (cache == null) {
                //no past cache has been found
                log.debug("GIT-SERVICE: No subtype id cache was found");
                cache = workspace.createInstance(BaseElementType.ELEMENT_ID_CACHE.getType(), GIT_SUB_TYPE_ID_CACHE_ID);
                cache.getPropertyAsSingle("id").set(GIT_SUB_TYPE_ID_CACHE_ID);
                for (GitBaseElementType type : GitBaseElementType.values()) {
                    cache.getPropertyAsMap(MAPPING).put(type.name, type.getType().id().toString());
                }
                workspace.concludeTransaction();
                log.debug("GIT-SERVICE: New subtype id cache has been created");
            } else {
                //past cache has been found
                for (GitBaseElementType type : GitBaseElementType.values()) {
                    Object object = cache.getPropertyAsMap(MAPPING).get(type.name);
                    if (object != null) {
                        Long id = Long.parseLong(object.toString());
                        Element element = workspace.findElement(Id.of(id));
                        if (element != null) {
                            type.instanceType = (InstanceType) element;
                            type.isInitialized = true;
                        }
                    }
                }
                loadedFromPersistence = true;
                log.debug("GIT-SERVICE: Successfully reconnected to subtype id cache");
            }
        }
    }

    public final static String  KEY = "key", NAME = "name", CONTENT = "content",
            LINK = "self", DESCRIPTION = "description", ID = "id", MAPPING = "map",
            HOME_PAGE = "homepage", HTML_URL = "html_url", GIT_URL = "git_url", OWNER = "owner",
            PROJECTS = "projects", ISSUES = "issues", PULL_REQUESTS = "pullRequests",
            COMMITS = "commits", BRANCHES = "branches", EMAIL = "email", LOCATION = "location",
            BIO = "bio", COMPANY = "company", TYPE = "type", PUBLIC_REPO_COUNT = "publicRepoCount",
            PRIVATE_REPO_COUNT = "privateRepoCount", USER_ID = "userId", MERGED_BY = "mergedBy",
            MERGED_AT = "mergedAt", MERGEABLE = "mergeable", REVIEWERS = "reviewers", HEAD = "head",
            BASE = "base", TOTAL_ADDITIONS = "totalAdditions", TOTAL_DELETIONS = "totalDeletions",
            FILES = "files", AUTHOR = "author", COMMITTER = "committer", REPORTED_BY = "reportedBy",
            COMMIT_MESSAGE = "commitMessage",  CLOSED_BY = "closedBy", TITLE = "title", BODY = "body",
            CLOSED_AT = "closedAt", LINKED_PULL_REQUEST = "linkedPullRequest", FULL_PATH = "fullPath",
            CREATOR = "creator", STATE = "state", PROTECTION_URL = "protectionURL", ASSIGNEE = "assignee",
            COMMENTS = "comments", REVIEWS = "reviews", REPOSITORY = "repository", URI = "uri",
            REQUESTED_REVIEWERS = "requestedReviewers", LABELS = "labels", LINKED_ISSUES = "linkedIssues",
            PROJECT_STATUS = "projectStatus", FROM_BRANCH = "fromBranch", TO_BRANCH = "toBranch;",
            PULL_ID = "pullId";
}


