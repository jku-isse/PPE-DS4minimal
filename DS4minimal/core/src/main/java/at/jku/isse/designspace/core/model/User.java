package at.jku.isse.designspace.core.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import at.jku.isse.designspace.core.controlflow.ControlEventEngine;
import at.jku.isse.designspace.core.controlflow.controlevents.UserCreationEvent;

public class User {

    private static long IDs = 1;
    public static HashMap<Long, User> users = new HashMap<>();

    public long id;
    String name;
    HashSet<Workspace> workspaces = new HashSet<>();

    public List<UserNotification> notifications = new LinkedList<>();

    public User(String name) {
        this.id = IDs++;
        users.put(id, this);
        this.name = name;

        //---------------ADDED FOR PERSISTENCE---------------
        ControlEventEngine.storeControlEvent(new UserCreationEvent(name));
        //---------------ADDED FOR PERSISTENCE---------------
    }

    public void addWorkspace(Workspace workspace) {
        workspaces.add(workspace);
    }
    public void removeWorkspace(Workspace workspace) {
        workspaces.remove(workspace);
    }
    public void addNotification(String notification){
        notifications.add(new UserNotification(notification));
    }
    public void setNotificationsSpotted(){
        notifications.forEach(x -> x.isSpotted = true);
    }
    public void clearNotifications(){
        notifications.clear();
    }

    public long id() { return id; }
    public String name() { return name; }

    public String toString() { return name+"{"+id()+"}<User>"; }
}