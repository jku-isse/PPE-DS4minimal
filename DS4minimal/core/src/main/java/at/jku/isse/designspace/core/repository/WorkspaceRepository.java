package at.jku.isse.designspace.core.repository;

import java.util.List;
import java.util.Set;

import at.jku.isse.designspace.core.events.Operation;
import at.jku.isse.designspace.core.model.Element;
import at.jku.isse.designspace.core.model.Id;
import at.jku.isse.designspace.core.model.Workspace;

public interface WorkspaceRepository extends ElementRepository {

    //List<Operation> pushToParentRepository();

    //List<Operation> pullFromParentRepository();

    //List<Operation> changeParent(WorkspaceRepository upstreamRepository);

    //List<Operation> mirror(WorkspaceRepository targetRepository, long since);

    List<Operation> allOperations();

    List<Operation> elementOperations(Id elementId);

    Set<Element> changedElements();

    boolean hasChangedElement(Id elementId);

    Set<Element> debugAllElements();

    Workspace workspace();

    EventStorageEngine storageEngine();
}