package at.jku.isse.designspace.core.model;

import java.util.HashMap;
import java.util.HashSet;

import at.jku.isse.designspace.core.controlflow.ControlEventEngine;
import at.jku.isse.designspace.core.controlflow.controlevents.ToolCreationEvent;

public class Tool {

    private static long IDs = 1;
    public static HashMap<Long, Tool> tools = new HashMap<>();

    public long id;
    String name;
    String version;
    HashSet<Workspace> workspaces = new HashSet<>();

    public Tool(String name, String version) {
        this.id = IDs++;
        tools.put(id, this);
        this.name = name;
        this.version = version;

        //---------------ADDED FOR PERSISTENCE---------------
        ControlEventEngine.storeControlEvent(new ToolCreationEvent(name, version));
        //---------------ADDED FOR PERSISTENCE---------------
    }

    public void addWorkspace(Workspace workspace) {
        workspaces.add(workspace);
    }
    public void removeWorkspace(Workspace workspace) {
        workspaces.remove(workspace);
    }

    public long id() { return id; }
    public String name() { return name; }
    public String version() { return version; }

    public String toString() { return name+"-"+version+"{"+id()+"}<Tool>"; }
}
