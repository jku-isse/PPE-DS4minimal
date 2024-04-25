package at.jku.isse.designspace.core.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import org.springframework.util.Assert;

import at.jku.isse.designspace.core.events.ElementCreate;
import at.jku.isse.designspace.core.events.ElementDelete;
import at.jku.isse.designspace.core.events.ElementUpdate;
import at.jku.isse.designspace.core.events.Operation;
import at.jku.isse.designspace.core.exceptions.OperationException;
import at.jku.isse.designspace.core.trees.modelhistory.caching.ElementState;


/**
 * This abstraction of an element describes elements that may only exist within a workspace,
 * such as an instance. It ensures that during instantiation, the object retains a reference
 * to the workspace it originates from.
 *
 * @param <T> The concrete instanceType of element.
 */
public abstract class Element<T extends Element> {

    /**
     * id the element uniquely identifies (essentially a long value)
     */
    protected Id id;

    /**
     * true if element was deleted
     */
    public boolean isDeleted = false;

    public Workspace workspace;

    public ElementState state;

    /**
     * properties that element declares
     */
    //protected HashMap<String, Property> properties = new HashMap();

    //public List<Operation> reconstructPropertyOperationCache = new ArrayList<>();
    //protected boolean avoidPropertyOperationCache = false;

    //************************************************************************
    //****** Constructors
    //************************************************************************

    //constructor for creating element through workspace
    protected Element(Workspace workspace, InstanceType instanceType, String name) {
        if (workspace==null) throw new IllegalArgumentException("workspacew must not be null");
        if (instanceType==null) throw new IllegalArgumentException("elementType must not be null");
        if (name==null) throw new IllegalArgumentException("name must not be null");

        this.id = Id.newId();
        this.workspace = workspace;

        String className = className();

        elementChanged( new ElementCreate(this.id(), className, instanceType.id()), false );

        workspace.state.load(this);

        var propertyTypes = instanceType.getPropertyTypes(true);
        var propertyNames = getPropertyNames();
        for (PropertyType propertyType : propertyTypes) {
            if(propertyNames.contains(propertyType.name()) || propertyType.hasProperty(ReservedNames.ISOPTIONAL) && propertyType.isOptional()){
                continue;
            }
            switch (propertyType.cardinality()) {
                case SET: createSetProperty(propertyType.name(), propertyType); break;
                case LIST: createListProperty(propertyType.name(), propertyType); break;
                case MAP: createMapProperty(propertyType.name(), propertyType); break;
                case SINGLE: createSingleProperty(propertyType.name(), null, propertyType); break;
            }
        }
        createSetProperty(ReservedNames.Authorized_Users, MetaProperty.authorizedUsersType);

        setName(name);
        setInstanceType(instanceType);
        setAuthorizedUsers();
    }
    //constructor for the construction of meta elements (for which there are no instance types)
    public Element(Workspace workspace) {
        this.id = Id.newId();
        this.workspace = workspace;
    }
    //constructor for reconstructing elements from operations (e.g., replay repository)
    protected Element(Workspace workspace, ElementCreate elementCreate) {
        this.id = elementCreate.elementId();
        this.workspace = workspace;
    }

    public void restore() {
        isDeleted=false;
    }

    /**
     * deletes the element. Since correct versioning does not allow for the actual deletion of elements, it is marked
     * deleted instead. No write operations are permitted thereafter; however, read operations are still allowed.
     * For this purpose, opposable references are removed from the opposite side only so that their last values
     * remain accessible (e.g., it is no longer contained in a folder but still points to the last folder that
     * contained it)
     */
    public void delete() {
        var deleteGroupId = this.id.value();
        // The contained elements of each property.
        Set containedElements = new HashSet();
        //Unset all opposable references
        Property property = null;
        for (PropertyType propertyType : getInstanceType().getPropertyTypes(false)) {
            property = getProperty(propertyType.name());
            if (property != null) {
                // delete all properties of an element if element is deleted
                //Unset all opposable references
                if (propertyType.opposedPropertyType() != null) {
                    switch (propertyType.cardinality()) {
                        case SET:
                            Set cacheSet = getPropertyAsSet(propertyType.name()).get();
                            if(propertyType.isContainer()) containedElements.addAll(cacheSet);
                            getPropertyAsSet(propertyType.name()).clear();
                            getPropertyAsSet(propertyType.name()).set(cacheSet);           //keep elements in deleted elements to understand what it was
                            break;
                        case LIST:
                            List cacheList = getPropertyAsList(propertyType.name()).get();
                            if(propertyType.isContainer()) containedElements.addAll(cacheList);
                            getPropertyAsList(propertyType.name()).clear();
                            getPropertyAsList(propertyType.name()).set(cacheList);         //keep elements in deleted elements to understand what it was
                            break;
                        case MAP:
                            Map cacheMap = getPropertyAsMap(propertyType.name()).get();
                            if(propertyType.isContainer()) {
                                for(Object o : cacheMap.values()){
                                    containedElements.add(o);
                                }
                            }
                            getPropertyAsMap(propertyType.name()).clear();
                            getPropertyAsMap(propertyType.name()).set(cacheMap);           //keep elements in deleted elements to understand what it was
                            break;
                        case SINGLE:
                            Object cacheSingle = getPropertyAsSingle(propertyType.name()).get();
                            if(propertyType.isContainer()) containedElements.add(cacheSingle);
                            getPropertyAsSingle(propertyType.name()).set(null);
                            getPropertyAsSingle(propertyType.name()).setValue(cacheSingle);    //keep elements in deleted elements to understand what it was
                    }
                }
                property.delete();
            }
        }
        getInstanceType().getPropertyAsSet(ReservedNames.INSTANCES).remove(this, deleteGroupId);
        if (getPropertyAsSingle(ReservedNames.CONTAINED_FOLDER) != null)
            getPropertyAsSingle(ReservedNames.CONTAINED_FOLDER).set(null);

        isDeleted = true;
        var elementDeleteOperation = new ElementDelete(this.id(), (ElementCreate) this.state.creationNode.data);
        elementDeleteOperation.deletedGroupId = deleteGroupId;
        elementChanged(elementDeleteOperation);
        // Delete all contained elements after element is deleted
        for(Object element : containedElements){
            if(element instanceof Element){
                if(!((Element<?>) element).isDeleted) ((Element<?>) element).delete();
            }
        }
    }
    public static int deleteGroupId = 0;
    public int getNextDeleteGroudId(){
        return deleteGroupId++;
    }
    public boolean isDeleted() { return isDeleted; }

    //************************************************************************
    //****** Properties made into Methods
    //************************************************************************

    public SetProperty<String> getAuthorizedUsers() {return  getPropertyAsSet(ReservedNames.Authorized_Users);}
    public void addAuthorizedUsers(Collection<String> users) {
    	getAuthorizedUsers().remove("*");
    	getAuthorizedUsers().addAll(users);
    }
    private void setAuthorizedUsers() {
    	if(getInstanceType().name() != "jira_artifact") 
    		getPropertyAsSet(ReservedNames.Authorized_Users).add("*");
    }
    
    public boolean isAccessibleFor(String userId) {
    	return getAuthorizedUsers().contains(userId) || getAuthorizedUsers().contains("*");
    }
    
    /**
     * @return the name of the instance; name is always present but can be null
     */
    public String name() { return (String) getPropertyAsValueOrNull(ReservedNames.NAME); }
    public void setName(String name) {
        if (name==null) throw new IllegalArgumentException("name must not be null");

        getPropertyAsSingle(ReservedNames.NAME).set(name);
    }

    /**
     * @return the type of the instance; type is always present and should not be null
     */
    public InstanceType getInstanceType() { return (InstanceType) getPropertyAsValueOrNull(ReservedNames.INSTANCE_OF); }
    public void setInstanceType(InstanceType instanceType) {
        if (instanceType==null) throw new IllegalArgumentException("instanceType must not be null");

        InstanceType old = (InstanceType) getPropertyAsSingle(ReservedNames.INSTANCE_OF).getValue();
        if (!getProperty(ReservedNames.INSTANCE_OF).set(instanceType)) return;
        if (old != null) old.getPropertyAsSet(ReservedNames.INSTANCES).remove(this);
        instanceType.getPropertyAsSet(ReservedNames.INSTANCES).add(this);
        
        //FIXME: establish if the new type is a subtype of previous type, if so, we need to set additional properties,
        // if a super type of previous type, then we would need to remove properties
        // if a completely unrelated type, then what the hell?
        // who/where is the element changed event set?
        
        // for test purpose just c/p exceprt from Element constructor
        var propertyTypes = instanceType.getPropertyTypes(true);
        var propertyNames = getPropertyNames();
        for (PropertyType propertyType : propertyTypes) {
            if(propertyNames.contains(propertyType.name()) || propertyType.hasProperty(ReservedNames.ISOPTIONAL) && propertyType.isOptional()){
                continue;
            }
            switch (propertyType.cardinality()) {
                case SET: createSetProperty(propertyType.name(), propertyType); break;
                case LIST: createListProperty(propertyType.name(), propertyType); break;
                case MAP: createMapProperty(propertyType.name(), propertyType); break;
                case SINGLE: createSingleProperty(propertyType.name(), null, propertyType); break;
            }
        }
        
    }

    public Id id() { return id; }

    public Folder getFolder() { return (Folder) getPropertyAsInstance(ReservedNames.CONTAINED_FOLDER); }

    public String getQualifiedName() {
        return toString();
    }


    //************************************************************************
    //****** Apply, reconstruct, and Listener
    //************************************************************************

    public void elementChanged(Operation operation) {
        workspace.addToTransaction(this, operation, true);
    }
    public void elementChanged(Operation operation, boolean execute) {
        workspace.addToTransaction(this, operation, execute);
    }

    //************************************************************************
    //****** Element Create/Delete and Property Change
    //************************************************************************

    public void reconstruct(Operation operation) {
        if (operation instanceof ElementCreate)
            id = (Id) operation.elementId();
        else if (operation instanceof ElementDelete)
            isDeleted = true;
        else if (operation instanceof ElementUpdate) {
            return;
            //reconstructPropertyOperationCache.add(operation);
            //if (avoidPropertyOperationCache) property(ReservedNames.NAME);
        } else
            throw new OperationException(operation, "Operation kind unknown: " + operation);
    }

//    /**
//     * This method adds a property to an element
//     * This procedure is required during initialization. A property cannot be deleted as this would
//     * delete its history. Instead use the property delete method to mark it deleted.
//     */
//    public void addProperty(Property property) {
//        //TODO: there are double declarations if (hasProperty(property.name)) throw new IllegalArgumentException("Property "+property.name+" exists already");
//
//        properties.put(property.name, property);
//    }

    /**
     * Returns true if the propertyName is accessible in this element.
     */
    public boolean hasProperty(String propertyName) {
        return this.state.hasProperty(propertyName);
    }

    /**
     * Returns true if the propertyName is accessible in this element and if it is set to some value (not null).
     */
    public boolean isPropertySet(String propertyName) {
        return this.state.isPropertySet(propertyName);
    }

    /**
     * Retrieves the property with the given name.
     * @param propertyName the name of the property whose you wish to retrieve. you might want to use the additional
     *                     propertyAs* methods as they directly query the property value, avoiding the property itself
     */
    public Property getProperty(String propertyName) {
        return workspace.state.load(id,propertyName);
    }

    /**
     * Retrieves properties declared in this element (if the element is an instance type then it does not retrieve
     * the parent properties)
     */
    public Collection<Property> getProperties() { return workspace.state.properties(id()); }
    /**
     * Retrieves the names of property declared in this element. (if the element is an instance type then it does
     * not retrieve the parent properties)
     */
    public Collection<String> getPropertyNames() {
        this.name();
        return workspace.state.propertyNames(id());
    }

    /**
     * retrieved the property value and depending on method converts the value (e.g., to an instance). some methods
     * handle situations where the value is not present (e.g., orNull returns null if there is no property)
     */
    public Object getPropertyAsValue(String propertyName) { return getProperty(propertyName).get(); }
    public Instance getPropertyAsInstance(String propertyName) { return (Instance) getPropertyAsValue(propertyName); }
    public InstanceType getPropertyAsInstanceType(String propertyName) {  return (InstanceType) getPropertyAsValue(propertyName); }
    public Object getPropertyAsValueOrNull(String propertyName) {
        Property property = getProperty(propertyName);
        if (property == null) return null;
        return property.get();
    }
    public <X extends RuntimeException> Object getPropertyAsValueOrException(String propertyName, Supplier<? extends X> exceptionSupplier) {
        Property property = getProperty(propertyName);
        if (property == null) throw exceptionSupplier.get();
        return property.get();
    }
    public <X extends RuntimeException> Object getPropertyAsValueOrElse(String propertyName, Supplier returningSupplier) {
        Property property = getProperty(propertyName);
        if (property == null) return returningSupplier.get();
        return property.get();
    }   
    
    public SingleProperty getPropertyAsSingle(String propertyName) { return (SingleProperty) getProperty(propertyName); }
    public SetProperty getPropertyAsSet(String propertyName) { return (SetProperty) getProperty(propertyName); }
    public ListProperty getPropertyAsList(String propertyName) { return (ListProperty) getProperty(propertyName); }
    public MapProperty getPropertyAsMap(String propertyName) { return (MapProperty) getProperty(propertyName); }

    /**
     * create single/set/list/map property
     */
    public SingleProperty createSingleProperty(String propertyName, Object value, PropertyType propertyType) {
        Assert.isTrue(propertyType!=null, "property needs a property type");
        SingleProperty property = new SingleProperty(this, propertyName, value, propertyType);
        //addProperty(property);
        return property;
    }
    public SetProperty createSetProperty(String propertyName, PropertyType propertyType) {
        Assert.isTrue(propertyType!=null, "property needs a property type");
        SetProperty property = new SetProperty(this, propertyName, propertyType);
        //addProperty(property);
        return property;
    }
    public ListProperty createListProperty(String propertyName, PropertyType propertyType) {
        Assert.isTrue(propertyType!=null, "property needs a property type");
        ListProperty property = new ListProperty(this, propertyName, propertyType);
        //addProperty(property);
        return property;
    }
    public MapProperty createMapProperty(String propertyName, PropertyType propertyType) {
        Assert.isTrue(propertyType!=null, "property needs a property type");
        MapProperty property = new MapProperty(this, propertyName, propertyType);
        //addProperty(property);
        return property;
    }

    //************************************************************************
    //****** CONSTRUCTOR
    //************************************************************************

    public boolean matchesWorkspace(Workspace workspace) { return (workspace==this.workspace || workspace==null); }
    abstract public String className();

    public boolean equals(Object o) {
        if (o==null || getClass() != o.getClass()) return false;
        if (o==this) return true;
        return Objects.equals(id, ((Element)o).id);
    }

    public String toString() { return "<{" + id() + "} "+getClass().getSimpleName()+">"; }

    /**
     * Checks if any of the contained properties are already set
     * @return true if one of the contained properties was set other than the type with the given value
     */
    public boolean checkPropertiesForContainment(PropertyType type, Object value) {
        for (Property property : this.getProperties()) {
            if (property.propertyType().isContained() && property.getValue() != null && (property.getValue() != value || property.propertyType != type)) {
                return true;
            }
        }
        return false;
    }
}
