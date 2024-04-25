package at.jku.isse.designspace.jama.service;

import java.util.Optional;

import at.jku.isse.designspace.artifactconnector.core.IArtifactProvider;
import at.jku.isse.designspace.core.model.Instance;

public interface IJamaService extends IArtifactProvider {

	enum JamaIdentifiers {
		JamaItemId, JamaItemDocKey, JamaReleaseId, JamaProjectId, JamaFilterId
	}
	
    /**
     * The idDescriptor specifies the type of the identifier.
     * The services are responsible for the correct use of the
     * identifier for getting the associated jama item.
     *
     * @param identifier
     * @param idDescriptor
     * @return
     */
    Optional<Instance> getJamaItem(String identifier, JamaIdentifiers idDescriptor);

}
