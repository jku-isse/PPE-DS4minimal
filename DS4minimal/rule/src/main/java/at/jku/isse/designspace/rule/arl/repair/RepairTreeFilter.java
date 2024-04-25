package at.jku.isse.designspace.rule.arl.repair;

import java.util.HashSet;

public abstract class RepairTreeFilter {
    public abstract boolean compliesTo(RepairAction repairAction);
    private void filterRepairNode(RepairNode repairNode) {
        if (repairNode instanceof RepairAction) {
            RepairAction action = (RepairAction) repairNode;
            if (!compliesTo(action)) {
                action.delete();
            }
        }else{
           for(RepairNode childNode : new HashSet<>(repairNode.getChildren())){
               filterRepairNode(childNode);
           }
        }
    }
    public void filterRepairTree(RepairNode repairTree){
        if(repairTree.getParent()!= null)
            throw new UnsupportedOperationException("repairTree must be the root node");
        else{
            for(RepairNode childNode : new HashSet<>(repairTree.getChildren())){
                filterRepairNode(childNode);
            }
        }
        repairTree.flattenRepairTree();
    }
}
