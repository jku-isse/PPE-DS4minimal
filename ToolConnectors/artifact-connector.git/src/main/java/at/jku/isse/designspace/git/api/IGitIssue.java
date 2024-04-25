package at.jku.isse.designspace.git.api;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IGitIssue {

    int getKey();

    String getId();

    String getBody();

    String getTitle();

    String getHTMLUrl();

    String getURI();

    String getState();

    /*
        An issue can belong to multiple projects
        and can have a different status for each project
     */
    Map<String, String> getProjectStatus();

    Instant closedAt();

    String[] getTags();

    IGitUser getAssignee();

    IGitUser getReportedBy();

    String getRepository();

    List<IGitComment> getComments();

    List<IGitCommit> getCommits();

    int[] getLinkedIssueKeys();

    /**
     *
     * All Pull Requests are also issues (at least in Github).
     * Issues must not be Pull Requests.
     *
     * @return
     */
    Optional<IGitPullRequest> getAsPullRequest();

    IGitRepository getRepositoryObject();

    List<IGitIssue> getLinkedArtifacts();

}
