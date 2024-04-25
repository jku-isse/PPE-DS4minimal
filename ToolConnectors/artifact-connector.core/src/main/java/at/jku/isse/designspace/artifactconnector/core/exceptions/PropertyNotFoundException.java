package at.jku.isse.designspace.artifactconnector.core.exceptions;

public class PropertyNotFoundException extends Exception {

    public PropertyNotFoundException() {
        super("The property you were trying to access, does not exist in this designspace instance");
    }

}
