package at.jku.isse.designspace.core.trees.collaboration;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.trees.abstracts.AbstractSynchronizedTree;

public class CollaborationTree extends AbstractSynchronizedTree<CollaborationTreeFactory, CollaborationTreeNode, CollaborationTreeEdgeType,Workspace> {
    private static CollaborationTree instance = null;
    public static CollaborationTree getInstance(){
        if (instance == null){
            instance = new CollaborationTree();
        }
        return instance;
    }

    public static Workspace get(long workspaceId){
        if(!getInstance().nodes.containsKey(workspaceId)){
            return null;
        }
        return getInstance().nodes.get(workspaceId).data;
    }
    public static Workspace getParent(long workspaceId){
        CollaborationTree collab = getInstance();
        if(!collab.nodes.containsKey(workspaceId)){
            return null;
        }
        var parent = collab.nodes.get(workspaceId).getParent(CollaborationTreeEdgeType.WORKSPACE);
        if(parent == null){
            return null;
        }
        return parent.data;
    }
    public static List<Workspace> getChildren(long workspaceId){
        return getInstance().nodes.get(workspaceId).getChildren(CollaborationTreeEdgeType.WORKSPACE).stream().map(x -> x.data).collect(Collectors.toList());
    }

    public static void changeSettings(long workspaceId, SynchFlag commit, SynchFlag update){
        getInstance().nodes.get(workspaceId).setting = new CollaborationSetting(true, true);
        CollaborationTree.getInstance().refreshModifiedTimestamp();
    }

    public static CollaborationSetting getSetting(long workspaceId){
        return getInstance().nodes.get(workspaceId).setting;
    }

    private CollaborationTree() {
        super(new CollaborationTreeFactory());
    }

    public void attach(Long parentId, Long childId) {
        super.attach(parentId, childId, CollaborationTreeEdgeType.WORKSPACE);
    }

    public String exportAsJSON() {
        try {
            JSONArray nodesList = new JSONArray();
            //JSONArray edgesList = new JSONArray();

            var nodesCopy = new LinkedList<>(nodes.values());
            for (CollaborationTreeNode node : nodesCopy) {
                JSONObject nodeObject = new JSONObject();
                nodeObject.put("id", node.id());
                nodeObject.put("name", node.label());
                nodeObject.put("operation_id", node.data.state.getLeafNode(true).id());

                nodeObject.put("updateTargetFlag", "");
                nodeObject.put("commitTargetFlag", "");

                nodeObject.put("autocommit", node.data.isAutoCommit());
                nodeObject.put("autoupdate", node.data.isAutoUpdate());

                var parentNode = node.getParent(CollaborationTreeEdgeType.WORKSPACE);
                if(parentNode != null){
                    nodeObject.put("parentId", parentNode.id());
                }

                JSONArray nodesListChilds = new JSONArray();

                for (var child : node.getChildren(CollaborationTreeEdgeType.WORKSPACE)) {
                    nodesListChilds.put(child.id());
                }

                if(nodesListChilds.length() > 0){
                    nodeObject.put("children", nodesListChilds);
                }
                
                nodesList.put(nodeObject);
            }


            JSONObject mainObject = new JSONObject();

            //mainObject.put("nodes", nodesList);

            //mainObject.put("links", edgesList);
            return nodesList.toString(4);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "NO JSON!";
    }
}
