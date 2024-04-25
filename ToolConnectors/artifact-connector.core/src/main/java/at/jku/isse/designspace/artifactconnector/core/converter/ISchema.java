package at.jku.isse.designspace.artifactconnector.core.converter;

import java.util.Optional;

import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;

public interface ISchema {

    String getSchemaId();

    InstanceType getRoot();

    Instance getSchemaInstance();

    Optional<String> findProperty(String s);

    InstanceType resolveFieldName(String fieldName);

}
