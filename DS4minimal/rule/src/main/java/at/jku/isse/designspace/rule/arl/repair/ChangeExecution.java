package at.jku.isse.designspace.rule.arl.repair;

import at.jku.isse.designspace.rule.arl.exception.ChangeExecutionException;

public interface ChangeExecution {

    boolean isExecutable();

    boolean isAbstract();

    void execute() throws ChangeExecutionException;

    boolean executed();

    boolean isUndoable();

    void undo() throws ChangeExecutionException;

}
