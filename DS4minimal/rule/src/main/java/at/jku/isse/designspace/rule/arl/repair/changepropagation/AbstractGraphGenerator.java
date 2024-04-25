package at.jku.isse.designspace.rule.arl.repair.changepropagation;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
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
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.AttributeType;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.graphml.GraphMLExporter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.service.WorkspaceService;
import at.jku.isse.designspace.rule.arl.exception.ChangeExecutionException;
import at.jku.isse.designspace.rule.arl.repair.AbstractRepairAction;
import at.jku.isse.designspace.rule.arl.repair.Repair;
import at.jku.isse.designspace.rule.arl.repair.SideEffect;
import at.jku.isse.designspace.rule.model.ConsistencyRule;
import at.jku.isse.designspace.rule.model.ConsistencyRuleType;

/**
 * @author Luciano
 * Abstract class used for extending graphs generators
 */
public abstract class AbstractGraphGenerator {

    protected List<RepairChangePropagator> propagators;
    protected static Graph<ModelStateVertex, DefaultEdge> mergedModelGraph;
    protected Set<ConsistencyRule> inconsistencies;
    protected static Set<ConsistencyRuleType> consistencyRules;
    protected Set<ConsistencyRuleType> childConsistencyRules;
    protected Workspace workspace;
    protected boolean preservePastChanges;
    protected Set<Instance> instances;
    protected boolean showConflicts;
    protected List<Change> changeHistory;
    protected List<GraphPath<ModelStateVertex, ChangeEdge>> allPaths;
    protected Set<ConflictEdge> conflictEdges;
    protected Set<String> propertiesToSkip;
    protected String ownerId;
    protected long conflictDetectionTime;


    protected RepairPathNode decisionTree;

    public AbstractGraphGenerator(ModelState state, Set<ConsistencyRuleType> crds, Workspace w, boolean preservePastChanges,
                                  boolean showConflicts, Set<String> propertiesToSkip){
        this.consistencyRules = crds;
        this.workspace = w;
        this.propagators = new ArrayList<>();
        this.mergedModelGraph = new SimpleDirectedGraph<>(DefaultEdge.class);
        if(propertiesToSkip ==null)
            this.propertiesToSkip = new HashSet<>();
        else
            this.propertiesToSkip = propertiesToSkip;
        propagators.add(new RepairChangePropagator(state, createChildWorkspace(workspace),childConsistencyRules, true, this.propertiesToSkip));
        this.preservePastChanges = preservePastChanges;
        this.showConflicts = showConflicts;
        this.changeHistory = new ArrayList<>();
    }

    /**
     * Update is called when a new change is found. It creates a new propagator based on the initial state and change and adds it to the propagators list.
     * @param state initial state
     * @param rules consistency rules
     * @param fixPastInconsistencies
     */
    public void updatePropagator(ModelState state, Set<ConsistencyRuleType> rules, boolean fixPastInconsistencies){
        this.consistencyRules = rules;
        propagators.add(new RepairChangePropagator(state, createChildWorkspace(workspace),childConsistencyRules, !fixPastInconsistencies, this.propertiesToSkip));
    };
    protected abstract void generateGraph(boolean keepInconsistentPaths, boolean keepAbstractRepairs, boolean generateMergedGraph, boolean addFolderContent);

    /**
     * Add past changes into a propagator, if preservePastChanges flag is TRUE.
     * @param propagator propagator where changes will be added
     * @return updated list with past changes
     */
    protected List<Change> updateChangeHistory(RepairChangePropagator propagator){
        if(preservePastChanges)
            if (!changeHistory.isEmpty())
                propagator.addChanges(changeHistory);
        Change newChange = propagator.getInitialChange();
        changeHistory.add(newChange);
        for (int i = 0; i < newChange.getSubChanges().size(); i++) {
            Change c = (Change) newChange.getSubChanges().get(i);
            changeHistory.add(newChange);
        }
        return changeHistory;
    }

    /**
     * Creates a child workspace for each propagator. Thus, it graph will be build by exploring repairs in these workspaces.
     * @param parentW parent workspace
     * @return child workspace
     */
    protected Workspace createChildWorkspace(Workspace parentW){
        Workspace childW = WorkspaceService.createWorkspace("childWorkspace", parentW,
                WorkspaceService.ANY_USER, null, false, false);
        childW.update();
        childW.concludeTransaction();
        parentW.concludeTransaction();
        inconsistencies = new HashSet<>();
        childConsistencyRules = new HashSet<>();
        // get inconsistencies from child workspace
        for (ConsistencyRuleType crd: consistencyRules){
            String name = crd.contextInstanceType().name();
            Object element =  childW.findElement(crd.id());
            childConsistencyRules.add((ConsistencyRuleType) element);
            childW.concludeTransaction();
        }
        return childW;
    }





    /**
     * Return shortest path from a initial state to all consistent state
     * @return sequence of repairs
     */
    public HashMap<Integer,Set<List<Repair>>> getShortestPaths(){

        HashMap<Integer,Set<List<Repair>>> repairSequences = new HashMap<>();
        int i = 0;
        for(RepairChangePropagator propagator : propagators){
            repairSequences.put(i,propagator.getShortestPaths());
            i++;
        }
        return repairSequences;
    }

    /**
     * Return shortest path from a initial state to any possible state
     * @param finalVertex
     * @return sequence of repairs
     */
    public Set<List<Repair>> getShortestPath(ModelStateVertex finalVertex) {
        Set<List<Repair>> repairSequences = new HashSet<>();
        if(finalVertex.getState().getRepair() ==null) return repairSequences;
        RepairChangePropagator p = propagators.get(finalVertex.getGraphHashCode());
        return p.getShortestPath(finalVertex.getState());
    }

    /**
     * Return sequences of repairs needed from a initial state to reach a state
     * @param allPaths all paths from initial state to selected state
     * @return sequence of repairs
     */
    public List<List<Repair>> getRepairSequences(List<GraphPath<ModelStateVertex, ChangeEdge>> allPaths) {
        List<List<Repair>> repairSequences = new ArrayList<>();
        if(allPaths == null) return repairSequences;
        for(GraphPath<ModelStateVertex, ChangeEdge> path: allPaths) {
            List<Repair> repairs = new ArrayList();
            for(DefaultEdge e: path.getEdgeList()) {
                if( e instanceof ChangeEdge) {
                    ChangeEdge changeEdge = (ChangeEdge) e;
                    if (changeEdge.getChange() instanceof Repair)
                        repairs.add((Repair) changeEdge.getChange());
                }
            }
            repairSequences.add(repairs);
        }
        repairSequences.sort(Comparator.comparing(List::size));  // order so smallest sequences are shown first
        return repairSequences;
    }

    public List<GraphPath<ModelStateVertex, ChangeEdge>> getAllPaths(){
        return allPaths;
    }

    /**
     * Returns all paths from initial state to selected state
     * @param finalVertex selected state
     * @return allPaths all paths from initial state to selected state
     */
    public List<GraphPath<ModelStateVertex, ChangeEdge>> getAllPaths(ModelStateVertex finalVertex) {
        if(finalVertex.getState().getRepair() == null) return null;
        return this.allPaths.stream().filter(path -> path.getEndVertex().equals(finalVertex)).collect(Collectors.toList());
    }

    /**
     * Returns a list of all node ids for each sequence from the initial node until all finalNodes
     * @param graphHashCode
     * @return
     */
    public List<GraphPath<ModelStateVertex, ChangeEdge>> getAllPaths(int graphHashCode) {
        return this.allPaths.stream().filter(path -> path.getStartVertex().getGraphHashCode()==graphHashCode).collect(Collectors.toList());
    }

    /**
     * Generates a list of all paths from the all initial nodes until all finalNodes
     * @return
     */
    public void generateAllPaths() {
        AllDirectedPaths pathsAlgorithm = new AllDirectedPaths(mergedModelGraph);
        Set<ModelStateVertex> allFinalStates = mergedModelGraph.vertexSet().stream().filter(modelStateVertex -> modelStateVertex.isFinal()).collect(Collectors.toSet());
        Set<ModelStateVertex> allInitialStates = mergedModelGraph.vertexSet().stream().filter(modelStateVertex -> modelStateVertex.getState().isInitial()).collect(Collectors.toSet());
        List<GraphPath<ModelStateVertex, ChangeEdge>> allPaths = new ArrayList<>();
        for(ModelStateVertex initialState : allInitialStates) {
            for(ModelStateVertex finalState : allFinalStates) {
                if(initialState.getGraphHashCode()==finalState.getGraphHashCode()) {
                    List<GraphPath<ModelStateVertex, ChangeEdge>> newPaths = pathsAlgorithm.getAllPaths(initialState, finalState, false, 100);
                    if (newPaths != null)
                        allPaths.addAll(newPaths);
                }
            }
        }
        this.allPaths = allPaths;
    }



    /**
     * Returns a list of all nodes for each sequence
     * @param allPaths
     * @return
     */
    public List<List<ModelState>> getSequenceNodes(List<GraphPath<ModelStateVertex, ChangeEdge>> allPaths) {
        List<List<ModelState>> sequenceNodes = new ArrayList<>();
        if(allPaths == null) return sequenceNodes;
        for(GraphPath<ModelStateVertex, ChangeEdge> path: allPaths) {
            List<ModelState> nodesIds = new ArrayList();
            for(ModelStateVertex v: path.getVertexList()) {
                nodesIds.add(v.getState());
            }
            sequenceNodes.add(nodesIds);
        }
        sequenceNodes.sort(Comparator.comparing(List::size));  // order so the smallest sequences are shown first
        return sequenceNodes;
    }

    /**
     * Returns a list of all node ids for each sequence
     * @param nodes
     * @param graphHashCode
     * @return
     */
    public List<List<Integer>> getSequenceNodesIds(List<List<ModelState>> nodes, int graphHashCode) {
        List<List<Integer>> sequenceNodes = new ArrayList<>();
        for (List<ModelState> nodeList : nodes){
            List<Integer> nodesIds = new ArrayList();
            for(ModelState v: nodeList) {
                nodesIds.add(Objects.hash(v.hashCode(), graphHashCode));
            }
            sequenceNodes.add(nodesIds);
        }
        return sequenceNodes;
    }




    /**
     * Returns side effects count for the sequences
     * @param repairSequence
     * @return
     */
    public List<Integer> getSideEffectsCount(List<Repair> repairSequence) {
        List<Integer> sideEffectsCount = new ArrayList<>();
        if(repairSequence == null) return sideEffectsCount;
        int positiveEffectsCount = 0;
        int negativeEffectsCount = 0;
        for (Repair r: repairSequence){
            positiveEffectsCount += r.getSideEffects(SideEffect.Type.POSITIVE).size();
            negativeEffectsCount += r.getSideEffects(SideEffect.Type.NEGATIVE).size();
        }
        sideEffectsCount.add(negativeEffectsCount);
        sideEffectsCount.add(positiveEffectsCount);
        return sideEffectsCount;
    }


    /**
     * Generates a string from the sequence of repairs.
     * @param repairSequences sequences
     * @return string of the possible sequences of repair to reach that node
     */
    private String getRepairSequencesString(List<List<Repair>> repairSequences) {
        StringBuilder sb = new StringBuilder(128);
        int i =1;
        for (List<Repair> repairList: repairSequences){
            sb.append("<Seq:"+i);
            sb.append(", size:"+repairList.size());
            sb.append(repairList);
            sb.append(">");
            sb.append(System.lineSeparator());
            sb.append(System.lineSeparator());
            sb.append(System.lineSeparator());
            i++;
        }
        return sb.toString();
    }





    private Set<ModelState> getNodesByLevel(Graph modelGraph, int level){
        Set<ModelState> levelNodes = new HashSet<>();
        for(Object state : modelGraph.vertexSet()){
            ModelState modelState = (ModelState) state;
            if (modelState.getLevel()==level)
                levelNodes.add(modelState);
        }
        return levelNodes;
    }

    protected void updateChildOrder(){
        for (RepairChangePropagator p: propagators){
            for (int i = 0; i < 15; i++) {
                int j = 0;
                for(ModelState modelState : getNodesByLevel(p.modelGraph,i)) {
                    modelState.setXOrder(j);
                    j++;
                }
            }
        }
    }



    /**
     * Executes the selected repair sequence
     * @param repairSequence
     */
    public void executeRepairSequence(List<Repair> repairSequence, int propagator, boolean commit){
        for (Repair r: repairSequence){
            try {
                r.execute();
                if(r.executed()){
                    Set<AbstractRepairAction> actions = (Set<AbstractRepairAction>) r.getRepairActions();
                    for (AbstractRepairAction action :actions){
                        Change c = new Change(action.getElement(),action.getProperty(),action.getOperator(),action.getValue(),
                                Change.ChangeType.POSITIVE);
                        changeHistory.add(c);
                    }

                }
            } catch (ChangeExecutionException e) {
                e.printStackTrace();
            }
        }
        propagators.get(propagator).getWorkspace().concludeTransaction();
        if(commit)
            propagators.get(propagator).getWorkspace().commit();


    }



    /**
     * Generates a repair propagation decision tree based on the analysis of the repair paths.
     */
    public void generateDecisionTree() {
        decisionTree = new RepairPathNode(-1,0,null);
        for (int i = 0; i < propagators.size(); i++) {
            List<GraphPath<ModelStateVertex, ChangeEdge>> propagatorsPath = getAllPaths(i);
            for (int j = 0; j < propagatorsPath.size(); j++) {
                GraphPath<ModelStateVertex, ChangeEdge> path = propagatorsPath.get(j);
                RepairPathNode pathNode = new RepairPathNode(i,j+1,path);
                decisionTree.addChild(pathNode);
            }
        }
        generateDecisionTree(decisionTree); // recursive call to generate child nodes
    }

    /**
     * Generates a repair propagation decision tree from a given parent node
     */
    private void generateDecisionTree(RepairPathNode parentNode){
        for (RepairPathNode childNode1 : parentNode.getChildren()) {
            for (RepairPathNode childNode2 : parentNode.getChildren()) {
                if(childNode1.getGraph() != childNode2.getGraph())  // if the graph is the same, then the nodes should not be related
                    if(!childNode1.conflictsWith(childNode2)) {
                        RepairPathNode newNode = new RepairPathNode(childNode2.getGraph(), childNode2.getSequenceNumber(),childNode2.getRepairPath());
                        childNode1.addChild(newNode);
                    }
            }
            generateDecisionTree(childNode1);
        }
    }

    /**
     * Generates json file representing the graph to be used by D3.js
     * @return json file representing the graph
     */
    public String exportAsJSON() {
        try {
            JSONArray nodesList = new JSONArray();
            JSONArray edgesList = new JSONArray();
            for (ModelStateVertex node : mergedModelGraph.vertexSet()) {
                JSONObject nodeObject = new JSONObject();
                nodeObject.put("id", node.hashCode());
                nodeObject.put("name", node.getStateName());
                nodeObject.put("label", node.toString());
                nodeObject.put("numberOfIncon", String.valueOf(node.getState().getNumberOfInconsistencies()));
                nodeObject.put("inconsistencies",node.getState().getInconsistenciesString());
                nodeObject.put("deltas", node.getState().getOperations());
                nodeObject.put("initialChange", node.getState().getUserChangeString());
                List allPaths;
                if(node.getState().isInitial())
                    allPaths = getAllPaths(node.getGraphHashCode());
                else
                    allPaths = getAllPaths(node);
                List<List<Repair>> repairSequences = getRepairSequences(allPaths);
                nodeObject.put("seqSize", repairSequences.size());
                List<List<ModelState>> nodeSequences = getSequenceNodes(allPaths);
                nodeObject.put("repairSequences", getSequenceNodesIds(nodeSequences,node.getGraphHashCode()));
                nodeObject.put("repairs", getRepairSequencesString(repairSequences));
                nodeObject.put("repairAlt", node.getState().getRepairAlternativesString());
                nodeObject.put("level", node.getState().getLevel());
                nodeObject.put("order", node.getState().getXOrder());
                nodeObject.put("isAbstract", node.getState().isAbstract());
                nodeObject.put("isConsistent", node.getState().isConsistent());
                nodeObject.put("isInitial", node.getState().isInitial());
                nodeObject.put("graph", node.getGraphHashCode());
                nodeObject.put("conflicts", node.getConflictsString());
                nodesList.put(nodeObject);

            }
            for (DefaultEdge edge : mergedModelGraph.edgeSet()) {
                if(!(edge instanceof ConflictEdge) || showConflicts){
                    JSONObject edgeObject = new JSONObject();
                    edgeObject.put("source", mergedModelGraph.getEdgeSource(edge).hashCode());
                    edgeObject.put("target", mergedModelGraph.getEdgeTarget(edge).hashCode());
                    edgeObject.put("label", edge.toString());
                    Set positiveSideEffects = new HashSet();
                    Set negativeSideEffects = new HashSet();
                    if(edge instanceof ChangeEdge) {
                        positiveSideEffects = ((ChangeEdge<?>) edge).getPositiveSideEffects();
                        negativeSideEffects = ((ChangeEdge<?>) edge).getNegativeSideEffects();
                    }
                    edgeObject.put("pSideEffects",positiveSideEffects.size());
                    edgeObject.put("nSideEffects",negativeSideEffects.size());
                    boolean isConflict = false;
                    boolean isAbstract = false;
                    if (edge instanceof ConflictEdge)
                        isConflict = true;
                    else
                        isAbstract = ((ChangeEdge)edge).isAbstract();
                    edgeObject.put("isConflict", isConflict);
                    edgeObject.put("isAbstract", isAbstract);

                    edgesList.put(edgeObject);
                }

            }
            JSONObject mainObject = new JSONObject();
            mainObject.put("nodes", nodesList);
            mainObject.put("links", edgesList);
            return mainObject.toString(4);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "ERROR! JSON file not generated!";
    }


    /**
     * TODO test it
     * Recursive call of the decision tree exporting
     * @param parent
     * @return
     * @throws JSONException
     */
    public JSONArray exportDTAsJSON(RepairPathNode parent) throws JSONException {
        JSONArray nodesList = new JSONArray();
        for (RepairPathNode child : parent.getChildren()) {
            JSONObject nodeObject = new JSONObject();
            nodeObject.put("id", child.toStringCompact());
            nodeObject.put("parent", parent.toStringCompact());
            nodeObject.put("children", exportDTAsJSON(child));
            nodesList.put(nodeObject);
        }
        return nodesList;
    }
    /**
     * TODO test it with D3
     * Generates a String representation of the decision tree
     * @return
     */
    public String exportDTAsJSON() {
        try {
            JSONArray nodesList = new JSONArray();
            JSONObject rootObject = new JSONObject();
            rootObject.put("id", decisionTree.toStringCompact());
            rootObject.put("children", exportDTAsJSON(decisionTree));
            nodesList.put(rootObject);

            JSONObject mainObject = new JSONObject();
            mainObject.put("nodes", nodesList);
            return mainObject.toString(4);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "ERROR! JSON file not generated!";
    }

    /**
     * Export every individual graph from each change;
     * Then it exports a graph with all other graphs and conflicting states
     * @param graphName list of graph names
     * @return true if no exception was thrown during export
     */
    public boolean exportGraphs(String graphName, boolean exportMerged){
        int i = 0;
        for (RepairChangePropagator p : propagators){
            if(!p.exportGraph(graphName,i))
                return false;
            i++;
        }
        if(exportMerged)
            return exportGraph(graphName+"Merged");
        return true;
    }

    /**
     * Export modelGraph into `DesignSpace-4.0\main\graphs\` folder. The graphML file can be opened with yEd.
     * @param graphName name of the graphMLFile
     * @return true if no exception was thrown during export
     */
    protected boolean exportGraph(String graphName) {
        GraphMLExporter<ModelStateVertex, DefaultEdge> exporter = new GraphMLExporter<>();
        exporter.registerAttribute("label", GraphMLExporter.AttributeCategory.NODE, AttributeType.STRING);
        exporter.registerAttribute("isConsistent", GraphMLExporter.AttributeCategory.NODE, AttributeType.BOOLEAN);
        exporter.registerAttribute("isInitial", GraphMLExporter.AttributeCategory.NODE, AttributeType.BOOLEAN);
        exporter.registerAttribute("edgeLabel", GraphMLExporter.AttributeCategory.EDGE, AttributeType.STRING);
        exporter.registerAttribute("isConflict", GraphMLExporter.AttributeCategory.EDGE, AttributeType.BOOLEAN);
        exporter.setVertexAttributeProvider((v) -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            map.put("label", DefaultAttribute.createAttribute(v.toString()));
            map.put("isConsistent", DefaultAttribute.createAttribute(v.getState().isConsistent()));
            map.put("isInitial", DefaultAttribute.createAttribute(v.getState().getRepair()==null));
            return map;
        });
        exporter.setEdgeAttributeProvider((e) -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            map.put("edgeLabel", DefaultAttribute.createAttribute(e.toString()));
            map.put("isConflict", DefaultAttribute.createAttribute(e instanceof ConflictEdge));
            return map;
        });
        try {
            exporter.exportGraph(mergedModelGraph, new File("graphs/"+graphName+".graphml"));
            return true;
        }catch (Exception e)    {
            e.printStackTrace();
            System.out.println("ERROR! Graph not exported!");
            return false;
        }
    }

    public Graph getGraph(){
        return mergedModelGraph;
    }

    public List<RepairChangePropagator> getPropagators(){
        return propagators;
    }
    public boolean isGraphEmpty(){
        return mergedModelGraph.vertexSet().isEmpty() && mergedModelGraph.edgeSet().isEmpty();
    }
    public void commitChildWorkspace(int propagator){propagators.get(propagator).getWorkspace().commit();}
    public List<Change> getChangeHistory() {
        return changeHistory;
    }
    public RepairPathNode getDecisionTree() {
        return decisionTree;
    }
    public long getConflictDetectionTime() {return conflictDetectionTime > 0 ? conflictDetectionTime:1;}
    public void setChangeHistory(List<Change> changeHistory) {
        this.changeHistory = changeHistory;
    }
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}
