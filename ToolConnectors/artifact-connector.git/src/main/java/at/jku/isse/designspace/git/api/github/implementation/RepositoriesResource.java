package at.jku.isse.designspace.git.api.github.implementation;

import at.jku.isse.designspace.git.api.IGitRepository;
import at.jku.isse.designspace.git.api.core.InsufficientDataException;
import at.jku.isse.designspace.git.api.core.ListResource;
import at.jku.isse.designspace.git.api.github.restclient.IGithubRestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RepositoriesResource extends ListResource<Map<String, Object>, IGitRepository> {

    public RepositoriesResource(IGithubRestClient gitRestClient) {
        super(gitRestClient);
    }

    @Override
    public List<IGitRepository> getResources() {
        ArrayList<IGitRepository> repositories = new ArrayList<>();
        ArrayList<Map<String, Object>> rawRepositories = this.getResource();

        for (Map<String, Object> rawRepository : rawRepositories) {
            try {
                RepositoryResource repositoryResource = new RepositoryResource(this.getSource(), rawRepository);
                repositories.add(repositoryResource);
            } catch (InsufficientDataException e) {
                System.out.println("GithubApi: The repository data had insufficient information");
            }
        }

        return repositories;
    }

    @Override
    protected ArrayList<Map<String, Object>> load(IGithubRestClient source) {
        return source.getRepositories();
    }

}
