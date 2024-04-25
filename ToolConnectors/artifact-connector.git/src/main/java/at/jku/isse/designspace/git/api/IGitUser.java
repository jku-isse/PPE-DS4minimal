package at.jku.isse.designspace.git.api;

public interface IGitUser {

    String getUserId();

    String getName();

    String getEmail();

    String getURI();

    String getLocation();

    String getBio();

    String getCompany();

    String getHTMLUrl();

    String getType();

    int getPublicRepoCount();

    int getPrivateRepoCount();

}
