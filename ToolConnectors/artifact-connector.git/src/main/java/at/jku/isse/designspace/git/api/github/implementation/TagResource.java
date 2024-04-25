package at.jku.isse.designspace.git.api.github.implementation;

import at.jku.isse.designspace.git.api.IGitTag;
import at.jku.isse.designspace.git.api.core.InsufficientDataException;
import at.jku.isse.designspace.git.api.core.MapResource;
import at.jku.isse.designspace.git.api.github.restclient.IGithubRestClient;

import java.util.Map;

public class TagResource extends MapResource implements IGitTag {

    private final String repositoryName;

    public TagResource(IGithubRestClient source, String repositoryName, Map<String, Object> data) throws InsufficientDataException {
        super(source, data);
        assert repositoryName != null;
        this.repositoryName = repositoryName;
        if (getName() != null) throw new InsufficientDataException();
    }

    @Override
    public String getName() {
        return accessStringProperty("name");
    }

    @Override
    public String getCommit() {
        Map<String, Object> resource = getResource();
        if (resource != null) {
            Object commitInner = resource.get("commit");
            if (commitInner != null) {
                Map<String, Object> content = (Map<String, Object>) commitInner;
                Object sha = content.get("sha");
                if (sha != null) {
                    return (String) sha;
                }
            }
        }
        return null;
    }

    @Override
    public String getSourceCodeZipUrl() {
        return accessStringProperty("zipball_url");
    }

    @Override
    public String getSourceCodeTarUrl() {
        return accessStringProperty("tarball_url");
    }

    @Override
    protected Map<String, Object> load(IGithubRestClient source) {
        // load will never be called, since comments are always initialized,
        // with directly with the provided data
        return null;
    }


}
