package at.jku.isse.designspace.jira.service;

import java.util.Map;
import java.util.Optional;

import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;

public interface IArtifactPusher {

    Optional<Instance> createInstance(Map<String, Object> artifact, InstanceType instanceType, boolean update, boolean withIssueLinks);

    Optional<Instance> findArtifact(String id);

    Instance createPlaceholderArtifact(String id, String key, InstanceType type);

}
