package at.jku.isse.designspace.core.controlflow.controlevents;

public class ToolCreationEvent extends ControlEvent {

    private static final StorageEventType TYPE = StorageEventType.TOOL_CREATION;

    protected String toolName;
    protected String version;

    public ToolCreationEvent(String toolName, String version) {
        super(TYPE);
        assert toolName != null && version != null;
        this.toolName = toolName;
        this.version = version;
    }

    public String getToolName() {
        return toolName;
    }

    public String getVersion() {
        return version;
    }

}
