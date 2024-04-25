package at.jku.isse.designspace.git.api.github.implementation;

import at.jku.isse.designspace.git.api.IGitPullRequest;
import at.jku.isse.designspace.git.api.core.InsufficientDataException;
import at.jku.isse.designspace.git.api.core.ListResource;
import at.jku.isse.designspace.git.api.github.restclient.IGithubRestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PullRequestsResource extends ListResource<Map<String, Object>, IGitPullRequest> {

    private final String repositoryName;

    public PullRequestsResource(IGithubRestClient source, String repositoryName) {
        super(source);
        assert repositoryName != null;
        this.repositoryName = repositoryName;
    }

    @Override
    public List<IGitPullRequest> getResources() {
        ArrayList<IGitPullRequest> pullRequests = new ArrayList<>();
        ArrayList<Map<String, Object>> rawPullRequests = this.getResource();

        for (Map<String, Object> rawPullRequest : rawPullRequests) {
            IGitPullRequest pullRequest = null;
            try {
                pullRequest = new PullRequestResource(this.getSource(), this.repositoryName, rawPullRequest);
            } catch (InsufficientDataException e) {
                System.out.println("GithubApi: The pullRequest data did not contain enough information");
            }
            pullRequests.add(pullRequest);
        }

        return pullRequests;
    }

    @Override
    protected ArrayList<Map<String, Object>> load(IGithubRestClient source) {
        return this.getSource().getPullRequests(this.repositoryName);
    }
}
