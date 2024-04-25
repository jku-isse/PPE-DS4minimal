package at.jku.isse.designspace.git.api.core;


import at.jku.isse.designspace.git.api.github.restclient.IGithubRestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class ListResource<T, R> extends RESTResource<ArrayList<T>> {

    public ListResource(IGithubRestClient source) {
        super(source);
    }

    public ListResource(IGithubRestClient source, ArrayList<T> data) {
        super(source, data);
    }

    public abstract List<R> getResources();

    @Override
    public boolean existsOnServer() {
        Object resource_ = this.getResource();
        if (resource_ == null) {
            return false;
        }

        return !(resource_ instanceof Map);
    }

}
