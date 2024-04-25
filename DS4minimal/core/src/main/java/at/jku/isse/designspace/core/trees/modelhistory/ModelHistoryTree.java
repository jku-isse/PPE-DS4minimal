package at.jku.isse.designspace.core.trees.modelhistory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import at.jku.isse.designspace.core.controlflow.ControlEventEngine;
import at.jku.isse.designspace.core.controlflow.controlevents.CommitEvent;
import at.jku.isse.designspace.core.controlflow.controlevents.OperationEvent;
import at.jku.isse.designspace.core.controlflow.controlevents.TransactionEvent;
import at.jku.isse.designspace.core.controlflow.controlevents.UpdateEvent;
import at.jku.isse.designspace.core.events.ElementCreate;
import at.jku.isse.designspace.core.events.ElementUpdate;
import at.jku.isse.designspace.core.events.Operation;
import at.jku.isse.designspace.core.events.PropertyCreate;
import at.jku.isse.designspace.core.model.Element;
import at.jku.isse.designspace.core.model.Id;
import at.jku.isse.designspace.core.model.Property;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.trees.abstracts.AbstractSynchronizedTree;
import at.jku.isse.designspace.core.trees.collaboration.CollaborationTree;
import at.jku.isse.designspace.core.trees.modelhistory.caching.WorkspaceState;
import at.jku.isse.designspace.core.trees.modelhistory.conflict.ConflictTier;
import at.jku.isse.designspace.core.trees.modelhistory.conflict.MergeReport;

public class ModelHistoryTree extends AbstractSynchronizedTree<ModelHistoryTreeFactory, ModelHistoryTreeNode, ModelHistoryTreeEdgeType, Operation> {
    private static long CONCLUSION_ID = 1;

    private static ModelHistoryTree instance = null;

    public static LinkedList<MergeReport> reports = new LinkedList<>();

    private ModelHistoryTree() {
        super(new ModelHistoryTreeFactory());
    }

    public static ModelHistoryTree getInstance() {
        if (instance == null) {
            instance = new ModelHistoryTree();
        }
        return instance;
    }

    private Map<Long, List<ModelHistoryTreeNode>> skipNodes = new HashMap<>();

    public ModelHistoryTreeNode add (Operation operation, Workspace workspace) {
        return add( operation,  workspace,  true);
    }

    public ModelHistoryTreeNode add (Operation operation, Workspace workspace, boolean execute) {
        ModelHistoryTreeNode newOperationNode = super.add(operation);
        if (newOperationNode == null) {
            return null;
        }

        //---------------ADDED FOR PERSISTENCE---------------
        ControlEventEngine.storeControlEvent(new OperationEvent(operation, workspace.id()));
        //---------------ADDED FOR PERSISTENCE---------------

        WorkspaceState workspaceState = workspace.state;
        var leafNode = workspaceState.getLeafNode(!operation.isConcluded());
        attach(leafNode, newOperationNode, ModelHistoryTreeEdgeType.OPERATION);
        var elementState = workspace.state.getElementState(operation.elementId(), true);
        if (elementState != null) {
            var leafNodeElement = elementState.getLeafNode(!operation.isConcluded());
            attach(leafNodeElement, newOperationNode, ModelHistoryTreeEdgeType.ELEMENT);
            if (elementState.creationNode == null && operation instanceof ElementCreate) {
                elementState.creationNode = newOperationNode;
            }
            if (operation instanceof ElementUpdate) {
                var propertyState = elementState.getPropertyState(((ElementUpdate) operation).name(), true);
                if (propertyState != null) {
                    var leafNodeProperty = propertyState.getLeafNode(!operation.isConcluded());
                    attach(leafNodeProperty, newOperationNode, ModelHistoryTreeEdgeType.PROPERTY);
                    if (propertyState.creationNode == null && operation instanceof PropertyCreate) {
                        propertyState.creationNode = newOperationNode;
                    }
                }
            }
        }

        if(execute){
            workspaceState.updateAndExecute(newOperationNode);
        }else{
            workspaceState.update(newOperationNode);
        }

        if (!operation.isConcluded() && !operation.isAutoCreated()) {
            workspaceState.operationsInTransaction.add(newOperationNode);
            workspaceState.lastInTransaction = newOperationNode;
        }

        return newOperationNode;
    }

    public ModelHistoryTreeNode attach (Operation operation, Workspace workspace) {
        ModelHistoryTreeNode newOperationNode = super.add(operation);
        if (newOperationNode == null) {
            return null;
        }

        //---------------ADDED FOR PERSISTENCE---------------
        ControlEventEngine.storeControlEvent(new OperationEvent(operation, workspace.id()));
        //---------------ADDED FOR PERSISTENCE---------------

        WorkspaceState workspaceState = workspace.state;
        var leafNode = workspaceState.getLeafNode(!operation.isConcluded());
        attach(leafNode, newOperationNode, ModelHistoryTreeEdgeType.OPERATION);
        var elementState = workspace.state.getElementState(operation.elementId(), true);
        if (elementState != null) {
            var leafNodeElement = elementState.getLeafNode(!operation.isConcluded());
            attach(leafNodeElement, newOperationNode, ModelHistoryTreeEdgeType.ELEMENT);
//            if (elementState.creationNode == null && operation instanceof ElementCreate) {
//                elementState.creationNode = newOperationNode;
//            }
            if (operation instanceof ElementUpdate) {
                var propertyState = elementState.getPropertyState(((ElementUpdate) operation).name(), true);
                if (propertyState != null) {
                    var leafNodeProperty = propertyState.getLeafNode(!operation.isConcluded());
                    attach(leafNodeProperty, newOperationNode, ModelHistoryTreeEdgeType.PROPERTY);
//                    if (propertyState.creationNode == null && operation instanceof PropertyCreate) {
//                        propertyState.creationNode = newOperationNode;
//                    }
                }
            }
        }

        return newOperationNode;
    }

    public static long getNextConclusionId(){
            return CONCLUSION_ID++;
    }
    public static long getCurrentConclusionId(){
        return CONCLUSION_ID;
    }
    public void concludeTransaction(Workspace workspace){
        concludeTransaction(workspace, false);
    }
    public void concludeTransaction(Workspace workspace, boolean isReplay) {
        WorkspaceState workspaceState = workspace.state;
        if (workspaceState.operationsInTransaction.size() == 0) {
            return;
        }
        
        long conclusionId = isReplay ? -1 : getNextConclusionId();

        if (!isReplay) {
        //---------------ADDED FOR PERSISTENCE---------------
        ControlEventEngine.storeControlEvent(new TransactionEvent(workspace.id()));
        //---------------ADDED FOR PERSISTENCE---------------
        }
        var concludedOperationsFromWorkspace = workspaceState.operationsInTransaction.stream().map(x -> x.data).collect(Collectors.toList());
        var allConcludedOperations = concludedOperationsFromWorkspace.stream().collect(Collectors.toList());
        workspaceState.operationsInTransaction.clear();

        allConcludedOperations.forEach(x -> {
        	if (!isReplay) {
        		x.conclude(conclusionId);
        	}
            var elementState = workspace.state.getElementState(x.elementId(), false);
            if (elementState.lastInTransaction != null) {
                elementState.last = elementState.lastInTransaction;
                elementState.lastInTransaction = null;
            }
            if (x instanceof ElementUpdate) {
                var propertyState = elementState.getPropertyState(((ElementUpdate) x).name(), false);
                if (propertyState.lastInTransaction != null) {
                    propertyState.last = propertyState.lastInTransaction;
                    propertyState.lastInTransaction = null;
                }
            }
        });
        if (!isReplay) {
        	var serviceOperations = workspace.notifyServiceProviders(concludedOperationsFromWorkspace);
        	if (serviceOperations.size() > 0) {
        		allConcludedOperations.addAll(serviceOperations);
        	}
        	serviceOperations.forEach(x -> {
        		x.conclude(conclusionId);
        		var elementState = workspace.state.getElementState(x.elementId(), false);
        		if (elementState.lastInTransaction != null) {
        			elementState.last = elementState.lastInTransaction;
        			elementState.lastInTransaction = null;
        		}
        		if (x instanceof ElementUpdate) {
        			var propertyState = elementState.getPropertyState(((ElementUpdate) x).name(), false);
        			if (propertyState.lastInTransaction != null) {
        				propertyState.last = propertyState.lastInTransaction;
        				propertyState.lastInTransaction = null;
        			}
        		}
        	});
        }

        workspaceState.last = workspaceState.lastInTransaction;
        workspaceState.lastInTransaction = null;
        if (!isReplay) {
        	workspace.notifyWorkspaceListeners(allConcludedOperations);
        	refreshModifiedTimestamp();
        }
    }

    public int countOperations(Workspace workspace){
        return getNodesFollowSkips(workspace.state.branchingPoint, workspace.state.getLeafNode(false), ModelHistoryTreeEdgeType.OPERATION, workspace).size();
    }

    public int countUnconcluded(Workspace workspace){
        return getNodes(workspace.state.getLeafNode(false), workspace.state.getLeafNode(true), ModelHistoryTreeEdgeType.OPERATION).size()-1;
    }

    public int countSkips(Workspace workspace){
        if(!skipNodes.containsKey(workspace.id())){
            return 0;
        }
        return skipNodes.get(workspace.id()).size();
    }
    public int countOperations(){
        return nodes.size();
    }

    public List<ModelHistoryTreeNode> add(List<Operation> operations, Workspace workspace) {
        List<ModelHistoryTreeNode> addedNodes = new ArrayList<>();
        operations.forEach(x -> addedNodes.add(add(x, workspace)));
        return addedNodes;
    }

    private void recursiveAddSkip(Workspace workspace, ModelHistoryTreeNode firstNodeAfterBranch, ModelHistoryTreeNode branchingPoint){
        for (var childWorkspace : workspace.children()) {
            //CREATE SKIP EDGE FOR EVERY CHILD WS
            firstNodeAfterBranch.parentSkip.put(childWorkspace.id(), branchingPoint);
            if(skipNodes.containsKey(childWorkspace.id())){
                skipNodes.get(childWorkspace.id()).add(firstNodeAfterBranch);
            }else{
                var list = new LinkedList<ModelHistoryTreeNode>();
                list.add(firstNodeAfterBranch);
                skipNodes.put(childWorkspace.id(), list);
            }

            recursiveAddSkip(childWorkspace, firstNodeAfterBranch, branchingPoint);
        }
    }

    private void recursiveAddElementSkip(Workspace workspace, ModelHistoryTreeNode skipNode, ModelHistoryTreeNode parentSkipNode){
        for (var childWorkspace : workspace.children()) {
            //CREATE SKIP EDGE FOR EVERY CHILD WS
            skipNode.parentElementSkip.put(workspace.id(), parentSkipNode.getParent(ModelHistoryTreeEdgeType.ELEMENT));
            recursiveAddElementSkip(childWorkspace, skipNode, parentSkipNode);
        }
    }

    private void recursiveAddPropertySkip(Workspace workspace, ModelHistoryTreeNode skipNode, ModelHistoryTreeNode parentSkipNode){
        for (var childWorkspace : workspace.children()) {
            //CREATE SKIP EDGE FOR EVERY CHILD WS
            skipNode.parentPropertySkip.put(workspace.id(), parentSkipNode.getParent(ModelHistoryTreeEdgeType.PROPERTY));
            recursiveAddPropertySkip(childWorkspace, skipNode, parentSkipNode);
        }
    }

    public void update (Workspace workspace){
        var child = workspace;
        var parent = child.parent();
        if (parent == null) {
            return;
        }
        var overwriteParent = CollaborationTree.getSetting(workspace.id()).overwriteParent;

        //---------------ADDED FOR PERSISTENCE---------------
        ControlEventEngine.storeControlEvent(new UpdateEvent(workspace.id()));
        //---------------ADDED FOR PERSISTENCE---------------


        var branchingPoint = child.state.branchingPoint;

        var parentLeafNode = parent.state.getLeafNode(false);
        var childLeafNode = child.state.getLeafNode(false);

        var parentNodes = getNodesFollowSkips(branchingPoint, parentLeafNode, ModelHistoryTreeEdgeType.OPERATION, parent);
        var childNodes = getNodesFollowSkips(branchingPoint, childLeafNode, ModelHistoryTreeEdgeType.OPERATION, child);
        var childSkipNodes = getOfParentNotUpdatedSkipNodes(child);

        //childSkipNodes.addAll(parentNodes);
        //parentNodes = childSkipNodes;

        parentNodes.addAll(0,childSkipNodes);

        MergeReport report = new MergeReport(parent, child, parentNodes, childNodes, overwriteParent);
        reports.add(report);

        LinkedList<ModelHistoryTreeNode> attachedPreNodes = new LinkedList<>();

        //////////////// INJECT PRE NODES BETWEEN PARENT AND CHILD FOR DELAYED UPDATE OF PARENT //////////////////

        boolean operationsInjected = false;
        for (var conflict : report.result.conflicts) {
            if (conflict.tier != ConflictTier.SECOND || conflict.resolutionPrepareUpdate.size() == 0) {
                continue;
            }

            ModelHistoryTreeNode lastInjected = null;
            ModelHistoryTreeNode firstInjected = null;

            for (var injectOperation : conflict.resolutionPrepareUpdate) {
                var attachedNode = attach(injectOperation, parent);
                attachedPreNodes.add(attachedNode);
                if (lastInjected != null) {
                    attachedNode.setParent(ModelHistoryTreeEdgeType.OPERATION, lastInjected);
                    attachedNode.setParent(ModelHistoryTreeEdgeType.ELEMENT, lastInjected);
                    attachedNode.setParent(ModelHistoryTreeEdgeType.PROPERTY, lastInjected);
                } else {
                    firstInjected = attachedNode;
                }
                lastInjected = attachedNode;
            }

            operationsInjected = firstInjected != null;

            firstInjected.setParent(ModelHistoryTreeEdgeType.OPERATION, parentLeafNode);
            firstInjected.setParent(ModelHistoryTreeEdgeType.ELEMENT, parentLeafNode);
            firstInjected.setParent(ModelHistoryTreeEdgeType.PROPERTY, parentLeafNode);

            var firstTargetNode = childNodes.get(0);
            firstTargetNode.setParent(ModelHistoryTreeEdgeType.OPERATION, lastInjected);

            var elementAlreadyReattachedMap = new HashMap<Long, ModelHistoryTreeNode>();
            var propertyAlreadyReattachedMap = new HashMap<String, ModelHistoryTreeNode>();

            for (var childNode : childNodes) {
                if (firstInjected.idElement() == childNode.idElement() && !elementAlreadyReattachedMap.containsKey(childNode.idElement())) {
                    elementAlreadyReattachedMap.put(childNode.idElement(), childNode);
                    //targetNodes and attachedPreNode have same parent, so they conflict
                    childNode.setParent(ModelHistoryTreeEdgeType.ELEMENT, lastInjected);
                    if (firstInjected.idProperty() == childNode.idProperty() && !propertyAlreadyReattachedMap.containsKey(childNode.idProperty())) {
                        propertyAlreadyReattachedMap.put(childNode.idProperty(), childNode);
                        childNode.setParent(ModelHistoryTreeEdgeType.PROPERTY, lastInjected);
                    }
                }
            }
        }

            //////////////// REPOSITION ELEMENT AND PROPERTY EDGES OF MOVED CHILD NODES //////////////////

            //add all operations that need to be executed to this list to get the To workspace to the correct state
            List<ModelHistoryTreeNode> executeOperationsOnChildWorkspace = new LinkedList<>();

            //add all operations that need to be updated to this list to get the To workspace to the correct state
            List<ModelHistoryTreeNode> updateOperationsOnChildWorkspace = new LinkedList<>();

            var targetParentElementWorkspaceOperationsSinceBranchMap = childNodes.stream().filter(node -> node.getParent(ModelHistoryTreeEdgeType.ELEMENT) != null).collect(Collectors.toMap(node -> node.getParent(ModelHistoryTreeEdgeType.ELEMENT).id(), Function.identity()));
            //operations from child branch, mapped by their parentProperty Property.Id, with this we can find out if there should be any parent changes
            var toParentPropertyWorkspaceOperationsSinceBranchMap = childNodes.stream().filter(node -> node.getParent(ModelHistoryTreeEdgeType.PROPERTY) != null).collect(Collectors.toMap(node -> node.getParent(ModelHistoryTreeEdgeType.PROPERTY).id(), Function.identity()));

            //if there are skipped nodes or branch nodes in the parent, we need to reposition all edges (Operation, Element and Property edges)
            for (var parentNode : parentNodes) {
                //skip node if it is the branching node, or if it is the first element node, or if no node from the child branch has any parent that has to be changed e.g. edge reattached to new parent
                if (parentNode.getParent(ModelHistoryTreeEdgeType.ELEMENT) == null || !targetParentElementWorkspaceOperationsSinceBranchMap.containsKey(parentNode.getParent(ModelHistoryTreeEdgeType.ELEMENT).id()) ) {
                    executeOperationsOnChildWorkspace.add(parentNode);
                    if(branchingPoint == childLeafNode && !childSkipNodes.contains(parentNode) ){
                        updateOperationsOnChildWorkspace.add(parentNode);
                    }
                    continue;
                }

                //executeOperationsOnChildWorkspace.add(parentNode);

                //if from nodes parent is found in toBranchNodes parents, reset the element parent to the new from node.
                var toNode = targetParentElementWorkspaceOperationsSinceBranchMap.get(parentNode.getParent(ModelHistoryTreeEdgeType.ELEMENT).id());
                var fromLastNodeElement = parent.state.getCurrentElementNode(parentNode.idElement(), false);

                //edge needs to be moved-> some conflict happened
                if(fromLastNodeElement != null && toNode != null && toNode.getParent(ModelHistoryTreeEdgeType.ELEMENT).id() != fromLastNodeElement.id()){
                    fromLastNodeElement.attach(toNode, ModelHistoryTreeEdgeType.ELEMENT);

                    for (var childWorkspace : child.children()) {
                        //CREATE SKIP ELEMENT EDGE FOR EVERY CHILD WS
                        recursiveAddElementSkip(childWorkspace, toNode, parentNode);
                        //toNode.parentElementSkip.put(childWorkspace.id(), node.getParent(OperationTreeEdgeType.ELEMENT));
                    }

                    if (toNode.data instanceof ElementUpdate) {
                        if (parentNode.getParent(ModelHistoryTreeEdgeType.PROPERTY) == null || !toParentPropertyWorkspaceOperationsSinceBranchMap.containsKey(parentNode.getParent(ModelHistoryTreeEdgeType.PROPERTY).id())) {
                            continue;
                        }
                        var fromLastNodeProperty = parent.state.getCurrentPropertyNode(parentNode.idElement(), ((ElementUpdate) toNode.data).name(), false);
                        fromLastNodeProperty.attach(toNode, ModelHistoryTreeEdgeType.PROPERTY);

                        //create elementskipedges only if commiting to parent: target and childs || update: only childs
                        for (var childWorkspace : child.children()) {
                            //CREATE SKIP ELEMENT EDGE FOR EVERY CHILD WS
                            recursiveAddPropertySkip(childWorkspace, toNode, parentNode);
                            //toNode.parentPropertySkip.put(childWorkspace.id(), node.getParent(OperationTreeEdgeType.PROPERTY));
                        }
                    }
                }
            }


        //////////////// REATTACHING WHOLE BRANCH //////////////////


        var firstNodeAfterBranchChild = childNodes.size() == 0 ? null : childNodes.get(0);

        //if moving operation edge of whole branch
        if (firstNodeAfterBranchChild != null) {
            if(!operationsInjected){
                parentLeafNode.attach(firstNodeAfterBranchChild, ModelHistoryTreeEdgeType.OPERATION);
            }

            //create operation skip edges
            //check if commiting to parent and if parent has new operations. Otherwise no skip is needed.
            if (firstNodeAfterBranchChild.getParent(ModelHistoryTreeEdgeType.OPERATION).id() != branchingPoint.id()) {
                recursiveAddSkip(child, firstNodeAfterBranchChild, branchingPoint);
            }
        }

        //if not moving means there is no branch only a different state in the same branch, thus removing edges
        if (skipNodes.containsKey(child.id())) {
            for (var skipNode : skipNodes.get(child.id()).stream().collect(Collectors.toList())) {
                if(!skipNode.parentSkip.containsKey(child.parent().id()))
                {
                    skipNode.parentSkip.remove(child.id());
                    skipNode.parentElementSkip.remove(child.id());
                    skipNode.parentPropertySkip.remove(child.id());
                    skipNodes.get(child.id()).remove(skipNode);
                    if(skipNodes.get(child.id()).size() == 0){
                        skipNodes.remove(child.id());
                    }
                }
            }
        }

        if (updateOperationsOnChildWorkspace.size() == executeOperationsOnChildWorkspace.size()) {
            updateOperationsOnChildWorkspace.forEach(x -> {
                child.state.update(x);
                child.state.execute(x);
            });
        }else{
            updateOperationsOnChildWorkspace.forEach(x -> child.state.update(x));
            executeOperationsOnChildWorkspace.forEach(x -> child.state.execute(x));
        }

        for (var conflict : report.result.conflicts) {
            if(conflict.merge){
                conflict.sourceNodes.forEach(x -> {
                    child.state.execute(x);
                });
            }
            conflict.resolutionUpdate.forEach(x -> add(x, child));
        }

        child.state.branchingPoint = parent.state.last;

        if(!parent.state.operationsInTransaction.isEmpty() && parent.state.operationsInTransaction.get(0).getParent(ModelHistoryTreeEdgeType.OPERATION).id() != parent.state.last.id()){
            parent.state.last.attach(parent.state.operationsInTransaction.get(0), ModelHistoryTreeEdgeType.OPERATION);
            //ALSO ATTACH ELEMENT AND PROPERTY NODES OF CONCLUDES
            //ATTACH SERVICE NODES IN EXTRA LIST
        }

        //NOTIFICATIONS
//<<<<<<< HEAD:core/src/main/java/at/jku/isse/designspace/core/trees/operation/OperationTree.java
//        if (commitToParent){
//            var newOperations = executeOperationsOnSourceWorkspace.stream().map(x -> x.data).collect(Collectors.toList());
//            if(!newOperations.isEmpty()) {
//                source.notifyWorkspaceListeners(newOperations);
//                source.notifyServiceListeners(newOperations);
//                if(!source.state.operationsInTransaction.isEmpty()){
//                    //this conclude would lead to an infinite cycle of updates, if the transactions are empty
//                    source.concludeTransaction();
//                }
//            }
//        } else {
//            var newOperations = executeOperationsOnTargetWorkspace.stream().map(x -> x.data).collect(Collectors.toList());
//
//            if(!newOperations.isEmpty() && ControlEventEngine.isInitialized()) {
//                target.notifyWorkspaceListeners(newOperations);
//                target.notifyServiceListeners(newOperations);
//                if(!target.state.operationsInTransaction.isEmpty()){
//                    //this conclude would lead to an infinite cycle of updates, if the transactions are empty
//                    target.concludeTransaction();
//                }
//=======
        var newOperations = executeOperationsOnChildWorkspace.stream().map(x -> x.data).collect(Collectors.toList());

        if(!newOperations.isEmpty()) {
            child.notifyWorkspaceListeners(newOperations);
            child.notifyServiceProviders(newOperations);
            if(!child.state.operationsInTransaction.isEmpty()) {
                //this conclude would lead to an infinite cycle of updates, if the transactions are empty
                child.concludeTransaction();
//>>>>>>> 15de6d16d63c535f1e95cd1307018031159904aa:core/src/main/java/at/jku/isse/designspace/core/trees/modelhistory/ModelHistoryTree.java
            }
        }

        refreshModifiedTimestamp();
    }

    public void commit (Workspace workspace){
        var child = workspace;
        var parent = child.parent();
        if (parent == null) {
            return;
        }

        //---------------ADDED FOR PERSISTENCE---------------
        ControlEventEngine.storeControlEvent(new CommitEvent(workspace.id()));

        //---------------ADDED FOR PERSISTENCE---------------

        var branchingPoint = child.state.branchingPoint;

        if (branchingPoint == null) {
            throw new IllegalStateException("Can't find branching node in child!");
        }

        if (branchingPoint != parent.state.getLeafNode(false)) {
            throw new IllegalStateException("Can't commit! Child is not up to date!");
        }

        var parentLeafNode = parent.state.getLeafNode(false);
        var childLeafNode = child.state.getLeafNode(false);

        var parentNodes = getNodesFollowSkips(branchingPoint, parentLeafNode, ModelHistoryTreeEdgeType.OPERATION, parent);
        var childNodes = getNodesFollowSkips(branchingPoint, childLeafNode, ModelHistoryTreeEdgeType.OPERATION, child);

        childNodes.forEach(x -> {
            parent.state.update(x);
            parent.state.execute(x);
        });

        child.state.branchingPoint = parent.state.last;

       var newOperations = childNodes.stream().map(x -> x.data).collect(Collectors.toList());
        if(!newOperations.isEmpty()) {
            parent.notifyWorkspaceListeners(newOperations);
            parent.notifyServiceProviders(newOperations);
            if(!parent.state.operationsInTransaction.isEmpty()){
                //this conclude would lead to an infinite cycle of updates, if the transactions are empty
                parent.concludeTransaction();
            }
        }
    }

    public List<ModelHistoryTreeNode> getNodes (ModelHistoryTreeNode fromAncestor, ModelHistoryTreeNode toDescendant, ModelHistoryTreeEdgeType edgeType){
        var allNodes = super.getNodes(fromAncestor, toDescendant, edgeType);
        return allNodes;
    }

    public List<ModelHistoryTreeNode> getNodesFollowSkips (ModelHistoryTreeNode fromAncestor, ModelHistoryTreeNode
    toDescendant, ModelHistoryTreeEdgeType edgeType, Workspace workspace){

        var nodesFound = new ArrayList<ModelHistoryTreeNode>();
        var currentNode = toDescendant;
        while (currentNode != null && currentNode != fromAncestor) {
            nodesFound.add(currentNode);
            switch (edgeType) {
                case OPERATION:
                    if (currentNode.parentSkip.containsKey(workspace.id())) {
                        currentNode = currentNode.parentSkip.get(workspace.id());
                        continue;
                    }
                    break;
                case ELEMENT:
                    if (currentNode.parentElementSkip.containsKey(workspace.id())) {
                        currentNode = currentNode.parentElementSkip.get(workspace.id());
                        continue;
                    }
                    break;
                case PROPERTY:
                    if (currentNode.parentPropertySkip.containsKey(workspace.id())) {
                        currentNode = currentNode.parentPropertySkip.get(workspace.id());
                        continue;
                    }
                    break;
            }

            currentNode = currentNode.getParent(edgeType);
        }

        if (currentNode == null && fromAncestor != null) { // fromAncestor not reachable from toDescendant
            throw new IllegalStateException("Ancestor is not reachable from the descendant!");
        }

        if (fromAncestor != null) {
            //nodesFound.add(fromAncestor);
        }

        Collections.reverse(nodesFound);
        return nodesFound;
    }

    public List<ModelHistoryTreeNode> getOfParentNotUpdatedSkipNodes(Workspace workspace){
        var nodesFound = new ArrayList<ModelHistoryTreeNode>();
        if(workspace.parent() == null || !skipNodes.containsKey(workspace.id())){
            return nodesFound;
        }

        for (var skipNode : skipNodes.get(workspace.id())) {
            if(!skipNode.parentSkip.containsKey(workspace.parent().id()) && skipNode.parentSkip.get(workspace.id()) != null)
            {
                var last = skipNode.parentSkip.get(workspace.id());
                var buff = skipNode.getParent(ModelHistoryTreeEdgeType.OPERATION);
                while(buff != last){
                    if(buff == null){
                        throw new IllegalStateException("SkipNode never reached!");
                    }
                    nodesFound.add(buff);
                    buff = buff.getParent(ModelHistoryTreeEdgeType.OPERATION);
                }
            }
        }

       /* var branchingPoint = workspace.state.branchingPoint;
        while(branchingPoint.parentSkip.containsKey(workspace.id()) && !branchingPoint.parentSkip.containsKey(workspace.parent().id())){
            var searchNode = branchingPoint.getParent(OperationTreeEdgeType.DELTA);
            var parentSkipNode = branchingPoint.parentSkip.get(workspace.id());
            while(searchNode != parentSkipNode){
                nodesFound.add(searchNode);
                searchNode = searchNode.getParent(OperationTreeEdgeType.DELTA);
            }
            branchingPoint = branchingPoint.parentSkip.get(workspace.id());
        }*/
        Collections.reverse(nodesFound);
        return nodesFound;
    }

    public List<ModelHistoryTreeNode> getSkipNodes (ModelHistoryTreeNode fromAncestor, ModelHistoryTreeNode
    toDescendant, ModelHistoryTreeEdgeType edgeType, Workspace workspace, boolean edgesMustIncludeParentSkip){

        var nodesFound = new ArrayList<ModelHistoryTreeNode>();
        boolean traversingThroughSkipArea = false;
        ModelHistoryTreeNode parentSkipNode = null;
        var currentNode = toDescendant;
        while (currentNode != null && currentNode != fromAncestor) {
            switch (edgeType) {
                case OPERATION:
                    if (currentNode.parentSkip.containsKey(workspace.id()) && (edgesMustIncludeParentSkip && currentNode.parentSkip.containsKey(workspace.parent().id()))) {
                        //currentNode = currentNode.parentSkip.get(workspace.id());
                        traversingThroughSkipArea = true;
                        parentSkipNode = currentNode.parentSkip.get(workspace.id());
                    }
                    break;
                case ELEMENT:
                    if (currentNode.parentElementSkip.containsKey(workspace.id())) {
                        //currentNode = currentNode.parentElementSkip.get(workspace.id());
                        traversingThroughSkipArea = true;
                        parentSkipNode = currentNode.parentElementSkip.get(workspace.id());
                    }
                    break;
                case PROPERTY:
                    if (currentNode.parentPropertySkip.containsKey(workspace.id())) {
                        //currentNode = currentNode.parentPropertySkip.get(workspace.id());
                        traversingThroughSkipArea = true;
                        parentSkipNode = currentNode.parentPropertySkip.get(workspace.id());
                    }
                    break;
            }

            currentNode = currentNode.getParent(edgeType);

            if (parentSkipNode == currentNode) {
                traversingThroughSkipArea = false;
                parentSkipNode = null;
            }

            if (traversingThroughSkipArea) {
                nodesFound.add(currentNode);
            }
        }

        if (currentNode == null && fromAncestor != null) { // fromAncestor not reachable from toDescendant
            return null;
        }

        Collections.reverse(nodesFound);
        return nodesFound;

    }

    public List<ModelHistoryTreeNode> getOperationsInTransaction(Workspace workspace){
        if(workspace.state.lastInTransaction == null){
            return new LinkedList<ModelHistoryTreeNode>();
        }
        var list = getNodes(workspace.state.last, workspace.state.lastInTransaction, ModelHistoryTreeEdgeType.OPERATION);
        list.remove(workspace.state.last);
        return list;
    }

    public String exportAsJSON () {
        try {
            JSONArray nodesList = new JSONArray();
            JSONArray stateNodesList = new JSONArray();

            JSONArray elementRootNodesList = new JSONArray();
            JSONArray propertyRootNodesList = new JSONArray();

            JSONArray skipEdgesList = new JSONArray();
            JSONArray skipEdgesElementList = new JSONArray();
            JSONArray skipEdgesPropertyList = new JSONArray();

            for (var ws : CollaborationTree.getInstance().getAllData()) {
                JSONObject wsState = new JSONObject();
                wsState.put("id",ws.id());
                wsState.put("leafNodeId",ws.state.getLeafNode(true).id());

                JSONArray elementLeafList = new JSONArray();
                var elementStateKeys = new LinkedList<>(ws.state.elementStates.keySet());
                for (var elementKey : elementStateKeys) {
                    JSONObject elementObject = new JSONObject();
                    elementObject.put("id", elementKey);
                    elementObject.put("leafNodeId", ws.state.elementStates.get(elementKey).getLeafNode(true).id());

                    JSONArray propertyLeafList = new JSONArray();
                    var propertyStateKeys = new LinkedList<>(ws.state.elementStates.get(elementKey).propertyStates.keySet());
                    for (var propertyKey : propertyStateKeys) {
                        JSONObject propertyObject = new JSONObject();
                        propertyObject.put("name", propertyKey);
                        //propertyObject.put("leafNodeId", ws.state.elementStates.get(elementKey).propertyStates.get(propertyKey).last.id());
                        propertyObject.put("leafNodeId", ws.state.elementStates.get(elementKey).propertyStates.get(propertyKey).getLeafNode(true).id());
                        propertyLeafList.put(propertyObject);
                    }
                    elementObject.put("propertyLeafs",propertyLeafList);
                    elementLeafList.put(elementObject);
                }
                wsState.put("elementLeafs", elementLeafList);
                stateNodesList.put(wsState);
            }

            var nodesCopy = new LinkedList<>(nodes.values());
            for (ModelHistoryTreeNode node : nodesCopy) {
                JSONObject nodeObject = new JSONObject();
                nodeObject.put("id", node.id());
                nodeObject.put("conclusionId", node.data.getConclusionId());
                nodeObject.put("name", node.label());
                nodeObject.put("elementId", node.data.elementId());
                var className = node.data.getClass().getSimpleName();
                nodeObject.put("type", className);
                nodeObject.put("description", node.data.toString());

                //XXXXXXXXXXXX SKIP EDGES XXXXXXXXXXXX//

                Hashtable<Long, JSONArray> parentSkips = new Hashtable<>();
                for (var skipWSId : node.parentSkip.keySet()) {

                    var skipParentNode = node.parentSkip.get(skipWSId);

                    JSONArray skipWSArray = null;
                    if (parentSkips.containsKey(skipParentNode.id())) {
                        skipWSArray = parentSkips.get(skipParentNode.id());
                    } else {
                        JSONObject parentNodeObject = new JSONObject();
                        parentNodeObject.put("node_id", node.id());
                        parentNodeObject.put("parentnode_id", skipParentNode.id());
                        skipEdgesList.put(parentNodeObject);

                        skipWSArray = new JSONArray();
                        parentNodeObject.put("ws_ids", skipWSArray);
                        parentSkips.put(skipParentNode.id(), skipWSArray);
                    }
                    var wsName = CollaborationTree.get(skipWSId).name();
                    skipWSArray.put(wsName);
                }

                //XXXXXXXXXXXX SKIP EDGES ELEMENTXXXXXXXXXXXX//

                parentSkips = new Hashtable<>();
                for (var skipWSId : node.parentElementSkip.keySet()) {

                    var skipParentNode = node.parentElementSkip.get(skipWSId);

                    JSONArray skipWSArray = null;
                    if (parentSkips.containsKey(skipParentNode.id())) {
                        skipWSArray = parentSkips.get(skipParentNode.id());
                    } else {
                        JSONObject parentNodeObject = new JSONObject();
                        parentNodeObject.put("elementId", node.idElement());
                        parentNodeObject.put("node_id", node.id());
                        parentNodeObject.put("parentnode_id", skipParentNode.id());
                        skipEdgesElementList.put(parentNodeObject);

                        skipWSArray = new JSONArray();
                        parentNodeObject.put("ws_ids", skipWSArray);
                        parentSkips.put(skipParentNode.id(), skipWSArray);
                    }
                    var wsName = CollaborationTree.get(skipWSId).name();
                    skipWSArray.put(wsName);
                }

                //XXXXXXXXXXXX SKIP EDGES PROPERTYXXXXXXXXXXXX//

                parentSkips = new Hashtable<>();
                for (var skipWSId : node.parentPropertySkip.keySet()) {

                    var skipParentNode = node.parentPropertySkip.get(skipWSId);

                    JSONArray skipWSArray = null;
                    if (parentSkips.containsKey(skipParentNode.id())) {
                        skipWSArray = parentSkips.get(skipParentNode.id());
                    } else {
                        JSONObject parentNodeObject = new JSONObject();
                        parentNodeObject.put("propertyId", node.idProperty());
                        parentNodeObject.put("node_id", node.id());
                        parentNodeObject.put("parentnode_id", skipParentNode.id());
                        skipEdgesPropertyList.put(parentNodeObject);

                        skipWSArray = new JSONArray();
                        parentNodeObject.put("ws_ids", skipWSArray);
                        parentSkips.put(skipParentNode.id(), skipWSArray);
                    }
                    var wsName = CollaborationTree.get(skipWSId).name();
                    skipWSArray.put(wsName);
                }

                //XXXXXXXXXXXX DELTA CHILD NODES XXXXXXXXXXXX//

                JSONArray nodesListChilds = new JSONArray();
                for (var child : node.getChildren(ModelHistoryTreeEdgeType.OPERATION)) {
                    nodesListChilds.put(child.id());
                }
                if (nodesListChilds.length() > 0) {
                    nodeObject.put("children", nodesListChilds);
                }
                var parentNode = node.getParent(ModelHistoryTreeEdgeType.OPERATION);
                if (parentNode != null) {
                    nodeObject.put("parentId", parentNode.id());
                }

                //XXXXXXXXXXXX ELEMENT CHILD NODES XXXXXXXXXXXX//

                if (node.isRoot(ModelHistoryTreeEdgeType.ELEMENT)) {
                    JSONObject rootElementNode = new JSONObject();
                    rootElementNode.put("id", node.idElement());
                    rootElementNode.put("operationId", node.id());
                    elementRootNodesList.put(rootElementNode);
                }

                JSONArray nodesListElementChilds = new JSONArray();

                for (var child : node.getChildren(ModelHistoryTreeEdgeType.ELEMENT)) {
                    nodesListElementChilds.put(child.id());
                }

                var parentElementNode = node.getParent(ModelHistoryTreeEdgeType.ELEMENT);
                if (parentElementNode != null) {
                    nodeObject.put("parentId", parentElementNode.id());
                }

                if (nodesListElementChilds.length() > 0) {
                    nodeObject.put("elementChildren", nodesListElementChilds);
                }

                //XXXXXXXXXXXX Property CHILD NODES XXXXXXXXXXXX//

                if (node.isRoot(ModelHistoryTreeEdgeType.PROPERTY) && node.idProperty() != null) {
                    JSONObject rootPropertyNode = new JSONObject();
                    rootPropertyNode.put("id", node.idProperty());
                    rootPropertyNode.put("operationId", node.id());
                    propertyRootNodesList.put(rootPropertyNode);
                }

                JSONArray nodesListPropertyChilds = new JSONArray();

                for (var child : node.getChildren(ModelHistoryTreeEdgeType.PROPERTY)) {
                    nodesListPropertyChilds.put(child.id());
                }

                var parentPropertyNode = node.getParent(ModelHistoryTreeEdgeType.PROPERTY);
                if (parentPropertyNode != null) {
                    nodeObject.put("parentId", parentPropertyNode.id());
                }

                if (nodesListPropertyChilds.length() > 0) {
                    nodeObject.put("propertyChildren", nodesListPropertyChilds);
                }

                nodesList.put(nodeObject);
            }

       /* for (OperationGraphEdge edge : edges) {
            JSONObject edgeObject = new JSONObject();
            edgeObject.put("source", edge.source.id());
            edgeObject.put("target", edge.dest.id());
            edgesList.put(edgeObject);
        }*/

            JSONObject mainObject = new JSONObject();
            mainObject.put("nodes", nodesList);
            mainObject.put("wsStateNodes", stateNodesList);
            mainObject.put("elementRootNodes", elementRootNodesList);
            mainObject.put("propertyRootNodes", propertyRootNodesList);
            mainObject.put("skipEdges", skipEdgesList);
            mainObject.put("elementSkipEdges", skipEdgesElementList);
            mainObject.put("propertySkipEdges", skipEdgesPropertyList);

            //mainObject.put("links", edgesList);
            return mainObject.toString(4);
            //Write JSON file
        /*try (FileWriter file = new FileWriter(fileName)) {
            file.write(mainObject.toString(4));
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "NO JSON!";
    }

   public List<Operation> operations (Workspace workspace){
        var workspaceLeafNode = workspace.state.getLeafNode(false);
        var allNodes = getNodesFollowSkips(null, workspaceLeafNode, ModelHistoryTreeEdgeType.OPERATION, workspace);
        return allNodes.stream().map(x -> x.data).collect(Collectors.toList());
   }

   public List<Operation> operations (Id elementId, Workspace workspace){
       var elementLeafNode = workspace.state.getLeafNodeElement(elementId.value(),false);
       if(elementLeafNode == null){
        return new LinkedList<Operation>();
       }
       var allNodes = getNodesFollowSkips(null, elementLeafNode, ModelHistoryTreeEdgeType.ELEMENT, workspace);
       return allNodes.stream().map(x -> x.data).collect(Collectors.toList());
   }

    public Element load (Id elementId, Workspace workspace){
        var state = workspace.state;
        return state.load(elementId);
    }

    public Property load (Id elementId, String propertyName, Workspace workspace){
        return workspace.state.load(elementId, propertyName);
    }

    public Set<Element> debugAllElements (Workspace workspace){
        var state = workspace.state;
        //Warning: this call could lead to a long processing time
        return state.elementStates.values().stream().map(x -> x.load()).collect(Collectors.toSet());
    }
}

