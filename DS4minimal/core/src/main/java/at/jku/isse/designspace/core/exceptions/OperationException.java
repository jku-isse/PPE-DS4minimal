package at.jku.isse.designspace.core.exceptions;

import at.jku.isse.designspace.core.events.Operation;

public class OperationException extends RuntimeException {

    Operation operation =null;

    public OperationException(Operation operation, String message) {
        super(message);
        this.operation = operation;
    }
}
