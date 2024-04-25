package at.jku.isse.designspace.jira.updateservice;

import java.sql.Timestamp;

public interface IChangeLogItem {

    String getId();

    String getCorrespondingArtifactId();

    Timestamp getTimestamp();

}
