package at.jku.isse.designspace.core.service;

import java.io.Serializable;

public class ServiceDescription implements Serializable, Comparable<ServiceDescription> {
    private String name;
    private String version;
    private Integer priority;
    private boolean persistenceAware;
    private transient Runnable initFunction;

    public ServiceDescription(String name, String version, Integer priority, Runnable initFunction, boolean persistenceAware) {
        this.name = name;
        this.version = version;
        this.priority = priority;
        this.persistenceAware = persistenceAware;
        this.initFunction = initFunction;
    }

    public String getName() {
        return name;
    }
    public String getVersion() {
        return version;
    }
    public Integer getPriority() {
        return priority;
    }

    public boolean isPersistenceAware() {
        return persistenceAware;
    }

    public Runnable getInitFunction() {
        return initFunction;
    }

    @Override
    public int compareTo(ServiceDescription sd) {
        return this.priority - sd.priority;
    }
}

