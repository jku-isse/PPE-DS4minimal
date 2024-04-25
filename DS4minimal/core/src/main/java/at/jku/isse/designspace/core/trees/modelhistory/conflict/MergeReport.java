package at.jku.isse.designspace.core.trees.modelhistory.conflict;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.core.trees.modelhistory.ModelHistoryTreeNode;

///conflicts only happen on update
public class MergeReport {
    public Timestamp timestamp;
    public Workspace parent;
    public Workspace child;
    public boolean overwriteParent;
    public MergeResult result;

    public MergeReport(Workspace parent, Workspace child, List<ModelHistoryTreeNode> listSource, List<ModelHistoryTreeNode> listTarget, boolean overwriteParent) {
        this.parent = parent;
        this.child = child;
        this.overwriteParent = overwriteParent;

        result = new MergeResult(listSource, listTarget, overwriteParent);
        timestamp = Timestamp.from(Instant.now());
    }
}
