package at.jku.isse.designspace.artifactconnector.core.exceptions;

public class NoCorrespondingInstanceException extends Exception {

    public NoCorrespondingInstanceException() {
        super("The artifact you are trying to access is not an instance in the designspace");
    }

}
