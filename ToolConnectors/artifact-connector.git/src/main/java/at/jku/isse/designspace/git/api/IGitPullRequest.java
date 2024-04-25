package at.jku.isse.designspace.git.api;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface IGitPullRequest extends IGitIssue {

    @Override
	int getKey();

    String getName();

    @Override
	String getHTMLUrl();

    @Override
	String getURI();

    @Override
	Map<String, String> getProjectStatus();

    Instant getMergedAt();

    IGitUser getMergedBy();

    String getFromBranch();

    String getDestinationBranch();

    IGitCommit getHead();

    IGitCommit getBase();

    @Override
	String getRepository();

    List<IGitUser> getRequestedReviewers();

}
