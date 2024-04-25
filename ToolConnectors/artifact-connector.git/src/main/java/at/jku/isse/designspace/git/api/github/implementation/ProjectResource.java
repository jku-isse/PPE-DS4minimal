package at.jku.isse.designspace.git.api.github.implementation;

import at.jku.isse.designspace.git.api.IGitProject;
import at.jku.isse.designspace.git.api.IGitUser;
import at.jku.isse.designspace.git.api.core.MapResource;
import at.jku.isse.designspace.git.api.github.restclient.IGithubRestClient;

import java.util.Map;

public class ProjectResource extends MapResource implements IGitProject {

    public ProjectResource(IGithubRestClient source) {
        super(source);
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getBody() {
        return null;
    }

    @Override
    public IGitUser getCreator() {
        return null;
    }

    @Override
    public String getState() {
        return null;
    }

    @Override
    public String getHTMLUrl() {
        return null;
    }

    @Override
    public String getURI() {
        return accessStringProperty("uri");
    }

    @Override
    protected Map<String, Object> load(IGithubRestClient source) {
        return null;
    }

}
