package at.jku.isse.designspace.git.api;

import java.util.List;

public interface IGitPullRequestReview {

    int getKey();

    IGitPullRequest getOwner();

    String getBody();

    String getHTMLUrl();

    String getURI();

    IGitUser getAuthor();

    String getPullRequestReviewState();

    List<IGitComment> getReviewComments();

}
