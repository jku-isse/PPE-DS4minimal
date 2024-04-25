package at.jku.isse.designspace.artifactconnector.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

import at.jku.isse.designspace.artifactconnector.core.endpoints.grpc.service.IResponder;
import at.jku.isse.designspace.core.model.InstanceType;

public interface IArtifactProvider extends IResponder {

    InstanceType getArtifactInstanceType(); // returns the default type

    Set<InstanceType> getArtifactInstanceTypes(); // returns all supported types
    
    Map<InstanceType, List<String>> getSupportedIdentifier(); // returns for each supported instance type the various supported identifiers
    
    
}
