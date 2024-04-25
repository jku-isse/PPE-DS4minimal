package at.jku.isse.passiveprocessengine.frontend.artifacts;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import at.jku.isse.designspace.artifactconnector.core.repository.ArtifactIdentifier;
import at.jku.isse.designspace.artifactconnector.core.repository.FetchResponse;
import at.jku.isse.designspace.artifactconnector.core.repository.IArtifactProvider;
import at.jku.isse.designspace.core.model.Element;
import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.passiveprocessengine.core.SchemaRegistry;
import at.jku.isse.passiveprocessengine.instance.ProcessException;
import at.jku.isse.passiveprocessengine.instance.factories.ProcessConfigFactory;

public class ProcessConfigProvider implements IArtifactProvider {

	private SchemaRegistry schemaReg;
	private InstanceRepository
	
	@Override
	public ServiceResponse getServiceResponse(String id, String identifierType) {
		try {
			
			long longId = Long.parseLong(id);
//			if (longId < 0) {
//				// create the object
//				try {
//					Instance inst = configFactory.createConfigInstance(UUID.randomUUID().toString(), identifierType);
//					return new ServiceResponse(0, "ProcessConfigProvider", "Created", inst.id().toString());
//				} catch(ProcessException e) {
//					return new ServiceResponse(1, "ProcessConfigProvider", e.getMessage(), id);
//				}
//			} else {
				Element el = ws.findElement(Id.of(longId));
				if (el != null) {
					return new ServiceResponse(0, "ProcessConfigProvider", "Found", id);
				} else 
					return new ServiceResponse(3, "ProcessConfigProvider", "Not found", id);
			//}
		} catch(Exception e) {
			return new ServiceResponse(1, "ProcessConfigProvider", "Invalid identifier type", id);
		}
	}

	
	@Override
	public InstanceType getArtifactInstanceType() {
		return configFactory.getBaseType();
	}

	@Override
	public Set<InstanceType> getArtifactInstanceTypes() {
		return Set.of(configFactory.getBaseType());
	}

	@Override
	public Map<InstanceType, List<String>> getSupportedIdentifier() {		
		// dynamically compiles list of configuration types
		//List<String> subtypes = configFactory.getBaseType().getAllSubTypes().stream().map(type -> type.name()).collect(Collectors.toList());		
		return Map.of(configFactory.getBaseType(), List.of("Designspace ID"));				
	}

	@Override
	public InstanceType getDefaultArtifactInstanceType() {
		configFactory.
	}

	@Override
	public Set<InstanceType> getProvidedArtifactInstanceTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<InstanceType, List<String>> getSupportedIdentifiers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<FetchResponse> fetchArtifact(Set<ArtifactIdentifier> artifactIdentifiers) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<FetchResponse> forceFetchArtifact(Set<ArtifactIdentifier> artifactIdentifiers) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
