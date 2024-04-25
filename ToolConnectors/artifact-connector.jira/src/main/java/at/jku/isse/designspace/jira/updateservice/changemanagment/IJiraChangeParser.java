package at.jku.isse.designspace.jira.updateservice.changemanagment;

import java.util.Map;

public interface IJiraChangeParser {

    void createChanges(Map<String, Object> webhookChange);

    void addChangeLogItemListener();

    void addIssueCreationEventListener();

    void addIssueLInkCreationEventListener();

    void addCommentCreationEventListener();

}
