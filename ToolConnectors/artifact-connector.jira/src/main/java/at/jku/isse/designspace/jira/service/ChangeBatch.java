package at.jku.isse.designspace.jira.service;


import java.sql.Timestamp;
import java.util.Set;

import at.jku.isse.designspace.jira.updateservice.IChangeLogItem;

public class ChangeBatch {

    private Timestamp timestamp;
    private Set<IChangeLogItem> changes;

    public ChangeBatch(Timestamp timestamp, Set<IChangeLogItem> changes) {
        this.timestamp = timestamp;
        this.changes = changes;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public Set<IChangeLogItem> getChanges() {
        return changes;
    }

}
