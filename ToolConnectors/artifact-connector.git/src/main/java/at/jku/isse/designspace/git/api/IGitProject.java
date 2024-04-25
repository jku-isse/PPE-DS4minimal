package at.jku.isse.designspace.git.api;

public interface IGitProject {

    String getName();

    String getBody();

    IGitUser getCreator();

    String getState();

    String getURI();

    String getHTMLUrl();

}
