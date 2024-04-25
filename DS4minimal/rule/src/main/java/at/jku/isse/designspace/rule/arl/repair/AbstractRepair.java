package at.jku.isse.designspace.rule.arl.repair;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import at.jku.isse.designspace.rule.arl.exception.ChangeExecutionException;
import at.jku.isse.designspace.rule.arl.repair.changepropagation.Change;

public abstract class AbstractRepair<E> implements Repair<E> {

    protected Set<RepairAction> repairActions;
    protected Set<SideEffect> sideEffects;
    private boolean executed;
    protected Object cre;

    protected AbstractRepair() {
        this.repairActions = new HashSet<>();
        this.sideEffects = new HashSet<>();
        executed = false;
    }

    protected AbstractRepair(Collection<RepairAction> repairActions, Object cre) {
        this();
        if (cre == null)
            throw new IllegalArgumentException("RuleEvaluation must not be null!");

        if (repairActions != null) {
            this.repairActions.addAll(repairActions);
        }
        this.cre = cre;
    }

    @Override
    public boolean isExecutable() {
        Iterator<RepairAction> repairActionIterator = this.repairActions.iterator();
        boolean executable = repairActionIterator.hasNext();
        while (executable && repairActionIterator.hasNext()
                && (executable &= repairActionIterator.next().isExecutable())) {
        }
        return executable;
    }

    @Override
    public boolean isAbstract() {
        boolean isAbstract = false;
        Iterator<RepairAction> iterator = repairActions.iterator();
        while (!isAbstract && iterator.hasNext()) {
            isAbstract |= iterator.next().isAbstract();
        }
        return isAbstract;
    }

    @Override
    public void execute() throws ChangeExecutionException {
        if (isExecutable()) {
            for (RepairAction repairAction : getRepairActions()) {
                repairAction.execute();
            }

            executed = true;

        }
    }


    @Override
    public boolean checkConflict(Repair r) {
        Collection<RepairAction> actions = this.getRepairActions();
        Set<RepairAction> actions2 = (Set<RepairAction>) r.getRepairActions();
        for (RepairAction a1 : actions){
            for (RepairAction a2 : actions2){
                if(a1.checkConflict(a2)){
                    return true;
                }
            }

        }
        return false;
    }


    @Override
    public Collection<RepairAction> getRepairActions() {
        return Collections.unmodifiableSet(this.repairActions);
    }

    @Override
    public void addRepairAction(RepairAction repairAction) {
        if (!this.repairActions.contains(repairAction)) {
            this.repairActions.add(repairAction);
        }
    }

    @Override
    public void removeRepairAction(RepairAction repairAction) {
        repairActions.remove(repairAction);
    }

    @Override
    public void addRepairActions(Collection<RepairAction> repairActions) {
        if (repairActions != null) {
            for (RepairAction repairAction : repairActions) {
                addRepairAction(repairAction);
            }
        }
    }

    @Override
    public void removeRepairActions(Collection<RepairAction> repairActions) {
        for (RepairAction ra : repairActions) {
            removeRepairAction(ra);
        }
    }

    @Override
    public void addSideEffects(Collection<SideEffect> sideEffects) {
        if(this.sideEffects == null) this.sideEffects = new HashSet<>();
        this.sideEffects.addAll(sideEffects);
    }

    @Override
    public Collection<SideEffect> getSideEffects() {
       return sideEffects;
    }

    @Override
    public Collection<SideEffect> getSideEffects(SideEffect.Type type) {
        return sideEffects.stream().filter(eSideEffect -> eSideEffect.getSideEffectType().equals(type)).collect(Collectors.toSet());
    }

    @Override
    public Collection<SideEffect> getSideEffects(E inconsistency) {
        return sideEffects.stream().filter(eSideEffect -> eSideEffect.getInconsistency().equals(inconsistency)).collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        Iterator<RepairAction> actionsIterator = this.repairActions.iterator();
        sb.append("{");
        while (actionsIterator.hasNext()) {
            RepairAction action = actionsIterator.next();
            sb.append(action.toString());
            if (actionsIterator.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 0;
        if(repairActions.isEmpty()) return Objects.hash(cre);
        for (RepairAction r : repairActions){
            hash += r.hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Change){ // to compare repair with changes
            for(RepairAction action : getRepairActions()){
                AbstractRepairAction abstractRepairAction = (AbstractRepairAction) action;
                if(abstractRepairAction.equals(obj))
                    return true;
            }
        }
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AbstractRepair<E> other = (AbstractRepair<E>) obj;
        if (repairActions == null) {
            return other.repairActions == null;
        } else return repairActions.equals(other.repairActions);
    }


    @Override
    public boolean isUndoable() {
        return isExecutable();
    }

    @Override
    public void undo() throws ChangeExecutionException {
        if (isUndoable()) {
            for (RepairAction action : getRepairActions()) {
                action.undo();
            }
        }

        executed = false;
    }

    @Override
    public boolean executed() {
        return executed;
    }

    @Override
    public Object getInconsistency() {
        return cre;
    }


}
