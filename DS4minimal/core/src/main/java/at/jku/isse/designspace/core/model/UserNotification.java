package at.jku.isse.designspace.core.model;

public class UserNotification {
    public String text = "";
    public boolean isSpotted = false;
    public UserNotification(String text) {
        this.text = text;
    }
}
