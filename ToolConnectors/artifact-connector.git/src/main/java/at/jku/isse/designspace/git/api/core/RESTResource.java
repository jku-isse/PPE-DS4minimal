package at.jku.isse.designspace.git.api.core;

import at.jku.isse.designspace.git.api.github.restclient.IGithubRestClient;

public abstract class RESTResource<T> {

    protected static final String NOT_FOUND_MESSAGE = "Not Found";

    protected IGithubRestClient source;
    private T resource;

    public RESTResource(IGithubRestClient source) {
        assert source != null;
        this.source = source;
    }

    public RESTResource(IGithubRestClient source, T resource) {
        assert resource != null && source != null;
        this.resource = resource;
        this.source = source;
    }

    protected T getResource() {
        if (resource == null) {
            this.resource = load(this.source);
        }
        return this.resource;
    }

    protected void setResource(T resource) {
        this.resource = resource;
    }

    protected void update() {
        this.resource = load(this.source);
    }

    protected IGithubRestClient getSource() {
        return source;
    }

    protected abstract T load(IGithubRestClient source);

    protected abstract boolean existsOnServer();

}
