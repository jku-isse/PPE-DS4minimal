package at.jku.isse.designspace.jama.connector.restclient.exception;

public class JamaTypeMismatchException extends RestClientException {
    public JamaTypeMismatchException () {}

    public JamaTypeMismatchException (String message) {
        super(message);
    }

}
