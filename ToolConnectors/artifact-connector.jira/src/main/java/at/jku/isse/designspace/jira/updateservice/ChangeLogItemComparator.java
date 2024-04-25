package at.jku.isse.designspace.jira.updateservice;

import java.util.Comparator;

public class ChangeLogItemComparator implements Comparator<ChangeLogItem> {

    @Override
    public int compare(ChangeLogItem item1, ChangeLogItem item2) {
        int res = item1.getTimestamp().compareTo(item2.getTimestamp());
        if (res==0) return 1;
        else return res;
    }

}
