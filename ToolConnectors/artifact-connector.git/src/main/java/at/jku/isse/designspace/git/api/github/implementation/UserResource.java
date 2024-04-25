package at.jku.isse.designspace.git.api.github.implementation;

import at.jku.isse.designspace.git.api.IGitUser;
import at.jku.isse.designspace.git.api.core.InsufficientDataException;
import at.jku.isse.designspace.git.api.core.MapResource;
import at.jku.isse.designspace.git.api.github.restclient.IGithubRestClient;

import java.util.Map;

public class UserResource extends MapResource implements IGitUser {

    private final String userId;

    public UserResource(IGithubRestClient source, String userId) {
        super(source);
        assert userId != null;
        this.userId = userId;
    }

    public UserResource(IGithubRestClient source, Map<String, Object> data) throws InsufficientDataException {
        super(source, data);
        this.userId = getUserId();
        if (userId == null) throw new InsufficientDataException();
    }

    @Override
    public String getUserId() {
        return accessStringProperty("login");
    }

    @Override
    public String getName() {
        return accessStringProperty("name");
    }

    @Override
    public String getEmail() {
        return accessStringProperty("email");
    }

    @Override
    public String getLocation() {
        return accessStringProperty("location");
    }

    @Override
    public String getBio() {
        return accessStringProperty("bio");
    }

    @Override
    public String getCompany() {
        return accessStringProperty("company");
    }

    @Override
    public String getHTMLUrl() {
        return accessStringProperty("html_url");
    }

    @Override
    public String getURI() {
        return accessStringProperty("url");
    }

    @Override
    public String getType() {
        return accessStringProperty("type");
    }

    @Override
    public int getPublicRepoCount() {
        return accessIntegerProperty("public_repos");
    }

    @Override
    public int getPrivateRepoCount() {
        return 0;
    }

    @Override
    protected Map<String, Object> load(IGithubRestClient source) {
        return source.getUser(this.userId);
    }

    public String getString() {
        StringBuilder sb = new StringBuilder();

        sb.append("{ \n");

        sb.append("userId = ").append(this.getUserId()).append(",\n");
        sb.append("name = ").append(this.getName()).append(",\n");
        sb.append("bio = ").append(this.getBio()).append(",\n");
        sb.append("html_url = ").append(this.getHTMLUrl()).append(",\n");
        sb.append("email = ").append(this.getEmail()).append(",\n");
        sb.append("privateRepoCount = ").append(this.getPrivateRepoCount()).append(",\n");
        sb.append("publicRepoCount = ").append(this.getPublicRepoCount()).append(",\n");

        sb.append("\n }");

        return sb.toString();
    }
}
