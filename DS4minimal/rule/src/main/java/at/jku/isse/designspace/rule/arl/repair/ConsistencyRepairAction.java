package at.jku.isse.designspace.rule.arl.repair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.PropertyType;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.service.WorkspaceService;
import at.jku.isse.designspace.rule.arl.evaluator.EvaluationNode;
import at.jku.isse.designspace.rule.arl.repair.changepropagation.Change;
import at.jku.isse.designspace.rule.checker.ConsistencyUtils;
import at.jku.isse.designspace.rule.model.ReservedNames;
import at.jku.isse.designspace.rule.model.Rule;
import at.jku.isse.designspace.rule.service.RuleService;

public class ConsistencyRepairAction extends AbstractRepairAction<Instance> {

    private Set<Change> bufferedRemovalChanges;
    private Set<Rule> rulesInContext;
    public ConsistencyRepairAction(RepairNode parent, String property, Instance contextElement, RepairSingleValueOption repairValueOption, EvaluationNode evalNode) {
        super(parent, property,contextElement, repairValueOption, evalNode);
        bufferedRemovalChanges = new HashSet<>();
        rulesInContext = new HashSet<>();
    }


    @Override
    public String getValueString(Object Value) {
        return oldValue.toString();
    }

    @Override
    public RepairNode highlightOwnedRepairs(String ownerId){
        Instance element = null;
        if(contextElement instanceof Instance)
            element = getCurrentWorkspaceInstance(contextElement);
        if(element == null) return null;
        this.owners = new ArrayList<>(element.getPropertyAsSet(ReservedNames.OWNERSHIP_PROPERTY).get());
        if(element.getPropertyAsSet(ReservedNames.OWNERSHIP_PROPERTY).contains(ownerId))
            this.highlight = true;
        else
            this.highlight = false;
        return this;
    }

    @Override
    protected void executeAddAction(Object element) {
        if(this.getValue() == null) return;
        if (property == null){
            if(this.getValue() instanceof Instance){
                Instance instance = getCurrentWorkspaceInstance((Instance) getValue());
                Workspace workspace = instance.workspace;
                WorkspaceService.createInstance(workspace,instance.name(),instance.getInstanceType());
                oldValue = instance;
                ConsistencyUtils.printRepair(this,oldValue);
            }
        } else  {
            Instance instance = getCurrentWorkspaceInstance((Instance) element);
            Object value = getValue();
            executeCollectionAdd(instance,property,value);
            oldValue = value;
            ConsistencyUtils.printRepair(this,"NA");
        }
    }

    @Override
    protected void executeDeleteAction(Object element) {
        if(this.getValue() == null) return;
        if (property == null) {
            if(this.getValue() instanceof Instance){
                Instance instance = getCurrentWorkspaceInstance((Instance) getValue());
                oldValue = instance;
                checkForOpposable(instance);
                rulesInContext = new HashSet<>(instance.getPropertyAsSet(ReservedNames.RULE_EVALUATIONS_IN_CONTEXT).get());
                instance.delete();
                ConsistencyUtils.printRepair(this,oldValue);
            }
        } else  {
            Instance instance = getCurrentWorkspaceInstance((Instance) element);
            Object value = getValue();
            oldValue = value;
            executeCollectionRemove(instance,value);
            ConsistencyUtils.printRepair(this,oldValue);
        }

    }




    @Override
    protected void executeModifyAction(Object element) {
        if ((element == null) || (property == null)) return;
        if (element instanceof Instance) {
            Instance instance = getCurrentWorkspaceInstance((Instance) element);
            oldValue = instance.getProperty(property).getValue();
            Object value = getValue();
            if(value instanceof Instance)
                value = getCurrentWorkspaceInstance((Instance) value);
            if (value != null) {
                instance.getPropertyAsSingle(property).set(value);
                ConsistencyUtils.printRepair(this,oldValue);
            }
        }
    }

    @Override
    protected void undoAddAction(Object element) {
        if(this.oldValue == null) return;
        if (property == null) {
            if(this.getValue() instanceof Instance){
                Instance instance =getCurrentWorkspaceInstance((Instance) oldValue);
                oldValue = instance;
                instance.delete();
                ConsistencyUtils.printUndo(oldValue,"deleted");
            }
        } else  {
            Instance instance = getCurrentWorkspaceInstance((Instance) element);
            Object value = oldValue;
            executeCollectionRemove(instance,value);
            oldValue = value;
            ConsistencyUtils.printUndo(oldValue,value);

        }
    }

    @Override
    protected void undoDeleteAction(Object element) {
        if(this.oldValue == null) return;
        if (property == null) {
            if(this.getValue() instanceof Instance) {
                Instance instance = getCurrentWorkspaceInstance((Instance) oldValue);
                instance.restore();
                for(Change c : bufferedRemovalChanges) {
                    Instance i = getCurrentWorkspaceInstance((Instance) c.getElement());
                    executeCollectionAdd(i, c.getProperty(),instance);
                }
                instance.getPropertyAsSet(ReservedNames.RULE_EVALUATIONS_IN_CONTEXT).addAll(rulesInContext);
                rulesInContext.clear();
                ConsistencyUtils.printUndo(oldValue,"restored");
            }
        } else  {
            Instance instance = getCurrentWorkspaceInstance((Instance) element);
            Object value = oldValue;
            executeCollectionAdd(instance,property,value);
            oldValue = value;
            ConsistencyUtils.printUndo(oldValue,value);
        }

    }


    @Override
    protected void undoModifyAction(Object element) {
        if ((element == null) || (property == null) || (oldValue == null)) return;
        if (element instanceof Instance) {
            Instance instance = getCurrentWorkspaceInstance((Instance) element);
            Object value = oldValue;
            if (value != null) {
                oldValue = instance.getProperty(property).getValue();
                instance.getPropertyAsSingle(property).set(value);
                ConsistencyUtils.printUndo(oldValue,value);
            }
        }
    }

    private void executeCollectionRemove(Instance instance, Object value) {
        PropertyType pt = instance.getProperty(property).propertyType();
        switch (pt.cardinality()) {
            case SET:
                instance.getPropertyAsSet(property).remove(value);
                break;
            case LIST:
                instance.getPropertyAsList(property).remove(value);
                break;
            case MAP: // TODO
                break;
            case SINGLE:
                instance.getPropertyAsSingle(property).set(null);
                break;
        }
    }

    private void executeCollectionAdd(Instance instance, String getProperty, Object value) {
        PropertyType pt = instance.getProperty(getProperty).propertyType();
        switch (pt.cardinality()) {
            case SET:
                instance.getPropertyAsSet(getProperty).add(value);
                break;
            case LIST:
                instance.getPropertyAsList(getProperty).add(value);
                break;
            case MAP:  // TODO
                break;
            case SINGLE:
                instance.getPropertyAsSingle(getProperty).set(value);
                break;
        }
    }


    private void checkForOpposable(Instance instance){
        for (PropertyType propertyType : instance.getInstanceType().getPropertyTypes(false)) {
            if (propertyType.opposedPropertyType()!=null && instance.getProperty(propertyType.name()) != null) {
                switch (propertyType.cardinality()) {
                    case SET:
                        Set cacheSet = instance.getPropertyAsSet(propertyType.name()).get();
                        for(Object o: cacheSet) {
                            if(o instanceof Instance)
                                setBufferedChanges(instance, (Instance) o);
                        }
                        break;
                    case LIST:
                        List cacheList = instance.getPropertyAsList(propertyType.name()).get();
                        for(Object o: cacheList) {
                            if(o instanceof Instance)
                                setBufferedChanges(instance, (Instance) o);
                        }
                        break;
                    case MAP: // TODO
                        break;
                    case SINGLE:
                        Object o = instance.getPropertyAsSingle(propertyType.name()).get();
                        if(o instanceof Instance)
                            setBufferedChanges(instance, (Instance) o);
                        break;
                }
            }

        }
    }

    private void setBufferedChanges(Instance instance, Instance opposable) {
        for (PropertyType propertyType : opposable.getInstanceType().getPropertyTypes(false)) {
            if (propertyType.opposedPropertyType()!=null && opposable.getProperty(propertyType.name()) != null) {
                switch (propertyType.cardinality()) {
                    case SET:
                        Set cacheSet = opposable.getPropertyAsSet(propertyType.name()).get();
                        for(Object o: cacheSet){
                            if(o.equals(instance)) {
                                Change c = new Change(opposable, propertyType.name(), Operator.MOD_EQ, instance, Change.ChangeType.NEGATIVE);
                                bufferedRemovalChanges.add(c);
                            }

                        }
                        break;
                    case LIST:
                        List cacheList = opposable.getPropertyAsList(propertyType.name()).get();
                        for(Object o: cacheList){
                            if(o.equals(instance)) {
                                Change c = new Change(opposable, propertyType.name(), Operator.MOD_EQ, instance, Change.ChangeType.NEGATIVE);
                                bufferedRemovalChanges.add(c);
                            }
                        }
                        break;
                    case MAP: // TODO
//                        Map cacheMap = opposable.getPropertyAsMap(getPropertyType.name()).get();
//                        for (Map.Entry<String,Object> entry : cacheMap.entrySet())
//                            if(entry.getValue().equals(instance))
//                                bufferedInstances.add(opposable);
                        break;
                    case SINGLE:
                        Object cacheSingle = opposable.getPropertyAsSingle(propertyType.name()).get();
                        if(cacheSingle==instance) {
                            Change c = new Change(opposable, propertyType.name(), Operator.MOD_EQ, instance, Change.ChangeType.NEGATIVE);
                            bufferedRemovalChanges.add(c);
                        }
                        break;
                }
            }
        }
    }



    @Override
    public Object getValue() {
        if(super.repairValueOption==null) return null;
        return super.repairValueOption.getValue();
    }

    @Override
    public void setValue(Object value) {
        super.repairValueOption.value = value;
    }



    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Change) { // if obj is a change, it must be cast to compare with the repair action
            Change c = (Change) obj;
            if(c.getOperator().equals(getOperator())) { // if the operator is different, then they are not equivalent
                if(getOperator().equals(Operator.MOD_EQ) || getOperator().equals(Operator.MOD_NEQ) || getOperator().equals(Operator.MOD_GT) ||
                        getOperator().equals(Operator.MOD_LT)) {
                    Instance i1 = getElement();
                    Instance i2 = (Instance) c.getElement();
                    return i1.equals(i2)
                            && getProperty().equals(c.getProperty()) && getValue().equals(c.getValue());
                }else { // if the operation is not MOD_EQ, then the repair is either a REMOVE or ADD
                       return getValue().equals(c.getValue());

                }
            }
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ConsistencyRepairAction other = (ConsistencyRepairAction) obj;
        if(property!=null && !property.equalsIgnoreCase(other.property))
            return false;
        if (repairValueOption == null) {
            if (other.repairValueOption != null) {
                return false;
            }
        } else if (!repairValueOption.equals(other.repairValueOption)) {
            return false;
        }else{
            //  adds and removes can only repeat if parent is a sequence
            if(getValue().equals(UnknownRepairValue.UNKNOWN) 
            		&& (repairValueOption.operator.equals(Operator.ADD) || repairValueOption.operator.equals(Operator.REMOVE))) {
                if(contextElement.equals(other.contextElement) && property.equals(other.property)
                        && this.parent != null && this.parent.equals(other.parent) 
                        //TODO: check for duplicate
                        //&& !this.parent.getNodeType().equals(Type.SEQUENCE)
                        )
                    return true;
                else
                    return false;
            }
        }

        if (contextElement == null) {
            return other.contextElement == null;
        } else
            return contextElement.equals(other.contextElement);
    }


    public void deleteRepairFromTree(){
        super.delete();
        if(this.parent != null) {
            this.parent.removeChild(this);
            this.setParent(null);
        }
        this.children.clear();
    }

    private Instance getCurrentWorkspaceInstance(Instance i){
        if(i.workspace.equals(RuleService.currentWorkspace))
            return i;
        else
            return RuleService.currentWorkspace.findElement(i.id());
    }
}
