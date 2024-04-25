package at.jku.isse.designspace.rule.arl.repair.changepropagation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import at.jku.isse.designspace.core.events.Operation;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.rule.arl.repair.AbstractRepairAction;
import at.jku.isse.designspace.rule.arl.repair.ConsistencyRepairAction;
import at.jku.isse.designspace.rule.arl.repair.Repair;
import at.jku.isse.designspace.rule.arl.repair.RepairAction;
import at.jku.isse.designspace.rule.arl.repair.RepairSingleValueOption;
import at.jku.isse.designspace.rule.model.ConsistencyRule;

/**
 * @author Luciano
 * RepairModelState defines a state reached by performing a repair or change.
 */
public class ModelState {
    private Set<ConsistencyRule> inconsistencies;
    private Set<ConsistencyRule> ignoredInconsistencies;
    private Repair repair;
    private List<Repair> previousRepairs;
    private List<Operation> operations;
    private Set<ModelState> parents;
    private Set<ModelState> children;
    private Change change;
    private boolean consistent;
    private long inconsistenciesHash;
    private int numberOfInconsistencies;
    private int xOrder;
    private long repairHashcode;
    private boolean isInitial;
    private String name;
    private Set<Repair> repairAlternatives;
    private Set<ModelState> conflictingVertices;

    public ModelState(Repair repair, Set<ConsistencyRule> consistencyRules, Set<ConsistencyRule> ignoredInconsistencies, boolean initial) {
        this.repair = repair;
        this.isInitial = initial;
        this.conflictingVertices = new HashSet<>();
        previousRepairs = new ArrayList<>();
        this.parents = new HashSet<>();
        this.children = new HashSet<>();
        if(repair != null){
            repairHashcode = repair.hashCode();
            this.previousRepairs.add(repair);
        }
        this.inconsistencies = new HashSet<>();
        this.inconsistencies.addAll(consistencyRules);
        this.inconsistenciesHash = inconsistencies.hashCode();
        if(inconsistencies.isEmpty())
            consistent = true;
        else
            consistent = false;
        numberOfInconsistencies = inconsistencies.size();
        if(ignoredInconsistencies!=null)
            this.ignoredInconsistencies=ignoredInconsistencies;
        else
            this.ignoredInconsistencies = new HashSet<>();
        repairAlternatives = new HashSet<>();
    }

    private void generateStateName() {
        name = " "+getLevel() + "." +getXOrder();
    }

    public String getName(){
        generateStateName();
        return name;
    }

    public Change getUserChange(){
        return this.change;
    }

    public String getUserChangeString(){
        if (this.change!=null)
            return this.change.toString();
        if(parents==null || parents.isEmpty())
            return this.children.stream().findFirst().get().getUserChangeString();
        if(parents!=null && !parents.isEmpty())
            return parents.stream().findFirst().get().getUserChangeString();

        return "";
    }

    public void setChange(Change change) {
        this.change = change;
    }

    public boolean isConsistent() {return  consistent;}


    public int getXOrder() {
        return xOrder;
    }

    public boolean isAbstract(){
        return repair == null ? false : repair.isAbstract();
    }

    public Set<ModelState> getParents() {
        return parents;
    }

    public void addParent(ModelState parent) {
        this.parents.add(parent);
    }


    public Set<ModelState> getChildren() {
        return children;
    }

    public void addChild(ModelState child) {
        this.children.add(child);
    }

    public void removeFromParents() {
        for(ModelState p : parents){
            p.removeChild(this);
        }
        this.parents.clear();
    }
    public void removeChild(ModelState child) {
        this.children.remove(child);
    }

    public void setXOrder(int c){
        xOrder = c;
    }
    public int getNumberOfInconsistencies() {return  numberOfInconsistencies;}

    public int getLevel(){
        if(isInitial())
            return 0;
        if(previousRepairs==null || previousRepairs.isEmpty())
            return 1;
        return previousRepairs.size()+1;
    }
    public Repair getRepair() {
        return repair;
    }
    public Repair getLastRepair() {
        if(previousRepairs.isEmpty()) return null;
        return previousRepairs.get(previousRepairs.size()-1);
    }
    public void setIgnoredInconsistencies(Set<ConsistencyRule> ignoredInconsistencies) {
        this.ignoredInconsistencies = ignoredInconsistencies;
    }
    public void removePastInconsistencies(){
        this.inconsistencies.removeAll(ignoredInconsistencies);
        this.inconsistenciesHash = inconsistencies.hashCode();
        if(inconsistencies.isEmpty())
            consistent = true;
        else
            consistent = false;
        numberOfInconsistencies = inconsistencies.size();
    }
    public boolean isConflict(ModelState o) {
        if(this.getRepair() == null && this.getUserChange() == null) return false;
        if(o.getRepair() == null && o.getUserChange() == null) return false;
        if (o == null || this.equals(o)|| getClass() != o.getClass()
                || this.getConflictingVertices().contains(o)) return false;
        if(this.equals(o)) return true;
        if(this.getRepair()==null) { // in case the current state only has a user change
            if (o.getRepair() == null)
                return false;
            else
                return o.compareRepairAndChange(this);
        }
        if(this.compareRepair(o.getRepair())) return true;
        return this.compareRepairAndChange(o);
    }
    public void addConflict(ModelState m){
        this.conflictingVertices.add(m);
    }
    public List<Repair> getPreviousRepairs() {
        return previousRepairs;
    }

    public Set<ModelState> getConflictingVertices(){
        return this.conflictingVertices;
    }

    public boolean isInitial() {
        return isInitial;
    }
    public boolean isFinal() {
        return children.isEmpty();
    }

    public String getRepairString() {
        StringBuilder sb = new StringBuilder(128);
        for (Repair repair : previousRepairs) {
            sb.append("<");
            sb.append(repair.toString());
            sb.append(">");
        }
        return sb.toString();
    }

    /**
     * Add repairs previously executed
     * @param previousRepairs repairs executed by parent model states
     */
    public void addPreviousRepairs(List<Repair>  previousRepairs) {
        List<Repair> oldRepairs = new ArrayList<>(this.previousRepairs);
        if(!this.previousRepairs.isEmpty()){
            this.previousRepairs.clear();
        }
        this.previousRepairs.addAll(previousRepairs);
        this.previousRepairs.addAll(oldRepairs);
    }

    /**
     * Compares two repairs to check if they have conflicts
     * @param r
     * @return
     */
    public boolean compareRepair(Repair r) {
        if (this.repair==null || r ==null) return false;
        List<RepairAction> actions = new ArrayList<>(repair.getRepairActions());
        List<RepairAction> actions2 = new ArrayList<>(r.getRepairActions());
        for (RepairAction action : actions) {
            for (RepairAction otherAction : actions2) {
                if (action.checkConflict(otherAction)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Compares all previous repairs of this state with those from other
     * @param state
     * @return
     */
    public boolean compareAllRepairs(ModelState state) {
        if(this.previousRepairs.isEmpty() || state.previousRepairs.isEmpty()) return false;
        for (Repair repair: previousRepairs) {
            boolean conflicts = false;
            for(Repair r: state.previousRepairs) {
                if(repair.checkConflict(r)){
                    conflicts = true;
                    break;
                }
            }
            if(!conflicts)
                return false;
        }
        return true;
    }

    public boolean compareRepairAndChange(ModelState otherState){
        Change c = otherState.getUserChange();
        return compareRepairAndChange(c);
    }

    public boolean compareRepairAndChange(Change c){
        if(c==null) return false;
        Set<AbstractRepairAction> repairActions = (Set<AbstractRepairAction>) this.repair.getRepairActions();
        AbstractRepairAction changeAction = new ConsistencyRepairAction(null,c.getProperty(),
                (Instance) c.getElement(),new RepairSingleValueOption(c.getOperator(),c.getValue()), null); //TODO: is null safe here?
        for (AbstractRepairAction ar : repairActions){
            if(changeAction.checkConflict(ar)) return true;
        }
        for (AbstractRepairAction ar : repairActions){
            for(Object obj: c.getSubChanges()){
                Change subChange = (Change) obj;
                if(subChange.getElement() == null)
                    System.out.println();
                changeAction = new ConsistencyRepairAction(null,subChange.getProperty(),
                        (Instance) subChange.getElement(),new RepairSingleValueOption(subChange.getOperator(),subChange.getValue()), null);//TODO: is null safe here?
                if(changeAction.checkConflict(ar)) return true;
            }
        }
        return false;
    }




    public void setOperations(List<Operation> operations) {
        this.operations = operations;
    }
    public List<Operation> getOperations() {
        return operations;
    }


    public Set<ConsistencyRule> getInconsistencies() {
        return new HashSet<>(inconsistencies);
    }


    public Set<ConsistencyRule> getIgnoredInconsistencies() {
        return ignoredInconsistencies;
    }
    public String getInconsistenciesString() {
        StringBuilder sb = new StringBuilder(128);

        for (ConsistencyRule cre : inconsistencies) {
            sb.append("(");
            sb.append(cre.getPropertyAsValue("name") == null ?  "?" : cre.getPropertyAsValue("name"));
            sb.append(",");
            sb.append(cre.getPropertyAsValue("contextInstance") == null ?  "?"
                    : ((Instance)cre.getPropertyAsValue("contextInstance")).getPropertyAsValue("name"));
            sb.append(")");
        }
        return sb.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ModelState that = (ModelState) o;
        return hashCode() == that.hashCode();
    }



    public int getPreviousRepairsHash(){
        Set<Repair> previousRepairsSet = new HashSet<>(previousRepairs);
        return previousRepairsSet.hashCode();
    }

    @Override
    public int hashCode() {
        Set<Repair> previousRepairsSet = new HashSet<>(previousRepairs);
        return Objects.hash(inconsistenciesHash,previousRepairsSet.hashCode(),isInitial()); // to consider set of repairs without considering the order
        //return Objects.hash(inconsistenciesHash,previousRepairs.hashCode()); // to consider list of repairs  considering the order (tree)
        //return Objects.hash(inconsistenciesHash,repair); // to consider only last repair, it causes problem with previous repair conflicts
    }

    @Override
    public String toString() {
        return "<"+getName()+" inconsistencies:"+ getInconsistencies().size()+
                ", isConsistent=" + isConsistent()  +
                '>';
    }



    public String getRepairAlternativesString() {
        if(repairAlternatives == null || repairAlternatives.isEmpty()) {
            if(getChildren()== null || getChildren().isEmpty()) return "";
            repairAlternatives = new HashSet<>();
            for (ModelState child : getChildren()){
                if(child.getLastRepair()==null) return "";
                repairAlternatives.add(child.getLastRepair());
            }
        }
        StringBuilder sb = new StringBuilder(128);
        for (Repair repair : repairAlternatives) {
            sb.append("<");
            sb.append(repair.toString());
            sb.append(">");
        }
        return sb.toString();
    }
}
