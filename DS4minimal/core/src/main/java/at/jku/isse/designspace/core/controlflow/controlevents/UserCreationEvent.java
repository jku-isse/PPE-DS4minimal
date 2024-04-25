package at.jku.isse.designspace.core.controlflow.controlevents;

public class UserCreationEvent extends ControlEvent {

    private static final StorageEventType TYPE = StorageEventType.USER_CREATION;

    protected String userName;

    public UserCreationEvent(String userName) {
        super(TYPE);
        assert userName != null;
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

}
