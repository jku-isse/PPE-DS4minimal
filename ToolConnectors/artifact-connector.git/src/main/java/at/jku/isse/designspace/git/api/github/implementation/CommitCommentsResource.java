package at.jku.isse.designspace.git.api.github.implementation;

import at.jku.isse.designspace.git.api.IGitComment;
import at.jku.isse.designspace.git.api.core.InsufficientDataException;
import at.jku.isse.designspace.git.api.core.ListResource;
import at.jku.isse.designspace.git.api.github.restclient.IGithubRestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommitCommentsResource extends ListResource<Map<String, Object>, IGitComment> {

    private final String repositoryName;
    private final String sha;

    public CommitCommentsResource(IGithubRestClient gitRestClient, String repositoryName, String sha) {
        super(gitRestClient);
        assert repositoryName != null && sha != null;
        this.repositoryName = repositoryName;
        this.sha = sha;
    }

    @Override
    public List<IGitComment> getResources() {
        ArrayList<IGitComment> commitComments = new ArrayList<>();
        ArrayList<Map<String, Object>> rawCommitsComments = this.getResource();

        for (Map<String, Object> rawCommitComment : rawCommitsComments) {
            try {
                CommentResource commitComment = new CommentResource(this.getSource(), rawCommitComment);
                commitComments.add(commitComment);
            } catch (InsufficientDataException e) {
                System.out.println("GithubApi: The comment data has insufficient information");
            }
        }

        return commitComments;
    }

    @Override
    protected ArrayList<Map<String, Object>> load(IGithubRestClient source) {
        return source.getCommitComments(this.repositoryName, sha);
    }

}
