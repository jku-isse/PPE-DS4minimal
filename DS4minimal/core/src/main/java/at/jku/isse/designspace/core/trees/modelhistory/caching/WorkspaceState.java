package at.jku.isse.designspace.core.trees.modelhistory.caching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.jku.isse.designspace.core.model.Element;
import at.jku.isse.designspace.core.model.Id;
import at.jku.isse.designspace.core.model.Property;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.trees.modelhistory.ModelHistoryTreeNode;

public class WorkspaceState {

    Workspace workspace;

    public ModelHistoryTreeNode last = null;
    public ModelHistoryTreeNode lastInTransaction = null;
    public ModelHistoryTreeNode branchingPoint = null;
    public List<ModelHistoryTreeNode> operationsInTransaction = new ArrayList<>();
    public Map<Id, ElementState> elementStates = new HashMap<>();

    public WorkspaceState(Workspace workspace) {
        this.workspace = workspace;
    }

    public void setParent(Workspace parent){
        var parentState = parent.state;
        for (var parentElementState : parentState.elementStates.values()) {
            if(!parentElementState.creationNode.data.isConcluded()){
                continue;
            }
            var newElementState = new ElementState(parentElementState.elementId, workspace);
            elementStates.put(parentElementState.elementId, newElementState);
            newElementState.last = parentElementState.last;
            newElementState.creationNode = parentElementState.creationNode;
            //newElementState.lastInTransaction = parentElementState.lastInTransaction;
            for (var parentPropertyState : parentElementState.propertyStates.values()) {
                var newPropertyState = new PropertyState(newElementState, parentPropertyState.propertyName);
                newElementState.propertyStates.put(newPropertyState.propertyName, newPropertyState);
                newPropertyState.last = parentPropertyState.last;
                newPropertyState.creationNode = parentPropertyState.creationNode;
                //newPropertyState.lastInTransaction = parentPropertyState.lastInTransaction;
            }
        }
        workspace.state.last = parentState.last;
        //workspace.state.lastInTransaction = parentState.lastInTransaction;
        workspace.state.branchingPoint = parentState.last;
    }


    public ModelHistoryTreeNode getLeafNode (boolean includeOperationsInTransaction) {
        if (lastInTransaction != null && includeOperationsInTransaction) {
            return lastInTransaction;
        }
        return last;
    }

    public ModelHistoryTreeNode getLeafNodeElement (long elementId, boolean includeOperationsInTransaction) {
        var elementState = getElementState(Id.of(elementId),  false);
        if (elementState == null) {
            return null;
        }
        return elementState.getLeafNode(includeOperationsInTransaction);
    }

    public ModelHistoryTreeNode getCurrentElementNode (long elementId, boolean includeOperationsInTransaction){
        if (!elementStates.containsKey(Id.of(elementId))) {
            return null;
        }
        return elementStates.get(Id.of(elementId)).getLeafNode(includeOperationsInTransaction);
    }

    public ModelHistoryTreeNode getCurrentPropertyNode (long elementId, String name, boolean includeOperationsInTransaction){
        if (!elementStates.containsKey(Id.of(elementId))) {
            return null;
        }
        ElementState elementState = getElementState(Id.of(elementId), false);
        return elementState.getCurrentPropertyNode(name, includeOperationsInTransaction);
    }

    public Element load (Id elementId)
    {
        var elementState = getElementState(elementId, false);
        if (elementState == null) {
            return null;
        }

        Element element = elementState.load();

        if(elementState.creationNode == null){
            elementState.creationNode = workspace.parent().state.getElementState(elementId,false).creationNode;
        }

        return element;
    }

    public Property load (Id elementId, String propertyName)
    {

        var elementState = getElementState(elementId, false);
        if (elementState == null) {
            return null;
        }
        return elementState.load(propertyName);
    }

    public void execute (ModelHistoryTreeNode modelHistoryTreeNode){
        var elementState = getElementState(modelHistoryTreeNode.data.elementId(), true);
        if (elementState == null) {
            return;
        }
        elementState.execute(modelHistoryTreeNode);
    }

    public void execute (List <ModelHistoryTreeNode> modelHistoryTreeNodes) {
        modelHistoryTreeNodes.forEach(x -> execute(x));
    }

    public void update (ModelHistoryTreeNode modelHistoryTreeNode){


        if(modelHistoryTreeNode.data.isConcluded() || modelHistoryTreeNode.data.isAutoCreated()){
            last = modelHistoryTreeNode;
        } else {
            lastInTransaction = modelHistoryTreeNode;
        }

        var elementState = getElementState(modelHistoryTreeNode.data.elementId(), true);
        if (elementState == null) {
            return;
        }
        elementState.update(modelHistoryTreeNode);
    }

    public void updateAndExecute (ModelHistoryTreeNode modelHistoryTreeNode){
        execute(modelHistoryTreeNode);
        update(modelHistoryTreeNode);
    }

    public void load (Element element){
        var elementState = getElementState(element.id(),  false);
        if (elementState == null) {
            throw new IllegalStateException("No ElementState found!");
        }
        elementState.load(element);
    }

    public void load (Property property){
        var elementState = getElementState(property.element.id(), false);
        if (elementState == null) {
            throw new IllegalStateException("ElementState does not exist!");
        }
        elementState.load(property);
    }

    public ElementState getElementState (Id idElement, boolean createIfNotFound){
        var elementState = elementStates.get(idElement);
        if (elementState == null) {
            if (createIfNotFound) {
                elementState = new ElementState(idElement, workspace);
                elementStates.put(idElement, elementState);
                if(workspace.parent() != null){
                    var parentState = workspace.parent().state.getElementState(idElement,false);
                    if(parentState != null) {
                        elementState.last = parentState.last;
                        elementState.creationNode = parentState.creationNode;
                    }
                }
            }
            else {
                return null;
            }
        }
        return elementState;
    }

    public Collection<String> propertyNames (Id elementId){
        var elementState = getElementState(elementId, false);
        if (elementState == null) {
            return null;
        }
        return elementState.propertyNames();
    }

    public Collection<Property> properties (Id elementId){
        var elementState = getElementState(elementId, false);
        if (elementState == null) {
            return null;
        }
        return elementState.properties();
    }

    public boolean hasProperty (Id elementId, String propertyName){
        var elementState = getElementState(elementId, false);
        if (elementState == null) {
            return false;
        }
        return elementState.hasProperty(propertyName);
    }

}
