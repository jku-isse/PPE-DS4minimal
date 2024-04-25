package at.jku.isse.designspace.artifactconnector.core.exceptions;

public class NoMoreEventsException extends Exception {

    public NoMoreEventsException() {
        super("There are no more events in the workspace for traveling further in to this direction");
    }

}
