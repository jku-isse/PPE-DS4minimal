package at.jku.isse.designspace.artifactconnector.core.updateservice.exceptions;

public class ConnectionException extends Exception {

    public ConnectionException() {
        super("Establishing a connection has failed, this mostly indicated invalid authentication or a wrong address");
    }

}
