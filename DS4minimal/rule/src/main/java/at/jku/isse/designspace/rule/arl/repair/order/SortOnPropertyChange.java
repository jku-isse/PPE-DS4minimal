package at.jku.isse.designspace.rule.arl.repair.order;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.rule.arl.repair.AbstractRepairAction;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;

public class SortOnPropertyChange implements RepairNodeScorer{

	@Override
	public void calculateAndSetScore(RepairNode node, RepairStats rs, String cre) {
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
			if(ra.getElement() instanceof Instance)
			{
				Instance ins=(Instance)ra.getElement();
				PropertyChange_DS prop=new PropertyChange_DS(ra.getProperty(), 0);
				String propU="";
				if(ra.getOperator().toString().equals("Add"))
				{
					propU="Add";
				}
				else if(ra.getOperator().toString().equals("Remove"))
				{
					propU="Remove";
				}
				else
					propU="Set";
				int sc=rs.getPropertyChangeScore(propU, ins.getInstanceType(), prop);
				int totalInstance=ins.getInstanceType().instances().size();
				if(totalInstance==0)
					node.setScore(0.00);
				else
					node.setScore((double)sc/(double)totalInstance);
				//System.out.println(node.getScore());
			}
			break;
		default:
			break;
		}
		
	}

	@Override
	public List<RepairNode> sortChildNodes(List<RepairNode> childNodes) {
		Comparator<RepairNode> comp = Comparator.comparing(RepairNode::getScore,
				(sc1, sc2) -> sc2.compareTo(sc1));
		List<RepairNode> childCollection = childNodes.stream().map(x -> (RepairNode) x).sorted(comp)
				.collect(Collectors.toList());
		return childCollection;
	}

}
