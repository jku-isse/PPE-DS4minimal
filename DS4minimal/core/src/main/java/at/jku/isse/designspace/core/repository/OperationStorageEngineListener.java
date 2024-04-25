package at.jku.isse.designspace.core.repository;

import java.util.Collection;

import at.jku.isse.designspace.core.events.Operation;

public interface OperationStorageEngineListener {

    void operationsInserted(Collection<Operation> operations);

}
