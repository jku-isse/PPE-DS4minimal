package at.jku.isse.passiveprocessengine.frontend.artifacts;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import at.jku.isse.designspace.artifactconnector.core.repository.ArtifactIdentifier;
import at.jku.isse.designspace.artifactconnector.core.repository.IArtifactProvider;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.passiveprocessengine.core.PPEInstance;
import at.jku.isse.passiveprocessengine.core.PPEInstanceType;
import at.jku.isse.passiveprocessengine.designspace.DesignSpaceSchemaRegistry;
import at.jku.isse.passiveprocessengine.instance.ProcessException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ArtifactResolver {

	private Set<IArtifactProvider> connectors = new HashSet<>();
	private DesignSpaceSchemaRegistry designspace;
	//private Workspace ws;
	//private Map<PPEInstanceType, List<String>> identifierTypes = new HashMap<>();
	
	public ArtifactResolver(DesignSpaceSchemaRegistry designspace) {
		this.designspace = designspace;
	}
	
	public List<String> getIdentifierTypesForInstanceType(PPEInstanceType type) {
		InstanceType dsType = designspace.mapProcessDomainInstanceTypeToDesignspaceInstanceType(type);
		List<String> types = connectors.stream()
			.flatMap(connector -> connector.getSupportedIdentifiers().entrySet().stream())
			.filter(entry -> entry.getKey().equals(dsType))
			.findAny().map(entry -> entry.getValue()).orElse(Collections.emptyList());
				
		if (types.isEmpty()) {
			if (type.getParentType() == null)
				return types; // an empty list;
			else { // else check if we can resolve first super type
					return getIdentifierTypesForInstanceType(type.getParentType());	
				}
		} else 
		return types; 
	}
	
	public Set<PPEInstanceType> getAvailableInstanceTypes() {
		return identifierTypes.keySet();
		List<String> types = connectors.stream()
				.flatMap(connector -> connector.getProvidedArtifactInstanceTypes().stream())
				.map(dsType -> designspace.g)
	}
	
//	public Set<PPEInstance> get(Set<String> artIds, String idType) {
//		Optional<IArtifactProvider> optConn = connectors.stream()
//				.filter(conn1 -> conn1.getSupportedIdentifier()
//										.values()
//										.stream()
//										.flatMap(ids -> ids.stream())
//										.anyMatch(str ->  str.equalsIgnoreCase(idType)))
//				.findAny();
//		if (optConn.isPresent()) {
//			ServiceResponse[] resps = optConn.get().getServiceResponse(artIds, idType);
//			return Arrays.asList(resps).stream()
//			.filter(resp -> resp.getKind() == ServiceResponse.SUCCESS)
//			.map(resp -> {															
//				Element el = ws.findElement(Id.of(Long.parseLong(resp.getInstanceId())));				
//				return (Instance)el;				
//			})
//			.filter(Objects::nonNull)
//			.collect(Collectors.toSet());
//		} else {
//			String msg = String.format("No service registered that provides artifacts of type %s", idType);
//			log.error(msg);
//			return Collections.emptySet();
//		}
//	}
	
	public PPEInstance get(ArtifactIdentifier artId) throws ProcessException {
		return get(artId, false);
	}
	
	public PPEInstance get(ArtifactIdentifier artId, boolean forceFetch) throws ProcessException {
		Optional<IArtifactProvider> optConn = connectors.stream()
			.filter(conn1 -> conn1.getSupportedIdentifier()
									.values()
									.stream()
									.flatMap(ids -> ids.stream())
									.anyMatch(str ->  str.equalsIgnoreCase(artId.getIdType())))
			.findAny();
		if (optConn.isPresent()) {
			ServiceResponse resp = optConn.get().getServiceResponse(artId.getId(), artId.getIdType(), forceFetch);
			if (resp.getKind() == ServiceResponse.SUCCESS) {
				//ws.update();
				Element el = ws.findElement(Id.of(Long.parseLong(resp.getInstanceId())));
				if (el == null) {
					String msg = String.format("Able to resolve artifact %s %s but unable to find element by id %s in process engine workspace", artId.getId(), artId.getType(), resp.getInstanceId());
					log.error(msg);
					throw new ProcessException(msg);
				} else if (el instanceof Instance) {
					return (Instance)el;
				} else {
					String msg = String.format("Able to resolve artifact %s %s but not of 'instance' type", artId.getId(), artId.getType());
					log.error(msg);
					throw new ProcessException(msg);
				}
			} else {
				throw new ProcessException(resp.getMsg());
			}
		} else {
			String msg = String.format("No service registered that provides artifacts of type %s", artId.getType());
			log.error(msg);
	        throw new ProcessException(msg);
		}
	}

	public void register(IArtifactProvider connector) {
		assert(connector != null);
		connectors.add(connector);
		
	}

	//TODO: make this that connector register themselves, and are queryied every time on demand
//	@EventListener
//    public void onApplicationEvent(ApplicationReadyEvent event) {
//		connectors.stream().forEach(connector -> identifierTypes.putAll(connector.getSupportedIdentifier()));
//	}
	
//	public void inject(Workspace ws) {
//		this.ws = ws;
//	}

}
