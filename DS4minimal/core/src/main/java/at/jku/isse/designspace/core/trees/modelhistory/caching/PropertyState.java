package at.jku.isse.designspace.core.trees.modelhistory.caching;

import java.util.HashMap;
import java.util.Map;

import at.jku.isse.designspace.core.events.ElementUpdate;
import at.jku.isse.designspace.core.events.PropertyCreate;
import at.jku.isse.designspace.core.events.PropertyDelete;
import at.jku.isse.designspace.core.events.PropertyUpdateAdd;
import at.jku.isse.designspace.core.events.PropertyUpdateRemove;
import at.jku.isse.designspace.core.events.PropertyUpdateSet;
import at.jku.isse.designspace.core.model.Cardinality;
import at.jku.isse.designspace.core.model.Element;
import at.jku.isse.designspace.core.model.Id;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.ListProperty;
import at.jku.isse.designspace.core.model.MapProperty;
import at.jku.isse.designspace.core.model.Property;
import at.jku.isse.designspace.core.model.PropertyType;
import at.jku.isse.designspace.core.model.ReservedNames;
import at.jku.isse.designspace.core.model.SetProperty;
import at.jku.isse.designspace.core.model.SingleProperty;
import at.jku.isse.designspace.core.trees.modelhistory.ModelHistoryTree;
import at.jku.isse.designspace.core.trees.modelhistory.ModelHistoryTreeEdgeType;
import at.jku.isse.designspace.core.trees.modelhistory.ModelHistoryTreeNode;

public class PropertyState {
    private interface OperationExecutor {
        void execute(ModelHistoryTreeNode node, PropertyState state);
    }

    private static final Map<Class, OperationExecutor> operationExecutors = new HashMap<>();

    static {
        operationExecutors.put(PropertyCreate.class, new OperationExecutor() {
            public void execute(ModelHistoryTreeNode node, PropertyState state) {
//                if (state.property != null && node.data.isInverted()) {
//                    operationExecutors.get(PropertyDelete.class).execute(node, state);
//                    return;
//                }

                if(state.property != null){
                    return;
                }

                Element element = state.elementState.element;
                PropertyCreate operation = (PropertyCreate) node.data;
                String propertyName = operation.name();
                Property property = null;

                if ((operation).propertyTypeId() == null) {
                    //happens with properties of meta types, which, fortunately, are all single properties
                    property = new SingleProperty(element, (PropertyCreate) operation);
                } else {
                    PropertyType propertyType = (PropertyType) element.workspace.findElement((Id) ((PropertyCreate) operation).propertyTypeId());
                    switch (propertyType.cardinality()) {
                        case SINGLE:
                            property = new SingleProperty(element, (PropertyCreate) operation);
                            break;
                        case SET:
                            property = new SetProperty(element, (PropertyCreate) operation);
                            break;
                        case LIST:
                            property = new ListProperty(element, (PropertyCreate) operation);
                            break;
                        case MAP:
                            property = new MapProperty(element, (PropertyCreate) operation);
                            break;
                        default:
                            throw new IllegalArgumentException("Unrecognized property kind " + operation);
                    }
                }

                state.property = property;

                if(state.property != null){
                    return;
                }

                Property instanceTypeProperty = state.elementState.load(ReservedNames.INSTANCE_OF);
                if (instanceTypeProperty == null) return;

                InstanceType instanceType = (InstanceType)instanceTypeProperty.get();
                if (instanceType == null) return;

                PropertyType propertyType = instanceType.getPropertyType(propertyName);
                if (propertyType == null) return;

                Cardinality cardinality = propertyType.cardinality();
                switch (cardinality) {
                    case SINGLE:
                        property = new SingleProperty(element, propertyName, null, propertyType, true);
                        break;
                    case SET:
                        property = new SetProperty(element, propertyName, propertyType, true);
                        break;
                    case LIST:
                        property = new ListProperty(element, propertyName, propertyType, true);
                        break;
                    case MAP:
                        property = new MapProperty(element, propertyName, propertyType, true);
                        break;
                    default:
                        throw new IllegalArgumentException("property type was not declared");
                }

                state.property = property;
            }
        });

        operationExecutors.put(PropertyUpdateAdd.class, new OperationExecutor() {
            public void execute(ModelHistoryTreeNode node, PropertyState state) {
                if (state.property == null) {
                    return;
                }
                state.property.reconstruct((ElementUpdate) node.data);
            }
        });
        operationExecutors.put(PropertyUpdateSet.class, new OperationExecutor() {
            public void execute(ModelHistoryTreeNode node, PropertyState state) {
                if (state.property == null) {
                    return;
                }
                state.property.reconstruct((ElementUpdate) node.data);
            }
        });
        operationExecutors.put(PropertyUpdateRemove.class, new OperationExecutor() {
            public void execute(ModelHistoryTreeNode node, PropertyState state) {
                if (state.property == null) {
                    return;
                }
                state.property.reconstruct((ElementUpdate) node.data);
            }
        });
        operationExecutors.put(PropertyDelete.class, new OperationExecutor() {
            public void execute(ModelHistoryTreeNode node, PropertyState state) {
                if (state.property == null) {
                    return;
                }
                state.property.isDeleted = true;
            }
        });
    }

    public boolean metaProperty = false;

    ElementState elementState;
    String propertyName = "";
    Property property = null;
    public ModelHistoryTreeNode last = null;
    public ModelHistoryTreeNode creationNode = null;
    public ModelHistoryTreeNode lastInTransaction = null;
    boolean loaded = false;

    public PropertyState(ElementState elementState, String propertyName) {
        this.elementState = elementState;
        this.propertyName = propertyName;
    }

    public ModelHistoryTreeNode getLeafNode (boolean includeOperationsInTransaction) {
        if (lastInTransaction != null && includeOperationsInTransaction) {
            return lastInTransaction;
        }
        return last;
    }

    public void update(ModelHistoryTreeNode modelHistoryTreeNode) {
        if (creationNode == null && modelHistoryTreeNode.data instanceof PropertyCreate) {
            creationNode = modelHistoryTreeNode;
        }

        if (modelHistoryTreeNode.data.isConcluded() || modelHistoryTreeNode.data.isAutoCreated()) {
            last = modelHistoryTreeNode;
        } else {
            lastInTransaction = modelHistoryTreeNode;
        }
    }

    public void execute(ModelHistoryTreeNode modelHistoryTreeNode) {
        if(!loaded){
            return;
        }

        if(property != null && property.executedOperations.containsKey(modelHistoryTreeNode.id())){
            property.executedOperations.remove(modelHistoryTreeNode.id());
            return;
        }

        var executor = operationExecutors.get(modelHistoryTreeNode.data.getClass());
        if (executor == null) {
            return;
        }
        executor.execute(modelHistoryTreeNode, this);
    }

    public void load(Property property) {
        if(this.property != null){
            throw new IllegalStateException("Property already loaded!");
        }
        this.property = property;
        loaded = true;
    }

    public Property load() {
        if (property != null) {
            return property;
        }

        if(last == null && lastInTransaction == null){
            return null;
        }

        loaded = true; //loaded flag must be set before executions, otherwise no operation will be executed

        //TODO create caching options, how to clone or copy etc...
        var elementNodes = ModelHistoryTree.getInstance().getNodesFollowSkips(null, getLeafNode(true), ModelHistoryTreeEdgeType.PROPERTY, elementState.workspace);
        elementNodes.forEach(x -> execute(x));

        if(property == null){
            throw new IllegalStateException("Property not created in load!");
        }

        return property;
    }

}
