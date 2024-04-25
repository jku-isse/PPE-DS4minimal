package at.jku.isse.designspace.git.updateservice;

import at.jku.isse.designspace.git.api.*;
import at.jku.isse.designspace.git.api.core.changemanagement.GitChange;

import java.util.Map;

public interface IGitChangePatcher {

    boolean applyIssueChange(GitChange<IGitIssue> issueChange);

    boolean applyPullRequestChange(GitChange<IGitPullRequest> pullRequestChange);

    boolean applyCommitChange(GitChange<IGitCommit> commitChange);

    boolean applyCommentChange(GitChange<IGitComment> commentChange);

    boolean applyUserChange(GitChange<IGitUser> userChange);

    boolean applyBranchChange(GitChange<IGitBranch> branchChange);

    boolean applyMapChange(GitChange<Map<String, Object>> mapChange);

    boolean applyRepositoryChange(GitChange<IGitRepository> repositoryChange);

    boolean applyIntegerChange(GitChange<Integer> integerChange);

    boolean applyStringChange(GitChange<String> stringChange);

    boolean applyBooleanChange(GitChange<Boolean> booleanChange);

}
