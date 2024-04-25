package at.jku.isse.designspace.git.api.github;

public enum GithubTypes {

    PULL_REQUEST("pull_request"), ISSUE("issue"), COMMIT("commit"),
    COMMENT("comment"), PROJECT("project"), PROJECT_CARD("project_card"),
    BRANCH("branch"), USER("user"), REPOSITORY("repository");

    private String name;

    GithubTypes(String name) {
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

}
