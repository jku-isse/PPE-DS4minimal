package at.jku.isse.designspace.core.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import at.jku.isse.designspace.core.model.Cardinality;
import at.jku.isse.designspace.core.model.Element;
//import at.jku.isse.designspace.core.repository.PartitionedElementRepository;
import at.jku.isse.designspace.core.model.EnumerationType;
import at.jku.isse.designspace.core.model.Folder;
import at.jku.isse.designspace.core.model.Id;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.ListProperty;
import at.jku.isse.designspace.core.model.MapProperty;
import at.jku.isse.designspace.core.model.Property;
import at.jku.isse.designspace.core.model.PropertyType;
import at.jku.isse.designspace.core.model.PublicWorkspace;
import at.jku.isse.designspace.core.model.SetProperty;
import at.jku.isse.designspace.core.model.SingleProperty;
import at.jku.isse.designspace.core.model.Tool;
import at.jku.isse.designspace.core.model.User;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.model.WorkspaceListener;
import at.jku.isse.designspace.core.trees.collaboration.CollaborationTree;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WorkspaceService {
    static public Workspace PUBLIC_WORKSPACE =null; //contains the most high level project-related types and instances

    @Autowired
    private WorkspaceService() throws IOException {

        PublicWorkspace.createPublicWorkspace();
    }

    //******************************************************************
    //*** WORKSPACE
    //******************************************************************

    static public Workspace createWorkspace(String name, Workspace parentWorkspace, User user, Tool tool, boolean autoUpdate, boolean autoCommit) {
        log.debug(";*****WorkspaceService.createWorkspace;" + null + ";name=" + name + " parentWorkspace=" + parentWorkspace + " autoUpdate=" + autoUpdate + " autoCommit=" + autoCommit + "  user=" + user + " tool=" + tool);
        Assert.notNull(user, "User must not be null");

        if (parentWorkspace == null) parentWorkspace = WorkspaceService.PUBLIC_WORKSPACE;

        Workspace workspace = Workspace.create(name, parentWorkspace, user, tool, autoUpdate, autoCommit);

        return workspace;
    }

    static public void deleteWorkspace(Workspace workspace) {
        log.debug(";*****WorkspaceService.deleteWorkspace;" + workspace + ";");
    }

    static public void concludeWorkspaceTransaction(Workspace workspace) {
        log.debug(";*****WorkspaceService.concludeWorkspaceTransaction;" + workspace + ";");
        workspace.concludeTransaction();
    }

    /**
     * Makes any changes within this workspace up to now visible to the parent workspace.
     */
    static public void commitWorkspace(Workspace workspace) {
        log.debug(";*****WorkspaceService.commitWorkspace;" + workspace + ";");
        workspace.commit();
    }

    /**
     * Makes any changes within the parent workspace up to now visible to this workspace
     */
    static public void updateWorkspace(Workspace workspace) {
        log.debug(";*****WorkspaceService.updateWorkspace;" + workspace + ";");
        workspace.update();
    }

    static public void setAutoUpdateWorkspace(Workspace workspace, boolean autoUpdate) {
        log.debug(";*****WorkspaceService.setAutoUpdateWorkspace;" + workspace + ";autoUpdate=" + autoUpdate);
        workspace.setAutoUpdate(autoUpdate);
    }

    static public void setAutoCommitWorkspace(Workspace workspace, boolean autoCommit) {
        log.debug(";*****WorkspaceService.setAutoCommitWorkspace;" + workspace + ";autoCommit=" + autoCommit);
        workspace.setAutoCommit(autoCommit);
    }

    static public void setWorkspaceName(Workspace workspace, String name) {
        log.debug(";*****WorkspaceService.setWorkspaceName;" + workspace + ";name=" + name);
        workspace.setName(name);
    }

//    static public void setWorkspaceParent(Workspace workspace, Workspace parentWorkspace, boolean autoUpdate, boolean autoCommit) {
//        log.debug(";*****WorkspaceService.setWorkspaceParent;" + workspace + ";parentWorkspace=" + parentWorkspace);
//        workspace.setParent(parentWorkspace, autoUpdate, autoCommit);
//    }

    static public Workspace getWorkspace(long workspaceId) {
        return CollaborationTree.getInstance().getData(workspaceId);
    }

    static public Element getWorkspaceElement(Workspace workspace, Id id) {
        return workspace.findElement(id);
    }

    static public Collection<Workspace> allWorkspaces() {
        return CollaborationTree.getInstance().getAllData();
    }

    static public void subscribeToWorkspace(Workspace workspace, WorkspaceListener listener) {
        log.debug(";*****WorkspaceService.subscribeToWorkspace;" + workspace + ";Workspace " + workspace);
        workspace.workspaceListeners.add(listener);
    }

    static public void unsubscribeFromWorkspace(Workspace workspace, WorkspaceListener listener) {
        log.debug(";*****WorkspaceService.unsubscribeFromWorkspace;" + workspace + ";subscriptionWorkspaceId=" + workspace.id());
        workspace.workspaceListeners.remove(listener);
    }



    //******************************************************************
    //*** USER
    //******************************************************************

    static public User registerUser(String name) {
        Assert.hasText(name, "Name must not be null or empty");
        for (User user : User.users.values()) {
            if (user.name().equals(name)) return user;
        }
        return new User(name);
    }

    static public Collection<User> allUsers() { return User.users.values(); }

    static public User getUser(long userId) { return User.users.get(userId); }

    static public User ANY_USER = new User("<ANY USER>");



    //******************************************************************
    //*** TOOL
    //******************************************************************

    static public Tool registerTool(String name, String version) {
        Assert.hasText(name, "Name must not be null or empty");
        Assert.hasText(version, "Version must not be null or empty");
        for (Tool tool : Tool.tools.values()) {
            if (tool.name().equals(name) && tool.version().equals(version)) return tool;
        }
        return new Tool(name, version);
    }

    static public Collection<Tool> allTools() { return Tool.tools.values(); }

    static public Tool getTool(long toolId) { return Tool.tools.get(toolId); }



    //****************************************************************************
    //*** INSTANCE
    //****************************************************************************

    static public Instance createInstance(Workspace workspace, String name, InstanceType instanceType) {
        if (instanceType==null) throw new IllegalArgumentException("instanceType should not be null");
        if (!instanceType.matchesWorkspace(workspace)) throw new IllegalArgumentException("instanceType is from a different workspace");

        Instance instance = workspace.createInstance(instanceType, name);
        log.debug(";*****createInstance;" + workspace + ";instance=" + instance);
        return instance;
    }

    static public void deleteInstance(Workspace workspace, Instance instance) {
        if (instance==null) throw new IllegalArgumentException("instance should not be null");
        if (!instance.matchesWorkspace(workspace)) throw new IllegalArgumentException("instance is from a different workspace");

        log.debug(";*****deleteInstance;" + workspace + ";instance=" + instance);
        instance.delete();
    }

    static public Element getElement(Workspace workspace, Id instanceId) { return workspace.findElement(instanceId); }
    static public Instance getInstance(Workspace workspace, Id instanceId) {
        return workspace.findElement(instanceId);
    }


    //****************************************************************************
    //*** PROPERTY VALUES
    //****************************************************************************

    static public void createProperty(Workspace workspace, Element element, String propertyName, PropertyType propertyType) {
        if (element==null) throw new IllegalArgumentException("element should not be null");
        if (!element.matchesWorkspace(workspace)) throw new IllegalArgumentException("element is from a different workspace");

        if (propertyType==null) {
            propertyType = element.getInstanceType().getPropertyType(propertyName);
            if (propertyType == null) throw new IllegalArgumentException("property needs a property type");
        }

        //in some special cases e.g. TypeTest -> typeProperties1
        //if someone creates PropertyTypes in metas, with the same name as the Properties they want to create
        if(element.hasProperty(propertyName)){
            return;
        }

        switch (propertyType.cardinality()) {
            case SINGLE:
                element.createSingleProperty(propertyName, null, propertyType);
                break;
            case SET:
                element.createSetProperty(propertyName, propertyType);
                break;
            case LIST:
                element.createListProperty(propertyName, propertyType);
                break;
            case MAP:
                element.createMapProperty(propertyName, propertyType);
        }
    }

    static public Property property(Workspace workspace, Element element, String propertyName) {
        if (element==null) throw new IllegalArgumentException("element should not be null");
        if (!element.matchesWorkspace(workspace)) throw new IllegalArgumentException("element is from a different workspace");

        return element.getProperty(propertyName);
    }
    static public SingleProperty getPropertyAsSingle(Workspace workspace, Element element, String propertyName) {
        if (element==null) throw new IllegalArgumentException("element should not be null");
        if (!element.matchesWorkspace(workspace)) throw new IllegalArgumentException("element is from a different workspace");

        return element.getPropertyAsSingle(propertyName);
    }
    static public SetProperty getPropertyAsSet(Workspace workspace, Element element, String propertyName) {
        if (element==null) throw new IllegalArgumentException("element should not be null");
        if (!element.matchesWorkspace(workspace)) throw new IllegalArgumentException("element is from a different workspace");

        return element.getPropertyAsSet(propertyName);
    }
    static public ListProperty getPropertyAsList(Workspace workspace, Element element, String propertyName) {
        if (element==null) throw new IllegalArgumentException("element should not be null");
        if (!element.matchesWorkspace(workspace)) throw new IllegalArgumentException("element is from a different workspace");

        return element.getPropertyAsList(propertyName);
    }
    static public MapProperty getPropertyAsMap(Workspace workspace, Element element, String propertyName) {
        if (element==null) throw new IllegalArgumentException("element should not be null");
        if (!element.matchesWorkspace(workspace)) throw new IllegalArgumentException("element is from a different workspace");

        return element.getPropertyAsMap(propertyName);
    }

    //****************************************************************************
    //*** INSTANCE TYPE
    //****************************************************************************
    static public InstanceType createInstanceType(Workspace workspace, String name, Folder folder, InstanceType ... superTypes) {
        if (name==null) throw new IllegalArgumentException("name must not be null");
        if (folder==null) throw new IllegalArgumentException("folder must not be null");
        if (!folder.matchesWorkspace(workspace)) throw new IllegalArgumentException("folder is from a different workspace");
        for (InstanceType instanceType : superTypes) if (!instanceType.matchesWorkspace(workspace)) throw new IllegalArgumentException("supertype is from a different workspace");

        log.debug(";*****createInstanceType;" + workspace + ";name=" + name + ", superTypes=" + Arrays.toString(superTypes) + ", folder=" + folder);
        InstanceType instanceType = workspace.createInstanceType(name, folder, superTypes);
        return instanceType;
    }

    static public InstanceType createEnumerationType(Workspace workspace, String name, List<String> literals, Folder folder) {
        if (name==null) throw new IllegalArgumentException("name must not be null");
        if (folder==null) throw new IllegalArgumentException("folder must not be null");
        if (literals.size()==0) throw new IllegalArgumentException("at least one literal expected");
        if (!folder.matchesWorkspace(workspace)) throw new IllegalArgumentException("folder is from a different workspace");

        log.debug(";*****createEnumerationType;" + workspace + ";name=" + name + ", literals=" + literals + ", folder=" + folder);

        InstanceType enumInstanceType = EnumerationType.create(workspace, name, Arrays.copyOf(literals.toArray(), literals.size(), String[].class));
        folder.addElement(enumInstanceType);
        return enumInstanceType;
    }

    static public void deleteInstanceType(Workspace workspace, InstanceType instanceType) {
        if (instanceType==null) throw new IllegalArgumentException("instanceType must not be null");
        if (!instanceType.matchesWorkspace(workspace)) throw new IllegalArgumentException("instanceType is from a different workspace");

        log.debug(";*****deleteInstanceType;" + workspace + ";instanceType=" + instanceType);
        instanceType.delete();
    }

    static public InstanceType getInstanceType(Workspace workspace, Id instanceTypeId) { return workspace.findElement(instanceTypeId); }

    static public InstanceType getInstanceTypeByQualifiedName(Workspace workspace, String[] names) {
        return workspace.instanceTypeWithQualifiedName(names);
    }

    //****************************************************************************
    //*** PROPERTY TYPES
    //****************************************************************************

    static public PropertyType createPropertyType(Workspace workspace, InstanceType instanceType, String name, Cardinality cardinality, InstanceType referencedInstanceType, boolean isOptional,boolean isContainer, boolean isContained) {
        return createPropertyType(workspace, instanceType, name, cardinality, referencedInstanceType, null, isOptional,isContainer,isContained);
    }
    static public PropertyType createPropertyType(Workspace workspace, InstanceType instanceType, String name, Cardinality cardinality, InstanceType referencedInstanceType, boolean isOptional) {
        return createPropertyType(workspace, instanceType, name, cardinality, referencedInstanceType, null, isOptional,false,false);
    }
    static public PropertyType createPropertyType(Workspace workspace, InstanceType instanceType, String name, Cardinality cardinality, InstanceType referencedInstanceType) {
        return createPropertyType(workspace, instanceType, name, cardinality, referencedInstanceType, null, false,false,false);
    }
    static public PropertyType createPropertyType(Workspace workspace, InstanceType instanceType, String name, Cardinality cardinality, InstanceType referencedInstanceType, String nativeType) {
        return createPropertyType(workspace, instanceType, name, cardinality, referencedInstanceType, nativeType, false,false,false);
    }
    static public PropertyType createPropertyType(Workspace workspace, InstanceType instanceType, String name, Cardinality cardinality, InstanceType referencedInstanceType, String nativeType, boolean isOptional) {
        return createPropertyType(workspace, instanceType, name, cardinality, referencedInstanceType, nativeType, isOptional,false,false);
    }
    static public PropertyType createPropertyType(Workspace workspace, InstanceType instanceType, String name, Cardinality cardinality, InstanceType referencedInstanceType, String nativeType, boolean isOptional, boolean isContainer, boolean isContained) {
        if (name==null) throw new IllegalArgumentException("name must not be null");
        if (instanceType==null) throw new IllegalArgumentException("instanceType must not be null");
        if (cardinality==null) throw new IllegalArgumentException("cardinality must not be null");
        if (referencedInstanceType==null) throw new IllegalArgumentException("referencedInstanceType must not be null");
        if (!instanceType.matchesWorkspace(workspace)) throw new IllegalArgumentException("instanceType is from a different workspace");
        if (!referencedInstanceType.matchesWorkspace(workspace)) throw new IllegalArgumentException("referencedInstanceType is from a different workspace");

        log.debug(";*****createPropertyType;{};ReferencePrimitivePropertyType(\"" + name + "\",Cardinality." + cardinality + "," + referencedInstanceType.name() + ");");

        PropertyType propertyType = instanceType.getPropertyType(name);
        if (propertyType==null)
            return instanceType.createPropertyType(name, cardinality, referencedInstanceType, nativeType, isOptional);
        else {
            if (!cardinality.equals(propertyType.cardinality())) throw new IllegalArgumentException("there already exists a property type but with different cardinality");
            if (!referencedInstanceType.equals(propertyType.referencedInstanceType())) throw new IllegalArgumentException("there already exists a property type but with different referencedInstanceType");
            return propertyType;
        }
    }
    static public PropertyType createOpposablePropertyType(Workspace workspace, InstanceType instanceTypeA, String nameA, Cardinality cardinalityA, InstanceType instanceTypeB, String nameB, Cardinality cardinalityB) {
        return createOpposablePropertyType(workspace, instanceTypeA, nameA, cardinalityA, false, instanceTypeB, nameB, cardinalityB, false);
    }
    static public PropertyType createOpposablePropertyType(Workspace workspace, InstanceType instanceTypeA, String nameA, Cardinality cardinalityA, boolean isOptionalA, InstanceType instanceTypeB, String nameB, Cardinality cardinalityB) {
        return createOpposablePropertyType(workspace, instanceTypeA, nameA, cardinalityA, isOptionalA, instanceTypeB, nameB, cardinalityB, false);
    }
    static public PropertyType createOpposablePropertyType(Workspace workspace, InstanceType instanceTypeA, String nameA, Cardinality cardinalityA, InstanceType instanceTypeB, String nameB, Cardinality cardinalityB, boolean isOptionalB) {
        return createOpposablePropertyType(workspace, instanceTypeA, nameA, cardinalityA, false, instanceTypeB, nameB, cardinalityB, isOptionalB);
    }
    static public PropertyType createContainmentPropertyType(Workspace workspace, InstanceType containerType, String containerName, Cardinality containerCardinality, InstanceType containedType, String containedName){
        if (containedName==null) throw new IllegalArgumentException("containedName must not be null");
        if (containerName==null) throw new IllegalArgumentException("containerName must not be null");
        if (containerType==null) throw new IllegalArgumentException("containerType must not be null");
        if (containedType==null) throw new IllegalArgumentException("containedType must not be null");
        if (containerCardinality==null) throw new IllegalArgumentException("containerCardinality must not be null");
        if (!containerType.matchesWorkspace(workspace)) throw new IllegalArgumentException("containerType is from a different workspace");
        if (!containedType.matchesWorkspace(workspace)) throw new IllegalArgumentException("containedType is from a different workspace");

        log.debug(";*****createContainedPropertyType;" + workspace + ";containerType=" + containerType + ", containerName=" + containerName + ", containerCardinality=" + containerCardinality + ", containedType=" + containedType + ", containedName=" + containedName);
        PropertyType propertyTypeContainer = containerType.getPropertyType(containerName);
        PropertyType propertyTypeContained = containedType.getPropertyType(containedName);
        if (propertyTypeContainer==null && propertyTypeContained==null)
            return containerType.createContainmentPropertyType(containerName, containerCardinality, containedType, containedName);
        else if (propertyTypeContainer!=null && propertyTypeContained!=null){
            if (propertyTypeContained.opposedPropertyType() != propertyTypeContainer) throw new IllegalArgumentException("there already exists a property type but with different opposedPropertyTypes");
            if (propertyTypeContainer.opposedPropertyType() != propertyTypeContained) throw new IllegalArgumentException("there already exists a property type but with different opposedPropertyTypes");
            if (!containerCardinality.equals(propertyTypeContainer.cardinality())) throw new IllegalArgumentException("there already exists a property type but with different cardinality");
            if (propertyTypeContained.cardinality() != Cardinality.SINGLE) throw new IllegalArgumentException("there already exists a property type but with different cardinality");
            if (!containerType.equals(propertyTypeContainer.referencedInstanceType())) throw new IllegalArgumentException("there already exists a property type but with different referencedInstanceType");
            if (!containedType.equals(propertyTypeContained.referencedInstanceType())) throw new IllegalArgumentException("there already exists a property type but with different referencedInstanceType");
            if (!propertyTypeContained.isContained()) throw new IllegalArgumentException("there already exists a property type but with different containment");
            if (!propertyTypeContainer.isContainer()) throw new IllegalArgumentException("there already exists a property type but with different containment");
            return propertyTypeContainer;
        }
        else
            throw new IllegalArgumentException("Single but not contained property type already exists");
    }
    static public PropertyType createOpposablePropertyType(Workspace workspace, InstanceType instanceTypeA, String nameA, Cardinality cardinalityA, boolean isOptionalA, InstanceType instanceTypeB, String nameB, Cardinality cardinalityB, boolean isOptionalB) {
        if (nameA==null) throw new IllegalArgumentException("nameA must not be null");
        if (nameB==null) throw new IllegalArgumentException("nameB must not be null");
        if (instanceTypeA==null) throw new IllegalArgumentException("instanceTypeA must not be null");
        if (instanceTypeB==null) throw new IllegalArgumentException("instanceTypeB must not be null");
        if (cardinalityA==null) throw new IllegalArgumentException("cardinalityA must not be null");
        if (cardinalityB==null) throw new IllegalArgumentException("cardinalityB must not be null");
        if (!instanceTypeA.matchesWorkspace(workspace)) throw new IllegalArgumentException("instanceTypeA is from a different workspace");
        if (!instanceTypeB.matchesWorkspace(workspace)) throw new IllegalArgumentException("instanceTypeB is from a different workspace");

        log.debug(";*****createOpposablePropertyType;" + workspace + ";instanceTypeA=" + instanceTypeA + ", nameA=" + nameA + ", cardinalityA=" + cardinalityA + ", instanceTypeB=" + instanceTypeB + ", nameB=" + nameB + ", cardinalityB=" + cardinalityB);

        PropertyType propertyTypeA = instanceTypeA.getPropertyType(nameA);
        PropertyType propertyTypeB = instanceTypeB.getPropertyType(nameB);
        if (propertyTypeA==null && propertyTypeB==null)
            return instanceTypeA.createOpposablePropertyType(nameA, cardinalityA, isOptionalA, instanceTypeB, nameB, cardinalityB, isOptionalB);
        else if (propertyTypeA!=null && propertyTypeB!=null){
            if (!cardinalityA.equals(propertyTypeA.cardinality())) throw new IllegalArgumentException("there already exists a property type but with different cardinality");
            if (!cardinalityB.equals(propertyTypeB.cardinality())) throw new IllegalArgumentException("there already exists a property type but with different cardinality");
            if (!instanceTypeA.equals(propertyTypeA.referencedInstanceType())) throw new IllegalArgumentException("there already exists a property type but with different referencedInstanceType");
            if (!instanceTypeB.equals(propertyTypeB.referencedInstanceType())) throw new IllegalArgumentException("there already exists a property type but with different referencedInstanceType");
            return propertyTypeA;
        }
        else
            throw new IllegalArgumentException("Single but not opposing property type already exists");
    }

    static public void deletePropertyType(Workspace workspace, InstanceType instanceType, String name) {
        if (name==null) throw new IllegalArgumentException("name must not be null");
        if (instanceType==null) throw new IllegalArgumentException("instanceType must not be null");
        if (!instanceType.matchesWorkspace(workspace)) throw new IllegalArgumentException("instanceType is from a different workspace");

        log.debug(";*****undefinePropertyType;" + workspace + ";instanceType=" + instanceType);

        instanceType.deletePropertyType(name, true);
    }



    //****************************************************************************
    //*** FOLDER
    //****************************************************************************

    static public Folder createSubfolder(Workspace workspace, Folder parentFolder, String name) {
        if (parentFolder==null) throw new IllegalArgumentException("parentFolder should not be null");
        if (!parentFolder.matchesWorkspace(workspace)) throw new IllegalArgumentException("parentFolder is from a different workspace");

        log.debug(";*****createSubfolder;" + workspace + ";parentFolder=" + parentFolder + " name=" + name);
        Folder folder = parentFolder.createSubfolder(name);
        return folder;
    }

    static public void deleteSubfolder(Workspace workspace, Folder parentFolder, String name) {
        if (parentFolder==null) throw new IllegalArgumentException("parentFolder should not be null");
        if (!parentFolder.matchesWorkspace(workspace)) throw new IllegalArgumentException("parentFolder is from a different workspace");

        log.debug(";*****deleteSubfolder;" + workspace + ";parentFolder=" + parentFolder + " name=" + name);
        //not implemented
    }

    static public Folder subfolder(Workspace workspace, Folder parentFolder, String name) {
        if (parentFolder==null) throw new IllegalArgumentException("parentFolder should not be null");
        if (!parentFolder.matchesWorkspace(workspace)) throw new IllegalArgumentException("parentFolder is from a different workspace");

        return parentFolder.subfolder(name);
    }

    static public Folder getFolder(Workspace workspace, Id folderId) {
        return (Folder) workspace.findElement(folderId);
    }
}
