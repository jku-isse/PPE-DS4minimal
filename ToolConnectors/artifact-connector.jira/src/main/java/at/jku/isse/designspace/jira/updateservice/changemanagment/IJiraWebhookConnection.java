package at.jku.isse.designspace.jira.updateservice.changemanagment;

import java.util.List;
import java.util.Map;

import at.jku.isse.designspace.jira.updateservice.ChangeLogItem;

public interface IJiraWebhookConnection {

    List<ChangeLogItem> parseChanges(Map<String, Object> webhookUpdate);

}
