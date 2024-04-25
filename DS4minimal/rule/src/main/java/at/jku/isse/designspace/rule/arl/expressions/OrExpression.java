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
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.arl.repair.RepairSingleValueOption;
import at.jku.isse.designspace.rule.arl.repair.RestrictionNode;
import at.jku.isse.designspace.rule.arl.repair.SequenceRepairNode;

public class OrExpression extends BinaryExpression<Boolean, Boolean, Boolean> {

	public OrExpression(Expression<Boolean> a, Expression<Boolean> b) {
		super(a, b);
		this.resultType = ArlType.BOOLEAN;
		if (a.resultType != ArlType.BOOLEAN || b.resultType != ArlType.BOOLEAN) throw new ParsingException("'or' operation does not have boolean arguments");
	}

	@Override
	public Boolean check(Boolean argA, Boolean argB) {
		return argA || argB;
	}

	@Override
	public String getARL() { return "OR(" + super.getARL()+")"; }

	@Override
	public String getOriginalARL(int indentation, boolean isOnNewLine) {
		String whitespace = createWhitespace(indentation) ;
		String whitespaceStart = isOnNewLine ? whitespace : "";
		return String.format("%s( %s \r\n"
				+ "%sor \r\n"
				+ "%s)"
				,whitespaceStart, this.a.getOriginalARL(indentation+2, false), whitespace, this.b.getOriginalARL(indentation+2, true));
	}
	
	@Override 
	public String getLocalARL() { return "OR"; }

//	@Override
//	public EvaluationNode evaluate(HashSet scopeElements) {
//		EvaluationNode aNode = this.a.evaluate(scopeElements);
//		if ((Boolean)aNode.resultValue) return new EvaluationNode(this, true, scopeElements, aNode);
//		EvaluationNode bNode = this.b.evaluate(scopeElements);
//		return new EvaluationNode(this, bNode.resultValue, aNode, bNode);
//	}

	@Override
	public void generateRepairTree(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {


		boolean aValue = (Boolean)evaluationNode.children[0].resultValue;
		if(evaluationNode.children.length<2) {
			try {
				EvaluationNode bNode = this.b.evaluate(evaluationNode.scopeElements);
				evaluationNode.addNode(bNode);
			} catch (Exception ex) {
				ex.printStackTrace();

			}
		}
		boolean bValue = (Boolean)evaluationNode.children[1].resultValue;
		// repair a OR b
		if(expectedValue.getExpectedEvaluationResult()){
			if(!aValue && !bValue) {
				RepairNode node = new AlternativeRepairNode(parent);
				evaluationNode.children[0].generateRepairTree(node, expectedValue);
				evaluationNode.children[1].generateRepairTree(node, expectedValue);
			}
		}
		// repair a
		else {
			RepairNode node = new SequenceRepairNode(parent);
			if(aValue && !bValue)
				evaluationNode.children[0].generateRepairTree(node, expectedValue);
			// repair b
			else if(!expectedValue.getExpectedEvaluationResult() && !aValue && bValue)
				evaluationNode.children[1].generateRepairTree(node, expectedValue);
			// repair a AND b
			else if(!expectedValue.getExpectedEvaluationResult() && aValue && bValue) {
				evaluationNode.children[0].generateRepairTree(node, expectedValue);
				evaluationNode.children[1].generateRepairTree(node, expectedValue);

			}

		}

	}
	
	@Override
	protected Object explainInternally(EvaluationNode node) {
		if (node.children[0].isMarkedAsOnRepairPath())
			node.incrementRepairGap();
		if (node.children[1].isMarkedAsOnRepairPath())
			node.decrementRepairGap();
		int gap = node.getRepairGap();
		if (gap > 0) // left hand side is repairable
			return "First part needs repair";
		else if (gap == 0) // both sides are repairable
			return "Either part or both can be repaired";
		else { // right hand side is repairable
			int absGap = Math.abs(gap);
			return "Second part needs repair";
		}
	}
	
	@Override
	public RestrictionNode generateRestrictions(Expression processedExpr) {
		if(this.a.equals(processedExpr)) // coming from branch a
		{
			//TODO: let's see what to do
			return null;
		}
		else if(this.b.equals(processedExpr)) // coming from branch b
		{
			//TODO
			return null;
		}
		else if(parent==null || parent instanceof RootExpression)
		{
			return null;
		}
		else
		{
			RestrictionNode.OrNode orNode = new RestrictionNode.OrNode(this.a.generateRestrictions(this), this.b.generateRestrictions(this));
			return orNode;
		}
		
	}
	
	@Override
	public RestrictionNode generateRestrictions(EvaluationNode evalNode, Expression processedExpr) {    	
		if(this.a.equals(processedExpr)) // Coming from right branch; check if left contains any restriction on element in context
		{ 
			//if(this.b.toString().contains(this.origin.expression.toString())) // if yes, then generate restriction
			if(this.b.inconsistency_origin!=null)
			{
				RestrictionNode aR=b.generateRestrictions(evalNode != null ? evalNode.children[1] : null, this);
				return aR;
			}
			return null;
		}
		else if(this.b.equals(processedExpr)) // Coming from left branch; check if right contains any restriction on element in context
		{
			//if(this.a.toString().contains(this.origin.expression.toString())) // if yes, then generate restriction
			if(this.a.inconsistency_origin!=null)
			{
				RestrictionNode bR=a.generateRestrictions(evalNode != null ? evalNode.children[0] : null, this);
				return bR;
			}
			return null;
		}
		
		else if (parent == null || parent instanceof RootExpression)
			return null; // if this is the top most evaluation, then its not a restriction but subject to repair


		EvaluationNode evalA = evalNode != null ? evalNode.children[0] : null;;
		EvaluationNode evalB = evalNode != null ? evalNode.children[1] : null;;

		// we need to distinguish where we come from,

		// if from A or B, then nothing
		if (( evalA != null && processedExpr == evalA.expression) || ( evalB != null && processedExpr == evalB.expression))
			return null;
		else { 		// from top, then both A and B
			
			RestrictionNode.OrNode orNode = new RestrictionNode.OrNode(this.a.generateRestrictions(evalA, this), this.b.generateRestrictions(evalB, this));
			return orNode;
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
