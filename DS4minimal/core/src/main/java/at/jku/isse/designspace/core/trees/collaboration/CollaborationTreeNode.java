package at.jku.isse.designspace.core.trees.collaboration;

import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.trees.abstracts.AbstractSynchronizedTreeNode;

public class CollaborationTreeNode extends AbstractSynchronizedTreeNode<CollaborationTreeNode, CollaborationTreeEdgeType,Workspace> {
    public CollaborationSetting setting = null;

    protected CollaborationTreeNode(Workspace data) {
        super(data);
        setting = CollaborationSetting.DEFAULT;
    }

    @Override
    public long id() {
        return data.id();
    }

    @Override
    public String name() {
        return data.name();
    }

    @Override
    public String label() {
        return name();
    }
}
