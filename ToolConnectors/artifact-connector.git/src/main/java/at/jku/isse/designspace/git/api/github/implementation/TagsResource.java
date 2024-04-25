package at.jku.isse.designspace.git.api.github.implementation;

import at.jku.isse.designspace.git.api.IGitTag;
import at.jku.isse.designspace.git.api.core.InsufficientDataException;
import at.jku.isse.designspace.git.api.core.ListResource;
import at.jku.isse.designspace.git.api.github.restclient.IGithubRestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TagsResource extends ListResource<Map<String, Object>, IGitTag> {

    private final String repositoryName;

    public TagsResource(IGithubRestClient source, String repositoryName) {
        super(source);
        assert repositoryName != null;
        this.repositoryName = repositoryName;
    }

    @Override
    public List<IGitTag> getResources() {
        ArrayList<IGitTag> tags = new ArrayList<>();
        ArrayList<Map<String, Object>> rawTags = this.getResource();

        for (Map<String, Object> rawTag : rawTags) {
            TagResource tag = null;
            try {
                tag = new TagResource(this.getSource(), this.repositoryName, rawTag);
            } catch (InsufficientDataException e) {
                System.out.println("GitApi: Tag Data was not complete");
            }
            tags.add(tag);
        }

        return tags;
    }

    @Override
    protected ArrayList<Map<String, Object>> load(IGithubRestClient source) {
        return source.getTags(this.repositoryName);
    }

}
