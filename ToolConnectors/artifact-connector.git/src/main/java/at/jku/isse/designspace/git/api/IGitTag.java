package at.jku.isse.designspace.git.api;

public interface IGitTag {

    String getName();

    String getCommit();

    String getSourceCodeZipUrl();

    String getSourceCodeTarUrl();

}
