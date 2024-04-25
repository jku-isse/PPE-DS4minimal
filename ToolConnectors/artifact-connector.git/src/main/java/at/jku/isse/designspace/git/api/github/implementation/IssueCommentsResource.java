package at.jku.isse.designspace.git.api.github.implementation;

import at.jku.isse.designspace.git.api.IGitComment;
import at.jku.isse.designspace.git.api.core.InsufficientDataException;
import at.jku.isse.designspace.git.api.core.ListResource;
import at.jku.isse.designspace.git.api.github.restclient.IGithubRestClient;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IssueCommentsResource extends ListResource<Map<String, Object>, IGitComment> {

    private final String repositoryName;
    private final Integer issueKey;

    public IssueCommentsResource(IGithubRestClient gitRestClient, String repositoryName, Integer issueKey) {
        super(gitRestClient);
        assert repositoryName != null && issueKey != null;
        this.repositoryName = repositoryName;
        this.issueKey = issueKey;
    }

    @Override
    public List<IGitComment> getResources() {
        ArrayList<IGitComment> comments = new ArrayList<>();
        ArrayList<Map<String, Object>> rawComments = this.getResource();

        for (Map<String, Object> rawComment : rawComments) {
            try {
                CommentResource comment = new CommentResource(this.getSource(), rawComment);
                comments.add(comment);
            } catch (InsufficientDataException e) {
                System.out.println("GithubApi: The comment data has insufficient information");
            }
        }

        return comments;
    }

    @Override
    protected ArrayList<Map<String, Object>> load(IGithubRestClient source) {
        return source.getIssueComments(this.repositoryName, issueKey);
    }

}
