package at.jku.isse.designspace.artifactconnector.core.servicespecificimplementations;

import at.jku.isse.designspace.core.model.InstanceType;

/**
 *
 * receiving the type of an instance for the designspace from the relation name
 * is important and needs to be made possible separately for every service
 * Such relations should also be mapped to the field name.
 *
 */
public interface ISubtypeMapper {

    InstanceType resolveType(String name);

}
