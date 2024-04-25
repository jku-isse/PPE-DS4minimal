package at.jku.isse.designspace.git.api;


public interface IGitFile {

    String getName();

    String getRepository();

    String getLocalPath();

    /**
     *
     * if this file was returned from a commit,
     * this will return the file with the state
     * after the commit.
     *
     * @return
     */
    String getContent();

    /**
     *
     * This method will return the current state
     * for the choosen branch.
     *
     * @param branch
     * @return
     */
    String getContent(String branch);

    String getFullPath();

}
