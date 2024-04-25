package at.jku.isse.designspace.git.api.github.implementation;

import at.jku.isse.designspace.git.api.IGitBranch;
import at.jku.isse.designspace.git.api.core.InsufficientDataException;
import at.jku.isse.designspace.git.api.core.MapResource;
import at.jku.isse.designspace.git.api.github.restclient.IGithubRestClient;

import java.util.Map;

public class BranchResource extends MapResource implements IGitBranch {

    private final String repositoryName;
    private final String branchName;

    public BranchResource(IGithubRestClient source, String repositoryName, String branchName) {
        super(source);
        assert repositoryName != null;
        this.repositoryName = repositoryName;
        this.branchName = branchName;
    }

    public BranchResource(IGithubRestClient source, String repositoryName, Map<String, Object> data) throws InsufficientDataException {
        super(source, data);
        assert repositoryName != null;
        this.repositoryName = repositoryName;
        this.branchName = getName();
        if (this.branchName == null) {
            throw new InsufficientDataException();
        }
    }

    @Override
    public int getKey() {
        return accessIntegerProperty("key");
    }

    @Override
    public String getHeadSha() {
        Map<String, Object> resource = getResource();
        if (resource != null) {
            Object commit_ = resource.get("commit");
            if (commit_ != null) {
                Map<String, Object> commit = (Map<String, Object>) commit_;
                Object sha = commit.get("sha");
                if (sha != null) {
                    return (String) sha;
                }
            }
        }
        return null;
    }

    @Override
    public String getOwner() {
        return this.repositoryName;
    }

    @Override
    public String getName() {
        return accessStringProperty("name");
    }

    @Override
    protected Map<String, Object> load(IGithubRestClient source) {
        return this.source.getBranch(this.repositoryName, this.branchName);
    }


}
