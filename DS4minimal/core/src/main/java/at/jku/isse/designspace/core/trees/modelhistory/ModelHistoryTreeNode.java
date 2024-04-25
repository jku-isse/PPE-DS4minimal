package at.jku.isse.designspace.core.trees.modelhistory;

import java.util.HashMap;
import java.util.Map;

import at.jku.isse.designspace.core.events.ElementUpdate;
import at.jku.isse.designspace.core.events.Operation;
import at.jku.isse.designspace.core.trees.abstracts.AbstractSynchronizedTreeNode;

public class ModelHistoryTreeNode extends AbstractSynchronizedTreeNode<ModelHistoryTreeNode, ModelHistoryTreeEdgeType, Operation> {

    protected static HashMap<Character,String> escapes = new HashMap<>();

    static{
        escapes.put('0',"\u2080");
        escapes.put('1',"\u2081");
        escapes.put('2',"\u2082");
        escapes.put('3',"\u2083");
        escapes.put('4',"\u2084");
        escapes.put('5',"\u2085");
        escapes.put('6',"\u2086");
        escapes.put('7',"\u2087");
        escapes.put('8',"\u2088");
        escapes.put('9',"\u2089");
    }

    public ModelHistoryTreeNode(Operation data) {
        super(data);
    }

    public Map<Long, ModelHistoryTreeNode> parentSkip = new HashMap<>(); //key -> workspaceId
    public Map<Long, ModelHistoryTreeNode> parentElementSkip = new HashMap<>(); //key -> workspaceId
    public Map<Long, ModelHistoryTreeNode> parentPropertySkip = new HashMap<>(); //key -> workspaceId

    public long idElement() {
        return data.elementId().value();
    }

    public String idProperty() {
        return data instanceof ElementUpdate ? idElement()+((ElementUpdate)data).name():id()+"";
    }

    @Override
    public long id() {
        return data.id();
    }

    @Override
    public String name() {
        return ""+data.id();
    }

    @Override
    public String label() {
//        String label = "\u0394";
//        char[] name = name().toCharArray();
//        for (int i = 0; i < name.length; i++){
//            label += escapes.get(name[i]);
//        }
        return name();
    }
}
