package at.jku.isse.designspace.rule.arl.repair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.ListProperty;
import at.jku.isse.designspace.core.model.PropertyType;
import at.jku.isse.designspace.core.model.ReservedNames;
import at.jku.isse.designspace.core.model.SetProperty;
import at.jku.isse.designspace.rule.arl.exception.ChangeExecutionException;
import at.jku.isse.designspace.rule.model.ConsistencyRule;

public abstract class AbstractRepairNode implements RepairNode {

    protected RepairNode parent;
    protected List<RepairNode> children;
    protected RepairNode.Type type;
    protected Object cre;
    protected boolean executed = false;
    protected int concreteLimit;
  //added field
    protected double score;
    protected int rank;

    protected AbstractRepairNode(RepairNode parent, RepairNode.Type type) {
    	this.score=-1;
    	this.rank=-1;
        this.parent = parent;
        //children = new HashSet<>();
        children=new LinkedList<>();
        this.type = type;
        if (parent != null)
            this.parent.addChild(this);
        if(this.parent!=null)
            setInconsistency(this.parent.getInconsistency());
        this.concreteLimit = 50;
    }

    protected AbstractRepairNode() {
    }

    @Override
    public void setRank(int r)
    {
    	this.rank=r;
    }
    @Override
    public int getRank()
    {
    	return this.rank;
    }
    
    @Override
    public void setScore(double sc)
    {
    	this.score=sc;
    }
    @Override
    public double getScore()
    {
    	return this.score;
    }
    
    @Override
    public RepairNode getParent() {
        return parent;
    }
    @Override
    public RepairNode getRoot() {
        if (this.parent == null)
            return this;
        return parent.getRoot();
    }
    @Override
    public void setParent(RepairNode parent) {
        this.parent = parent;
    }

    @Override
    public List<RepairNode> getChildren() {
        return children;
    }

    @Override
    public void addChild(RepairNode child) {
        child.setParent(this);
        if (child != null && !isChildDuplicate(child)) {
            children.add(child);
        }
    }
    private boolean isChildDuplicate(RepairNode childNode){
        for (RepairNode child : children) {
            if(child.equals(childNode))
                return true;
        }
        return false;
    }
    @Override
    public void removeChild(RepairNode child) {
        children.remove(child);
        if(child instanceof ConsistencyRepairAction)
        {
        if (this.type.equals(Type.SEQUENCE)) {
        	// if this is a sequence, it implies that all children are needed to execute a complete repair, hence when one is removed, then also remove the rest.
        	for(RepairNode childNode : new HashSet<>(children)){ // copy needed to avoid concurrent modification exception!
        		childNode.delete();
        		childNode.setParent(null); // we set parent null already here, so that the child no longer calls us again for removal
            }
        	// now we also remove this node as its empty
        	if (parent != null) {        		
        		parent.removeChild(this);
        		this.setParent(null);
        	}
        }}
    }

    @Override
    public RepairNode.Type getNodeType() {
        return type;
    }

    @Override
    public void setNodeType(RepairNode.Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type.toString();
    }

    @Override
    public void execute() throws ChangeExecutionException {
        for (RepairNode child : getChildren()) {
            child.execute();
        }
        executed = true;
    }

    @Override
    public boolean isAbstract() {
        boolean isAbstract = false;
        Iterator<RepairNode> iterator = getChildren().iterator();
        while (!isAbstract && iterator.hasNext()) {
            isAbstract |= iterator.next()
                    .isAbstract();
        }
        return isAbstract;
    }

    @Override
    public void undo() throws ChangeExecutionException {
        for (RepairNode child : getChildren()) {
            if (child.isUndoable()) {
                child.undo();
            }
        }
    }

    @Override
    public boolean executed() {
        return executed;
    }

    @Override
    public int getHeight(){
        return getHeight(this);
    }


    @Override
    public int getHeight(RepairNode node) {
        if(node == null) return 0;
        Integer h=0;

        for(RepairNode n : node.getChildren()){
            h = Math.max(h, getHeight(n));
        }
        return h+1;

    }

    @Override
    public Set<RepairAction> getRepairActions() {
        Set<RepairAction> actions = new HashSet<>();
        if(this instanceof ConsistencyRepairAction){
            actions.add((ConsistencyRepairAction) this);
        }else{
            for (RepairNode node: children){
                actions.addAll(node.getRepairActions());
            }
        }

        return actions;
    }

    @Override
    public RepairNode highlightOwnedRepairs(String ownerId) {
        if (!this.children.isEmpty()){
            for (RepairNode rn: children){
                rn.highlightOwnedRepairs(ownerId);
            }
        }
        return this;
    }

    /**
     * Check if the limit of computable repairs was reached
     * @param limit
     * @return
     */
    @Override
    public boolean checkForLimit(int limit){
        if(this.type == Type.SEQUENCE) {
            int result = 1;
            for (RepairNode node : this.getChildren()) {
                if (node.getNodeType() == Type.ALTERNATIVE) {
                    int childrenSize2 = node.getChildren().size();
                    result = childrenSize2 * result;
                }else{
                    if(!node.checkForLimit(limit))
                        return false;
                }
                if (result > limit)
                    return false;
            }
        } else
        if(!this.children.isEmpty())
            for (RepairNode node : this.getChildren()) {
                if(!node.checkForLimit(limit))
                    return false;
            }
        return true;
    }
    @Override
    public Set<Repair> getRepairs() {
        Set<Repair> repairs = new HashSet<>();
        if (checkForLimit(5000)) {
            if (this.type == Type.ALTERNATIVE) {
                for (RepairNode node : this.getChildren()) {
                    if (node instanceof ConsistencyRepairAction) {
                        Repair r = new ConsistencyRepair(getInconsistency());
                        r.addRepairAction((RepairAction) node);
                        repairs.add(r);
                    } else {
                        repairs.addAll(node.getRepairs());
                    }
                }
            } else if (this.type == Type.SEQUENCE) {
                List<RepairAction> actions = new ArrayList<>();
                List<Set<Repair>> alternativeRepairs = new LinkedList<>();
                for (RepairNode node : this.getChildren()) {
                    if (node instanceof ConsistencyRepairAction) {
                        actions.add((RepairAction) node);
                    } else {
                        alternativeRepairs.add(node.getRepairs());

                    }
                }
                if (!actions.isEmpty()) {
                    Repair r = new ConsistencyRepair(getInconsistency());
                    r.addRepairActions(actions);
                    Set<Repair> alternativeRepair = new HashSet<>();
                    alternativeRepair.add(r);
                    alternativeRepairs.add(alternativeRepair);
                }
                generateRepairs(repairs, alternativeRepairs, new HashSet<RepairAction>());
            }
        }else{
            return generateRepairsSample();
        }
        removeConflicts(repairs);
        return repairs;
    }

    @Override
    public Set<Repair> getConcreteRepairs(Set<Object> objects, int limit){
        if(limit != 0)
            this.concreteLimit = limit;
        return getConcreteRepairs(objects, true,false);
    }

    @Override
    public Set<Repair> getConcreteRepairs(Set<Object> objects, boolean validate, boolean keepAbstract){
        Set<Repair> repairs = this.getRepairs();
        return getConcreteRepairs(repairs,objects,validate,keepAbstract);
    }

    @Override
    public Set<Repair> getConcreteRepairsFilteredByOwner(String ownerId, Set<Object> objects, boolean validate, boolean keepAbstract){
        Set<Repair> repairs = this.getRepairsFilteredByOwner(ownerId);
        return getConcreteRepairs(repairs,objects,validate,keepAbstract);
    }


    private Set<Repair> getConcreteRepairs(Set<Repair> repairs,Set<Object> objects,boolean validate, boolean keepAbstract){
        for(Repair r : new HashSet<>(repairs)) {
            if(r.isAbstract()) {
                repairs.addAll(convertAbstractToConcrete(r, objects, validate));
                if(!keepAbstract)
                    repairs.remove(r);
            }
        }
        return repairs;
    }


    protected void removeConflicts(Set<Repair> repairs) {
        Set<Repair> conflictRepairs = new HashSet<>(repairs);
        repairs.clear();
        for (Repair repair: conflictRepairs) {
            List<RepairAction> actions = new ArrayList<>(repair.getRepairActions());
            for (RepairAction action : actions) {
                for (RepairAction otherAction : actions) {
                    if(!action.equals(otherAction))
                        if (action.interferesWith(otherAction)) {
                            repair.removeRepairAction(action);
                        }
                }
            }
            if(!repair.getRepairActions().isEmpty())
                repairs.add(repair);
        }
    }

    private Set<Repair> generateRepairs(Set<Repair> repairs, List<Set<Repair>> alternativeRepairs,
                                        Set<RepairAction> actions) {
        if(repairs.size()>5000)
            return repairs;
        if (!alternativeRepairs.isEmpty()) {
            Set<Repair> alternativeRepair = alternativeRepairs.get(0);
            alternativeRepairs.remove(0);
            for (Repair repair : alternativeRepair) {
                Set<RepairAction > newActions = new HashSet<>(actions);
                newActions.addAll(repair.getRepairActions());
                generateRepairs(repairs, new ArrayList<>(alternativeRepairs), newActions);
            }
        } else {
            Repair repair = new ConsistencyRepair(getInconsistency());
            repair.addRepairActions(actions);
            repairs.add(repair);
        }
        return repairs;
    }

    /** Gets ramdom repairs from the possible wants until the limit is reached
     *
     * @return
     */
    @Override
    public Set<Repair> generateRepairsSample( ) {
        Set<Repair> repairs = new HashSet<>();
        if (this.type == Type.ALTERNATIVE) {
            Random random = new Random();
            List<RepairNode> children = new ArrayList<>(this.getChildren());
            for(int i = 0; i < 2 ; i++){
                int index = random.nextInt(this.getChildren().size());
                RepairNode node = children.get(index);
                if (node instanceof ConsistencyRepairAction) {
                    Repair r = new ConsistencyRepair(getInconsistency());
                    r.addRepairAction((RepairAction) node);
                    repairs.add(r);
                } else {
                    repairs.addAll(node.generateRepairsSample());
                }
//                if(repairs.size()>5000)
//                    return repairs;
            }
        } else if (this.type == Type.SEQUENCE) {
            List<RepairAction> actions = new ArrayList<>();
            List<Set<Repair>> alternativeRepairs = new LinkedList<>();
            for (RepairNode node : this.getChildren()) {
                if (node instanceof ConsistencyRepairAction) {
                    actions.add((RepairAction) node);
                } else {
                    alternativeRepairs.add(node.generateRepairsSample());

                }
            }

            if (!actions.isEmpty()) {
                Repair r = new ConsistencyRepair(getInconsistency());
                r.addRepairActions(actions);

                Set<Repair> alternativeRepair = new HashSet<>();
                alternativeRepair.add(r);
                alternativeRepairs.add(alternativeRepair);
            }

            generateRepairs(repairs, alternativeRepairs, new HashSet<RepairAction>());
        }
        return repairs;
    }

    @Override
    public Set<Repair> filterRepairs(Set<Repair> repairs) {
        Set<Repair> filteredRepairs = new HashSet<>();
        for (Iterator<Repair> iterator = repairs.iterator(); iterator.hasNext();) {
            Repair repair = iterator.next();
            for (Object obj1 : new ArrayList<>(repair.getRepairActions())) {
                RepairAction action = (RepairAction) obj1;
                for (Object obj2 : new ArrayList<>(repair.getRepairActions())) {
                    RepairAction otherAction = (RepairAction) obj2;
                    if(!action.equals(otherAction))
                        if (action.checkConflict(otherAction)) {
                            repair.removeRepairAction(action);
                        }


                }
            }
            if (repair.getRepairActions()
                    .isEmpty()) {
                iterator.remove();
            }else
                filteredRepairs.add(repair);
        }
        return filteredRepairs;
    }


    @Override
    public RepairNode flattenRepairTree() {
        if (!(this instanceof ConsistencyRepairAction)) {
           //for (RepairNode repairNodeChild : new ArrayList<>(this.getChildren())) {
        	for(int i=0;i<this.getChildren().size();i++) {
        		RepairNode repairNodeChild=this.getChildren().get(i);
                repairNodeChild.flattenRepairTree();
                if (repairNodeChild.getNodeType().equals(this.getNodeType())) {
                    for (RepairNode grandChild : new ArrayList<>(repairNodeChild.getChildren())) {
                        RepairNode newNode = grandChild.flattenRepairTree();
                        this.addChild(newNode);
                    }
                    repairNodeChild.delete();
                }
                else if (repairNodeChild.getChildren().size() == 1) {
                    RepairNode grandChild = repairNodeChild.getChildren().stream().findFirst().get();
                    RepairNode newNode = grandChild.flattenRepairTree();
                    this.addChild(newNode);
                    repairNodeChild.delete();
                    this.flattenRepairTree();
                }
                else if(repairNodeChild.getChildren().isEmpty() && !(repairNodeChild instanceof ConsistencyRepairAction)){
                    repairNodeChild.delete();
                }

            }
            if(this.getChildren().size() == 1) {
                RepairNode repairChild = this.getChildren().stream().findFirst().get();
                if(!repairChild.getChildren().isEmpty())
                    return repairChild;
            }
        }
        return this;
    }

    @Override
    public Set<Repair> getRepairsFilteredByOwner(String ownerId) {
        this.highlightOwnedRepairs(ownerId);
        Set<Repair> repairs = this.getRepairs();
        Set<Repair> filteredRepairs = new HashSet<>();
        if(ownerId==null) return repairs;

        for (Iterator<Repair> iterator = repairs.iterator(); iterator.hasNext();) {
            Repair repair = iterator.next();
            List<RepairAction> actions = new ArrayList<>(repair.getRepairActions());
            for (RepairAction ra : actions) {
                if(ra.getOwners() != null && ra.getOwners().contains(ownerId)) {
                    filteredRepairs.add(repair);
                    break;
                }
            }
        }
        return filteredRepairs;
    }


    /**
     * Generates concrete repair actions from the abstract one
     * @param abstractRepair abstractRepair to be converted
     * @param objects objects where concrete values are extracted from
     * @return concrete repairs
     */
    @Override
    public Map<RepairAction,Set<RepairAction>> generateConcreteRepairActions(Repair abstractRepair, Set<Object> objects){
        Map<RepairAction,Set<RepairAction>> concreteRepairs = new HashMap<>();
        ConcreteRepairValueGenerator valueGenerator = ConcreteRepairValueGenerator.getInstance();
        Set<RepairAction> abstractActions = new HashSet<>(abstractRepair.getRepairActions());
        for(RepairAction repairAction: abstractActions.stream().filter(a->a.isAbstract()).collect(Collectors.toSet())) {
            AbstractRepairAction abstractRepairAction = (AbstractRepairAction) repairAction;
            Instance repairInstance = (Instance) abstractRepairAction.getElement();
            Set<RepairAction> concreteActions = new HashSet<>();
            switch (abstractRepairAction.getOperator()) {
                case REMOVE:
                    if(repairInstance.getProperty(abstractRepairAction.getProperty()) instanceof SetProperty) {
                        SetProperty<Object> possibleValues = (SetProperty<Object>) repairInstance.getPropertyAsSet(abstractRepairAction.getProperty());
                        for (Object obj : possibleValues) {
                            concreteActions.add(new ConsistencyRepairAction(abstractRepairAction.getParent(), abstractRepairAction.getProperty(),
                                    repairInstance, new RepairSingleValueOption(Operator.REMOVE, obj), abstractRepairAction.evalNode));
                            concreteRepairs.put(abstractRepairAction, new HashSet<>(concreteActions));
                        }
                    }else if(repairInstance.getProperty(abstractRepairAction.getProperty()) instanceof ListProperty) {
                        ListProperty<Object> possibleValues = (ListProperty<Object>) repairInstance.getPropertyAsList(abstractRepairAction.getProperty());
                        for (Object obj : possibleValues) {
                            concreteActions.add(new ConsistencyRepairAction(abstractRepairAction.getParent(), abstractRepairAction.getProperty(),
                                    repairInstance, new RepairSingleValueOption(Operator.REMOVE, obj), abstractRepairAction.evalNode));
                            concreteRepairs.put(abstractRepairAction, new HashSet<>(concreteActions));
                        }
                    }
                    break;
                case MOD_EQ:
                case MOD_NEQ:
                case MOD_GT:
                case MOD_LT:
                    Set<Object> concreteValues = valueGenerator.getAllValues(objects,abstractRepairAction.getProperty(),repairInstance.getPropertyAsValue(abstractRepairAction.getProperty()).getClass()); //get possible values from concrete generator
                    for(Object value: concreteValues){
                        concreteActions.add(new ConsistencyRepairAction(abstractRepairAction.getParent(), abstractRepairAction.getProperty(),
                                repairInstance, new RepairSingleValueOption(Operator.MOD_EQ,value), abstractRepairAction.evalNode));
                    }
                    concreteRepairs.put(abstractRepairAction,new HashSet<>(concreteActions));
                    break;
                case ADD:
                    PropertyType propertyType = repairInstance.getProperty(abstractRepairAction.getProperty()).propertyType();
                    InstanceType type = (InstanceType) propertyType.getPropertyAsValue(ReservedNames.REFERENCED_INSTANCE_TYPE);
                    Set<Object> concreteAddValues = null;
                    String basicTypeClassName = getBasicClassName(type);
                    if(basicTypeClassName!=null) {
                        try {
                            concreteAddValues = valueGenerator.getAllValues(objects,Class.forName(basicTypeClassName));
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else
                        concreteAddValues= valueGenerator.getAllInstancesAsValues(objects,type);
                    for(Object value: concreteAddValues){
                        if(!value.equals(repairInstance) && !repairInstance.getPropertyAsSet(abstractRepairAction.getProperty()).contains(value))
                            concreteActions.add(new ConsistencyRepairAction(abstractRepairAction.getParent(), abstractRepairAction.getProperty(),
                                    repairInstance, new RepairSingleValueOption(Operator.ADD,value), abstractRepairAction.evalNode));
                    }
                    concreteRepairs.put(abstractRepairAction,new HashSet<>(concreteActions));
                    break;
                // TODO generate concrete repair for adding and modifying a new element
            }
        }
        // now also add any already concrete repair actions to this set:
        abstractActions.stream()
                .filter(a->!a.isAbstract())
                .forEach(a -> concreteRepairs.put(a, Set.of(a)));


        return concreteRepairs;
    }

    private String getBasicClassName(InstanceType type){
        List<String> basicTypesStrings = new ArrayList<>();
        basicTypesStrings.add("string");
        basicTypesStrings.add("long");
        basicTypesStrings.add("integer");
        basicTypesStrings.add("boolean");
        basicTypesStrings.add("real");
        basicTypesStrings.add("number");
        String className = null;
        if(basicTypesStrings.contains(type.name().toLowerCase())){
            className = "java.lang.";
            switch (type.name()) {
                case "Number":
                case "Real":
                    className = className.concat("Number");
                    break;
                case "Long":
                case "Integer":
                    className = className.concat("Long");
                    break;
                default:
                    className = className.concat(type.name());
                    break;
            }
        }
        return className;

    }


    /**
     * Converts abstract repairs into concrete ones
     * @param abstractRepair
     * @param objects list of objects containing concrete values
     * @param validate sets a boolean to check if repairs must be validated now or later
     * @return list of concrete repairs
     */
    @Override
    public Set<Repair> convertAbstractToConcrete(Repair abstractRepair, Set<Object> objects, boolean validate) {
        ConsistencyRule inconsistency = (ConsistencyRule) abstractRepair.getInconsistency();
        Set<Repair> concreteRepairs = new HashSet<>();
        Map<RepairAction,Set<RepairAction>> concreteMap = generateConcreteRepairActions(abstractRepair, objects);
        List<Set<RepairAction>> concreteRepairActions = new ArrayList<>();
        for (Map.Entry<RepairAction,Set<RepairAction>> entry : concreteMap.entrySet()) {
            concreteRepairActions.add(entry.getValue());
        }
        Set<Set<RepairAction>> cartesian = cartesianProduct(concreteRepairActions);
        if(validate)
            validateRepairs(cartesian, concreteRepairs, inconsistency);
        else {
            for(Set<RepairAction> repairActions: cartesian) {
                if(!repairActions.isEmpty()){
                    ConsistencyRepair repair = new ConsistencyRepair(getInconsistency());
                    repair.addRepairActions(repairActions);
                    concreteRepairs.add(repair);
                }
            }
        }
        return concreteRepairs;
    }

    /**
     * Validate concrete repairs
     * @param cartesian
     * @param concreteRepairs
     * @param inconsistency
     * @return repairs validated
     */
    public void validateRepairs(Set<Set<RepairAction>> cartesian,  Set<Repair> concreteRepairs,
                                ConsistencyRule inconsistency){
        for(Set<RepairAction> repairActions: cartesian){
            ConsistencyRepair repair = new ConsistencyRepair(getInconsistency());
            repair.addRepairActions(repairActions);
            try {
                repair.execute();
                inconsistency.workspace.concludeTransaction();
                boolean isRepaired = (inconsistency.isConsistent());
                if(isRepaired && !repairActions.isEmpty()) {
                    concreteRepairs.add(repair);
                }
                repair.undo();
                inconsistency.workspace.concludeTransaction();
                if(concreteRepairs.size()>=concreteLimit) return; // to stop validation

            } catch (ChangeExecutionException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Generates a cartesian product of possible concrete repairs.
     * @param sets
     * @return cartesian product of possible concrete repairs
     */
    @SuppressWarnings("unchecked")
    public Set<Set<RepairAction>> cartesianProduct(List<Set<RepairAction>> sets) {
        if (sets.size() < 2) {
            Set<Set<RepairAction>> cartesian = new HashSet<>();
            if(!sets.isEmpty()) {
                Set<RepairAction> actions = sets.get(0);
                for (RepairAction repairAction : actions) {
                    Set<RepairAction> newRepair = new HashSet<>();
                    newRepair.add(repairAction);
                    cartesian.add(new HashSet<>(newRepair));
                }
            }
            return cartesian;
        }

        return _cartesianProduct(0, sets);
    }

    private Set<Set<RepairAction>> _cartesianProduct(int index, List<Set<RepairAction>> sets) {
        Set<Set<RepairAction>> ret = new HashSet<>();
        if (index == sets.size()) {
            ret.add(new HashSet<>());
        } else {
            for (RepairAction obj : sets.get(index)) {
                if(ret.size()>50000)
                    return ret;
                for (Set<RepairAction> set : _cartesianProduct(index+1, sets)) {
                    set.add(obj);
                    ret.add(set);
                }
            }
        }

        return ret;
    }
    @Override
    public Object getInconsistency() {
        return cre;
    }

    @Override
    public void setInconsistency(Object cre) {
        this.cre = cre;
    }
    @Override
    public void delete() {
        if(parent!=null)
            this.getParent().removeChild(this);
        this.setParent(null);
    }
}