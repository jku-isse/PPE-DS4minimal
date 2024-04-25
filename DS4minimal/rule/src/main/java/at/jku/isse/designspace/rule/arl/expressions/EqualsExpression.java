/**
 * ModelAnalyzerFramework
 * (C) Johannes Kepler University Linz, Austria, 2005-2013
 * Institute for Systems Engineering and Automation (SEA)
 *
 * The software may only be used for academic purposes (teaching, scientific research). Any
 * redistribution or commercialization of the software program and documentation (or any part
 * thereof) requires prior written permission of the JKU. Redistributions of source code must retain
 * the above copyright notice, this list of conditions and the following disclaimer.
 * This software program and documentation are copyrighted by Johannes Kepler University Linz,
 * Austria (the JKU). The software program and documentation are supplied AS IS, without
 * any accompanying services from the JKU. The JKU does not warrant that the operation of the program
 * will be uninterrupted or error-free. The end-user understands that the program was developed for
 * research purposes and is advised not to rely exclusively on the program for any reason.
 *
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR
 * CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS
 * DOCUMENTATION, EVEN IF THE AUTHOR HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. THE AUTHOR
 * SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE AUTHOR HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 * ENHANCEMENTS, OR MODIFICATIONS.
 */
package at.jku.isse.designspace.rule.arl.expressions;

import at.jku.isse.designspace.rule.arl.evaluator.EvaluationNode;
import at.jku.isse.designspace.rule.arl.exception.ParsingException;
import at.jku.isse.designspace.rule.arl.parser.ArlType;
import at.jku.isse.designspace.rule.arl.repair.AlternativeRepairNode;
import at.jku.isse.designspace.rule.arl.repair.Operator;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.arl.repair.RepairSingleValueOption;
import at.jku.isse.designspace.rule.arl.repair.RestrictionNode;
import at.jku.isse.designspace.rule.arl.repair.RestrictionNode.BipartComparatorNode;
import at.jku.isse.designspace.rule.arl.repair.RestrictionNode.ValueNode;

public class EqualsExpression<AT,BT> extends BinaryExpression<Boolean, AT, BT> {

	public EqualsExpression(Expression<AT> a, Expression<BT> b) {
		super(a, b);
		this.resultType = ArlType.BOOLEAN;
		if (!a.resultType.isComparable(b.resultType)) throw new ParsingException("'%s' is not comparable to '%s'", a, b);
	}

	@Override
	public Boolean check(AT argA, BT argB) {
		if (argA != null && argB != null)
			return argA.equals(argB);
		else
			return argA == argB;
	}

	@Override
	public String getARL() { return "EQUALS(" + super.getARL()+")"; }

	@Override
	public String getOriginalARL(int indentation, boolean isOnNewLine) { 
		String whitespace = isOnNewLine ? createWhitespace(indentation) : "";
		return whitespace+this.a.getOriginalARL(indentation, false) + " = " + this.b.getOriginalARL(indentation, false); 
	}

	@Override 
	public String getLocalARL() { return "EQUALS";	}
	
	@Override
	public void generateRepairTree(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {

		RepairNode node = new AlternativeRepairNode(parent);
		Operator op;
		if(expectedValue.getExpectedEvaluationResult())
			op = Operator.MOD_EQ;
		else
			op = Operator.MOD_NEQ;
		evaluationNode.children[0].generateRepairTree(node,  new RepairSingleValueOption(op, evaluationNode.children[1].resultValue));
		evaluationNode.children[1].generateRepairTree(node,  new RepairSingleValueOption(op, evaluationNode.children[0].resultValue));
	}

	@Override
	protected Object explainInternally(EvaluationNode node) {
		if (node.children[0].isMarkedAsOnRepairPath())
			node.incrementRepairGap();
		if (node.children[1].isMarkedAsOnRepairPath())
			node.decrementRepairGap();
		int gap = node.getRepairGap();
		if (gap > 0) // left hand side is repairable
			return "First part can be repaired";
		else if (gap == 0) // both sides are repairable
			return "Either part can be repaired";
		else { // right hand side is repairable
			int absGap = Math.abs(gap);
			return "Second part can be repaired";
		}
	}
	
/*	@Override
	public RestrictionNode generateRestrictions(EvaluationNode evalNode, Expression processedExpr, RestrictionNode prev) {
		EvaluationNode parentEN = getParentIfExpressionMatches(evalNode);
		EvaluationNode evalA=evalNode != null ? evalNode.children[0] : null;
		EvaluationNode evalB=evalNode != null ? evalNode.children[1] : null;
		if(a.equals(processedExpr)) // coming from bottom left
		{	RestrictionNode pR=null;
			if(this.parent.inconsistency_origin!=null)
			{
				pR=parentEN.expression.generateRestrictions(parentEN,this,prev);
			}
			RestrictionNode bNode = b.generateRestrictions(evalB, this);
			if(pR!=null)
			{	
				prev.setNextNode(bNode);
				return new RestrictionNode.AndNode(prev, pR);
			}
			return bNode;
		}
		else if (b.equals(processedExpr)) // coming from bottom right
		{
			RestrictionNode aNode = a.generateRestrictions(evalA, this);
			return aNode;
		}
		else // coming from parent
		{
			setVariable(evalA, evalB);
			RestrictionNode aNode = a.generateRestrictions(evalA, this);			 
			RestrictionNode bNode = b.generateRestrictions(evalB, this);
			RestrictionNode fNode=null;
			if(aNode instanceof ValueNode)
			{
				fNode=new BipartComparatorNode(Operator.MOD_EQ, bNode, aNode);
			}
			else
			{
				fNode=new BipartComparatorNode(Operator.MOD_EQ, aNode, bNode);
			}
			return fNode;
		}
	}*/

	@Override
	public RestrictionNode generateRestrictions(EvaluationNode evalNode, Expression processedExpr) {   
		EvaluationNode parentEN = getParentIfExpressionMatches(evalNode);
		EvaluationNode evalA=evalNode != null ? evalNode.children[0] : null;
		EvaluationNode evalB=evalNode != null ? evalNode.children[1] : null;
		if ( a.equals(processedExpr) ) { // from bottom left
			return this.childOriginatedRestrictions(evalB, evalA, parentEN);
		} else if ( b.equals(processedExpr)) { // from bottom right
			return this.childOriginatedRestrictions(evalA, evalB, parentEN);
		} else { //processedExpr == parent
			// selecting which branch to be kept as variable and which should be turned to value
			setVariable(evalA, evalB);
			RestrictionNode aNode = a.generateRestrictions(evalA, this);			 
			RestrictionNode bNode = b.generateRestrictions(evalB, this);
			RestrictionNode fNode=null;
			if(aNode instanceof ValueNode)
			{
				fNode=new BipartComparatorNode(Operator.MOD_EQ, bNode, aNode);
			}
			else
			{
				fNode=new BipartComparatorNode(Operator.MOD_EQ, aNode, bNode);
			}
			if(parentEN!=null && parentEN.expression instanceof NotExpression && this.parent.restGenerated==0)
			{
				return new RestrictionNode.NotNode(fNode);
			}
			else return fNode;
		}
	}

	@Override
	public RestrictionNode generateRestrictions(Expression processedExpr)
	{
		if(this.a.equals(processedExpr)) // originating from branch a
		{

		}
		else if(this.b.equals(processedExpr)) // originating from branch b
		{

		}
		else // originating from parent
		{
			analyzeExpressions(this.a, this.b,this);
			RestrictionNode aNode = a.generateRestrictions(this);			 
			RestrictionNode bNode = b.generateRestrictions(this);
			if(aNode instanceof ValueNode)
			{
				BipartComparatorNode fNode = new BipartComparatorNode(Operator.MOD_EQ, bNode, aNode);
				return fNode;
			}
			else
			{
				BipartComparatorNode fNode = new BipartComparatorNode(Operator.MOD_EQ, aNode, bNode);
				return fNode;
			}
		}


		return null;
	}

	public RestrictionNode childOriginatedRestrictions(EvaluationNode sibling, EvaluationNode eval_sibling, EvaluationNode parent)
	{
		/*sibling refers to the other branch whereas eval_sibling refers to where the last processed expression is coming from*/
		RestrictionNode sibR=sibling.expression.generateRestrictions(sibling, this);
		RestrictionNode parentR=null;
		RestrictionNode evalSibR=null;
		if(parent!=null)
			parentR=parent.expression.generateRestrictions(parent, this);
		if(!(eval_sibling.expression.getrestGenerated()==1)) // restriction have not been generated
		{
			eval_sibling.setisVariable(true);
			eval_sibling.expression.restGeneratedIncrement();
			evalSibR=eval_sibling.expression.generateRestrictions(eval_sibling, this);
		}
		if(parentR!=null && sibR!=null && evalSibR!=null)
		{
			//TODO
			return null;
		}
		else if(sibR!=null && evalSibR!=null)
		{
			BipartComparatorNode fNode = new BipartComparatorNode(Operator.MOD_EQ, evalSibR, sibR);
			if(parent!=null && parent.expression instanceof NotExpression && this.parent.restGenerated==0)
			{
				return new RestrictionNode.NotNode(fNode);
			}
			return fNode;
		}
		else if(parentR!=null && sibR!=null)
		{
			//TODO:
			if(evalSibR==null && sibR instanceof ValueNode)
			{
				eval_sibling.setisVariable(true);
				evalSibR=eval_sibling.expression.generateRestrictions(eval_sibling, this);
				eval_sibling.expression.restGeneratedIncrement();
				RestrictionNode mainRest=evalSibR;
				evalSibR=evalSibR.getNextNode();
				RestrictionNode child1= new RestrictionNode.BipartComparatorNode(Operator.MOD_EQ, evalSibR, sibR);
				if(parent.expression instanceof AndExpression || parent.expression instanceof IteratorExpression)
					mainRest.setNextNodeFluent(new RestrictionNode.AndNode(child1, parentR));
				else if(parent.expression instanceof OrExpression)
					mainRest.setNextNodeFluent(new RestrictionNode.OrNode(child1, parentR));
				else if(parent.expression instanceof XorExpression)
					mainRest.setNextNodeFluent(new RestrictionNode.XOrNode(child1, parentR));
				else
				{
					//System.out.println("BinaryExpression L:110");
				}
				return mainRest;
			}
			else
			{
				//TODO:Problematic see when this executes
				System.out.println("BinaryExpression L:106 Study in Detail");
				RestrictionNode rest=new RestrictionNode.AndNode(sibR,parentR);
				return rest;
			}
		}
		else if (parentR == null) {
			//TODO
			RestrictionNode opNode = new RestrictionNode.OnlyComparatorNode(Operator.MOD_EQ);
			opNode.setNextNode(sibR);
			if(parent!=null && parent.expression instanceof NotExpression)
			{
				RestrictionNode notNode = new RestrictionNode.NotNode(opNode);		
				return notNode;
			}
			//TODO: study the case in which the if executes.
			if(evalSibR!=null)
			{
				//System.out.println("BinaryExpression L:128 Study in Detail");
				RestrictionNode temp=evalSibR;
				while(evalSibR.getNextNode()!=null)
					evalSibR=evalSibR.getNextNode();
				evalSibR.setNextNode(opNode);
				return temp;
			}
			else
			{
				return opNode;
			}
		}
		else if (sibR != null)
			return parentR;
		else
			return null;
			
	}

	@Override
	public String getPropertySet() {
		String str="";
		for(Expression child : this.children)
		{
			if(child instanceof PropertyCallExpression)
			{
				PropertyCallExpression pce=(PropertyCallExpression) child;
				str=str+(pce.property)+",";
			}
			else if(child instanceof LiteralExpression)
			{
				str=str+((LiteralExpression)child).value;
			}
		}
		return str;
	}

	@Override
	public boolean ispropertySetPresent(String it, Expression prev) {
		// TODO Auto-generated method stub
		return false;
	}

	

}
