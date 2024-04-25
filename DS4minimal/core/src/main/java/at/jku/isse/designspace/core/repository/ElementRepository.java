package at.jku.isse.designspace.core.repository;

import java.util.List;

import at.jku.isse.designspace.core.events.Operation;
import at.jku.isse.designspace.core.model.Element;
import at.jku.isse.designspace.core.model.Id;

public interface ElementRepository {

    void save(List<Operation> operations);
    Element load(Id elementId);
}