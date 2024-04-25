package at.jku.isse.designspace.rule.arl.repair.order;

import java.util.List;

import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.arl.repair.RepairNode.Type;

public class RepairTreeRanker {
	
	RepairNodeScorer scorer;
	public RepairTreeRanker(RepairNodeScorer scorer) {
		this.scorer = scorer;
	}
	public void assignRanks(RepairNode node, int max_rank)
	{
		//Root Node
		if(node.getParent()==null)
			node.setRank(max_rank);
		else
		{
			if(node.getParent().getNodeType()==Type.SEQUENCE)
				node.setRank(node.getParent().getRank());
			else
				node.setRank(max_rank);
		}
		switch(node.getNodeType())
		{
		case ALTERNATIVE:
			if(node.getChildren().size()>0)
			{
				List<RepairNode> sorted = scorer.sortChildNodes(node.getChildren());
				for (RepairNode child : sorted) {
					assignRanks(child, max_rank);
					int curr_rank=this.getMaxRank(child, 1);
					if(curr_rank== max_rank)
					{
						max_rank+=1;
					}
					else if(curr_rank>max_rank)
					{
						max_rank=curr_rank+1;
					}
				}
			}
			break;
		case SEQUENCE:
			if(node.getChildren().size()>0)
			{
				List<RepairNode> sorted = scorer.sortChildNodes(node.getChildren());
				for (RepairNode child : sorted) {
					assignRanks(child, max_rank);
				}
			}
			break;
		default: // do nothing
			break;
		}
	}
	
	
	public int getMaxRank(RepairNode rn,int ret) {
		int curr=rn.getRank();
		if(ret<curr)
			ret=curr;
		for(RepairNode child: rn.getChildren())
		{
			int childRank=getMaxRank(child,ret);
			if(childRank>ret)
				ret=childRank;
		}
		return ret;
	}

}
