package at.jku.isse.designspace.artifactconnector.core.exceptions;

public class SchemaAlreadyExistsException extends Exception {

    public SchemaAlreadyExistsException() {
        super("There already is a schema with the server id, you provided. Pick another serverId!");
    }

}
