package at.jku.isse.designspace.rule.arl.repair;

import at.jku.isse.designspace.rule.arl.evaluator.EvaluationNode;
import at.jku.isse.designspace.rule.arl.expressions.Expression;
import lombok.Data;


@Data
public class RepairRestriction {
	final Expression expressionToExecuteOn;
	final EvaluationNode evalNode;	
	final Expression prevExpression;
	
	private RestrictionNode rootNode = null;
	
	public RestrictionNode getRootNode() {
		if (rootNode == null) {
			printExpressionTree(expressionToExecuteOn);
			rootNode = expressionToExecuteOn.generateRestrictions(evalNode, prevExpression);
		}
		return rootNode;
	}
	// Helper Functions for debugging Only.
	public void printExpressionTree(Expression eNode)
	{
		Expression rNode=getRootExp(eNode);
		rNode.resetInconsistencyData();
		evalNode.expression.setInconsistency_Origin(evalNode);
		printExpTree(rNode,1);
		System.out.println();
	}
	public void printExpTree(Expression node, int position)
	{
		node.setrestGenerated(0);
		node.setOrigin(evalNode);		
		node.setValue(false);
		String treeLevel = "";
		for (int i = 0; i < position; i++)
			treeLevel = treeLevel.concat(" -- ");
		System.out.println(treeLevel.concat(node.toString()));
		for(int i=0;i<node.getChildren().size();i++)
		{
			printExpTree((Expression)node.getChildren().get(i),position+1);
		}
		
	}
	public Expression getRootExp(Expression eNode)
	{
		if(eNode.getParent()!=null)
			eNode=getRootExp(eNode.getParent());
		return eNode;
	}
	public void printEvalTree(EvaluationNode eNode)
	{
		EvaluationNode rNode=getRootEval(eNode);
		printTree(rNode,1);
		System.out.println();
	}
	public void printTree(EvaluationNode node, int position)
	{
		String treeLevel = "";
		for (int i = 0; i < position; i++)
			treeLevel = treeLevel.concat(" -- ");
		System.out.println(treeLevel.concat(node.toString()));
		for (EvaluationNode child : node.children) {
			printTree(child, position + 1);
		}
	}
	public EvaluationNode getRootEval(EvaluationNode eNode)
	{
		if(eNode.parentEvalNode!=null)
			eNode=getRootEval(eNode.parentEvalNode);
		return eNode;
	}
	// End here
	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RepairRestriction other = (RepairRestriction) obj;
		if (evalNode == null) {
			if (other.evalNode != null)
				return false;
		} else if (!evalNode.equals(other.evalNode))
			return false;
		if (expressionToExecuteOn == null) {
			if (other.expressionToExecuteOn != null)
				return false;
		} else if (!expressionToExecuteOn.equals(other.expressionToExecuteOn))
			return false;		
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((evalNode == null) ? 0 : evalNode.hashCode());
		result = prime * result + ((expressionToExecuteOn == null) ? 0 : expressionToExecuteOn.hashCode());		
		return result;
	}
}
