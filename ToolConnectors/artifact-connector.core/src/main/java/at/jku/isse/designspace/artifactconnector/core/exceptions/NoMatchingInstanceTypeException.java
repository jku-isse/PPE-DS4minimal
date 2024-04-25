package at.jku.isse.designspace.artifactconnector.core.exceptions;

public class NoMatchingInstanceTypeException extends Exception {

    public NoMatchingInstanceTypeException() {
        super("The field you were trying to access has no instanceType mapped in the provided mapper!");
    }

}
