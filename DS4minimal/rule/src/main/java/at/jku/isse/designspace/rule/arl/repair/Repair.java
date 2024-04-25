package at.jku.isse.designspace.rule.arl.repair;

import java.util.Collection;

import at.jku.isse.designspace.rule.arl.evaluator.RuleEvaluation;

public interface Repair<E> extends ChangeExecution, Comparable<Repair>{

    /**
     *
     * @return all {@link RepairAction}s being part of <code>this</code>{@link Repair}.
     */
    Collection<RepairAction> getRepairActions();
    void addRepairAction(RepairAction repairAction);
    void removeRepairAction(RepairAction repairAction);

    boolean checkConflict(Repair r);


    void addRepairActions(Collection<RepairAction> repairActions);
    void removeRepairActions(Collection<RepairAction> repairActions);
    void addSideEffects(Collection<SideEffect> sideEffects);
    /**
     *
     * @return the {@link SideEffect}s executing this {@link Repair} has on other {@link RuleEvaluation}s.
     */
    Collection<SideEffect> getSideEffects();
    Collection<SideEffect> getSideEffects(SideEffect.Type type);
    Collection<SideEffect> getSideEffects(E inconsistency);
    Object getInconsistency();

}
