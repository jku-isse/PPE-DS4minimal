package at.jku.isse.designspace.rule.arl.repair;

public class ContextualizedPositiveSideEffect<E> extends SideEffect<E> {

	private RepairAction matchingRepair;
	
	public ContextualizedPositiveSideEffect(E inconsistency, Type type, RepairAction matchingRepair) {
		super(inconsistency, type);		
		this.matchingRepair = matchingRepair;
	}

	public ContextualizedPositiveSideEffect(E inconsistency, int type, RepairAction matchingRepair) {
		super(inconsistency, type);	
		this.matchingRepair = matchingRepair;
	}	
	
	public RepairAction getMatchingRepair() {
		return matchingRepair;
	}
	
}
