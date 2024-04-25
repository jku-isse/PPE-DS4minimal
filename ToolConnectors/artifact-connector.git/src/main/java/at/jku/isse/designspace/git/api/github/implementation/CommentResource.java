package at.jku.isse.designspace.git.api.github.implementation;

import at.jku.isse.designspace.git.api.IGitComment;
import at.jku.isse.designspace.git.api.IGitUser;
import at.jku.isse.designspace.git.api.core.InsufficientDataException;
import at.jku.isse.designspace.git.api.core.MapResource;
import at.jku.isse.designspace.git.api.github.restclient.IGithubRestClient;

import java.util.Map;

public class CommentResource extends MapResource implements IGitComment {

    public CommentResource(IGithubRestClient source, Map<String, Object> data) throws InsufficientDataException {
        super(source, data);
        if (getHTMLUrl() == null) {
            throw new InsufficientDataException();
        }
    }

    @Override
    public int getKey() {
        return accessIntegerProperty("id");
    }

    @Override
    public String getBody() {
        return accessStringProperty("body");
    }

    @Override
    public IGitUser getAuthor() {
        Map<String, Object> resource = this.getResource();
        if (resource != null) {
            Object author_ = resource.get("user");
            if (author_ != null) {
                try {
                    return new UserResource(this.getSource(), (Map<String, Object>) author_);
                } catch (InsufficientDataException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
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
    protected Map<String, Object> load(IGithubRestClient source) {
        // load will never be called, since comments are always initialized,
        // with directly with the provided data
        return null;
    }

    public String getString() {
        StringBuilder sb = new StringBuilder();

        sb.append("{ \n");

        sb.append("key = ").append(this.getKey()).append(",\n");
        sb.append("body = ").append(this.getBody()).append(",\n");
        sb.append("html_url = ").append(this.getHTMLUrl()).append(",\n");
        sb.append("author = ").append(this.getAuthor()).append(",\n");

        sb.append("\n }");

        return sb.toString();
    }

}
