package at.jku.isse.designspace.core.trees.collaboration;


public class CollaborationSetting {
    public boolean overwriteParent;

    public boolean merge;

    public CollaborationSetting(boolean overwriteParent, boolean merge){
        this.overwriteParent = overwriteParent;
        this.merge = merge;
    }

    static public CollaborationSetting DEFAULT = new CollaborationSetting(false, true);
}
