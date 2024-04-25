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

import java.util.HashSet;
import java.util.Objects;

import at.jku.isse.designspace.rule.arl.evaluator.EvaluationNode;
import at.jku.isse.designspace.rule.arl.exception.ParsingException;
import at.jku.isse.designspace.rule.arl.parser.ArlType;
import at.jku.isse.designspace.rule.arl.repair.RepairNode;
import at.jku.isse.designspace.rule.arl.repair.RepairSingleValueOption;
import at.jku.isse.designspace.rule.arl.repair.RestrictionNode;

public class LiteralExpression<RT> extends Expression<RT>  {

	RT value;

	public LiteralExpression(RT v) {
		super();
		this.value = v;
		if (v==null)
			this.resultType = ArlType.NULL;
		else if (v instanceof Long)
			this.resultType = ArlType.INTEGER;
		else if (v instanceof Double)
			this.resultType = ArlType.REAL;
		else if (v instanceof String)
			this.resultType = ArlType.STRING;
		else if (v instanceof Boolean)
			this.resultType = ArlType.BOOLEAN;
		else
			throw new ParsingException("Literal type not supported: "+value);
	}

	@Override
	public EvaluationNode evaluate(HashSet scopeElements)  {
		return new EvaluationNode(this, value);
	}

	@Override
	public RT evaluate(Expression<?> child) {
		return null;
	}

	@Override
	public String getARL() {
		if (this.value instanceof String)
			return "LITERAL('" + this.value + "')";
		else
			return "LITERAL(" + this.value + ")";
	}

	@Override
	public String getOriginalARL(int indentation, boolean isOnNewLine) {
		String whitespace = isOnNewLine ? createWhitespace(indentation) : "";
		if (this.value instanceof String)
			return whitespace +"'" + this.value + "'";
		else
			return whitespace + this.value.toString();
	}
	
	@Override 
	public String getLocalARL() { return Objects.toString(this.value);	}
	
	@Override
	public void generateRepairTree(RepairNode parent, String property, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
 		//new ExecutableRepairAction(parent, property, evaluationNode, expectedValue);
	}

	@Override
	public void generateRepairTree(RepairNode parent, RepairSingleValueOption expectedValue, EvaluationNode evaluationNode) {
		//new ExecutableRepairAction(parent, null, evaluationNode, expectedValue);
	}
	
	@Override
	public RestrictionNode generateRestrictions(Expression processedExpr) {
		String valueStr =  this.value instanceof String ? "'" + this.value + "'" : this.value.toString();
		this.restGeneratedIncrement();
		return new RestrictionNode.ValueNode(valueStr);
	}
	
	@Override
	public RestrictionNode generateRestrictions(EvaluationNode evalNode, Expression processedExpr) {
		String valueStr =  this.value instanceof String ? "'" + this.value + "'" : this.value.toString();
		this.restGeneratedIncrement();
		return new RestrictionNode.ValueNode(valueStr); 
	}
}
