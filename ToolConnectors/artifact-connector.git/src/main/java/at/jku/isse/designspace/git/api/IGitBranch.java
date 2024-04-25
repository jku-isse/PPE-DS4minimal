package at.jku.isse.designspace.git.api;

public interface IGitBranch {

    int getKey();

    String getHeadSha();

    String getOwner();

    String getName();

}
