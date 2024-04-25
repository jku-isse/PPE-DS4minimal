package at.jku.isse.designspace.git.api;

import java.util.List;

public interface IGitRepository {

    String getDescription();

    String getHomepage();

    String getName();

    String getHTMLUrl();

    String getURI();

    String getGitUrl();

    IGitUser getOwner();

    List<IGitTag> getTags();

    List<IGitIssue> getIssues();

    List<IGitPullRequest> getPullRequests();

    List<IGitProject> getProjects();

    List<IGitCommit> getCommits();

    List<IGitBranch> getBranches();
}
