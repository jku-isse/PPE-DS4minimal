package at.jku.isse.designspace.git.api.github.implementation;

import at.jku.isse.designspace.git.api.IGitIssue;
import at.jku.isse.designspace.git.api.core.InsufficientDataException;
import at.jku.isse.designspace.git.api.core.ListResource;
import at.jku.isse.designspace.git.api.github.restclient.IGithubRestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IssuesResource extends ListResource<Map<String, Object>, IGitIssue> {

    private final String repositoryName;

    public IssuesResource(IGithubRestClient source, String repositoryName) {
        super(source);
        assert repositoryName != null;
        this.repositoryName = repositoryName;
    }

    @Override
    public List<IGitIssue> getResources() {
        ArrayList<IGitIssue> issues = new ArrayList<>();
        ArrayList<Map<String, Object>> rawIssues = this.getResource();

        for (Map<String, Object> rawIssue : rawIssues) {
            IssueResource issue = null;
            try {
                issue = new IssueResource(this.getSource(), this.repositoryName, rawIssue);
            } catch (InsufficientDataException e) {
                System.out.println("GitApi: Issue Data was not complete");
            }
            issues.add(issue);
        }

        return issues;
    }

    @Override
    protected ArrayList<Map<String, Object>> load(IGithubRestClient source) {
        return this.getSource().getIssues(this.repositoryName);
    }

}
