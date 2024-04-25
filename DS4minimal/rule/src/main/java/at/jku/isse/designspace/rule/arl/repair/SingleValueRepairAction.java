package at.jku.isse.designspace.rule.arl.repair;


public interface SingleValueRepairAction<E> extends RepairAction<E> {

    /**
     * Returns the value that must be assigned to the scope element.
     *
     * @return the value that must be assigned to the scope element.
     */
    Object getValue();

    /**
     * Returns a clone of this action with an different assignable value applied
     * to the cloned action. It should be used if more than one assignable value
     * exists for this action.
     *
     * @param assignableValue
     *            The new assignable value for the cloned repair action.
     * @return A clone of this RepairAction.
     */
    RepairAction clone(Object assignableValue);


}