package at.jku.isse.designspace.rule.arl.repair.order;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import at.jku.isse.designspace.rule.arl.repair.AbstractRepairAction;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.arl.repair.RestrictionNode;

public class SortOnRestriction implements RepairNodeScorer {
	
	//returns sorted nodes based on scores
	public List<RepairNode> sortChildNodes(List<RepairNode> childNodes) {
		Comparator<RepairNode> comp = Comparator.comparing(RepairNode::getScore,
				(sc1, sc2) -> sc2.compareTo(sc1));
		List<RepairNode> childCollection = childNodes.stream().map(x -> (RepairNode) x).sorted(comp)
				.collect(Collectors.toList());
	//	Collections.sort(node.getChildren(),Comparator.comparing(RepairNode::toString));
		return childNodes;
	}
		
	/*
	 * The function traverses through the repairNode tree and assign each node a
	 * score based on the length of restrictions. Helper of sortRTonRestrictions
	 */
	public void calculateAndSetScore(RepairNode node, RepairStats rs,String cre) {
		switch (node.getNodeType()) {
		case ALTERNATIVE:
			double min = 2147483647; // Largest Integer value that can be stored
			if (node.getChildren().size() > 0) {
				for (RepairNode child : node.getChildren()) {
					calculateAndSetScore(child,rs,cre);
					if (child.getScore() < min)
						min = child.getScore();
				}
			}
			node.setScore(min);
			break;
		case SEQUENCE:
			double sum = 0;
			if (node.getChildren().size() > 0) {
				for (RepairNode child : node.getChildren()) {
					calculateAndSetScore(child,rs,cre);
					sum += (1/child.getScore());
				}
			}
			node.setScore(1/sum);
			break;
		case MULTIVALUE:
		case VALUE:
			
			AbstractRepairAction ra = (AbstractRepairAction) node;
			RestrictionNode rootNode = ra.getRepairValueOption().getRestriction() != null
					? ra.getRepairValueOption().getRestriction().getRootNode()
					: null;
			if (rootNode != null) {
				double score=1.0/(rootNode.getNumberOfRestrictions()+1.0);
				node.setScore(score);
			} else {
				node.setScore(1.0);
			}
			break;
		default: // do nothing
			break;
		}
	}
	
	

}
