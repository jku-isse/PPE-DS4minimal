package at.jku.isse.designspace.rule.arl.repair.order;

import java.util.List;

import at.jku.isse.designspace.rule.arl.repair.RepairNode;

public class NoSort implements RepairNodeScorer{

	@Override
	public void calculateAndSetScore(RepairNode node, RepairStats rs,String cre) {
		node.setScore(0);
		for (RepairNode child : node.getChildren()) {
			calculateAndSetScore(child, rs, cre);
		}
	}
	@Override
	public List<RepairNode> sortChildNodes(List<RepairNode> childNodes) {
		//List<RepairNode> childCollection = childNodes.stream().collect(Collectors.toList());
		return childNodes;
	}

}