package at.jku.isse.designspace.core.exceptions;

import at.jku.isse.designspace.core.model.Id;

public abstract class CoreException extends RuntimeException {
    Id offendingElement;
    String offendingPropertyName;
    Id otherOffendingElement;
    String otherOffendingPropertyName;

    public CoreException(Id offendingElement, String offendingPropertyName, String message) {
        super(message);
        this.offendingElement = offendingElement;
        this.offendingPropertyName = offendingPropertyName;
        this.otherOffendingElement = null;
        this.otherOffendingPropertyName = null;
    }
    public CoreException(Id offendingElement, String offendingPropertyName, Id otherOffendingElement, String otherOffendingPropertyName, String message) {
        super(message);
        this.offendingElement = offendingElement;
        this.offendingPropertyName = offendingPropertyName;
        this.otherOffendingElement = otherOffendingElement;
        this.otherOffendingPropertyName = otherOffendingPropertyName;
    }
}
