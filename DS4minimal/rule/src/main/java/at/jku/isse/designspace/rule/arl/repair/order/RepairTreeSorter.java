package at.jku.isse.designspace.rule.arl.repair.order;

import java.util.Collections;
import java.util.Comparator;

import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.checker.ConsistencyUtils;

public class RepairTreeSorter {
	
	RepairStats rs;
	RepairNodeScorer scorer;
	public RepairTreeSorter(RepairStats rs_, RepairNodeScorer scorer)
	{
		rs=rs_;
		this.scorer = scorer;
	}
	RepairTreeRanker rtRank;
	
	public void updateTreeOnScores(RepairNode rn,String cre)
	{
		rtRank=new RepairTreeRanker(this.scorer);
		//ConsistencyUtils.printRepairTree(rn);
		this.scorer.calculateAndSetScore(rn, rs,cre);
		this.sortTree(rn, 1);
		rtRank.assignRanks(rn, 1);
		ConsistencyUtils.printRepairTree(rn);
	}
	
	public void sortTree(RepairNode node, int position) {
		/*String treeLevel = "";
		for (int i = 0; i < position; i++)
			treeLevel = treeLevel.concat(" -- ");
		RuleService.logger.warn(treeLevel.concat(node.toString())+"  ==>S= "+node.getScore()+" ==> R="+node.getRank());*/
		//List<RepairNode> sorted = scorer.sortChildNodes(node.getChildren());
		Collections.sort(node.getChildren(),Comparator.comparing(RepairNode::getScore).reversed());
		for (RepairNode child : node.getChildren()) {
			sortTree(child, position + 1);
		}
	}

	public int getMaxRank(RepairNode rn) {
		return rtRank.getMaxRank(rn,0);
		
	}

}
