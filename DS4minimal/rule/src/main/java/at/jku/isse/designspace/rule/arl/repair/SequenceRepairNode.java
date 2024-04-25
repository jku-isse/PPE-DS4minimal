package at.jku.isse.designspace.rule.arl.repair;


import java.util.Iterator;

public class SequenceRepairNode extends AbstractRepairNode {

    public SequenceRepairNode(RepairNode parent) {
        super(parent, Type.SEQUENCE);
    }

    @Override
    public boolean isExecutable() {
        boolean executable = true;
        Iterator<RepairNode> iterator = getChildren().iterator();
        while (executable && iterator.hasNext()) {
            executable &= iterator.next().isExecutable();
        }
        return executable;
    }

    @Override
    public boolean executed() {
        boolean executed = true;
        Iterator<RepairNode> iterator = getChildren().iterator();
        while (executed && iterator.hasNext()) {
            executed &= iterator.next().executed();
        }
        return executed;
    }

    @Override
    public boolean isUndoable() {
        boolean undoable = true;
        Iterator<RepairNode> iterator = getChildren().iterator();
        while (undoable && iterator.hasNext()) {
            undoable &= iterator.next().isUndoable();
        }
        return undoable;
    }

}