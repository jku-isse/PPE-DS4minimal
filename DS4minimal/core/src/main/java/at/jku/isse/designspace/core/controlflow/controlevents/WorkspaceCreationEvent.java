package at.jku.isse.designspace.core.controlflow.controlevents;

import at.jku.isse.designspace.core.model.Workspace;

public class WorkspaceCreationEvent extends ControlEvent {

    public static final StorageEventType TYPE = StorageEventType.WORKSPACE_CREATION;

    private long workspaceId;
    private String workspaceName;

    private long parentWorkspaceId = -1;

    private boolean autoUpdate;
    private boolean autoCommit;

    private long userId = -1;
    private long toolId = -1;

    public WorkspaceCreationEvent(Workspace workspace) {
        super(TYPE);
        assert workspace != null;

        this.workspaceId = workspace.id();
        this.workspaceName = workspace.name();
        this.autoUpdate = workspace.isAutoUpdate();
        this.autoCommit = workspace.isAutoCommit();

        if (workspace.parent() != null) {
            this.parentWorkspaceId = workspace.parent().id();
        }

        if (workspace.user() != null) {
            this.userId = workspace.user().id();
        }

        if (workspace.tool() != null) {
            this.toolId = workspace.tool().id();
        }
    }

    public long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public long getParentWorkspaceId() {
        return parentWorkspaceId;
    }

    public void setParentWorkspaceId(long parentWorkspaceId) {
        this.parentWorkspaceId = parentWorkspaceId;
    }

    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    public void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getToolId() {
        return toolId;
    }

    public void setToolId(long toolId) {
        this.toolId = toolId;
    }

}
