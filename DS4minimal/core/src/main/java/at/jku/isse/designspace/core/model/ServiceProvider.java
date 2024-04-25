package at.jku.isse.designspace.core.model;

import java.util.Collection;

import at.jku.isse.designspace.core.events.Operation;

public interface ServiceProvider {

    /**
     * service request is issued immediately preceeding transactionConcluded or updated to allow services
     * (e.g., consistency checker) to add their contributions (e.g., inconsistencies)
     */
    void handleServiceRequest(Workspace workspace, Collection<Operation> operations);

    void initialize();

    String getName();
    String getVersion();
    int getPriority();
    boolean isPersistenceAware();
}