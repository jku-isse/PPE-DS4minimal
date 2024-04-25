package at.jku.isse.designspace.rule.arl.repair.changepropagation;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ModelStateVertex {
    private ModelState state;
    private  int graphHashCode;
    private Set<ModelStateVertex> conflictingVertices;

    public ModelStateVertex(ModelState state, int graphHashCode) {
        this.state = state;
        this.graphHashCode = graphHashCode;
        this.conflictingVertices = new HashSet<>();
    }

    public boolean isFinal(){
        return getState().getChildren().isEmpty();
    }

    public ModelState getState() {
        return state;
    }

    public String getStateName(){
        return graphHashCode+"."+state.getName().trim();
    }

    public void setState(ModelState state) {
        this.state = state;
    }

    public int getGraphHashCode() {
        return graphHashCode;
    }

    public void setGraphHashCode(int graphHashCode) {
        this.graphHashCode = graphHashCode;
    }

    public void addConflict(ModelStateVertex m){
        this.conflictingVertices.add(m);
    }

    public Set<ModelStateVertex> getConflictingVertices(){
        return this.conflictingVertices;
    }

    public String getConflictsString() {
        StringBuilder sb = new StringBuilder(128);
        for (ModelStateVertex mv : conflictingVertices){
            sb.append(mv.getStateName());
            sb.append(", ");
        }
        return sb.toString();

    }

    @Override
    public String toString() {
        return "State{" +state.toString()+
                ", graph=" + graphHashCode +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ModelStateVertex that = (ModelStateVertex) o;
        return hashCode() == that.hashCode();
    }

    @Override
    public int hashCode() {
        return Objects.hash(state.hashCode(), graphHashCode);
    }

    public boolean isConflict(ModelStateVertex o) {
        if(this.graphHashCode==o.graphHashCode) return false;
        return this.getState().isConflict(o.getState());
    }


}
