package at.jku.isse.designspace.rule.arl.repair.order;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import at.jku.isse.designspace.rule.arl.repair.AbstractRepairAction;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;

public class SortOnRepairPercentage implements RepairNodeScorer{
	
	@Override
	public List<RepairNode> sortChildNodes(List<RepairNode> childNodes) {
		Comparator<RepairNode> comp = Comparator.comparing(RepairNode::getScore,
				(sc1, sc2) -> sc2.compareTo(sc1));
		List<RepairNode> childCollection = childNodes.stream().map(x -> (RepairNode) x).sorted(comp)
				.collect(Collectors.toList());
		return childCollection;
		
	}
	@Override
	public void calculateAndSetScore(RepairNode node, RepairStats rs,String cre) {
		switch (node.getNodeType()) {
		case ALTERNATIVE:
			double maxA = 0;
			if (node.getChildren().size() > 0) {
				for (RepairNode child : node.getChildren()) {
					calculateAndSetScore(child,rs,cre);
					if (child.getScore() > maxA)
						maxA = child.getScore();
				}
			}
			node.setScore(maxA);
			break;
		case SEQUENCE:
			double min = 2147483647;
			if (node.getChildren().size() > 0) {
				for (RepairNode child : node.getChildren()) {
					calculateAndSetScore(child,rs,cre);
					if (child.getScore() < min) {
						min = child.getScore();
					}
				}
			}
			node.setScore(min);
			break;
		case MULTIVALUE: // follow through
		case VALUE:
			AbstractRepairAction ra = (AbstractRepairAction) node;
			double countS = rs.getRTSelectScore(ra, cre);
			double countUS=rs.getRTUnSelectScore(ra, cre);
			if(countS==0 && countUS==0)
				node.setScore(0.0);
			else
				node.setScore((countS/(countS+countUS))*100.00);
			break;
		default:
			break;
		}
	}


	
	


	

}
