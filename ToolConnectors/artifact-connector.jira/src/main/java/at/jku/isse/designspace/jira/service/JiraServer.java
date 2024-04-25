package at.jku.isse.designspace.jira.service;

public enum JiraServer {

    CLOUD("cloud"), DRONOLOGY("Dronology"), FREQUENTIS("Frequentis");

    String name;

    JiraServer(String name) {
        this.name = name;
    }

}
