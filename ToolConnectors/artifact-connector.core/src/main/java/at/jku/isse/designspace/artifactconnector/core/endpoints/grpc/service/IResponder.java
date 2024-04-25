package at.jku.isse.designspace.artifactconnector.core.endpoints.grpc.service;

import java.util.Set;

public interface IResponder {

    ServiceResponse getServiceResponse(String id, String identifierType); 
    
    ServiceResponse getServiceResponse(String id, String identifierType, boolean doForceRefetch); 
    
    // redefined semantics of service to be used as identifierType, identifierType needs to be unique across all supported types to identify service and also which artifact type to return.
    
    ServiceResponse[] getServiceResponse(Set<String> ids, String identifierType);
    
    ServiceResponse[] getServiceResponse(Set<String> ids, String identifierType, boolean doForceRefetch);

}