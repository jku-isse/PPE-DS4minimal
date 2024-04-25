package at.jku.isse.designspace.jira.service;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.core.model.Workspace;
import at.jku.isse.designspace.jira.updateservice.ChangeLogItem;
import at.jku.isse.designspace.jira.updateservice.IChangeLogItem;
import at.jku.isse.designspace.jira.updateservice.IChangeLogItemFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HistoryManager implements IHistoryManager {

    private final static String FIELD_NAME_ARTIFACT_CREATION_ITEM = "ARTIFACT_CREATION";

    private final IArtifactPusher jiraArtifactPusher;
    private final InstanceType schema;
    private final Workspace workspace;
    private final PriorityQueue<ChangeBatch> upcomingChanges;
    private final IChangeLogItemFactory changeLogItemFactory;

    @Autowired
    private IJiraService jiraService;

    public HistoryManager(IArtifactPusher artifactPusher, IChangeLogItemFactory changeLogItemFactory, InstanceType instanceType, Workspace workspace) {
        this.jiraArtifactPusher = artifactPusher;
        this.changeLogItemFactory = changeLogItemFactory;
        this.schema = instanceType;
        this.workspace = workspace;

        this.upcomingChanges = new PriorityQueue<>((batch1, batch2) -> {
            int res = batch1.getTimestamp().compareTo(batch2.getTimestamp());
            if (res == 0) return 1;
            else return res;
        });
    }

    @Override
    public void pushEntireDatabaseToDesignspace() {
        //ToDo: Implement JSON Replayer or use a separate Workspace for the replay
        /*IReplayableSession<Artifact> replayableSession = timeTravelingConnector.getSessionForEntireDatabase();
        replayableSession.oldest();
        replayableSessionCache = replayableSession.getAllSessionArtifacts();

        //old version --> creation of artifacts is not a changeLogItem
        /*replayableSession.getAllSessionArtifacts().values().stream().
                filter(artifact -> !artifact.getId().contains("_")).
                forEach(artifact -> jiraArtifactPusher.pushArtifact(artifact, schema.getRoot()));


        //new version --> creation of artifacts is a changeLogItem

        List<IChangeLogItem> artifactCreationChangeLogItems = new ArrayList<>();
        for (Artifact artifact : replayableSession.getAllSessionArtifacts().values().stream().
                filter(artifact -> !artifact.getId().contains("_")).collect(Collectors.toList())) {

            Object created = artifact.getProperties().get("created");
            if (created != null) {
                ChangeLogItem changeLogItem = new PropertyChangeLogItem();
                changeLogItem.setArtifactId(artifact.getId());
                changeLogItem.setCorrespondingArtifactId(artifact.getId());
                changeLogItem.setField(FIELD_NAME_ARTIFACT_CREATION_ITEM);
                changeLogItem.setTimeCreated(reformatTime(created.toString()));
                changeLogItem.setCorrespondingArtifactIdInSource(artifact.getIdInSource());
                artifactCreationChangeLogItems.add(changeLogItem);
            }

        }*/
    }

    @Override
    public void queueEntireDatabaseToDesignspace() {
        //ToDo: Implement JSON Replayer or use a separate Workspace for the replay
        /*
        IReplayableSession<Artifact> replayableSession = timeTravelingConnector.getSessionForEntireDatabase();
        replayableSession.oldest();
        replayableSessionCache = replayableSession.getAllSessionArtifacts();

        //old version --> creation of artifacts is not a changeLogItem
        /*replayableSession.getAllSessionArtifacts().values().stream().
                filter(artifact -> !artifact.getId().contains("_")).
                forEach(artifact -> jiraArtifactPusher.pushArtifact(artifact, schema.getRoot()));


        //new version --> creation of artifacts is a changeLogItem

        List<IChangeLogItem> artifactCreationChangeLogItems = new ArrayList<>();
        for (Artifact artifact : replayableSession.getAllSessionArtifacts().values().stream().
                filter(artifact -> !artifact.getId().contains("_")).collect(Collectors.toList())) {

            Object created = artifact.getProperties().get("created");
            if (created != null) {
                ChangeLogItem changeLogItem = new PropertyChangeLogItem();
                changeLogItem.setArtifactId(artifact.getId());
                changeLogItem.setCorrespondingArtifactId(artifact.getId());
                changeLogItem.setField(FIELD_NAME_ARTIFACT_CREATION_ITEM);
                changeLogItem.setTimeCreated(reformatTime(created.toString()));
                changeLogItem.setCorrespondingArtifactIdInSource(artifact.getIdInSource());
                artifactCreationChangeLogItems.add(changeLogItem);
            }

        }

        addUpdatesToQueue(replayableSession.getSortedHistory());
        addUpdatesToQueue(artifactCreationChangeLogItems);
        */
    }

    private String reformatTime(String timeCreated) {
        String modifiedTime = timeCreated.replace('T', ' ');
        modifiedTime = modifiedTime.substring(0, modifiedTime.indexOf('+'));
        return modifiedTime;
    }

    @Override
    public void queueArtifactWithHistory(String key, InstanceType schema) {
        //ToDo: Implement JSON Replayer or use a separate Workspace for the replay
        /*
        try {
            IReplayableSession<Artifact> replayableSession = timeTravelingConnector.getSession(1, key);
            replayableSession.oldest();
            Optional<Artifact> artifact_ = replayableSession.getArtifact(key);
            if (artifact_.isPresent()) {
                jiraArtifactPusher.pushArtifact(artifact_.get(), schema);
            }
            addUpdatesToQueue(replayableSession.getSortedHistory());
        } catch (RestClientException re) {
            log.debug("Jira Service : The artifact with provided key " + key + " cannot be fetched!");
        }*/
    }

    @Override
    public void pushArtifactWithHistory(String key, InstanceType schema) {
        //ToDo: Implement JSON Replayer or use a separate Workspace for the replay
        /*
        try {
            IReplayableSession<Artifact> replayableSession = timeTravelingConnector.getSession(1, key);
            replayableSession.oldest();
            Optional<Artifact> artifact_ = replayableSession.getArtifact(key);
            if (artifact_.isPresent()) {
                jiraArtifactPusher.pushArtifact(artifact_.get(), schema);
            }
            applyUpdates(replayableSession.getSortedHistory());
        } catch (RestClientException re) {
            log.debug("Jira Service : The artifact with provided key cannot be fetched!");
        }
        */
    }


    @Override
    public void queueArtifactWithHistory(String id) {
        //ToDo: Implement JSON Replayer or use a separate Workspace for the replay
    }


    @Override
    public Set<IChangeLogItem> peekNextChanges() {
        if (!this.upcomingChanges.isEmpty()) {
            return this.upcomingChanges.peek().getChanges();
        }
        return null;
    }

    @Override
    public Timestamp peekNextTimeStamp() {
        if (!this.upcomingChanges.isEmpty()) {
            return this.upcomingChanges.peek().getTimestamp();
        }
        return null;
    }

    @Override
    public void commitChangesForNextTimeStamp() {
        if (!this.upcomingChanges.isEmpty()) {
            this.upcomingChanges.poll().getChanges().forEach(change -> applyUpdate((ChangeLogItem) change));
            this.workspace.concludeTransaction();
        }
    }

    @Override
    public void pushArtifactWithHistory(String key) {
        pushArtifactWithHistory(key, schema);
    }

    @Override
    public void addUpdatesToQueue(Collection<IChangeLogItem> changes) {
        Timestamp currentTimestamp, lastTimestamp = null;
        Set<IChangeLogItem> currentBatchSet = new HashSet<>();

        for (IChangeLogItem changeLogItem : changes) {
            currentTimestamp = changeLogItem.getTimestamp();
            if (lastTimestamp == null) {
                currentBatchSet.add(changeLogItem);
            } else if (lastTimestamp.equals(currentTimestamp)) {
                currentBatchSet.add(changeLogItem);
            } else {
                addToExistingOrCreateNewBatch(lastTimestamp, currentBatchSet);
                currentBatchSet = new HashSet<>();
                currentBatchSet.add(changeLogItem);
            }
            lastTimestamp = currentTimestamp;
        }

        if (!currentBatchSet.isEmpty()) {
            addToExistingOrCreateNewBatch(lastTimestamp, currentBatchSet);
        }
    }

    @Override
    public void addUpdateToQueue(IChangeLogItem change) {
        HashSet<IChangeLogItem> changes = new HashSet<>();
        addToExistingOrCreateNewBatch(change.getTimestamp(), changes);
    }

    private void addToExistingOrCreateNewBatch(Timestamp timestamp, Set<IChangeLogItem> changeLogItems) {
        Optional<ChangeBatch> batch = this.upcomingChanges.stream().filter(change -> change.getTimestamp().equals(timestamp)).findAny();
        if (batch.isPresent()) {
            batch.get().getChanges().addAll(changeLogItems);
        } else {
            this.upcomingChanges.add(new ChangeBatch(timestamp, changeLogItems));
        }
    }

    @Override
    public int applyUpdates(Collection<IChangeLogItem> changes) {

        int updatesPerformed = 0;

        for(IChangeLogItem change : changes) {
            if(applyUpdate((ChangeLogItem) change)) {
                updatesPerformed ++;
            }
        }

        return updatesPerformed;

    }

    @Override
    public boolean applyUpdate(ChangeLogItem change) {
        try {
            Optional<Instance> instance = this.jiraArtifactPusher.findArtifact(change.getArtifactId());
            if (instance.isPresent()) {
                change.applyChange(instance.get(), this.jiraService);
                return true;
            }
        } catch (Exception e) {
            log.debug("JIRA_SERVICE: Change " + change.getId() + " was not applied, because of the following excpetion: \n " + e.getMessage());
        }
        return false;
    }

}