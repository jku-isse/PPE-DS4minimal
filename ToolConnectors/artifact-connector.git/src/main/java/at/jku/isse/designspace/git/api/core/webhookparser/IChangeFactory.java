package at.jku.isse.designspace.git.api.core.webhookparser;

import at.jku.isse.designspace.git.api.core.changemanagement.listener.*;

public interface IChangeFactory {

    void createChanges(Object updateResponse);

    void addIssueChangeListener(IIssueChangeListener issueChangeListener);

    void addPullRequestChangeListener(IPullRequestChangeListener pullRequestChangeListener);

    void addCommitChangeListener(ICommitChangeListener commitChangeListener);

    void addCommentChangeListener(ICommentChangeListener commentChangeListener);

    void addUserChangeListener(IUserChangeListener userChangeListener);

    void addBranchChangeListener(IBranchChangeListener branchChangeListener);

    void addRepositoryChangeListener(IRepositoryChangeListener repositoryChangeListener);

    void addMapChangeListener(IMapChangeListener mapChangeListener);

    void addStringChangeListener(IStringChangeListener stringChangeListener);

    void addIntegerChangeListener(IIntegerChangeListener integerChangeListener);

    void addBooleanChangeListener(IBooleanChangeListener booleanChangeListener);
}
