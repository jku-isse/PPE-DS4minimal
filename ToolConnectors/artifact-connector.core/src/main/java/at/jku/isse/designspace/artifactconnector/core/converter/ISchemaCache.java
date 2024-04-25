package at.jku.isse.designspace.artifactconnector.core.converter;

import at.jku.isse.designspace.core.model.InstanceType;

public interface ISchemaCache {

    void addSchema(InstanceType instanceType, String schemaId);

    InstanceType getSchema(String schemaId);

}
