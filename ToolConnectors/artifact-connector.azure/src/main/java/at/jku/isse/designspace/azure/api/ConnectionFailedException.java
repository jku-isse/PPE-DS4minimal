package at.jku.isse.designspace.azure.api;

public class ConnectionFailedException extends Exception {

    public ConnectionFailedException(String msg) {
        super(msg);
    }
}
