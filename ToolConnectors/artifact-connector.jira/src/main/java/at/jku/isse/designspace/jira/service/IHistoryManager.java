package at.jku.isse.designspace.jira.service;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Set;

import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.jira.updateservice.ChangeLogItem;
import at.jku.isse.designspace.jira.updateservice.IChangeLogItem;

public interface IHistoryManager {

    /**
     *
     * method for directly updating the Designspace.
     *
     * @param change
     * @return
     */
    boolean applyUpdate(ChangeLogItem change);

    /**
     *
     * method for directly updating the Designspace.
     *
     * @param changes
     * @return
     */
    int applyUpdates(Collection<IChangeLogItem> changes);

    /**
     *
     * Method loads the the initial versions of all Artifacts from the connected
     * artifactrelations database into the Designspace and queues the changes.
     * They can be applied using the peek and commit methods of this interface.
     *
     */
    void queueEntireDatabaseToDesignspace();

    /**
     *
     * Method loads the the initial versions of all Artifacts from the connected
     * artifactrelations database into the Designspace and applies the changes.
     *
     */
    void pushEntireDatabaseToDesignspace();

    /**
     *
     * Method creates initial version of an Artifact
     * and queues changes. Replay can be controlled with peek
     * and commit methods.
     *
     * @param id
     * @param schema
     */
    void queueArtifactWithHistory(String id, InstanceType schema);

    /**
     *
     * Method creates initial version of an Artifact
     * and directly applies all changes from history.
     *
     * @param id
     * @param schema
     */
    void pushArtifactWithHistory(String id, InstanceType schema);

    /**
     *
     * Returns the all ChangeLogItems associated with the next Timestamp.
     *
     * @return
     */
    Set<IChangeLogItem> peekNextChanges();

    /**
     *
     * Returns the timestamp of the next ChangeLogItem in the queue of changes.
     *
     * @return
     */
    Timestamp peekNextTimeStamp();

    /**
     *
     * Commits the set of changes associated with the next timestamp.
     *
     */
    void commitChangesForNextTimeStamp();

    /**
     *
     * Method creates initial version of an Artifact
     * and queues changes. Replay can be controlled with peek
     * and commit methods.
     *
     * @param id
     */
    void queueArtifactWithHistory(String id);

    /**
     *
     * Method creates initial version of an Artifact
     * and directly applies all changes from history.
     *
     * @param id
     */
    void pushArtifactWithHistory(String id);

    /**
     *
     * Adds changes to the queue of upcoming changes.
     *
     * @param changes
     * @return
     */
    void addUpdatesToQueue(Collection<IChangeLogItem> changes) ;

    /**
     *
     * Adds change to the queue of upcoming changes.
     *
     * @param change
     */
    void addUpdateToQueue(IChangeLogItem change);

}
