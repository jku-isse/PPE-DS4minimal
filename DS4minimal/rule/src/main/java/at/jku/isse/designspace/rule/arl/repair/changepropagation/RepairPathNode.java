package at.jku.isse.designspace.rule.arl.repair.changepropagation;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.jgrapht.GraphPath;

/**
 * Defines a node of the Repair Propagation Decision Tree
 */
public class RepairPathNode {
    private int graph;
    private GraphPath<ModelStateVertex, ChangeEdge> repairPath;
    private Set<GraphPath<ModelStateVertex, ChangeEdge>> conflictingPaths;
    private Set<GraphPath<ModelStateVertex, ChangeEdge>> comparedPaths;
    private Set<RepairPathNode> children;
    private RepairPathNode parentPath;
    private int sequenceNumber;


    public RepairPathNode(int graph, int sequenceNumber, GraphPath<ModelStateVertex, ChangeEdge> repairPath) {
        this.graph = graph;
        this.sequenceNumber = sequenceNumber;
        this.repairPath = repairPath;
        children = new HashSet<>();
        conflictingPaths = new HashSet<>();
        comparedPaths = new HashSet<>();
    }


    public int getGraph() {
        return graph;
    }


    public void setGraph(int graph) {
        this.graph = graph;
    }

    public GraphPath<ModelStateVertex, ChangeEdge> getRepairPath() {
        return repairPath;
    }

    public void setRepairPath(GraphPath<ModelStateVertex, ChangeEdge> repairPath) {
        this.repairPath = repairPath;
    }



    private void addConflictPath(GraphPath<ModelStateVertex, ChangeEdge> conflictPath){
        this.conflictingPaths.add(conflictPath);
    }

    public void removeConflictPath(RepairPathNode conflictPath){
        this.conflictingPaths.remove(conflictPath.getRepairPath());
    }

    public Set<RepairPathNode> getChildren() {
        return children;
    }

    public void setChildren(Set<RepairPathNode> children) {
        this.children = children;
    }

    /**
     * Adds a @child into the children set of @this. It alsos add @this as the parent.
     * @param child
     */
    public void addChild(RepairPathNode child){
        this.children.add(child);
        child.setParentPath(this);
    }

    public void removeChild(RepairPathNode child){
        this.children.remove(child);
    }

    public RepairPathNode getParentPath() {
        return parentPath;
    }

    public void setParentPath(RepairPathNode parentPath) {
        this.parentPath = parentPath;
    }

    public boolean isEmpty(){
        return children.isEmpty();
    }

    /**
     * Checks if @this path conflicts with @other.
     * To improve performance the comparison is made only once, then the path compared is stored in memory.
     * @param other a path from a different graph
     * @return true if conflict exists
     */
    public boolean conflictsWith(RepairPathNode other){
        if(this.comparedPaths.contains(other.getRepairPath())) {
            if (this.conflictingPaths.contains(other.getRepairPath()))
                return true;
            else
                return false;
        }else {
            this.comparedPaths.add(other.getRepairPath());
            for (ModelStateVertex state1 : repairPath.getVertexList()) {
                for (ModelStateVertex state2 : other.getRepairPath().getVertexList()) {
                    if (state1.getConflictingVertices().contains(state2)) {
                        this.conflictingPaths.add(other.getRepairPath());
                        return true;
                    }
                }
            }
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepairPathNode that = (RepairPathNode) o;
        return Objects.equals(this.hashCode(), that.hashCode());
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(graph, repairPath, conflictingPaths, children, parentPath);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(graph);
        sb.append(".");
        sb.append(sequenceNumber);
        sb.append("<");
        sb.append(repairPath == null ?  "" : repairPath.toString());
        sb.append(">");
        sb.append(")");
        return sb.toString();
    }

    public String toStringCompact() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(graph);
        sb.append(".");
        sb.append(sequenceNumber);
        sb.append(")");
        return sb.toString();
    }
}
