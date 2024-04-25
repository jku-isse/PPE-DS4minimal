package at.jku.isse.designspace.artifactconnector.core.idcache;

import java.util.Set;

import at.jku.isse.designspace.core.model.Id;

public interface IIdCache {

    Id getDesignspaceId(String serviceId);

    void addEntry(String serviceId, Id designspaceId);

    Set<Id> getAllInstanceIds();

    Set<String> getAllServiceIds();

}
