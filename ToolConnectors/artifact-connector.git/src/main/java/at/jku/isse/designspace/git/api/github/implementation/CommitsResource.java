package at.jku.isse.designspace.git.api.github.implementation;

import at.jku.isse.designspace.git.api.IGitCommit;
import at.jku.isse.designspace.git.api.core.ListResource;
import at.jku.isse.designspace.git.api.github.restclient.IGithubRestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommitsResource extends ListResource<Map<String, Object>, IGitCommit> {

    private final String repositoryName;
    private final Integer pullRequestKey;
    private final String[] branchNames;

    public CommitsResource(IGithubRestClient source, String repositoryName, String[] branchNames) {
        super(source);
        assert repositoryName != null;
        assert branchNames != null;
        this.pullRequestKey = null;
        this.repositoryName = repositoryName;
        this.branchNames = branchNames;
    }

    public CommitsResource(IGithubRestClient source, String repositoryName, Integer pullRequestKey, String[] branchNames) {
        super(source);

        assert repositoryName != null;
        assert pullRequestKey != null;
        assert branchNames != null;

        this.repositoryName = repositoryName;
        this.pullRequestKey = pullRequestKey;
        this.branchNames = branchNames;
    }

    @Override
    public List<IGitCommit> getResources() {
        ArrayList<IGitCommit> commits = new ArrayList<>();
        ArrayList<Map<String, Object>> rawCommits = this.getResource();

        for (Map<String, Object> rawCommit : rawCommits) {
            Object sha = rawCommit.get("sha");
            if (sha != null) {
                IGitCommit commit = new CommitResource(this.getSource(), this.repositoryName, branchNames, (String) sha);
                commits.add(commit);
            }
        }

        return commits;
    }

    @Override
    protected ArrayList<Map<String, Object>> load(IGithubRestClient source) {
        if (pullRequestKey == null) {
            return this.getSource().getCommits(this.repositoryName);
        }
        return this.getSource().getCommits(this.repositoryName, this.pullRequestKey);
    }

}
