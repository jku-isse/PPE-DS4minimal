package at.jku.isse.designspace.git.api.github.implementation;

import at.jku.isse.designspace.git.api.*;
import at.jku.isse.designspace.git.api.core.InsufficientDataException;
import at.jku.isse.designspace.git.api.core.MapResource;
import at.jku.isse.designspace.git.api.github.restclient.IGithubRestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RepositoryResource extends MapResource implements IGitRepository {

    private final String repositoryName;

    public RepositoryResource(IGithubRestClient source, String repositoryName) {
        super(source);
        assert repositoryName != null;
        this.repositoryName = repositoryName;
    }

    public RepositoryResource(IGithubRestClient source, Map<String, Object> data) throws InsufficientDataException {
        super(source, data);
        this.repositoryName = getName();
        if (this.repositoryName == null) throw new InsufficientDataException();
    }

    @Override
    public String getDescription() {
        return accessStringProperty("description");
    }

    @Override
    public String getHomepage() {
        return accessStringProperty("homepage");
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
    public String getGitUrl() {
        return accessStringProperty("git_url");
    }

    @Override
    public IGitUser getOwner() {
        Map<String, Object> resource = this.getResource();
        if (resource != null) {
            Object incompleteUser_ = resource.get("owner");
            if (incompleteUser_ != null) {
                Map<String, Object> incompleteUser = (Map<String, Object>) incompleteUser_;
                Object login_ = incompleteUser.get("login");
                return new UserResource(this.getSource(), (String) login_);
            }
        }
        return null;
    }

    @Override
    public List<IGitTag> getTags() {
        TagsResource tagsResource = new TagsResource(this.getSource(), this.repositoryName);
        return tagsResource.getResources();
    }

    @Override
    public List<IGitIssue> getIssues() {
        IssuesResource issuesResource = new IssuesResource(this.getSource(), this.repositoryName);
        return issuesResource.getResources();
    }

    @Override
    public List<IGitPullRequest> getPullRequests() {
        PullRequestsResource pullRequestResource = new PullRequestsResource(this.getSource(), this.repositoryName);
        return pullRequestResource.getResources();
    }

    @Override
    public List<IGitProject> getProjects() {
        return new ArrayList<>();
    }

    @Override
    public List<IGitCommit> getCommits() {
        CommitsResource commitsResource = new CommitsResource(this.getSource(), this.repositoryName, new String[0]);
        return commitsResource.getResources();
    }

    @Override
    public List<IGitBranch> getBranches() {
        BranchesResource branchesResource = new BranchesResource(this.getSource(), this.repositoryName);
        return branchesResource.getResources();
    }

    @Override
    protected Map<String, Object> load(IGithubRestClient source) {
        return source.getRepository(repositoryName);
    }

    public String getString() {
        StringBuilder sb = new StringBuilder();

        sb.append("{ \n");

        sb.append("name = ").append(this.getName()).append(",\n");
        sb.append("description = ").append(this.getDescription()).append(",\n");
        sb.append("homepage = ").append(this.getHomepage()).append(",\n");
        sb.append("labels = ").append(this.getTags()).append(",\n");
        sb.append("html_url = ").append(this.getHTMLUrl()).append(",\n");

        IGitUser owner = this.getOwner();
        if (owner != null) {
            sb.append("reported_by = ").append(owner.getUserId()).append(",\n");
        }

        List<IGitBranch> branches = this.getBranches() ;
        sb.append("branches = [ \n");
        for (IGitBranch branch : branches) {
            sb.append("title : ").append(branch.getName()).append(",\n");
        }
        sb.append("],");

        List<IGitIssue> issues = this.getIssues() ;
        sb.append("issues = [ \n");
        for (IGitIssue issue : issues) {
            sb.append("{ \n");
            sb.append("key : ").append(issue.getKey()).append(",\n");
            sb.append("title : ").append(issue.getTitle()).append(",\n");
            sb.append("body : ").append(issue.getBody()).append(",\n");
            sb.append("}, \n");
        }
        sb.append("],");

        List<IGitCommit> commits = this.getCommits() ;
        sb.append("commits = [ \n");
        for (IGitCommit commit : commits) {
            sb.append("{ \n");
            sb.append("sha : ").append(commit.getSha()).append(",\n");
            sb.append("totalAdditions : ").append(commit.getTotalAdditions()).append(",\n");
            sb.append("totalDeletions : ").append(commit.getTotalDeletions()).append(",\n");
            sb.append("} \n");
        }

        sb.append("]");
        sb.append("\n }");

        return sb.toString();
    }
}
