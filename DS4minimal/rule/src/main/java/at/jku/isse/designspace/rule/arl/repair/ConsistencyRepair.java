package at.jku.isse.designspace.rule.arl.repair;

import java.util.Collection;

import at.jku.isse.designspace.core.model.Instance;

public class ConsistencyRepair extends AbstractRepair<Instance> {

    public ConsistencyRepair(Collection<RepairAction> repairActions,
                              Object cre) {
        super(repairActions, cre);
    }

    public ConsistencyRepair(Object cre) {
        this.cre = cre;
    }

    @Override
    public int compareTo(Repair o) {
        return this.toString().compareTo(o.toString());
    }



}

