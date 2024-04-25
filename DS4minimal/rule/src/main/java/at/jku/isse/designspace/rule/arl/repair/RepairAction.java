package at.jku.isse.designspace.rule.arl.repair;

import java.util.List;

import at.jku.isse.designspace.rule.arl.evaluator.EvaluationNode;
import at.jku.isse.designspace.rule.arl.exception.ChangeExecutionException;

public interface RepairAction<E> extends RepairNode {


    /**
     *
     * @return the {@link Operator} of this {@link RepairAction}
     */
    Operator getOperator();
    String getProperty();
    E getElement();
    void setElement(E element);
    Object getValue();
    void setValue(Object value);
    Object getOldValue();
    EvaluationNode getEvalNode();

    /**
     * Calculates whether <code>this<code> {@link RepairAction} conflicts with
     * <code>other<code/>, i.e. applying both would render at least of them
     * obsolete (have no effect).
     *
     * @param other
     *            The other {@link RepairAction} to test with
     * @return whether the {@link RepairAction}s are contradicting.
     */
    boolean interferesWith(RepairAction other);

    /**
     * Checks if repair actions conflict with each other.
     *
     * @param other repairAction to be checked
     * @return true if repairAction is undoing other repairAction
     */
    boolean checkConflict(RepairAction other);

    /**
     * Executes this {@link RepairAction} on an element other than the element
     *
     * @param element
     * @throws ChangeExecutionException
     */
    void execute(Object element) throws ChangeExecutionException;

    void undo(Object element) throws ChangeExecutionException;

    boolean isHighlight();

    List<String> getOwners();

}
