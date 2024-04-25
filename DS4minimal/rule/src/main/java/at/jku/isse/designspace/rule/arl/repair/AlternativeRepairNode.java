package at.jku.isse.designspace.rule.arl.repair;

import java.util.Iterator;
public class AlternativeRepairNode extends AbstractRepairNode {

    public AlternativeRepairNode(RepairNode parent) {
        super(parent, Type.ALTERNATIVE);
    }

    @Override
    public boolean isExecutable() {
        return false;
    }

    @Override
    public boolean executed() {
        boolean executed = false;
        Iterator<RepairNode> iterator = getChildren().iterator();
        while (!executed && iterator.hasNext()) {
            executed |= iterator.next().executed();
        }
        return executed;
    }

    @Override
    public boolean isUndoable() {
        boolean undoable = false;
        Iterator<RepairNode> iterator = getChildren().iterator();
        while (!undoable && iterator.hasNext()) {
            undoable |= iterator.next().isUndoable();
        }
        return undoable;
    }


}