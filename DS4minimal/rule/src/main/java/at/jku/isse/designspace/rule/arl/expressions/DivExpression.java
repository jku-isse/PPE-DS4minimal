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
import at.jku.isse.designspace.rule.arl.exception.ParsingException;
import at.jku.isse.designspace.rule.arl.parser.ArlType;
import at.jku.isse.designspace.rule.arl.repair.AlternativeRepairNode;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.arl.repair.RepairSingleValueOption;

public class DivExpression<RT extends Number> extends BinaryExpression<RT, RT, RT> {

	public DivExpression(Expression<RT> a, Expression<RT> b) {
		super(a, b);
		this.resultType = a.resultType;
		if (!a.resultType.conformsTo(ArlType.NUMBER)) throw new ParsingException("'%s' is not a number", a);
		if (!b.resultType.conformsTo(ArlType.NUMBER)) throw new ParsingException("'%s' is not a number", b);
	}

	@Override
	public RT check(RT a, RT b) {
		if (a instanceof Double)
			return (RT) Double.valueOf(a.doubleValue() / b.doubleValue());
		else if (a instanceof Long)
			return (RT) Long.valueOf(a.longValue() / b.longValue());
		else
			throw new EvaluationException("Unsupported division operation on types "+a.toString()+" and "+b.toString());
	}

	@Override
	public String getARL() { return "DIV(" + super.getARL()+")"; }
	
	@Override
	public String getOriginalARL(int indentation, boolean isOnNewLine) { 
		String whitespace = createWhitespace(indentation);
		return whitespace+this.a.getOriginalARL(indentation, false) + " / " + this.b.getOriginalARL(indentation, false); }

	@Override 
	public String getLocalARL() { return "DIV";	}
	
	@Override
	public void generateRepairTree(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {

		RepairNode node = new AlternativeRepairNode(parent);
		Number resultA = (Number) expectedValue.getValue();
		Number resultB = (Number) expectedValue.getValue();
		if(resultA instanceof Long) {
			resultA = resultA.longValue() * (Long) evaluationNode.children[1].resultValue;
			resultB = resultB.longValue() * (Long) evaluationNode.children[0].resultValue;
		}else
		if(resultA instanceof Double) {
			resultA = resultA.doubleValue() * (Double) evaluationNode.children[1].resultValue;
			resultB = resultB.doubleValue() * (Double) evaluationNode.children[0].resultValue;
		}
		evaluationNode.children[0].generateRepairTree(node, new RepairSingleValueOption(expectedValue.operator, resultA));
		evaluationNode.children[1].generateRepairTree(node,  new RepairSingleValueOption(expectedValue.operator, resultB));

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
