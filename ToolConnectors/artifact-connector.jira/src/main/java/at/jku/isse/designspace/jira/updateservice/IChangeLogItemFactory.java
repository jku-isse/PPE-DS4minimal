package at.jku.isse.designspace.jira.updateservice;

import java.util.ArrayList;
import java.util.Map;

public interface IChangeLogItemFactory {

    ArrayList<ChangeLogItem> createChangeLog(Map<String, Object> rawChangeLog, String issueId, String issueKey);

}
