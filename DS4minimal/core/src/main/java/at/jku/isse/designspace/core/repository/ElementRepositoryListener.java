package at.jku.isse.designspace.core.repository;

import at.jku.isse.designspace.core.model.Id;

public interface ElementRepositoryListener {

    void elementUpdated(Id elementId);

}
