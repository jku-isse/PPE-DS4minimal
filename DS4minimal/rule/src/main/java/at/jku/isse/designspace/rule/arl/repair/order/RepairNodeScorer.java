package at.jku.isse.designspace.rule.arl.repair.order;

import java.util.List;

import at.jku.isse.designspace.rule.arl.repair.RepairNode;

public interface RepairNodeScorer {
	public void calculateAndSetScore(RepairNode node, RepairStats rs,String cre);
	public List<RepairNode> sortChildNodes(List<RepairNode> childNodes) ;
}
	