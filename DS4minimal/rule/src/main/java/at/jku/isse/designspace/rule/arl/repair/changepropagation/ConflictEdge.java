package at.jku.isse.designspace.rule.arl.repair.changepropagation;

import org.jgrapht.graph.DefaultEdge;

public class ConflictEdge extends DefaultEdge {
    String conflict;
    int sourceHash;
    int targetHash;
    public ConflictEdge(String conflict,int sourceHash, int targetHash){
        super();
        this.conflict = conflict;
        this.sourceHash = sourceHash;
        this.targetHash = targetHash;
    }

    public Object getSource(){
        return super.getSource();
    }
    public Object getTarget(){
        return super.getTarget();
    }
    public int getSourceHash() {return sourceHash;}
    public int getTargetHash() {return targetHash;}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{conflict=´"+conflict+ "' ");
        sb.append(getSource() == null ?  "" : getSource().toString());
        sb.append("--->");
        sb.append(getTarget() == null ?  "" : getTarget().toString());
        sb.append("}");
        return sb.toString();
    }
}
