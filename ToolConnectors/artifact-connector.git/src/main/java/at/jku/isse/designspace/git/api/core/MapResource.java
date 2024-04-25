package at.jku.isse.designspace.git.api.core;

import at.jku.isse.designspace.git.api.github.restclient.IGithubRestClient;

import java.util.Map;

public abstract class MapResource extends RESTResource<Map<String, Object>> {

    public MapResource(IGithubRestClient source) {
        super(source);
    }

    public MapResource(IGithubRestClient source, Map<String, Object> data) {
        super(source, data);
    }

    protected String accessStringProperty(String name) {
        assert name != null;
        Map<String, Object> resource = this.getResource();
        if (resource != null) {
            Object value = resource.get(name);
            if (value != null) {
                try {
                    return (String) value;
                } catch (ClassCastException ce) {
                    ce.printStackTrace();
                }
            }
        }
        return null;
    }

    protected Integer accessIntegerProperty(String name) {
        assert name != null;
        Map<String, Object> resource = this.getResource();
        if (resource != null) {
            Object value = resource.get(name);
            if (value != null) {
                try {
                    return (Integer) value;
                } catch (ClassCastException ce) {
                    ce.printStackTrace();
                }
            }
        }
        return -1;
    }

    protected Boolean accessBooleanProperty(String name) {
        assert name != null;
        Map<String, Object> resource = this.getResource();
        if (resource != null) {
            Object value = resource.get(name);
            if (value != null) {
                try {
                    return (Boolean) value;
                } catch (ClassCastException ce) {
                    ce.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public boolean existsOnServer() {
        Map<String, Object> resource = this.getResource();
        if (resource == null) {
            return false;
        }

        Object message = resource.get("message");
        return message == null || !message.equals(NOT_FOUND_MESSAGE);
    }

}
