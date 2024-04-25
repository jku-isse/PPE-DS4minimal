package at.jku.isse.designspace.core.exceptions;

import at.jku.isse.designspace.core.model.Id;

public class WrongArgumentException extends CoreException {
    public WrongArgumentException(Id offendingElement, String offendingPropertyName, String message)
    {
        super(offendingElement, offendingPropertyName, message);
    }
    public WrongArgumentException(Id offendingElement, String offendingPropertyName, Id otherOffendingElement, String otherOffendingPropertyName, String message)
    {
        super(offendingElement, offendingPropertyName, otherOffendingElement, otherOffendingPropertyName, message);
    }
}
