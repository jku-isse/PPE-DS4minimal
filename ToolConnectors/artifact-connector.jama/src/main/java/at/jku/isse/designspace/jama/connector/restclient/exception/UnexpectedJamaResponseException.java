package at.jku.isse.designspace.jama.connector.restclient.exception;

public class UnexpectedJamaResponseException extends RuntimeException {
    public UnexpectedJamaResponseException(String message) {
        super(message);
    }
}
