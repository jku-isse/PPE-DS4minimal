package at.jku.isse.designspace.rule.arl.repair.changepropagation;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;

import at.jku.isse.designspace.rule.arl.repair.Repair;
import at.jku.isse.designspace.rule.arl.repair.SideEffect;
import at.jku.isse.designspace.rule.model.ConsistencyRule;

public class ChangeEdge<CT> extends DefaultEdge {
    private CT change;
    private boolean isAbstract;
    private Set<ConsistencyRule> positiveSideEffects;
    private Set<ConsistencyRule> negativeSideEffects;
    public ChangeEdge(CT c, boolean isAbstract){
        change = c;
        this.isAbstract = isAbstract;
        positiveSideEffects = new HashSet<>();
        negativeSideEffects = new HashSet<>();
    }
    public ChangeEdge(CT c, boolean isAbstract, Set<ConsistencyRule> positiveSideEffects, Set<ConsistencyRule> negativeSideEffects){
        change = c;
        this.isAbstract = isAbstract;
        this.positiveSideEffects = positiveSideEffects;
        this.negativeSideEffects = negativeSideEffects;
    }
    public CT getChange(){
        return change;
    }

    public boolean isAbstract(){
        return isAbstract;
    }

    public Set<ConsistencyRule> getPositiveSideEffects() {
        return new HashSet<>(positiveSideEffects);
    }

    public Set<ConsistencyRule> getNegativeSideEffects() {
        return new HashSet<>(negativeSideEffects);
    }

    public void saveSideEffects(Set<ConsistencyRule> previousInconsistencies, Set<ConsistencyRule> newInconsistencies){
        negativeSideEffects = new HashSet<>(newInconsistencies);
        negativeSideEffects.removeAll(previousInconsistencies);

        positiveSideEffects = new HashSet<>(previousInconsistencies);
        positiveSideEffects.removeAll(newInconsistencies);
        if(change instanceof Repair) {
            Set<SideEffect> sideEffects = new HashSet<>();
            for (ConsistencyRule icon : positiveSideEffects){
                SideEffect se = new SideEffect(icon,2);
                sideEffects.add(se);
            }
            for (ConsistencyRule icon : negativeSideEffects){
                SideEffect se = new SideEffect(icon,1);
                sideEffects.add(se);
            }
            ((Repair) change).addSideEffects(sideEffects);
        }
    }

    @Override
    public String toString() {
        return change.toString();
    }
}
