package at.jku.isse.designspace.core.trees.modelhistory.caching;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import at.jku.isse.designspace.core.events.ElementCreate;
import at.jku.isse.designspace.core.events.ElementDelete;
import at.jku.isse.designspace.core.events.ElementUpdate;
import at.jku.isse.designspace.core.model.Cardinality;
import at.jku.isse.designspace.core.model.Element;
import at.jku.isse.designspace.core.model.Factory;
import at.jku.isse.designspace.core.model.Id;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.Property;
import at.jku.isse.designspace.core.model.PropertyType;
import at.jku.isse.designspace.core.model.ReservedNames;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.trees.modelhistory.ModelHistoryTree;
import at.jku.isse.designspace.core.trees.modelhistory.ModelHistoryTreeEdgeType;
import at.jku.isse.designspace.core.trees.modelhistory.ModelHistoryTreeNode;

public class ElementState {
    private interface OperationExecutor {
        void execute(ModelHistoryTreeNode node, ElementState state);
    }

    private static final Map<Class, OperationExecutor> operationExecutors = new HashMap<>();

    static {
        operationExecutors.put(ElementCreate.class, new OperationExecutor() {
            public void execute(ModelHistoryTreeNode node, ElementState state) {
                if (state.element != null) {
                    throw new IllegalStateException("Element already created!");
                }
//                if (state.element != null && node.data.isInverted()) {
//                    operationExecutors.get(ElementDelete.class).execute(node, state);
//                    return;
//                }
                Element element = Factory.reconstructElement(state.workspace, (ElementCreate) node.data);
                state.load(element);
            }
        });
        operationExecutors.put(ElementDelete.class, new OperationExecutor() {
            public void execute(ModelHistoryTreeNode node, ElementState state) {
                if (state.element == null) {
                    return;
                }
//                if (node.data.isInverted()) {
//                    //ToDo
//                    //operationExecutors.get(ElementDeleted.class).execute(node,state);
//                    return;
//                }
                //updated opposable properties etc...
                //state.element.isDeleted = true;
                //state.element.delete();

                //var element = state.element;
                //Unset all opposable references
                var element = state.element;
                Property property = null;
                for (PropertyType propertyType : element.getInstanceType().getPropertyTypes(false)) {
                    property = element.getProperty(propertyType.name());
                    if (property != null) {
                        // delete all properties of an element if element is deleted
                        if (propertyType.opposedPropertyType() != null) {
                            switch (propertyType.cardinality()) {
                                case SET:
                                    Set cacheSet = element.getPropertyAsSet(propertyType.name()).get();
                                    element.getPropertyAsSet(propertyType.name()).clear();
                                    element.getPropertyAsSet(propertyType.name()).set(cacheSet);           //keep elements in deleted elements to understand what it was
                                    break;
                                case LIST:
                                    List cacheList = element.getPropertyAsList(propertyType.name()).get();
                                    element.getPropertyAsList(propertyType.name()).clear();
                                    element.getPropertyAsList(propertyType.name()).set(cacheList);         //keep elements in deleted elements to understand what it was
                                    break;
                                case MAP:
                                    Map cacheMap = element.getPropertyAsMap(propertyType.name()).get();
                                    element.getPropertyAsMap(propertyType.name()).clear();
                                    element.getPropertyAsMap(propertyType.name()).set(cacheMap);           //keep elements in deleted elements to understand what it was
                                    break;
                                case SINGLE:
                                    Object cacheSingle = element.getPropertyAsSingle(propertyType.name()).get();
                                    element.getPropertyAsSingle(propertyType.name()).set(null);
                                    element.getPropertyAsSingle(propertyType.name()).setValue(cacheSingle);    //keep elements in deleted elements to understand what it was
                            }
                        }
                    }
                }
                element.getInstanceType().getPropertyAsSet(ReservedNames.INSTANCES).remove(this);
                if (element.getPropertyAsSingle(ReservedNames.CONTAINED_FOLDER) != null)
                    element.getPropertyAsSingle(ReservedNames.CONTAINED_FOLDER).set(null);

                element.isDeleted = true;
            }
        });
    }

    public Workspace workspace;
    public Id elementId;
    public Element element = null;
    public ModelHistoryTreeNode creationNode = null;
    public ModelHistoryTreeNode last = null;
    public ModelHistoryTreeNode lastInTransaction = null;
    public Map<String, PropertyState> propertyStates = new HashMap<>();
    boolean loaded = false;

    public ElementState(Id elementId, Workspace workspace) {
        this.workspace = workspace;
        this.elementId = elementId;
    }

    public ModelHistoryTreeNode getLeafNode(boolean includeOperationsInTransaction) {
        if (lastInTransaction != null && includeOperationsInTransaction) {
            return lastInTransaction;
        }
        return last;
    }

    public ModelHistoryTreeNode getCurrentPropertyNode(String name, boolean includeOperationsInTransaction) {
        if (!propertyStates.containsKey(name)) {
            return null;
        }
        return propertyStates.get(name).getLeafNode(includeOperationsInTransaction);
    }

    public void execute(ModelHistoryTreeNode modelHistoryTreeNode) {
        if (modelHistoryTreeNode.data instanceof ElementUpdate) {
            var elementUpdated = (ElementUpdate) modelHistoryTreeNode.data;
            var propertyState = getPropertyState(elementUpdated.name(), true);
            if (propertyState != null) {
                propertyState.execute(modelHistoryTreeNode);
            }
        } else if (loaded) {
            var handler = operationExecutors.get(modelHistoryTreeNode.data.getClass());
            if (handler == null) {
                return;
            }
            handler.execute(modelHistoryTreeNode, this);
        }
    }

    public void update(ModelHistoryTreeNode modelHistoryTreeNode) {
        if (creationNode == null && modelHistoryTreeNode.data instanceof ElementCreate) {
            creationNode = modelHistoryTreeNode;
        }

        if (modelHistoryTreeNode.data.isConcluded() || modelHistoryTreeNode.data.isAutoCreated()) {
            last = modelHistoryTreeNode;
        } else {
            lastInTransaction = modelHistoryTreeNode;
        }

        if (modelHistoryTreeNode.data instanceof ElementUpdate) {
            var elementUpdated = (ElementUpdate) modelHistoryTreeNode.data;
            var propertyState = getPropertyState(elementUpdated.name(), true);
            if (propertyState != null) {
                propertyState.update(modelHistoryTreeNode);
            }
        }
    }

    public Element load () {
        if (element != null) {
            return element;
        }

        switch((int)elementId.value()) {
            // case 1:
            // return workspace.ELEMENT;
            // case 2:
            // return workspace.META_ELEMENT_TYPE;
            //case 3:
            // return workspace.META_PROPERTY_TYPE;
            // case 4:
            // break;
            // return workspace.ELEMENT_TYPE;
            case 5:
                element = Workspace.STRING;
                break;
            case 6:
                element =  Workspace.INTEGER;
                break;
            case 7:
                element =  Workspace.REAL;
                break;
            case 8:
                element =  Workspace.BOOLEAN;
                break;
            case 9:
                element =  Workspace.DATE;
                break;
            case 10:
                element =  Workspace.GENERIC_SINGLE_PROPERTY_TYPE;
                break;
            case 11:
                element =  Workspace.GENERIC_SET_PROPERTY_TYPE;
                break;
            case 12:
                element =  Workspace.GENERIC_LIST_PROPERTY_TYPE;
                break;
            case 13:
                element =  Workspace.GENERIC_MAP_PROPERTY_TYPE;
                break;
            default:
                break;
        }

        loaded = true; //loaded flag must be set before executions, otherwise no operation will be executed

        if(element != null){
            return element;
        }

        var elementNodes = ModelHistoryTree.getInstance().getNodesFollowSkips(null, getLeafNode(true), ModelHistoryTreeEdgeType.ELEMENT, workspace);
        elementNodes.forEach(x -> execute(x));

        if(element != null){
            return element;
        }

        throw new IllegalStateException("Element "+elementId+" could not be loaded in workspace "+workspace.name()+"!");
    }

    public Property load (String propertyName){
        var propertyState = getPropertyState(propertyName, false);
        if (propertyState != null) {
            Property loadedProperty = propertyState.load();
            if (loadedProperty != null) {
                return loadedProperty;
            }
        }

        var propertyStateInstanceOf = getPropertyState(ReservedNames.INSTANCE_OF, false);
        if (propertyStateInstanceOf == null) {
            return null;
        }
        Property instanceTypeProperty = load(ReservedNames.INSTANCE_OF);
        if (instanceTypeProperty == null) return null;
        InstanceType instanceType = (InstanceType) instanceTypeProperty.get();
        if (instanceType == null) return null;

        PropertyType propertyType = instanceType.getPropertyType(propertyName);
        if (propertyType == null) return null;

        Cardinality cardinality = propertyType.cardinality();
        switch (cardinality) {
            case SINGLE:
                return element.createSingleProperty(propertyName, null, propertyType);
            case SET:
                return element.createSetProperty(propertyName, propertyType);
            case LIST:
                return element.createListProperty(propertyName, propertyType);
            case MAP:
                return element.createMapProperty(propertyName, propertyType);
            default:
                throw new IllegalArgumentException("property type was not declared");
        }


    }

    public void load (Property property){
        var propertyState = getPropertyState(property.name, false);
        if (propertyState == null) {
            throw new IllegalStateException("No PropertyState found!");
        }
        propertyState.load(property);
    }

    public void load (Element element){
        if (this.element != null) {
            throw new IllegalStateException("Element already loaded!");
        }
        this.element = element;
        loaded = true;
        element.state = this;
    }

    public PropertyState getPropertyState (String propertyName,boolean createIfNotFound){
        var propertyState = propertyStates.get(propertyName);
        if (propertyState == null) {
            if (createIfNotFound) {
                propertyState = new PropertyState(this, propertyName);
                propertyStates.put(propertyName, propertyState);
                if (workspace.parent() != null) {
                    var parentState = workspace.parent().state.getElementState(elementId, false);
                    var parentpropertyState = parentState == null ? null : parentState.getPropertyState(propertyName, false);
                    if (parentpropertyState != null) {
                        propertyState.last = parentpropertyState.last;
                    }
                }
            } else {
                return null;
            }
        }
        return propertyState;
    }


    public Collection<String> propertyNames () {
        return propertyStates.keySet();
    }

    public Collection<Property> properties () {
        return propertyStates.values().stream().map(x -> x.load()).collect(Collectors.toList());
    }

    public boolean hasProperty (String propertyName){
        return propertyStates.containsKey(propertyName);
    }

    public boolean isPropertySet (String propertyName){
        if(hasProperty(propertyName)){
            return load(propertyName).getValue() != null;
        }
        return false;
    }
}
