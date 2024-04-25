package at.jku.isse.designspace.rule.arl.repair.changepropagation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.SimpleDirectedGraph;

import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.rule.model.ConsistencyRuleType;


/**
 * @author Luciano
 * Generate graphs in parallel, then merges them into a single graph where conflicting states are identified.
 */
public class ParallelGraphGenerator extends AbstractGraphGenerator {



    public ParallelGraphGenerator(ModelState state, Set<ConsistencyRuleType> rules,Workspace w,
                                  boolean preservePastChanges, boolean showConflicts, Set<String> propertiesToSkip) {
        super(state, rules,w,preservePastChanges, showConflicts, propertiesToSkip);
        conflictEdges = new HashSet<>();
    }


    /**
     * Propagates changes individually using the propagators.
     * Then, merge all the generated graphs into one, linking conflicting states.
     */
    public void generateGraph(boolean keepInconsistentPaths, boolean keepAbstractRepairs, boolean generateMergedGraph, boolean addFolderContent) {

        for (int i= 0; i < propagators.size(); i++) {

            RepairChangePropagator propagator = propagators.get(i);
            if(ownerId!=null) propagator.setOwnerId(this.ownerId);
            if(generateMergedGraph)updateChangeHistory(propagator); // add user changes into the propagator
            if(!propagator.isPropagated()) {
                propagator.keepInconsistentPaths = keepInconsistentPaths; // to keep inconsistent nodes where propagation is not possible
                propagator.keepAbstractRepairs = keepAbstractRepairs; // to keep abstract repairs in the graph
                propagator.propagateChange(getObjectsForConcreteValues(propagator, changeHistory, addFolderContent));

            }
            if(!propagator.removePastIncon && i > 0) {     // add edge between two changes
                propagator.getInitialState().addParent(propagators.get(i-1).getInitialState());
                propagators.get(i-1).getInitialState().addChild(propagator.getInitialState());
                ModelStateVertex source = new ModelStateVertex(propagators.get(i-1).getInitialState(),i-1);
                ModelStateVertex target = new ModelStateVertex(propagator.getInitialState(),i);
                if(!this.mergedModelGraph.containsVertex(source)) {
                    this.mergedModelGraph.addVertex(source);
                }
                if(!this.mergedModelGraph.containsVertex(target)) {
                    this.mergedModelGraph.addVertex(target);
                }
                this.mergedModelGraph.addEdge(source,target, new ChangeEdge(propagators.get(i-1).getInitialChange(),false));
            }

        }
        if(generateMergedGraph)
            generateMergedGraph();

    }

    /**
     * Merge graphs into one
     */
    private void generateMergedGraph(){
        for (int i= 0; i < propagators.size(); i++) {
            RepairChangePropagator propagator = propagators.get(i);
            SimpleDirectedGraph<ModelState, ChangeEdge> graph = (SimpleDirectedGraph<ModelState, ChangeEdge>) propagator.getModelGraph();

            for(ChangeEdge re : graph.edgeSet()){
                ChangeEdge edge = new ChangeEdge(re.getChange(), re.isAbstract(),re.getPositiveSideEffects(),re.getNegativeSideEffects());
                ModelStateVertex source = new ModelStateVertex(graph.getEdgeSource(re),i);
                ModelStateVertex target = new ModelStateVertex(graph.getEdgeTarget(re),i);

                if(!this.mergedModelGraph.containsVertex(source)) {
                    this.mergedModelGraph.addVertex(source);
                }
                if(!this.mergedModelGraph.containsVertex(target)) {
                    this.mergedModelGraph.addVertex(target);
                }
                this.mergedModelGraph.addEdge(source,target,edge);

            }
        }
        updateChildOrder();
        generateAllPaths();

        if(showConflicts) {
            findConflicts();
        }
    }



    private void findConflicts() {
        for (int i = 0; i < propagators.size(); i++) {
            RepairChangePropagator propagator = this.getPropagators().get(i);
            if(i>0) {
                for (int j = 0; j < i; j++) {
                    RepairChangePropagator propagator2 = this.getPropagators().get(j);
                    propagator.findConflicts(propagator2, true);
                }
            }
            for(Object obj: propagator.getConflictEdges()) {
                ConflictEdge conflictEdge = (ConflictEdge) obj;
                int state1Hash = conflictEdge.sourceHash;
                int state2Hash = conflictEdge.targetHash;
                ModelStateVertex state1 = mergedModelGraph.vertexSet().stream().filter(modelStateVertex -> modelStateVertex.getState().hashCode() == state1Hash).findFirst().get();
                ModelStateVertex state2 = mergedModelGraph.vertexSet().stream().filter(modelStateVertex -> modelStateVertex.getState().hashCode() == state2Hash).findFirst().get();
                state1.addConflict(state2);
                state2.addConflict(state1);
                ConflictEdge ce = new ConflictEdge("conflicting changes", state1Hash, state2Hash);
                this.mergedModelGraph.addEdge(state1, state2, ce);
                conflictEdges.add(ce);
            }
        }


    }

    /**
     * retrieve the objects that will be used for generating concrete repairs
     * @param propagator
     * @param pastChanges
     * @param addInstances
     * @return
     */
    public Set<Object> getObjectsForConcreteValues(RepairChangePropagator propagator, List<Change> pastChanges, boolean addInstances){
        Set<Object> objects = new HashSet();
        for (Change c : pastChanges) {  // adding only instances that were changed
            if(addInstances){
                if(c.getElement() instanceof Instance) {
                    Instance i = (Instance) c.getElement();
                    objects.add(propagator.workspace.findElement(i.id()));
                }
                if(c.getValue() instanceof Instance) {
                    Instance i2 = (Instance) c.getValue();
                    objects.add(propagator.workspace.findElement(i2.id()));
                }
            }
            else {
                objects.add(c);
            }

        }
        return objects;
    }

    public Set<ConflictEdge> getConflictEdges(){
        Set<ConflictEdge> filteredConflicts = new HashSet<>();
        conflictEdges.forEach(ce -> {
            if(ce.getSource() !=null && ce.getTarget() != null)
                filteredConflicts.add(ce);
        });
        return filteredConflicts;
    }

}
