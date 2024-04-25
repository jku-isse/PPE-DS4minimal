package at.jku.isse.designspace.git.api;

import java.util.List;

public interface IGitCommit {

    String getSha();

    String getRepository();

    String getHTMLUrl();

    String getCommitMessage();

    int getTotalDeletions();

    int getTotalAdditions();

    String[] getBranches();

    List<IGitFile> getFiles();

    IGitUser getAuthor();

    IGitUser getCommitter();

    List<IGitComment> getComments();

    int[] getLinkedIssueKeys();

    String getURI();

}
