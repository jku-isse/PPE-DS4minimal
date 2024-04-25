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

public class GTEExpression<AT,BT> extends BinaryExpression<Boolean, AT, BT> {

	public GTEExpression(Expression<AT> a, Expression<BT> b) {
		super(a, b);
		this.resultType = ArlType.BOOLEAN;
		if (!a.resultType.isComparable(b.resultType)) throw new ParsingException("'%s' is not comparable to '%s'", a, b);
	}

	@Override
	public Boolean check(AT argA, BT argB) {
		if (argA==null)
			return false;
		else if (argA instanceof Long)
			return ((Long)argA).compareTo((Long)argB) >= 0;
			else if (argA instanceof Double)
				return ((Double)argA).compareTo((Double)argB) >= 0;
				else
					return ((Comparable)argA).compareTo(argB) >= 0;
	}

	@Override
	public String getARL() {
		return "GTE(" + super.getARL()+")";
	}

	@Override
	public String getOriginalARL(int indentation, boolean isOnNewLine) { 
		String whitespace = isOnNewLine ? createWhitespace(indentation) : "";
		return whitespace+this.a.getOriginalARL(indentation, false) + " >= " + this.b.getOriginalARL(indentation, false); 
	}

	@Override 
	public String getLocalARL() { return "GTE";	}

	@Override
	public void generateRepairTree(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode)  {
		RepairSingleValueOption aValueOption = new RepairSingleValueOption(Operator.MOD_GT,
				evaluationNode.children[0].resultValue);
		RepairSingleValueOption bValueOption = new RepairSingleValueOption(Operator.MOD_LT,
				evaluationNode.children[1].resultValue);

		RepairNode node = new AlternativeRepairNode(parent);
		// if a >= b is expected then generates repair tree with (a > b, a = b) or (b < a, b = a)
		if (expectedValue.getExpectedEvaluationResult()) {
			// repair a
			for (Object option : bValueOption.invert()) {
				evaluationNode.children[0].generateRepairTree(node,  (RepairSingleValueOption) option);
			}
			// repair b
			for (Object option : aValueOption.invert()) {
				evaluationNode.children[1].generateRepairTree(node,  (RepairSingleValueOption) option);
			}

		} else {
			// else a >= b is true bue false is expected. Then repair should be either (b > a) or (a < b)
			evaluationNode.children[0].generateRepairTree(node,  bValueOption);
			evaluationNode.children[1].generateRepairTree(node,  aValueOption);


		}
	}

	@Override
	public RestrictionNode generateRestrictions(EvaluationNode evalNode, Expression processedExpr) {    			 
		if (parent == null || parent instanceof RootExpression)
			return null; // if this is the top most evaluation, then its not a restriction but subject to repair		 
		EvaluationNode evalA = evalNode != null ? evalNode.children[0] : null;
		EvaluationNode evalB = evalNode != null ? evalNode.children[1] : null;
		if ( a.equals(processedExpr) ) { // from bottom left
			RestrictionNode bR = b.generateRestrictions(evalB, this);
			RestrictionNode parentR = parent.generateRestrictions(evalNode != null ? evalNode.parentEvalNode : null, this);
			if (parentR != null && bR != null)
				return new RestrictionNode.AndNode(bR, parentR); //FIXME this ANDNode is not correct
			else if (parentR == null) {
				RestrictionNode opNode = new RestrictionNode.OnlyComparatorNode(Operator.MOD_GT);
				opNode.setNextNode(bR);
				return opNode;
			} else if (bR != null)
				return parentR;
			else return null;
		} else if ( b.equals(processedExpr)) { // from bottom right
			RestrictionNode aR = a.generateRestrictions(evalA, this);
			RestrictionNode parentR = parent.generateRestrictions(evalNode != null ? evalNode.parentEvalNode : null, this);
			if (parentR != null && aR != null)
				return new RestrictionNode.AndNode(aR, parentR); //FIXME this ANDNode is not correct
			else if (parentR == null) {
				RestrictionNode opNode = new RestrictionNode.OnlyComparatorNode(Operator.MOD_GT);
				opNode.setNextNode(aR);
				return opNode;
			} else if (aR != null)
				return parentR;
			else return null;
			// return parent.generateRestrictions(evalNode != null ? evalNode.parentEvalNode : null, this);
		} else { //processedExpr == parent
			setVariable(evalA, evalB);
			RestrictionNode aNode = a.generateRestrictions(evalA, this);			 
			RestrictionNode bNode = b.generateRestrictions(evalB, this);
			if(aNode instanceof ValueNode)
			{
				BipartComparatorNode f1Node = new BipartComparatorNode(Operator.MOD_GT, bNode, aNode);
				BipartComparatorNode f2Node = new BipartComparatorNode(Operator.MOD_EQ, bNode, aNode);
				RestrictionNode.OrNode orNode = new RestrictionNode.OrNode(f1Node,f2Node);
				return orNode;
			}
			else
			{
				BipartComparatorNode f1Node = new BipartComparatorNode(Operator.MOD_GT, aNode, bNode);
				BipartComparatorNode f2Node = new BipartComparatorNode(Operator.MOD_EQ, aNode, bNode);
				RestrictionNode.OrNode orNode = new RestrictionNode.OrNode(f1Node,f2Node);
				return orNode;
			}
		}
	}

	@Override
	public String getPropertySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean ispropertySetPresent(String it, Expression prev) {
		// TODO Auto-generated method stub
		return false;
	}


}
