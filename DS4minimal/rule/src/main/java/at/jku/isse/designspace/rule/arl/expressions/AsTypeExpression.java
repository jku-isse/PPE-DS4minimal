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
import at.jku.isse.designspace.rule.arl.exception.EvaluationException;
import at.jku.isse.designspace.rule.arl.parser.ArlType;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.arl.repair.RepairSingleValueOption;
import at.jku.isse.designspace.rule.arl.repair.RestrictionNode;

public class AsTypeExpression<RT> extends BinaryExpression<RT, RT, RT> {

	public AsTypeExpression(Expression<RT> a, Expression<RT> b) {
		super(a, b);
		TypeExpression typeExpression = (TypeExpression) b;
		this.resultType = typeExpression.value;
	}

	@Override
	public RT check(RT argA, RT argB) {
		if (argA != null && !ArlType.get(argA).conformsTo((ArlType) argB))
			throw new EvaluationException(argA+ " cannot be cast as "+argB+" type.");
		return (RT) argA;
	}

	@Override
	public String getARL() { return "ASTYPE(" + super.getARL()+")"; }

	@Override 
	public String getOriginalARL(int indentation, boolean isOnNewLine) { 
		String whitespace = isOnNewLine ? createWhitespace(indentation) : "";
		return this.a.getOriginalARL(indentation, true)+whitespace+"\r\n.asType(" + this.b.getOriginalARL(indentation, false)+")"; }

	@Override 
	public String getLocalARL() { return "ASTYPE";	}

	@Override
	public void generateRepairTree(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
		evaluationNode.children[0].generateRepairTree(parent,expectedValue);
	}
	@Override
	public RestrictionNode generateRestrictions(EvaluationNode evalNode, Expression processedExpr) {
		if(this.a.equals(processedExpr) || this.b.equals(processedExpr))
		{
			if(this.parent.getrestGenerated()==0)
			{
			EvaluationNode parentEN = getParentIfExpressionMatches(evalNode);
			this.parent.restGeneratedIncrement();
			return this.parent.generateRestrictions(parentEN, this);
			}
			else return null;
		}
		else if(this.parent.equals(processedExpr))
		{
			EvaluationNode evalA=evalNode != null ? evalNode.children[0] : null;
			EvaluationNode evalB=evalNode != null ? evalNode.children[1] : null;
			if(evalA.expression.getrestGenerated()==0)
			{
				RestrictionNode restA=evalA.expression.generateRestrictions(evalA, this);
				evalA.expression.restGeneratedIncrement();
				return restA;
			}
			
			//RestrictionNode restB=evalB.expression.generateRestrictions(evalB, this);
			return null; //for now we are assuming only A side will have restriction whereas the other side would only be a type.

		}
		return null;
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
