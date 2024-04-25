package at.jku.isse.designspace.rule.arl.repair;

import java.util.Set;

public class OperatorRepairTreeFilter extends RepairTreeFilter{
    Set<Operator> operators;

    public OperatorRepairTreeFilter(Set<Operator> operators) {
        this.operators = operators;
    }

    @Override
    public boolean compliesTo(RepairAction repairAction) {
        if (operators.contains(repairAction.getOperator()))
            return false;
        else
            return true;
    }
}
