package at.jku.isse.designspace.core.model;

import static at.jku.isse.designspace.core.model.ReservedNames.INSTANCE_CLASS_NAME;

import java.util.Set;

import at.jku.isse.designspace.core.events.ElementCreate;

/**
 * An instance describes a typed element where the type is referred to as instance type. An instance
 * always has a type and a name. In addition, it may have other properties as defined in the instance type
 * (each property corresponds to a property type in the instance type).
 * Instances represent arbitrary bits of engineering knowledge (e.g., a shape in Visio, a
 * requirement or a Java method).
 * Instances are created through the workspace
 */
public class Instance extends Element<Instance> {

    static public Instance create(Workspace workspace, InstanceType instanceType, String name) {
        return new Instance(workspace, instanceType, name);
    }

    //************************************************************************
    //****** Constructors
    //************************************************************************

    //constructor for creating element through workspace
    protected Instance(Workspace workspace, InstanceType instanceType, String name) {
        super(workspace, instanceType, name);

        //Workspace.logger.debug(";create instance;{};id={} instanceType={}", workspace, id(), instanceType);
    }

    //constructor for reconstructing element through repository
    protected Instance(Workspace workspace, ElementCreate elementCreate) {
        super(workspace, elementCreate);
    }


    //constructor for root workspace where no workspace exists yet
    protected Instance(Workspace workspace) { super(workspace); }


    //************************************************************************
    //****** Helper
    //************************************************************************

    public String className() { return INSTANCE_CLASS_NAME; }

    public String toString() {
        return "Instance-"+name()+"{"+id+"}"+ getInstanceType();
    }

    // ownership properties
    public void addOwner(User u1){
        this.getPropertyAsSet(ReservedNames.OWNERSHIP_PROPERTY).add(String.valueOf(u1.id));
    }
    public void removeOwner(User u1){
        this.getPropertyAsSet(ReservedNames.OWNERSHIP_PROPERTY).remove(String.valueOf(u1.id));
    }
    public void clearOwnership(){this.getPropertyAsSet(ReservedNames.OWNERSHIP_PROPERTY).clear();}
    
    // authorizedUser properties
    public void addauthorizedUser(String u1){
    	if(getAuthorizedUsers().getValue().equals(Set.of("*")))
    		return;
        this.getPropertyAsSet(ReservedNames.Authorized_Users).add(u1);
    }
}