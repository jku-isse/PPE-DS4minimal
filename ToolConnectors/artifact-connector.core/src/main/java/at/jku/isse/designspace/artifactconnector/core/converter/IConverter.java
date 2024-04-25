package at.jku.isse.designspace.artifactconnector.core.converter;

import java.util.Map;
import java.util.Optional;

import at.jku.isse.designspace.core.model.InstanceType;

public interface IConverter {

    Optional<InstanceType> findSchema(String schemaId);

    InstanceType createSchema(Map<String, Object> schema, Map<String, Object> linkTypes, Map<String, Object> names, String schemaId);

    void synchronizeSchemata(InstanceType existingSchema, Map<String, Object> newSchemaMap, Map<String, Object> newLinkTypeMap);

}

