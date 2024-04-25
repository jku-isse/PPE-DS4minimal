package at.jku.isse.designspace.core.trees.collaboration;

import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.trees.abstracts.AbstractFactory;

public class CollaborationTreeFactory extends AbstractFactory<CollaborationTreeNode, CollaborationTreeEdgeType, Workspace> {

    @Override
    public CollaborationTreeNode CreateNode(Workspace data) {
        return new CollaborationTreeNode(data);
    }

}
