package at.jku.isse.designspace.core.trees.abstracts;



import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractSynchronizedTreeNode<T extends AbstractSynchronizedTreeNode<T,K,L>,K extends Enum,L> {
    protected AbstractSynchronizedTreeNode(L data) {
        this.data = data;
    }

    private Map<String,T> parentMap = new HashMap<>();
    private Map<String,Map<Long,T>> childrenMaps = new HashMap<>();

    public abstract long id();
    public abstract String name();
    public abstract String label();
    public final L data;

    protected Map<Long,T> getChildrenMap(K edgeType){
        if(!childrenMaps.containsKey(edgeType.toString())){
            HashMap<Long,T> childrenMap = new HashMap<Long,T>();
            childrenMaps.put(edgeType.toString(),childrenMap);
        }
        return childrenMaps.get(edgeType.toString());
    }

    public T getParent(K edgeType){
        return getParent(edgeType.toString());
    }
    public T getParent(String key){
        return parentMap.get(key);
    }

    public void setParent(K edgeType, T node){ parentMap.put(edgeType.toString(), node); }

    public void attach(T child, K edgeType){
        var childrenMap = getChildrenMap(edgeType);
        if(childrenMap.containsKey(child.id())){
            return;
        }
        if(child.getParent(edgeType) != null){
            child.getParent(edgeType).detach(child, edgeType);
        }
        childrenMap.put(child.id(),child);
        child.setParent(edgeType, (T)this);
    }

    public void detach(T child, K edgeType){
        var childrenMap = getChildrenMap(edgeType);
        childrenMap.remove(child.id());
        child.setParent(edgeType, null);
    }

    public T getChild(long id, K edgeType){
        var childrenMap = getChildrenMap(edgeType);
        return childrenMap.get(id);
    }

    public List<T> getChildren(K edgeType){
        var childrenMap = getChildrenMap(edgeType);
        return (List<T>)childrenMap.values().stream().collect(Collectors.toList());
    }

    public boolean isRoot(K edgeType){
        return getParent(edgeType) == null;
    }
    public boolean isLeaf(K edgeType){
        return getChildrenMap(edgeType).isEmpty();
    }
}
