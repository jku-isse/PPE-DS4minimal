package at.jku.isse.designspace.rule.arl.repair;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RepairNode extends ChangeExecution {

    enum Type {
        ALTERNATIVE {
            @Override
            public String toString() {
                return "*";
            }
        },
        SEQUENCE {
            @Override
            public String toString() {
                return "+";
            }
        },
        VALUE, MULTIVALUE
    }

    // Added Code
    public void setScore(double sc);
    public double getScore();
    public void setRank(int r);
    public int getRank();
     // End
    
    /**
     * Returns the parent of a repair action. The parent is null if it is the
     * top node of the repair action tree.
     *
     * @return the parent of the repair action
     */
    RepairNode getParent();
    RepairNode getRoot();
    void setParent(RepairNode parent);

    /**
     * Returns the children of this node. If it is a leave not it returns an
     * empty set.
     *
     * @return the children of this node
     */
    List<RepairNode> getChildren();

    void addChild(RepairNode child);

    void removeChild(RepairNode child);

    RepairNode.Type getNodeType();

    void setNodeType(RepairNode.Type type);


    int getHeight();
    int getHeight(RepairNode node);

    /**
     * Returns the repair Actions of this node. If it is a leave not it returns an
     * empty set.
     *
     * @return the repairs Actions
     */
    Set<RepairAction> getRepairActions();

    /**
     *    * highlights the repair actions based on user ownership
     *
     * @param ownerId id of the owner
     * @return a highlighted repair node
     */

    RepairNode highlightOwnedRepairs(String ownerId);

    /**
     * Returns the repairs of this node. If it is a leave not it returns an
     * empty set.
     *
     * @return the repairs
     */
    Set<Repair> getRepairs();
    Set<Repair> filterRepairs(Set<Repair> repairs);


    /**
     * Returns the number of validated concrete repairs specified by @param limit (the order is random)
     * @param objects other objects to extract values for the generation
     * @param limit max number of repairs
     * @return
     */
    Set<Repair> getConcreteRepairs(Set<Object> objects, int limit);

    /**
     * Returns concrete repairs
     * @param objects other objects to extract values for the generation
     * @param validate sets a boolean to check if repairs must be validated
     * @param keepAbstract to check if abstract repairs should be kept
     * @return concrete repairs
     */
    Set<Repair> getConcreteRepairs(Set<Object> objects, boolean validate, boolean keepAbstract);

    /**
     * Returns concrete repairs filtered by ownership
     * @param  ownerId User id of the owner
     * @param objects other objects to extract values for the generation
     * @param validate sets a boolean to check if repairs must be validated
     * @param keepAbstract to check if abstract repairs should be kept
     * @return concrete repairs
     */
    Set<Repair> getConcreteRepairsFilteredByOwner(String ownerId, Set<Object> objects, boolean validate, boolean keepAbstract);
    /**
     * Generates concrete repairs from an abstract one
     * @param abstractRepair abstractRepair to be converted
     * @param objects objects where values are extracted
     * @return concrete repairs
     */
    Map<RepairAction,Set<RepairAction>> generateConcreteRepairActions(Repair abstractRepair, Set<Object> objects);

    /**
     * Converts an abstract repair into a set of possible concrete repairs based on the property being repaired
     * @param abstractRepair
     * @param objects
     * @param validate sets a boolean to check if repairs must be validated
     * @return set of concrete repairs
     */
    Set<Repair> convertAbstractToConcrete(Repair abstractRepair, Set<Object> objects,
                                          boolean validate);

    /**
     *    * highlights the repair actions based on user ownership
     *
     * @param ownerId id of the owner
     * @return a highlighted repair node
     */

    Set<Repair> getRepairsFilteredByOwner(String ownerId);

    /**
     * Flattens the repair tree removing redundancies
     *  @return flatten repairTree
     */
    RepairNode flattenRepairTree();

    boolean checkForLimit(int limit);

    Set<Repair> generateRepairsSample();
    Object getInconsistency();

    void setInconsistency(Object cre);

    void delete();
	
}
