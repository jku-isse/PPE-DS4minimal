package at.jku.isse.designspace.core.trees.modelhistory;

import at.jku.isse.designspace.core.events.Operation;
import at.jku.isse.designspace.core.trees.abstracts.AbstractFactory;

public class ModelHistoryTreeFactory extends AbstractFactory<ModelHistoryTreeNode, ModelHistoryTreeEdgeType, Operation> {

    @Override
    public ModelHistoryTreeNode CreateNode(Operation data) {
        return new ModelHistoryTreeNode(data);
    }
}
