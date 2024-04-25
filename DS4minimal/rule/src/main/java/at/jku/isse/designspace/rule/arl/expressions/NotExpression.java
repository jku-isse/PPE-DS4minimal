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
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.arl.repair.RepairSingleValueOption;
import at.jku.isse.designspace.rule.arl.repair.RestrictionNode;
import at.jku.isse.designspace.rule.arl.repair.SequenceRepairNode;

public class NotExpression extends UnaryExpression<Boolean, Boolean> {

	public NotExpression(Expression<Boolean> a) {
		super(a);
		this.resultType = ArlType.BOOLEAN;
		if (a.resultType != ArlType.BOOLEAN) throw new ParsingException("'not' does not have a boolean arguments");
	}

	@Override
	public Boolean check(Boolean argA) { return !argA; }

	@Override
	public String getARL() {
		return "NOT(" + super.getARL()+")";
	}
	
	@Override
	public String getOriginalARL(int indentation, boolean isOnNewLine) {
    	String whitespace = isOnNewLine ? createWhitespace(indentation) : "";
		return whitespace+"not(" + super.getOriginalARL(indentation, isOnNewLine)+")";
	}
	
	@Override 
	public String getLocalARL() { return "NOT";	}

	@Override
	public void generateRepairTree(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {

		RepairNode node = new SequenceRepairNode(parent);
		boolean	value = !expectedValue.getExpectedEvaluationResult();
		evaluationNode.children[0].generateRepairTree(node, new RepairSingleValueOption(expectedValue.operator, expectedValue.getValue(),value));


	}
	
	@Override
	public RestrictionNode generateRestrictions(EvaluationNode evalNode, Expression processedExpr) {
		if (parent != null && !parent.equals(processedExpr) /*&& !(parent instanceof RootExpression)*/)
		{
			RestrictionNode pR= parent.generateRestrictions(evalNode != null ? evalNode.parentEvalNode : null, this);
			return pR;
		}
		else {
			EvaluationNode nextEvalNode = evalNode != null ? evalNode.children[0] : null;
			this.restGeneratedIncrement();
			RestrictionNode notNode = new RestrictionNode.NotNode(a.generateRestrictions(nextEvalNode, this));			
			return notNode;
		}
	}
	@Override
	public RestrictionNode generateRestrictions(Expression processedExpr)
	{
		if(parent!=null && parent.equals(processedExpr)) // originating from parent
		{
			RestrictionNode restA= new RestrictionNode.NotNode(this.a.generateRestrictions(this));
			return restA;
		}
		else // originating from child
		{
			
		}
		return null;
	}
}
