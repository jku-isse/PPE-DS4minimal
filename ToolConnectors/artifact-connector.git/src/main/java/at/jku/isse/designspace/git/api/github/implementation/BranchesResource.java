package at.jku.isse.designspace.git.api.github.implementation;

import at.jku.isse.designspace.git.api.IGitBranch;
import at.jku.isse.designspace.git.api.core.InsufficientDataException;
import at.jku.isse.designspace.git.api.core.ListResource;
import at.jku.isse.designspace.git.api.github.restclient.IGithubRestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BranchesResource extends ListResource<Map<String, Object>, IGitBranch> {

    private final String repositoryName;

    public BranchesResource(IGithubRestClient source, String repositoryName) {
        super(source);
        assert repositoryName != null;
        this.repositoryName = repositoryName;
    }

    @Override
    public List<IGitBranch> getResources() {
        ArrayList<IGitBranch> branches = new ArrayList<>();
        ArrayList<Map<String, Object>> rawBranches = this.getResource();

        for (Map<String, Object> rawIssue : rawBranches) {
            BranchResource branch = null;
            try {
                branch = new BranchResource(this.getSource(), this.repositoryName, rawIssue);
            } catch (InsufficientDataException e) {
                System.out.println("GitApi: Branch Data was not complete");
            }
            branches.add(branch);
        }

        return branches;
    }

    @Override
    protected ArrayList<Map<String, Object>> load(IGithubRestClient source) {
        return this.getSource().getBranches(this.repositoryName);
    }

}
