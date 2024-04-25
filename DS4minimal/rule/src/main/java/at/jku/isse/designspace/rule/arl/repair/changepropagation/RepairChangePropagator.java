package at.jku.isse.designspace.rule.arl.repair.changepropagation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.AttributeType;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.graphml.GraphMLExporter;

import at.jku.isse.designspace.core.model.Id;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.service.WorkspaceService;
import at.jku.isse.designspace.rule.arl.repair.AbstractRepair;
import at.jku.isse.designspace.rule.arl.repair.AbstractRepairAction;
import at.jku.isse.designspace.rule.arl.repair.ConsistencyRepairAction;
import at.jku.isse.designspace.rule.arl.repair.Repair;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.arl.repair.RepairSingleValueOption;
import at.jku.isse.designspace.rule.model.ConsistencyRule;
import at.jku.isse.designspace.rule.model.ConsistencyRuleType;
import at.jku.isse.designspace.rule.model.ReservedNames;
import at.jku.isse.designspace.rule.service.RuleService;

/**
 * @author Luciano
 * Propagator for propagating repairs.
 */
@SuppressWarnings({"rawtypes", "unchecked", "SuspiciousMethodCalls"})
public class RepairChangePropagator<ET> {
    protected Graph<ModelState, ChangeEdge> modelGraph;
    protected Set<ConsistencyRule> inconsistencies;
    protected Set<ConsistencyRule> otherInconsistencies;
    protected Set<ConsistencyRuleType> consistencyRules;
    protected Change initialChange;
    protected Set<Change> changes;
    protected ModelState initialState;
    protected ModelState changedState;
    protected Workspace workspace;
    protected boolean propagated = false;
    protected boolean keepInconsistentPaths = false;
    protected boolean keepAbstractRepairs = false;
    protected boolean removePastIncon;
    protected Set<String> propertiesToSkip;
    protected long propagationTime;
    protected Set<ConflictEdge> conflictEdges;
    private List<GraphPath<ModelState, ChangeEdge>> allPaths;
    private HashMap<Id,Set<Repair>> currentRepairs;
    private String ownerId;
    private static final int maxLevel = 10;


    public RepairChangePropagator(ModelState state, Workspace workspace, Set<ConsistencyRuleType> rules, boolean removePastIncon, Set<String> propertiesToSkip) {
        this.modelGraph = new SimpleDirectedGraph<>(ChangeEdge.class);
        this.conflictEdges = new HashSet<>();
        this.inconsistencies = new HashSet<>();
        this.changes = new HashSet<>();
        this.initialChange = state.getUserChange();
        if(this.initialChange !=null)
            this.changes.add(this.initialChange);
        this.initialState = new ModelState(null,new HashSet<>(), null, true);
        this.changedState = state;
        this.removePastIncon = removePastIncon;
        if(this.removePastIncon)
            this.changedState.removePastInconsistencies();
        this.initialState.addChild(changedState);
        this.changedState.addParent(initialState);
        modelGraph.addVertex(initialState);
        ChangeEdge firstEdge = new ChangeEdge<>(initialChange, false);
        firstEdge.saveSideEffects(new HashSet<>(), changedState.getInconsistencies());
        addModelState(initialState,changedState,firstEdge);
        this.workspace = workspace;
        this.consistencyRules = rules;
        for (ConsistencyRuleType cr: rules){ // evaluate rules in child workspace
            this.inconsistencies.addAll(cr.consistencyRuleEvaluations().stream().
                    filter(consistencyRule -> !consistencyRule.isConsistent() && !consistencyRule.hasEvaluationError()).collect(Collectors.toSet()));
        }
        this.propertiesToSkip = propertiesToSkip;
        this.otherInconsistencies = new HashSet<>();
        this.currentRepairs = new HashMap<>();
        workspace.concludeTransaction();

    }


    /**
     * Remove ignoredInconsistencies from the modelState. Ignored Inconsistencies are those not created by user changes.
     */
    public void removeIgnoredInconsistencies(Set<ConsistencyRule> newInconsistencies){
        if(changedState.getIgnoredInconsistencies() == null)
            return;
        for(ConsistencyRule newCre : new HashSet<>(newInconsistencies)){
            for(ConsistencyRule cre : changedState.getIgnoredInconsistencies()) {
                Instance i1 = (Instance) cre.getPropertyAsValue(ReservedNames.CONTEXT_INSTANCE);
                Instance i2 = (Instance) newCre.getPropertyAsValue(ReservedNames.CONTEXT_INSTANCE);
                if(cre.name().equals(newCre.name()) &&
                        cre.consistencyRuleDefinition().rule().equals(newCre.consistencyRuleDefinition().rule()) &&
                        i1.equals(i2)) {
                    newInconsistencies.remove(newCre);
                    otherInconsistencies.add(newCre);
                }
            }
        }
    }

    public void addModelState(ModelState originModelState, ModelState endModelState, ChangeEdge edge){
        try {
            modelGraph.addVertex(endModelState);
            modelGraph.addEdge(originModelState,endModelState,edge);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Checks if repair conflicts with user changes.
     * @param r repair to be checked
     * @return true if repair is undoing initial change
     */
    private boolean checkConflictWithChange(Repair r){
        Set<AbstractRepairAction> repairActions = (Set<AbstractRepairAction>) r.getRepairActions();
        for (Change c : changes) {
            AbstractRepairAction changeAction = new ConsistencyRepairAction(null,c.getProperty(),
                    (Instance) c.getElement(),new RepairSingleValueOption(c.getOperator(),c.getValue()), null);//TODO: is null safe here?
            for (AbstractRepairAction ar : repairActions){
                if(changeAction.checkConflict(ar)) return true;
            }
        }
        return false;
    }

    /**
     * Checks if repair conflicts with previous repairs explored in parents model states).
     * @param r repair to be checked
     * @param previousRepairs previousRepairs          
     * @return true if repair is undoing previous repairs
     */
    private boolean checkConflictWithRepairs(Repair r,List<Repair> previousRepairs){
        if (previousRepairs == null) return false;
        Set<AbstractRepairAction> repairActions = (Set<AbstractRepairAction>) r.getRepairActions();
        for (Repair repair : previousRepairs) {
            Set<AbstractRepairAction> previousRepairActions = (Set<AbstractRepairAction>) repair.getRepairActions();
            for (AbstractRepairAction previousAction : previousRepairActions) {
                for (AbstractRepairAction ar : repairActions){
                    if(previousAction.checkConflict(ar))
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if the repair contains repair actions that modifies a property that should be skipped.
     * @param r repair
     */
    private boolean isPropertySkipped(Repair r){
        if(propertiesToSkip==null) return false;
        Set<AbstractRepairAction> repairActions = (Set<AbstractRepairAction>) r.getRepairActions();
        for (AbstractRepairAction repairAction : repairActions) {
            if(propertiesToSkip.contains(repairAction.getProperty()))
                return true;
        }
        return false;
    }


    public void propagateChange(Set<Object> objects) {
        if(this.removePastIncon)
            removeIgnoredInconsistencies(inconsistencies);
        long startPropagationTime = System.currentTimeMillis() ;
        propagateChange(changedState, inconsistencies,objects);
        long endPropagationTime = System.currentTimeMillis();
        this.propagationTime = endPropagationTime - startPropagationTime;
        propagated = true;
    }



    public boolean isPropagated() {
        return propagated;
    }

    /**
     * Propagate a single change that generates inconsistencies. Repairs are generated and executed for fixing all
     * original and new inconsistencies. Then, repairs are undone so other repairs can be explored.
     * @param state initial model state
     * @param objects set of objects used for getting values to generate concrete repairs
     */
    private void propagateChange(ModelState state, Set<ConsistencyRule> inconsistencies, Set<Object> objects) {
        Set<Repair> repairs = new HashSet<>();
        if(maxLevel > 0 && state.getLevel() > maxLevel) return;
        if(RuleService.currentWorkspace != workspace) RuleService.currentWorkspace = workspace;

        for (ConsistencyRule cre : new HashSet<>(inconsistencies)) {
            RuleService.evaluator.evaluate(cre);
            RepairNode repairTree = RuleService.repairTree(cre);
            if(repairTree!=null) {
                if(!currentRepairs.containsKey(cre.id())){ // saving repairs in memory to avoid generation for every model state
                    currentRepairs.put(cre.id(),repairTree.getConcreteRepairsFilteredByOwner(ownerId,objects, false, true));
                }
                repairs.addAll(currentRepairs.get(cre.id()));
            }
            else
                inconsistencies.remove(cre);
        }
        List<Repair> repairList = new ArrayList<>(repairs);
        Collections.sort(repairList); // based on toString from ConsistencyRepair

        for (Repair repair : repairList) {
            if(!checkConflictWithChange(repair) && !checkConflictWithRepairs(repair,state.getPreviousRepairs()) && !isPropertySkipped(repair)) {
                if(repair.isAbstract() && keepAbstractRepairs) { //if repair is abstract it not possible to propagate, thus it creates the new state only
                    ChangeEdge changeEdge = new ChangeEdge(repair, true);
                    Set<ConsistencyRule> newInconsistencies = new HashSet<>(inconsistencies);
                    newInconsistencies.remove(repair.getInconsistency());
                    if(this.removePastIncon)removeIgnoredInconsistencies(newInconsistencies); // removes ignored inconsistencies
                    if(keepInconsistentPaths || newInconsistencies.isEmpty()) {
                        ModelState newState = new ModelState(repair, newInconsistencies, null, false);
                        newState.addPreviousRepairs(state.getPreviousRepairs());
                        newState.setOperations(new ArrayList<>());
                        newState.addParent(state);
                        state.addChild(newState);
                        changeEdge.saveSideEffects(inconsistencies,newInconsistencies);
                        if(modelGraph.containsVertex(newState))
                            modelGraph.addEdge(state, newState, changeEdge);
                        else {
                            addModelState(state,newState,changeEdge);
                        }
                    }
                }else if(repair.isExecutable()){
                    try {
                        repair.execute();
                        workspace.concludeTransaction();
                        ConsistencyRule cre = (ConsistencyRule) repair.getInconsistency();
                        if(cre.isConsistent()) {
                            Set<ConsistencyRule> newInconsistencies = new HashSet<>();
                            for (ConsistencyRuleType crd : consistencyRules) {
                                newInconsistencies.addAll(crd.consistencyRuleEvaluations().stream()
                                        .filter(r -> !r.isConsistent() && !r.hasEvaluationError()).collect(Collectors.toSet()));  // get inconsistencies not repaired
                            }
                            if(this.removePastIncon)removeIgnoredInconsistencies(newInconsistencies); // removes ignored inconsistencies
                            ChangeEdge changeEdge = new ChangeEdge(repair, false);
                            changeEdge.saveSideEffects(inconsistencies,newInconsistencies);
                            ModelState newState = new ModelState(repair, newInconsistencies, null, false);
                            newState.addPreviousRepairs(state.getPreviousRepairs());
                            newState.addParent(state);
                            state.addChild(newState);
                            this.inconsistencies.addAll(newInconsistencies);
                            workspace.concludeTransaction();
                            if(modelGraph.containsVertex(newState))
                                modelGraph.addEdge(state, newState, changeEdge);
                            else {
                                addModelState(state,newState,changeEdge);
                                if(!newState.isConsistent())
                                    propagateChange(newState,newInconsistencies,objects);
                            }
                        }
                        repair.undo();
                        workspace.concludeTransaction();
                        for (ConsistencyRule cr: inconsistencies) {
                            if (cr.isDeleted()) {
                                cr.restore();  // restore CREs deleted for undoing repair. Required when repair operator is #REMOVE
                                for (ConsistencyRuleType crd : consistencyRules) {
                                    if (Objects.equals(crd.name(), cr.ruleDefinition().name())) {
                                        crd.getPropertyAsSet(at.jku.isse.designspace.core.model.ReservedNames.INSTANCES).add(cr);
                                        crd.getPropertyAsMap(ReservedNames.RULE_EVALUATIONS_BY_CONTEXT_INSTANCE).put(cr.id().toString(), cr);
                                    }
                                    workspace.concludeTransaction();
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        boolean inconsistentFinalState = state.getChildren().isEmpty() && !state.isConsistent();
        if(inconsistentFinalState && !keepInconsistentPaths) {
            modelGraph.removeVertex(state);
            state.removeFromParents();
        }

    }

    /**
     * Return shortest path from the initial state to consistent states
     * @return sequence of repairs
     */
    public Set<List<Repair>> getShortestPaths() {
        Set<List<Repair>> repairSequences = new HashSet<>();
        for(ModelState state : modelGraph.vertexSet().stream().filter(o -> o.isConsistent() && o!=initialState).collect(Collectors.toSet())){
            GraphPath<ModelState, ChangeEdge> shortestPath = DijkstraShortestPath.findPathBetween(modelGraph,initialState,state);
            List<Repair> repairs = new ArrayList();
            if(shortestPath != null) {
                for(ChangeEdge e: shortestPath.getEdgeList()) {
                    if(e.getChange() instanceof Repair)
                        repairs.add((Repair)e.getChange());
                }
                repairSequences.add(repairs);
            }

        }
        return repairSequences;
    }

    /**
     * Return shortest path from a initial state to any possible state
     * @return sequence of repairs
     */
    public Set<List<Repair>> getShortestPath(ModelState finalState) {
        Set<List<Repair>> repairSequences = new HashSet<>();
        if(finalState.getRepair() ==null) return repairSequences;
        GraphPath<ModelState, ChangeEdge> shortestPath = DijkstraShortestPath.findPathBetween(modelGraph,initialState,finalState);
        List<Repair> repairs = new ArrayList();
        if(shortestPath != null) {
            for(DefaultEdge e: shortestPath.getEdgeList()) {
                ChangeEdge changeEdge = (ChangeEdge) e;
                if(changeEdge.getChange() instanceof Repair)
                    repairs.add((Repair)changeEdge.getChange());

            }
            repairSequences.add(repairs);
        }

        return repairSequences;
    }

    /**
     * Find conflicts considering the history of changes
     * @param changeHistory
     */
    public void findConflicts(List<Change> changeHistory){
        for(ModelState state1: modelGraph.vertexSet().stream().filter(modelState -> modelState.getRepair()!=null).collect(Collectors.toSet())){
            for (Change c: changeHistory){
                if(!c.equals(this.getInitialChange())){
                    if(state1.compareRepairAndChange(c)){
                        state1.addConflict(new ModelState(null,new HashSet<>(),null,false));
                        String conflictString = state1 + " conflicts with " + c;
                        ConflictEdge ce = new ConflictEdge(conflictString, state1.hashCode(), c.hashCode());
                        conflictEdges.add(ce);
                    }
                }
            }
        }
    }

    /**
     * Find conflicts considering the repairs
     * @param propagator2
     * @param considerRepairs
     */
    public void findConflicts(RepairChangePropagator propagator2, boolean considerRepairs){
        for(ModelState state1: modelGraph.vertexSet()){
            if(considerRepairs) {
                Graph<ModelState, ChangeEdge> otherGraph = propagator2.getModelGraph();
                for (ModelState state2 : otherGraph.vertexSet().stream().collect(Collectors.toSet())) {
                    if (state1.isConflict(state2)) {
                        state1.addConflict(state2);
                        String conflictString = state1 + " conflicts with " + state2;
                        ConflictEdge ce = new ConflictEdge(conflictString, state1.hashCode(), state2.hashCode());
                        conflictEdges.add(ce);
                    }

                }
            }
            ModelState state2 = propagator2.changedState;
            if (state1.isConflict(state2)) {
                state1.addConflict(state2);
                String conflictString = state1 + " conflicts with " + state2;
                ConflictEdge ce = new ConflictEdge(conflictString, state1.hashCode(), state2.hashCode());
                conflictEdges.add(ce);
            }

        }
    }


    /**
     * Generates a list of all paths from the all initial nodes until all finalNodes
     * @return
     */
    public void generateAllPaths() {
        AllDirectedPaths pathsAlgorithm = new AllDirectedPaths(modelGraph);
        Set<ModelState> allFinalStates = modelGraph.vertexSet().stream().filter(modelState -> modelState.isFinal()).collect(Collectors.toSet());
        Set<ModelState> allInitialStates = modelGraph.vertexSet().stream().filter(modelStateVertex -> modelStateVertex.isInitial()).collect(Collectors.toSet());
        List<GraphPath<ModelState, ChangeEdge>> allPaths = new ArrayList<>();
        for(ModelState initialState : allInitialStates) {
            for(ModelState finalState : allFinalStates) {
                List<GraphPath<ModelState, ChangeEdge>> newPaths = pathsAlgorithm.getAllPaths(initialState, finalState,
                        false, 100);
                if (newPaths != null)
                    allPaths.addAll(newPaths);

            }
        }
        this.allPaths = allPaths;
    }

    public int countFilteredRepairs(){
        return (int) modelGraph.vertexSet().stream().filter(modelState -> !modelState.getConflictingVertices().isEmpty()
                && modelState.getRepair() != null).count();
    }

    public List<ModelState> getConsistentModelStates() {
        return modelGraph.vertexSet()
                .stream()
                .filter(modelState -> modelState.isConsistent() && modelState.isFinal() && !modelState.isAbstract())
                .collect(Collectors.toList());
    }

    public List<ModelState> getInconsistentModelStates() {
        return modelGraph.vertexSet()
                .stream()
                .filter(modelState -> !modelState.isConsistent())
                .collect(Collectors.toList());
    }

    public List<ModelState> getDeadEnds() {
        return modelGraph.vertexSet()
                .stream()
                .filter(modelState -> !modelState.isConsistent() && modelState.isFinal())
                .collect(Collectors.toList());
    }

    public List<ModelState> getAbstractModelStates() {
        return modelGraph.vertexSet()
                .stream()
                .filter(modelState -> modelState.isAbstract())
                .collect(Collectors.toList());
    }

    public List<ModelState> getAllModelStates() {
        return modelGraph.vertexSet()
                .stream().filter(modelState -> !modelState.getParents().isEmpty())
                .collect(Collectors.toList());
    }

    public List<ChangeEdge> getAllRepairsEdges() {
        return modelGraph.edgeSet()
                .stream().filter(changeEdge -> changeEdge.getChange() instanceof Repair)
                .collect(Collectors.toList());
    }

    public List<AbstractRepair> getAllRepairs(){
        List<AbstractRepair> repairs = new ArrayList<>();
        for (ChangeEdge edge : getAllRepairsEdges()){
            repairs.add((AbstractRepair) edge.getChange());
        }
        return repairs;
    }
    public Graph<ModelState, ChangeEdge> getModelGraph() {return modelGraph;}

    /**
     * destroy objects to reduce memory consumption
     */
    public void destroyGraph() {
        this.modelGraph = null;
        WorkspaceService.deleteWorkspace(workspace);
    }
    public boolean addChanges(List<Change> changes) {
        return this.changes.addAll(changes);
    }
    public void addInitialChange(Change change) {
        this.initialChange = change;
        this.changes.add(initialChange);
    }
    public boolean isGraphEmpty(){
        return modelGraph.vertexSet().isEmpty() && modelGraph.edgeSet().isEmpty();
    }
    public Change getInitialChange() {return initialChange;}
    public ModelState getInitialState() {return initialState;}
    public Set<ConsistencyRuleType> getConsistencyRules(){return consistencyRules;}
    public void  setConsistencyRules(Set<ConsistencyRuleType> crds){consistencyRules = crds;}
    public Workspace getWorkspace() {return workspace;}
    public long getPropagationTime() { return propagationTime > 0 ? propagationTime:1;}
    public int getInconsistencies(){return inconsistencies.size();}
    public Set<ConflictEdge> getConflictEdges() {return conflictEdges;}

    public List<GraphPath<ModelState, ChangeEdge>> getAllPaths() {
        if(allPaths==null || allPaths.isEmpty())
            generateAllPaths();
        return allPaths;
    }

    public int getInconsistenciesCount() {
        return inconsistencies.size();
    }

    /**
     * Export modelGraph into `DesignSpace-4.0\main\graphs\` folder. The graphML file can be opened with yEd.
     * @param graphName name of the graphMLFile
     * @return true if no exception was thrown during export
     */
    public boolean exportGraph(String graphName, int i){
        GraphMLExporter<ModelState, ChangeEdge> exporter = new GraphMLExporter<>();
        exporter.registerAttribute("label", GraphMLExporter.AttributeCategory.NODE, AttributeType.STRING);
        exporter.registerAttribute("isConsistent", GraphMLExporter.AttributeCategory.NODE, AttributeType.BOOLEAN);
        exporter.registerAttribute("isAbstract", GraphMLExporter.AttributeCategory.NODE, AttributeType.BOOLEAN);
        exporter.registerAttribute("isInitial", GraphMLExporter.AttributeCategory.NODE, AttributeType.BOOLEAN);
        exporter.registerAttribute("numberOfConflicts", GraphMLExporter.AttributeCategory.NODE, AttributeType.INT);
        exporter.registerAttribute("repair", GraphMLExporter.AttributeCategory.EDGE, AttributeType.STRING);
        exporter.setVertexAttributeProvider((v) -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            map.put("label", DefaultAttribute.createAttribute(v.toString()));
            map.put("isConsistent", DefaultAttribute.createAttribute(v.isConsistent()));
            map.put("isAbstract", DefaultAttribute.createAttribute(v.isAbstract()));
            map.put("numberOfConflicts", DefaultAttribute.createAttribute(v.getConflictingVertices().size()));
            map.put("isInitial", DefaultAttribute.createAttribute(v.getRepair()==null));
            return map;
        });
        exporter.setEdgeAttributeProvider((e) -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            map.put("repair", DefaultAttribute.createAttribute(e.toString()));
            return map;
        });
        try {
            exporter.exportGraph(modelGraph, new File("graphs/"+graphName+i+".graphml"));
            return true;
        }catch (Exception e)    {
            e.printStackTrace();
            System.out.println("ERROR! Graph not exported!");
            return false;
        }
    }

    public int getPositiveSideEffects() {
        return (int) otherInconsistencies.stream().filter(i -> i.isConsistent()).count();
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}
