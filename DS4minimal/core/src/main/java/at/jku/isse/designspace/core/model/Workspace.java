package at.jku.isse.designspace.core.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

import at.jku.isse.designspace.core.controlflow.ControlEventEngine;
import at.jku.isse.designspace.core.controlflow.controlevents.AutoCommitChangeEvent;
import at.jku.isse.designspace.core.controlflow.controlevents.AutoUpdateChangeEvent;
import at.jku.isse.designspace.core.controlflow.controlevents.NameChangeEvent;
import at.jku.isse.designspace.core.controlflow.controlevents.WorkspaceCreationEvent;
import at.jku.isse.designspace.core.events.Operation;
//import at.jku.isse.designspace.core.repository.PartitionedElementRepository;
import at.jku.isse.designspace.core.service.WorkspaceService;
import at.jku.isse.designspace.core.trees.collaboration.CollaborationTree;
import at.jku.isse.designspace.core.trees.collaboration.CollaborationTreeEdgeType;
import at.jku.isse.designspace.core.trees.modelhistory.ModelHistoryTree;
import at.jku.isse.designspace.core.trees.modelhistory.caching.WorkspaceState;

/**
 * A Workspace is simply a working area within the DesignSpace where instances/instance types of any kind
 * may be created, modified, and deleted.  We speak of changes or operations. Many workspaces are private
 * workspaces that are directly linked to a tool being used by an engineer (user). In that case, the
 * instances/instance types are not changed in the workspace but rather in the tool and then propagated to
 * the workspace using operation messages - where they are reconstructed. Workspaces may form tree-like hierachies
 * with many children and single parent whereby children workspaces are able to access instances/types of
 * their parents. The topmost (root) workspace thus contains instances/types accessible by all workspaces.
 * This topmost workspace is hence referred to as Public Workspace
 */
public class Workspace {
    public static boolean DISABLE_SERVICES = false;
    public static int MAX_SERVICE_NOTIFICATION_ROUNDS = 6;

    protected final CollaborationTree collaborationNetwork;
    protected final ModelHistoryTree modelHistoryTree;
    public WorkspaceState state;

    //static public PartitionedElementRepository partitionedRepository;

    //operations a stored in the repository
    //protected WorkspaceRepository repository;

    //workspaces have their own IDs, starting with 1 for the Public Workspace
    private static AtomicLong IDs = new AtomicLong(1);
    private long id;

    public Instance debug_instance = null;
    public InstanceType debug_instanceType = null;

    //workspaces have names and may have user/tool 'owners'
    protected String name = null;
    protected User user = null;
    protected Tool tool = null;

    //essential types
    public InstanceType INSTANCE_TYPE;
    public InstanceType FOLDER_TYPE;
    public InstanceType ENUMERATION_TYPE;

    public InstanceType ELEMENT;
    public InstanceType META_INSTANCE_TYPE;
    public InstanceType META_PROPERTY_TYPE;

    static public InstanceType STRING;
    static public InstanceType INTEGER;
    static public InstanceType REAL;
    static public InstanceType BOOLEAN;
    static public InstanceType DATE;

    static public PropertyType STRING_PROPERTY;
    static public PropertyType INTEGER_PROPERTY;
    static public PropertyType REAL_PROPERTY;
    static public PropertyType BOOLEAN_PROPERTY;
    static public PropertyType DATE_PROPERTY;
    static public PropertyType REFERENCE_PROPERTY;

    static public MetaPropertyType GENERIC_SINGLE_PROPERTY_TYPE;
    static public MetaPropertyType GENERIC_SET_PROPERTY_TYPE;
    static public MetaPropertyType GENERIC_LIST_PROPERTY_TYPE;
    static public MetaPropertyType GENERIC_MAP_PROPERTY_TYPE;

    //essential folders
    public Folder ROOT_FOLDER;              //root folder of all other folders (should never be modified)
    public Folder TYPES_FOLDER;             //subfolder of root folder containing types
    public Folder PROJECTS_FOLDER;          //subfolder of root folder containing instances and possibly also types

    //protected Workspace parentWorkspace;
    //protected Set<Workspace> childWorkspaces = new HashSet();

    static public Set<ServiceProvider> serviceProviders = new HashSet<>();        //services listen to all workspaces
    public Set<WorkspaceListener> workspaceListeners = new HashSet<>();         //clients listen to their respective workspaces

    /**
     * The point in time until which workspace parent state is visible. For instance, if this workspace
     * fixates its parent to time x then all changes in the parent that occurred after x will
     * not be visible to this workspace. If this value is not set, all changes are visible.
     */
    protected boolean isAutoUpdate = false;

    /**
     * The point in time at which this workspace state is visible to the parent state. For instance, if a workspace
     * fixates its parent version to time x, all changes in this workspace after time x will
     * not be visible to the parent. If this value is not set, all updates will immediately be visible.
     */
    protected boolean isAutoCommit = false;

    //temporary storage of elements accessed in this workspace (whether they were created here or reconstructed from a parent)
    //public HashMap<Id, Element> elementsInCache = new HashMap<>();

    //temporary buffer for all new operations in this workspace
    //public List<Operation> operationsInTransaction = new ArrayList<>();

    public static Workspace create(String name, Workspace parentWorkspace, User user, Tool tool, boolean autoUpdate, boolean autoCommit) {
        Assert.notNull(user, "user must not be null");

        Workspace workspace = new Workspace(parentWorkspace);

        if (name == null || name.isBlank())
            workspace.name = user.name + (tool == null ? "" : "-" + tool.name) + workspace.id();
        else
            workspace.name = name;

        //workspace.repository = partitionedRepository.getPartition(workspace.id(), workspace, parentWorkspace);

        //workspace.parentWorkspace = parentWorkspace;
        //workspace.parentWorkspace.childWorkspaces.add(workspace);

        workspace.isAutoUpdate = autoUpdate;
        workspace.isAutoCommit = autoCommit;

        workspace.user = user;
        workspace.user.addWorkspace(workspace);
        workspace.tool = tool;
        if (workspace.tool != null) workspace.tool.addWorkspace(workspace);

        workspace.ELEMENT = workspace.its(WorkspaceService.PUBLIC_WORKSPACE.ELEMENT);
        workspace.META_INSTANCE_TYPE = workspace.its(WorkspaceService.PUBLIC_WORKSPACE.META_INSTANCE_TYPE);
        workspace.META_PROPERTY_TYPE = workspace.its(WorkspaceService.PUBLIC_WORKSPACE.META_PROPERTY_TYPE);

        workspace.INSTANCE_TYPE = workspace.its(WorkspaceService.PUBLIC_WORKSPACE.INSTANCE_TYPE);
        workspace.FOLDER_TYPE = workspace.its(WorkspaceService.PUBLIC_WORKSPACE.FOLDER_TYPE);
        workspace.ENUMERATION_TYPE = workspace.its(WorkspaceService.PUBLIC_WORKSPACE.ENUMERATION_TYPE);

        workspace.ROOT_FOLDER = workspace.its(WorkspaceService.PUBLIC_WORKSPACE.ROOT_FOLDER);
        workspace.TYPES_FOLDER = workspace.its(WorkspaceService.PUBLIC_WORKSPACE.TYPES_FOLDER);
        workspace.PROJECTS_FOLDER = workspace.its(WorkspaceService.PUBLIC_WORKSPACE.PROJECTS_FOLDER);

        //---------------ADDED FOR PERSISTENCE---------------
        ControlEventEngine.storeControlEvent(new WorkspaceCreationEvent(workspace));
        //---------------ADDED FOR PERSISTENCE---------------

        return workspace;
    }

    protected Workspace() {
        id = IDs.getAndIncrement();

        collaborationNetwork = CollaborationTree.getInstance();
        collaborationNetwork.add(this);

        state = new WorkspaceState(this);
        modelHistoryTree = ModelHistoryTree.getInstance();
    }

    protected Workspace(Workspace parent) {
        this();
        if (parent != null) {
            CollaborationTree.getInstance().attach(parent.id(), id());
            state.setParent(parent);
        }
    }

    public long id() {
        return id;
    }


    //****************************************************************************
    //*** TYPES
    //****************************************************************************
    public InstanceType createInstanceType(String name, Folder folder, InstanceType... superTypes) {
        if (INSTANCE_TYPE !=null && superTypes.length==0) superTypes=new InstanceType[]{INSTANCE_TYPE};
        Assert.isTrue(superTypes.length>0 || Arrays.asList(superTypes).stream().allMatch(superType -> superType.workspace == this), "superType is not of this workspace. Use workspace.its(instanceType) to convert");
        Assert.isTrue(folder == null || folder.workspace == this, "folder is not of this workspace. Use workspace.its(instanceType) to convert");
        Assert.hasText(name, "Name must not be null or empty");

        InstanceType instanceType = new InstanceType(this, META_INSTANCE_TYPE, name, superTypes);
        if (folder != null) folder.addElement(instanceType);
        return instanceType;
    }


    //****************************************************************************
    //*** INSTANCES
    //****************************************************************************

    public Instance createInstance(InstanceType instanceType, String name) {
        if (instanceType.workspace != this) throw new IllegalArgumentException("instance type is not of this workspace. Use workspace.its(instanceType) to convert");
        return instanceType.instantiate(name);
    }


    //****************************************************************************
    //*** FINDING ELEMENT IN WORKSPACE and CONVERTING TO 'ITS' VARIANT
    //****************************************************************************

    /**
     * find an element visible to this workspace. this will first look within the transaction (the latest
     * changes not yet stored), then the cache if we previously already accessed this element, and finally
     * the repository where the element needs to be reconstructed - possible also from parents' operations.
     */
    public <T extends Element> T findElement(Id elementId) {
        Element element = (T) modelHistoryTree.load(elementId, this);
        return (T) element;
    }

    /**
     * get a workspace's view on an element. Since an element may be visible in different workspace
     * and each workspace may modify this element concurrently - perhaps even differently - each workspace
     * needs 'its' own, modifiable view. Essentially, a view is a reconstructed element (a clone)
     */
    public <T extends Element> T its(T element) {
        if (element.workspace == this) return element;
        return (T) findElement(element.id());
    }

    /**
     * this flag tells if an element was modified in this workspace or is simply an unmodified view
     * from the parent's workspace
     */
    public boolean wasModifiedHere(Element element) {
        //check if leafNode of element is same as parent leafNode
        var leafNode = state.getLeafNodeElement(element.id().value(), false);
        if (leafNode == null) {
            return false;
        }
        if(parent() == null){
            return false;
        }
        //var leafNode = element.state.getLeafNode(true);
        var parentLeafNode = parent().state.getLeafNodeElement(element.id().value(), false);
        if(parentLeafNode == null){
            return true;
        }
        return leafNode.id() != parentLeafNode.id();
    }


    //****************************************************************************
    //*** TRANSACTION - changes are not visible to others until transaction concluded
    //****************************************************************************
    public void addToTransaction(Element element, Operation operation, boolean execute) {
        Assert.notNull(element, "Element must not be null");
        modelHistoryTree.add(operation, this, execute);
    }


    /**
     * Concludes transaction - a group of logically related operations that should not be separated. For example,
     * adding an instance to a folder should also add the folder to the instance (opposable operation); or a
     * newly created UML class should also contain all its associations. These logical groups are referred to
     * as transactions. All operations are added to a transaction until conclude transaction happens. Then
     * these operations are stores, committed, or updated together
     */
    public synchronized void concludeTransaction() {
        modelHistoryTree.concludeTransaction(this);
        if (isAutoCommit || notifiedToCommit) {
            commit();
        }
        if (notifiedToUpdate) {
            update();
        }
        for (var childWorkspace : CollaborationTree.getInstance().getChildren(id(), CollaborationTreeEdgeType.WORKSPACE)) {
            if (childWorkspace.data.isAutoUpdate()) {
                childWorkspace.data.notifyToUpdateOnConclude();
            }
        }
    }

//<<<<<<< HEAD
    public void concludeTransactionWithoutNotifyingServices() {
    	modelHistoryTree.concludeTransaction(this, true);
        if (isAutoCommit || notifiedToCommit) {
            commit();
        }
        if (notifiedToUpdate) {
            update();
        }
        for (var childWorkspace : CollaborationTree.getInstance().getChildren(id(), CollaborationTreeEdgeType.WORKSPACE)) {
            if (childWorkspace.data.isAutoUpdate()) {
                childWorkspace.data.notifyToUpdateOnConclude();
            }
        }
    }
//
//    public void notifyWorkspaceListeners(List<Operation> operations) {
//=======
    public void notifyWorkspaceListeners(Collection<Operation> operations) {
//>>>>>>> 15de6d16d63c535f1e95cd1307018031159904aa
        for (WorkspaceListener listener : workspaceListeners)
            listener.handleUpdated(operations);
    }

    public synchronized List<Operation> notifyServiceProviders(Collection<Operation> operations) {
        if(DISABLE_SERVICES){
            return new LinkedList<>();
        }
        var notificationRound = 0;
        var serviceProvidersSortedByPriority = serviceProviders.stream().sorted(Comparator.comparingInt(ServiceProvider::getPriority)).collect(Collectors.toList());
        var serviceOperationsPackage = new LinkedList<Operation>();
        serviceOperationsPackage.addAll(operations);
        var servicePosMap = new HashMap<String, Integer>(); // up to which position a service provider has seen the events
        //var serviceMap = new HashMap<String, List<Operation>>();
        var allServiceOperations = new LinkedList<Operation>();
        int serviceOpTotalCount = 0;
        do {
            for (ServiceProvider provider : serviceProvidersSortedByPriority) {
                Integer seenUntil = 0;
                //if (serviceMap.containsKey(provider.getName())) {
                if (servicePosMap.containsKey(provider.getName())) {
                    //serviceOperationsPackage.removeAll(serviceMap.get(provider.getName()));
                    seenUntil = servicePosMap.get(provider.getName());
                }                                
                //provider.handleServiceRequest(this, serviceOperationsPackage);
                provider.handleServiceRequest(this, serviceOperationsPackage.subList(seenUntil, serviceOperationsPackage.size())); // from the point we have seen till the end
                var currentServiceOperations = state.operationsInTransaction.stream().map(x -> x.data).collect(Collectors.toList());
                serviceOpTotalCount += currentServiceOperations.size();
                allServiceOperations.addAll(currentServiceOperations);
                serviceOperationsPackage.addAll(currentServiceOperations);
                //serviceMap.put(provider.getName(), currentServiceOperations);
                servicePosMap.put(provider.getName(), serviceOperationsPackage.size()); //as we just added the new service operations, the list size is what we have so far seen. 
                
                state.operationsInTransaction.clear();
            }
            //if(notificationRound == 0){
            //    serviceOperationsPackage.removeAll(operations);
            //}
            notificationRound++;
        } while(/*serviceOperationsPackage.size() > 0*/ serviceOpTotalCount > 0  && notificationRound < MAX_SERVICE_NOTIFICATION_ROUNDS);

        return allServiceOperations;
    }

    //****************************************************************************
    //*** OPERATIONS/CHANGES
    //****************************************************************************

    /**
     * updates operations (changes) from the parent workspace. No actual changes are moved but the read-time stamp
     * is set to the current time (hence, this workspace now have access to the latest changes in the parent
     * workspace because it is now allowed to read all operations up to this point). However, the cache is updated
     * with the latest changes, if applicable. Moreover, the operations are propagated further if needed (e.g., all
     * children workspaces that have autoUpdate=true)
     */
    public void update() {
        if (parent() == null) {
            return;
        }
        modelHistoryTree.concludeTransaction(this);
        modelHistoryTree.update(this);
        notifiedToUpdate = false;

        //propagate downwards...
        for (var item : CollaborationTree.getInstance().getChildren(id(), CollaborationTreeEdgeType.WORKSPACE)) {
            if (item.data.isAutoUpdate()) {
                item.data.notifyToUpdateOnConclude();
            }
        }
    }

    /**
     * commit operations (changes) to the parent workspace. The changes are moved from this workspace to the parent
     * workspace but are still accessible by this workspace because the read-time stamp is set to the current time
     * (hence, this workspace now has access to the latest changes in the parent workspace because it is now
     * allowed to read all operations up to this point). Because of this, a commit triggers an automatic update
     * to ensure that the workspace knows about other parent changes. Furthermore, the parent cache is updated
     * with the committed changes. Moreover, the operations are propagated further if needed (e.g., to all parent's
     * children workspaces that have autoUpdate=true)
     */
    public void commit() {
        modelHistoryTree.concludeTransaction(this);
        update();
        modelHistoryTree.commit(this);
        notifiedToCommit = false;

        //propagate upwards...
        if (parent().isAutoCommit()) {
            parent().notifyToCommitOnConclude();
        }
        for (var childWorkspace : CollaborationTree.getInstance().getChildren(parent().id(), CollaborationTreeEdgeType.WORKSPACE)) {
            if (childWorkspace.id() != id() && childWorkspace.data.isAutoUpdate()) {
                childWorkspace.data.notifyToUpdateOnConclude();
            }
        }
    }

    private boolean notifiedToCommit = false;
    public void notifyToCommitOnConclude(){
        if(state.operationsInTransaction.isEmpty()){
            commit();
            notifiedToCommit = false;
        }else{
            notifiedToCommit = true;
        }
    }

    private boolean notifiedToUpdate = false;
    public void notifyToUpdateOnConclude(){
        if(state.operationsInTransaction.isEmpty()){
            update();
            notifiedToUpdate = false;
        }else{
            notifiedToUpdate = true;
        }
    }

    //****************************************************************************
    //*** PARENT CHILDREN WORKSPACES
    //****************************************************************************
//TODO: setParent not implemented in operationTree

//    public void setParent(Workspace newParentWorkspace, boolean autoUpdate, boolean autoCommit) {
//        Assert.notNull(newParentWorkspace, "a workspace must have a parent workspace");
//        Assert.isTrue(!isPublic(), "the parent of the public workspace cannot be changed");
//        Assert.isTrue(children().size() == 0, "Only leave workspaces can change their parent (for now)");
//        //TODO: change parent only if new parent is in ancestor hierarchy
//
//        //---------------ADDED FOR PERSISTENCE---------------
//        ControlEventEngine.storeControlEvent(new ParentChangeEvent(this.id(), newParentWorkspace.id()));
//        //---------------ADDED FOR PERSISTENCE---------------
//
//        if (parentWorkspace != null) parentWorkspace.childWorkspaces.remove(this);
//        parentWorkspace = newParentWorkspace;
//        newParentWorkspace.childWorkspaces.add(this);
//
//        this.operationsInTransaction = new ArrayList<>();
//        this.elementsInCache = new HashMap<>();
//
//        elementsInCache.put(STRING.id(),STRING);
//        elementsInCache.put(INTEGER.id(),INTEGER);
//        elementsInCache.put(REAL.id(),REAL);
//        elementsInCache.put(BOOLEAN.id(),BOOLEAN);
//        elementsInCache.put(DATE.id(),DATE);
//        elementsInCache.put(GENERIC_SINGLE_PROPERTY_TYPE.id(),GENERIC_SINGLE_PROPERTY_TYPE);
//        elementsInCache.put(GENERIC_SET_PROPERTY_TYPE.id(),GENERIC_SET_PROPERTY_TYPE);
//        elementsInCache.put(GENERIC_LIST_PROPERTY_TYPE.id(),GENERIC_LIST_PROPERTY_TYPE);
//        elementsInCache.put(GENERIC_MAP_PROPERTY_TYPE.id(),GENERIC_MAP_PROPERTY_TYPE);
//
//        WorkspaceRepository parentRepository = (parentWorkspace == null) ? null : parentWorkspace.repository;
//        this.isAutoUpdate = autoUpdate;
//        this.isAutoCommit = autoCommit;
//        List<Operation> operations = repository.changeParent(parentRepository);
//
//        notifyServiceListeners(operations);
//        operations = new CompositeUnmodifiableList<>(operations, operationsInTransaction);
//        notifyWorkspaceListeners(operations);
    // }

    public void setAutoUpdate(boolean autoUpdate) {
        if (this.isAutoUpdate == autoUpdate) {
            return;
        }
        this.isAutoUpdate = autoUpdate;
        if (autoUpdate)
            notifyToUpdateOnConclude();
        //else
        //concludeTransaction();

        //---------------ADDED FOR PERSISTENCE---------------
        ControlEventEngine.storeControlEvent(new AutoUpdateChangeEvent(this.id(), autoUpdate));
        //---------------ADDED FOR PERSISTENCE---------------
        CollaborationTree.getInstance().refreshModifiedTimestamp();
    }

    public void setAutoCommit(boolean autoCommit) {
        if (this.isAutoCommit == autoCommit) {
            return;
        }
        this.isAutoCommit = autoCommit;
        if (autoCommit)
            notifyToCommitOnConclude();
        //else
        //concludeTransaction();

        //---------------ADDED FOR PERSISTENCE---------------
        ControlEventEngine.storeControlEvent(new AutoCommitChangeEvent(this.id(), autoCommit));
        //---------------ADDED FOR PERSISTENCE---------------
        CollaborationTree.getInstance().refreshModifiedTimestamp();
    }

    public List<Operation> operations() {
        return modelHistoryTree.operations(this);
    }


    //****************************************************************************
    //*** BASICS
    //****************************************************************************

    public boolean isPublic() { return false; }

    public String name() { return this.name; }
    public void setName(String name) { this.name = name;
        //---------------ADDED FOR PERSISTENCE---------------
        ControlEventEngine.storeControlEvent(new NameChangeEvent(this.id(), name));
        //---------------ADDED FOR PERSISTENCE---------------
    }

    public Workspace parent () {
        return CollaborationTree.getParent(id());
    }
    public List<Workspace> children () {
        return CollaborationTree.getChildren(id());
    }

    public User user() { return user; }
    public Tool tool() { return tool; }

    public Folder rootFolder() { return ROOT_FOLDER; }

    public boolean isAutoUpdate() { return isAutoUpdate; }
    public boolean isAutoCommit() { return isAutoCommit; }

    //public WorkspaceRepository repository() {
//        return repository;
//    }

    public Set<Workspace> descendants(boolean includeSelf) {
        Set<Workspace> descendants = new HashSet<>();

        if (includeSelf) descendants.add(this);
        for (Workspace child : children())
            descendants.addAll(child.descendants(true));

        return descendants;
    }

    public Set<Workspace> ancestors(boolean includeSelf) {
        Set<Workspace> ancestors = new HashSet<>();

        if (includeSelf) ancestors.add(this);
        ancestors.addAll(parent().ancestors(true));

        return ancestors;
    }

    @Override
    public String toString() { return this.name+"{"+id()+"}<Workspace>"; }

    @Override
    public int hashCode() { return (int)id; }

    //****************************************************************************
    //*** USEFUL DEBUGGING METHODS BUT SHOULD NORMALLY NOT BE USED
    //****************************************************************************

    public Set<InstanceType> debugInstanceTypes() {
        Set<InstanceType> instanceTypes = new HashSet<>();
        for (Element element : modelHistoryTree.debugAllElements(this)) {
            if (element instanceof InstanceType)
                instanceTypes.add((InstanceType) element);
        }
        return instanceTypes;
    }

    public Set<Instance> debugInstances() {
        Set<Instance> instances = new HashSet<>();
        for (Element element : modelHistoryTree.debugAllElements(this)) {
            if (element instanceof Instance)
                instances.add((Instance) element);
        }
        return instances;
    }

    public Instance debugInstanceFindByName(String name) {
        for (Element element : modelHistoryTree.debugAllElements(this)) {
            if (element instanceof Instance && ((Instance) element).name().equals(name)) return (Instance) element;
        }
        return null;
    }

    public InstanceType debugInstanceTypeFindByName(String name) {
        for (Element element : modelHistoryTree.debugAllElements(this)) {
            if (element instanceof InstanceType && ((InstanceType) element).name().equals(name)) {
                return (InstanceType)element;
            }
        }
        return null;
    }


    /**
     * returns instance type with a given qualified name starting from this folder (relative path). assumes that the name is unique.
     * if not then it returns the first one finds
     */
    public InstanceType instanceTypeWithQualifiedName(String[] names) {
        if (names.length<1) return null;
        Folder subfolder=this.TYPES_FOLDER;
        for (int i=0; i<names.length-1; i++) {
            subfolder=subfolder.subfolder(names[i]);
        }
        if (subfolder != null)
        	return subfolder.instanceTypeWithName(names[names.length-1]);
        else return null;
    }

    public InstanceType instanceTypeWithQualifiedName(String qualifiedName){
        String[] names = qualifiedName.split(ReservedNames.QUALIFIED_NAME_SEPARATOR);
        return instanceTypeWithQualifiedName(names);

    }

    //****************************************************************************
    //*** STATISTICS
    //****************************************************************************
    public int countElements () {
        return state.elementStates.size();
    }
    public int countOperations() {
        return modelHistoryTree.countOperations(this);
    }
    public int countUnconcluded () {
        return modelHistoryTree.countUnconcluded(this);
    }
    public int countSkips () {
        return modelHistoryTree.countSkips(this);
    }

    ///****************************************
}
