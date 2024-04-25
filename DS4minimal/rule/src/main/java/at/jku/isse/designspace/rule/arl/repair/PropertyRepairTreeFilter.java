package at.jku.isse.designspace.rule.arl.repair;

import java.util.Set;

public class PropertyRepairTreeFilter extends RepairTreeFilter{
    Set<String> properties;

    public PropertyRepairTreeFilter(Set<String> properties) {
        this.properties = properties;
    }

    @Override
    public boolean compliesTo(RepairAction repairAction) {
        if (properties.contains(repairAction.getProperty()))
            return false;
        else
            return true;
    }
}
