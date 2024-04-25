package at.jku.isse.designspace.rule.arl.repair;


import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.rule.arl.evaluator.EvaluationNode;
import at.jku.isse.designspace.rule.arl.exception.ChangeExecutionException;
public abstract class AbstractRepairAction<E> extends AbstractRepairNode implements RepairAction<E> {

    protected String property;
    protected RepairSingleValueOption repairValueOption;
    protected Object oldValue;
    protected E contextElement;
    private final static Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    protected boolean highlight;
    protected List<String> owners;
    protected EvaluationNode evalNode;

    public AbstractRepairAction(RepairNode parent, String property, E contextElement, RepairSingleValueOption repairValueOption, EvaluationNode evalNode) {
        if(repairValueOption.getValue() == null && contextElement == null)
            return;
        else
            this.repairValueOption = repairValueOption;
        this.property = property;
        this.contextElement = contextElement;
        oldValue = null;
        highlight = false;
        this.parent = parent;
        //children = new HashSet<>();
        children=new LinkedList<>();
        this.type = RepairNode.Type.VALUE;
        if(this.parent!=null)
            setInconsistency(this.parent.getInconsistency());
        if (parent != null)
            this.parent.addChild(this);
        this.evalNode = evalNode;        
        addRepairToEvaluationNode();
    }

    public void addRepairToEvaluationNode() {
    	evalNode.markAsOnRepairPath();
        evalNode.addRepair(this);    	
    }

    public Object getOldValue() {
        return oldValue;
    }
    @Override
    public E getElement() {
        return contextElement;
    }

    @Override
    public void setElement(E element){
        this.contextElement = element;
    }


    @Override
    public String getProperty() {
        return property;
    }
    @Override
    public EvaluationNode getEvalNode() {
        return this.evalNode;
    }


    public abstract String getValueString(Object Value);

    public RepairSingleValueOption getRepairValueOption() {
        return repairValueOption;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(getElement() == null ?  "" : getInstanceString());
        sb.append(".");
        sb.append(getProperty() == null ?  "" : getProperty());
        sb.append(" ");
        sb.append(getOperator().toString());
        sb.append(" ");
        sb.append(getValue() == null ?  "?" : getValue());
        sb.append(")");
        return highlight ? String.join("\u0332",sb.toString().split("",-1)): sb.toString();
    }

    private String getInstanceString(){
        Instance i = (Instance) getElement();
        String typeString = i.getInstanceType().toString();
        return i.toString().substring(9)+"("+typeString.substring(14,typeString.length()-6)+"Type)";
    }
    @Override
    public void execute() throws ChangeExecutionException {
        oldValue = repairValueOption.getValue();
        if (oldValue instanceof Collection)
            oldValue = new ArrayList<>((Collection<?>) oldValue);
        execute(contextElement);
        executed = true;
    }

    @Override
    public boolean isUndoable() {
        return executed && oldValue != null;
    }

    @Override
    public void undo() throws ChangeExecutionException {
        undo(getElement());
        executed = false;
    }

    @Override
    public void undo(Object element) throws ChangeExecutionException {
        try {
            switch (getOperator()) {
                case ADD:
                    undoAddAction(element);
                    break;
                case REMOVE:
                    undoDeleteAction(element);
                    break;
                case MOD_EQ:
                    undoModifyAction(element);
                    break;
                default:
                    // other actions are abstract
            }
        } catch (Exception ex) {
            // log.warning("Exception/"+ toString() + ": " + ex.getMessage());
            throw new ChangeExecutionException(ex.getMessage());
        }
    }

    @Override
    public void execute(Object element) throws ChangeExecutionException {
        try {
            switch (getOperator()) {
                case ADD:
                    executeAddAction(element);
                    break;
                case REMOVE:
                    executeDeleteAction(element);
                    break;
                case MOD_EQ:
                    executeModifyAction(element);
                    break;
                default:
                    // other actions are abstract
            }
        } catch (Exception ex) {
            throw new ChangeExecutionException(ex.getMessage());
        }
    }

    @Override
    public void addChild(RepairNode child) {
        throw new UnsupportedOperationException("Repair actions must not have children");
    }

    @Override
    public void removeChild(RepairNode child) {
        throw new UnsupportedOperationException("Repair actions must not have children");
    }

    @Override
    public boolean isAbstract() {
        if (repairValueOption.getValue() != null && repairValueOption.getValue().equals(UnknownRepairValue.UNKNOWN))
            return true;
        switch (getOperator()) {
            case ADD:
            case REMOVE:
            case MOD_EQ:
                return false;
            default:
                return true;
        }
    }

    @Override
    public boolean isExecutable() {
        return !isAbstract();
    }

    @Override
    public Operator getOperator() {
        if (repairValueOption==null) return null;
        return repairValueOption.operator;
    }

    protected abstract void executeAddAction(Object element);

    protected abstract void executeDeleteAction(Object element);

    protected abstract void executeModifyAction(Object element);

    protected abstract void undoAddAction(Object element);

    protected abstract void undoDeleteAction(Object element);

    protected abstract void undoModifyAction(Object element);



    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }
        if(getOperator().equals(Operator.ADD) && getValue()==null) return false;
        AbstractRepairAction other = (AbstractRepairAction) obj;
        if (repairValueOption == null) {
            if (other.repairValueOption != null) {
                return false;
            }
        } else if (!repairValueOption.equals(other.repairValueOption)) {
            return false;
        }
        if (contextElement == null) {
            return other.contextElement == null;
        } else return contextElement.equals(other.contextElement);


    }

    @Override
    public boolean checkConflict(RepairAction ra){
        if(!(ra instanceof AbstractRepairAction)) return false;
        if(ra.getValue()==null || this.getValue()==null) return false;
        if(this.equals(ra)) return true;
        AbstractRepairAction other = (AbstractRepairAction) ra;
        if(ra.getOperator().equals(Operator.ADD) && this.getOperator().equals(Operator.REMOVE)) { // check if other repair is ADD and this repair is REMOVE the same element
            if(this.getValue().equals(UnknownRepairValue.UNKNOWN) || ra.getValue().equals(UnknownRepairValue.UNKNOWN)) return false;
            Instance i = (Instance) this.getValue();
            Instance i2 = (Instance) other.getValue();
            if(i.equals(i2)) return true;
        }
        if(ra.getOperator().equals(Operator.REMOVE) && this.getOperator().equals(Operator.ADD)) { // check if other repair is REMOVE and this repair is ADD the same element
            if(this.getValue().equals(UnknownRepairValue.UNKNOWN) || ra.getValue().equals(UnknownRepairValue.UNKNOWN)) return false;
            Instance i = (Instance) this.getValue();
            Instance i2 = (Instance) other.getValue();
            if(i.equals(i2)) return true;
        }
        if(this.getProperty()!= null && other.getProperty() != null) { // check if is the same repair actions with different values
            Instance i = (Instance) this.getElement();
            Instance i2 = (Instance) other.getElement();
            if (i.equals(i2) && this.getProperty().equals(other.getProperty()))
                return true;
        }
        if(this.getElement() instanceof Instance){ // check if other repair action is Add or Remove and this is Modify on the same element
            Instance i = (Instance) this.getElement();
            if(other.getValue() instanceof Instance) {
                Instance i2 = (Instance) other.getValue();
                if(i.equals(i2)) return true;
            }
        }
        if(this.getValue() instanceof Instance){ // check if this repair action is Add or Remove and other is Modify the same element
            Instance i = (Instance) this.getValue();
            if(other.getElement() instanceof Instance) {
                Instance i2 = (Instance) other.getElement();
                if(i.equals(i2)) return true;
            }
        }

        return false;
    }

    /**
     * Used to checking if repair actions are conflicting in the repair tree level
     * @param other The other {@link RepairAction} to test with     *
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean interferesWith(RepairAction other) {
        Object myValues = getValue();
        if (this instanceof SingleValueRepairAction)
            myValues =  this.getValue();

        Object otherValues = other.getValue();

        if (other instanceof SingleValueRepairAction)
            otherValues = other.getValue();

        if (contextElement == null && Operator.REMOVE == getOperator()) {
            switch (other.getOperator()) {
                case ADD:
                case MOD_EQ:
                    return Objects.equals(myValues, otherValues);
                default:
                    return false;
            }
        } else if (contextElement != null && contextElement.equals(other.getElement())) {
            switch (getOperator()) {
                case ADD:
                    switch (other.getOperator()) {
                        case REMOVE:
                            return Objects.equals(myValues, otherValues);
                        default:
                            return false;
                    }
                case REMOVE:
                    switch (other.getOperator()) {
                        case ADD:
                            return Objects.equals(myValues, otherValues);
                        default:
                            return false;
                    }
                case MOD_EQ:
                    switch (other.getOperator()) {
                        case MOD_EQ:
                            return !Objects.equals(myValues, otherValues);
                        case MOD_GT:
                        case MOD_LT:
                        case MOD_NEQ:
                            return Objects.equals(myValues, otherValues);
                        default:
                            return other.interferesWith(this);
                    }
                case MOD_GT:
                    switch (other.getOperator()) {
                        case MOD_GT:
                        case MOD_NEQ:
                            return false;
                        case MOD_LT:
                            double x1 = ((Number) myValues).doubleValue(), x2 = ((Number) otherValues).doubleValue();
                            return x1 >= x2;
                        default:
                            return other.interferesWith(this);
                    }
                case MOD_LT:
                    switch (other.getOperator()) {
                        case MOD_LT:
                        case MOD_NEQ:
                            return false;
                        default:
                            return other.interferesWith(this);
                    }
                case MOD_NEQ:
                    switch (other.getOperator()) {
                        case MOD_NEQ:
                            return false;
                        default:
                            return other.interferesWith(this);
                    }
                default:
                    return false;
            }
        }
        return false;
    }

    public boolean isHighlight() {
        return highlight;
    }

    public void setHighlight(boolean highlight) {
        this.highlight = highlight;
    }

    public List<String> getOwners() {
        return owners;
    }

    public void setOwners(List<String> owners) {
        this.owners = owners;
    }



}
