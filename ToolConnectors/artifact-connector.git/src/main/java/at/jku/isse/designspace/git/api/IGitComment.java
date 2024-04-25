package at.jku.isse.designspace.git.api;

public interface IGitComment {

    int getKey();

    String getBody();

    IGitUser getAuthor();

    String getURI();

    String getHTMLUrl();

}
