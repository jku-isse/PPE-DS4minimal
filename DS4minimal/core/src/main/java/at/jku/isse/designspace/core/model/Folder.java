package at.jku.isse.designspace.core.model;

import static at.jku.isse.designspace.core.model.ReservedNames.FOLDER_CLASS_NAME;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

import at.jku.isse.designspace.core.events.ElementCreate;
import at.jku.isse.designspace.core.service.WorkspaceService;

/**
 * Folder is an organizsational construct for storing instances and/or instance types that belong together
 * e.g., coming from the same tool. Folders can be structured hierachically, having subfolders but single parents
 * create a folder by adding it as a subfolder structure
 */
public class Folder extends Instance {

    static public Folder create(Workspace workspace, String name) { return new Folder(workspace, name); }

    protected Folder(Workspace workspace, String name) { super(workspace, workspace.FOLDER_TYPE, name); }
    protected Folder(Workspace workspace, ElementCreate elementCreate) { super(workspace, elementCreate); }

    /**
     * returns parent folder; or null of root folder
     */
    public Folder parentFolder() {
        return (Folder) getPropertyAsSingle(ReservedNames.FOLDER_PARENT).get();
    }

    /**
     * create a subfolder. The subfolder references this folder as parent. Note: delete the subfolder
     * by calling delete() on it.
     */
    public Folder createSubfolder(String name) {
        Folder folder = create(workspace, name);
        getPropertyAsSet(ReservedNames.FOLDER_SUBFOLDERS).add(folder);
        return folder;
    }

    /**
     * returns subfolder with name; or null if not found
     */
    public Folder subfolder(String folderName) {
        for (Folder subfolder : (Collection<Folder>) subfolders()) {
            if (subfolder.name().equals(folderName)) return subfolder;
        }
        return null;
    }
    /**
     * returns all subfolders
     */
    public SetProperty<Folder> subfolders() {
        return getPropertyAsSet(ReservedNames.FOLDER_SUBFOLDERS);
    }


    /**
     * true if  folder is a descendant of the given folder, or the folder itself.
     */
    public boolean isWithinHierarchyOf(Folder folder) {
        Assert.notNull(folder, "Folder must not be null");
        if (equals(folder)) return true;
        Folder parentFolder = parentFolder();
        if (parentFolder!=null) return parentFolder.isWithinHierarchyOf(folder);
        return false;
    }

    /**
     * add element (instance or instance type) to folder
     */
    public void addElement(Element element) {
        Assert.notNull(element, "Element must not be null");

        getPropertyAsSet(ReservedNames.FOLDER_CONTENT).add(element);
    }

    /**
     * remove element (instance or instance type) from folder
     */
    public void removeElement(Element element) {
        Assert.notNull(element, "Element must not be null");

        getPropertyAsSet(ReservedNames.FOLDER_CONTENT).remove(element);
    }

    /**
     * returns all instance types within folder
     */
    public Set<InstanceType> instanceTypes() {
        return (Set) getPropertyAsSet(ReservedNames.FOLDER_CONTENT).get().stream().filter(i -> i instanceof InstanceType).collect(Collectors.toSet());
    }

    /**
     * returns all instances within folder
     */
    public Set<Instance> instances() {
        return (Set) getPropertyAsSet(ReservedNames.FOLDER_CONTENT).get().stream().filter(i -> i instanceof Instance).collect(Collectors.toSet());
    }


    /**
     * returns instance type with a given name within this folder. assumes that the name is unique.
     * if not then it returns the first one it finds that is not deleted
     */
    public InstanceType instanceTypeWithName(String name) {
        for (InstanceType instanceType : instanceTypes()) {
            if (instanceType.name().equals(name) && !instanceType.isDeleted) return instanceType;
        }
        return null;
    }

    /**
     * returns instance type with a given qualified name starting from this folder (relative path). assumes that the name is unique.
     * if not then it returns the first one finds
     */
    public InstanceType instanceTypeWithQualifiedName(String qualifiedName) {
        String[] names = qualifiedName.split(ReservedNames.QUALIFIED_NAME_SEPARATOR);
        if (names.length<1) return null;

        Folder subfolder=this;
        for (int i=0; i<names.length-1; i++) {
            subfolder=subfolder.subfolder(names[i]);
        }
        return subfolder.instanceTypeWithName(names[names.length-1]);
    }

    /**
     * returns the qualified name which is a concat of all parent folder names and its name
     */
    public String getQualifiedName() {
        if (parentFolder()!=null)
            return parentFolder().getQualifiedName()+ReservedNames.QUALIFIED_NAME_SEPARATOR +name();
        else
            return name();
    }

    public String className() { return FOLDER_CLASS_NAME; }

    public String toString() { return this.getQualifiedName()+"{"+id()+"}<Folder>"; }

    public static InstanceType buildType() {
        WorkspaceService.PUBLIC_WORKSPACE.FOLDER_TYPE = WorkspaceService.PUBLIC_WORKSPACE.createInstanceType(ReservedNames.FOLDER_NAME, WorkspaceService.PUBLIC_WORKSPACE.TYPES_FOLDER, WorkspaceService.PUBLIC_WORKSPACE.INSTANCE_TYPE);

        WorkspaceService.createOpposablePropertyType(
                WorkspaceService.PUBLIC_WORKSPACE,
                WorkspaceService.PUBLIC_WORKSPACE.FOLDER_TYPE,
                ReservedNames.FOLDER_CONTENT,
                Cardinality.SET,
                WorkspaceService.PUBLIC_WORKSPACE.ELEMENT,
                ReservedNames.CONTAINED_FOLDER,
                Cardinality.SINGLE);


        WorkspaceService.createOpposablePropertyType(
                WorkspaceService.PUBLIC_WORKSPACE,
                WorkspaceService.PUBLIC_WORKSPACE.FOLDER_TYPE,
                ReservedNames.FOLDER_SUBFOLDERS,
                Cardinality.SET,
                WorkspaceService.PUBLIC_WORKSPACE.FOLDER_TYPE,
                ReservedNames.FOLDER_PARENT,
                Cardinality.SINGLE);

        return WorkspaceService.PUBLIC_WORKSPACE.FOLDER_TYPE;
    }
}
