package at.jku.isse.designspace.core.trees.abstracts;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbstractSynchronizedTree<M extends AbstractFactory<T, K, L>, T extends AbstractSynchronizedTreeNode<T, K, L>, K extends Enum, L> {
    ///ToDo: exchange all collections with synchronzied-collections
    private Timestamp lastModified = new Timestamp(System.currentTimeMillis());
    protected Map<Long, T> nodes = new ConcurrentHashMap<>();

    public final M factory;

    public AbstractSynchronizedTree(M factory) {
        this.factory = factory;
    }

    public Timestamp lastModified(){
        return lastModified;
    }
    public void refreshModifiedTimestamp(){
        lastModified = new Timestamp(System.currentTimeMillis());
    }

   /* public AbstractSynchronizedTree(M factory, String fileName) {
        //ToDo: Load as XML
        this.factory = factory;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String thisClass = this.getClass().getSimpleName();
            String fileClass = reader.readLine();
            if (thisClass != fileClass){
                throw new IllegalArgumentException("Wrong file format!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    public T getRoot(K edgeType) {
        return nodes.values().stream().filter(n -> n.isRoot(edgeType)).findFirst().orElseGet(() -> null);
    }

    public List<T> getAllLeafs(K edgeType) {
        return nodes.values().stream().filter(n -> n.isLeaf(edgeType)).collect(Collectors.toList());
    }

    public T add(L data) {
        T newNode = factory.CreateNode(data);
        if (nodes.containsKey(newNode.id())) {
            return null;
        }
        nodes.put(newNode.id(), newNode);
        refreshModifiedTimestamp();
        return newNode;
    }

    public void attach(T parent, T child, K edgeType) {
        //T parentNode = nodes.get(idParent);
        //T childNode = nodes.get(idChild);
        if (parent == null || child == null) {
            return;
        }
        parent.attach(child, edgeType);
    }

    public void attach(Long parentId, Long childId, K edgeType) {
        T parentNode = nodes.get(parentId);
        T childNode = nodes.get(childId);
        if (parentNode == null || childNode == null) {
            return;
        }
        parentNode.attach(childNode, edgeType);
    }

    public void detach(T parent, T child, K edgeType) {
        //T parentNode = nodes.get(idParent);
        //T childNode = nodes.get(idChild);
        if (parent == null || child == null) {
            return;
        }
        parent.detach(child, edgeType);
    }

    public void clear() {
        nodes.clear();
    }

    public L getData(long id) {
        if(!nodes.containsKey(id)){
            return null;
        }
        return ((T) nodes.get(id)).data;
    }

    public T getNode(long id) {
        return ((T) nodes.get(id));
    }

    public List<T> getChildren(long id, K edgeType) {
        return ((T) nodes.get(id)).getChildren(edgeType);
    }

    public T getParent(long id, K edgeType) {
        return ((T) nodes.get(id)).getParent(edgeType);
    }

    public Collection<T> getAllNodes() {
        return nodes.values().stream().collect(Collectors.toList());
    }
    public Collection<L> getAllData() {
        return nodes.values().stream().map(x -> x.data).collect(Collectors.toList());
    }

    /**
     * Returns a list of all nodes between ancestor and descendant. Includes both.
     */
    public List<T> getNodes(T fromAncestor, T toDescendant, K edgeType) {
        var nodesFound = new ArrayList<T>();
        var currentNode = toDescendant;
        while (currentNode != null && currentNode != fromAncestor) {
            nodesFound.add(currentNode);
            currentNode = currentNode.getParent(edgeType);
        }
        if(currentNode == null && fromAncestor != null){ // fromAncestor not reachable from toDescendant
            System.out.println("Warning: Ancestor was not reachable!");
            return null;
        }

        if (fromAncestor != null) {
            nodesFound.add(fromAncestor);
        }

        Collections.reverse(nodesFound);
        return nodesFound;
    }

        public String exportAsJSON (K edgeType){
            try {
                JSONArray nodesList = new JSONArray();
                //JSONArray edgesList = new JSONArray();

                for (T node : nodes.values()) {
                    JSONObject nodeObject = new JSONObject();
                    nodeObject.put("id", node.id());
                    nodeObject.put("name", node.label());

                /*for (var enumConstant : enumType.getEnumConstants()) {
                    JSONArray nodesListChilds = new JSONArray();
                    for (var child : node.getChildren(enumConstant)) {
                        JSONObject nodeChild = new JSONObject();
                        nodeChild.put("id", child.id());
                        nodesListChilds.put(nodeChild);
                    }
                    if(nodesListChilds.length() > 0){
                        nodeObject.put("childs"+enumConstant.name(), nodesListChilds);
                    }
                }*/
                    JSONArray nodesListChilds = new JSONArray();
                    for (var child : node.getChildren(edgeType)) {
                        JSONObject nodeChild = new JSONObject();
                        nodeChild.put("id", child.id());
                        nodesListChilds.put(nodeChild);
                    }
                    if (nodesListChilds.length() > 0) {
                        nodeObject.put("childs" + edgeType.name(), nodesListChilds);
                    }
                    nodesList.put(nodeObject);
                }

                JSONObject mainObject = new JSONObject();
                return nodesList.toString(4);
                //Write JSON file
            /*try (FileWriter file = new FileWriter(fileName)) {
                file.write(mainObject.toString(4));
                file.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return "NO JSON!";
        }
    }
