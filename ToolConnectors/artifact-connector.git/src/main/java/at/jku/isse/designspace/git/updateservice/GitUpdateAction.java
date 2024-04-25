package at.jku.isse.designspace.git.updateservice;

import at.jku.isse.designspace.artifactconnector.core.updateservice.core.action.UpdateAction;
import at.jku.isse.designspace.artifactconnector.core.updateservice.core.connection.ServiceConnection;
import at.jku.isse.designspace.git.service.GitService;

import java.sql.Timestamp;
import java.util.Map;

public class GitUpdateAction extends UpdateAction<Map<String, Object>> {

    private GitService gitService;
    private Map<String, Object> changeData;

    public GitUpdateAction(Timestamp timestamp, GitService gitService, ActionKind actionKind, ServiceConnection.ServerKind serverKind, Map<String, Object> changeData) {
        super(timestamp, actionKind, serverKind);
        this.changeData = changeData;
        this.gitService = gitService;
    }

    @Override
    public Map<String, Object> getUpdatedValue() {
        return changeData;
    }

    @Override
    public void applyUpdate() {
        if (gitService.getAPI().isPresent()) {
            gitService.getAPI().get().getChangeFactory().createChanges(changeData);
        }
    }

}
