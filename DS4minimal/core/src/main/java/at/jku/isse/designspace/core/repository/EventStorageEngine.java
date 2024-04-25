package at.jku.isse.designspace.core.repository;

import java.util.List;
import java.util.Set;

import at.jku.isse.designspace.core.events.Operation;
import at.jku.isse.designspace.core.model.Id;

public interface EventStorageEngine {

    void appendOperations(List<Operation> operations);
    void appendOperation(Operation operation);

    List<Operation> operations(long until);
    List<Operation> operations(long since, long until);
    List<Operation> operations(Id elementId, long until);

    Set<Id> elements(long until);

    void clear();
}