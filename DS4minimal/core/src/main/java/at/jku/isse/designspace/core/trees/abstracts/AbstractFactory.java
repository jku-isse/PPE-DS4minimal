package at.jku.isse.designspace.core.trees.abstracts;

public abstract class AbstractFactory<T extends AbstractSynchronizedTreeNode<T,K,L>, K extends Enum,L> {
    public abstract T CreateNode(L data);
}
